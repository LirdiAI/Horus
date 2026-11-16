/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestion
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.ChatHud
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  net.minecraft.client.gui.widget.TextFieldWidget
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.client.util.math.Rect2i
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Util
 *  org.joml.Matrix3x2fStack
 */
package horus.client.module.render;

import com.mojang.brigadier.suggestion.Suggestion;
import horus.client.module.Module;
import horus.client.module.ModuleCategory;
import horus.client.module.ModuleInfo;
import horus.client.module.render.ChatTab;
import horus.client.module.render.SmoothFeatureModule;
import horus.client.render.Render2D;
import horus.client.render.SoftGlowDrawData;
import horus.client.setting.BooleanSetting;
import horus.client.setting.ColorSetting;
import horus.client.setting.LegacyNumberSetting;
import horus.client.ui.ClickGuiStyle;
import horus.client.ui.hud.NotificationHud;
import horus.client.util.Colors;
import horus.mixin.ChatHudMixin;
import horus.mixin.TextFieldWidgetAccessor;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

@ModuleInfo(name="CustomChat", description="\u0428\u0435\u0439\u0434\u0435\u0440\u043d\u044b\u0439 \u0447\u0430\u0442: \u0435\u0434\u0438\u043d\u0430\u044f \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u0430\u044f \u043f\u0430\u043d\u0435\u043b\u044c, \u043f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0441\u0441\u044b\u043b\u043e\u043a \u0438 \u043c\u0435\u043d\u044e \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0439.", category=ModuleCategory.RENDER)
public final class CustomChatModule
extends Module {
    private static CustomChatModule instance;
    public final BooleanSetting blur = this.addSetting(new BooleanSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435 (\u0411\u043b\u044e\u0440)", "\u0428\u0435\u0439\u0434\u0435\u0440\u043d\u043e\u0435 \u0440\u0430\u0437\u043c\u044b\u0442\u0438\u0435 \u0438\u0433\u0440\u043e\u0432\u043e\u0433\u043e \u043c\u0438\u0440\u0430 (Kawase Blur) \u0437\u0430 \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u043e\u0439 \u043f\u0430\u043d\u0435\u043b\u044c\u044e.", true));
    public final BooleanSetting glow = this.addSetting(new BooleanSetting("\u041d\u0435\u043e\u043d\u043e\u0432\u043e\u0435 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435", "\u041c\u044f\u0433\u043a\u043e\u0435 \u043e\u0431\u044a\u0435\u043c\u043d\u043e\u0435 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435 (Soft Glow) \u043f\u043e \u043a\u043e\u043d\u0442\u0443\u0440\u0443 \u043f\u0430\u043d\u0435\u043b\u0438.", true));
    public final BooleanSetting animatedInput = this.addSetting(new BooleanSetting("\u0410\u043d\u0438\u043c\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0439 \u0432\u0432\u043e\u0434", "\u041f\u043b\u0430\u0432\u043d\u0430\u044f \u043a\u0430\u0440\u0435\u0442\u043a\u0430, \u0440\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0435 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u0438 \u044d\u0444\u0444\u0435\u043a\u0442\u044b \u0432\u0432\u043e\u0434\u0430.", true));
    public final BooleanSetting modeBadge = this.addSetting(new BooleanSetting("\u0411\u0435\u0439\u0434\u0436 \u0440\u0435\u0436\u0438\u043c\u0430", "\u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0442\u044c \u0431\u0435\u0439\u0434\u0436 \u0440\u0435\u0436\u0438\u043c\u0430 (\u0427\u0410\u0422, \u041a\u041e\u041c\u0410\u041d\u0414\u0410 \u0438 \u0434\u0440.) \u0432 \u0441\u0442\u0440\u043e\u043a\u0435 \u0432\u0432\u043e\u0434\u0430.", false));
    public final BooleanSetting tabs = new BooleanSetting("\u0412\u043a\u043b\u0430\u0434\u043a\u0438 \u0447\u0430\u0442\u043e\u0432", "\u0412\u043a\u043b\u0430\u0434\u043a\u0438", false);
    public final BooleanSetting linkHighlight = this.addSetting(new BooleanSetting("\u041f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0441\u0441\u044b\u043b\u043e\u043a", "\u0412\u044b\u0434\u0435\u043b\u044f\u0442\u044c \u043a\u043b\u0438\u043a\u0430\u0431\u0435\u043b\u044c\u043d\u044b\u0435 \u0441\u0441\u044b\u043b\u043a\u0438 \u0433\u043e\u043b\u0443\u0431\u044b\u043c \u0446\u0432\u0435\u0442\u043e\u043c \u0441 \u043f\u043e\u0434\u0447\u0435\u0440\u043a\u0438\u0432\u0430\u043d\u0438\u0435\u043c.", true));
    public final BooleanSetting customColor = this.addSetting(new BooleanSetting("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0441\u0432\u043e\u0439 \u0446\u0432\u0435\u0442 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f \u0432\u043c\u0435\u0441\u0442\u043e \u0442\u0435\u043c\u044b \u043a\u043b\u0438\u0435\u043d\u0442\u0430.", false));
    public final ColorSetting color = this.addSetting(new ColorSetting("\u0426\u0432\u0435\u0442", "\u0426\u0432\u0435\u0442 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f \u043f\u0430\u043d\u0435\u043b\u0438.").setValue(-1));
    public final BooleanSetting antispam = this.addSetting(new BooleanSetting("\u0410\u043d\u0442\u0438\u0441\u043f\u0430\u043c", "\u041e\u0431\u044a\u0435\u0434\u0438\u043d\u044f\u0442\u044c \u043e\u0434\u0438\u043d\u0430\u043a\u043e\u0432\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f (x2, x3).", true));
    public final BooleanSetting smooth = this.addSetting(new BooleanSetting("\u041f\u043b\u0430\u0432\u043d\u0430\u044f \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f", "\u041f\u043b\u0430\u0432\u043d\u043e\u0435 \u043f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u0438 \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u0435 \u0432\u044b\u0441\u043e\u0442\u044b \u043f\u0430\u043d\u0435\u043b\u0438.", true));
    public final BooleanSetting mentionHighlight = this.addSetting(new BooleanSetting("\u041f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u043d\u0438\u043a\u0430", "\u0412\u044b\u0434\u0435\u043b\u044f\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0441 \u0442\u0432\u043e\u0438\u043c \u043d\u0438\u043a\u043e\u043c \u0437\u043e\u043b\u043e\u0442\u0438\u0441\u0442\u044b\u043c \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u0435\u043c.", true));
    public final BooleanSetting smoothSlide = this.addSetting(new BooleanSetting("\u041f\u043b\u0430\u0432\u043d\u044b\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f", "\u041f\u043b\u0430\u0432\u043d\u044b\u0439 \u0432\u044b\u0435\u0437\u0434 \u0438 \u043f\u043e\u044f\u0432\u043b\u0435\u043d\u0438\u0435 \u043d\u043e\u0432\u044b\u0445 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0439.", true));
    public final BooleanSetting timestamps = this.addSetting(new BooleanSetting("\u0422\u0430\u0439\u043c\u0448\u0442\u0430\u043c\u043f\u044b", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u0432\u0440\u0435\u043c\u044f \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0438 [\u0427\u0427:\u041c\u041c] \u043f\u0435\u0440\u0435\u0434 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f\u043c\u0438.", true));
    public final BooleanSetting jumpToBottom = this.addSetting(new BooleanSetting("\u041a\u043d\u043e\u043f\u043a\u0430 \u0432\u043d\u0438\u0437", "\u041a\u043d\u043e\u043f\u043a\u0430 \u0431\u044b\u0441\u0442\u0440\u043e\u0439 \u043f\u0440\u043e\u043a\u0440\u0443\u0442\u043a\u0438 \u0432\u043d\u0438\u0437 \u043f\u0440\u0438 \u0447\u0442\u0435\u043d\u0438\u0438 \u0438\u0441\u0442\u043e\u0440\u0438\u0438.", true));
    public final BooleanSetting chatSearch = new BooleanSetting("\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u0447\u0430\u0442\u0443", "\u041f\u043e\u0438\u0441\u043a", false);
    public final LegacyNumberSetting radius = this.addSetting(new LegacyNumberSetting("\u0421\u043a\u0440\u0443\u0433\u043b\u0435\u043d\u0438\u0435", "\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u043a\u0440\u0443\u0433\u043b\u0435\u043d\u0438\u044f \u0443\u0433\u043b\u043e\u0432 \u0441\u0442\u0435\u043a\u043b\u044f\u043d\u043d\u043e\u0439 \u043f\u0430\u043d\u0435\u043b\u0438.", 4.5, 0.0, 10.0, 0.5));
    private ChatTab currentTab = ChatTab.ALL;
    private final Map<ChatTab, Integer> unreadCounts = new ConcurrentHashMap<ChatTab, Integer>();
    private final List<TabBounds> tabBoundsList = new ArrayList<TabBounds>();
    private final Set<String> mutedPlayers = ConcurrentHashMap.newKeySet();
    private long lastMessageTime = 0L;
    private boolean smoothSlideMatrixPushed = false;
    private int unreadScrolledCount = 0;
    private float jumpBtnX = 0.0f;
    private float jumpBtnY = 0.0f;
    private float jumpBtnSize = 22.0f;
    private boolean isSearchOpen = false;
    private boolean searchFocused = false;
    private String searchQuery = "";
    private float searchBtnX = 0.0f;
    private float searchBtnY = 0.0f;
    private float searchBtnW = 16.0f;
    private float searchBtnH = 15.0f;
    private float searchCloseBtnX = 0.0f;
    private float searchCloseBtnY = 0.0f;
    private float searchCloseBtnSize = 12.0f;
    private float animatedCursorX = 0.0f;
    private int lastTextLength = 0;
    private int lastCursor = 0;
    private float typingIntensity = 0.0f;
    private float counterScale = 1.0f;
    private float animatedFill = 0.0f;
    private long lastFrameTime = 0L;
    private float animatedHeight = 0.0f;
    private float animatedAlpha = 0.0f;
    private float lastPanelX = 2.0f;
    private float lastPanelY = 0.0f;
    private float lastPanelW = 320.0f;
    private float lastPanelH = 140.0f;
    private String lastSpamText = "";
    private int spamCount = 1;
    private boolean hasRecentMention = false;
    private long lastMentionTime = 0L;
    private boolean isPlayerPopupActive = false;
    private String hoveredPlayerNick = null;
    private String playerRank = "\u0418\u0433\u0440\u043e\u043a";
    private float popupX = 0.0f;
    private float popupY = 0.0f;
    private final float popupW = 176.0f;
    private final float popupH = 96.0f;
    private long lastHoverTime = 0L;
    private float popupAlpha = 0.0f;
    private float popupScale = 0.85f;
    private String hoveredUrl = null;
    private long urlHoverStartTime = 0L;
    private boolean isUrlPopupActive = false;
    private float urlPopupX = 0.0f;
    private float urlPopupY = 0.0f;
    private final float urlPopupW = 160.0f;
    private final float urlPopupH = 46.0f;
    private float urlPopupAlpha = 0.0f;
    public static final Pattern URL_PATTERN;
    private static final Pattern VANILLA_PLAYER_MSG;
    private static final Pattern PLAYER_MSG_PATTERN;
    private static final Pattern PM_ARROW_PATTERN;
    private static final String[] SERVER_TAGS;
    private static final Set<String> STOP_WORDS;

    private CustomChatModule() {
        this.color.withVisibility(this.customColor::isChecked);
    }

    public static CustomChatModule getInstance() {
        if (instance == null) {
            instance = new CustomChatModule();
        }
        return instance;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public int getAccentColor() {
        if (this.customColor.isChecked()) {
            return this.color.getValue();
        }
        return ClickGuiStyle.intVal;
    }

    public ChatTab getCurrentTab() {
        return this.currentTab;
    }

    public void setTab(ChatTab tab) {
        if (this.currentTab != tab) {
            this.currentTab = tab;
            this.unreadCounts.put(tab, 0);
            if (CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null) {
                ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).invokeRefreshTrimmedMessages();
            }
        }
    }

    public void resetHoveredElements() {
        this.hoveredPlayerNick = null;
        this.isPlayerPopupActive = false;
        this.popupAlpha = 0.0f;
        this.popupScale = 0.85f;
        this.playerRank = "\u0418\u0433\u0440\u043e\u043a";
        this.hoveredUrl = null;
        this.isUrlPopupActive = false;
        this.urlPopupAlpha = 0.0f;
    }

    public void openPlayerPopup(String nick, String rank) {
        if (nick == null || nick.isBlank()) {
            return;
        }
        this.hoveredPlayerNick = nick;
        this.playerRank = rank != null && !rank.isBlank() ? rank : "\u0418\u0433\u0440\u043e\u043a";
        this.isPlayerPopupActive = true;
        this.popupAlpha = 0.0f;
        this.popupScale = 0.82f;
        this.lastHoverTime = System.currentTimeMillis();
    }

    public void closePlayerPopup() {
        this.isPlayerPopupActive = false;
    }

    public String extractRank(String nick, String fullLine) {
        String rank;
        int lastOpen;
        String prefixPart;
        int lastClose;
        int nickIdx;
        Text prefix;
        PlayerListEntry entry;
        if (mc.getNetworkHandler() != null && nick != null && (entry = mc.getNetworkHandler().getPlayerListEntry(nick)) != null && entry.getScoreboardTeam() != null && (prefix = entry.getScoreboardTeam().getPrefix()) != null && !prefix.getString().isBlank()) {
            String pStr = Formatting.strip((String)prefix.getString()).trim();
            if (pStr.startsWith("[") && pStr.endsWith("]")) {
                pStr = pStr.substring(1, pStr.length() - 1).trim();
            }
            if (!pStr.isBlank()) {
                return pStr;
            }
        }
        if (fullLine != null && nick != null && (nickIdx = fullLine.indexOf(nick)) > 0 && (lastClose = (prefixPart = fullLine.substring(0, nickIdx).trim()).lastIndexOf(93)) != -1 && (lastOpen = prefixPart.lastIndexOf(91, lastClose)) != -1 && lastClose > lastOpen + 1 && !(rank = prefixPart.substring(lastOpen + 1, lastClose).trim()).isBlank() && rank.length() <= 16) {
            return rank;
        }
        return "\u0418\u0433\u0440\u043e\u043a";
    }

    public String getClickedUrlAt(double mouseX, double mouseY) {
        if (!this.isEnabled() || !this.linkHighlight.isChecked() || CustomChatModule.mc.inGameHud == null || CustomChatModule.mc.inGameHud.getChatHud() == null || CustomChatModule.mc.textRenderer == null) {
            return null;
        }
        ChatHud chatHud = CustomChatModule.mc.inGameHud.getChatHud();
        double chatScale = CustomChatModule.mc.options != null ? (Double)CustomChatModule.mc.options.getChatScale().getValue() : 1.0;
        int chatWidth = CustomChatModule.mc.options != null ? ChatHud.getWidth((double)((Double)CustomChatModule.mc.options.getChatWidth().getValue())) : 320;
        int screenHeight = mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 400;
        double relX = (mouseX - 4.0) / chatScale;
        double relY = ((double)(screenHeight - 40) - mouseY) / chatScale;
        if (relX >= 0.0 && relX <= (double)chatWidth && relY >= 0.0) {
            int lineIdxFromBottom = (int)(relY / 9.0);
            int scrolledLines = ((ChatHudMixin)chatHud).chatScrollbarPos();
            int lineIndex = lineIdxFromBottom + scrolledLines;
            List<ChatHudLine.Visible> visibleLines = ((ChatHudMixin)chatHud).visibleMessages();
            if (visibleLines != null && lineIndex >= 0 && lineIndex < visibleLines.size()) {
                ChatHudLine.Visible line = visibleLines.get(lineIndex);
                ArrayList<CharInfo> charList = new ArrayList<>();
                float[] curX = new float[]{0.0f};
                line.content().accept((idx, style, cp) -> {
                    float cw = CustomChatModule.mc.textRenderer.getWidth(Character.toString(cp));
                    charList.add(new CharInfo(cp, curX[0], curX[0] + cw));
                    curX[0] = curX[0] + cw;
                    return true;
                });
                int charIndex = -1;
                for (int i = 0; i < charList.size(); ++i) {
                    CharInfo ci = (CharInfo)charList.get(i);
                    if (!(relX >= (double)ci.startX) || !(relX <= (double)ci.endX)) continue;
                    charIndex = i;
                    break;
                }
                if (charIndex != -1) {
                    StringBuilder sb = new StringBuilder();
                    for (CharInfo ci : charList) {
                        sb.appendCodePoint(ci.codePoint);
                    }
                    String fullLine = sb.toString();
                    Matcher urlMatcher = URL_PATTERN.matcher(fullLine);
                    while (urlMatcher.find()) {
                        int uStart = urlMatcher.start();
                        int uEnd = urlMatcher.end();
                        if (charIndex < uStart || charIndex >= uEnd) continue;
                        return urlMatcher.group();
                    }
                }
            }
        }
        return null;
    }

    public Text formatIncomingMessage(Text text) {
        String raw;
        if (!this.isEnabled()) {
            return text;
        }
        Text result = text;
        if (this.timestamps.isChecked() && !(raw = text.getString().trim()).isEmpty() && !raw.startsWith("[") && !raw.matches("^\\[\\d{1,2}:\\d{2}\\].*")) {
            String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            MutableText prefix = Text.literal((String)("[" + timeStr + "] ")).styled(s -> s.withColor(Formatting.GRAY));
            result = Text.empty().append((Text)prefix).append(result);
        }
        if (this.linkHighlight.isChecked()) {
            result = this.formatLinksInText(result);
        }
        return result;
    }

    public Text formatLinksInText(Text text) {
        if (!this.linkHighlight.isChecked()) {
            return text;
        }
        String raw = text.getString();
        if (raw == null || raw.isBlank()) {
            return text;
        }
        Matcher matcher = URL_PATTERN.matcher(raw);
        if (!matcher.find()) {
            return text;
        }
        matcher.reset();
        MutableText result = Text.empty();
        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > lastEnd) {
                result.append((Text)Text.literal((String)raw.substring(lastEnd, start)));
            }
            String url = matcher.group();
            int linkColor = ClickGuiStyle.intVal & 0xFFFFFF;
            MutableText linkText = Text.literal((String)url).styled(style -> style.withColor(linkColor).withUnderline(Boolean.valueOf(true)));
            result.append((Text)linkText);
            lastEnd = end;
        }
        if (lastEnd < raw.length()) {
            result.append((Text)Text.literal((String)raw.substring(lastEnd)));
        }
        return result;
    }

    public static String getSiteTitle(String url) {
        String name;
        String clean = url.toLowerCase();
        if (clean.startsWith("http://")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("https://")) {
            clean = clean.substring(8);
        }
        String domain = clean.split("/")[0].split("\\?")[0];
        if (domain.contains("discord.gg") || domain.contains("discord.com")) {
            return "Discord";
        }
        if (domain.contains("vk.com") || domain.contains("vk.ru")) {
            return "\u0412\u041a\u043e\u043d\u0442\u0430\u043a\u0442\u0435 (VK)";
        }
        if (domain.contains("t.me") || domain.contains("telegram")) {
            return "Telegram";
        }
        if (domain.contains("youtube.com") || domain.contains("youtu.be")) {
            return "YouTube";
        }
        if (domain.contains("github.com")) {
            return "GitHub";
        }
        if (domain.contains("reallyworld")) {
            return "ReallyWorld";
        }
        if (domain.contains("funtime")) {
            return "FunTime";
        }
        String[] parts = domain.split("\\.");
        if (parts.length >= 2 && !(name = parts[parts.length - 2]).isEmpty()) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return domain;
    }

    public void openUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String finalUrl = !url.startsWith("http://") && !url.startsWith("https://") ? "https://" + url : url;
        try {
            Util.getOperatingSystem().open(URI.create(finalUrl));
        }
        catch (Throwable t) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(finalUrl));
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private boolean isExplicitServerMarker(String clean) {
        String lower = clean.toLowerCase();
        for (String tag : SERVER_TAGS) {
            if (!lower.startsWith(tag)) continue;
            return true;
        }
        return clean.startsWith("===") || clean.startsWith("---") || clean.startsWith("___") || clean.startsWith("\u2550\u2550\u2550") || clean.startsWith("***");
    }

    private String stripLeadingTimestamp(String clean) {
        if (clean == null) {
            return "";
        }
        return clean.replaceFirst("^\\[\\d{1,2}:\\d{2}\\]\\s*", "").trim();
    }

    private boolean hasPlayerChannelTag(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.startsWith("[g] ") || lower.startsWith("[l] ") || lower.startsWith("[\u0433] ") || lower.startsWith("[\u043b] ") || lower.startsWith("[global] ") || lower.startsWith("[local] ") || lower.startsWith("[\u043e\u0431\u0449\u0438\u0439] ") || lower.startsWith("[\u043b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439] ") || lower.startsWith("[\u0442\u043e\u0440\u0433\u043e\u0432\u044b\u0439] ") || lower.startsWith("[trade] ") || lower.startsWith("[\u043c\u0438\u0440] ") || lower.startsWith("[\u0440\u044f\u0434\u043e\u043c] ") || lower.startsWith("(g) ") || lower.startsWith("(l) ") || lower.startsWith("(\u0433) ") || lower.startsWith("(\u043b) ") || lower.startsWith("! ") || lower.startsWith("!") || lower.startsWith("~ ") || lower.startsWith("~")) {
            return true;
        }
        return s.matches("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\u2605\u25cf\u26a1\u2620\ud83d\udde1\ud83d\udee1\u2726\u272a\u2022]+\\s*\\[(?:G|L|\u0413|\u041b|Global|Local|Trade|\u041e\u0431\u0449\u0438\u0439|\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439|\u041c\u0438\u0440|\u0420\u044f\u0434\u043e\u043c)\\]\\s*.+");
    }

    private boolean containsOnlinePlayerSpeech(String s) {
        char[] delims;
        if (mc == null || mc.getNetworkHandler() == null) {
            return false;
        }
        int delimIdx = -1;
        for (char d : delims = new char[]{':', '\u00bb', '\u203a', '>', '\u27a0', '\u279c', '\u279d', '\u27a1', '~'}) {
            int idx = s.indexOf(d);
            if (idx == -1 || delimIdx != -1 && idx >= delimIdx) continue;
            delimIdx = idx;
        }
        int arrowIdx = s.indexOf("->");
        if (arrowIdx != -1 && (delimIdx == -1 || arrowIdx < delimIdx)) {
            delimIdx = arrowIdx;
        }
        if (delimIdx <= 0) {
            return false;
        }
        String prefix = s.substring(0, delimIdx).trim();
        if (prefix.isEmpty()) {
            return false;
        }
        int lastSpace = prefix.lastIndexOf(32);
        String candidate = lastSpace != -1 ? prefix.substring(lastSpace + 1).trim() : prefix;
        if ((candidate = candidate.replaceAll("^[<\\[(]+|[>\\])]+$", "").trim()).length() >= 3 && candidate.length() <= 16 && !STOP_WORDS.contains(candidate.toLowerCase(Locale.ROOT))) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                if (!entry.getProfile().name().equalsIgnoreCase(candidate)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerChat(String clean) {
        if (clean == null || clean.isBlank()) {
            return false;
        }
        String s = this.stripLeadingTimestamp(clean);
        if (s.isEmpty()) {
            return false;
        }
        if (this.isExplicitServerMarker(s)) {
            return false;
        }
        if (VANILLA_PLAYER_MSG.matcher(s).matches()) {
            return true;
        }
        if (this.isPrivateMessage(s)) {
            return true;
        }
        if (this.isClanMessage(s)) {
            return true;
        }
        if (this.hasPlayerChannelTag(s)) {
            return true;
        }
        if (PLAYER_MSG_PATTERN.matcher(s).matches()) {
            return true;
        }
        return this.containsOnlinePlayerSpeech(s);
    }

    public boolean isPrivateMessage(String clean) {
        if (clean == null || clean.isBlank()) {
            return false;
        }
        String s = this.stripLeadingTimestamp(clean);
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("[\u043b\u0441]") || lower.contains("[pm]") || lower.contains("[msg]") || lower.contains("[w]")) {
            return true;
        }
        if (lower.contains("\u0448\u0435\u043f\u0447\u0435\u0442") || lower.contains("whispers") || lower.contains("\u0432\u0430\u043c \u0448\u0435\u043f\u0447\u0435\u0442") || lower.contains("\u0432\u044b \u0448\u0435\u043f\u0447\u0435\u0442\u0435")) {
            return true;
        }
        if (PM_ARROW_PATTERN.matcher(s).matches() || s.contains("-> \u044f") || s.contains("-> \u043c\u043d\u0435") || s.contains("-> me")) {
            return true;
        }
        return lower.startsWith("[\u043e\u0442 ") || lower.startsWith("[\u043a\u043e\u043c\u0443 ") || lower.startsWith("\u043e\u0442:") || lower.startsWith("\u043a\u043e\u043c\u0443:");
    }

    public boolean isClanMessage(String clean) {
        if (clean == null || clean.isBlank()) {
            return false;
        }
        String s = this.stripLeadingTimestamp(clean);
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("[\u043a\u043b\u0430\u043d]") || lower.contains("[clan]") || lower.contains("[\u043a]") || lower.contains("[c]") || lower.contains("[\u043f\u0430\u0442\u0438]") || lower.contains("[party]") || lower.contains("[\u0433\u0438\u043b\u044c\u0434\u0438\u044f]") || lower.contains("[guild]") || lower.startsWith("\u043a\u043b\u0430\u043d >") || lower.startsWith("clan >") || lower.startsWith("\u043a\u043b\u0430\u043d:") || lower.startsWith("clan:");
    }

    public boolean isStaffMessage(String clean) {
        if (clean == null || clean.isBlank()) {
            return false;
        }
        String s = this.stripLeadingTimestamp(clean);
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("[staff]") || lower.contains("[st]") || lower.contains("[admin]") || lower.contains("[\u0430\u0434\u043c\u0438\u043d]") || lower.contains("[\u043c\u043e\u0434\u0435\u0440]") || lower.contains("[\u0445\u0435\u043b\u043f\u0435\u0440]") || lower.contains("[\u043a\u0443\u0440\u0430\u0442\u043e\u0440]") || lower.contains("[\u0441\u0442\u0430\u0436\u0435\u0440]") || lower.contains("[\u0442\u0438\u043a\u0435\u0442]") || lower.contains("[ticket]") || lower.contains("[\u0440\u0435\u043f\u043e\u0440\u0442]") || lower.contains("[report]") || lower.contains("[\u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0430]") || lower.contains("[\u0431\u0430\u043d]") || lower.contains("[\u043c\u0443\u0442]")) {
            return true;
        }
        if (lower.startsWith("[a] ") || lower.startsWith("[s] ") || lower.startsWith("@staff ") || lower.startsWith("@admin ")) {
            return true;
        }
        return !(!lower.contains("\u0437\u0430\u0431\u0430\u043d\u0438\u043b") && !lower.contains("\u0437\u0430\u043c\u0443\u0442\u0438\u043b") && !lower.contains("\u043a\u0438\u043a\u043d\u0443\u043b") && !lower.contains("\u0440\u0430\u0437\u0431\u0430\u043d\u0438\u043b") && !lower.contains("\u0440\u0430\u0437\u043c\u0443\u0442\u0438\u043b") || !lower.contains("\u043d\u0430") && !lower.contains("\u043f\u043e \u043f\u0440\u0438\u0447\u0438\u043d\u0435") && !lower.contains("\u043f\u0440\u0438\u0447\u0438\u043d\u0430"));
    }

    public boolean isServerMessage(String clean) {
        if (clean == null || clean.isBlank()) {
            return false;
        }
        String s = this.stripLeadingTimestamp(clean);
        if (s.isEmpty()) {
            return false;
        }
        if (this.isPlayerChat(clean)) {
            return false;
        }
        if (this.isPrivateMessage(clean)) {
            return false;
        }
        if (this.isClanMessage(clean)) {
            return false;
        }
        if (this.hasPlayerChannelTag(s) || PLAYER_MSG_PATTERN.matcher(s).matches() || this.containsOnlinePlayerSpeech(s)) {
            return false;
        }
        if (this.isExplicitServerMarker(s)) {
            return true;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.contains("\u043f\u043e\u0433\u0438\u0431") || lower.contains("\u0443\u0431\u0438\u0442") || lower.contains("fell from") || lower.contains("slain by") || lower.contains("\u0441\u0433\u043e\u0440\u0435\u043b") || lower.contains("\u0443\u0442\u043e\u043d\u0443\u043b") || lower.contains("\u0440\u0430\u0437\u0431\u0438\u043b\u0441\u044f") || lower.contains("\u0432\u0437\u043e\u0440\u0432\u0430\u043d") || lower.contains("\u0437\u0430\u0448\u0435\u043b \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440") || lower.contains("\u043f\u043e\u043a\u0438\u043d\u0443\u043b \u0441\u0435\u0440\u0432\u0435\u0440") || lower.contains("joined the game") || lower.contains("left the game") || lower.contains("\u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f") || lower.contains("\u0431\u0430\u043b\u0430\u043d\u0441") || lower.contains("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430") || lower.contains("\u043f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430") || lower.contains("\u043e\u0447\u0438\u0441\u0442\u043a\u0430") || lower.contains("\u0434\u0440\u043e\u043f") || lower.contains("\u0432\u0430\u0440\u043f") || lower.contains("\u0441\u0435\u0440\u0432\u0435\u0440") || lower.contains("\u043f\u0440\u0430\u0432\u0438\u043b\u0430") || lower.contains("\u043d\u0430\u0448 \u0441\u0430\u0439\u0442") || lower.contains("\u0434\u0438\u0441\u043a\u043e\u0440\u0434") || lower.contains("\u0433\u0440\u0443\u043f\u043f\u0430 \u0432\u043a\u043e\u043d\u0442\u0430\u043a\u0442\u0435") || lower.contains("\u0442\u0435\u043b\u0435\u0433\u0440\u0430\u043c") || lower.contains("\u043f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u0430\u043a\u043a\u0430\u0443\u043d\u0442") || lower.contains("\u0443\u0441\u043f\u0435\u0448\u043d\u043e") || lower.contains("\u043e\u0448\u0438\u0431\u043a\u0430") || lower.contains("\u0432\u043d\u0438\u043c\u0430\u043d\u0438\u0435") || lower.contains("\u043c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439") || lower.contains("\u0430\u0438\u0440\u0434\u0440\u043e\u043f") || lower.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || lower.contains("\u0431\u043e\u0441\u0441") || lower.contains("\u043f\u043e\u0431\u0435\u0434\u0438\u043b") || lower.contains("\u043d\u0430\u0433\u0440\u0430\u0434\u0430") || lower.contains("\u0441\u043f\u0430\u0432\u043d") || lower.contains("pvp")) {
            return true;
        }
        if (s.startsWith("\u00bb ") || s.startsWith("\u203a ") || s.startsWith("\u25cf ") || s.startsWith("\u2605 ") || s.startsWith("(!)") || s.startsWith("[!]")) {
            return true;
        }
        return !s.contains(":") && !s.contains("\u00bb") && !s.contains("\u203a") && !s.contains("<") && !s.contains("->");
    }

    public boolean shouldDisplayInCurrentTab(Text text) {
        if (text == null) {
            return false;
        }
        String raw = text.getString();
        if (raw == null) {
            return false;
        }
        String clean = Formatting.strip((String)raw);
        if (this.chatSearch.isChecked() && this.isSearchOpen && !this.searchQuery.isBlank() && !clean.toLowerCase(Locale.ROOT).contains(this.searchQuery.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (!this.mutedPlayers.isEmpty()) {
            String cleanLower = clean.toLowerCase(Locale.ROOT);
            for (String muted : this.mutedPlayers) {
                if (!cleanLower.contains(muted)) continue;
                return false;
            }
        }
        if (!this.tabs.isChecked() || this.currentTab == ChatTab.ALL) {
            return true;
        }
        String trimmedClean = clean.trim();
        if (trimmedClean.isEmpty()) {
            return false;
        }
        switch (this.currentTab) {
            case PM: {
                return this.isPrivateMessage(trimmedClean);
            }
            case CLAN: {
                return this.isClanMessage(trimmedClean);
            }
            case STAFF: {
                return this.isStaffMessage(trimmedClean);
            }
            case SERVER: {
                return this.isServerMessage(trimmedClean);
            }
        }
        return true;
    }

    public void trackIncomingTabMessage(Text text) {
        if (!this.tabs.isChecked()) {
            return;
        }
        String raw = text.getString();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String clean = Formatting.strip((String)raw).trim();
        if (clean.isEmpty()) {
            return;
        }
        for (ChatTab tab : ChatTab.values()) {
            if (tab == ChatTab.ALL || tab == this.currentTab) continue;
            boolean matches = false;
            switch (tab) {
                case PM: {
                    matches = this.isPrivateMessage(clean);
                    break;
                }
                case CLAN: {
                    matches = this.isClanMessage(clean);
                    break;
                }
                case STAFF: {
                    matches = this.isStaffMessage(clean);
                    break;
                }
                case SERVER: {
                    matches = this.isServerMessage(clean);
                    break;
                }
            }
            if (!matches) continue;
            this.unreadCounts.compute(tab, (k, v) -> (v == null ? 0 : v) + 1);
        }
    }

    private float sf() {
        if (mc == null || mc.getWindow() == null) {
            return 1.0f;
        }
        return (float)mc.getWindow().getScaleFactor() / 2.0f;
    }

    public void drawClickGuiPanel(float x, float y, float w, float h, float radius, int bodyColor, int outlineColor, float stroke) {
        float s = this.sf();
        if (this.blur.isChecked()) {
            Render2D.processData60(x * s + 0.5f * s, y * s + 0.5f * s, w * s - 1.0f * s, h * s - 1.0f * s, radius * s, 30.0f, 1.0f, -1);
        }
        Render2D.processData51(x * s, y * s, w * s, h * s, radius * s, bodyColor);
        if (outlineColor != 0 && stroke > 0.0f) {
            Render2D.processData86(x * s, y * s, w * s, h * s, radius * s, stroke * s, outlineColor);
        }
    }

    public void drawClickGuiGlow(float x, float y, float w, float h, float radius, int glowColor, float glowRadius, float intensity) {
        if (!this.glow.isChecked() || intensity <= 0.001f) {
            return;
        }
        float s = this.sf();
        Render2D.processData58(new SoftGlowDrawData(x * s, y * s, w * s, h * s, radius * s, glowColor, glowRadius * s).withIntensity(intensity));
    }

    public void drawClickGuiRect(float x, float y, float w, float h, float radius, int color) {
        float s = this.sf();
        Render2D.processData51(x * s, y * s, w * s, h * s, radius * s, color);
    }

    public void drawClickGuiOutline(float x, float y, float w, float h, float radius, float stroke, int color) {
        float s = this.sf();
        Render2D.processData86(x * s, y * s, w * s, h * s, radius * s, stroke * s, color);
    }

    /** Coordinates here use vanilla GUI units; the renderer uses a fixed 2x UI scale. */
    private void drawGlossyGlass(DrawContext context, float x, float y, float w, float h,
                                 float corner, float alpha, int accent, boolean input) {
        if (w <= 2.0f || h <= 2.0f || alpha <= 0.001f) return;
        float s = this.sf();
        float r = Math.min(corner, Math.min(w, h) * 0.5f);
        if (this.blur.isChecked()) {
            Render2D.processData61(new horus.client.render.GlassDrawData(
                x * s, y * s, w * s, h * s, r * s, r * s, r * s, r * s,
                0xB018202E, alpha, 3.5f, 0xCCDCEFFF,
                input ? 0.88f : 0.80f, true, 0.16f, 0.006f, 1.0f, 0.0f
            ).withBlurRadius(input ? 16.0f : 24.0f));
        }
        // A dark neutral tint keeps server colours legible over bright sky and snow.
        this.drawClickGuiRect(x, y, w, h, r,
            Colors.computeInt7(0xFF0B111D, Math.round((this.blur.isChecked() ? 78 : 190) * alpha)));
        this.drawClickGuiOutline(x + 0.5f, y + 0.5f, w - 1.0f, h - 1.0f, r, 0.65f,
            Colors.computeInt7(0xFFDCEBFF, Math.round((input ? 78 : 52) * alpha)));
        // Thin reflected light along the upper bevel, not over the message glyphs.
        this.drawClickGuiRect(x + r + 2.0f, y + 1.0f, Math.max(0.0f, w - r * 2.0f - 4.0f),
            0.75f, 0.35f, Colors.computeInt7(0xFFF1F7FF, Math.round(95 * alpha)));
        this.drawClickGuiRect(x + r + 3.0f, y + h - 1.5f, Math.max(0.0f, w * 0.4f - r),
            0.5f, 0.25f, Colors.computeInt7(accent, Math.round(60 * alpha)));
    }

    private boolean beginShaders(DrawContext context) {
        if (!Render2D.isRendering()) {
            Render2D.renderElement5(context);
            return true;
        }
        return false;
    }

    private void flushShaders(boolean began) {
        if (began && Render2D.isRendering()) {
            Render2D.updateState6();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderGlassPanel(ChatHud chatHud, DrawContext drawContext, int currentTick, boolean focused) {
        float targetAlpha;
        ChatHudLine.Visible line;
        int age;
        List<ChatHudLine.Visible> visibleLines;
        if (!this.isEnabled() || CustomChatModule.mc.player == null) {
            return;
        }
        if ((visibleLines = ((ChatHudMixin)chatHud).visibleMessages()) == null) return;
        int maxLines = chatHud.getVisibleLineCount();
        int visibleCount = 0;
        int firstLine = Math.max(0, ((ChatHudMixin)chatHud).chatScrollbarPos());
        float lineHeight = (float)Math.floor(9.0 * ((Double)mc.options.getChatLineSpacing().getValue() + 1.0));
        for (int i = firstLine; i < Math.min(visibleLines.size(), firstLine + maxLines); i++) {
            if (focused || currentTick - visibleLines.get(i).addedTime() < 200) visibleCount++;
        }
        float chatScale = CustomChatModule.mc.options != null ? (float)((Double)CustomChatModule.mc.options.getChatScale().getValue()).doubleValue() : 1.0f;
        int screenHeight = drawContext.getScaledWindowHeight();
        float chatWidth = ChatHud.getWidth((double)((Double)CustomChatModule.mc.options.getChatWidth().getValue()));
        float maxLineW = 0.0f;
        if (CustomChatModule.mc.textRenderer != null) {
            for (ChatHudLine.Visible line2 : visibleLines) {
                float w;
                int age2 = currentTick - line2.addedTime();
                if (age2 >= 200 && !focused || !((w = (float)CustomChatModule.mc.textRenderer.getWidth(line2.content()) * chatScale) > maxLineW)) continue;
                maxLineW = w;
            }
        }
        float panelW = Math.max(Math.max(chatWidth * chatScale + 16.0f, maxLineW + 24.0f), 320.0f);
        if (drawContext != null) {
            panelW = Math.min(panelW, (float)drawContext.getScaledWindowWidth() - 4.0f);
        }
        float headerH = 0.0f;
        float inputH = focused ? 20.0f : 0.0f;
        float messageAreaH = focused ? Math.max(maxLines * lineHeight * chatScale + 18.0f, 120.0f) : visibleCount * lineHeight * chatScale + 8.0f;
        float targetHeight = headerH + messageAreaH + inputH;
        float f = targetAlpha = visibleCount > 0 || focused ? 1.0f : 0.0f;
        if (this.smooth.isChecked()) {
            this.animatedHeight += (targetHeight - this.animatedHeight) * 0.22f;
            this.animatedAlpha += (targetAlpha - this.animatedAlpha) * 0.18f;
        } else {
            this.animatedHeight = targetHeight;
            this.animatedAlpha = targetAlpha;
        }
        if (this.animatedAlpha < 0.02f || this.animatedHeight < 3.0f) {
            return;
        }
        float bottomY = focused ? (float)screenHeight - 4.0f : (float)screenHeight - 34.0f;
        float panelX = 2.0f;
        float panelH = this.animatedHeight;
        float panelY = bottomY - panelH;
        float r = (float)this.radius.getValue();
        this.lastPanelX = panelX;
        this.lastPanelY = panelY;
        this.lastPanelW = panelW;
        this.lastPanelH = panelH;
        boolean isMentionActive = this.mentionHighlight.isChecked() && System.currentTimeMillis() - this.lastMentionTime < 5000L;
        int baseAccent = isMentionActive ? -19712 : this.getAccentColor();
        int glowColor = Colors.computeInt7(baseAccent, Math.round(90.0f * this.animatedAlpha));
        int bodyColor = Colors.computeInt7(ClickGuiStyle.intVal2, Math.round(200.0f * this.animatedAlpha));
        int outlineColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(180.0f * this.animatedAlpha));
        boolean began = this.beginShaders(drawContext);
        try {
            if (this.glow.isChecked()) {
                this.drawClickGuiGlow(panelX, panelY, panelW, panelH, r, glowColor, 4.0f, 0.25f * this.animatedAlpha);
            }
            this.drawGlossyGlass(drawContext, panelX, panelY, panelW, panelH, r, this.animatedAlpha, baseAccent, false);
            int accentLine = Colors.computeInt7(baseAccent, Math.round(230.0f * this.animatedAlpha));
            // Keep the left edge clear: message text begins at vanilla x = 4.
            if (focused) {
                int divColor = Colors.computeInt7(ClickGuiStyle.intVal11, Math.round(100.0f * this.animatedAlpha));
                this.drawClickGuiRect(panelX + 4.0f, bottomY - inputH, panelW - 8.0f, 0.75f, 0.5f, divColor);
            }
        }
        finally {
            this.flushShaders(began);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderChatTabs(DrawContext context, TextRenderer textRenderer, int fieldX, int fieldY, int chatWidth, int mouseX, int mouseY) {
        this.renderJumpButton(context, textRenderer, mouseX, mouseY);
    }

    public void renderJumpButton(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (!this.isEnabled() || CustomChatModule.mc.player == null || textRenderer == null) {
            return;
        }
        boolean showJumpBtn = false;
        float jumpX = 0.0f;
        float jumpY = 0.0f;
        float jumpSize = 0.0f;
        boolean isHoverJump = false;
        int scrollbarPos = 0;
        if (this.jumpToBottom.isChecked() && CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null && (scrollbarPos = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos()) > 0) {
            showJumpBtn = true;
            jumpSize = 20.0f;
            jumpX = this.lastPanelX + this.lastPanelW - jumpSize - 8.0f;
            jumpY = (float)context.getScaledWindowHeight() - 24.0f - jumpSize - 6.0f;
            this.jumpBtnX = jumpX;
            this.jumpBtnY = jumpY;
            this.jumpBtnSize = jumpSize;
            isHoverJump = (float)mouseX >= jumpX && (float)mouseX <= jumpX + jumpSize && (float)mouseY >= jumpY && (float)mouseY <= jumpY + jumpSize;
        }
        if (showJumpBtn) {
            int accent = ClickGuiStyle.intVal;
            boolean began = this.beginShaders(context);
            try {
                if (this.glow.isChecked()) {
                    this.drawClickGuiGlow(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0f, Colors.computeInt7(accent, 110), 4.5f, 0.85f);
                }
                int jBody = isHoverJump ? ClickGuiStyle.intVal9 : ClickGuiStyle.intVal46;
                int jOut = isHoverJump ? ClickGuiStyle.intVal : ClickGuiStyle.intVal11;
                this.drawClickGuiPanel(jumpX, jumpY, jumpSize, jumpSize, jumpSize / 2.0f, jBody, jOut, 0.75f);
            }
            finally {
                this.flushShaders(began);
            }
            String jumpIcon = "\u2193";
            int jIconW = textRenderer.getWidth(jumpIcon);
            context.drawText(textRenderer, jumpIcon, (int)(jumpX + (jumpSize - (float)jIconW) / 2.0f), (int)(jumpY + (jumpSize - 8.0f) / 2.0f), isHoverJump ? -1 : ClickGuiStyle.intVal, false);
            if (this.unreadScrolledCount > 0) {
                String badge = String.valueOf(Math.min(99, this.unreadScrolledCount));
                int bW = textRenderer.getWidth(badge) + 6;
                float bX = jumpX + jumpSize - 4.0f;
                float bY = jumpY - 3.0f;
                boolean bBegan = this.beginShaders(context);
                try {
                    this.drawClickGuiPanel(bX, bY, bW, 10.0f, 3.0f, ClickGuiStyle.intVal, ClickGuiStyle.intVal, 0.65f);
                }
                finally {
                    this.flushShaders(bBegan);
                }
                context.drawText(textRenderer, badge, (int)(bX + 3.0f), (int)(bY + 1.0f), -1, false);
            }
        }
    }

    private boolean isNickChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }

    private boolean isValidPlayerNick(String word) {
        if (word == null || word.length() < 3 || word.length() > 16) {
            return false;
        }
        if (STOP_WORDS.contains(word.toLowerCase())) {
            return false;
        }
        if (CustomChatModule.mc.player != null && CustomChatModule.mc.player.getGameProfile().name().equalsIgnoreCase(word)) {
            return false;
        }
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                if (!entry.getProfile().name().equalsIgnoreCase(word)) continue;
                return true;
            }
        }
        return true;
    }

    public void updateHoveredElements(int mouseX, int mouseY) {
    }

    public void renderPopups(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int button, Consumer<String> setChatFieldText) {
        int scrollPos;
        String clickedUrl;
        if (!this.isEnabled()) {
            return false;
        }
        if (this.linkHighlight.isChecked() && button == 0 && (clickedUrl = this.getClickedUrlAt(mouseX, mouseY)) != null) {
            this.openUrl(clickedUrl);
            NotificationHud.processData3(this.category.getIcon(), "\u0421\u0441\u044b\u043b\u043a\u0430", "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u043c \u0432 \u0431\u0440\u0430\u0443\u0437\u0435\u0440\u0435...", true);
            return true;
        }
        if (this.jumpToBottom.isChecked() && CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null && button == 0 && (scrollPos = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos()) > 0 && mouseX >= (double)this.jumpBtnX && mouseX <= (double)(this.jumpBtnX + this.jumpBtnSize) && mouseY >= (double)this.jumpBtnY && mouseY <= (double)(this.jumpBtnY + this.jumpBtnSize)) {
            CustomChatModule.mc.inGameHud.getChatHud().resetScroll();
            this.unreadScrolledCount = 0;
            return true;
        }
        return false;
    }

    public boolean handleIncomingMessage(ChatHud chatHud, Text text) {
        if (!this.isEnabled()) {
            return false;
        }
        String rawText = text.getString();
        if (CustomChatModule.mc.player != null && this.mentionHighlight.isChecked()) {
            String myNick = CustomChatModule.mc.player.getGameProfile().name();
            if (rawText.toLowerCase().contains(myNick.toLowerCase())) {
                this.lastMentionTime = System.currentTimeMillis();
                this.hasRecentMention = true;
            }
        }
        if (this.antispam.isChecked()) {
            List<ChatHudLine> allMessages = ((ChatHudMixin)chatHud).allMessages();
            if (!rawText.isBlank() && !allMessages.isEmpty() && rawText.equals(this.lastSpamText)) {
                ++this.spamCount;
                ChatHudLine firstLine = allMessages.get(0);
                allMessages.set(0, new ChatHudLine(firstLine.creationTick(), (Text)text.copy().append((Text)Text.literal((String)(" (x" + this.spamCount + ")")).formatted(Formatting.GOLD)), firstLine.signature(), firstLine.indicator()));
                ((ChatHudMixin)chatHud).invokeRefreshTrimmedMessages();
                return true;
            }
            this.lastSpamText = rawText;
            this.spamCount = 1;
        }
        return false;
    }

    public void resetAnimatedInput() {
        this.animatedCursorX = 0.0f;
        this.lastTextLength = 0;
        this.lastCursor = 0;
        this.typingIntensity = 0.0f;
        this.counterScale = 1.0f;
        this.animatedFill = 0.0f;
        this.lastFrameTime = 0L;
    }

    public float getLastPanelX() {
        return this.lastPanelX;
    }

    public float getLastPanelY() {
        return this.lastPanelY;
    }

    public float getLastPanelW() {
        return this.lastPanelW;
    }

    public float getLastPanelH() {
        return this.lastPanelH;
    }

    public void updateChatFieldBounds(TextFieldWidget chatField) {
        if (!this.isEnabled() || chatField == null) {
            return;
        }
        int screenHeight = mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 400;
        float panelBottom = (float)screenHeight - 4.0f;
        float inputH = 20.0f;
        float inputY = panelBottom - inputH;
        int fieldX = (int)(this.lastPanelX + 6.0f);
        int fieldY = (int)(inputY + 3.5f);
        int rightReserved = this.modeBadge.isChecked() && this.animatedInput.isChecked() ? 120 : 52;
        int screenWidth = mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 640;
        int textW = Math.max(20, Math.min(screenWidth - fieldX - rightReserved - 8,
            (int)(this.lastPanelW - rightReserved - 12.0f)));
        chatField.setX(fieldX);
        chatField.setY(fieldY);
        chatField.setWidth(textW);
        chatField.setHeight(13);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderInputFieldBackground(DrawContext context, TextFieldWidget chatField) {
        if (!this.isEnabled() || chatField == null) {
            return;
        }
        int screenHeight = context.getScaledWindowHeight();
        float panelBottom = (float)screenHeight - 4.0f;
        float inputH = 20.0f;
        float inputY = panelBottom - inputH;
        int fieldX = (int)(this.lastPanelX + 4.0f);
        int fieldW = (int)(this.lastPanelW - 8.0f);
        int fieldY = (int)(inputY + 2.0f);
        int fieldH = 16;
        float r = 5.0f;
        int accent = ClickGuiStyle.intVal;
        boolean began = this.beginShaders(context);
        try {
            if (this.glow.isChecked()) {
                float glowRad = 4.0f + (this.animatedInput.isChecked() ? 5.0f * this.typingIntensity : 0.0f);
                int glowAlpha = (int)(70.0f + (this.animatedInput.isChecked() ? 110.0f * this.typingIntensity : 0.0f));
                float glowIntensity = 0.2f + (this.animatedInput.isChecked() ? 0.2f * this.typingIntensity : 0.0f);
                this.drawClickGuiGlow(fieldX, fieldY, fieldW, fieldH, r, Colors.computeInt7(accent, Math.min(255, glowAlpha)), glowRad, glowIntensity);
            }
            int outlineAlpha = (int)(110.0f + (this.animatedInput.isChecked() ? 120.0f * this.typingIntensity : 0.0f));
            int outlineColor = Colors.computeInt7(accent, Math.min(255, outlineAlpha));
            float stroke = 0.65f + (this.animatedInput.isChecked() ? 0.2f * this.typingIntensity : 0.0f);
            this.drawGlossyGlass(context, fieldX, fieldY, fieldW, fieldH, r, 1.0f, this.getAccentColor(), true);
        }
        finally {
            this.flushShaders(began);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderInputFieldForeground(DrawContext context, TextRenderer textRenderer, TextFieldWidget chatField, int mouseX, int mouseY) {
        if (!this.isEnabled() || chatField == null) {
            return;
        }
        int screenHeight = context.getScaledWindowHeight();
        float panelBottom = (float)screenHeight - 4.0f;
        float inputH = 20.0f;
        float inputY = panelBottom - inputH;
        int fieldX = (int)(this.lastPanelX + 4.0f);
        int fieldW = (int)(this.lastPanelW - 8.0f);
        int fieldY = (int)(inputY + 2.0f);
        int fieldH = 16;
        long now = System.currentTimeMillis();
        float dt = this.lastFrameTime == 0L ? 0.016f : Math.min(0.1f, (float)(now - this.lastFrameTime) / 1000.0f);
        this.lastFrameTime = now;
        String text = chatField.getText();
        int cursor = chatField.getCursor();
        int currentLen = text.length();
        if (this.animatedInput.isChecked()) {
            int clampedFirst;
            if (currentLen != this.lastTextLength || cursor != this.lastCursor) {
                this.typingIntensity = 1.0f;
                this.counterScale = 1.35f;
                this.lastTextLength = currentLen;
                this.lastCursor = cursor;
            } else {
                this.typingIntensity = Math.max(0.0f, this.typingIntensity - dt * 2.8f);
                this.counterScale += (1.0f - this.counterScale) * Math.min(1.0f, dt * 14.0f);
            }
            int firstChar = 0;
            if (chatField instanceof TextFieldWidgetAccessor) {
                TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor)chatField;
                firstChar = accessor.getFirstCharacterIndex();
            }
            String visiblePrefix = "";
            int clampedCursor = Math.max(0, Math.min(cursor, text.length()));
            if (clampedCursor >= (clampedFirst = Math.max(0, Math.min(firstChar, text.length())))) {
                visiblePrefix = text.substring(clampedFirst, clampedCursor);
            }
            float textStartX = chatField.getX();
            float targetCursorX = textStartX + (float)textRenderer.getWidth(visiblePrefix);
            this.animatedCursorX = this.animatedCursorX <= 0.0f || Math.abs(this.animatedCursorX - targetCursorX) > 120.0f ? targetCursorX : (this.animatedCursorX += (targetCursorX - this.animatedCursorX) * Math.min(1.0f, dt * 24.0f));
            if (text.isEmpty()) {
                String hint = textRenderer.trimToWidth("Сообщение или /команда", Math.max(0, chatField.getWidth() - 6));
                context.drawText(textRenderer, hint, (int)textStartX + 3, chatField.getY() + 2, 0xFFAEBFD3, false);
            }
            if (chatField.isFocused()) {
                int accent = ClickGuiStyle.intVal;
                float pulse = 0.8f + 0.2f * (float)Math.sin((double)now / 130.0);
                float caretAlpha = Math.min(1.0f, pulse * 0.9f + this.typingIntensity * 0.35f);
                int caretColor = Colors.computeInt7(accent, (int)(255.0f * caretAlpha));
                float caretX = this.animatedCursorX;
                float caretY = (float)chatField.getY() + 1.0f;
                float caretH = 10.0f;
                float caretW = 1.8f;
                boolean caretBegan = this.beginShaders(context);
                try {
                    if (this.glow.isChecked()) {
                        this.drawClickGuiGlow(caretX - 2.0f, caretY - 1.0f, caretW + 4.0f, caretH + 2.0f, 1.5f, Colors.computeInt7(accent, (int)(130.0f * caretAlpha)), 3.5f, 0.85f + 0.35f * this.typingIntensity);
                    }
                    this.drawClickGuiRect(caretX, caretY, caretW, caretH, 0.75f, caretColor);
                }
                finally {
                    this.flushShaders(caretBegan);
                }
            }
            String countStr = currentLen + "/256";
            int countColor = currentLen > 240 ? -1096636 : (currentLen > 200 ? -680437 : ClickGuiStyle.intVal12);
            int countW = textRenderer.getWidth(countStr);
            int countX = fieldX + fieldW - countW - 8;
            int countY = fieldY + 4;
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            if (Math.abs(this.counterScale - 1.0f) > 0.02f) {
                float centerX = (float)countX + (float)countW / 2.0f;
                float centerY = (float)countY + 4.0f;
                matrices.translate(centerX, centerY);
                matrices.scale(this.counterScale, this.counterScale);
                matrices.translate(-centerX, -centerY);
            }
            context.drawText(textRenderer, countStr, countX, countY, countColor, false);
            matrices.popMatrix();
            Object modeLabel = null;
            int modeColor = 0;
            int badgeW = 0;
            int badgeH = 12;
            int badgeX = 0;
            int badgeY = fieldY + 2;
            int badgeBorder = 0;
            if (this.modeBadge.isChecked()) {
                if (text.startsWith("/w ") || text.startsWith("/msg ") || text.startsWith("/tell ") || text.startsWith("/m ")) {
                    String[] parts = text.split(" ");
                    modeLabel = parts.length >= 2 && !parts[1].isBlank() ? "\u2709 \u041b\u0421 \u2192 " + parts[1] : "\u2709 \u041b\u0421";
                    modeColor = -680437;
                } else if (text.startsWith("/")) {
                    modeLabel = "/ \u041a\u041e\u041c\u0410\u041d\u0414\u0410";
                    modeColor = -1;
                } else if (text.startsWith("@")) {
                    modeLabel = "@ \u041c\u0415\u041d\u0428\u0415\u041d";
                    modeColor = -16337196;
                } else if (text.startsWith("!")) {
                    modeLabel = "! \u041a\u0420\u0418\u041a";
                    modeColor = -1096636;
                } else {
                    modeLabel = "\ud83d\udcac \u0427\u0410\u0422";
                    modeColor = ClickGuiStyle.intVal;
                }
                int badgeTextW = textRenderer.getWidth((String)modeLabel);
                badgeW = badgeTextW + 8;
                badgeX = countX - badgeW - 6;
                badgeBorder = Colors.computeInt7(modeColor, 130);
            }
            float targetFill = Math.min(1.0f, (float)currentLen / 256.0f);
            this.animatedFill += (targetFill - this.animatedFill) * Math.min(1.0f, dt * 16.0f);
            float barStartX = (float)fieldX + 3.0f;
            float barY = (float)(fieldY + fieldH) - 1.5f;
            float maxBarW = (float)fieldW - 6.0f;
            float fillW = maxBarW * this.animatedFill;
            int progressColor = currentLen > 240 ? -1096636 : (currentLen > 200 ? -680437 : ClickGuiStyle.intVal);
            boolean badgeBegan = this.beginShaders(context);
            try {
                if (this.modeBadge.isChecked() && modeLabel != null) {
                    this.drawClickGuiPanel(badgeX, badgeY, badgeW, badgeH, 4.0f, ClickGuiStyle.intVal46, badgeBorder, 0.65f);
                }
                if (this.animatedFill > 0.005f) {
                    if (this.glow.isChecked()) {
                        this.drawClickGuiGlow(barStartX, barY - 1.0f, fillW, 2.5f, 1.0f, Colors.computeInt7(progressColor, (int)(130.0f + 100.0f * this.typingIntensity)), 3.0f, 0.8f + 0.3f * this.typingIntensity);
                    }
                    this.drawClickGuiRect(barStartX, barY, fillW, 1.5f, 0.75f, progressColor);
                }
            }
            finally {
                this.flushShaders(badgeBegan);
            }
            if (this.modeBadge.isChecked() && modeLabel != null) {
                context.drawText(textRenderer, (String)modeLabel, badgeX + 5, badgeY + (badgeH - 8) / 2 + 1, modeColor, false);
            }
        } else {
            String countStr = currentLen + "/256";
            int countW = textRenderer.getWidth(countStr);
            context.drawText(textRenderer, countStr, fieldX + fieldW - countW - 6, fieldY + 4, ClickGuiStyle.intVal12, false);
        }
        this.renderJumpButton(context, textRenderer, mouseX, mouseY);
        this.updateHoveredElements(mouseX, mouseY);
        this.renderPopups(context, textRenderer, mouseX, mouseY);
    }

    public void onNewMessageAdded() {
        this.lastMessageTime = System.currentTimeMillis();
        if (CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null) {
            this.unreadScrolledCount = ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos() > 0 ? ++this.unreadScrolledCount : 0;
        }
    }

    public void beginSmoothSlide(DrawContext context) {
        long elapsed;
        this.smoothSlideMatrixPushed = false;
        if (!this.isEnabled() || !this.smoothSlide.isChecked() || CustomChatModule.mc.inGameHud == null || CustomChatModule.mc.inGameHud.getChatHud() == null) {
            return;
        }
        if (SmoothFeatureModule.getInstance().isEnabled() && SmoothFeatureModule.getInstance().smoothChat.isChecked()) {
            return;
        }
        if (((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).chatScrollbarPos() == 0 && (elapsed = System.currentTimeMillis() - this.lastMessageTime) < 200L) {
            float progress = (float)elapsed / 200.0f;
            float ease = 1.0f - (1.0f - progress) * (1.0f - progress) * (1.0f - progress);
            float chatScale = CustomChatModule.mc.options != null ? (float)((Double)CustomChatModule.mc.options.getChatScale().getValue()).doubleValue() : 1.0f;
            float offsetY = 9.0f * chatScale * (1.0f - ease);
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(0.0f, offsetY);
            this.smoothSlideMatrixPushed = true;
        }
    }

    public void endSmoothSlide(DrawContext context) {
        if (this.smoothSlideMatrixPushed) {
            this.smoothSlideMatrixPushed = false;
            context.getMatrices().popMatrix();
        }
    }

    public void toggleSearch() {
        this.searchFocused = this.isSearchOpen = !this.isSearchOpen;
        if (!this.isSearchOpen) {
            this.searchQuery = "";
        }
        this.refreshMessages();
    }

    public void closeSearch() {
        this.isSearchOpen = false;
        this.searchFocused = false;
        this.searchQuery = "";
        this.refreshMessages();
    }

    public boolean handleKeyPressed(int key, int modifiers) {
        if (!this.isEnabled()) {
            return false;
        }
        if (this.isPlayerPopupActive && key == 256) {
            this.closePlayerPopup();
            return true;
        }
        return false;
    }

    public boolean handleCharTyped(String ch) {
        return false;
    }

    public boolean isPlayerMuted(String nick) {
        return nick != null && this.mutedPlayers.contains(nick.toLowerCase(Locale.ROOT));
    }

    public void toggleMutePlayer(String nick) {
        if (nick == null || nick.isBlank()) {
            return;
        }
        String lower = nick.toLowerCase(Locale.ROOT);
        if (this.mutedPlayers.contains(lower)) {
            this.mutedPlayers.remove(lower);
        } else {
            this.mutedPlayers.add(lower);
        }
        this.refreshMessages();
    }

    public void refreshMessages() {
        if (CustomChatModule.mc.inGameHud != null && CustomChatModule.mc.inGameHud.getChatHud() != null) {
            ((ChatHudMixin)CustomChatModule.mc.inGameHud.getChatHud()).invokeRefreshTrimmedMessages();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderSuggestionWindow(DrawContext context, Rect2i area, String typedText, List<Suggestion> suggestions, int inWindowIndex, int selection, int mouseX, int mouseY, Consumer<Integer> onSelect) {
        int spaceIdx;
        String cleanTyped;
        int globalIdx;
        int i;
        if (!this.isEnabled() || suggestions == null || suggestions.isEmpty() || CustomChatModule.mc.textRenderer == null) {
            return;
        }
        int totalCount = suggestions.size();
        int maxVisible = Math.min(totalCount, 8);
        float rowH = 13.5f;
        float padY = 3.5f;
        float totalH = (float)maxVisible * rowH + padY * 2.0f;
        int maxTextW = 80;
        for (int i2 = 0; i2 < totalCount; ++i2) {
            Suggestion s = suggestions.get(i2);
            int w = CustomChatModule.mc.textRenderer.getWidth(s.getText());
            if (s.getTooltip() != null) {
                w += CustomChatModule.mc.textRenderer.getWidth(" \u2022 " + s.getTooltip().getString()) + 8;
            }
            if (w <= maxTextW) continue;
            maxTextW = w;
        }
        float totalW = Math.max((float)maxTextW + 36.0f, 125.0f);
        int screenW = mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 640;
        int screenHeight = mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 400;
        float panelBottom = (float)screenHeight - 4.0f;
        float inputH = 20.0f;
        float inputY = panelBottom - inputH;
        float targetX = Math.max(this.lastPanelX + 6.0f, (float)area.getX());
        if (targetX + totalW > (float)screenW - 6.0f) {
            targetX = (float)screenW - totalW - 6.0f;
        }
        float gap = 3.0f;
        float targetY = inputY - gap - totalH;
        area.setX((int)targetX);
        area.setY((int)targetY);
        area.setWidth((int)totalW);
        area.setHeight((int)totalH);
        float r = 6.0f;
        int accent = this.getAccentColor();
        boolean began = this.beginShaders(context);
        try {
            if (this.glow.isChecked()) {
                this.drawClickGuiGlow(targetX, targetY, totalW, totalH, r, Colors.computeInt7(accent, 95), 6.0f, 0.85f);
            }
            this.drawClickGuiPanel(targetX, targetY, totalW, totalH, r, ClickGuiStyle.intVal2, ClickGuiStyle.intVal11, 0.7f);
            int accentLine = Colors.computeInt7(accent, 220);
            this.drawClickGuiRect(targetX + 2.0f, targetY + 4.0f, 1.5f, totalH - 8.0f, 0.75f, accentLine);
            boolean hasScroll = totalCount > maxVisible;
            float rowW = totalW - 10.0f - (hasScroll ? 5.0f : 0.0f);
            for (i = 0; i < maxVisible && (globalIdx = i + inWindowIndex) < totalCount; ++i) {
                boolean isHovered;
                float rowY = targetY + padY + (float)i * rowH;
                float rowX = targetX + 5.0f;
                boolean isSelected = globalIdx == selection;
                boolean bl = isHovered = (float)mouseX >= rowX && (float)mouseX <= rowX + rowW && (float)mouseY >= rowY && (float)mouseY <= rowY + rowH;
                if (isHovered && onSelect != null && globalIdx != selection) {
                    onSelect.accept(globalIdx);
                }
                if (isSelected) {
                    this.drawClickGuiPanel(rowX, rowY, rowW, rowH - 1.0f, 3.5f, ClickGuiStyle.intVal11, ClickGuiStyle.intVal, 0.65f);
                    continue;
                }
                if (!isHovered) continue;
                this.drawClickGuiPanel(rowX, rowY, rowW, rowH - 1.0f, 3.5f, ClickGuiStyle.intVal46, ClickGuiStyle.intVal3, 0.45f);
            }
            if (hasScroll) {
                float trackX = targetX + totalW - 4.5f;
                float trackY = targetY + 3.0f;
                float trackH = totalH - 6.0f;
                float trackW = 2.0f;
                this.drawClickGuiRect(trackX, trackY, trackW, trackH, 1.0f, ClickGuiStyle.intVal46);
                float thumbH = Math.max(8.0f, trackH * ((float)maxVisible / (float)totalCount));
                float thumbY = trackY + (trackH - thumbH) * ((float)inWindowIndex / (float)(totalCount - maxVisible));
                this.drawClickGuiRect(trackX, thumbY, trackW, thumbH, 1.0f, ClickGuiStyle.intVal);
            }
        }
        finally {
            this.flushShaders(began);
        }
        String string = cleanTyped = typedText != null ? typedText.trim() : "";
        if (cleanTyped.startsWith("/")) {
            cleanTyped = cleanTyped.substring(1);
        }
        String currentWord = (spaceIdx = cleanTyped.lastIndexOf(32)) != -1 ? cleanTyped.substring(spaceIdx + 1) : cleanTyped;
        for (i = 0; i < maxVisible && (globalIdx = i + inWindowIndex) < totalCount; ++i) {
            int normalColor;
            boolean isSelected;
            Suggestion s = suggestions.get(globalIdx);
            float rowY = targetY + padY + (float)i * rowH;
            float rowX = targetX + 5.0f;
            boolean bl = isSelected = globalIdx == selection;
            if (isSelected) {
                context.drawText(CustomChatModule.mc.textRenderer, "\u203a", (int)(rowX + 4.0f), (int)(rowY + 2.5f), accent, false);
            } else {
                context.drawText(CustomChatModule.mc.textRenderer, "\u2022", (int)(rowX + 4.0f), (int)(rowY + 2.5f), ClickGuiStyle.intVal12, false);
            }
            String text = s.getText();
            float textX = rowX + 13.0f;
            int n = normalColor = isSelected ? -1 : ClickGuiStyle.intVal12;
            if (!currentWord.isEmpty() && text.toLowerCase(Locale.ROOT).startsWith(currentWord.toLowerCase(Locale.ROOT))) {
                String matchPart = text.substring(0, currentWord.length());
                String restPart = text.substring(currentWord.length());
                int matchColor = isSelected ? -10929 : accent;
                context.drawText(CustomChatModule.mc.textRenderer, matchPart, (int)textX, (int)(rowY + 2.5f), matchColor, false);
                int matchW = CustomChatModule.mc.textRenderer.getWidth(matchPart);
                context.drawText(CustomChatModule.mc.textRenderer, restPart, (int)(textX + (float)matchW), (int)(rowY + 2.5f), normalColor, false);
                textX += (float)(matchW + CustomChatModule.mc.textRenderer.getWidth(restPart));
            } else {
                context.drawText(CustomChatModule.mc.textRenderer, text, (int)textX, (int)(rowY + 2.5f), normalColor, false);
                textX += (float)CustomChatModule.mc.textRenderer.getWidth(text);
            }
            if (s.getTooltip() == null) continue;
            String desc = " \u2022 " + s.getTooltip().getString();
            context.drawText(CustomChatModule.mc.textRenderer, desc, (int)(textX + 2.0f), (int)(rowY + 2.5f), ClickGuiStyle.intVal7, false);
        }
    }

    public boolean onSuggestionClicked(Rect2i area, List<Suggestion> suggestions, int inWindowIndex, int mouseX, int mouseY, Consumer<Integer> onComplete) {
        if (!this.isEnabled() || suggestions == null || suggestions.isEmpty() || area == null) {
            return false;
        }
        if (!area.contains(mouseX, mouseY)) {
            return false;
        }
        float rowH = 13.5f;
        float padY = 3.5f;
        int row = (int)(((float)mouseY - ((float)area.getY() + padY)) / rowH);
        int globalIdx = inWindowIndex + row;
        if (globalIdx >= 0 && globalIdx < suggestions.size()) {
            if (onComplete != null) {
                onComplete.accept(globalIdx);
            }
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean renderCommandMessages(DrawContext context, List<OrderedText> messages, int origX, int origWidth, TextRenderer textRenderer) {
        float targetW;
        if (!this.isEnabled() || messages == null || messages.isEmpty() || textRenderer == null) {
            return false;
        }
        int screenW = mc.getWindow() != null ? mc.getWindow().getScaledWidth() : 640;
        int screenH = mc.getWindow() != null ? mc.getWindow().getScaledHeight() : 400;
        float panelBottom = (float)screenH - 4.0f;
        float inputH = 20.0f;
        float inputY = panelBottom - inputH;
        float rowH = 12.5f;
        float padY = 4.0f;
        float padX = 8.0f;
        float totalH = (float)messages.size() * rowH + padY * 2.0f;
        float targetBottomY = inputY - 3.0f;
        float targetY = targetBottomY - totalH;
        float targetX = Math.max(this.lastPanelX + 6.0f, (float)origX);
        if (targetX + (targetW = Math.max((float)origWidth + 24.0f, 140.0f)) > (float)screenW - 6.0f) {
            targetW = (float)screenW - targetX - 6.0f;
        }
        float r = 5.0f;
        int warningAccent = -1096636;
        int glowColor = Colors.computeInt7(warningAccent, 85);
        int bodyColor = Colors.computeInt7(ClickGuiStyle.intVal2, 225);
        int outlineColor = Colors.computeInt7(warningAccent, 160);
        boolean began = this.beginShaders(context);
        try {
            if (this.glow.isChecked()) {
                this.drawClickGuiGlow(targetX, targetY, targetW, totalH, r, glowColor, 5.0f, 0.8f);
            }
            this.drawClickGuiPanel(targetX, targetY, targetW, totalH, r, bodyColor, outlineColor, 0.65f);
            this.drawClickGuiRect(targetX + 2.0f, targetY + 3.0f, 1.5f, totalH - 6.0f, 0.75f, warningAccent);
        }
        finally {
            this.flushShaders(began);
        }
        for (int i = 0; i < messages.size(); ++i) {
            OrderedText msg = messages.get(i);
            float lineY = targetY + padY + (float)i * rowH;
            float lineX = targetX + padX;
            if (i == 0) {
                context.drawText(textRenderer, "!", (int)lineX, (int)(lineY + 1.5f), -495247, false);
                lineX += 8.0f;
            }
            context.drawText(textRenderer, msg, (int)lineX, (int)(lineY + 1.5f), -1, false);
        }
        return true;
    }

    static {
        URL_PATTERN = Pattern.compile("(https?:\\/\\/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+|(?:[a-zA-Z0-9-]+\\.)+(?:com|ru|org|net|io|gg|me|online|xyz|fun|info|su)(?:\\/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?)", 2);
        VANILLA_PLAYER_MSG = Pattern.compile("^<[A-Za-z0-9_.]{2,16}>\\s*.+");
        PLAYER_MSG_PATTERN = Pattern.compile("^(?:\\[[^\\]]+\\]|\\([^\\)]+\\)|[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\u2605\u25cf\u26a1\u2620\ud83d\udde1\ud83d\udee1\u2726\u272a\u2022]+\\s*)*\\s*[A-Za-z0-9_.]{2,16}\\s*(?::|\u00bb|\u203a|>|\u27a0|\u279c|\u279d|\u27a1|~|\u2014>|->)\\s*.+");
        PM_ARROW_PATTERN = Pattern.compile("\\[.*(?:->|\u2014>|<-).*\\].*");
        SERVER_TAGS = new String[]{"[!]", "(!)", "[\u2605]", "[\u0441\u0435\u0440\u0432\u0435\u0440]", "[\u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f]", "[\u0432\u043d\u0438\u043c\u0430\u043d\u0438\u0435]", "[\u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435]", "[\u043e\u043f\u043e\u0432\u0435\u0449\u0435\u043d\u0438\u0435]", "[\u0430\u043d\u043e\u043d\u0441]", "[reallyworld]", "[funtime]", "[aresmine]", "[holyworld]", "[\u0441\u0438\u0441\u0442\u0435\u043c\u0430]", "[system]", "[server]", "[broadcast]", "[alert]", "[event]", "[\u0438\u0432\u0435\u043d\u0442]", "[\u0430\u0438\u0440\u0434\u0440\u043e\u043f]", "[\u043c\u0438\u0441\u0442\u0438\u043a]", "[\u0432\u0430\u0439\u043f]", "[\u043c\u0430\u0433\u0430\u0437\u0438\u043d]", "[\u0434\u043e\u043d\u0430\u0442]", "[\u0430\u0443\u043a\u0446\u0438\u043e\u043d]", "[\u0430\u0432\u0442\u043e]", "[\u043a\u0432\u0435\u0441\u0442]", "[\u043d\u0430\u0433\u0440\u0430\u0434\u0430]", "[\u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434]", "[\u043f\u0440\u0430\u0432\u0438\u043b\u0430]", "[\u043f\u043e\u043c\u043e\u0449\u044c]", "[\u0441\u043f\u0440\u0430\u0432\u043a\u0430]", "[\u043a\u043e\u043d\u0441\u043e\u043b\u044c]", "[console]", "[\u0438\u043d\u0444\u043e]", "[info]", "[\u043b\u043e\u0433]", "[log]"};
        STOP_WORDS = Set.of("http", "https", "warp", "clan", "party", "true", "false", "null", "info", "help", "spawn", "home", "discord", "reallyworld", "funtime", "server", "teleport", "player", "pvp", "grief", "anarchy", "rules", "vk", "telegram", "shop", "donate");
    }

    private static class CharInfo {
        final int codePoint;
        final float startX;
        final float endX;

        CharInfo(int codePoint, float startX, float endX) {
            this.codePoint = codePoint;
            this.startX = startX;
            this.endX = endX;
        }
    }

    public static class TabBounds {
        public final ChatTab tab;
        public final float x;
        public final float y;
        public final float w;
        public final float h;

        public TabBounds(ChatTab tab, float x, float y, float w, float h) {
            this.tab = tab;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.w) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.h);
        }
    }
}
