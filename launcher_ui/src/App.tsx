import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Bug,
  CheckCircle2,
  Globe,
  RotateCcw,
  ShieldCheck,
  Sparkles,
  Terminal,
} from 'lucide-react';
import {
  AccountProfile,
  ClientSettings,
  CosmeticsState,
  OverlayType,
  ProxyConfig,
  ServerEntry,
  WorldEntry,
} from './types/launcher';
import {
  ACCENT_PRESETS,
  INITIAL_ACCOUNTS,
  INITIAL_SERVERS,
  INITIAL_WORLDS,
} from './data/mockData';
import { PixelAvatar } from './components/PixelAvatar';
import { HorusLogo } from './components/HorusLogo';
import { AccountManagerPanel } from './components/panels/AccountManagerPanel';
import { CosmeticsSelectionScreen } from './components/panels/CosmeticsSelectionScreen';
import { ProxyScreen } from './components/panels/ProxyScreen';
import { OptionsScreen } from './components/panels/OptionsScreen';
import { MultiplayerScreen } from './components/panels/MultiplayerScreen';
import { SelectWorldScreen } from './components/panels/SelectWorldScreen';
import { LauncherLoaderModal } from './components/panels/LauncherLoaderModal';
import { InGameSessionModal } from './components/panels/InGameSessionModal';
import { uiSound } from './utils/sound';
import { fetchSupabaseProfile, upsertSupabaseProfile } from './utils/supabase';

