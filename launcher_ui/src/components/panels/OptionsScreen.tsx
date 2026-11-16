import React from 'react';
import { Code2, Cpu, Monitor, Palette, Sliders, Volume2, X } from 'lucide-react';
import { ClientSettings } from '../../types/launcher';
import { ACCENT_PRESETS } from '../../data/mockData';
import { uiSound } from '../../utils/sound';

interface OptionsScreenProps {
  settings: ClientSettings;
  onChangeSettings: (next: ClientSettings) => void;
  onReplayWelcome: () => void;
  onClose: () => void;
}

export const OptionsScreen: React.FC<OptionsScreenProps> = ({
  settings,
  onChangeSettings,
  onReplayWelcome,
  onClose,
}) => {
  return (
    <div
      className="w-full max-w-[540px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Options &amp; InterfaceModule
            </span>
            <span className="px-2 py-0.5 text-[10px] font-mono rounded-md bg-[#1E2127] text-[#8E95A2] border border-[#2C313D]">
              UiTheme.flag = true
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            Configure GuiScale, Render2D telemetry, JVM memory, and client theme
          </p>
        </div>

        <button
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="w-8 h-8 rounded-[9px] bg-[#0B0D10] border border-[#383E4C] hover:border-white text-[#8E95A2] hover:text-white flex items-center justify-center transition-all duration-200 cursor-pointer"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Options Body */}
      <div className="p-6 space-y-5 max-h-[390px] overflow-y-auto">
        {/* GUI Scale factor */}
        <div>
          <label className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5 mb-2">
            <Monitor className="w-3.5 h-3.5 text-[#DE6C14]" />
            <span>GuiScale Factor (spaceFactor)</span>
          </label>
          <div className="grid grid-cols-3 gap-2">
            {[
              { label: 'Compact (0.9x)', value: 0.9 },
              { label: 'Native (1.0x)', value: 1.0 },
              { label: 'Large (1.12x)', value: 1.12 },
            ].map((item) => {
              const active = Math.abs(settings.guiScale - item.value) < 0.01;
              return (
                <button
                  key={item.value}
                  type="button"
                  onClick={() => {
                    uiSound.playClick();
                    onChangeSettings({ ...settings, guiScale: item.value });
                  }}
                  className={`py-2 px-3 rounded-[9px] text-[12px] font-semibold border transition-all cursor-pointer ${
                    active
                      ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                      : 'bg-[#0B0D10] border-[#2C313D] text-[#8E95A2] hover:border-white hover:text-white'
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Accent Color Selector */}
        <div>
          <label className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5 mb-2">
            <Palette className="w-3.5 h-3.5 text-[#DE6C14]" />
            <span>COLOR_BTN_ACCENT Preset</span>
          </label>
          <div className="grid grid-cols-1 gap-1.5">
            {ACCENT_PRESETS.map((preset) => {
              const active = settings.accentId === preset.id;
              return (
                <button
                  key={preset.id}
                  type="button"
                  onClick={() => {
                    uiSound.playClick(true);
                    onChangeSettings({ ...settings, accentId: preset.id });
                  }}
                  className={`flex items-center justify-between px-3.5 py-2 rounded-[9px] border text-left transition-all cursor-pointer ${
                    active
                      ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                      : 'bg-[#0B0D10] border-[#2C313D] text-[#8E95A2] hover:border-white hover:text-white'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <span
                      className="w-4 h-4 rounded-full border border-white/25"
                      style={{ backgroundColor: preset.base }}
                    />
                    <span className="text-[12.5px] font-semibold">{preset.name}</span>
                  </div>
                  <span className="text-[11px] font-mono text-[#636875]">{preset.base}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* JVM Memory Slider */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-[#DE6C14]" />
              <span>JVM Heap Allocation (-Xmx)</span>
            </label>
            <span className="text-[12px] font-mono font-semibold text-[#EBECEF]">
              {settings.ramAllocationMb} MB
            </span>
          </div>
          <input
            type="range"
            min={2048}
            max={16384}
            step={512}
            value={settings.ramAllocationMb}
            onChange={(e) =>
              onChangeSettings({
                ...settings,
                ramAllocationMb: Number(e.target.value),
              })
            }
            className="w-full accent-[#DE6C14] bg-[#0B0D10] h-2 rounded-lg cursor-pointer"
          />
          <div className="flex justify-between text-[10px] font-mono text-[#636875] mt-1">
            <span>2048 MB</span>
            <span>4096 MB (Recommended)</span>
            <span>8192 MB</span>
            <span>16384 MB</span>
          </div>
        </div>

        {/* Toggle Switches */}
        <div className="space-y-2 pt-1 border-t border-[#222731]">
          {/* Render2D Debug HUD */}
          <div className="flex items-center justify-between py-1.5">
            <div className="flex items-center gap-2.5">
              <Code2 className="w-4 h-4 text-[#8E95A2]" />
              <div>
                <div className="text-[12.5px] font-semibold text-[#EBECEF]">
                  Render2D &amp; Hitbox Inspector Overlay
                </div>
                <div className="text-[11px] text-[#636875]">
                  Visualize MainMenuScreen.DataRecord hitboxes, scale &amp; AnimatedValue states
                </div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                onChangeSettings({
                  ...settings,
                  showJavaDebugHud: !settings.showJavaDebugHud,
                });
              }}
              className={`px-3 py-1 rounded-[7px] text-[11px] font-mono font-semibold border transition-colors cursor-pointer ${
                settings.showJavaDebugHud
                  ? 'bg-[#DE6C14] border-white text-white'
                  : 'bg-[#0B0D10] border-[#383E4C] text-[#636875] hover:text-white'
              }`}
            >
              {settings.showJavaDebugHud ? 'ON' : 'OFF'}
            </button>
          </div>

          {/* UI Sound Engine */}
          <div className="flex items-center justify-between py-1.5">
            <div className="flex items-center gap-2.5">
              <Volume2 className="w-4 h-4 text-[#8E95A2]" />
              <div>
                <div className="text-[12.5px] font-semibold text-[#EBECEF]">
                  UI Micro-Audio Feedback
                </div>
                <div className="text-[11px] text-[#636875]">
                  Subtle hover and click sound synthesis
                </div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => {
                const next = !settings.soundEnabled;
                uiSound.enabled = next;
                if (next) uiSound.playClick(true);
                onChangeSettings({
                  ...settings,
                  soundEnabled: next,
                });
              }}
              className={`px-3 py-1 rounded-[7px] text-[11px] font-mono font-semibold border transition-colors cursor-pointer ${
                settings.soundEnabled
                  ? 'bg-[#DE6C14] border-white text-white'
                  : 'bg-[#0B0D10] border-[#383E4C] text-[#636875] hover:text-white'
              }`}
            >
              {settings.soundEnabled ? 'ON' : 'OFF'}
            </button>
          </div>

          {/* Ambient Glow */}
          <div className="flex items-center justify-between py-1.5">
            <div className="flex items-center gap-2.5">
              <Sliders className="w-4 h-4 text-[#8E95A2]" />
              <div>
                <div className="text-[12.5px] font-semibold text-[#EBECEF]">
                  Subtle Ambient Spotlight
                </div>
                <div className="text-[11px] text-[#636875]">
                  Soft radial aura behind center menu stack (#0B0D10 base preserved)
                </div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                onChangeSettings({
                  ...settings,
                  ambientGlow: !settings.ambientGlow,
                });
              }}
              className={`px-3 py-1 rounded-[7px] text-[11px] font-mono font-semibold border transition-colors cursor-pointer ${
                settings.ambientGlow
                  ? 'bg-[#DE6C14] border-white text-white'
                  : 'bg-[#0B0D10] border-[#383E4C] text-[#636875] hover:text-white'
              }`}
            >
              {settings.ambientGlow ? 'ON' : 'OFF'}
            </button>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <button
          type="button"
          onClick={() => {
            uiSound.playClick(true);
            onClose();
            onReplayWelcome();
          }}
          className="horus-pill-dark px-3 py-1.5 rounded-[8px] text-[11.5px] font-semibold cursor-pointer"
        >
          Replay Welcome Transition (flag3)
        </button>

        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="horus-pill-accent px-4 py-1.5 rounded-[8px] text-[12px] font-semibold cursor-pointer"
        >
          Done
        </button>
      </div>
    </div>
  );
};
