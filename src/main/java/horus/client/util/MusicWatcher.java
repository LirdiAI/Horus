package horus.client.util;

import horus.client.HorusClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Слушает системные медиа (Windows SMTC): любой плеер — Spotify, YouTube Music в браузере, VK и т.д.
 * Отдаёт название трека, исполнителя и обложку (iTunes) для DynamicIsland.
 */
public final class MusicWatcher {
   private static final String POWER_SHELL_SCRIPT = """
      $ErrorActionPreference = 'SilentlyContinue'
      [Console]::OutputEncoding = [Text.Encoding]::UTF8
      Add-Type -AssemblyName System.Runtime.WindowsRuntime
      $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]
      $asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
      $asTaskAction = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncAction' })[0]
      function Await($WinRtTask, $ResultType) {
         $asTask = $asTaskGeneric.MakeGenericMethod($ResultType)
         $netTask = $asTask.Invoke($null, @($WinRtTask))
         $netTask.Wait(-1) | Out-Null
         $netTask.Result
      }
      function AwaitAction($WinRtAction) {
         $netTask = $asTaskAction.Invoke($null, @($WinRtAction))
         $netTask.Wait(-1) | Out-Null
      }
      $manager = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
      $cmdFile = Join-Path $env:TEMP 'horus_media_cmd.txt'
      $lastKey = ''
      $lastStatus = ''
      $lastPos = -1
      while ($true) {
         try {
            $session = $manager.GetSessions() | Select-Object -First 1
            if ($session -ne $null) {
               $status = [string]$session.GetPlaybackInfo().PlaybackStatus
               if ($status -ne $lastStatus) {
                  Write-Output ('STATUS=' + $status)
                  $lastStatus = $status
               }
               $props = Await $session.TryGetMediaPropertiesAsync() ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
               $key = $props.Title + '|' + $props.Artist
               if ($key -ne $lastKey) {
                  Write-Output ('TITLE=' + $props.Title)
                  Write-Output ('ARTIST=' + $props.Artist)
                  Write-Output 'EOL'
                  $lastKey = $key
               }
               $timeline = $session.GetTimelineProperties()
               $pos = [math]::Round($timeline.Position.TotalSeconds, 1)
               if ($pos -ne $lastPos) {
                  Write-Output ('POS=' + $pos + ' END=' + [math]::Round($timeline.EndTime.TotalSeconds, 1))
                  $lastPos = $pos
               }
               if (Test-Path $cmdFile) {
                  $cmd = (Get-Content $cmdFile -Raw).Trim().ToUpper()
                  Remove-Item $cmdFile -Force -ErrorAction SilentlyContinue
                  if ($cmd -eq 'PLAY') { AwaitAction ($session.TryPlayAsync()) }
                  elseif ($cmd -eq 'PAUSE') { AwaitAction ($session.TryPauseAsync()) }
                  elseif ($cmd -eq 'NEXT') { AwaitAction ($session.TrySkipNextAsync()) }
                  elseif ($cmd -eq 'PREV') { AwaitAction ($session.TrySkipPreviousAsync()) }
               }
            } else {
               if ($lastStatus -ne '') {
                  Write-Output 'STATUS=None'
                  $lastStatus = ''
               }
               $lastKey = ''
            }
         } catch {
         }
         Start-Sleep -Milliseconds 700
      }
      """;;

