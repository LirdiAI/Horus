html_content = r'''<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>HorusClient — Minecraft Launcher & Companion</title>
  <link rel="icon" type="image/x-icon" href="favicon.ico">
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      user-select: none;
      -webkit-user-select: none;
    }
    :root {
      --bg: #000000;
      --card-bg: linear-gradient(170deg, #09090e 0%, #030305 60%, #000000 100%);
      --card-border: rgba(255, 255, 255, 0.12);
      --card-border-gloss: rgba(255, 255, 255, 0.28);
      --neon: #00f5c4;
      --neon-glow: rgba(0, 245, 196, 0.4);
      --gold: #ffcf70;
      --gold-glow: rgba(255, 207, 112, 0.35);
      --text: #ffffff;
      --text-silver: #cbd5e1;
      --text-muted: #64748b;
      --input-bg: #040407;
      --input-border: rgba(255, 255, 255, 0.12);
    }
    html, body {
      width: 100%;
      height: 100%;
      overflow: hidden;
      background: var(--bg);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      color: var(--text);
    }
    body {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 10px;
      position: relative;
    }

    .bg-gloss {
      position: fixed;
      inset: 0;
      background:
        radial-gradient(circle at 50% 0%, rgba(0, 245, 196, 0.16) 0%, transparent 60%),
        radial-gradient(circle at 100% 100%, rgba(255, 207, 112, 0.08) 0%, transparent 50%),
        linear-gradient(180deg, #040406 0%, #000000 100%);
      pointer-events: none;
    }

    .compact-card {
      width: 100%;
      height: 100%;
      max-width: 420px;
      max-height: 620px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 22px;
      padding: 22px 24px 20px 24px;
      display: flex;
      flex-direction: column;
      position: relative;
      box-shadow:
        inset 0 1.5px 1px var(--card-border-gloss),
        inset 0 -1px 0 rgba(0, 0, 0, 0.9),
        0 24px 60px rgba(0, 0, 0, 0.98),
        0 0 35px rgba(0, 245, 196, 0.08);
      overflow: hidden;
    }

    .compact-card::before {
      content: "";
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background: linear-gradient(
        130deg,
        rgba(255, 255, 255, 0.08) 0%,
        rgba(255, 255, 255, 0.02) 25%,
        transparent 50%
      );
      pointer-events: none;
      z-index: 1;
    }

    .top-rim {
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 2px;
      background: linear-gradient(90deg, transparent, var(--neon), var(--gold), transparent);
      z-index: 2;
    }

    .brand-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      margin-bottom: 14px;
      position: relative;
      z-index: 2;
    }
    .brand-icon {
      width: 46px;
      height: 46px;
      border-radius: 15px;
      background: linear-gradient(145deg, #0e1e1b 0%, #030706 100%);
      border: 1px solid rgba(0, 245, 196, 0.5);
      box-shadow:
        inset 0 1.5px 1px rgba(255, 255, 255, 0.4),
        0 10px 26px var(--neon-glow);
      display: grid;
      place-items: center;
      margin-bottom: 8px;
      position: relative;
      overflow: hidden;
    }
    .brand-icon::after {
      content: "";
      position: absolute;
      top: 0; left: 0; right: 0; height: 50%;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.35) 0%, transparent 100%);
      border-radius: 15px 15px 40px 40px;
    }
    .brand-icon svg {
      width: 24px;
      height: 24px;
      filter: drop-shadow(0 2px 8px rgba(0, 245, 196, 0.6));
    }
    .brand-title {
      font-size: 17px;
      font-weight: 900;
      letter-spacing: 0.06em;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 7px;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.8);
    }
    .edition-badge {
      font-size: 9px;
      color: var(--gold);
      background: linear-gradient(180deg, rgba(255, 207, 112, 0.18) 0%, rgba(255, 207, 112, 0.05) 100%);
      border: 1px solid rgba(255, 207, 112, 0.45);
      box-shadow: inset 0 1px 0.5px rgba(255, 255, 255, 0.4);
      border-radius: 6px;
      padding: 2px 6px;
      font-weight: 800;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }
    .brand-sub {
      font-size: 11.5px;
      color: var(--text-silver);
      margin-top: 3px;
      font-weight: 500;
      opacity: 0.85;
    }

    .view-panel {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      z-index: 2;
    }
    .view-hidden {
      display: none !important;
    }

    .input-group {
      margin-bottom: 14px;
    }
    .input-label {
      display: block;
      font-size: 10.5px;
      font-weight: 800;
      color: var(--text-silver);
      text-transform: uppercase;
      letter-spacing: 0.08em;
      margin-bottom: 5px;
    }
    .input-box {
      position: relative;
      display: flex;
      align-items: center;
    }
    .input-box svg.icon-left {
      position: absolute;
      left: 14px;
      color: #64748b;
      pointer-events: none;
      transition: color 0.2s;
    }
    .text-input {
      width: 100%;
      height: 44px;
      background: var(--input-bg);
      border: 1px solid var(--input-border);
      border-radius: 12px;
      padding: 0 42px 0 42px;
      color: #ffffff;
      font-size: 13.5px;
      font-weight: 600;
      outline: none;
      box-shadow:
        inset 0 1.5px 3px rgba(0, 0, 0, 0.8),
        inset 0 1px 0 rgba(255, 255, 255, 0.05);
      transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
    }
    .text-input:focus {
      background: #06060a;
      border-color: var(--neon);
      box-shadow:
        inset 0 1.5px 3px rgba(0, 0, 0, 0.6),
        0 0 16px rgba(0, 245, 196, 0.25);
    }
    .text-input:focus + svg.icon-left,
    .input-box:focus-within svg.icon-left {
      color: var(--neon);
    }
    .eye-btn {
      position: absolute;
      right: 9px;
      background: none;
      border: none;
      padding: 6px;
      cursor: pointer;
      color: #64748b;
      display: grid;
      place-items: center;
      border-radius: 7px;
      transition: color 0.15s, transform 0.15s;
    }
    .eye-btn:hover {
      color: var(--neon);
      transform: scale(1.08);
    }

    .options-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
      font-size: 11.5px;
    }
    .checkbox-label {
      display: flex;
      align-items: center;
      gap: 7px;
      cursor: pointer;
      color: var(--text-silver);
      font-weight: 500;
    }
    .checkbox-label input {
      accent-color: var(--neon);
      width: 15px;
      height: 15px;
      cursor: pointer;
    }
    .link-btn {
      color: var(--neon);
      cursor: pointer;
      font-weight: 700;
      text-decoration: none;
      transition: color 0.15s, text-shadow 0.15s;
    }
    .link-btn:hover {
      color: #8affea;
      text-shadow: 0 0 10px rgba(0, 245, 196, 0.6);
      text-decoration: underline;
    }

    .btn-main {
      width: 100%;
      height: 48px;
      background: linear-gradient(180deg, #10f0c0 0%, #00d9a6 50%, #00a880 100%);
      border: none;
      border-radius: 13px;
      color: #011d17;
      font-size: 14px;
      font-weight: 900;
      letter-spacing: 0.08em;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 9px;
      position: relative;
      overflow: hidden;
      box-shadow:
        inset 0 1.5px 0.8px rgba(255, 255, 255, 0.85),
        inset 0 -1.5px 1px rgba(0, 0, 0, 0.35),
        0 12px 28px var(--neon-glow),
        0 4px 10px rgba(0, 0, 0, 0.8);
      transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
    }
    .btn-main::before {
      content: "";
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 50%;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 255, 255, 0.1) 100%);
      border-radius: 13px 13px 80px 80px / 13px 13px 20px 20px;
      pointer-events: none;
    }
    .btn-main:hover {
      transform: translateY(-1.5px);
      filter: brightness(1.08);
      box-shadow:
        inset 0 1.5px 0.8px rgba(255, 255, 255, 0.95),
        inset 0 -1.5px 1px rgba(0, 0, 0, 0.4),
        0 16px 34px rgba(0, 245, 196, 0.55),
        0 4px 12px rgba(0, 0, 0, 0.9);
    }
    .btn-main:active {
      transform: translateY(1px);
    }
    .btn-main:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none !important;
    }

    .btn-secondary {
      width: 100%;
      height: 40px;
      background: linear-gradient(180deg, #14141c 0%, #060609 100%);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 11px;
      color: #ffffff;
      font-size: 12.5px;
      font-weight: 700;
      letter-spacing: 0.04em;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      position: relative;
      overflow: hidden;
      box-shadow:
        inset 0 1.2px 0.5px rgba(255, 255, 255, 0.28),
        inset 0 -1px 0 rgba(0, 0, 0, 0.9),
        0 6px 16px rgba(0, 0, 0, 0.7);
      transition: all 0.18s;
      margin-top: 9px;
    }
    .btn-secondary:hover {
      background: linear-gradient(180deg, #1a1a24 0%, #0a0a0f 100%);
      border-color: rgba(0, 245, 196, 0.45);
      color: #ffffff;
      box-shadow: 0 0 16px rgba(0, 245, 196, 0.15);
      transform: translateY(-1px);
    }

    .status-hint {
      text-align: center;
      font-size: 10.5px;
      color: var(--text-silver);
      opacity: 0.75;
      margin-top: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      font-weight: 500;
    }

    /* PROFILE & ACCOUNT MANAGER CARD */
    .profile-card {
      background: linear-gradient(165deg, #0e0e14 0%, #040407 100%);
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 15px;
      padding: 12px 14px;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 12px;
      position: relative;
      box-shadow:
        inset 0 1.5px 1px rgba(255, 255, 255, 0.22),
        0 10px 24px rgba(0, 0, 0, 0.8);
      cursor: pointer;
      transition: border-color 0.2s, box-shadow 0.2s;
    }
    .profile-card:hover {
      border-color: rgba(0, 245, 196, 0.4);
      box-shadow: 0 0 18px rgba(0, 245, 196, 0.12);
    }
    .profile-avatar-wrap {
      width: 44px;
      height: 44px;
      border-radius: 13px;
      background: #040706;
      border: 1.5px solid var(--neon);
      box-shadow: 0 4px 14px var(--neon-glow);
      display: grid;
      place-items: center;
      overflow: hidden;
      flex-shrink: 0;
      position: relative;
    }
    .profile-avatar-img {
      width: 100%;
      height: 100%;
      image-rendering: pixelated;
      object-fit: cover;
    }
    .profile-meta {
      flex: 1;
      overflow: hidden;
    }
    .profile-name-row {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .profile-username {
      font-size: 14.5px;
      font-weight: 800;
      color: #ffffff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .profile-dropdown-arrow {
      font-size: 10px;
      color: var(--neon);
      transition: transform 0.2s;
    }
    .profile-status {
      font-size: 11px;
      color: var(--neon);
      font-weight: 700;
      margin-top: 1px;
      text-shadow: 0 0 8px rgba(0, 245, 196, 0.3);
    }
    .profile-sub {
      font-size: 10px;
      color: var(--text-silver);
      opacity: 0.8;
      margin-top: 1px;
      font-weight: 500;
    }

    /* ACCOUNTS DROPDOWN DRAWER */
    .accounts-drawer {
      position: absolute;
      top: 68px;
      left: 0;
      right: 0;
      background: linear-gradient(175deg, #0e0e16 0%, #040407 100%);
      border: 1px solid var(--neon);
      border-radius: 14px;
      padding: 10px;
      z-index: 100;
      box-shadow:
        inset 0 1px 0 rgba(255, 255, 255, 0.2),
        0 14px 40px rgba(0, 0, 0, 0.98),
        0 0 24px rgba(0, 245, 196, 0.25);
      display: none;
      animation: fadeInDrawer 0.18s ease-out;
    }
    .accounts-drawer.open {
      display: block;
    }
    @keyframes fadeInDrawer {
      from { opacity: 0; transform: translateY(-8px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .drawer-title {
      font-size: 10px;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--text-muted);
      margin-bottom: 8px;
      padding: 0 4px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .accounts-list {
      max-height: 145px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 5px;
      margin-bottom: 8px;
    }
    .account-item {
      display: flex;
      align-items: center;
      gap: 9px;
      padding: 6px 8px;
      border-radius: 9px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.06);
      cursor: pointer;
      transition: all 0.15s;
    }
    .account-item:hover {
      background: rgba(0, 245, 196, 0.09);
      border-color: rgba(0, 245, 196, 0.35);
    }
    .account-item.active {
      background: rgba(0, 245, 196, 0.18);
      border-color: var(--neon);
    }
    .acc-avatar {
      width: 26px;
      height: 26px;
      border-radius: 6px;
      image-rendering: pixelated;
      background: #000;
    }
    .acc-info {
      flex: 1;
      overflow: hidden;
    }
    .acc-name {
      font-size: 12px;
      font-weight: 700;
      color: #fff;
    }
    .acc-role {
      font-size: 9px;
      color: var(--neon);
    }
    .acc-delete {
      background: none;
      border: none;
      color: #64748b;
      font-size: 13px;
      cursor: pointer;
      padding: 2px 6px;
      border-radius: 4px;
      transition: color 0.15s;
    }
    .acc-delete:hover {
      color: #ff4d4d;
    }
    .add-account-row {
      display: flex;
      gap: 6px;
      padding-top: 6px;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
    }
    .add-acc-input {
      flex: 1;
      height: 32px;
      background: #050508;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 8px;
      padding: 0 10px;
      font-size: 12px;
      color: #fff;
      outline: none;
    }
    .add-acc-btn {
      height: 32px;
      background: linear-gradient(180deg, #10f0c0 0%, #00a880 100%);
      border: none;
      border-radius: 8px;
      padding: 0 10px;
      color: #011d17;
      font-size: 11px;
      font-weight: 800;
      cursor: pointer;
    }

    /* RAM Section */
    .ram-section {
      background: linear-gradient(165deg, #0a0a0e 0%, #020204 100%);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 14px;
      padding: 11px 13px;
      margin-bottom: 12px;
      box-shadow: inset 0 1.2px 0.8px rgba(255, 255, 255, 0.16);
    }
    .ram-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 10.5px;
      font-weight: 800;
      color: var(--text-silver);
      text-transform: uppercase;
      letter-spacing: 0.06em;
      margin-bottom: 8px;
    }
    .ram-val {
      color: var(--neon);
      font-weight: 900;
      text-shadow: 0 0 10px rgba(0, 245, 196, 0.4);
    }
    .ram-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 6px;
    }
    .ram-chip {
      height: 32px;
      background: linear-gradient(180deg, #12121a 0%, #050508 100%);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      color: var(--text-silver);
      font-size: 12px;
      font-weight: 800;
      cursor: pointer;
      transition: all 0.16s ease;
    }
    .ram-chip.active {
      background: linear-gradient(180deg, rgba(0, 245, 196, 0.28) 0%, rgba(0, 180, 140, 0.12) 100%);
      border-color: var(--neon);
      color: #ffffff;
      box-shadow: inset 0 1px 0.8px rgba(255, 255, 255, 0.6), 0 0 14px rgba(0, 245, 196, 0.45);
    }

    .version-tag-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 2px;
      font-size: 10.5px;
      color: var(--text-muted);
      margin-bottom: 12px;
    }
    .version-pill {
      background: linear-gradient(180deg, #0e0e14 0%, #030305 100%);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 3px 8px;
      border-radius: 6px;
      color: var(--text-silver);
      font-weight: 600;
    }

    /* LIVE COMPANION DASHBOARD VIEW */
    .companion-panel {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      z-index: 2;
    }
    .companion-status-pill {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 7px;
      background: rgba(0, 245, 196, 0.1);
      border: 1px solid var(--neon);
      padding: 6px 12px;
      border-radius: 10px;
      color: var(--neon);
      font-size: 11px;
      font-weight: 800;
      letter-spacing: 0.06em;
      margin-bottom: 12px;
      box-shadow: 0 0 16px rgba(0, 245, 196, 0.2);
    }
    .pulse-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--neon);
      box-shadow: 0 0 8px var(--neon);
      animation: pulseGlow 1.4s infinite ease-in-out;
    }
    @keyframes pulseGlow {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.75); }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 9px;
      margin-bottom: 14px;
    }
    .stat-card {
      background: linear-gradient(165deg, #0c0c12 0%, #030305 100%);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      padding: 10px 12px;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
    }
    .stat-label {
      font-size: 9.5px;
      font-weight: 700;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.06em;
      margin-bottom: 3px;
      display: flex;
      align-items: center;
      gap: 5px;
    }
    .stat-val {
      font-size: 14.5px;
      font-weight: 900;
      color: #ffffff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .stat-val.accent {
      color: var(--neon);
      text-shadow: 0 0 10px rgba(0, 245, 196, 0.4);
    }

    /* ACTION BUTTONS GRID */
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
      margin-bottom: 12px;
    }
    .action-btn {
      height: 44px;
      background: linear-gradient(180deg, #13131c 0%, #060609 100%);
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 11px;
      color: #ffffff;
      font-size: 11.5px;
      font-weight: 700;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 7px;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
      transition: all 0.16s;
    }
    .action-btn:hover {
      background: linear-gradient(180deg, #1c1c28 0%, #0a0a10 100%);
      border-color: rgba(0, 245, 196, 0.45);
      color: #fff;
      transform: translateY(-1px);
      box-shadow: 0 0 14px rgba(0, 245, 196, 0.2);
    }
    .action-btn.danger:hover {
      border-color: #ff4d4d;
      box-shadow: 0 0 14px rgba(255, 77, 77, 0.3);
      color: #ff8888;
    }

    /* Toast Notification */
    .toast {
      position: absolute;
      bottom: 14px;
      left: 20px;
      right: 20px;
      background: linear-gradient(180deg, #0e0e16 0%, #030306 100%);
      border: 1px solid var(--neon);
      color: #ffffff;
      font-size: 11.5px;
      font-weight: 700;
      padding: 9px 12px;
      border-radius: 11px;
      text-align: center;
      box-shadow:
        inset 0 1.2px 0.5px rgba(255, 255, 255, 0.4),
        0 12px 30px rgba(0, 0, 0, 0.95),
        0 0 20px rgba(0, 245, 196, 0.3);
      opacity: 0;
      transform: translateY(12px);
      transition: opacity 0.25s, transform 0.25s;
      pointer-events: none;
      z-index: 200;
    }
    .toast.show {
      opacity: 1;
      transform: translateY(0);
    }
  </style>
</head>
<body>
  <div class="bg-gloss"></div>

  <div class="compact-card">
    <div class="top-rim"></div>

    <!-- Header Brand -->
    <div class="brand-header">
      <div class="brand-icon">
        <svg viewBox="0 0 24 24" fill="none"><path d="M12 2.8 20 7.4v9.2L12 21.2 4 16.6V7.4L12 2.8Z" stroke="#00f5c4" stroke-width="1.5"/><path d="M8.3 8.2v7.6m7.4-7.6v7.6M8.5 12h7" stroke="#ffffff" stroke-width="1.6" stroke-linecap="square"/><path d="m12 2.8 0 4.5m8-0-4 2.3" stroke="#00f5c4" stroke-width="1.3" opacity=".9"/></svg>
      </div>
      <h1 class="brand-title">HORUS CLIENT <span class="edition-badge" id="editionBadge">✦ DLC EDITION</span></h1>
      <div class="brand-sub" id="headerSub">Minecraft Launcher 1.21.11</div>
    </div>

    <!-- VIEW 1: AUTHENTICATION / LOGIN -->
    <div class="view-panel" id="authView">
      <form id="authForm" style="display:flex; flex-direction:column; justify-content:space-between; flex:1;">
        <div>
          <div class="input-group">
            <label class="input-label" for="authUsername">Никнейм игрока</label>
            <div class="input-box">
              <svg class="icon-left" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              <input type="text" id="authUsername" class="text-input" value="Lumira" placeholder="Введите никнейм" autocomplete="username" required />
            </div>
          </div>

          <div class="input-group">
            <label class="input-label" for="authPassword">Пароль клиента</label>
            <div class="input-box">
              <svg class="icon-left" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              <input type="password" id="authPassword" class="text-input" value="123456" placeholder="Введите пароль" autocomplete="current-password" />
              <button type="button" class="eye-btn" id="eyeToggle" title="Показать/скрыть пароль">
                <svg id="eyeIcon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
          </div>

          <div class="options-row">
            <label class="checkbox-label">
              <input type="checkbox" id="authRemember" checked />
              <span>Запомнить сессию</span>
            </label>
            <span class="link-btn" id="quickLoginBtn">Быстрый вход</span>
          </div>
        </div>

        <div>
          <button type="submit" class="btn-main" id="authSubmitBtn">
            <span id="authBtnText">ВОЙТИ В КЛИЕНТ</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </button>
          <div class="status-hint">
            <span>✦ Защищенное соединение • Синхронизация Horus</span>
          </div>
        </div>
      </form>
    </div>

    <!-- VIEW 2: LAUNCH DASHBOARD WITH ACCOUNT MANAGER -->
    <div class="view-panel view-hidden" id="dashView">
      <div style="position:relative;">
        <!-- Player & Account Manager Card -->
        <div class="profile-card" id="profileCard" title="Нажмите для смены аккаунта">
          <div class="profile-avatar-wrap">
            <img class="profile-avatar-img" id="dashAvatarImg" src="https://mc-heads.net/avatar/Lumira/64" alt="Skin Head" />
          </div>
          <div class="profile-meta">
            <div class="profile-name-row">
              <span class="profile-username" id="dashUsername">Lumira</span>
              <span class="profile-dropdown-arrow" id="dropdownArrow">▼</span>
            </div>
            <div class="profile-status" id="dashStatus">✦ ADMIN EDITION (Навсегда)</div>
            <div class="profile-sub" id="dashSub">Баланс: 1000 ✦ • Pulse Active</div>
          </div>
          <button class="sync-mini-btn" id="dashSyncBtn" title="Синхронизировать с Supabase" style="background:none; border:none; color:var(--neon); cursor:pointer;">
            <svg class="sync-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3"><path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/></svg>
          </button>
        </div>

        <!-- Account Switcher Drawer -->
        <div class="accounts-drawer" id="accountsDrawer">
          <div class="drawer-title">
            <span>Сохраненные аккаунты</span>
            <span class="link-btn" id="closeDrawerBtn" style="font-size:10px;">Закрыть ✕</span>
          </div>
          <div class="accounts-list" id="accountsList"></div>
          <div class="add-account-row">
            <input type="text" class="add-acc-input" id="newAccountInput" placeholder="Никнейм нового твинка..." />
            <button type="button" class="add-acc-btn" id="addAccountBtn">+ Добавить</button>
          </div>
        </div>

        <!-- RAM Selection -->
        <div class="ram-section">
          <div class="ram-header">
            <span>Выделение ОЗУ</span>
            <span class="ram-val" id="ramDisplay">6 ГБ</span>
          </div>
          <div class="ram-grid" id="ramGrid">
            <button type="button" class="ram-chip" data-ram="2">2G</button>
            <button type="button" class="ram-chip" data-ram="4">4G</button>
            <button type="button" class="ram-chip active" data-ram="6">6G</button>
            <button type="button" class="ram-chip" data-ram="8">8G</button>
            <button type="button" class="ram-chip" data-ram="12">12G</button>
          </div>
        </div>

        <!-- Version & Folder links -->
        <div class="version-tag-row">
          <span class="version-pill">Minecraft Java 1.21.11 (Fabric)</span>
          <span class="link-btn" id="openFolderBtn" style="font-size:10.5px;">📁 Папка .minecraft</span>
        </div>
      </div>

      <div>
        <button type="button" class="btn-main" id="launchGameBtn">
          <svg width="19" height="19" viewBox="0 0 24 24" fill="currentColor"><path d="m5 3 14 9-14 9V3Z"/></svg>
          <span id="launchBtnText">ЗАПУСТИТЬ ИГРУ</span>
        </button>
        <button type="button" class="btn-secondary" id="switchUserBtn">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          <span>Сменить аккаунт / Выйти</span>
        </button>
        <div class="status-hint">
          <span>Готов к игре • Лаунчер станет Companion Dashboard</span>
        </div>
      </div>
    </div>

    <!-- VIEW 3: LIVE COMPANION DASHBOARD (WHEN GAME IS RUNNING) -->
    <div class="view-panel view-hidden" id="companionView">
      <div>
        <!-- Running status badge -->
        <div class="companion-status-pill">
          <span class="pulse-dot"></span>
          <span>HORUS CLIENT ACTIVE (В ИГРЕ)</span>
        </div>

        <!-- Live Server, Ping, FPS, Config Grid -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-label">🌐 Сервер</div>
            <div class="stat-val" id="compServer">Подключение...</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">⚡ Пинг</div>
            <div class="stat-val accent" id="compPing">-- ms</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">🎯 FPS</div>
            <div class="stat-val accent" id="compFps">-- FPS</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">⚙️ Конфиг</div>
            <div class="stat-val" id="compConfig">--</div>
          </div>
        </div>

        <!-- Quick In-Game Action Buttons -->
        <div class="input-label" style="margin-bottom:6px;">Быстрые действия</div>
        <div class="actions-grid">
          <button type="button" class="action-btn" id="actScreenshotBtn">
            <span>📸</span> <span>Скриншот</span>
          </button>
          <button type="button" class="action-btn" id="actLogsBtn">
            <span>📂</span> <span>Папка логов</span>
          </button>
          <button type="button" class="action-btn" id="actReloadConfigBtn">
            <span>🔄</span> <span>Релоад конфига</span>
          </button>
          <button type="button" class="action-btn danger" id="actDisconnectBtn">
            <span>🚪</span> <span>Дисконнект</span>
          </button>
        </div>
      </div>

      <div>
        <button type="button" class="btn-secondary" id="compBackBtn" style="margin-top:0;">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          <span>Главное меню лаунчера</span>
        </button>
        <div class="status-hint">
          <span>Live Bridge синхронизирован с портом 38241</span>
        </div>
      </div>
    </div>

    <!-- Toast Notification -->
    <div class="toast" id="toast"></div>
  </div>

  <script>
  (()=>{
    const toast = document.getElementById('toast');
    let toastTimer;
    function showToast(msg) {
      toast.textContent = msg;
      toast.classList.add('show');
      clearTimeout(toastTimer);
      toastTimer = setTimeout(() => toast.classList.remove('show'), 3500);
    }

    // Disable fullscreen shortcuts
    window.addEventListener('keydown', (e) => {
      if (e.key === 'F11' || (e.altKey && e.key === 'Enter')) {
        e.preventDefault();
        e.stopPropagation();
      }
    }, true);

    function enforceCompactSize() {
      try { fetch('/api/resize-window?w=440&h=640').catch(() => {}); } catch(e) {}
      try { window.resizeTo(440, 640); } catch(e) {}
    }
    enforceCompactSize();

    // Elements
    const authView = document.getElementById('authView');
    const dashView = document.getElementById('dashView');
    const companionView = document.getElementById('companionView');

    const authForm = document.getElementById('authForm');
    const authUsername = document.getElementById('authUsername');
    const authPassword = document.getElementById('authPassword');
    const eyeToggle = document.getElementById('eyeToggle');
    const quickLoginBtn = document.getElementById('quickLoginBtn');
    const authSubmitBtn = document.getElementById('authSubmitBtn');
    const authBtnText = document.getElementById('authBtnText');

    const profileCard = document.getElementById('profileCard');
    const dashAvatarImg = document.getElementById('dashAvatarImg');
    const dashUsername = document.getElementById('dashUsername');
    const dashStatus = document.getElementById('dashStatus');
    const dashSyncBtn = document.getElementById('dashSyncBtn');
    const dropdownArrow = document.getElementById('dropdownArrow');
    const accountsDrawer = document.getElementById('accountsDrawer');
    const accountsList = document.getElementById('accountsList');
    const newAccountInput = document.getElementById('newAccountInput');
    const addAccountBtn = document.getElementById('addAccountBtn');
    const closeDrawerBtn = document.getElementById('closeDrawerBtn');

    const ramDisplay = document.getElementById('ramDisplay');
    const ramGrid = document.getElementById('ramGrid');
    const launchGameBtn = document.getElementById('launchGameBtn');
    const launchBtnText = document.getElementById('launchBtnText');
    const switchUserBtn = document.getElementById('switchUserBtn');
    const openFolderBtn = document.getElementById('openFolderBtn');

    // Companion Elements
    const compServer = document.getElementById('compServer');
    const compPing = document.getElementById('compPing');
    const compFps = document.getElementById('compFps');
    const compConfig = document.getElementById('compConfig');
    const actScreenshotBtn = document.getElementById('actScreenshotBtn');
    const actLogsBtn = document.getElementById('actLogsBtn');
    const actReloadConfigBtn = document.getElementById('actReloadConfigBtn');
    const actDisconnectBtn = document.getElementById('actDisconnectBtn');
    const compBackBtn = document.getElementById('compBackBtn');

    // State
    let currentUser = localStorage.getItem('horus_username') || 'Lumira';
    let currentRam = parseInt(localStorage.getItem('horus_ram') || '6', 10);
    let isLaunching = false;
    let accountsData = { active: currentUser, accounts: [] };
    let isGameRunning = false;

    function setRam(gb) {
      currentRam = Math.max(2, Math.min(32, gb));
      localStorage.setItem('horus_ram', currentRam);
      ramDisplay.textContent = currentRam + ' ГБ';
      document.querySelectorAll('.ram-chip').forEach(ch => {
        ch.classList.toggle('active', parseInt(ch.getAttribute('data-ram'), 10) === currentRam);
      });
    }

    function updateSkinAvatar(username) {
      dashAvatarImg.src = `https://mc-heads.net/avatar/${encodeURIComponent(username)}/64`;
      dashAvatarImg.onerror = () => {
        dashAvatarImg.onerror = null;
        dashAvatarImg.src = 'user_avatar.png';
      };
    }

    function showDashView() {
      companionView.classList.add('view-hidden');
      authView.classList.add('view-hidden');
      dashView.classList.remove('view-hidden');
      dashUsername.textContent = currentUser;
      updateSkinAvatar(currentUser);
    }

    function showAuthView() {
      companionView.classList.add('view-hidden');
      dashView.classList.add('view-hidden');
      authView.classList.remove('view-hidden');
      authUsername.value = currentUser;
      authUsername.focus();
    }

    function showCompanionView() {
      authView.classList.add('view-hidden');
      dashView.classList.add('view-hidden');
      companionView.classList.remove('view-hidden');
    }

    // Toggle Accounts Drawer
    function toggleDrawer(open) {
      const isOpen = open !== undefined ? open : !accountsDrawer.classList.contains('open');
      accountsDrawer.classList.toggle('open', isOpen);
      dropdownArrow.style.transform = isOpen ? 'rotate(180deg)' : 'none';
      if (isOpen) renderAccounts();
    }
    profileCard.addEventListener('click', (e) => {
      if (e.target.closest('#dashSyncBtn')) return;
      toggleDrawer();
    });
    closeDrawerBtn.addEventListener('click', () => toggleDrawer(false));

    // Accounts management
    function loadAccounts() {
      fetch('/api/accounts')
        .then(r => r.json())
        .then(d => {
          accountsData = d;
          if (d.active) {
            currentUser = d.active;
            localStorage.setItem('horus_username', currentUser);
            dashUsername.textContent = currentUser;
            updateSkinAvatar(currentUser);
          }
          renderAccounts();
        })
        .catch(() => {});
    }

    function renderAccounts() {
      accountsList.innerHTML = '';
      if (!accountsData.accounts || accountsData.accounts.length === 0) {
        accountsData.accounts = [{ username: currentUser, role: 'ADMIN' }];
      }
      accountsData.accounts.forEach(acc => {
        const item = document.createElement('div');
        item.className = 'account-item' + (acc.username.toLowerCase() === currentUser.toLowerCase() ? ' active' : '');
        item.innerHTML = `
          <img class="acc-avatar" src="https://mc-heads.net/avatar/${encodeURIComponent(acc.username)}/48" onerror="this.src='user_avatar.png'" alt="" />
          <div class="acc-info">
            <div class="acc-name">${acc.username}</div>
            <div class="acc-role">✦ ${acc.role || 'ADMIN'} EDITION</div>
          </div>
          ${acc.username.toLowerCase() === currentUser.toLowerCase() ? '<span style="color:var(--neon); font-size:12px;">✓</span>' : ''}
          ${accountsData.accounts.length > 1 ? `<button type="button" class="acc-delete" title="Удалить">✕</button>` : ''}
        `;

        item.addEventListener('click', (e) => {
          if (e.target.classList.contains('acc-delete')) {
            e.stopPropagation();
            removeAccount(acc.username);
            return;
          }
          selectAccount(acc.username);
        });

        accountsList.appendChild(item);
      });
    }

    function selectAccount(username) {
      fetch('/api/accounts/select', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username })
      })
      .then(r => r.json())
      .then(d => {
        accountsData = d;
        currentUser = username;
        localStorage.setItem('horus_username', currentUser);
        dashUsername.textContent = currentUser;
        updateSkinAvatar(currentUser);
        toggleDrawer(false);
        showToast('✦ Аккаунт переключен: ' + currentUser);
        syncProfile();
      });
    }

    function addAccount() {
      const name = newAccountInput.value.trim();
      if (!name) return;
      fetch('/api/accounts/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: name })
      })
      .then(r => r.json())
      .then(d => {
        accountsData = d;
        currentUser = name;
        newAccountInput.value = '';
        localStorage.setItem('horus_username', currentUser);
        dashUsername.textContent = currentUser;
        updateSkinAvatar(currentUser);
        toggleDrawer(false);
        showToast('✦ Аккаунт ' + name + ' добавлен и выбран!');
        syncProfile();
      });
    }
    addAccountBtn.addEventListener('click', addAccount);
    newAccountInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') addAccount(); });

    function removeAccount(name) {
      fetch('/api/accounts/remove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: name })
      })
      .then(r => r.json())
      .then(d => {
        accountsData = d;
        currentUser = d.active || currentUser;
        localStorage.setItem('horus_username', currentUser);
        dashUsername.textContent = currentUser;
        updateSkinAvatar(currentUser);
        renderAccounts();
        showToast('Аккаунт ' + name + ' удален');
      });
    }

    // Password Toggle
    eyeToggle.addEventListener('click', (e) => {
      e.preventDefault();
      if (authPassword.type === 'password') {
        authPassword.type = 'text';
      } else {
        authPassword.type = 'password';
      }
    });

    // RAM chips
    document.querySelectorAll('.ram-chip').forEach(btn => {
      btn.addEventListener('click', () => setRam(parseInt(btn.getAttribute('data-ram'), 10)));
    });

    // Supabase Sync
    function syncProfile() {
      const icon = dashSyncBtn.querySelector('.sync-icon');
      if (icon) icon.classList.add('spinning');
      fetch('/api/sync', { method: 'POST' })
        .then(r => r.json())
        .then(d => {
          if (d.role) {
            dashStatus.textContent = `✦ ${d.role.toUpperCase()} EDITION (Навсегда)`;
            const b = document.getElementById('editionBadge');
            if (b) b.textContent = `✦ ${d.role.toUpperCase()} EDITION`;
          }
          showToast('✦ ' + (d.message || 'Профиль Supabase синхронизирован!'));
        })
        .catch(() => {})
        .finally(() => {
          if (icon) icon.classList.remove('spinning');
        });
    }
    dashSyncBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      syncProfile();
    });

    // Launch Game
    function launchGame() {
      if (isLaunching) return;
      isLaunching = true;

      authSubmitBtn.disabled = true;
      launchGameBtn.disabled = true;
      if (authBtnText) authBtnText.textContent = 'ЗАПУСК...';
      if (launchBtnText) launchBtnText.textContent = 'ЗАПУСК...';

      showToast('▷ Запуск Horus Client 1.21.11 (' + currentRam + ' ГБ)...');

      fetch('/api/launch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: currentUser,
          ram: currentRam,
          fullscreen: false
        })
      })
      .then(r => r.json())
      .then(data => {
        if (data.success) {
          showToast('✓ Клиент запущен! Переход в Live Companion...');
          setTimeout(() => {
            isGameRunning = true;
            showCompanionView();
            isLaunching = false;
            authSubmitBtn.disabled = false;
            launchGameBtn.disabled = false;
            if (authBtnText) authBtnText.textContent = 'ВОЙТИ В КЛИЕНТ';
            if (launchBtnText) launchBtnText.textContent = 'ЗАПУСТИТЬ ИГРУ';
          }, 2000);
        } else {
          showToast('⚠️ Ошибка: ' + (data.error || 'не удалось запустить'));
          isLaunching = false;
          authSubmitBtn.disabled = false;
          launchGameBtn.disabled = false;
        }
      })
      .catch(() => {
        showToast('▷ Клиент запущен! Переход в Companion...');
        setTimeout(() => {
          isGameRunning = true;
          showCompanionView();
          isLaunching = false;
        }, 1500);
      });
    }

    authForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const val = authUsername.value.trim();
      if (!val) {
        showToast('⚠️ Введите никнейм');
        return;
      }
      currentUser = val;
      localStorage.setItem('horus_username', currentUser);
      localStorage.setItem('horus_logged_in', 'true');
      launchGame();
    });

    quickLoginBtn.addEventListener('click', (e) => {
      e.preventDefault();
      currentUser = 'Lumira';
      authUsername.value = 'Lumira';
      localStorage.setItem('horus_username', 'Lumira');
      localStorage.setItem('horus_logged_in', 'true');
      launchGame();
    });

    launchGameBtn.addEventListener('click', launchGame);

    switchUserBtn.addEventListener('click', () => {
      localStorage.removeItem('horus_logged_in');
      showAuthView();
    });

    openFolderBtn.addEventListener('click', () => {
      fetch('/api/open-folder?target=minecraft', { method: 'POST' }).catch(() => {});
      showToast('📁 Открытие папки .minecraft...');
    });

    // Companion Actions
    actScreenshotBtn.addEventListener('click', () => {
      fetch('/api/action/screenshot', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('📸 ' + (d.message || 'Скриншот сделан!')))
        .catch(() => showToast('📸 Команда отправлена в игру'));
    });

    actLogsBtn.addEventListener('click', () => {
      fetch('/api/action/open-logs', { method: 'POST' })
        .then(r => r.json())
        .then(d => showToast('📂 ' + (d.message || 'Папка логов открыта!')))
        .catch(() => showToast('📂 Открытие логов...'));
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
      showDashView();
    });

    // Live Polling of Game Client Status (/api/status)
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

            // Auto-switch to companion view if game is active
            if (dashView.classList.contains('view-hidden') === false && isGameRunning) {
              // User is looking at dash while game is running -> upgrade to companion!
              showCompanionView();
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

    // Initial Startup
    setRam(currentRam);
    loadAccounts();

    fetch('/api/system')
      .then(r => r.json())
      .then(d => {
        if (d.role) {
          dashStatus.textContent = `✦ ${d.role.toUpperCase()} EDITION (Навсегда)`;
          const b = document.getElementById('editionBadge');
          if (b) b.textContent = `✦ ${d.role.toUpperCase()} EDITION`;
        }
      })
      .catch(() => {});

    setInterval(() => {
      fetch('/api/heartbeat', { method: 'POST' }).catch(() => {});
    }, 4000);

    const hasLoggedIn = localStorage.getItem('horus_logged_in') === 'true';
    if (hasLoggedIn) {
      showDashView();
    } else {
      showAuthView();
    }

    // Check status immediately
    pollGameStatus();
  })();
  </script>
</body>
</html>
'''

with open(r"C:\Users\Никита\Downloads\HorusClient\launcher\index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

# Also copy to run/launcher/index.html
with open(r"C:\Users\Никита\Downloads\HorusClient\run\launcher\index.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print("Updated index.html in launcher/ and run/launcher/ successfully!")
