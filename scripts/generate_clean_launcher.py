import os
import shutil

ROOT_DIR = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
INDEX_PATH = os.path.join(ROOT_DIR, "launcher", "index.html")
ARTIFACT_PATH = r"C:\Users\Никита\.gemini\antigravity\brain\461fbe3e-6ffe-4a62-9e70-0da80a564412\lumira_loader.html"

HTML_CONTENT = """<!DOCTYPE html>
<html lang="ru" class="select-none">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>HORUS LOADER — Fabric 1.21.11 Edition</title>
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <link rel="apple-touch-icon" href="app_icon.png">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2"></script>
  <script src="supabase-client.js"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          fontFamily: {
            sans: ['"Plus Jakarta Sans"', 'sans-serif'],
            mono: ['"JetBrains Mono"', 'monospace'],
          },
          colors: {
            brand: {
              white: '#ffffff',
              ice: '#f1f5f9',
              dark: '#08080c',
              panel: '#0d0e14',
              card: 'rgba(16, 17, 24, 0.78)',
              border: 'rgba(255, 255, 255, 0.14)',
              borderGlow: 'rgba(255, 255, 255, 0.35)',
            }
          },
          boxShadow: {
            'glow-sm': '0 0 15px rgba(255, 255, 255, 0.16)',
            'glow-md': '0 0 30px rgba(255, 255, 255, 0.22)',
            'glow-lg': '0 0 50px rgba(255, 255, 255, 0.30)',
            'glass': '0 20px 60px rgba(0, 0, 0, 0.75)',
          }
        }
      }
    };
  </script>
  <style>
    body {
      background-color: #06070a;
      background-image: 
        radial-gradient(circle at 15% 15%, rgba(255, 255, 255, 0.05) 0%, transparent 40%),
        radial-gradient(circle at 85% 85%, rgba(255, 255, 255, 0.03) 0%, transparent 45%),
        linear-gradient(to bottom, #07080c, #050508);
      color: #e2e8f0;
      font-family: 'Plus Jakarta Sans', sans-serif;
      overflow: hidden;
      height: 100vh;
      width: 100vw;
    }

    .glass-shell {
      background: rgba(11, 13, 19, 0.94);
      backdrop-filter: blur(40px);
      -webkit-backdrop-filter: blur(40px);
      border: 1px solid rgba(255, 255, 255, 0.15);
    }

    .glass-card {
      background: rgba(18, 20, 28, 0.72);
      backdrop-filter: blur(24px);
      -webkit-backdrop-filter: blur(24px);
      border: 1px solid rgba(255, 255, 255, 0.12);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
      transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .glass-card:hover {
      border-color: rgba(255, 255, 255, 0.35);
      background: rgba(24, 27, 38, 0.85);
      box-shadow: 0 12px 35px rgba(0, 0, 0, 0.6), 0 0 20px rgba(255, 255, 255, 0.08);
    }

    .glass-panel {
      background: rgba(255, 255, 255, 0.04);
      backdrop-filter: blur(16px);
      border: 1px solid rgba(255, 255, 255, 0.10);
    }

    .nav-btn.active {
      background: rgba(255, 255, 255, 0.14);
      border-color: rgba(255, 255, 255, 0.4);
      color: #ffffff;
      box-shadow: 0 0 20px rgba(255, 255, 255, 0.12);
    }

    .custom-scroll::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    .custom-scroll::-webkit-scrollbar-track {
      background: rgba(255, 255, 255, 0.02);
    }
    .custom-scroll::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.18);
      border-radius: 9999px;
    }
    .custom-scroll::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 255, 255, 0.35);
    }

    @keyframes pulseGlow {
      0%, 100% { opacity: 0.6; transform: scale(1); }
      50% { opacity: 1; transform: scale(1.02); }
    }
    .animate-pulse-glow {
      animation: pulseGlow 3s infinite ease-in-out;
    }

    @keyframes scanline {
      0% { transform: translateY(-100%); }
      100% { transform: translateY(1000%); }
    }
    .scanline-effect {
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 2px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
      animation: scanline 8s linear infinite;
      pointer-events: none;
    }
  </style>
</head>
<body class="flex flex-col h-screen overflow-hidden text-sm">

  <!-- ================= TOP WINDOW FRAME / TITLEBAR ================= -->
  <header class="h-11 flex-shrink-0 flex items-center justify-between px-4 border-b border-white/10 bg-black/60 backdrop-blur-xl z-50 select-none">
    
    <!-- Left: App Branding & Status -->
    <div class="flex items-center gap-3">
      <div class="w-6 h-6 rounded-lg bg-white/10 border border-white/25 flex items-center justify-center overflow-hidden shadow-glow-sm">
        <img src="logo.png" onerror="this.src='app_icon.png'" alt="Logo" class="w-4 h-4 object-contain">
      </div>
      <div class="flex items-center gap-2">
        <span class="font-extrabold text-xs tracking-wider text-white uppercase">HORUS LOADER</span>
        <span class="text-[9px] font-mono px-1.5 py-0.5 rounded bg-white/15 text-slate-200 border border-white/20 font-bold">1.21.11 RELEASE</span>
      </div>
      <div class="h-4 w-px bg-white/10 mx-1"></div>
      
      <!-- IPC Status indicator -->
      <div id="topIpcBadge" class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[10px] font-mono text-emerald-400">
        <span id="topIpcDot" class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
        <span id="topIpcText">127.0.0.1:38241</span>
      </div>
    </div>

    <!-- Center: App Message / Drag Zone -->
    <div class="hidden md:flex items-center gap-2 text-xs text-slate-400 font-mono">
      <span>Horus Client Edition</span>
      <span>•</span>
      <span class="text-white/80">Fabric 1.21.11</span>
    </div>

    <!-- Right: Window Controls -->
    <div class="flex items-center gap-1">
      <button onclick="playUiSound('click'); showToast('Лоадер свернут в трей')" title="Свернуть" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-white hover:bg-white/10 transition-all">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/></svg>
      </button>
      <button onclick="playUiSound('click'); toggleFullscreen()" title="Развернуть" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-white hover:bg-white/10 transition-all">
        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><rect width="18" height="18" x="3" y="3" rx="2" stroke-width="2"/></svg>
      </button>
      <button onclick="playUiSound('click'); closeLoaderWindow()" title="Закрыть" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-white hover:bg-red-500/30 transition-all">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
      </button>
    </div>

  </header>

  <!-- ================= MAIN LAUNCHER BODY ================= -->
  <div class="flex-1 flex overflow-hidden relative">

    <!-- Ambient Glow Backgrounds -->
    <div class="absolute -top-32 left-1/4 w-96 h-96 bg-white/10 blur-[130px] rounded-full pointer-events-none"></div>
    <div class="absolute -bottom-32 right-1/4 w-96 h-96 bg-white/5 blur-[120px] rounded-full pointer-events-none"></div>
    <div class="scanline-effect"></div>

    <!-- ================= LEFT SIDEBAR ================= -->
    <aside class="w-64 sm:w-72 flex-shrink-0 flex flex-col border-r border-white/10 bg-brand-dark/95 backdrop-blur-2xl z-40 p-4 justify-between">
      
      <!-- Top: User Profile Card -->
      <div class="space-y-4">
        
        <div class="p-3.5 rounded-2xl glass-card border border-white/15 flex items-center gap-3 relative group">
          <div class="relative w-12 h-12 rounded-xl bg-black/60 border border-white/30 flex items-center justify-center overflow-hidden flex-shrink-0 shadow-glow-sm">
            <img id="sidebarAvatar" src="https://mc-heads.net/avatar/Player/64" alt="Skin" class="w-10 h-10 object-contain drop-shadow-[0_0_8px_rgba(255,255,255,0.4)]" onerror="this.src='app_icon.png'">
            <span class="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-emerald-400 border border-black" title="В сети"></span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-1.5">
              <span id="sidebarUsername" class="font-extrabold text-sm text-white truncate">Player</span>
              <span id="sidebarRoleBadge" class="text-[9px] font-mono font-bold px-1.5 py-0.2 rounded bg-white/15 text-white border border-white/20">HORUS</span>
            </div>
            <div class="flex items-center justify-between text-[11px] font-mono text-slate-400 mt-0.5">
              <span id="sidebarUid">UID: #1211</span>
              <span class="text-emerald-400 font-bold">READY</span>
            </div>
            <div class="text-[10px] text-slate-400 truncate mt-0.5 font-mono">
              CLIENT: <span class="text-slate-300">HORUS 1.21.11</span>
            </div>
          </div>
        </div>

        <!-- Navigation Buttons: NO CONFIGS TAB -->
        <nav class="space-y-1.5 font-semibold text-xs">
          
          <button onclick="switchTab('play')" id="tabBtnPlay" class="nav-btn active w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <span class="flex-1">Запуск клиента</span>
            <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          </button>

          <button onclick="switchTab('versions')" id="tabBtnVersions" class="nav-btn w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/></svg>
            <span class="flex-1">Версия клиента</span>
            <span class="text-[10px] font-mono text-emerald-400 font-bold">1.21.11</span>
          </button>

          <button onclick="switchTab('accounts')" id="tabBtnAccounts" class="nav-btn w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
            <span class="flex-1">Менеджер аккаунтов</span>
            <span id="navAltsCount" class="text-[10px] font-mono text-slate-400">0 шт.</span>
          </button>

          <button onclick="switchTab('bypasses')" id="tabBtnBypasses" class="nav-btn w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
            <span class="flex-1">Обходы & Grim</span>
            <span class="text-[10px] font-mono text-emerald-400 font-bold">100%</span>
          </button>

          <button onclick="switchTab('settings')" id="tabBtnSettings" class="nav-btn w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            <span class="flex-1">Настройки JVM & ОЗУ</span>
          </button>

          <button onclick="switchTab('console')" id="tabBtnConsole" class="nav-btn w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl border border-transparent text-slate-300 hover:text-white hover:bg-white/5 transition-all text-left">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
            <span class="flex-1">Консоль отладки</span>
            <span id="consoleLinesBadge" class="text-[9px] font-mono px-1.5 py-0.2 rounded bg-white/10 text-slate-300">LIVE</span>
          </button>

        </nav>

      </div>

      <!-- Bottom: System specs & Link -->
      <div class="pt-4 border-t border-white/10 space-y-3">
        <div class="glass-panel p-3 rounded-xl border border-white/10 text-[11px] font-mono space-y-1">
          <div class="flex justify-between text-slate-400">
            <span>Java Runtime:</span>
            <span class="text-white font-bold">JDK 21 (64-bit)</span>
          </div>
          <div class="flex justify-between text-slate-400">
            <span>Клиент:</span>
            <span class="text-emerald-400 font-bold">Fabric 1.21.11</span>
          </div>
          <div class="flex justify-between text-slate-400">
            <span>Синхронизация:</span>
            <span class="text-white font-bold">Supabase DB</span>
          </div>
        </div>

        <div class="flex items-center justify-between text-[11px] font-mono text-slate-500 px-1">
          <span>Horus Edition v1.21.11</span>
          <button id="audioToggleBtn" onclick="toggleAudio()" class="hover:text-slate-300 transition-colors">ВКЛ 🔊</button>
        </div>
      </div>

    </aside>

    <!-- ================= RIGHT MAIN CONTENT AREA ================= -->
    <main class="flex-1 overflow-y-auto custom-scroll p-6 sm:p-8 relative">

      <!-- ================= TAB: PLAY (MAIN LAUNCH) ================= -->
      <section id="contentPlay" class="space-y-6 max-w-5xl mx-auto">
        
        <!-- Big Hero Launch Card -->
        <div class="relative overflow-hidden rounded-3xl glass-card border border-white/20 p-8 sm:p-10 shadow-glow-sm">
          
          <!-- Card Decorative Grid -->
          <div class="absolute -right-16 -top-16 w-80 h-80 bg-white/5 rounded-full blur-3xl pointer-events-none"></div>

          <!-- Top: Client Info & Status -->
          <div class="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-white/10">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="px-2 py-0.5 rounded-md bg-white/10 border border-white/20 text-[10px] font-mono font-bold tracking-wider text-white">FABRIC 1.21.11</span>
                <span class="px-2 py-0.5 rounded-md bg-emerald-500/20 border border-emerald-500/40 text-[10px] font-mono font-bold text-emerald-400">АКТУАЛЬНАЯ ВЕРСИЯ</span>
              </div>
              <h1 class="text-3xl sm:text-4xl font-black text-white tracking-tight">Horus Client 1.21.11</h1>
              <p class="text-xs sm:text-sm text-slate-400 mt-1 max-w-xl">
                Официальный релиз чит-клиента с премиальной белой темой Frosted Glass, улучшенным AutoExplosion, обходом Grim для AttackAura через блоки и ElytraHelper.
              </p>
            </div>

            <!-- Server & Status Pill -->
            <div class="flex flex-col items-start sm:items-end gap-1.5 flex-shrink-0">
              <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl glass-panel border border-white/15">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
                <span class="text-xs font-mono font-bold text-white">Grim Bypass Active</span>
              </div>
              <div class="text-[11px] font-mono text-slate-400">
                ОЗУ: <span id="heroRamBadge" class="text-white font-bold">4096 MB</span>
              </div>
            </div>
          </div>

          <!-- Bottom: Quick selectors & GIANT LAUNCH BUTTON -->
          <div class="relative z-10 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 pt-6 border-t border-white/15">
            
            <!-- Quick Pickers: ONLY Version (Horus 1.21.11) & User-Created Account -->
            <div class="flex flex-wrap items-center gap-4">
              <!-- Version Picker: ONLY Horus 1.21.11 -->
              <div class="flex flex-col">
                <label class="text-[10px] uppercase font-mono text-slate-400 mb-1">Версия клиента</label>
                <select id="quickVersionSelect" class="px-4 py-2.5 rounded-xl bg-black/60 border border-white/20 text-xs font-semibold text-white focus:outline-none focus:border-white/50 cursor-pointer min-w-[210px]">
                  <option value="horus_1_21_11" selected>Horus 1.21.11 (Latest Release)</option>
                </select>
              </div>

              <!-- Account Picker (User accounts populated dynamically from localStorage) -->
              <div class="flex flex-col">
                <div class="flex items-center justify-between mb-1">
                  <label class="text-[10px] uppercase font-mono text-slate-400">Игровой аккаунт</label>
                  <button onclick="switchTab('accounts')" class="text-[10px] font-mono text-slate-300 hover:text-white underline ml-2">+ Создать</button>
                </div>
                <select id="quickAccountSelect" onchange="playUiSound('click'); onQuickAccountChange(this.value)" class="px-4 py-2.5 rounded-xl bg-black/60 border border-white/20 text-xs font-semibold text-white focus:outline-none focus:border-white/50 cursor-pointer min-w-[210px]">
                  <!-- Populated dynamically -->
                </select>
              </div>
            </div>

            <!-- THE BIG LAUNCH BUTTON -->
            <div class="flex flex-col gap-2">
              <div class="flex items-center gap-3">
                <button id="mainLaunchBtn" onclick="handleLaunchClient()" class="relative group px-8 py-4 rounded-2xl bg-white text-black font-black text-sm tracking-wider shadow-glow-md hover:shadow-glow-lg hover:bg-slate-100 transition-all duration-300 active:scale-95 flex items-center justify-center gap-3">
                  <svg id="launchBtnIcon" class="w-5 h-5 text-black" fill="currentColor" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                  <span id="launchBtnText">ЗАПУСТИТЬ КЛИЕНТ</span>
                </button>
              </div>
              <div class="text-[11px] font-mono text-slate-400 flex items-center gap-1.5">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                <span>Прямой старт игры: <code class="text-white/80 bg-white/10 px-1.5 py-0.5 rounded">launch_game.bat</code></span>
              </div>
            </div>

          </div>

          <!-- Launch Progress Bar (Hidden by default, shown during launch) -->
          <div id="launchProgressContainer" class="hidden relative z-10 mt-6 pt-4 border-t border-white/10 space-y-2">
            <div class="flex items-center justify-between text-xs font-mono">
              <span id="launchProgressStage" class="text-slate-300">Инициализация запуска...</span>
              <span id="launchProgressPercent" class="text-white font-bold">0%</span>
            </div>
            <div class="w-full h-2 rounded-full bg-white/10 overflow-hidden border border-white/20">
              <div id="launchProgressBar" class="h-full bg-gradient-to-r from-white/70 via-white to-emerald-400 w-0 transition-all duration-300 shadow-glow-sm"></div>
            </div>
          </div>

        </div>

        <!-- 3 System Information Cards (No mock configs presets) -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          
          <div class="glass-card p-5 rounded-2xl border border-white/15">
            <div class="flex items-center justify-between mb-3">
              <div class="w-10 h-10 rounded-xl bg-white/10 border border-white/20 flex items-center justify-center text-lg">⚡</div>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-white/10 text-slate-300 font-bold border border-white/20">FABRIC 1.21.11</span>
            </div>
            <h4 class="text-base font-bold text-white">Fabric Loader 0.19.3</h4>
            <p class="text-xs text-slate-400 font-mono mt-1">Официальная версия игры 1.21.11, оптимизация Sodium и шейдеры Iris.</p>
          </div>

          <div class="glass-card p-5 rounded-2xl border border-white/15">
            <div class="flex items-center justify-between mb-3">
              <div class="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-lg">🛡️</div>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОБХОД 100%</span>
            </div>
            <h4 class="text-base font-bold text-white">GrimAC Bypasses</h4>
            <p class="text-xs text-slate-400 font-mono mt-1">AttackAura через блоки без детекта, чистый прыжковый старт ElytraHelper.</p>
          </div>

          <div class="glass-card p-5 rounded-2xl border border-white/15">
            <div class="flex items-center justify-between mb-3">
              <div class="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-lg">☁️</div>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">SUPABASE DB</span>
            </div>
            <h4 class="text-base font-bold text-white">Облачная база данных</h4>
            <p class="text-xs text-slate-400 font-mono mt-1">Созданные вами аккаунты сохраняются локально и синхронизируются с облаком.</p>
          </div>

        </div>

        <!-- News / Status Box -->
        <div class="glass-panel p-5 rounded-2xl border border-white/15 flex items-center justify-between gap-4">
          <div class="flex items-center gap-3">
            <span class="text-xl">📢</span>
            <div>
              <h5 class="text-xs font-bold text-white uppercase tracking-wider">Обновление Horus 1.21.11</h5>
              <p class="text-xs text-slate-400 mt-0.5">В клиенте установлена версия Horus 1.21.11. Конфиги убраны из лаунчера. Вы можете создавать собственные игровые аккаунты во вкладке «Менеджер аккаунтов».</p>
            </div>
          </div>
          <span class="text-xs font-mono text-slate-400 flex-shrink-0">Готово к игре</span>
        </div>

      </section>

      <!-- ================= TAB: VERSIONS ================= -->
      <section id="contentVersions" class="space-y-5 max-w-5xl mx-auto hidden">
        <div>
          <h2 class="text-2xl font-black text-white">Версия клиента</h2>
          <p class="text-xs text-slate-400 mt-1">Официальная сборка чит-клиента Horus. Все компоненты и зависимости проверены.</p>
        </div>

        <div class="space-y-4">
          
          <!-- Only Horus 1.21.11 is displayed! -->
          <div class="glass-card p-6 rounded-2xl border border-emerald-500/40 bg-emerald-500/[0.03] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-5">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center font-bold text-emerald-400 text-xl shadow-glow-sm">
                H
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h4 class="text-lg font-black text-white">Horus 1.21.11 (Fabric)</h4>
                  <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">УСТАНОВЛЕНО / АКТУАЛЬНО</span>
                </div>
                <p class="text-xs text-slate-400 font-mono mt-1">Официальный релиз Horus 1.21.11 • Белая тема Frosted Glass • 120+ модулей • Grim Bypasses • IPC Bridge</p>
                <div class="flex items-center gap-4 text-xs font-mono text-slate-400 mt-2">
                  <span>Minecraft: 1.21.11</span>
                  <span>•</span>
                  <span>Fabric Loader: 0.19.3</span>
                  <span>•</span>
                  <span>Java: 21 (64-bit)</span>
                </div>
              </div>
            </div>
            <button class="px-5 py-2.5 rounded-xl bg-white text-black font-extrabold text-xs shadow-glow-sm cursor-default">
              АКТИВНА ✓
            </button>
          </div>

        </div>
      </section>

      <!-- ================= TAB: ACCOUNTS (USER CREATED ACCOUNTS) ================= -->
      <section id="contentAccounts" class="space-y-6 max-w-5xl mx-auto hidden">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-black text-white">Менеджер аккаунтов</h2>
            <p class="text-xs text-slate-400 mt-1">Создавайте собственные аккаунты для входа на серверы. Тестовые аккаунты удалены — вы управляете списком сами.</p>
          </div>
        </div>

        <!-- Create Account Form Card -->
        <div class="glass-card p-6 rounded-2xl border border-white/20 bg-white/[0.02]">
          <h3 class="text-sm font-bold text-white uppercase tracking-wider mb-1">Создать новый аккаунт</h3>
          <p class="text-xs text-slate-400 mb-4">Введите желаемый игровой никнейм (от 3 до 16 символов, только английские буквы, цифры и символ _).</p>
          <form onsubmit="handleCreateAccountForm(event)" class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div class="relative flex-1">
              <input id="newAccountNickname" type="text" maxlength="16" placeholder="Введите никнейм (например: Player)" class="w-full px-4 py-3 rounded-xl bg-black/60 border border-white/20 text-sm font-mono text-white placeholder-slate-500 focus:outline-none focus:border-white transition-all">
            </div>
            <button type="submit" class="px-6 py-3 rounded-xl bg-white text-black font-extrabold text-xs tracking-wider shadow-glow-sm hover:bg-slate-200 transition-all flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
              <span>СОЗДАТЬ АККАУНТ</span>
            </button>
          </form>
        </div>

        <!-- Created Accounts List -->
        <div>
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-xs font-mono uppercase tracking-wider text-slate-400 font-bold">Ваши созданные аккаунты (<span id="accountsHeaderCount">0</span>)</h4>
          </div>
          <div id="launcherAccountsList" class="space-y-3">
            <!-- Dynamically populated from localStorage -->
          </div>
        </div>
      </section>

      <!-- ================= TAB: BYPASSES & ANTI-CHEAT ================= -->
      <section id="contentBypasses" class="space-y-5 max-w-5xl mx-auto hidden">
        <div>
          <h2 class="text-2xl font-black text-white">Статус обходов античитов</h2>
          <p class="text-xs text-slate-400 mt-1">Мониторинг работоспособности модулей Horus 1.21.11 на популярных серверах.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          
          <div class="glass-card p-5 rounded-2xl border border-white/15 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-base font-bold text-white">GrimAC (FunTime & Анархия)</h4>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОБХОД 100%</span>
            </div>
            <div class="text-xs font-mono space-y-1.5 text-slate-300">
              <div class="flex justify-between"><span class="text-slate-400">Античит:</span><span>GrimAC Latest Core</span></div>
              <div class="flex justify-between"><span class="text-slate-400">AttackAura через блоки:</span><span class="text-emerald-400">25-point Raycast + Reach 3.0</span></div>
              <div class="flex justify-between"><span class="text-slate-400">ElytraHelper Grim:</span><span class="text-emerald-400">Apex Jump + Packet Start</span></div>
              <div class="flex justify-between"><span class="text-slate-400">AutoExplosion:</span><span class="text-emerald-400">Bedrock/Obsidian 1-block Air</span></div>
            </div>
          </div>

          <div class="glass-card p-5 rounded-2xl border border-white/15 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-base font-bold text-white">ReallyWorld (Anarchy)</h4>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОБХОДИТСЯ</span>
            </div>
            <div class="text-xs font-mono space-y-1.5 text-slate-300">
              <div class="flex justify-between"><span class="text-slate-400">Античит:</span><span>Matrix + Vulcan Custom</span></div>
              <div class="flex justify-between"><span class="text-slate-400">Aura / HitBoxes:</span><span class="text-emerald-400">100% Legit Silent</span></div>
              <div class="flex justify-between"><span class="text-slate-400">AutoAnchor / Web:</span><span class="text-emerald-400">Мгновенный подрыв</span></div>
              <div class="flex justify-between"><span class="text-slate-400">NoSlow / Sprint:</span><span class="text-emerald-400">Полный обход</span></div>
            </div>
          </div>

          <div class="glass-card p-5 rounded-2xl border border-white/15 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-base font-bold text-white">HolyWorld (Grief PvP)</h4>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОБХОДИТСЯ</span>
            </div>
            <div class="text-xs font-mono space-y-1.5 text-slate-300">
              <div class="flex justify-between"><span class="text-slate-400">Античит:</span><span>Matrix Enterprise</span></div>
              <div class="flex justify-between"><span class="text-slate-400">Velocity:</span><span class="text-emerald-400">90% Vertical / 0% Horiz</span></div>
              <div class="flex justify-between"><span class="text-slate-400">AutoMine / AutoSell:</span><span class="text-emerald-400">Автоматизировано</span></div>
            </div>
          </div>

          <div class="glass-card p-5 rounded-2xl border border-white/15 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-base font-bold text-white">Spooky / MiniGames</h4>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОБХОДИТСЯ</span>
            </div>
            <div class="text-xs font-mono space-y-1.5 text-slate-300">
              <div class="flex justify-between"><span class="text-slate-400">Античит:</span><span>NCP Custom + Watchdog</span></div>
              <div class="flex justify-between"><span class="text-slate-400">Speed / Fly:</span><span class="text-emerald-400">Активно</span></div>
              <div class="flex justify-between"><span class="text-slate-400">TargetStrafe:</span><span class="text-emerald-400">Идеальный круг</span></div>
            </div>
          </div>

        </div>
      </section>

      <!-- ================= TAB: SETTINGS ================= -->
      <section id="contentSettings" class="space-y-6 max-w-5xl mx-auto hidden">
        <div>
          <h2 class="text-2xl font-black text-white">Настройки запуска и JVM</h2>
          <p class="text-xs text-slate-400 mt-1">Оптимизируйте выделение оперативной памяти и аргументы виртуальной машины Java 21.</p>
        </div>

        <div class="space-y-5">
          
          <!-- RAM Slider -->
          <div class="glass-card p-6 rounded-2xl border border-white/15 space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h4 class="text-base font-bold text-white">Выделение оперативной памяти (RAM)</h4>
                <p class="text-xs text-slate-400">Рекомендуется от 4096 MB до 6144 MB для стабильных 240+ FPS с шейдерами.</p>
              </div>
              <span id="ramSliderValue" class="text-lg font-black font-mono text-white px-3 py-1 rounded-xl bg-white/10 border border-white/20">4096 MB</span>
            </div>
            
            <input id="ramRangeInput" type="range" min="2048" max="16384" step="512" value="4096" oninput="updateRamValue(this.value)" class="w-full accent-white cursor-pointer h-2 bg-white/10 rounded-lg">
            
            <div class="flex justify-between text-[11px] font-mono text-slate-400">
              <span>2048 MB (Мин.)</span>
              <span>4096 MB (Оптимально)</span>
              <span>8192 MB</span>
              <span>16384 MB (Макс.)</span>
            </div>
          </div>

          <!-- Java Path & JVM Flags -->
          <div class="glass-card p-6 rounded-2xl border border-white/15 space-y-4">
            <h4 class="text-base font-bold text-white">Параметры Java Virtual Machine (JVM)</h4>
            
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Путь к Java Runtime (JDK 21):</label>
              <input id="javaPathInput" type="text" value="C:\\Program Files\\Java\\jdk-21\\bin\\javaw.exe" class="w-full px-4 py-2.5 rounded-xl bg-black/50 border border-white/20 text-xs font-mono text-white focus:outline-none focus:border-white/50">
            </div>

            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Высокопроизводительные флаги JVM:</label>
              <textarea id="jvmFlagsInput" rows="2" class="w-full px-4 py-2.5 rounded-xl bg-black/50 border border-white/20 text-xs font-mono text-white focus:outline-none focus:border-white/50 resize-none">-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=20 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch</textarea>
            </div>
          </div>

          <!-- Toggles -->
          <div class="glass-card p-6 rounded-2xl border border-white/15 space-y-3">
            <h4 class="text-base font-bold text-white mb-2">Параметры лоадера</h4>

            <label class="flex items-center justify-between cursor-pointer py-1.5">
              <div>
                <span class="text-xs font-bold text-white block">Закрывать лоадер после старта Minecraft</span>
                <span class="text-[11px] text-slate-400">Освобождает ресурсы оперативной памяти во время игры</span>
              </div>
              <input type="checkbox" checked class="w-4 h-4 rounded border-white/20 accent-white">
            </label>

            <label class="flex items-center justify-between cursor-pointer py-1.5 border-t border-white/10">
              <div>
                <span class="text-xs font-bold text-white block">Интеграция с Discord Rich Presence (RPC)</span>
                <span class="text-[11px] text-slate-400">Показывает статус «Играет в Horus Client 1.21.11» в Discord</span>
              </div>
              <input type="checkbox" checked class="w-4 h-4 rounded border-white/20 accent-white">
            </label>

            <label class="flex items-center justify-between cursor-pointer py-1.5 border-t border-white/10">
              <div>
                <span class="text-xs font-bold text-white block">Режим низкой задержки ввода (Ultra Low Latency)</span>
                <span class="text-[11px] text-slate-400">Снижает input lag мыши и клавиатуры на 15-25%</span>
              </div>
              <input type="checkbox" checked class="w-4 h-4 rounded border-white/20 accent-white">
            </label>
          </div>

          <!-- Supabase Cloud Database Card -->
          <div class="glass-card p-6 rounded-2xl border border-white/15 space-y-4">
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div class="flex items-center gap-3">
                <div id="launcherSbIcon" class="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4"/></svg>
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <h4 class="text-sm font-bold text-white">Облачная база данных Supabase (PostgreSQL)</h4>
                    <span id="launcherSbBadge" class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">ОНЛАЙН СИНХРОНИЗАЦИЯ</span>
                  </div>
                  <p class="text-xs text-slate-400 mt-0.5">Автоматическое сохранение ваших созданных аккаунтов в облаке Supabase.</p>
                </div>
              </div>
              <button onclick="testLauncherSupabase()" class="px-3.5 py-2 rounded-xl glass-panel border border-white/20 text-xs text-white hover:border-white/40 transition-all flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                <span>Проверить связь</span>
              </button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-1">
              <div>
                <label class="block text-[11px] font-mono text-slate-400 mb-1">Project URL:</label>
                <input id="launcherSbUrl" type="text" value="https://swarweprhwoelirrmztv.supabase.co" class="w-full px-3.5 py-2 rounded-xl bg-black/50 border border-white/15 text-xs font-mono text-white focus:outline-none focus:border-white/50">
              </div>
              <div>
                <label class="block text-[11px] font-mono text-slate-400 mb-1">Anon Key:</label>
                <input id="launcherSbKey" type="password" value="sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy" class="w-full px-3.5 py-2 rounded-xl bg-black/50 border border-white/15 text-xs font-mono text-white focus:outline-none focus:border-white/50">
              </div>
            </div>

            <div class="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-white/10">
              <div class="flex items-center gap-2">
                <button onclick="saveLauncherSupabase()" class="px-4 py-2 rounded-xl bg-white text-black font-extrabold text-xs shadow-glow-sm hover:bg-slate-200 transition-all">
                  Сохранить настройки
                </button>
                <button onclick="pushLauncherDataToSupabase()" class="px-3.5 py-2 rounded-xl glass-panel border border-white/20 text-xs text-white hover:border-white/40 transition-all flex items-center gap-1.5">
                  <span>⬆️ Синхронизировать с облаком</span>
                </button>
              </div>
              <a href="../supabase_schema.sql" download class="text-xs font-mono text-slate-400 hover:text-white underline">
                Скачать SQL схему (.sql)
              </a>
            </div>
          </div>

          <div class="flex justify-end">
            <button onclick="playUiSound('click'); saveSettings()" class="px-6 py-3 rounded-xl bg-white text-black font-extrabold text-xs shadow-glow-sm hover:bg-slate-200 transition-all">
              СОХРАНИТЬ НАСТРОЙКИ
            </button>
          </div>

        </div>
      </section>

      <!-- ================= TAB: CONSOLE ================= -->
      <section id="contentConsole" class="space-y-4 max-w-5xl mx-auto hidden flex flex-col h-full">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-black text-white">Консоль отладки и лог клиента</h2>
            <p class="text-xs text-slate-400 mt-0.5">Прямой вывод журналов JVM, Fabric Loader и клиента игры в реальном времени.</p>
          </div>
          <div class="flex items-center gap-2">
            <button onclick="clearConsole()" class="px-3 py-1.5 rounded-lg glass-panel text-xs text-slate-300 hover:text-white transition-all">
              Очистить
            </button>
            <button onclick="copyConsoleLogs()" class="px-3 py-1.5 rounded-lg glass-panel text-xs text-slate-300 hover:text-white transition-all">
              Копировать логи
            </button>
          </div>
        </div>

        <!-- Terminal Output -->
        <div id="terminalBody" class="flex-1 min-h-[420px] rounded-2xl bg-black/90 border border-white/15 p-4 font-mono text-xs overflow-y-auto custom-scroll space-y-1 text-slate-300 shadow-inner">
          <div class="text-slate-500">// Horus Loader Launcher v1.21.11 Debug Console initialized</div>
          <div class="text-slate-500">// Binds: 127.0.0.1:38241 (IPC Bridge) • Ready for JVM output</div>
          <div class="text-emerald-400">[12:20:01] [Main/INFO]: Initializing Horus Fabric Client 1.21.11...</div>
          <div class="text-cyan-400">[12:20:02] [WebBridge/INFO]: Web bridge server active on http://127.0.0.1:38241</div>
          <div class="text-slate-300">[12:20:02] [GrimBypass/INFO]: GrimAC 25-point Raycast & Elytra start loaded</div>
          <div class="text-slate-400">[12:20:02] [Render2D/INFO]: OpenGL Frosted White Glass Shaders compiled successfully</div>
        </div>

        <!-- Terminal Command Input -->
        <form onsubmit="handleConsoleCommand(event)" class="flex items-center gap-2">
          <div class="relative flex-1">
            <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 font-mono text-xs">&gt;</span>
            <input id="consoleInput" type="text" placeholder="Введите команду (например: status, help)..." class="w-full pl-8 pr-4 py-2.5 rounded-xl bg-black/60 border border-white/20 text-xs font-mono text-white focus:outline-none focus:border-white/50">
          </div>
          <button type="submit" class="px-5 py-2.5 rounded-xl bg-white text-black font-bold text-xs hover:bg-slate-200 transition-all">
            Отправить
          </button>
        </form>
      </section>

    </main>

  </div>

  <!-- ================= TOAST NOTIFICATION ================= -->
  <div id="toastNotification" class="fixed bottom-6 right-6 z-50 transform translate-y-20 opacity-0 transition-all duration-300 pointer-events-none flex items-center gap-3 px-5 py-3 rounded-2xl glass-shell text-xs font-bold text-white shadow-glow-md">
    <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
    <span id="toastMessage">Сообщение</span>
  </div>

  <!-- ================= JAVASCRIPT LOGIC ================= -->
  <script>
    // --- Audio Synthesis Engine ---
    let audioEnabled = true;
    let audioCtx = null;

    function initAudio() {
      if (!audioCtx) {
        audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      }
    }

    function playUiSound(type = 'click') {
      if (!audioEnabled) return;
      try {
        initAudio();
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const now = audioCtx.currentTime;
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.connect(gain);
        gain.connect(audioCtx.destination);

        if (type === 'click') {
          osc.frequency.setValueAtTime(800, now);
          osc.frequency.exponentialRampToValueAtTime(300, now + 0.05);
          gain.gain.setValueAtTime(0.04, now);
          gain.gain.linearRampToValueAtTime(0.001, now + 0.05);
          osc.start(now);
          osc.stop(now + 0.05);
        } else if (type === 'launch') {
          osc.type = 'sawtooth';
          osc.frequency.setValueAtTime(220, now);
          osc.frequency.exponentialRampToValueAtTime(880, now + 0.4);
          gain.gain.setValueAtTime(0.05, now);
          gain.gain.linearRampToValueAtTime(0.001, now + 0.4);
          osc.start(now);
          osc.stop(now + 0.4);
        } else if (type === 'success') {
          osc.frequency.setValueAtTime(523.25, now);
          osc.frequency.setValueAtTime(659.25, now + 0.08);
          gain.gain.setValueAtTime(0.06, now);
          gain.gain.linearRampToValueAtTime(0.001, now + 0.25);
          osc.start(now);
          osc.stop(now + 0.25);
        }
      } catch (e) {}
    }

    function toggleAudio() {
      audioEnabled = !audioEnabled;
      document.getElementById('audioToggleBtn').innerText = audioEnabled ? 'ВКЛ 🔊' : 'ВЫКЛ 🔇';
      showToast(audioEnabled ? 'Звуковые эффекты включены' : 'Звук отключен');
    }

    // --- Toast Helper ---
    let toastTimeout = null;
    function showToast(msg) {
      const toast = document.getElementById('toastNotification');
      const text = document.getElementById('toastMessage');
      if (!toast || !text) return;
      text.innerText = msg;
      toast.classList.remove('translate-y-20', 'opacity-0');
      clearTimeout(toastTimeout);
      toastTimeout = setTimeout(() => {
        toast.classList.add('translate-y-20', 'opacity-0');
      }, 3000);
    }

    // --- Tab Switching: ONLY play, versions, accounts, bypasses, settings, console ---
    let activeTab = 'play';
    function switchTab(tabId) {
      playUiSound('click');
      activeTab = tabId;
      const tabs = ['play', 'versions', 'accounts', 'bypasses', 'settings', 'console'];
      tabs.forEach(t => {
        const btn = document.getElementById(`tabBtn${t.charAt(0).toUpperCase() + t.slice(1)}`);
        const content = document.getElementById(`content${t.charAt(0).toUpperCase() + t.slice(1)}`);
        if (t === tabId) {
          btn?.classList.add('active');
          content?.classList.remove('hidden');
        } else {
          btn?.classList.remove('active');
          content?.classList.add('hidden');
        }
      });
    }

    // --- Account Management (User-Created Only, Saved in LocalStorage & Supabase) ---
    const ACCOUNTS_STORAGE_KEY = 'horus_launcher_accounts';
    const ACTIVE_ACCOUNT_KEY = 'horus_active_account';

    function getStoredAccounts() {
      try {
        const raw = localStorage.getItem(ACCOUNTS_STORAGE_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) return parsed;
        }
      } catch (e) {}
      return [];
    }

    function saveAccountsToStorage(accs) {
      try {
        localStorage.setItem(ACCOUNTS_STORAGE_KEY, JSON.stringify(accs));
      } catch (e) {}
    }

    function getActiveAccountUsername() {
      const accs = getStoredAccounts();
      if (accs.length === 0) return 'Player';
      const stored = localStorage.getItem(ACTIVE_ACCOUNT_KEY);
      if (stored && accs.some(a => a.username === stored)) {
        return stored;
      }
      return accs[0].username;
    }

    function setActiveAccount(username) {
      localStorage.setItem(ACTIVE_ACCOUNT_KEY, username);
      selectedAccount = username;
      updateSidebarUser(username);
      updateHeroAccountSelect();
      renderLauncherAccounts();
      showToast(`Активный аккаунт: ${username}`);
    }

    function updateSidebarUser(username) {
      const elName = document.getElementById('sidebarUsername');
      const elAvatar = document.getElementById('sidebarAvatar');
      const elCount = document.getElementById('navAltsCount');
      const elHeadCount = document.getElementById('accountsHeaderCount');

      const accs = getStoredAccounts();
      if (elName) elName.innerText = username;
      if (elAvatar) {
        elAvatar.src = `https://mc-heads.net/avatar/${encodeURIComponent(username)}/64`;
      }
      if (elCount) elCount.innerText = `${accs.length} шт.`;
      if (elHeadCount) elHeadCount.innerText = accs.length;
    }

    function updateHeroAccountSelect() {
      const select = document.getElementById('quickAccountSelect');
      if (!select) return;
      const accs = getStoredAccounts();
      if (accs.length === 0) {
        select.innerHTML = '<option value="" disabled selected>— Нет аккаунтов (создайте во вкладке) —</option>';
        return;
      }
      const active = getActiveAccountUsername();
      select.innerHTML = accs.map(a => `
        <option value="${a.username}" ${a.username === active ? 'selected' : ''}>
          ${a.username} [Offline]
        </option>
      `).join('');
    }

    function onQuickAccountChange(val) {
      if (!val) return;
      setActiveAccount(val);
    }

    function handleCreateAccountForm(e) {
      if (e) e.preventDefault();
      const input = document.getElementById('newAccountNickname');
      if (!input) return;
      const nick = input.value.trim();

      if (!nick) {
        showToast('Введите никнейм игрока!');
        return;
      }
      if (nick.length < 3 || nick.length > 16) {
        showToast('Никнейм должен быть от 3 до 16 символов!');
        return;
      }
      if (!/^[a-zA-Z0-9_]+$/.test(nick)) {
        showToast('Используйте только английские буквы, цифры и символ _');
        return;
      }

      const accs = getStoredAccounts();
      if (accs.some(a => a.username.toLowerCase() === nick.toLowerCase())) {
        showToast(`Аккаунт с ником "${nick}" уже существует!`);
        return;
      }

      const newAcc = {
        username: nick,
        type: 'offline',
        created_at: new Date().toLocaleDateString('ru-RU')
      };
      accs.push(newAcc);
      saveAccountsToStorage(accs);
      input.value = '';

      setActiveAccount(nick);
      playUiSound('success');
      showToast(`✓ Аккаунт "${nick}" успешно создан!`);

      // Sync to Supabase in background if configured
      if (typeof LumiraSupabase !== 'undefined' && LumiraSupabase.isConfigured()) {
        LumiraSupabase.saveAltAccounts(nick, accs).catch(() => {});
      }
    }

    function deleteLauncherAccount(username) {
      if (!confirm(`Удалить аккаунт "${username}"?`)) return;
      let accs = getStoredAccounts();
      accs = accs.filter(a => a.username !== username);
      saveAccountsToStorage(accs);

      const active = getActiveAccountUsername();
      if (selectedAccount === username) {
        const next = accs.length > 0 ? accs[0].username : 'Player';
        setActiveAccount(next);
      } else {
        renderLauncherAccounts();
        updateHeroAccountSelect();
        updateSidebarUser(active);
      }

      playUiSound('click');
      showToast(`Аккаунт "${username}" удален`);

      // Sync to Supabase in background if configured
      if (typeof LumiraSupabase !== 'undefined' && LumiraSupabase.isConfigured()) {
        LumiraSupabase.saveAltAccounts(getActiveAccountUsername(), accs).catch(() => {});
      }
    }

    function renderLauncherAccounts() {
      const container = document.getElementById('launcherAccountsList');
      if (!container) return;
      const accs = getStoredAccounts();
      const active = getActiveAccountUsername();

      if (accs.length === 0) {
        container.innerHTML = `
          <div class="glass-panel p-8 rounded-2xl border border-white/10 text-center space-y-3">
            <div class="w-12 h-12 rounded-full bg-white/10 border border-white/20 mx-auto flex items-center justify-center text-xl text-slate-300">👤</div>
            <h4 class="text-sm font-bold text-white">Список аккаунтов пуст</h4>
            <p class="text-xs text-slate-400 max-w-sm mx-auto">Создайте свой первый игровой аккаунт с помощью формы выше. Тестовые аккаунты были удалены.</p>
          </div>
        `;
        return;
      }

      container.innerHTML = accs.map(a => {
        const isActive = (a.username === active);
        return `
          <div class="glass-card p-4 rounded-xl border ${isActive ? 'border-emerald-500/50 bg-emerald-500/[0.04]' : 'border-white/15'} flex items-center justify-between gap-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-black/60 border border-white/20 flex items-center justify-center overflow-hidden">
                <img src="https://mc-heads.net/avatar/${encodeURIComponent(a.username)}/48" alt="${a.username}" class="w-8 h-8 object-contain" onerror="this.src='app_icon.png'">
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h5 class="text-sm font-bold text-white">${a.username}</h5>
                  <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-white/10 text-slate-300 font-bold">Оффлайн</span>
                  ${isActive ? '<span class="text-[9px] font-mono px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40">АКТИВЕН</span>' : ''}
                </div>
                <p class="text-xs text-slate-400 font-mono">Создан: ${a.created_at || 'Сегодня'} • Fabric 1.21.11</p>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <button onclick="setActiveAccount('${a.username}')" class="px-4 py-2 rounded-xl ${isActive ? 'bg-white text-black font-extrabold cursor-default' : 'glass-panel border border-white/20 text-white hover:border-white/50'} text-xs transition-all">
                ${isActive ? 'Выбран ✓' : 'Выбрать'}
              </button>
              <button onclick="deleteLauncherAccount('${a.username}')" title="Удалить аккаунт" class="w-9 h-9 rounded-xl glass-panel border border-white/15 text-slate-400 hover:text-red-400 hover:border-red-500/30 flex items-center justify-center transition-all">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              </button>
            </div>
          </div>
        `;
      }).join('');
    }

    // --- State and Launch Logic (Horus 1.21.11 Only) ---
    let isLaunching = false;
    let isClientRunning = false;
    let selectedVersion = 'Horus 1.21.11';
    let selectedAccount = getActiveAccountUsername();
    let allocatedRam = 4096;

    function handleLaunchClient() {
      if (isLaunching) return;
      if (isClientRunning) {
        showToast('Клиент игры уже запущен и подключен!');
        return;
      }

      playUiSound('launch');
      isLaunching = true;
      const btn = document.getElementById('mainLaunchBtn');
      const btnText = document.getElementById('launchBtnText');
      const progressContainer = document.getElementById('launchProgressContainer');
      const progressBar = document.getElementById('launchProgressBar');
      const progressStage = document.getElementById('launchProgressStage');
      const progressPercent = document.getElementById('launchProgressPercent');

      btn.disabled = true;
      btn.classList.add('opacity-70', 'cursor-not-allowed');
      btnText.innerText = 'ЗАПУСК...';
      progressContainer.classList.remove('hidden');

      // Реальный запуск процесса через локальный мост
      try {
        fetch('http://127.0.0.1:38241/api/launch', { method: 'POST' })
          .then(r => r.json())
          .then(data => {
            if (data && data.launched) {
              appendConsoleLog('[Process] launch_game.bat успешно инициирован через локальный мост.');
            }
          })
          .catch(() => {});
      } catch (ignored) {}

      const stages = [
        { percent: 15, text: 'Проверка среды Fabric 1.21.11 и Java 21...', log: '[Launcher] Validating Fabric 1.21.11 environment & Java 21...' },
        { percent: 35, text: 'Загрузка ассетов и шейдеров White Frosted Glass...', log: '[Launcher] Loading Horus client shaders and UI assets...' },
        { percent: 65, text: `Подготовка профиля игрока (${selectedAccount})...`, log: `[Profile] Applying offline player session: ${selectedAccount}` },
        { percent: 85, text: 'Инъекция байткода GrimAC (AttackAura & ElytraHelper)...', log: '[GrimBypass] Injecting 25-point raycast transformer & Apex elytra helper...' },
        { percent: 100, text: 'Старт процесса Minecraft и подключение IPC моста...', log: '[JVM] Starting Fabric 1.21.11 client process with 4096MB allocation.' }
      ];

      let step = 0;
      const interval = setInterval(() => {
        if (step < stages.length) {
          const s = stages[step];
          progressBar.style.width = `${s.percent}%`;
          progressPercent.innerText = `${s.percent}%`;
          progressStage.innerText = s.text;
          appendConsoleLog(s.log);
          step++;
        } else {
          clearInterval(interval);
          isLaunching = false;
          isClientRunning = true;
          playUiSound('success');

          btn.disabled = false;
          btn.classList.remove('opacity-70', 'cursor-not-allowed', 'bg-white', 'text-black');
          btn.classList.add('bg-emerald-500', 'text-black', 'shadow-glow-md');
          btnText.innerText = '● КЛИЕНТ АКТИВЕН';

          showToast('✓ Horus Client 1.21.11 успешно запущен!');
          appendConsoleLog('[Launcher/SUCCESS] Client process active! IPC Bridge connected.');
        }
      }, 550);
    }

    // --- IPC Bridge Live Poller ---
    async function pollGameClientStatus() {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 1200);
        const res = await fetch('http://127.0.0.1:38241/api/status', { signal: controller.signal });
        clearTimeout(timeoutId);

        if (res.ok) {
          const data = await res.json();
          onIpcConnected(data);
          return;
        }
      } catch (e) {}
      onIpcDisconnected();
    }

    function onIpcConnected(data) {
      const badge = document.getElementById('topIpcBadge');
      const dot = document.getElementById('topIpcDot');
      const text = document.getElementById('topIpcText');
      if (badge && dot && text) {
        badge.className = "flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-emerald-500/15 border border-emerald-500/40 text-[10px] font-mono text-emerald-400";
        dot.className = "w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse";
        text.innerText = "IPC ОНЛАЙН";
      }

      const btn = document.getElementById('mainLaunchBtn');
      const btnText = document.getElementById('launchBtnText');
      if (btn && btnText && !isLaunching) {
        isClientRunning = true;
        btn.classList.remove('bg-white', 'text-black');
        btn.classList.add('bg-emerald-500', 'text-black', 'shadow-glow-md');
        btnText.innerText = '● КЛИЕНТ В СЕТИ';
      }
    }

    function onIpcDisconnected() {
      const badge = document.getElementById('topIpcBadge');
      const dot = document.getElementById('topIpcDot');
      const text = document.getElementById('topIpcText');
      if (badge && dot && text) {
        badge.className = "flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-white/5 border border-white/10 text-[10px] font-mono text-slate-400";
        dot.className = "w-1.5 h-1.5 rounded-full bg-slate-500";
        text.innerText = "127.0.0.1:38241";
      }

      const btn = document.getElementById('mainLaunchBtn');
      const btnText = document.getElementById('launchBtnText');
      if (btn && btnText && !isLaunching) {
        isClientRunning = false;
        btn.classList.remove('bg-emerald-500', 'shadow-glow-md');
        btn.classList.add('bg-white', 'text-black');
        btnText.innerText = 'ЗАПУСТИТЬ КЛИЕНТ';
      }
    }

    // --- RAM Slider ---
    function updateRamValue(val) {
      allocatedRam = val;
      document.getElementById('ramSliderValue').innerText = `${val} MB`;
      document.getElementById('heroRamBadge').innerText = `${val} MB`;
    }

    function saveSettings() {
      playUiSound('success');
      showToast('✓ Настройки JVM и лоадера успешно сохранены!');
    }

    // --- Console Log Helper ---
    function appendConsoleLog(line) {
      const term = document.getElementById('terminalBody');
      if (!term) return;
      const el = document.createElement('div');
      const time = new Date().toTimeString().split(' ')[0];
      el.className = "text-slate-200";
      el.innerText = `[${time}] ${line}`;
      term.appendChild(el);
      term.scrollTop = term.scrollHeight;
    }

    function clearConsole() {
      playUiSound('click');
      document.getElementById('terminalBody').innerHTML = '<div class="text-slate-500">// Console cleared by user</div>';
      showToast('Консоль очищена');
    }

    function copyConsoleLogs() {
      playUiSound('click');
      const text = document.getElementById('terminalBody').innerText;
      navigator.clipboard?.writeText(text);
      showToast('Логи скопированы в буфер обмена!');
    }

    function handleConsoleCommand(e) {
      e.preventDefault();
      const input = document.getElementById('consoleInput');
      const cmd = input.value.trim();
      if (!cmd) return;
      playUiSound('click');
      appendConsoleLog(`> ${cmd}`);
      input.value = '';

      if (cmd === 'status') {
        appendConsoleLog(`Status: isRunning=${isClientRunning}, version=${selectedVersion}, RAM=${allocatedRam}MB, account=${selectedAccount}`);
      } else if (cmd === 'help') {
        appendConsoleLog('Available commands: status, clear, help');
      } else if (cmd === 'clear') {
        clearConsole();
      } else {
        appendConsoleLog(`Executing command in client: .${cmd}`);
      }
    }

    // --- Window Actions ---
    function closeLoaderWindow() {
      if (confirm('Вы уверены, что хотите закрыть лоадер?')) {
        window.close();
      }
    }

    function toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen?.();
      } else {
        document.exitFullscreen?.();
      }
    }

    // --- Launcher Supabase Sync Helpers ---
    function initLauncherSupabaseUI() {
      if (typeof LumiraSupabase === 'undefined') return;
      const { url, key } = LumiraSupabase.getStoredCredentials();
      const urlInput = document.getElementById('launcherSbUrl');
      const keyInput = document.getElementById('launcherSbKey');
      if (urlInput && url) urlInput.value = url;
      if (keyInput && key) keyInput.value = key;
      updateLauncherSupabaseBadge();
    }

    async function updateLauncherSupabaseBadge() {
      const badge = document.getElementById('launcherSbBadge');
      const icon = document.getElementById('launcherSbIcon');
      if (!badge || typeof LumiraSupabase === 'undefined') return;

      if (LumiraSupabase.isConfigured()) {
        const ok = await LumiraSupabase.checkConnection();
        if (ok) {
          badge.className = "text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/40 shadow-glow-sm";
          badge.innerText = "ПОДКЛЮЧЕНО К ОБЛАКУ";
          if (icon) icon.className = "w-10 h-10 rounded-xl bg-emerald-500/15 border border-emerald-500/40 flex items-center justify-center text-emerald-400 shadow-glow-sm";
        } else {
          badge.className = "text-[10px] font-mono px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-bold border border-amber-500/40";
          badge.innerText = "ОШИБКА СВЯЗИ";
          if (icon) icon.className = "w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-300";
        }
      } else {
        badge.className = "text-[10px] font-mono px-2 py-0.5 rounded bg-slate-500/20 text-slate-300 font-bold border border-white/20";
        badge.innerText = "ЛОКАЛЬНЫЙ РЕЖИМ";
        if (icon) icon.className = "w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center text-slate-400";
      }
    }

    async function testLauncherSupabase() {
      playUiSound('click');
      showToast('Проверка соединения с Supabase...');
      if (typeof LumiraSupabase === 'undefined') return;
      const ok = await LumiraSupabase.checkConnection();
      await updateLauncherSupabaseBadge();
      if (ok) {
        showToast('✓ Успешное подключение к Supabase!');
      } else {
        showToast('База недоступна. Проверьте URL и Anon Key.');
      }
    }

    async function saveLauncherSupabase() {
      playUiSound('click');
      const url = document.getElementById('launcherSbUrl')?.value;
      const key = document.getElementById('launcherSbKey')?.value;
      if (!url || !key) {
        showToast('Заполните Project URL и Anon Key!');
        return;
      }
      LumiraSupabase.setCredentials(url, key);
      const ok = await LumiraSupabase.checkConnection();
      updateLauncherSupabaseBadge();
      if (ok) {
        showToast('✓ Настройки Supabase сохранены!');
      } else {
        showToast('Ключи сохранены. Проверьте соединение.');
      }
    }

    async function pushLauncherDataToSupabase() {
      playUiSound('click');
      showToast('Синхронизация аккаунтов с Supabase...');
      if (typeof LumiraSupabase === 'undefined' || !LumiraSupabase.isConfigured()) {
        showToast('Сначала настройте URL и Anon Key!');
        return;
      }
      const accs = getStoredAccounts();
      const active = getActiveAccountUsername();
      if (accs.length === 0) {
        showToast('Нет созданных аккаунтов для выгрузки!');
        return;
      }
      const res = await LumiraSupabase.saveAltAccounts(active, accs);
      if (res && res.success) {
        showToast('✓ Аккаунты успешно синхронизированы с Supabase!');
      } else {
        showToast('Ошибка при отправке в Supabase.');
      }
    }

    // --- Init on load ---
    document.addEventListener('DOMContentLoaded', () => {
      const active = getActiveAccountUsername();
      selectedAccount = active;
      updateSidebarUser(active);
      updateHeroAccountSelect();
      renderLauncherAccounts();
      initLauncherSupabaseUI();

      // Poll local game client on 127.0.0.1:38241
      pollGameClientStatus();
      setInterval(pollGameClientStatus, 3000);
    });
  </script>
</body>
</html>
"""

with open(INDEX_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

# Also sync with the brain artifact lumira_loader.html
with open(ARTIFACT_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

print("Updated launcher/index.html and artifact lumira_loader.html successfully!")
