import React, { useEffect, useState } from 'react';
import {
  CheckCircle2,
  Cpu,
  KeyRound,
  Layers,
  Rocket,
  ShieldCheck,
  Terminal,
  X,
} from 'lucide-react';
import { AccountProfile, ClientSettings } from '../../types/launcher';
import { HorusLogo } from '../HorusLogo';
import { uiSound } from '../../utils/sound';

interface LauncherLoaderModalProps {
  activeAccount: AccountProfile;
  settings: ClientSettings;
  onChangeSettings: (next: ClientSettings) => void;
  onCompleteLaunch: () => void;
  onClose: () => void;
}

const LAUNCH_STEPS = [
  'Connecting to ru.dreamix.protection.api.impl.profile.ProfileAPI...',
  'Authenticated session token [HWID Verified • Tier: Lifetime]',
  'Allocating JVM Heap (-Xmx4096M -XX:+UseG1GC)...',
  'Mounting assets/horus/textures/gui/mainmenu/icons/logo.png...',
  'Compiling Render2D shaders & FontFace.SEMI glyph atlas...',
  'Initializing InterfaceModule & ProxyManager hooks...',
  'Invoking MinecraftClient.setScreen(new MainMenuScreen(true))...',
];

const CHANGELOG_ITEMS = [
  {
    version: 'v4.2.8 — Horus UI Overhaul',
    date: 'Today',
    changes: [
      'Redesigned MainMenuScreen with minimalist #0B0D10 obsidian theme & #DE6C14 accent pill',
      'Added smooth 240ms gray-to-white hover border interpolation (0xFF383E4C -> 0xFFFFFFFF)',
      'Integrated slide-in AccountManagerPanel & rotatable CosmeticsSelectionScreen',
      'Updated GrimAC / Polar velocity & ElytraTarget movement bypasses for 1.21.4',
    ],
  },
  {
    version: 'v4.2.5 — Dreamix Protection & Proxy Update',
    date: '4 days ago',
    changes: [
      'Added native SOCKS5/SOCKS4 low-latency relay switcher inside ProxyScreen',
      'Improved FontText.processData3 subpixel anti-aliasing on all GuiScale factors',
    ],
  },
];