export function App() {
  // ProfileAPI & AccountManagerPanel state with Supabase sync & persistence
  const [accounts, setAccounts] = useState<AccountProfile[]>(() => {
    try {
      const saved = localStorage.getItem('horus_saved_accounts');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {}
    return INITIAL_ACCOUNTS;
  });

  const [activeAccountId, setActiveAccountId] = useState<string>(() => {
    try {
      const savedUser = localStorage.getItem('horus_username');
      if (savedUser) {
        const match = accounts.find((a) => a.username.toLowerCase() === savedUser.toLowerCase());
        if (match) return match.id;
      }
    } catch (e) {}
    return INITIAL_ACCOUNTS[0].id;
  });

  // Overlay state (flag = accounts, flag2 = cosmetics, plus sub-screens)
  const [activeOverlay, setActiveOverlay] = useState<OverlayType>('none');

  // Welcome transition state (flag3 in MainMenuScreen.java)
  const [welcomeKey, setWelcomeKey] = useState<number>(0);
  const [isEntered, setIsEntered] = useState<boolean>(false);

  // ProxyManager state
  const [proxy, setProxy] = useState<ProxyConfig>({
    enabled: false,
    protocol: 'SOCKS5',
    host: '185.212.44.109',
    port: '1080',
    username: '',
    password: '',
    activePresetId: 'px-de-1',
    lastPing: 19,
  });

  // CosmeticsSelectionScreen state
  const [cosmetics, setCosmetics] = useState<CosmeticsState>({
    selectedCapeId: 'cape-fallen',
    selectedAuraId: 'aura-ember',
    showWings: false,
    rotationY: 180,
    rotationX: 0,
  });

  // Multiplayer & Singleplayer lists
  const [servers, setServers] = useState<ServerEntry[]>(INITIAL_SERVERS);
  const [worlds, setWorlds] = useState<WorldEntry[]>(INITIAL_WORLDS);

  // Active simulated in-game session
  const [activeSession, setActiveSession] = useState<{
    title: string;
    address: string;
  }>({
    title: 'FunTime Anarchy',
    address: 'mc.funtime.su',
  });

  // Client & InterfaceModule settings
  const [settings, setSettings] = useState<ClientSettings>({
    guiScale: 1.0,
    accentId: 'fallen-orange',
    soundEnabled: true,
    ambientGlow: true,
    showWindowChrome: true,
    showJavaDebugHud: false,
    ramAllocationMb: 4096,
    selectedBranch: 'Fallen 1.21.4 (Stable)',
    language: 'en',
  });

  // Exit scheduleStop notification state
  const [exitToast, setExitToast] = useState<boolean>(false);

  // Mouse telemetry for Render2D debug HUD
  const [mousePos, setMousePos] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [viewport, setViewport] = useState<{ w: number; h: number }>({
    w: typeof window !== 'undefined' ? window.innerWidth : 1280,
    h: typeof window !== 'undefined' ? window.innerHeight : 720,
  });

  // Trigger staggered slideInPC(index) entrance animation
  useEffect(() => {
    setIsEntered(false);
    const timer = setTimeout(() => {
      setIsEntered(true);
    }, 60);
    return () => clearTimeout(timer);
  }, [welcomeKey]);

  // Track window dimensions for scale calculation: Math.min(screenW / 960.0F, screenH / 540.0F)
  useEffect(() => {
    const onResize = () => {
      setViewport({ w: window.innerWidth, h: window.innerHeight });
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  // ESC key handler matching keyPressed(KeyInput) -> key == 256 in MainMenuScreen.java
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (activeOverlay !== 'none') {
          uiSound.playClick();
          setActiveOverlay('none');
        } else if (exitToast) {
          setExitToast(false);
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [activeOverlay, exitToast]);

  // Active ProfileAPI model
  const activeAccount = useMemo(
    () => accounts.find((a) => a.id === activeAccountId) || accounts[0],
    [accounts, activeAccountId]
  );

  // Exact welcomeName() implementation from MainMenuScreen.java lines 62-69
  const welcomeName = useMemo(() => {
    return activeAccount && activeAccount.username && activeAccount.username.trim().length > 0
      ? activeAccount.username
      : 'yougame';
  }, [activeAccount]);

  // Current accent color preset
  const activeAccent = useMemo(
    () => ACCENT_PRESETS.find((p) => p.id === settings.accentId) || ACCENT_PRESETS[0],
    [settings.accentId]
  );

  // Replay welcomeTransition (new MainMenuScreen(true))
  const triggerWelcomeTransition = useCallback(() => {
    setActiveOverlay('none');
    setExitToast(false);
    setWelcomeKey((k) => k + 1);
  }, []);

  // Supabase synchronization effect
  useEffect(() => {
    if (!activeAccount || !activeAccount.username) return;
    const user = activeAccount.username;
    try {
      localStorage.setItem('horus_username', user);
    } catch (e) {}

    // Sync with Supabase profiles table
    fetchSupabaseProfile(user).then((profile) => {
      if (profile && profile.role) {
        setAccounts((prev) =>
          prev.map((acc) =>
            acc.username.toLowerCase() === user.toLowerCase()
              ? {
                  ...acc,
                  role: (profile.role as any) || acc.role,
                  uid: profile.id ? profile.id.slice(0, 4) : acc.uid,
                }
              : acc
          )
        );
      }
    });

    // Notify backend
    fetch('/api/accounts/select', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user }),
    }).catch(() => {});
  }, [activeAccountId]);

  // Persist accounts to localStorage
  useEffect(() => {
    try {
      localStorage.setItem('horus_saved_accounts', JSON.stringify(accounts));
    } catch (e) {}
  }, [accounts]);

  // Handlers for AccountManagerPanel
  const handleAddAccount = (newAcc: AccountProfile) => {
    setAccounts((prev) => [newAcc, ...prev]);
    fetchSupabaseProfile(newAcc.username).then((profile) => {
      if (profile && profile.role) {
        setAccounts((prev) =>
          prev.map((a) =>
            a.id === newAcc.id
              ? {
                  ...a,
                  role: (profile.role as any) || a.role,
                  uid: profile.id ? profile.id.slice(0, 4) : a.uid,
                }
              : a
          )
        );
      } else {
        upsertSupabaseProfile(newAcc.username, newAcc.role);
      }
    });
  };

  const handleDeleteAccount = (id: string) => {
    setAccounts((prev) => {
      const next = prev.filter((a) => a.id !== id);
      if (id === activeAccountId && next.length > 0) {
        setActiveAccountId(next[0].id);
      }
      return next;
    });
  };

  const handleToggleFavorite = (id: string) => {
    setAccounts((prev) =>
      prev.map((a) => (a.id === id ? { ...a, favorite: !a.favorite } : a))
    );
  };

  // Overlay open flag (equivalent to overlayAnim = Math.max(altAnim, cosAnim) in MainMenuScreen.java)
  const isOverlayOpen = activeOverlay !== 'none';

  // Computed Java scale metric for debug inspector
  const computedJavaScale = useMemo(() => {
    return Math.min(viewport.w / 960.0, viewport.h / 540.0) * settings.guiScale;
  }, [viewport, settings.guiScale]);

  return (
    <div
      onMouseMove={(e) => setMousePos({ x: e.clientX, y: e.clientY })}
      style={
        {
          '--accent-base': activeAccent.base,
          '--accent-hover': activeAccent.hover,
        } as React.CSSProperties
      }
      className="relative w-screen h-screen bg-[#0B0D10] text-[#EBECEF] overflow-hidden flex flex-col justify-between select-none"
    >
      {/* Subtle Ambient Radial Spotlight (optional, preserves solid #0B0D10 obsidian aesthetic) */}
      {settings.ambientGlow && (
        <div
          className="pointer-events-none absolute inset-0 transition-opacity duration-500"
          style={{
            opacity: isOverlayOpen ? 0.35 : 0.75,
            background: `radial-gradient(circle at 50% 47%, ${activeAccent.base}12 0%, rgba(20, 23, 29, 0.22) 34%, rgba(11, 13, 16, 0) 68%)`,
          }}
        />
      )}

      {/* Optional Render2D Debug Grid when showJavaDebugHud is active */}
      {settings.showJavaDebugHud && (
        <div
          className="pointer-events-none absolute inset-0 opacity-20"
          style={{
            backgroundImage:
              'linear-gradient(to right, #383E4C 1px, transparent 1px), linear-gradient(to bottom, #383E4C 1px, transparent 1px)',
            backgroundSize: '48px 48px',
          }}
        />
      )}

      {/* =====================================================================
          TOP CHROME BAR (drawChrome() Top-Left Badge + Top-Right Launcher Controls)
          Dims to 1.0 - overlayAnim * 0.45 (0.55 opacity) when an overlay is open!
         ===================================================================== */}
      <header
        className="relative z-20 w-full px-5 pt-4 flex items-start justify-between transition-opacity duration-[260ms] ease-out"
        style={{
          opacity: isEntered ? (isOverlayOpen ? 0.55 : 1) : 0,
        }}
      >
        {/* Top-Left Profile Avatar & "Logged in as <user>" Badge (exact drawChrome reproduction) */}
        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            setActiveOverlay((prev) => (prev === 'accounts' ? 'none' : 'accounts'));
          }}
          onMouseEnter={() => uiSound.playHover()}
          className={`group flex items-center gap-2.5 px-2.5 py-1.5 rounded-[10px] border transition-all duration-[240ms] cursor-pointer text-left ${
            settings.showJavaDebugHud
              ? 'border-dashed border-[#8B5CF6] bg-[#14171D]/80'
              : 'border-transparent hover:border-[#383E4C] hover:bg-[#14171D]/70'
          }`}
          title="Click to Swap Accounts (toggleAlt)"
        >
          {/* Procedural 16x16 Steve / Skin Avatar Head */}
          <div className="relative flex items-center justify-center">
            <PixelAvatar
              skin={activeAccount.skin}
              size={Math.round(28 * settings.guiScale)}
              className="rounded-[3px] shadow-sm"
            />
          </div>

          {/* Two-line text stack: "Logged in as" (#636875) + welcomeName() (#EBECEF -> #FFFFFF on hover) */}
          <div className="flex flex-col leading-tight">
            <span className="text-[11px] font-semibold text-[#636875] tracking-[-0.01em]">
              Logged in as
            </span>
            <span className="text-[14.5px] font-semibold text-[#EBECEF] group-hover:text-[#FFFFFF] transition-colors duration-[240ms] tracking-[-0.015em]">
              {welcomeName}
            </span>
          </div>
        </button>

        {/* Top-Right Minimalist Client / Launcher Controls */}
        <div className="flex items-center gap-2">
          {/* Render2D Telemetry Pill (when enabled in Options or toggled) */}
          {settings.showJavaDebugHud && (
            <div className="hidden lg:flex items-center gap-3 px-3 py-1.5 rounded-[9px] bg-[#14171D]/90 border border-[#383E4C] text-[11px] font-mono text-[#8E95A2]">
              <span>scale: {computedJavaScale.toFixed(2)}</span>
              <span>
                mx: {mousePos.x} my: {mousePos.y}
              </span>
              <span className="text-[#8B5CF6]">
                overlayAnim: {isOverlayOpen ? '1.00' : '0.00'}
              </span>
            </div>
          )}

          {/* Proxy Status Indicator Pill (if proxy is active) */}
          {proxy.enabled && (
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                setActiveOverlay('proxies');
              }}
              onMouseEnter={() => uiSound.playHover()}
              className="hidden sm:flex items-center gap-1.5 px-2.5 py-1.5 rounded-[9px] bg-[#14171D] border border-[#383E4C] hover:border-white text-[11.5px] font-mono text-[#10B981] transition-all duration-[240ms] cursor-pointer"
            >
              <Globe className="w-3.5 h-3.5" />
              <span>
                {proxy.protocol} • {proxy.lastPing ?? 19}ms
              </span>
            </button>
          )}

          {/* Cosmetics Screen Toggle Button (this.toggleCosmetics() in MainMenuScreen.java) */}
          <button
            type="button"
            onClick={() => {
              uiSound.playClick();
              setActiveOverlay((prev) => (prev === 'cosmetics' ? 'none' : 'cosmetics'));
            }}
            onMouseEnter={() => uiSound.playHover()}
            className={`horus-pill-dark px-3 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer ${
              activeOverlay === 'cosmetics' ? '!border-white !text-white !bg-[#1E2127]' : ''
            }`}
            title="Open CosmeticsSelectionScreen"
          >
            <Sparkles className="w-3.5 h-3.5 text-[#8B5CF6]" />
            <span>Cosmetics</span>
          </button>

          {/* Java Render2D Inspector Quick Toggle */}
          <button
            type="button"
            onClick={() => {
              uiSound.playClick();
              setSettings((s) => ({ ...s, showJavaDebugHud: !s.showJavaDebugHud }));
            }}
            onMouseEnter={() => uiSound.playHover()}
            className={`horus-pill-dark px-2.5 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer ${
              settings.showJavaDebugHud ? '!border-white !text-white !bg-[#1E2127]' : ''
            }`}
            title="Toggle Render2D Hitbox & Telemetry Overlay"
          >
            <Bug className="w-3.5 h-3.5 text-[#8E95A2]" />
            <span className="hidden md:inline">Render2D</span>
          </button>

          {/* Fallen Client Loader / Build Selector */}
          <button
            type="button"
            onClick={() => {
              uiSound.playClick(true);
              setActiveOverlay((prev) => (prev === 'launcher' ? 'none' : 'launcher'));
            }}
            onMouseEnter={() => uiSound.playHover()}
            className="horus-pill-accent px-3.5 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
            title="Open Fallen Client Loader & Build Manager"
          >
            <Terminal className="w-3.5 h-3.5" />
            <span>Loader</span>
            <span className="hidden sm:inline font-mono text-[10.5px] opacity-85">
              1.21.4
            </span>
          </button>
        </div>
      </header>

      {/* =====================================================================
          CENTER VIEWPORT: drawCenterMenu() + Slide-in Overlay Panels
         ===================================================================== */}
      <main className="relative z-10 flex-1 flex items-center justify-center px-4">
        {/* Main Centered Menu Stack (drawCenterMenu)
            Translates upward by 18px*scale and fades to 0 when overlayAnim -> 1 */}
        <div
          style={{
            transform: `scale(${settings.guiScale}) translateY(${
              isOverlayOpen ? '-26px' : '0px'
            })`,
            opacity: isOverlayOpen ? 0 : 1,
            pointerEvents: isOverlayOpen ? 'none' : 'auto',
          }}
          className="w-full max-w-[380px] flex flex-col items-center transition-all duration-[260ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
        >
          {/* Stagger Index 0: Header Logo + Greeting + Subtitle */}
          <div
            style={{
              transitionDelay: isEntered ? '40ms' : '0ms',
              opacity: isEntered ? 1 : 0,
              transform: isEntered ? 'translateY(0px)' : 'translateY(14px)',
            }}
            className="flex flex-col items-center text-center transition-all duration-[450ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
          >
            {/* Center Logo above greeting (logoW = 14.5F * scale) */}
            <div
              onClick={triggerWelcomeTransition}
              title="Click to replay welcomeTransition animation"
              className="mb-5 cursor-pointer group relative flex items-center justify-center"
            >
              <HorusLogo
                size={28}
                className="transition-transform duration-300 group-hover:scale-110"
              />
            </div>

            {/* Greeting: "Good time of day, " + this.welcomeName() */}
            <h1 className="text-[19.5px] font-semibold text-[#EBECEF] tracking-[-0.015em] leading-snug">
              Good time of day, {welcomeName}
            </h1>

            {/* Subtitle: "Welcome to Fallen, the best Minecraft client." */}
            <p className="text-[12.5px] font-medium text-[#636875] mt-1 tracking-[-0.005em]">
              Welcome to Fallen, the best Minecraft client.
            </p>
          </div>

          {/* 3 Main Pill Buttons Stack (Singleplayer, Multiplayer, Swap Accounts) */}
          <div className="w-full mt-7 flex flex-col gap-[12px]">
            {/* Stagger Index 1: Singleplayer */}
            <div
              style={{
                transitionDelay: isEntered ? '110ms' : '0ms',
                opacity: isEntered ? 1 : 0,
                transform: isEntered ? 'translateY(0px)' : 'translateY(14px)',
              }}
              className="w-full transition-all duration-[450ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
            >
              <button
                type="button"
                onClick={() => {
                  uiSound.playClick();
                  setActiveOverlay('singleplayer');
                }}
                onMouseEnter={() => uiSound.playHover()}
                className={`horus-pill-dark w-full h-[50px] rounded-[13px] text-[13.5px] font-semibold tracking-[0.005em] flex items-center justify-center cursor-pointer relative ${
                  settings.showJavaDebugHud ? 'ring-1 ring-dashed ring-[#8B5CF6]/70' : ''
                }`}
              >
                <span>Singleplayer</span>
                {settings.showJavaDebugHud && (
                  <span className="absolute right-3 text-[9px] font-mono text-[#8B5CF6]">
                    idx:1 [230x32]
                  </span>
                )}
              </button>
            </div>

            {/* Stagger Index 2: Multiplayer */}
            <div
              style={{
                transitionDelay: isEntered ? '180ms' : '0ms',
                opacity: isEntered ? 1 : 0,
                transform: isEntered ? 'translateY(0px)' : 'translateY(14px)',
              }}
              className="w-full transition-all duration-[450ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
            >
              <button
                type="button"
                onClick={() => {
                  uiSound.playClick();
                  setActiveOverlay('multiplayer');
                }}
                onMouseEnter={() => uiSound.playHover()}
                className={`horus-pill-dark w-full h-[50px] rounded-[13px] text-[13.5px] font-semibold tracking-[0.005em] flex items-center justify-center cursor-pointer relative ${
                  settings.showJavaDebugHud ? 'ring-1 ring-dashed ring-[#8B5CF6]/70' : ''
                }`}
              >
                <span>Multiplayer</span>
                {settings.showJavaDebugHud && (
                  <span className="absolute right-3 text-[9px] font-mono text-[#8B5CF6]">
                    idx:2 [230x32]
                  </span>
                )}
              </button>
            </div>

            {/* Stagger Index 3: Swap Accounts (Accent Orange button: 0xFF8B5CF6 -> 0xFFC084FC) */}
            <div
              style={{
                transitionDelay: isEntered ? '250ms' : '0ms',
                opacity: isEntered ? 1 : 0,
                transform: isEntered ? 'translateY(0px)' : 'translateY(14px)',
              }}
              className="w-full transition-all duration-[450ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
            >
              <button
                type="button"
                onClick={() => {
                  uiSound.playClick(true);
                  setActiveOverlay('accounts');
                }}
                onMouseEnter={() => uiSound.playHover()}
                className={`horus-pill-accent w-full h-[50px] rounded-[13px] text-[13.5px] font-semibold tracking-[0.005em] flex items-center justify-center cursor-pointer relative ${
                  settings.showJavaDebugHud ? 'ring-1 ring-dashed ring-white' : ''
                }`}
              >
                <span>Swap Accounts</span>
                {settings.showJavaDebugHud && (
                  <span className="absolute right-3 text-[9px] font-mono text-white/90">
                    idx:3 [ACCENT]
                  </span>
                )}
              </button>
            </div>
          </div>

          {/* Stagger Index 4: Bottom Text Links Row (Options | Proxies | Exit) */}
          <div
            style={{
              transitionDelay: isEntered ? '320ms' : '0ms',
              opacity: isEntered ? 1 : 0,
              transform: isEntered ? 'translateY(0px)' : 'translateY(11px)',
            }}
            className="w-full mt-7 flex items-center justify-between px-6 transition-all duration-[450ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
          >
            {/* linkOptions */}
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                setActiveOverlay('options');
              }}
              onMouseEnter={() => uiSound.playHover()}
              className={`horus-text-link px-3 py-1.5 text-[13px] font-semibold cursor-pointer ${
                settings.showJavaDebugHud ? 'border border-dashed border-[#636875]' : ''
              }`}
            >
              Options
            </button>

            {/* linkProxies: ProxyManager.getInstance().isEnabled() ? "Proxies (ON)" : "Proxies" */}
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                setActiveOverlay('proxies');
              }}
              onMouseEnter={() => uiSound.playHover()}
              className={`horus-text-link px-3 py-1.5 text-[13px] font-semibold cursor-pointer flex items-center gap-1.5 ${
                proxy.enabled ? '!text-[#EBECEF]' : ''
              } ${settings.showJavaDebugHud ? 'border border-dashed border-[#636875]' : ''}`}
            >
              <span>{proxy.enabled ? 'Proxies (ON)' : 'Proxies'}</span>
              {proxy.enabled && (
                <span className="w-1.5 h-1.5 rounded-full bg-[#8B5CF6]" />
              )}
            </button>

            {/* linkExit: minecraftClient::scheduleStop */}
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                setExitToast(true);
              }}
              onMouseEnter={() => uiSound.playHover()}
              className={`horus-text-link px-3 py-1.5 text-[13px] font-semibold cursor-pointer ${
                settings.showJavaDebugHud ? 'border border-dashed border-[#636875]' : ''
              }`}
            >
              Exit
            </button>
          </div>
        </div>

        {/* =====================================================================
            SLIDE-IN OVERLAY PANELS
            Exact formula from MainMenuScreen.java:
            panelX = (screenW - 329.5F * scale) / 2.0F + 24.0F * scale * (1.0F - altAnim)
            Slides in from +24px on X axis over 260ms!
           ===================================================================== */}
        <div
          onClick={() => setActiveOverlay('none')}
          className={`fixed inset-0 z-30 flex items-center justify-center p-4 transition-all duration-[260ms] ease-[cubic-bezier(0.22,1,0.36,1)] ${
            isOverlayOpen
              ? 'opacity-100 pointer-events-auto bg-black/35 backdrop-blur-[2px]'
              : 'opacity-0 pointer-events-none'
          }`}
        >
          <div
            style={{
              transform: isOverlayOpen
                ? `scale(${settings.guiScale}) translateX(0px)`
                : `scale(${settings.guiScale}) translateX(34px)`,
            }}
            className="w-full flex items-center justify-center transition-transform duration-[260ms] ease-[cubic-bezier(0.22,1,0.36,1)]"
          >
            {activeOverlay === 'accounts' && (
              <AccountManagerPanel
                accounts={accounts}
                activeAccountId={activeAccountId}
                onSelectAccount={(id) => setActiveAccountId(id)}
                onAddAccount={handleAddAccount}
                onDeleteAccount={handleDeleteAccount}
                onToggleFavorite={handleToggleFavorite}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'cosmetics' && (
              <CosmeticsSelectionScreen
                activeAccount={activeAccount}
                cosmetics={cosmetics}
                onChangeCosmetics={setCosmetics}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'proxies' && (
              <ProxyScreen
                proxy={proxy}
                onChangeProxy={setProxy}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'options' && (
              <OptionsScreen
                settings={settings}
                onChangeSettings={setSettings}
                onReplayWelcome={triggerWelcomeTransition}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'multiplayer' && (
              <MultiplayerScreen
                servers={servers}
                proxy={proxy}
                onAddServer={(srv) => setServers((prev) => [srv, ...prev])}
                onJoinServer={(name, address) => {
                  setActiveSession({ title: name, address });
                  setActiveOverlay('ingame');
                }}
                onOpenProxies={() => setActiveOverlay('proxies')}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'singleplayer' && (
              <SelectWorldScreen
                worlds={worlds}
                onAddWorld={(w) => setWorlds((prev) => [w, ...prev])}
                onDeleteWorld={(id) =>
                  setWorlds((prev) => prev.filter((item) => item.id !== id))
                }
                onLaunchWorld={(worldName) => {
                  setActiveSession({
                    title: worldName,
                    address: 'Integrated Singleplayer Server (127.0.0.1)',
                  });
                  setActiveOverlay('ingame');
                }}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'launcher' && (
              <LauncherLoaderModal
                activeAccount={activeAccount}
                settings={settings}
                onChangeSettings={setSettings}
                onCompleteLaunch={triggerWelcomeTransition}
                onClose={() => setActiveOverlay('none')}
              />
            )}

            {activeOverlay === 'ingame' && (
              <InGameSessionModal
                sessionTitle={activeSession.title}
                sessionAddress={activeSession.address}
                activeAccount={activeAccount}
                proxy={proxy}
                onDisconnect={() => setActiveOverlay('none')}
              />
            )}
          </div>
        </div>
      </main>

      {/* =====================================================================
          EXIT SCHEDULE-STOP TOAST (minecraftClient::scheduleStop)
         ===================================================================== */}
      {exitToast && (
        <div className="fixed bottom-14 left-1/2 -translate-x-1/2 z-40 px-4 py-3 rounded-[12px] bg-[#14171D] border border-[#FFFFFF] shadow-[0_16px_50px_rgba(0,0,0,0.85)] flex items-center gap-4 animate-in fade-in slide-in-from-bottom-2 duration-200">
          <div className="flex items-center gap-2 text-[12.5px] text-[#EBECEF]">
            <CheckCircle2 className="w-4 h-4 text-[#8B5CF6]" />
            <span>
              <code className="font-mono text-[#8E95A2]">minecraftClient.scheduleStop()</code>{' '}
              invoked
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                uiSound.playClick(true);
                triggerWelcomeTransition();
              }}
              className="horus-pill-accent px-3 py-1 rounded-[7px] text-[11.5px] font-semibold flex items-center gap-1 cursor-pointer"
            >
              <RotateCcw className="w-3 h-3" />
              <span>Restart Client</span>
            </button>
            <button
              type="button"
              onClick={() => setExitToast(false)}
              className="horus-pill-dark px-2.5 py-1 rounded-[7px] text-[11.5px] font-semibold cursor-pointer"
            >
              Dismiss
            </button>
          </div>
        </div>
      )}

      {/* =====================================================================
          BOTTOM POLICY DISCLAIMER FOOTER (drawChrome() lines 199-204)
          "By logging into your account you agree to all of our policies,"
          "including our Privacy Policy and Terms of Service"
          Color: COLOR_TEXT_POLICY = 0xFF3B404A (#3B404A)
         ===================================================================== */}
      <footer
        className="relative z-20 w-full pb-4 px-5 flex flex-col items-center justify-center text-center pointer-events-none transition-opacity duration-[260ms]"
        style={{
          opacity: isEntered ? (isOverlayOpen ? 0.55 : 1) : 0,
        }}
      >
        <p className="text-[11px] font-semibold text-[#3B404A] leading-[1.45] tracking-[0.005em]">
          By logging into your account you agree to all of our policies,
        </p>
        <p className="text-[11px] font-semibold text-[#3B404A] leading-[1.45] tracking-[0.005em]">
          including our Privacy Policy and Terms of Service
        </p>

        {/* Subtle Left/Right Corner Metadata */}
        <div className="hidden sm:flex w-full items-center justify-between text-[10px] font-mono text-[#2A2E37] mt-1">
          <span className="flex items-center gap-1">
            <ShieldCheck className="w-3 h-3" />
            horus.client.ui.MainMenuScreen
          </span>
          <span>{settings.selectedBranch}</span>
        </div>
      </footer>
    </div>
  );
}

export default App;