   private static final AtomicBoolean started = new AtomicBoolean();
   private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6L)).build();
   private static final ExecutorService coverExecutor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "horus-music-cover");
      thread.setDaemon(true);
      return thread;
   });
   private static final Map<String, Identifier> coverIds = new HashMap<>();

   private static volatile String title = "";
   private static volatile String artist = "";
   private static volatile boolean playing;
   private static volatile String coverKey = "";
   private static volatile byte[] pendingCover;
   private static volatile boolean coverPending;
   private static volatile float positionSeconds;
   private static volatile float durationSeconds;
   private static volatile long positionMillis;

   /** Отправляет команду системному плееру: PLAY / PAUSE / NEXT / PREV. */
   public static void sendCommand(String command) {
      try {
         Path commandPath = Path.of(System.getProperty("java.io.tmpdir"), "horus_media_cmd.txt");
         Files.writeString(commandPath, command + "\n", StandardCharsets.UTF_8);
      } catch (IOException ignored) {
      }
   }

   /** Текущая позиция трека в секундах (с локальной интерполяцией между опросами). */
   public static float getPlayPosition() {
      float interpolated = positionSeconds + (playing ? (System.currentTimeMillis() - positionMillis) / 1000.0F : 0.0F);
      float duration = durationSeconds;
      return duration > 1.0F ? Math.min(interpolated, duration) : interpolated;
   }

   public static float getDuration() {
      return durationSeconds;
   }

   private MusicWatcher() {
   }

   public static void start() {
      if (started.compareAndSet(false, true)) {
         Thread thread = new Thread(MusicWatcher::run, "horus-music");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public static boolean isPlaying() {
      return playing;
   }

   public static String getTitle() {
      return title;
   }

   public static String getArtist() {
      return artist;
   }

   /** Identifier зарегистрированной обложки или null, если ещё грузится/не найдена. */
   public static Identifier getCover() {
      String key = coverKey;
      return key.isEmpty() ? null : coverIds.get(key);
   }

   private static void run() {
      Process process = null;

      try {
         Path scriptPath = Path.of(System.getProperty("java.io.tmpdir"), "horus_media_watch.ps1");
         Files.writeString(scriptPath, POWER_SHELL_SCRIPT, StandardCharsets.UTF_8);
         ProcessBuilder processBuilder = new ProcessBuilder(
            "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", scriptPath.toAbsolutePath().toString()
         );
         processBuilder.redirectErrorStream(false);
         process = processBuilder.start();

         StringBuilder titleBuilder = new StringBuilder();
         StringBuilder artistBuilder = new StringBuilder();
         boolean listening = false;

         try (var reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
         )) {
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.startsWith("TITLE=")) {
                  titleBuilder.setLength(0);
                  titleBuilder.append(line.substring(6));
               } else if (line.startsWith("ARTIST=")) {
                  artistBuilder.setLength(0);
                  artistBuilder.append(line.substring(7));
               } else if (line.startsWith("STATUS=")) {
                  String status = line.substring(7).trim();
                  playing = status.equals("Playing");
               } else if (line.startsWith("POS=")) {
                  int spaceIndex = line.indexOf(' ', 4);
                  if (spaceIndex > 4) {
                     try {
                        positionSeconds = Float.parseFloat(line.substring(4, spaceIndex));
                        String end = line.substring(spaceIndex + 1);
                        if (end.startsWith("END=")) {
                           durationSeconds = Float.parseFloat(end.substring(4));
                        }
                     } catch (NumberFormatException ignored) {
                     }
                     positionMillis = System.currentTimeMillis();
                  }
               } else if (line.equals("EOL")) {
                  listening = false;
                  applyTrack(titleBuilder.toString(), artistBuilder.toString());
               } else if (line.equals("NONE")) {
                  playing = false;
                  listening = false;
               }
            }
         }
      } catch (Throwable throwable) {
         // PowerShell/SMTC недоступен — просто нет музыки в острове
      } finally {
         if (process != null) {
            process.destroy();
         }
      }
   }

   private static void applyTrack(String newTitle, String newArtist) {
      String key = (newTitle.trim() + "|" + newArtist.trim()).toLowerCase(java.util.Locale.ROOT);
      title = newTitle.trim();
      artist = newArtist.trim();
      positionSeconds = 0.0F;
      positionMillis = System.currentTimeMillis();

      if (!key.equals(coverKey)) {
         coverKey = key;
         pendingCover = null;
         coverPending = false;
         if (!coverIds.containsKey(key)) {
            requestCover(key, title, artist);
         }
      }
   }

   private static void requestCover(String key, String trackTitle, String trackArtist) {
      coverExecutor.execute(() -> {
         try {
            String term = URLEncoder.encode(trackArtist + " " + trackTitle, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(
               URI.create("https://itunes.apple.com/search?media=music&entity=song&limit=1&term=" + term)
            ).timeout(Duration.ofSeconds(8L)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = response.body();
            String coverUrl = findJsonString(body, "artworkUrl100");
            if (coverUrl == null) {
               return;
            }

            String big = coverUrl.replace("100x100bb", "600x600bb");
            HttpRequest imageRequest = HttpRequest.newBuilder(URI.create(big)).timeout(Duration.ofSeconds(10L)).build();
            HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = imageResponse.body();
            if (bytes != null && bytes.length > 100) {
               pendingCover = bytes;
               coverPending = true;
               MinecraftClient.getInstance().execute(MusicWatcher::buildCoverTexture);
            }
         } catch (Throwable ignored) {
            // нет интернета/не найден трек — остров покажет логотип
         }
      });
   }

   private static String findJsonString(String body, String field) {
      if (body == null) {
         return null;
      }

      String needle = "\"" + field + "\":\"";
      int index = body.indexOf(needle);
      if (index < 0) {
         return null;
      }

      int start = index + needle.length();
      int end = body.indexOf('"', start);
      return end > start ? body.substring(start, end).replace("\\u0026", "&") : null;
   }

   /** Вызывается на главном потоке: декодирует скачанную обложку в текстуру. */
   private static void buildCoverTexture() {
      if (!coverPending || pendingCover == null) {
         return;
      }

      byte[] bytes = pendingCover;
      pendingCover = null;
      coverPending = false;
      String key = coverKey;

      try {
         BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
         if (bufferedImage == null) {
            return;
         }

         int width = bufferedImage.getWidth();
         int height = bufferedImage.getHeight();
         NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> "horus_media_cover", width, height, false);
         NativeImage image = texture.getImage();

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               image.setColorArgb(x, y, bufferedImage.getRGB(x, y));
            }
         }

         texture.upload();
         Identifier identifier = HorusClient.id("images/media_cover");
         MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
         coverIds.put(key, identifier);
      } catch (Throwable ignored) {
      }
   }
}