export const LauncherLoaderModal: React.FC<LauncherLoaderModalProps> = ({
  activeAccount,
  settings,
  onChangeSettings,
  onCompleteLaunch,
  onClose,
}) => {
  const [isLaunching, setIsLaunching] = useState(false);
  const [stepIndex, setStepIndex] = useState(0);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (!isLaunching) return;

    const totalDuration = 2100;
    const intervalMs = 50;
    let elapsed = 0;

    const timer = setInterval(() => {
      elapsed += intervalMs;
      const pct = Math.min(100, Math.round((elapsed / totalDuration) * 100));
      setProgress(pct);
      const nextStep = Math.min(
        LAUNCH_STEPS.length - 1,
        Math.floor((elapsed / totalDuration) * LAUNCH_STEPS.length)
      );
      setStepIndex(nextStep);

      if (elapsed >= totalDuration) {
        clearInterval(timer);
        uiSound.playClick(true);
        fetch('/api/launch', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: activeAccount.username,
            ram: Math.round(settings.ramAllocationMb / 1024),
          }),
        }).catch(() => {});
        setTimeout(() => {
          onCompleteLaunch();
        }, 160);
      }
    }, intervalMs);

    return () => clearInterval(timer);
  }, [isLaunching, onCompleteLaunch]);

  const branches: ClientSettings['selectedBranch'][] = [
    'Fallen 1.21.4 (Stable)',
    'Horus 1.21.4-dev (Nightly)',
    'Fallen 1.16.5 (Legacy)',
  ];

  return (
    <div
      className="w-full max-w-[620px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.88)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-[10px] bg-[#0B0D10] border border-[#383E4C] flex items-center justify-center">
            <HorusLogo size={20} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
                Fallen Client Loader
              </span>
              <span className="px-2 py-0.5 text-[10px] font-mono rounded-md bg-[#DE6C14]/15 text-[#F28028] border border-[#DE6C14]/30">
                Dreamix Guard v3.4
              </span>
            </div>
            <p className="text-[12px] text-[#636875]">
              Select client build, inspect protection telemetry &amp; initialize MainMenuScreen
            </p>
          </div>
        </div>

        <button
          disabled={isLaunching}
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="w-8 h-8 rounded-[9px] bg-[#0B0D10] border border-[#383E4C] hover:border-white text-[#8E95A2] hover:text-white flex items-center justify-center transition-all duration-200 cursor-pointer disabled:opacity-40"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Body */}
      <div className="p-6 space-y-5">
        {/* ProfileAPI Protection Telemetry Cards */}
        <div className="grid grid-cols-3 gap-2.5">
          <div className="p-3 rounded-[11px] bg-[#0B0D10] border border-[#282D38]">
            <div className="text-[10.5px] text-[#636875] flex items-center gap-1.5">
              <KeyRound className="w-3.5 h-3.5 text-[#DE6C14]" />
              <span>ProfileAPI.getName()</span>
            </div>
            <div className="text-[14px] font-semibold text-[#EBECEF] mt-1 truncate">
              {activeAccount.username}
            </div>
            <div className="text-[10.5px] font-mono text-[#8E95A2] mt-0.5">
              UID #{activeAccount.uid} • {activeAccount.role}
            </div>
          </div>

          <div className="p-3 rounded-[11px] bg-[#0B0D10] border border-[#282D38]">
            <div className="text-[10.5px] text-[#636875] flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-[#10B981]" />
              <span>Dreamix Protection</span>
            </div>
            <div className="text-[14px] font-semibold text-[#EBECEF] mt-1">HWID Bound</div>
            <div className="text-[10.5px] font-mono text-[#10B981] mt-0.5">
              Status: Verified
            </div>
          </div>

          <div className="p-3 rounded-[11px] bg-[#0B0D10] border border-[#282D38]">
            <div className="text-[10.5px] text-[#636875] flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-[#8E95A2]" />
              <span>JVM Runtime</span>
            </div>
            <div className="text-[14px] font-semibold text-[#EBECEF] mt-1">
              Java 21.0.4 LTS
            </div>
            <div className="text-[10.5px] font-mono text-[#8E95A2] mt-0.5">
              Heap: {settings.ramAllocationMb} MB
            </div>
          </div>
        </div>

        {/* Build Branch Selector */}
        <div>
          <label className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5 mb-2">
            <Layers className="w-3.5 h-3.5 text-[#DE6C14]" />
            <span>Select Client Branch</span>
          </label>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
            {branches.map((branch) => {
              const active = settings.selectedBranch === branch;
              return (
                <button
                  key={branch}
                  type="button"
                  disabled={isLaunching}
                  onClick={() => {
                    uiSound.playClick();
                    onChangeSettings({ ...settings, selectedBranch: branch });
                  }}
                  className={`px-3 py-2.5 rounded-[10px] border text-left transition-all cursor-pointer ${
                    active
                      ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                      : 'bg-[#0B0D10] border-[#2C313D] text-[#8E95A2] hover:border-white hover:text-white'
                  }`}
                >
                  <div className="text-[12px] font-semibold truncate">{branch}</div>
                  <div className="text-[10px] font-mono text-[#636875] mt-0.5">
                    {branch.includes('Nightly')
                      ? 'Build #0429 • Experimental'
                      : branch.includes('1.16.5')
                      ? 'Build #0312 • Classic'
                      : 'Build #0428 • Recommended'}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Changelog or Live Injection Terminal */}
        {isLaunching ? (
          <div className="p-4 rounded-[12px] bg-[#0B0D10] border border-[#383E4C] space-y-3">
            <div className="flex items-center justify-between text-[12px]">
              <span className="font-mono text-[#EBECEF] flex items-center gap-2">
                <Terminal className="w-3.5 h-3.5 text-[#DE6C14] animate-pulse" />
                <span>Injecting {settings.selectedBranch}...</span>
              </span>
              <span className="font-mono font-bold text-[#DE6C14]">{progress}%</span>
            </div>

            {/* Progress Bar */}
            <div className="w-full h-2 rounded-full bg-[#14171D] overflow-hidden border border-[#282D38]">
              <div
                className="h-full bg-[#DE6C14] transition-all duration-75 rounded-full"
                style={{ width: `${progress}%` }}
              />
            </div>

            {/* Console Output Lines */}
            <div className="space-y-1 font-mono text-[11px] pt-1">
              {LAUNCH_STEPS.slice(0, stepIndex + 1).map((line, i) => (
                <div
                  key={i}
                  className={`flex items-center gap-2 ${
                    i === stepIndex ? 'text-[#EBECEF]' : 'text-[#636875]'
                  }`}
                >
                  <CheckCircle2
                    className={`w-3 h-3 shrink-0 ${
                      i === stepIndex ? 'text-[#DE6C14]' : 'text-[#10B981]'
                    }`}
                  />
                  <span className="truncate">{line}</span>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="p-4 rounded-[12px] bg-[#0B0D10] border border-[#282D38] max-h-[155px] overflow-y-auto space-y-3">
            {CHANGELOG_ITEMS.map((item) => (
              <div key={item.version}>
                <div className="flex items-center justify-between">
                  <span className="text-[12.5px] font-semibold text-[#EBECEF]">
                    {item.version}
                  </span>
                  <span className="text-[10.5px] font-mono text-[#636875]">{item.date}</span>
                </div>
                <ul className="mt-1.5 space-y-1">
                  {item.changes.map((c, idx) => (
                    <li
                      key={idx}
                      className="text-[11.5px] text-[#8E95A2] flex items-start gap-2"
                    >
                      <span className="text-[#DE6C14] mt-0.5">•</span>
                      <span>{c}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <span className="text-[11px] font-mono text-[#636875]">
          horus.client.ui.MainMenuScreen(true)
        </span>

        <button
          type="button"
          disabled={isLaunching}
          onClick={() => {
            uiSound.playClick(true);
            setIsLaunching(true);
            setProgress(0);
            setStepIndex(0);
          }}
          onMouseEnter={() => uiSound.playHover()}
          className="horus-pill-accent px-5 py-2 rounded-[9px] text-[12.5px] font-semibold flex items-center gap-2 cursor-pointer disabled:opacity-60"
        >
          <Rocket className="w-4 h-4" />
          <span>{isLaunching ? 'Initializing Client...' : 'Launch & Inject Fallen'}</span>
        </button>
      </div>
    </div>
  );
};
