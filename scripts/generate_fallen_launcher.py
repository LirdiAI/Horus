# -*- coding: utf-8 -*-
import os
import base64
from pathlib import Path

ROOT = Path(r"C:\Users\Никита\Downloads\HorusClient")
logo_path = ROOT / "launcher" / "logo.png"
with open(logo_path, "rb") as f:
    logo_b64 = base64.b64encode(f.read()).decode("ascii")

html_content = f"""<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Fallen — Minecraft Launcher & Companion</title>
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <style>
    * {{
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      user-select: none;
      -webkit-user-select: none;
    }}
    :root {{
      --bg: #0B0D10;
      --card-bg: #0B0D10;
      --card-inner: #14171D;
      --card-inner-hover: #1E2127;
      --card-border: #383E4C;
      --card-border-hover: #FFFFFF;
      --accent: #DE6C14;
      --accent-hover: #F28028;
      --text-primary: #EBECEF;
      --text-secondary: #8E95A2;
      --text-muted: #636875;
      --text-policy: #3B404A;
      --radius-btn: 9px;
      --radius-card: 16px;
    }}
    html, body {{
      width: 100%;
      height: 100%;
      overflow: hidden;
      background: var(--bg);
      font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      color: var(--text-primary);
    }}
    body {{
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 10px;
      position: relative;
    }}

    .launcher-shell {{
      width: 100%;
      height: 100%;
      max-width: 430px;
      max-height: 630px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-card);
      padding: 16px 20px 14px 20px;
      display: flex;
      flex-direction: column;
      position: relative;
      box-shadow:
        inset 0 1px 0 rgba(255, 255, 255, 0.05),
        0 20px 50px rgba(0, 0, 0, 0.95);
      overflow: hidden;
    }}

    /* Top Chrome Row (Logged in as & Fallen Badge) */
    .top-chrome {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      position: relative;
      z-index: 10;
    }}

    .profile-pill {{
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 10px;
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      cursor: pointer;
      transition: all 0.2s ease;
    }}
    .profile-pill:hover {{
      background: var(--card-inner-hover);
      border-color: var(--card-border-hover);
    }}
    .profile-avatar {{
      width: 20px;
      height: 20px;
      border-radius: 4px;
      object-fit: cover;
      background: #B58463;
      border: 1px solid rgba(255, 255, 255, 0.15);
      image-rendering: pixelated;
    }}
    .profile-info {{
      display: flex;
      flex-direction: column;
      line-height: 1.1;
    }}
    .profile-meta-label {{
      font-size: 8.5px;
      font-weight: 600;
      color: var(--text-muted);
      letter-spacing: 0.3px;
      text-transform: uppercase;
    }}
    .profile-name-text {{
      font-size: 12px;
      font-weight: 600;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: 4px;
    }}
    .profile-pill:hover .profile-name-text {{
      color: #FFFFFF;
    }}
    .profile-arrow {{
      font-size: 8px;
      color: var(--text-secondary);
      transition: transform 0.2s ease;
    }}
    .profile-pill.active .profile-arrow {{
      transform: rotate(180deg);
    }}

    .client-badge {{
      font-size: 10px;
      font-weight: 600;
      color: var(--text-secondary);
      padding: 4px 8px;
      border-radius: var(--radius-btn);
      border: 1px solid var(--card-border);
      background: var(--card-inner);
      letter-spacing: 0.5px;
    }}

    /* Hero / Center Brand */
    .hero-center {{
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      margin-bottom: 12px;
      position: relative;
      z-index: 2;
    }}
    .hero-logo {{
      width: 36px;
      height: 36px;
      margin-bottom: 6px;
      filter: drop-shadow(0 4px 12px rgba(222, 108, 20, 0.2));
    }}
    .hero-greeting {{
      font-size: 16.5px;
      font-weight: 600;
      color: var(--text-primary);
      letter-spacing: -0.2px;
      margin-bottom: 2px;
    }}
    .hero-subtitle {{
      font-size: 11px;
      color: var(--text-muted);
      letter-spacing: -0.1px;
    }}

    /* Panels Container */
    .content-area {{
      flex: 1;
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;
    }}

    .view-card {{
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 100%;
      opacity: 1;
      transition: opacity 0.2s ease, transform 0.2s ease;
    }}
    .view-hidden {{
      display: none !important;
    }}

    /* Main Pill Buttons (Matching MainMenuScreen) */
    .pill-btn {{
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      width: 100%;
      height: 36px;
      border-radius: var(--radius-btn);
      border: 1px solid var(--card-border);
      font-size: 12.5px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      text-decoration: none;
      user-select: none;
    }}
    .pill-btn-dark {{
      background: var(--card-inner);
      color: var(--text-secondary);
    }}
    .pill-btn-dark:hover {{
      background: var(--card-inner-hover);
      border-color: var(--card-border-hover);
      color: var(--text-primary);
    }}
    .pill-btn-dark:active {{
      transform: scale(0.985);
    }}

    .pill-btn-accent {{
      background: var(--accent);
      border-color: var(--card-border);
      color: #FFFFFF;
    }}
    .pill-btn-accent:hover {{
      background: var(--accent-hover);
      border-color: var(--card-border-hover);
      color: #FFFFFF;
    }}
    .pill-btn-accent:active {{
      transform: scale(0.985);
    }}

    /* Account Manager Overlay */
    .accounts-overlay {{
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(11, 13, 16, 0.94);
      backdrop-filter: blur(6px);
      border-radius: var(--radius-card);
      z-index: 20;
      display: flex;
      flex-direction: column;
      padding: 12px;
      border: 1px solid var(--card-border);
      transition: all 0.25s ease;
    }}
    .accounts-overlay.hidden {{
      display: none;
    }}
    .overlay-header {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
    }}
    .overlay-title {{
      font-size: 13px;
      font-weight: 600;
      color: var(--text-primary);
    }}
    .overlay-close {{
      font-size: 11px;
      color: var(--text-muted);
      cursor: pointer;
      padding: 3px 8px;
      border-radius: 6px;
      border: 1px solid var(--card-border);
      background: var(--card-inner);
    }}
    .overlay-close:hover {{
      border-color: var(--card-border-hover);
      color: #FFF;
    }}
    .accounts-scroll {{
      flex: 1;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 6px;
      padding-right: 4px;
    }}
    .account-item {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 10px;
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      cursor: pointer;
      transition: all 0.18s ease;
    }}
    .account-item:hover {{
      border-color: var(--card-border-hover);
      background: var(--card-inner-hover);
    }}
    .account-item.active {{
      border-color: var(--accent);
      background: rgba(222, 108, 20, 0.08);
    }}
    .account-left {{
      display: flex;
      align-items: center;
      gap: 8px;
    }}
    .account-avatar {{
      width: 24px;
      height: 24px;
      border-radius: 4px;
      image-rendering: pixelated;
    }}
    .account-name {{
      font-size: 12px;
      font-weight: 600;
      color: var(--text-primary);
    }}
    .account-active-badge {{
      font-size: 9px;
      color: var(--accent);
      font-weight: 600;
    }}
    .account-del-btn {{
      color: var(--text-muted);
      font-size: 13px;
      padding: 2px 6px;
      border-radius: 4px;
      border: none;
      background: none;
      cursor: pointer;
    }}
    .account-del-btn:hover {{
      color: #FF5A5A;
    }}

    .account-add-row {{
      display: flex;
      gap: 6px;
      margin-top: 10px;
    }}
    .account-input {{
      flex: 1;
      height: 32px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      padding: 0 10px;
      color: var(--text-primary);
      font-size: 12px;
      outline: none;
    }}
    .account-input:focus {{
      border-color: var(--card-border-hover);
    }}
    .account-add-btn {{
      height: 32px;
      padding: 0 12px;
      background: var(--accent);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      color: #FFF;
      font-size: 11.5px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }}
    .account-add-btn:hover {{
      background: var(--accent-hover);
      border-color: var(--card-border-hover);
    }}

    /* RAM Section */
    .ram-container {{
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      padding: 8px 12px;
      margin-bottom: 12px;
    }}
    .ram-meta {{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }}
    .ram-title {{
      font-size: 10.5px;
      font-weight: 600;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }}
    .ram-val {{
      font-size: 11.5px;
      font-weight: 600;
      color: var(--text-primary);
    }}
    .ram-chips-grid {{
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 6px;
    }}
    .ram-chip-btn {{
      height: 26px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 6px;
      color: var(--text-secondary);
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.18s ease;
    }}
    .ram-chip-btn:hover {{
      border-color: var(--card-border-hover);
      color: var(--text-primary);
    }}
    .ram-chip-btn.active {{
      background: var(--accent);
      border-color: var(--card-border-hover);
      color: #FFFFFF;
    }}

    /* Auth Inputs */
    .form-group {{
      display: flex;
      flex-direction: column;
      gap: 4px;
      margin-bottom: 10px;
    }}
    .form-label {{
      font-size: 10.5px;
      font-weight: 600;
      color: var(--text-secondary);
    }}
    .form-input-wrap {{
      display: flex;
      align-items: center;
      height: 34px;
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      padding: 0 10px;
      transition: border-color 0.2s ease;
    }}
    .form-input-wrap:focus-within {{
      border-color: var(--card-border-hover);
    }}
    .form-input-wrap input {{
      flex: 1;
      background: transparent;
      border: none;
      outline: none;
      color: var(--text-primary);
      font-size: 12.5px;
    }}

    /* Companion Live Dashboard Styles */
    .companion-banner {{
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 7px;
      background: rgba(34, 197, 94, 0.08);
      border: 1px solid rgba(34, 197, 94, 0.3);
      border-radius: var(--radius-btn);
      margin-bottom: 12px;
      font-size: 11px;
      font-weight: 600;
      color: #4ade80;
      letter-spacing: 0.4px;
    }}
    .pulsing-indicator {{
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #22c55e;
      box-shadow: 0 0 8px #22c55e;
      animation: pulseAnim 1.6s infinite ease-in-out;
    }}
    @keyframes pulseAnim {{
      0%, 100% {{ opacity: 0.35; transform: scale(0.9); }}
      50% {{ opacity: 1; transform: scale(1.2); }}
    }}

    .stats-matrix {{
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      margin-bottom: 12px;
    }}
    .stat-box {{
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      padding: 8px 10px;
      transition: border-color 0.2s ease;
    }}
    .stat-box:hover {{
      border-color: var(--card-border-hover);
    }}
    .stat-box-label {{
      font-size: 10px;
      color: var(--text-muted);
      margin-bottom: 2px;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }}
    .stat-box-val {{
      font-size: 13.5px;
      font-weight: 600;
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }}
    .stat-box-val.accent {{
      color: var(--accent);
    }}

    .quick-actions-grid {{
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
      margin-bottom: 12px;
    }}
    .act-btn {{
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      height: 32px;
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-btn);
      color: var(--text-secondary);
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.18s ease;
    }}
    .act-btn:hover {{
      background: var(--card-inner-hover);
      border-color: var(--card-border-hover);
      color: var(--text-primary);
    }}
    .act-btn.danger:hover {{
      border-color: #EF4444;
      color: #EF4444;
    }}

    /* Bottom Footer Text Links (Matching MainMenuScreen) */
    .links-footer {{
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 18px;
      margin-top: 10px;
      margin-bottom: 8px;
    }}
    .footer-link {{
      font-size: 11px;
      color: var(--text-muted);
      cursor: pointer;
      transition: color 0.2s ease;
      text-decoration: none;
    }}
    .footer-link:hover {{
      color: var(--text-primary);
    }}

    /* Bottom Centered Policy Disclaimer (Matching MainMenuScreen lines 214-219) */
    .policy-disclaimer {{
      text-align: center;
      font-size: 9.5px;
      line-height: 1.35;
      color: var(--text-policy);
      margin-top: auto;
      padding-top: 6px;
    }}

    /* Toast */
    .toast {{
      position: absolute;
      bottom: 22px;
      left: 50%;
      transform: translateX(-50%) translateY(20px);
      background: var(--card-inner);
      border: 1px solid var(--card-border-hover);
      color: #FFFFFF;
      padding: 7px 14px;
      border-radius: var(--radius-btn);
      font-size: 11.5px;
      font-weight: 500;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.8);
      pointer-events: none;
      opacity: 0;
      transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
      z-index: 100;
      white-space: nowrap;
    }}
    .toast.show {{
      opacity: 1;
      transform: translateX(-50%) translateY(0);
    }}
  </style>
</head>
<body>
  <div class="launcher-shell">
    <!-- Top Chrome Row -->
    <div class="top-chrome">
      <!-- Profile Badge (Toggle Alt / Accounts) -->
      <div class="profile-pill" id="profileBadgeBtn" title="Сменить аккаунт / Управление">
        <img class="profile-avatar" id="topAvatarImg" src="https://mc-heads.net/avatar/Lumira/64" alt="Avatar" onerror="this.src='https://crafatar.com/avatars/steve?overlay';" />
        <div class="profile-info">
          <span class="profile-meta-label">Logged in as</span>
          <span class="profile-name-text"><span id="topUsername">Lumira</span> <span class="profile-arrow">▼</span></span>
        </div>
      </div>
      <div class="client-badge" id="editionBadge">✦ FALLEN CLIENT</div>
    </div>

    <!-- Center Hero Branding -->
    <div class="hero-center">
      <img class="hero-logo" src="data:image/png;base64,{logo_b64}" alt="Logo" />
      <div class="hero-greeting" id="centerGreeting">Good time of day, Lumira</div>
      <div class="hero-subtitle">Welcome to Fallen, the best Minecraft client.</div>
    </div>

    <!-- Content Area for Views -->
    <div class="content-area">

      <!-- VIEW 1: AUTHENTICATION -->
      <div class="view-card" id="authView">
        <form id="authForm" style="display:flex; flex-direction:column; justify-content:space-between; flex:1;">
          <div style="margin-top:10px;">
            <div class="form-group">
              <label class="form-label">Никнейм игрока</label>
              <div class="form-input-wrap">
                <input type="text" id="authUsername" value="Lumira" placeholder="Введите никнейм" required />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Пароль клиента</label>
              <div class="form-input-wrap">
                <input type="password" id="authPassword" value="123456" placeholder="Введите пароль" />
              </div>
            </div>
          </div>
          <div>
            <button type="submit" class="pill-btn pill-btn-accent" style="margin-bottom:8px;">
              <span>Войти в Fallen</span>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </button>
            <button type="button" class="pill-btn pill-btn-dark" id="quickAuthBtn">
              <span>Быстрый вход</span>
            </button>
          </div>
        </form>
      </div>

      <!-- VIEW 2: LAUNCH DASHBOARD -->
      <div class="view-card view-hidden" id="dashView">
        <div style="display:flex; flex-direction:column; gap:10px;">
          <!-- RAM Allocation Selector -->
          <div class="ram-container">
            <div class="ram-meta">
              <span class="ram-title">Выделение ОЗУ</span>
              <span class="ram-val" id="ramDisplay">6 ГБ</span>
            </div>
            <div class="ram-chips-grid" id="ramGrid">
              <button type="button" class="ram-chip-btn" data-ram="2">2G</button>
              <button type="button" class="ram-chip-btn" data-ram="4">4G</button>
              <button type="button" class="ram-chip-btn active" data-ram="6">6G</button>
              <button type="button" class="ram-chip-btn" data-ram="8">8G</button>
              <button type="button" class="ram-chip-btn" data-ram="12">12G</button>
            </div>
          </div>
        </div>

        <div style="display:flex; flex-direction:column; gap:8px; margin-top:auto;">
          <!-- Main Launch Button (Play) -->
          <button type="button" class="pill-btn pill-btn-accent" id="launchGameBtn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="m5 3 14 9-14 9V3Z"/></svg>
            <span id="launchBtnText">Запустить игру</span>
          </button>

          <!-- Swap Accounts Button (Matching MainMenuScreen accent/dark) -->
          <button type="button" class="pill-btn pill-btn-dark" id="swapAccountsBtn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87 sound"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            <span>Сменить аккаунт</span>
          </button>
        </div>
      </div>

      <!-- VIEW 3: LIVE COMPANION DASHBOARD (WHEN GAME IS RUNNING) -->
      <div class="view-card view-hidden" id="companionView">
        <div>
          <!-- Active Badge -->
          <div class="companion-banner">
            <span class="pulsing-indicator"></span>
            <span>FALLEN CLIENT ACTIVE (В ИГРЕ)</span>
          </div>

          <!-- Live Stats Matrix -->
          <div class="stats-matrix">
            <div class="stat-box">
              <div class="stat-box-label">Сервер</div>
              <div class="stat-box-val" id="compServer">FunnyTimes.net</div>
            </div>
            <div class="stat-box">
              <div class="stat-box-label">Пинг</div>
              <div class="stat-box-val accent" id="compPing">-- ms</div>
            </div>
            <div class="stat-box">
              <div class="stat-box-label">FPS</div>
              <div class="stat-box-val accent" id="compFps">-- FPS</div>
            </div>
            <div class="stat-box">
              <div class="stat-box-label">Активный конфиг</div>
              <div class="stat-box-val" id="compConfig">default</div>
            </div>
          </div>

          <!-- Quick Actions Grid -->
          <div class="quick-actions-grid">
            <button type="button" class="act-btn" id="actScreenshotBtn">
              <span>📸</span> <span>Скриншот</span>
            </button>
            <button type="button" class="act-btn" id="actLogsBtn">
              <span>📂</span> <span>Папка логов</span>
            </button>
            <button type="button" class="act-btn" id="actReloadConfigBtn">
              <span>🔄</span> <span>Релоад конфига</span>
            </button>
            <button type="button" class="act-btn danger" id="actDisconnectBtn">
              <span>🚪</span> <span>Дисконнект</span>
            </button>
          </div>
        </div>

        <div>
          <button type="button" class="pill-btn pill-btn-dark" id="compBackBtn">
            <span>Главное меню</span>
          </button>
        </div>
      </div>

      <!-- Swap Accounts Overlay Panel -->
      <div class="accounts-overlay hidden" id="accountsOverlay">
        <div class="overlay-header">
          <span class="overlay-title">Менеджер аккаунтов</span>
          <span class="overlay-close" id="closeAccountsOverlayBtn">✕ Закрыть</span>
        </div>
        <div class="accounts-scroll" id="accountsList"></div>
        <div class="account-add-row">
          <input type="text" class="account-input" id="newAccountInput" placeholder="Никнейм нового твинка..." />
          <button type="button" class="account-add-btn" id="addAccountBtn">+ Добавить</button>
        </div>
      </div>

    </div>

    <!-- Bottom Footer Text Links (Matching MainMenuScreen) -->
    <div class="links-footer">
      <span class="footer-link" id="linkOptions">Options</span>
      <span class="footer-link" id="linkFolder">Папка игры</span>
      <span class="footer-link" id="linkExit">Exit</span>
    </div>

    <!-- Bottom Centered Policy Disclaimer (Matching MainMenuScreen lines 214-219) -->
    <div class="policy-disclaimer">
      By logging into your account you agree to all of our policies,<br>
      including our Privacy Policy and Terms of Service
    </div>

    <!-- Toast Notification -->
    <div class="toast" id="toast"></div>
  </div>

  <script>
  (function() {
    const authView = document.getElementById('authView');
    const dashView = document.getElementById('dashView');
    const companionView = document.getElementById('companionView');
    const accountsOverlay = document.getElementById('accountsOverlay');

    const topUsername = document.getElementById('topUsername');
    const topAvatarImg = document.getElementById('topAvatarImg');
    const centerGreeting = document.getElementById('centerGreeting');
    const profileBadgeBtn = document.getElementById('profileBadgeBtn');
    const swapAccountsBtn = document.getElementById('swapAccountsBtn');
    const closeAccountsOverlayBtn = document.getElementById('closeAccountsOverlayBtn');
    const accountsList = document.getElementById('accountsList');
    const newAccountInput = document.getElementById('newAccountInput');
    const addAccountBtn = document.getElementById('addAccountBtn');

    const ramGrid = document.getElementById('ramGrid');
    const ramDisplay = document.getElementById('ramDisplay');
    const launchGameBtn = document.getElementById('launchGameBtn');
    const launchBtnText = document.getElementById('launchBtnText');

    const compServer = document.getElementById('compServer');
    const compPing = document.getElementById('compPing');
    const compFps = document.getElementById('compFps');
    const compConfig = document.getElementById('compConfig');
    const actScreenshotBtn = document.getElementById('actScreenshotBtn');
    const actLogsBtn = document.getElementById('actLogsBtn');
    const actReloadConfigBtn = document.getElementById('actReloadConfigBtn');
    const actDisconnectBtn = document.getElementById('actDisconnectBtn');
    const compBackBtn = document.getElementById('compBackBtn');

    const linkOptions = document.getElementById('linkOptions');
    const linkFolder = document.getElementById('linkFolder');
    const linkExit = document.getElementById('linkExit');
    const toast = document.getElementById('toast');

    let currentRam = parseInt(localStorage.getItem('horus_ram') || '6', 10);
    let currentUser = localStorage.getItem('horus_username') || 'Lumira';
    let isGameRunning = false;
    let accountsData = [];

    function showToast(msg) {
      toast.textContent = msg;
      toast.classList.add('show');
      setTimeout(() => toast.classList.remove('show'), 2600);
    }

    function updateUserDisplay(user) {
      currentUser = user;
      localStorage.setItem('horus_username', user);
      topUsername.textContent = user;
      centerGreeting.textContent = 'Good time of day, ' + user;
      topAvatarImg.src = 'https://mc-heads.net/avatar/' + encodeURIComponent(user) + '/64';
    }

    function showView(view) {
      authView.classList.add('view-hidden');
      dashView.classList.add('view-hidden');
      companionView.classList.add('view-hidden');
      view.classList.remove('view-hidden');
    }

    // RAM Picker
    function setRam(val) {
      currentRam = val;
      localStorage.setItem('horus_ram', val);
      ramDisplay.textContent = val + ' ГБ';
      ramGrid.querySelectorAll('.ram-chip-btn').forEach(btn => {
        btn.classList.toggle('active', parseInt(btn.dataset.ram, 10) === val);
      });
      fetch('/api/ram', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ram: val })
      }).catch(() => {});
    }

    ramGrid.addEventListener('click', (e) => {
      const btn = e.target.closest('.ram-chip-btn');
      if (btn) setRam(parseInt(btn.dataset.ram, 10));
    });

    // Accounts Overlay
    function toggleAccountsOverlay(force) {
      const isHidden = accountsOverlay.classList.contains('hidden');
      const open = force !== undefined ? force : isHidden;
      accountsOverlay.classList.toggle('hidden', !open);
      profileBadgeBtn.classList.toggle('active', open);
      if (open) loadAccounts();
    }

    profileBadgeBtn.addEventListener('click', () => toggleAccountsOverlay());
    if (swapAccountsBtn) swapAccountsBtn.addEventListener('click', () => toggleAccountsOverlay(true));
    closeAccountsOverlayBtn.addEventListener('click', () => toggleAccountsOverlay(false));

    function loadAccounts() {
      fetch('/api/accounts')
        .then(r => r.json())
        .then(d => {
          accountsData = d.accounts || [];
          renderAccountsList(accountsData, d.active || currentUser);
          if (d.active) updateUserDisplay(d.active);
        })
        .catch(() => {
          const fallback = [{ username: currentUser, added_at: '' }];
          renderAccountsList(fallback, currentUser);
        });
    }

    function renderAccountsList(list, activeName) {
      accountsList.innerHTML = '';
      list.forEach(acc => {
        const item = document.createElement('div');
        const isActive = acc.username.toLowerCase() === (activeName || currentUser).toLowerCase();
        item.className = 'account-item' + (isActive ? ' active' : '');
        item.innerHTML = `
          <div class="account-left">
            <img class="account-avatar" src="https://mc-heads.net/avatar/${encodeURIComponent(acc.username)}/64" onerror="this.src='https://crafatar.com/avatars/steve?overlay';" />
            <div>
              <div class="account-name">${acc.username}</div>
              ${isActive ? '<div class="account-active-badge">● Активен</div>' : ''}
            </div>
          </div>
          <button class="account-del-btn" title="Удалить">✕</button>
        `;
        item.addEventListener('click', (e) => {
          if (e.target.classList.contains('account-del-btn')) return;
          selectAccount(acc.username);
        });
        const delBtn = item.querySelector('.account-del-btn');
        delBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          deleteAccount(acc.username);
        });
        accountsList.appendChild(item);
      });
    }

    function selectAccount(name) {
      fetch('/api/accounts/select', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: name })
      })
      .then(r => r.json())
      .then(d => {
        if (d.success) {
          updateUserDisplay(name);
          toggleAccountsOverlay(false);
          showToast('Аккаунт переключен: ' + name);
        }
      })
      .catch(() => {
        updateUserDisplay(name);
        toggleAccountsOverlay(false);
        showToast('Выбран: ' + name);
      });
    }

    function deleteAccount(name) {
      fetch('/api/accounts/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: name })
      })
      .then(r => r.json())
      .then(() => {
        showToast('Удален аккаунт: ' + name);
        loadAccounts();
      })
      .catch(() => showToast('Ошибка удаления'));
    }

    addAccountBtn.addEventListener('click', () => {
      const val = newAccountInput.value.trim();
      if (!val) return;
      fetch('/api/accounts/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: val })
      })
      .then(r => r.json())
      .then(d => {
        newAccountInput.value = '';
        if (d.success) {
          selectAccount(val);
        } else {
          showToast(d.error || 'Ошибка добавления');
        }
      })
      .catch(() => {
        updateUserDisplay(val);
        toggleAccountsOverlay(false);
        newAccountInput.value = '';
      });
    });

    // Auth Form
    const authForm = document.getElementById('authForm');
    authForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const user = document.getElementById('authUsername').value.trim() || 'Lumira';
      updateUserDisplay(user);
      localStorage.setItem('fallen_logged_in', 'true');
      showView(dashView);
      showToast('Успешный вход в Fallen');
    });

    document.getElementById('quickAuthBtn').addEventListener('click', () => {
      updateUserDisplay('Lumira');
      localStorage.setItem('fallen_logged_in', 'true');
      showView(dashView);
      showToast('Быстрый вход: Lumira');
    });

    // Launch Game
    launchGameBtn.addEventListener('click', () => {
      if (isGameRunning) {
        showView(companionView);
        return;
      }}
      launchBtnText.textContent = 'Запуск...';
      launchGameBtn.style.opacity = '0.75';
      fetch('/api/launch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: currentUser, ram: currentRam })
      })
      .then(r => r.json())
      .then(d => {
        launchBtnText.textContent = 'Клиент запущен';
        showToast('🎮 Игра запускается...');
        setTimeout(() => {
          showView(companionView);
          launchBtnText.textContent = 'Запустить игру';
          launchGameBtn.style.opacity = '1';
        }, 2000);
      })
      .catch(() => {
        showToast('🚀 Запуск процесса Minecraft...');
        setTimeout(() => {
          showView(companionView);
          launchBtnText.textContent = 'Запустить игру';
          launchGameBtn.style.opacity = '1';
        }, 2000);
      });
    });

    // Companion In-Game Actions
    actScreenshotBtn.addEventListener('click', () => {
      fetch('/api/action/screenshot', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('📸 ' + (d.message || 'Скриншот сделан!')))
        .catch(() => showToast('📸 Запрос на скриншот отправлен'));
    });

    actLogsBtn.addEventListener('click', () => {
      fetch('/api/action/open-logs', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('📂 ' + (d.message || 'Папка логов открыта!')))
        .catch(() => showToast('📂 Открытие папки логов...'));
    });

    actReloadConfigBtn.addEventListener('click', () => {
      fetch('/api/action/reload-config', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('🔄 ' + (d.message || 'Конфиг перезагружен!')))
        .catch(() => showToast('🔄 Перезагрузка конфига...'));
    });

    actDisconnectBtn.addEventListener('click', () => {
      fetch('/api/action/disconnect', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('🚪 ' + (d.message || 'Дисконнект выполнен!')))
        .catch(() => showToast('🚪 Дисконнект...'));
    });

    compBackBtn.addEventListener('click', () => {
      showView(dashView);
    });

    // Live Game Status Polling (/api/status)
    function pollGameStatus() {
      fetch('/api/status')
        .then(r => r.json())
        .then(d => {
          if (d.server || d.fps || d.online || d.companion_mode) {
            isGameRunning = true;
            compServer.textContent = d.server || 'В игре';
            compPing.textContent = (d.ping !== undefined ? d.ping : '--') + ' ms';
            compFps.textContent = (d.fps !== undefined ? d.fps : '--') + ' FPS';
            compConfig.textContent = d.activeConfig || 'default';

            if (!dashView.classList.contains('view-hidden')) {
              showView(companionView);
            }
          } else {
            isGameRunning = false;
          }
        })
        .catch(() => {
          isGameRunning = false;
        });
    }
    setInterval(pollGameStatus, 1500);

    // Bottom Links
    linkFolder.addEventListener('click', () => {
      fetch('/api/action/open-folder', { method: 'POST' })
        .then(r => r.json())
        .catch(() => showToast('📁 Папка игры: .minecraft'));
    });
    linkOptions.addEventListener('click', () => {
      toggleAccountsOverlay(true);
    });
    linkExit.addEventListener('click', () => {
      window.close();
    });

    // Startup Initialization
    setRam(currentRam);
    updateUserDisplay(currentUser);
    loadAccounts();

    fetch('/api/system')
      .then(r => r.json())
      .then(d => {
        if (d.role) {
          document.getElementById('editionBadge').textContent = '✦ ' + d.role.toUpperCase() + ' EDITION';
        }
      })
      .catch(() => {});

    const hasLoggedIn = localStorage.getItem('fallen_logged_in') === 'true';
    if (hasLoggedIn) {
      showView(dashView);
    } else {
      showView(authView);
    }

    pollGameStatus();
  })();
  </script>
</body>
</html>
"""

# Write to all destinations
destinations = [
    ROOT / "launcher" / "index.html",
    ROOT / "run" / "launcher" / "index.html",
    ROOT / "src" / "main" / "resources" / "assets" / "horus" / "launcher" / "index.html",
    Path(r"C:\Users\Никита\Downloads\deluxe-source\launcher\index.html")
]

for dst in destinations:
    dst.parent.mkdir(parents=True, exist_ok=True)
    with open(dst, "w", encoding="utf-8") as f:
        f.write(html_content)
    print(f"Wrote to {dst} ({len(html_content)} bytes)")
