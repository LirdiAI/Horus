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
  <title>PulseVisuals — Horus Client 1.21.11</title>
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600;700&display=swap" rel="stylesheet">
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    :root {
      --bg-dark: #0f1015;
      --sidebar-bg: #111117;
      --card-bg: #171922;
      --card-border: rgba(255, 255, 255, 0.07);
      --purple: #6448e3;
      --purple-hover: #755bf2;
      --purple-light: #8b78f6;
    }

    body {
      background-color: #0f1015;
      background-image: 
        radial-gradient(circle at 75% 15%, rgba(100, 72, 227, 0.12) 0%, transparent 45%),
        radial-gradient(circle at 10% 85%, rgba(59, 130, 246, 0.06) 0%, transparent 40%);
      color: #ffffff;
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      overflow: hidden;
      height: 100vh;
      width: 100vw;
    }

    /* Shader Card with Frosted Glass */
    .shader-card {
      background: rgba(23, 25, 34, 0.85);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 16px;
      transition: all 0.22s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .shader-card:hover {
      border-color: rgba(255, 255, 255, 0.16);
      box-shadow: 0 12px 35px rgba(0, 0, 0, 0.5), 0 0 20px rgba(100, 72, 227, 0.15);
    }

    /* Vibrant Purple Glow Button (Exactly like PulseVisuals Play Button) */
    .btn-play-purple {
      background: linear-gradient(135deg, #6448e3 0%, #5237d8 100%);
      box-shadow: 0 4px 20px rgba(100, 72, 227, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.25);
      border-radius: 10px;
      color: #ffffff;
      font-weight: 700;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .btn-play-purple:hover {
      background: linear-gradient(135deg, #745bf2 0%, #5e43e6 100%);
      box-shadow: 0 6px 28px rgba(100, 72, 227, 0.65), inset 0 1px 0 rgba(255, 255, 255, 0.35);
      transform: translateY(-1px);
    }

    .btn-play-purple:active {
      transform: translateY(1px);
      box-shadow: 0 2px 10px rgba(100, 72, 227, 0.4);
    }

    /* Sidebar Active Tab Pill */
    .nav-btn.active {
      background: #6448e3;
      color: #ffffff;
      box-shadow: 0 4px 18px rgba(100, 72, 227, 0.4);
      border-radius: 10px;
    }

    /* News & Cosmetic Card Styling */
    .news-card {
      background: #171922;
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 14px;
      transition: all 0.2s ease;
    }

    .news-card:hover {
      background: #1d202c;
      border-color: rgba(255, 255, 255, 0.14);
      transform: translateY(-2px);
    }

    .cosmetic-card {
      background: #171922;
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 14px;
      transition: all 0.22s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .cosmetic-card:hover {
      background: #1d202c;
      border-color: rgba(100, 72, 227, 0.4);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5), 0 0 16px rgba(100, 72, 227, 0.2);
      transform: translateY(-2px);
    }

    /* Custom Scrollbar */
    .custom-scroll::-webkit-scrollbar {
      width: 5px;
    }
    .custom-scroll::-webkit-scrollbar-track {
      background: transparent;
    }
    .custom-scroll::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.12);
      border-radius: 9999px;
    }
    .custom-scroll::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 255, 255, 0.25);
    }
  </style>
