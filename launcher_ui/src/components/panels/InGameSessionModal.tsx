import React, { useState } from 'react';
import {
  Activity,
  ArrowLeft,
  Crosshair,
  Eye,
  Heart,
  ShieldAlert,
  Sparkles,
  Swords,
  Zap,
} from 'lucide-react';
import { AccountProfile, ProxyConfig } from '../../types/launcher';
import { PixelAvatar } from '../PixelAvatar';
import { HorusLogo } from '../HorusLogo';
import { uiSound } from '../../utils/sound';

interface InGameSessionModalProps {
  sessionTitle: string;
  sessionAddress: string;
  activeAccount: AccountProfile;
  proxy: ProxyConfig;
  onDisconnect: () => void;
}

interface ClientModuleItem {
  id: string;
  name: string;
  category: 'Combat' | 'Movement' | 'Render' | 'Player';
  suffix: string;
  bind: string;
  enabled: boolean;
}

const INITIAL_MODULES: ClientModuleItem[] = [
  { id: 'm-aura', name: 'AttackAura', category: 'Combat', suffix: 'Grim 1.21', bind: 'R', enabled: true },
  { id: 'm-vel', name: 'Velocity', category: 'Combat', suffix: 'Cancel', bind: 'V', enabled: true },
  { id: 'm-totem', name: 'AutoTotem', category: 'Combat', suffix: 'Smart', bind: 'NONE', enabled: true },
  { id: 'm-strafe', name: 'TargetStrafe', category: 'Movement', suffix: ' Elytra', bind: 'G', enabled: true },
  { id: 'm-noslow', name: 'NoSlow', category: 'Movement', suffix: 'GrimTick', bind: 'NONE', enabled: true },
  { id: 'm-sprint', name: 'KeepSprint', category: 'Movement', suffix: 'Rage', bind: 'NONE', enabled: true },
  { id: 'm-hud', name: 'InterfaceModule', category: 'Render', suffix: 'Fallen', bind: 'RSHIFT', enabled: true },
  { id: 'm-esp', name: 'NameTags', category: 'Render', suffix: 'Armor+HP', bind: 'NONE', enabled: true },
  { id: 'm-china', name: 'ChinaHat', category: 'Render', suffix: 'Ember', bind: 'NONE', enabled: false },
  { id: 'm- pearl', name: 'ClickPearl', category: 'Player', suffix: 'Silent', bind: 'MOUSE3', enabled: false },
];

