import React, { useState } from 'react';
import { Activity, Check, Globe, Power, Shield, Wifi, X } from 'lucide-react';
import { ProxyConfig, ProxyPreset } from '../../types/launcher';
import { PROXY_PRESETS } from '../../data/mockData';
import { uiSound } from '../../utils/sound';

interface ProxyScreenProps {
  proxy: ProxyConfig;
  onChangeProxy: (next: ProxyConfig) => void;
  onClose: () => void;
}

export const ProxyScreen: React.FC<ProxyScreenProps> = ({
  proxy,
  onChangeProxy,
  onClose,
}) => {
  const [testingPing, setTestingPing] = useState(false);

  const handleToggleEnabled = () => {
    uiSound.playClick(true);
    onChangeProxy({
      ...proxy,
      enabled: !proxy.enabled,
    });
  };

  const handleSelectPreset = (preset: ProxyPreset) => {
    uiSound.playClick(true);
    onChangeProxy({
      ...proxy,
      enabled: true,
      protocol: preset.protocol,
      host: preset.host,
      port: String(preset.port),
      activePresetId: preset.id,
      lastPing: preset.ping,
    });
  };

  const handlePingTest = () => {
    uiSound.playClick();
    setTestingPing(true);
    setTimeout(() => {
      const measured = Math.floor(14 + Math.random() * 22);
      onChangeProxy({
        ...proxy,
        lastPing: measured,
      });
      setTestingPing(false);
    }, 380);
  };

  return (
    <div
      className="w-full max-w-[520px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Proxy Manager
            </span>
            <span
              className={`px-2 py-0.5 text-[10px] font-mono rounded-md border ${
                proxy.enabled
                  ? 'bg-[#10B981]/15 text-[#10B981] border-[#10B981]/30'
                  : 'bg-[#1E2127] text-[#636875] border-[#2C313D]'
              }`}
            >
              {proxy.enabled ? 'Proxies (ON)' : 'DISABLED'}
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            horus.client.proxy.ProxyManager • Route multiplayer packets through SOCKS5/4
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

      {/* Master Toggle Strip */}
      <div className="px-6 py-3.5 bg-[#0B0D10]/80 border-b border-[#222731] flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div
            className={`w-9 h-9 rounded-[10px] flex items-center justify-center border transition-colors ${
              proxy.enabled
                ? 'bg-[#DE6C14]/15 border-[#DE6C14] text-[#DE6C14]'
                : 'bg-[#14171D] border-[#2C313D] text-[#636875]'
            }`}
          >
            <Globe className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[13px] font-semibold text-[#EBECEF]">
              {proxy.enabled ? 'Proxy Tunnel Active' : 'Direct Connection (No Proxy)'}
            </div>
            <div className="text-[11px] text-[#636875] font-mono">
              {proxy.host}:{proxy.port} ({proxy.protocol})
              {proxy.lastPing !== null && ` • ${proxy.lastPing} ms`}
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={handleToggleEnabled}
          onMouseEnter={() => uiSound.playHover()}
          className={`px-3.5 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer ${
            proxy.enabled ? 'horus-pill-accent' : 'horus-pill-dark'
          }`}
        >
          <Power className="w-3.5 h-3.5" />
          <span>{proxy.enabled ? 'Disable' : 'Enable Proxy'}</span>
        </button>
      </div>

      <div className="p-6 space-y-4">
        {/* Quick Presets */}
        <div>
          <div className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2] mb-2 flex items-center justify-between">
            <span>Dreamix Protected Relays</span>
            <span className="text-[10.5px] font-normal text-[#636875]">1-click connect</span>
          </div>

          <div className="grid grid-cols-1 gap-1.5 max-h-[165px] overflow-y-auto pr-1">
            {PROXY_PRESETS.map((preset) => {
              const isSelected = proxy.activePresetId === preset.id && proxy.enabled;
              return (
                <button
                  key={preset.id}
                  type="button"
                  onClick={() => handleSelectPreset(preset)}
                  onMouseEnter={() => uiSound.playHover()}
                  className={`flex items-center justify-between px-3.5 py-2 rounded-[10px] border text-left transition-all duration-200 cursor-pointer ${
                    isSelected
                      ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                      : 'bg-[#0E1116] border-[#2C313D] hover:bg-[#191C22] hover:border-white text-[#8E95A2]'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <span className="px-1.5 py-0.5 rounded bg-[#181C24] border border-[#2C313D] text-[10px] font-mono font-bold text-[#EBECEF]">
                      {preset.country}
                    </span>
                    <div>
                      <div className="text-[12.5px] font-semibold text-[#EBECEF]">
                        {preset.label}
                      </div>
                      <div className="text-[10.5px] font-mono text-[#636875]">
                        {preset.host}:{preset.port} • {preset.protocol}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="text-[11px] font-mono text-[#10B981] flex items-center gap-1">
                      <Wifi className="w-3 h-3" />
                      {preset.ping} ms
                    </span>
                    {isSelected && <Check className="w-3.5 h-3.5 text-[#DE6C14]" />}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Custom Endpoint Inputs */}
        <div className="pt-2 border-t border-[#222731] space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-[11.5px] font-semibold uppercase tracking-wider text-[#8E95A2]">
              Manual Proxy Endpoint
            </span>
            <div className="flex gap-1">
              {(['SOCKS5', 'SOCKS4', 'HTTP'] as const).map((proto) => (
                <button
                  key={proto}
                  type="button"
                  onClick={() =>
                    onChangeProxy({
                      ...proxy,
                      protocol: proto,
                      activePresetId: null,
                    })
                  }
                  className={`px-2 py-0.5 rounded text-[10.5px] font-mono font-medium transition-colors cursor-pointer ${
                    proxy.protocol === proto
                      ? 'bg-[#DE6C14] text-white'
                      : 'bg-[#0B0D10] text-[#8E95A2] border border-[#2C313D] hover:text-white'
                  }`}
                >
                  {proto}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div className="col-span-2">
              <label className="block text-[10.5px] text-[#636875] mb-1">Host / IPv4</label>
              <input
                type="text"
                value={proxy.host}
                onChange={(e) =>
                  onChangeProxy({
                    ...proxy,
                    host: e.target.value,
                    activePresetId: null,
                  })
                }
                placeholder="127.0.0.1"
                className="w-full bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[8px] px-3 py-1.5 text-[12.5px] font-mono text-[#EBECEF] outline-none transition-colors"
              />
            </div>
            <div>
              <label className="block text-[10.5px] text-[#636875] mb-1">Port</label>
              <input
                type="text"
                value={proxy.port}
                onChange={(e) =>
                  onChangeProxy({
                    ...proxy,
                    port: e.target.value,
                    activePresetId: null,
                  })
                }
                placeholder="1080"
                className="w-full bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[8px] px-3 py-1.5 text-[12.5px] font-mono text-[#EBECEF] outline-none transition-colors"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <button
          type="button"
          onClick={handlePingTest}
          className="horus-pill-dark px-3 py-1.5 rounded-[8px] text-[11.5px] font-semibold flex items-center gap-1.5 cursor-pointer"
        >
          <Activity className={`w-3.5 h-3.5 text-[#DE6C14] ${testingPing ? 'animate-spin' : ''}`} />
          <span>{testingPing ? 'Testing latency...' : 'Test Ping'}</span>
        </button>

        <div className="flex items-center gap-2">
          <span className="text-[11px] text-[#636875] flex items-center gap-1">
            <Shield className="w-3.5 h-3.5 text-[#10B981]" />
            IPv4 Leak Protection
          </span>
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
    </div>
  );
};
