import React, { useState } from 'react';
import {
  Globe,
  Play,
  Plus,
  RefreshCw,
  Server,
  Shield,
  Signal,
  X,
} from 'lucide-react';
import { ProxyConfig, ServerEntry } from '../../types/launcher';
import { uiSound } from '../../utils/sound';

interface MultiplayerScreenProps {
  servers: ServerEntry[];
  proxy: ProxyConfig;
  onAddServer: (server: ServerEntry) => void;
  onJoinServer: (targetName: string, address: string) => void;
  onOpenProxies: () => void;
  onClose: () => void;
}

export const MultiplayerScreen: React.FC<MultiplayerScreenProps> = ({
  servers,
  proxy,
  onAddServer,
  onJoinServer,
  onOpenProxies,
  onClose,
}) => {
  const [selectedId, setSelectedId] = useState<string>(servers[0]?.id || '');
  const [refreshing, setRefreshing] = useState(false);
  const [showAdd, setShowAdd] = useState(false);
  const [newName, setNewName] = useState('');
  const [newIp, setNewIp] = useState('');

  const selectedServer = servers.find((s) => s.id === selectedId) || servers[0];

  const handleRefresh = () => {
    uiSound.playClick();
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 420);
  };

  const handleCreateServer = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newIp.trim()) return;
    const entry: ServerEntry = {
      id: `srv-${Date.now()}`,
      name: newName.trim() || newIp.trim(),
      address: newIp.trim(),
      motd: '§7Custom Server • Added via Fallen MultiplayerScreen',
      players: Math.floor(20 + Math.random() * 450),
      maxPlayers: 1000,
      ping: Math.floor(15 + Math.random() * 25),
      version: '1.21.4',
      tag: 'HvH',
    };
    uiSound.playClick(true);
    onAddServer(entry);
    setSelectedId(entry.id);
    setNewName('');
    setNewIp('');
    setShowAdd(false);
  };

  // Strip Minecraft § formatting codes for clean rendering
  const cleanMotd = (raw: string) => raw.replace(/§[0-9a-fk-or]/gi, '');

  return (
    <div
      className="w-full max-w-[650px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Multiplayer Servers
            </span>
            <span className="px-2 py-0.5 text-[10px] font-mono rounded-md bg-[#1E2127] text-[#8E95A2] border border-[#2C313D]">
              ViaVersion 1.16.5 – 1.21.4
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            Select a server or double-click to inject Fallen into session
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => {
              uiSound.playClick();
              onOpenProxies();
            }}
            className={`px-2.5 py-1.5 rounded-[8px] text-[11px] font-mono border flex items-center gap-1.5 transition-colors cursor-pointer ${
              proxy.enabled
                ? 'bg-[#10B981]/15 border-[#10B981]/40 text-[#10B981]'
                : 'bg-[#0B0D10] border-[#383E4C] hover:border-white text-[#8E95A2]'
            }`}
          >
            <Globe className="w-3.5 h-3.5" />
            <span>{proxy.enabled ? `Proxy: ${proxy.host}` : 'Proxy: OFF'}</span>
          </button>

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
      </div>

      {/* Optional Add Server Form */}
      {showAdd && (
        <form
          onSubmit={handleCreateServer}
          className="px-6 py-4 bg-[#0E1117] border-b border-[#222731] grid grid-cols-1 sm:grid-cols-12 gap-2.5"
        >
          <input
            type="text"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            placeholder="Server Name"
            className="sm:col-span-4 bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[8px] px-3 py-1.5 text-[12.5px] text-[#EBECEF] outline-none"
          />
          <input
            type="text"
            value={newIp}
            onChange={(e) => setNewIp(e.target.value)}
            placeholder="Server Address (e.g. play.server.net)"
            autoFocus
            className="sm:col-span-5 bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[8px] px-3 py-1.5 text-[12.5px] font-mono text-[#EBECEF] outline-none"
          />
          <button
            type="submit"
            className="sm:col-span-3 horus-pill-accent rounded-[8px] py-1.5 text-[12px] font-semibold cursor-pointer"
          >
            Save Server
          </button>
        </form>
      )}

      {/* Server List */}
      <div className="p-6 space-y-2 max-h-[320px] overflow-y-auto">
        {servers.map((srv) => {
          const isSelected = srv.id === selectedId;
          return (
            <div
              key={srv.id}
              onClick={() => {
                uiSound.playClick();
                setSelectedId(srv.id);
              }}
              onDoubleClick={() => {
                uiSound.playClick(true);
                onJoinServer(srv.name, srv.address);
              }}
              onMouseEnter={() => uiSound.playHover()}
              className={`flex items-center justify-between px-4 py-3 rounded-[11px] border transition-all duration-200 cursor-pointer ${
                isSelected
                  ? 'bg-[#1E2127] border-white'
                  : 'bg-[#0E1116] border-[#2C313D] hover:bg-[#181B22] hover:border-white'
              }`}
            >
              <div className="flex items-center gap-3.5 min-w-0">
                <div className="w-10 h-10 rounded-[9px] bg-[#14171D] border border-[#2C313D] flex items-center justify-center shrink-0">
                  <Server className="w-4 h-4 text-[#DE6C14]" />
                </div>
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-[14px] font-semibold text-[#EBECEF] truncate">
                      {srv.name}
                    </span>
                    <span className="px-1.5 py-0.2 rounded text-[10px] font-mono bg-[#181C24] text-[#8E95A2] border border-[#2C313D]">
                      {srv.tag}
                    </span>
                    <span className="text-[11px] font-mono text-[#636875] hidden sm:inline">
                      {srv.address}
                    </span>
                  </div>
                  <div className="text-[12px] text-[#8E95A2] truncate mt-0.5">
                    {cleanMotd(srv.motd)}
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-4 shrink-0 ml-3">
                <div className="text-right">
                  <div className="text-[12px] font-mono font-semibold text-[#EBECEF]">
                    {srv.players.toLocaleString()}
                    <span className="text-[#636875]">/{srv.maxPlayers.toLocaleString()}</span>
                  </div>
                  <div className="text-[10.5px] font-mono text-[#10B981] flex items-center justify-end gap-1">
                    <Signal className="w-3 h-3" />
                    <span>{refreshing ? '...' : `${srv.ping} ms`}</span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Footer Actions */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handleRefresh}
            className="horus-pill-dark px-3 py-1.5 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin text-[#DE6C14]' : ''}`} />
            <span>Refresh</span>
          </button>
          <button
            type="button"
            onClick={() => {
              uiSound.playClick();
              setShowAdd((v) => !v);
            }}
            className="horus-pill-dark px-3 py-1.5 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5 text-[#DE6C14]" />
            <span>Add Server</span>
          </button>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-[11px] text-[#636875] hidden sm:flex items-center gap-1 mr-1">
            <Shield className="w-3.5 h-3.5 text-[#DE6C14]" />
            Spoof &amp; Bypass Ready
          </span>
          <button
            type="button"
            disabled={!selectedServer}
            onClick={() => {
              if (!selectedServer) return;
              uiSound.playClick(true);
              onJoinServer(selectedServer.name, selectedServer.address);
            }}
            className="horus-pill-accent px-4 py-1.5 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
          >
            <Play className="w-3.5 h-3.5 fill-current" />
            <span>Connect to Server</span>
          </button>
        </div>
      </div>
    </div>
  );
};