export const InGameSessionModal: React.FC<InGameSessionModalProps> = ({
  sessionTitle,
  sessionAddress,
  activeAccount,
  proxy,
  onDisconnect,
}) => {
  const [modules, setModules] = useState<ClientModuleItem[]>(INITIAL_MODULES);
  const [targetHp, setTargetHp] = useState<number>(16.5);

  const toggleModule = (id: string) => {
    uiSound.playClick(true);
    setModules((prev) =>
      prev.map((m) => (m.id === id ? { ...m, enabled: !m.enabled } : m))
    );
  };

  const simulateHit = () => {
    uiSound.playClick(true);
    setTargetHp((hp) => (hp <= 3.5 ? 20.0 : Number((hp - 2.5).toFixed(1))));
  };

  const activeModules = modules
    .filter((m) => m.enabled)
    .sort((a, b) => (b.name + b.suffix).length - (a.name + a.suffix).length);

  return (
    <div
      className="w-full max-w-[780px] rounded-[16px] bg-[#0B0D10] border border-[#383E4C] shadow-[0_24px_90px_rgba(0,0,0,0.92)] overflow-hidden relative"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Top In-Game Watermark Bar (InterfaceModule) */}
      <div className="px-5 py-3.5 bg-[#14171D] border-b border-[#252A35] flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2.5">
          <div className="px-3 py-1 rounded-[8px] bg-[#0B0D10] border border-[#383E4C] flex items-center gap-2 text-[12px] font-mono">
            <HorusLogo size={15} />
            <span className="font-bold text-[#EBECEF]">Fallen</span>
            <span className="text-[#636875]">|</span>
            <span className="text-[#DE6C14] font-semibold">{activeAccount.username}</span>
            <span className="text-[#636875]">|</span>
            <span className="text-[#8E95A2]">340 FPS</span>
            <span className="text-[#636875]">|</span>
            <span className="text-[#10B981]">
              {proxy.enabled ? `${proxy.lastPing ?? 18}ms (Proxy)` : '16ms'}
            </span>
          </div>

          <span className="text-[11.5px] text-[#636875] font-mono hidden sm:inline">
            Connected: <strong className="text-[#8E95A2]">{sessionTitle}</strong> ({sessionAddress})
          </span>
        </div>

        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            onDisconnect();
          }}
          className="horus-pill-dark px-3 py-1 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
        >
          <ArrowLeft className="w-3.5 h-3.5 text-[#DE6C14]" />
          <span>Back to MainMenuScreen (ESC)</span>
        </button>
      </div>

      {/* Simulated Viewport with ClickGUI & TargetHUD & ArrayList */}
      <div className="p-6 grid grid-cols-1 md:grid-cols-12 gap-5 min-h-[380px] relative bg-[radial-gradient(circle_at_50%_20%,rgba(222,108,20,0.08),transparent_65%)]">
        {/* Left & Center: Interactive ClickGUI & TargetHUD */}
        <div className="md:col-span-8 flex flex-col justify-between space-y-4">
          {/* Mini ClickGUI Panel */}
          <div className="rounded-[12px] bg-[#14171D]/95 border border-[#383E4C] p-4">
            <div className="flex items-center justify-between mb-3 pb-2 border-b border-[#222731]">
              <div className="flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-[#DE6C14]" />
                <span className="text-[13px] font-semibold text-[#EBECEF]">
                  horus.client.module.render.InterfaceModule — ClickGUI Preview
                </span>
              </div>
              <span className="text-[10.5px] font-mono text-[#636875]">
                Click module to toggle
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {modules.map((mod) => (
                <button
                  key={mod.id}
                  type="button"
                  onClick={() => toggleModule(mod.id)}
                  onMouseEnter={() => uiSound.playHover()}
                  className={`flex items-center justify-between px-3 py-2 rounded-[9px] border text-left transition-all cursor-pointer ${
                    mod.enabled
                      ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                      : 'bg-[#0B0D10] border-[#282D38] text-[#636875] hover:border-[#383E4C] hover:text-[#8E95A2]'
                  }`}
                >
                  <div className="min-w-0">
                    <div className="text-[12.5px] font-semibold flex items-center gap-1.5">
                      <span
                        className={`w-1.5 h-1.5 rounded-full ${
                          mod.enabled ? 'bg-[#DE6C14]' : 'bg-[#383E4C]'
                        }`}
                      />
                      <span className="truncate">{mod.name}</span>
                    </div>
                    <div className="text-[10px] font-mono text-[#636875] pl-3">
                      {mod.category} • {mod.suffix}
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#0B0D10] border border-[#282D38] text-[#8E95A2]">
                    {mod.bind}
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Interactive Fallen TargetHUD Preview */}
          <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-[12px] bg-[#14171D] border border-[#383E4C]">
            <div className="flex items-center gap-3.5">
              <div className="relative">
                <PixelAvatar skin={activeAccount.skin} size={38} />
                <span className="absolute -top-1 -right-1 px-1 rounded bg-[#DE6C14] text-[9px] font-mono font-bold text-white">
                  WIN
                </span>
              </div>
              <div className="min-w-[180px]">
                <div className="flex items-center justify-between">
                  <span className="text-[13.5px] font-semibold text-[#EBECEF]">
                    Target: GrimBot_1337
                  </span>
                  <span className="text-[12px] font-mono font-bold text-[#DE6C14] flex items-center gap-1">
                    <Heart className="w-3 h-3 fill-current" />
                    {targetHp.toFixed(1)} HP
                  </span>
                </div>
                {/* Health Bar */}
                <div className="w-full h-2 rounded-full bg-[#0B0D10] border border-[#282D38] mt-1.5 overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-[#DE6C14] to-[#F28028] transition-all duration-200 rounded-full"
                    style={{ width: `${(targetHp / 20) * 100}%` }}
                  />
                </div>
                <div className="text-[10.5px] font-mono text-[#636875] mt-1">
                  Distance: 2.84m • HurtTime: 0 • Totems: 3
                </div>
              </div>
            </div>

            <button
              type="button"
              onClick={simulateHit}
              className="horus-pill-accent px-3.5 py-2 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
            >
              <Swords className="w-3.5 h-3.5" />
              <span>Simulate Critical Hit</span>
            </button>
          </div>
        </div>

        {/* Right Column: Live Horus ArrayList */}
        <div className="md:col-span-4 flex flex-col items-end justify-between">
          <div className="w-full flex flex-col items-end">
            <div className="text-[10.5px] font-mono uppercase tracking-wider text-[#636875] mb-2 flex items-center gap-1.5">
              <Activity className="w-3 h-3 text-[#DE6C14]" />
              <span>Active ArrayList ({activeModules.length})</span>
            </div>

            <div className="flex flex-col items-end space-y-1 w-full">
              {activeModules.map((m) => (
                <div
                  key={m.id}
                  className="px-2.5 py-1 rounded-l-[7px] bg-[#14171D] border-l-2 border-y border-[#282D38] border-r-2 border-r-[#DE6C14] text-[12px] font-medium flex items-center gap-1.5 shadow-sm"
                >
                  <span className="text-[#EBECEF] font-semibold">{m.name}</span>
                  <span className="text-[#8E95A2] text-[11px]">{m.suffix}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Bottom Keystrokes Widget */}
          <div className="w-full mt-4 p-3 rounded-[11px] bg-[#14171D] border border-[#282D38] flex items-center justify-between text-[11px] font-mono text-[#8E95A2]">
            <div className="flex items-center gap-1.5">
              <Zap className="w-3.5 h-3.5 text-[#DE6C14]" />
              <span>BPS: 14.82</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Crosshair className="w-3.5 h-3.5 text-[#10B981]" />
              <span>CPS: 13 / 11</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Eye className="w-3.5 h-3.5 text-[#8E95A2]" />
              <span>FOV: 105</span>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Notice */}
      <div className="px-5 py-2.5 bg-[#101218] border-t border-[#222731] flex items-center justify-between text-[11px] text-[#636875]">
        <span className="flex items-center gap-1.5">
          <ShieldAlert className="w-3.5 h-3.5 text-[#DE6C14]" />
          <span>Horus 2D Render Engine • FontFace.SEMI Active</span>
        </span>
        <span className="font-mono">Press ESC to return to MainMenuScreen</span>
      </div>
    </div>
  );
};