</head>
<body class="flex flex-col h-screen overflow-hidden text-sm select-none">

  <!-- ================= TOP WINDOW FRAME / HEADER ================= -->
  <header class="h-11 flex-shrink-0 flex items-center justify-between px-5 z-50">
    
    <!-- Left: Pulse Logo & Greeting -->
    <div class="flex items-center gap-3">
      <div class="flex items-center gap-1.5 font-bold text-white tracking-wide">
        <span class="text-xl text-[#6448e3] font-black">∿</span>
        <span class="text-sm font-extrabold tracking-tight">PulseVisuals</span>
      </div>
      <div class="text-xs text-slate-400 font-medium">
        С возвращением, <span id="headerGreetingNick" class="text-slate-300 font-semibold">HorusPegas</span>!
      </div>
    </div>

    <!-- Right: Gift Button & Window Controls -->
    <div class="flex items-center gap-3">
      <button onclick="openGiftModal()" class="btn-play-purple px-4 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 shadow-md">
        <span>🎁</span>
        <span>Открыть подарок</span>
      </button>

      <div class="flex items-center gap-1 text-slate-400">
        <button onclick="minimizeWindow()" class="w-7 h-7 rounded-lg flex items-center justify-center hover:text-white hover:bg-white/10 transition-colors">
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/></svg>
        </button>
        <button onclick="toggleFullscreen()" class="w-7 h-7 rounded-lg flex items-center justify-center hover:text-white hover:bg-white/10 transition-colors">
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><rect width="18" height="18" x="3" y="3" rx="2" stroke-width="2"/></svg>
        </button>
        <button onclick="closeAppWindow()" class="w-7 h-7 rounded-lg flex items-center justify-center hover:text-white hover:bg-red-500/30 transition-colors">
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
        </button>
      </div>
    </div>

  </header>

  <!-- ================= MAIN LAUNCHER BODY ================= -->
  <div class="flex-1 flex overflow-hidden px-4 pb-4 gap-4">

    <!-- ================= LEFT SIDEBAR ================= -->
    <aside class="w-44 flex-shrink-0 flex flex-col justify-between py-1 select-none">
      
      <!-- Navigation Tabs -->
      <nav class="space-y-1 font-semibold text-xs text-slate-400">
        
        <button onclick="switchTab('main')" id="tabBtnMain" class="nav-btn active w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
          <span>Главная</span>
        </button>

        <button onclick="switchTab('versions')" id="tabBtnVersions" class="nav-btn w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left hover:text-white hover:bg-white/5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
          <span>Версии</span>
        </button>

        <button onclick="switchTab('accounts')" id="tabBtnAccounts" class="nav-btn w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left hover:text-white hover:bg-white/5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
          <span>Аккаунты</span>
        </button>

        <button onclick="switchTab('bypasses')" id="tabBtnBypasses" class="nav-btn w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left hover:text-white hover:bg-white/5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/></svg>
          <span>Мастерская</span>
        </button>

        <button onclick="switchTab('news')" id="tabBtnNews" class="nav-btn w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left hover:text-white hover:bg-white/5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/></svg>
          <span>Новости</span>
        </button>

        <button onclick="switchTab('settings')" id="tabBtnSettings" class="nav-btn w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl transition-all text-left hover:text-white hover:bg-white/5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          <span>Настройки</span>
        </button>

      </nav>

      <!-- Bottom Profile Box (Matches Screenshot 1:1) -->
      <div class="space-y-2">
        <div class="p-3 rounded-2xl bg-[#171922] border border-white/5 space-y-2 shadow-lg">
          <div class="flex items-center gap-2.5">
            <div class="w-10 h-10 rounded-xl overflow-hidden bg-black/60 border border-white/10 flex-shrink-0 flex items-center justify-center">
              <img id="sidebarAvatar" src="https://mc-heads.net/avatar/HorusPegas/48" class="w-full h-full object-contain" onerror="this.src='app_icon.png'">
            </div>
            <div class="min-w-0 flex-1">
              <span class="text-[9px] font-bold px-1.5 py-0.5 rounded-md bg-[#241e3d] text-[#c4b5fd] border border-[#3c3066]">👤 Юзер</span>
              <div id="sidebarUsername" class="text-xs font-bold text-white truncate mt-0.5">HorusPegas #4370</div>
            </div>
          </div>
          
          <div class="flex items-center justify-between text-xs pt-1 border-t border-white/5">
            <div class="flex items-center gap-2 text-slate-400">
              <button onclick="switchTab('accounts')" title="Сменить аккаунт" class="hover:text-white transition-colors">⮌</button>
              <button onclick="switchTab('accounts')" title="Редактировать" class="hover:text-white transition-colors">✏️</button>
              <button onclick="switchTab('bypasses')" title="Косметика" class="hover:text-white transition-colors">💳</button>
            </div>
            <span class="text-[10px] font-bold text-[#a78bfa]">20 дн.</span>
          </div>
        </div>

        <div class="text-[9px] text-[#4b5563] text-center font-medium">
          ©2026 All Rights Reserved
        </div>
      </div>

    </aside>

    <!-- ================= RIGHT MAIN AREA ================= -->
    <main class="flex-1 overflow-y-auto custom-scroll relative">

      <!-- ================= TAB 1: MAIN (PULSEVISUALS SCREENSHOT REPLICA) ================= -->
      <section id="contentMain" class="space-y-4">
        
        <!-- Hero Banner with Minecraft Steve & Amethyst Art -->
        <div class="relative overflow-hidden rounded-2xl bg-[#171922] border border-white/10 min-h-[212px] flex items-center justify-between p-7 shadow-2xl">
          
          <!-- Minecraft Artwork on Right with CSS Mask Gradient -->
          <div class="absolute inset-y-0 right-0 w-3/5 bg-cover bg-right bg-no-repeat pointer-events-none" style="background-image: url('steve_art.png'); mask-image: linear-gradient(to right, transparent 0%, black 40%); -webkit-mask-image: linear-gradient(to right, transparent 0%, black 40%);"></div>

          <!-- Left Overlay -->
          <div class="relative z-10 max-w-md space-y-3">
            <div>
              <h1 class="text-3xl font-extrabold tracking-tight text-white leading-tight">
                Добро пожаловать<br>
                <span class="text-[#8b78f6] drop-shadow-[0_0_25px_rgba(139,120,246,0.6)]">в Pulse Visuals</span>
              </h1>
              <p class="text-xs text-slate-400 mt-1 font-medium">Твой путь, твой стиль, твоя игра</p>
            </div>

            <!-- Actions Row: [⚔ Играть] + [Fabric 1.21.11 v] -->
            <div class="flex items-center gap-3 pt-1">
              <button id="mainLaunchBtn" onclick="handleLaunchClient()" class="btn-play-purple px-6 py-2.5 flex items-center gap-2.5 text-xs font-bold active:scale-95 transition-all">
                <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/></svg>
                <span id="launchBtnText">Играть</span>
              </button>

              <div class="flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-[#171922] border border-white/10 text-xs font-semibold text-white shadow-sm cursor-pointer hover:border-white/20 transition-all">
                <svg class="w-4 h-4 text-slate-400" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
                <span>Fabric 1.21.11</span>
                <svg class="w-3.5 h-3.5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
              </div>
            </div>

            <!-- Nickname Pill -->
            <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#12141c]/90 border border-white/10 text-xs shadow-inner">
              <span class="text-[#8b78f6] font-bold text-sm">∿</span>
              <span class="text-slate-400 font-medium">Никнейм:</span>
              <img id="heroPillAvatar" src="https://mc-heads.net/avatar/HorusPegas/32" class="w-4 h-4 rounded" onerror="this.src='app_icon.png'">
              <span id="heroPillUsername" class="font-bold text-white">HorusPegas</span>
            </div>
          </div>

        </div>

        <!-- Bottom Two-Column Grid: Новости (Left) & Топ косметики (Right) -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
          
          <!-- LEFT COLUMN: НОВОСТИ -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="text-sm font-bold text-white flex items-center gap-2">
                <span>📰</span> Новости
              </h3>
              <button onclick="switchTab('news')" class="text-xs text-slate-400 hover:text-white transition-colors">Показать все</button>
            </div>

            <div class="space-y-2.5">
              <!-- Item 1: Обновление -->
              <div class="news-card p-4 space-y-1.5 cursor-pointer">
                <div class="flex items-center gap-2 text-xs">
                  <span class="px-2 py-0.5 rounded-md bg-[#6448e3] text-white text-[10px] font-bold">Обновление</span>
                  <span class="text-slate-400 text-[11px] font-mono">12.05.2026</span>
                </div>
                <h4 class="text-sm font-bold text-white">Новые функции в 1.21.11</h4>
                <p class="text-xs text-slate-400 line-clamp-1">Разработчики добавили новые модули и улучшили обходы GrimAC...</p>
              </div>

              <!-- Item 2: Мир -->
              <div class="news-card p-4 space-y-1.5 cursor-pointer">
                <div class="flex items-center gap-2 text-xs">
                  <span class="px-2 py-0.5 rounded-md bg-[#3b82f6] text-white text-[10px] font-bold">Мир</span>
                  <span class="text-slate-400 text-[11px] font-mono">10.05.2026</span>
                </div>
                <h4 class="text-sm font-bold text-white">Обновление биомов</h4>
                <p class="text-xs text-slate-400 line-clamp-1">Новые биомы океана и переработанная генерация пещер...</p>
              </div>

              <!-- Item 3: Дроп -->
              <div class="news-card p-4 space-y-1.5 cursor-pointer">
                <div class="flex items-center gap-2 text-xs">
                  <span class="px-2 py-0.5 rounded-md bg-[#8b5cf6] text-white text-[10px] font-bold">Дроп</span>
                  <span class="text-slate-400 text-[11px] font-mono">08.05.2026</span>
                </div>
                <h4 class="text-sm font-bold text-white">Осенний дроп контента</h4>
                <p class="text-xs text-slate-400 line-clamp-1">Новые кастомные крылья, плащи и эксклюзивные анимации...</p>
              </div>
            </div>
          </div>

          <!-- RIGHT COLUMN: ТОП КОСМЕТИКИ -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="text-sm font-bold text-white flex items-center gap-2">
                <span>✨</span> Топ косметики
              </h3>
              <button onclick="switchTab('bypasses')" class="text-xs text-slate-400 hover:text-white transition-colors">Открыть магазин</button>
            </div>

            <!-- 3x2 Grid of Item Cards -->
            <div class="grid grid-cols-3 gap-2.5">
              
              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/emote_67.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>💃</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Эмоция</span>
                  <span class="text-xs font-bold text-white truncate block">Эмоция - 67</span>
                </div>
              </div>

              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/emote_twerk.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>🕺</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Эмоция</span>
                  <span class="text-xs font-bold text-white truncate block">Эмоция - Твёрк</span>
                </div>
              </div>

              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/wings_angel.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>🪽</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Крылья</span>
                  <span class="text-xs font-bold text-white truncate block">Крылья - Angel</span>
                </div>
              </div>

              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/cape_wave.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>🧣</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Плащ</span>
                  <span class="text-xs font-bold text-white truncate block">Плащ - Wave</span>
                </div>
              </div>

              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/crown_elite.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>👑</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Шляпа</span>
                  <span class="text-xs font-bold text-white truncate block">Корона - Elite</span>
                </div>
              </div>

              <div class="cosmetic-card p-3 space-y-1.5 cursor-pointer group">
                <div class="h-16 rounded-xl bg-[#12141c] border border-white/5 flex items-center justify-center text-xl group-hover:scale-105 transition-transform overflow-hidden">
                  <img src="items/aura_glow.png" class="h-full w-full object-contain" onerror="this.outerHTML='<span class=\'text-2xl\'>✨</span>'">
                </div>
                <div>
                  <span class="text-[10px] font-medium text-slate-400 block">Аура</span>
                  <span class="text-xs font-bold text-white truncate block">Аура - Glow</span>
                </div>
              </div>

            </div>
          </div>

        </div>

      </section>

      <!-- ================= TAB 2: VERSIONS ================= -->
      <section id="contentVersions" class="space-y-4 hidden">
        <div>
          <h2 class="text-xl font-bold text-white">Версии клиента</h2>
          <p class="text-xs text-slate-400 mt-0.5">Официальная сборка для Fabric 1.21.11.</p>
        </div>

        <div class="shader-card p-6 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-[#6448e3]/20 border border-[#6448e3]/40 flex items-center justify-center font-bold text-[#8b78f6] text-xl">
              H
            </div>
            <div>
              <div class="flex items-center gap-2">
                <h4 class="text-base font-bold text-white">Horus 1.21.11 (Fabric)</h4>
                <span class="px-2 py-0.5 rounded-md bg-[#6448e3] text-white text-[9px] font-bold">АКТУАЛЬНО</span>
              </div>
              <p class="text-xs text-slate-400 mt-1">Официальный релиз • Шейдеры Iris + Sodium • Обходы GrimAC • Java 21</p>
            </div>
          </div>
          <button class="px-4 py-2 rounded-xl bg-white text-black font-bold text-xs cursor-default">АКТИВНА ✓</button>
        </div>
      </section>

      <!-- ================= TAB 3: ACCOUNTS ================= -->
      <section id="contentAccounts" class="space-y-4 hidden">
        <div>
          <h2 class="text-xl font-bold text-white">Менеджер аккаунтов</h2>
          <p class="text-xs text-slate-400 mt-0.5">Создавайте собственные аккаунты для входа в игру.</p>
        </div>

        <!-- Add Form -->
        <div class="shader-card p-5 space-y-2">
          <label class="text-xs font-bold text-slate-400 block uppercase tracking-wider">Создать новый аккаунт</label>
          <form onsubmit="handleCreateAccount(event)" class="flex items-center gap-3">
            <input id="newNickInput" type="text" placeholder="Введите никнейм игрока..." class="flex-1 px-4 py-2.5 rounded-xl bg-[#12141c] border border-white/10 text-xs text-white focus:outline-none focus:border-[#6448e3] transition-all">
            <button type="submit" class="btn-play-purple px-5 py-2.5 text-xs font-bold whitespace-nowrap">+ Добавить</button>
          </form>
        </div>

        <!-- Accounts List -->
        <div id="accountsContainer" class="space-y-2.5">
          <!-- Dynamically populated -->
        </div>
      </section>

      <!-- ================= TAB 4: WORKSHOP / BYPASSES ================= -->
      <section id="contentBypasses" class="space-y-4 hidden">
        <div>
          <h2 class="text-xl font-bold text-white">Мастерская & Обходы</h2>
          <p class="text-xs text-slate-400 mt-0.5">Модули обхода античита GrimAC и популярных серверов.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="shader-card p-5 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="font-bold text-white">GrimAC (FunTime)</h4>
              <span class="px-2 py-0.5 rounded-md bg-[#6448e3] text-white text-[9px] font-bold">100% BYPASS</span>
            </div>
            <div class="text-xs text-slate-300 space-y-1.5 font-mono">
              <div class="flex justify-between"><span class="text-slate-500">AttackAura:</span><span class="text-[#8b78f6]">25-point Raycast (через блоки)</span></div>
              <div class="flex justify-between"><span class="text-slate-500">ElytraHelper:</span><span class="text-[#8b78f6]">Apex Jump + Packet Start</span></div>
              <div class="flex justify-between"><span class="text-slate-500">AutoExplosion:</span><span class="text-[#8b78f6]">Bedrock/Obsidian 1-block Air</span></div>
            </div>
          </div>

          <div class="shader-card p-5 space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="font-bold text-white">ReallyWorld (Matrix)</h4>
              <span class="px-2 py-0.5 rounded-md bg-[#3b82f6] text-white text-[9px] font-bold">ОБХОДИТСЯ</span>
            </div>
            <div class="text-xs text-slate-300 space-y-1.5 font-mono">
              <div class="flex justify-between"><span class="text-slate-500">Aura / HitBoxes:</span><span class="text-emerald-400">100% Legit Silent</span></div>
              <div class="flex justify-between"><span class="text-slate-500">AutoAnchor:</span><span class="text-emerald-400">Мгновенный подрыв</span></div>
              <div class="flex justify-between"><span class="text-slate-500">NoSlow Sprint:</span><span class="text-emerald-400">Полный обход</span></div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= TAB 5: NEWS ================= -->
      <section id="contentNews" class="space-y-4 hidden">
        <div>
          <h2 class="text-xl font-bold text-white">Все новости проекта</h2>
          <p class="text-xs text-slate-400 mt-0.5">Полная лента обновлений и анонсов.</p>
        </div>

        <div class="space-y-3">
          <div class="news-card p-5 space-y-2">
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded-md bg-[#6448e3] text-white text-[10px] font-bold">Обновление</span>
              <span class="text-slate-400 text-xs font-mono">12.05.2026</span>
            </div>
            <h4 class="text-base font-bold text-white">Новые функции в 1.21.11</h4>
            <p class="text-xs text-slate-300">Разработчики добавили улучшенные алгоритмы обхода GrimAC для модулей AttackAura и ElytraHelper. Оптимизирован рендеринг чанков в связке с Sodium 0.8.14.</p>
          </div>

          <div class="news-card p-5 space-y-2">
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded-md bg-[#3b82f6] text-white text-[10px] font-bold">Мир</span>
              <span class="text-slate-400 text-xs font-mono">10.05.2026</span>
            </div>
            <h4 class="text-base font-bold text-white">Обновление биомов</h4>
            <p class="text-xs text-slate-300">Переработана генерация океанических глубин и кристаллов аметиста в пещерах. Добавлены новые шейдерные эффекты преломления воды.</p>
          </div>

          <div class="news-card p-5 space-y-2">
            <div class="flex items-center gap-2">
              <span class="px-2 py-0.5 rounded-md bg-[#8b5cf6] text-white text-[10px] font-bold">Дроп</span>
              <span class="text-slate-400 text-xs font-mono">08.05.2026</span>
            </div>
            <h4 class="text-base font-bold text-white">Осенний дроп контента</h4>
            <p class="text-xs text-slate-300">В каталог косметики добавлены эксклюзивные анимации (Твёрк, Эмоция 67), кастомные крылья Angel и неоновые плащи.</p>
          </div>
        </div>
      </section>

      <!-- ================= TAB 6: SETTINGS ================= -->
      <section id="contentSettings" class="space-y-4 hidden">
        <div>
          <h2 class="text-xl font-bold text-white">Настройки запуска</h2>
          <p class="text-xs text-slate-400 mt-0.5">Параметры оперативной памяти и Java 21.</p>
        </div>

        <div class="shader-card p-6 space-y-5">
          <div>
            <div class="flex justify-between items-center mb-2">
              <label class="text-xs font-bold text-white">Выделение ОЗУ (RAM):</label>
              <span id="ramValText" class="text-xs font-mono font-bold text-[#8b78f6]">4096 MB</span>
            </div>
            <input id="ramInput" type="range" min="2048" max="16384" step="512" value="4096" oninput="updateRam(this.value)" class="w-full accent-[#6448e3] cursor-pointer">
          </div>

          <div>
            <label class="text-xs font-bold text-white block mb-1">Путь к Java 21 (javaw.exe):</label>
            <input type="text" value="C:\\Program Files\\Java\\jdk-21\\bin\\javaw.exe" class="w-full px-3.5 py-2 rounded-xl bg-[#12141c] border border-white/10 text-xs font-mono text-white focus:outline-none focus:border-[#6448e3]">
          </div>

          <div>
            <label class="text-xs font-bold text-white block mb-1">Флаги виртуальной машины JVM:</label>
            <input type="text" value="-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=20 -XX:+UnlockExperimentalVMOptions" class="w-full px-3.5 py-2 rounded-xl bg-[#12141c] border border-white/10 text-xs font-mono text-white focus:outline-none focus:border-[#6448e3]">
          </div>

          <div class="flex justify-end pt-2">
            <button onclick="saveSettingsMsg()" class="btn-play-purple px-5 py-2 text-xs font-bold">Сохранить</button>
          </div>
        </div>
      </section>

    </main>

  </div>

  <!-- ================= JAVASCRIPT LOGIC ================= -->
  <script>
    const STORAGE_KEY = 'pulse_accounts';
    const ACTIVE_KEY = 'pulse_active';

    function getAccounts() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) return JSON.parse(raw);
      } catch (e) {}
      return [{ username: 'HorusPegas', created_at: 'Сегодня' }];
    }

    function saveAccounts(accs) {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(accs));
      } catch (e) {}
    }

    function getActiveUser() {
      const active = localStorage.getItem(ACTIVE_KEY);
      const accs = getAccounts();
      if (active && accs.some(a => a.username === active)) return active;
      return accs.length > 0 ? accs[0].username : 'HorusPegas';
    }

    function setActiveUser(name) {
      localStorage.setItem(ACTIVE_KEY, name);
      updateUIProfile();
      renderAccountsList();
    }

    function updateUIProfile() {
      const u = getActiveUser();
      const sidebarName = document.getElementById('sidebarUsername');
      const sidebarAvatar = document.getElementById('sidebarAvatar');
      const headerGreeting = document.getElementById('headerGreetingNick');
      const heroNick = document.getElementById('heroPillUsername');
      const heroAvatar = document.getElementById('heroPillAvatar');

      if (sidebarName) sidebarName.innerText = `${u} #4370`;
      if (headerGreeting) headerGreeting.innerText = u;
      if (heroNick) heroNick.innerText = u;
      
      const headUrl = `https://mc-heads.net/avatar/${encodeURIComponent(u)}/48`;
      if (sidebarAvatar) sidebarAvatar.src = headUrl;
      if (heroAvatar) heroAvatar.src = headUrl;
    }

    function renderAccountsList() {
      const container = document.getElementById('accountsContainer');
      if (!container) return;
      const accs = getAccounts();
      const active = getActiveUser();

      container.innerHTML = accs.map(a => {
        const isActive = (a.username === active);
        return `
          <div class="news-card p-3.5 flex items-center justify-between border ${isActive ? 'border-[#6448e3] bg-[#1a1d2c]' : 'border-white/5 bg-[#171922]'}">
            <div class="flex items-center gap-3">
              <img src="https://mc-heads.net/avatar/${encodeURIComponent(a.username)}/48" class="w-9 h-9 rounded-xl border border-white/10" onerror="this.src='app_icon.png'">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-white">${a.username}</span>
                  ${isActive ? '<span class="text-[9px] font-bold px-1.5 py-0.2 rounded bg-[#241e3d] text-[#c4b5fd]">АКТИВЕН ✓</span>' : ''}
                </div>
                <span class="text-[10px] text-slate-400">Оффлайн • Создан: ${a.created_at || 'Сегодня'}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              ${!isActive ? `<button onclick="setActiveUser('${a.username}')" class="px-3 py-1.5 rounded-lg bg-[#242838] hover:bg-[#30364c] text-xs font-semibold transition-all">Выбрать</button>` : ''}
              <button onclick="deleteAccount('${a.username}')" class="w-8 h-8 rounded-lg bg-red-900/30 hover:bg-red-800/50 text-red-400 flex items-center justify-center transition-all">🗑️</button>
            </div>
          </div>
        `;
      }).join('');
    }

    function handleCreateAccount(e) {
      e.preventDefault();
      const input = document.getElementById('newNickInput');
      if (!input) return;
      const nick = input.value.trim();
      if (!nick || nick.length < 3) {
        alert('Введите никнейм от 3 до 16 символов!');
        return;
      }
      const accs = getAccounts();
      if (accs.some(a => a.username.toLowerCase() === nick.toLowerCase())) {
        alert('Такой аккаунт уже существует!');
        return;
      }
      accs.push({ username: nick, created_at: new Date().toLocaleDateString('ru-RU') });
      saveAccounts(accs);
      input.value = '';
      setActiveUser(nick);
    }

    function deleteAccount(nick) {
      if (!confirm(`Удалить аккаунт ${nick}?`)) return;
      let accs = getAccounts().filter(a => a.username !== nick);
      if (accs.length === 0) accs = [{ username: 'HorusPegas', created_at: 'Сегодня' }];
      saveAccounts(accs);
      if (getActiveUser() === nick) {
        setActiveUser(accs[0].username);
      } else {
        renderAccountsList();
      }
    }

    // --- Tab Switcher ---
    function switchTab(tabId) {
      const tabs = ['main', 'versions', 'accounts', 'bypasses', 'news', 'settings'];
      tabs.forEach(t => {
        const btn = document.getElementById(`tabBtn${t.charAt(0).toUpperCase() + t.slice(1)}`);
        const content = document.getElementById(`content${t.charAt(0).toUpperCase() + t.slice(1)}`);
        if (t === tabId) {
          btn?.classList.add('active');
          btn?.classList.remove('hover:text-white', 'hover:bg-white/5');
          content?.classList.remove('hidden');
        } else {
          btn?.classList.remove('active');
          btn?.classList.add('hover:text-white', 'hover:bg-white/5');
          content?.classList.add('hidden');
        }
      });
    }

    // --- Launch Client ---
    let isLaunching = false;
    function handleLaunchClient() {
      if (isLaunching) return;
      isLaunching = true;
      const btn = document.getElementById('mainLaunchBtn');
      const text = document.getElementById('launchBtnText');

      if (btn && text) {
        btn.classList.add('opacity-75', 'cursor-not-allowed');
        text.innerText = 'Запуск...';
      }

      // Call local backend bridge
      try {
        fetch('http://127.0.0.1:38241/api/launch', { method: 'POST' })
          .then(r => r.json())
          .then(data => {
            if (btn && text) {
              btn.classList.remove('opacity-75', 'cursor-not-allowed');
              text.innerText = '● В игре';
            }
          })
          .catch(() => {
            if (btn && text) {
              text.innerText = '● Запущено';
            }
          });
      } catch (e) {
        if (btn && text) text.innerText = '● Запущено';
      }
    }

    // --- Modal / Popup Helpers ---
    function openGiftModal() {
      alert('🎁 Вам доступен эксклюзивный статус и доступ ко всем обходам Horus 1.21.11!');
    }

    function updateRam(val) {
      const el = document.getElementById('ramValText');
      if (el) el.innerText = `${val} MB`;
    }

    function saveSettingsMsg() {
      alert('✓ Настройки сохранены!');
    }

    function minimizeWindow() {
      window.blur?.();
    }

    function toggleFullscreen() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen?.();
      } else {
        document.exitFullscreen?.();
      }
    }

    function closeAppWindow() {
      try {
        fetch('http://127.0.0.1:38241/api/exit', { method: 'POST' }).catch(() => {});
      } catch (e) {}
      window.close();
    }

    // Init on load
    document.addEventListener('DOMContentLoaded', () => {
      updateUIProfile();
      renderAccountsList();

      // Poll server status
      setInterval(() => {
        try {
          fetch('http://127.0.0.1:38241/api/heartbeat', { method: 'POST' }).catch(() => {});
        } catch (e) {}
      }, 2500);
    });
  </script>
</body>
</html>
"""

with open(INDEX_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

with open(ARTIFACT_PATH, "w", encoding="utf-8") as f:
    f.write(HTML_CONTENT)

print("Updated launcher/index.html and artifact to exact PulseVisuals design!")
