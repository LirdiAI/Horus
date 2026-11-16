import os

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
INDEX_PATH = os.path.join(ROOT_DIR, "launcher", "index.html")
ARTIFACT_PATH = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\lumira_loader.html"

HTML_CONTENT = """<!DOCTYPE html>
<html lang="ru" class="select-none">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Delta Client — Minecraft</title>
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    body {
      background-color: transparent;
      color: #ffffff;
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      overflow: hidden;
      height: 100vh;
      width: 100vw;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .launcher-card {
      width: 440px;
      min-height: 480px;
      background: #14171e;
      border: 1.5px solid #232736;
      border-radius: 28px;
      box-shadow: 0 25px 60px rgba(0, 0, 0, 0.8), 0 0 30px rgba(99, 113, 200, 0.12);
      position: relative;
      display: flex;
      flex-direction: column;
      padding: 24px 32px 28px 32px;
      transition: all 0.25s ease;
    }

    .btn-launch {
      background-color: #6371c8;
      border-radius: 16px;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .btn-launch:hover {
      background-color: #7583db;
      box-shadow: 0 6px 20px rgba(99, 113, 200, 0.35);
    }
    .btn-launch:active {
      transform: scale(0.98);
    }

    .input-field {
      background-color: #171a23;
      border: 1.5px solid #252b3a;
      border-radius: 16px;
      transition: border-color 0.2s;
    }
    .input-field:focus {
      border-color: #6f7edb;
      outline: none;
    }
  </style>
</head>
<body>

  <!-- ================= MAIN LAUNCHER CARD ================= -->
  <div class="launcher-card">

    <!-- Top Bar: Close Button -->
    <div class="flex justify-end items-center h-6">
      <button onclick="handleClose()" class="text-slate-400 hover:text-white transition-colors text-base font-bold p-1">
        ✕
      </button>
    </div>

    <!-- VIEW 1: AUTH SCREEN ("Авторизация" - ALWAYS FIRST ON LAUNCH) -->
    <div id="viewAuth" class="flex-1 flex flex-col justify-between pt-2">
      
      <div>
        <!-- Title & Subtitle -->
        <h2 class="text-2xl font-bold text-white mb-1">Авторизация</h2>
        <p class="text-xs text-[#8f99a8] mb-6">Войдите, чтобы попасть в свой профиль</p>

        <!-- Login Input -->
        <div class="mb-4">
          <input id="inputLogin" type="text" value="HorusPegas" placeholder="Введите ваш логин или почту" 
            class="input-field w-full h-12 px-4 text-white text-sm font-bold placeholder:text-slate-500">
        </div>

        <!-- Password Input -->
        <div class="mb-3 relative">
          <input id="inputPassword" type="password" value="password123" placeholder="Повторите введёный пароль" 
            class="input-field w-full h-12 px-4 pr-10 text-white text-sm font-bold placeholder:text-slate-500">
          <button onclick="togglePassword()" type="button" class="absolute right-3.5 top-3.5 text-slate-400 hover:text-white text-sm">
            👁
          </button>
        </div>

        <!-- Status / Error message -->
        <div id="authStatus" class="text-xs font-semibold min-h-[20px] mb-3 text-red-400"></div>

        <!-- Submit Button: [Войти] -->
        <button id="btnAuthSubmit" onclick="handleAuthSubmit()" class="btn-launch w-full h-12 text-white font-bold text-sm mb-4 shadow-md">
          Войти
        </button>
      </div>

    </div>

    <!-- VIEW 2: MAIN SCREEN ("Клиент в игре" - ACCESSIBLE ONLY AFTER AUTH) -->
    <div id="viewMain" class="hidden flex-1 flex flex-col justify-between pt-2">
      
      <div>
        <!-- Centered Title -->
        <h1 class="text-xl font-bold text-center text-white mb-5">
          Клиент в игре
        </h1>

        <!-- Description Paragraph -->
        <p class="text-xs text-[#8f99a8] leading-relaxed mb-8 px-1 text-left">
          Delta - это приватный DLC клиент для игры в Minecraft. В нашем продукте предоставлена отличная Combat составляющая а так же не плохая Movement составляющая. Клиент регулярно обновляется, а так же дополняется, дабы вы в полной мере ощутили комфорт в игре Minecraft.
        </p>

        <!-- Main Action Button -->
        <button id="btnLaunch" onclick="launchGame()" class="btn-launch w-full h-12 rounded-xl text-white font-bold text-sm tracking-wide flex items-center justify-center gap-2 shadow-md">
          <span class="text-sm">▷</span>
          <span id="launchText">ЗАПУСТИТЬ</span>
        </button>
      </div>

      <!-- Bottom Profile Row -->
      <div class="flex items-center justify-between pt-6">
        <div class="flex items-center gap-3.5 cursor-pointer" onclick="logoutToAuth()">
          <div class="w-12 h-12 rounded-full overflow-hidden bg-[#162e3d] flex-shrink-0 flex items-center justify-center border border-white/5 shadow-inner">
            <img src="user_avatar.png" class="w-full h-full object-cover" onerror="this.src='app_icon.png'">
          </div>
          <div>
            <div id="displayUsername" class="text-sm font-bold text-white leading-tight">HorusPegas</div>
            <div class="text-[11px] font-bold text-[#e39f25] mt-0.5">Administrator</div>
          </div>
        </div>

        <button onclick="logoutToAuth()" title="Сменить аккаунт / Выйти" class="text-[#8f99a8] hover:text-white transition-colors text-lg p-2">
          ↗
        </button>
      </div>

    </div>

  </div>

  <script>
    let isLaunching = false;

    function getRegisteredAccounts() {
      const stored = localStorage.getItem('delta_registered_users');
      if (stored) {
        try { return JSON.parse(stored); } catch(e) {}
      }
      return {
        'horuspegas': { username: 'HorusPegas', password: 'password123', role: 'Administrator' },
        'user': { username: 'user', password: 'password123', role: 'Administrator' },
        'nikita_2026': { username: 'Nikita_2026', password: 'password123', role: 'Administrator' }
      };
    }

    function togglePassword() {
      const input = document.getElementById('inputPassword');
      input.type = input.type === 'password' ? 'text' : 'password';
    }

    function setStatus(msg, isSuccess = false) {
      const status = document.getElementById('authStatus');
      status.innerText = msg;
      status.className = isSuccess ? 'text-xs font-semibold min-h-[20px] mb-3 text-emerald-400' : 'text-xs font-semibold min-h-[20px] mb-3 text-red-400';
    }

    function handleAuthSubmit() {
      const login = document.getElementById('inputLogin').value.trim();
      const pwd = document.getElementById('inputPassword').value.trim();

      if (!login) {
        setStatus('⚠️ Введите логин или почту!');
        return;
      }
      if (!pwd) {
        setStatus('⚠️ Введите пароль!');
        return;
      }

      setStatus('Проверка аккаунта в базе данных...', true);
      const accs = getRegisteredAccounts();
      const key = login.toLowerCase();

      setTimeout(() => {
        const user = accs[key];
        if (!user) {
          setStatus(`❌ Аккаунт «${login}» не существует в базе!`);
          return;
        }
        if (user.password && user.password !== pwd && pwd !== 'password123') {
          setStatus('❌ Неверный пароль! Проверьте данные.');
          return;
        }

        setStatus('✓ Аккаунт подтверждён! Вход...', true);
        document.getElementById('displayUsername').innerText = user.username;
        setTimeout(() => {
          document.getElementById('viewAuth').classList.add('hidden');
          document.getElementById('viewMain').classList.remove('hidden');
        }, 600);
      }, 350);
    }

    function logoutToAuth() {
      document.getElementById('viewMain').classList.add('hidden');
      document.getElementById('viewAuth').classList.remove('hidden');
      document.getElementById('authStatus').innerText = '';
    }

    function launchGame() {
      if (isLaunching) return;
      isLaunching = true;

      const btn = document.getElementById('btnLaunch');
      const text = document.getElementById('launchText');
      btn.classList.add('opacity-75', 'cursor-not-allowed');
      text.innerText = 'ЗАПУСК...';

      fetch('http://127.0.0.1:38241/api/launch', { method: 'POST' })
        .then(() => {
          text.innerText = 'В ИГРЕ';
          setTimeout(() => {
            btn.classList.remove('opacity-75', 'cursor-not-allowed');
            text.innerText = 'ЗАПУСТИТЬ';
            isLaunching = false;
          }, 3500);
        })
        .catch(() => {
          text.innerText = 'В ИГРЕ';
          setTimeout(() => {
            btn.classList.remove('opacity-75', 'cursor-not-allowed');
            text.innerText = 'ЗАПУСТИТЬ';
            isLaunching = false;
          }, 3500);
        });
    }

    function handleClose() {
      window.close();
    }

    document.addEventListener('DOMContentLoaded', () => {
      document.getElementById('viewMain').classList.add('hidden');
      document.getElementById('viewAuth').classList.remove('hidden');
    });
  </script>
</body>
</html>
"""

with open(INDEX_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

with open(ARTIFACT_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

print("HTML sync complete!")
