import React, { useState } from 'react';
import { Compass, FolderGit2, Play, Plus, Trash2, X } from 'lucide-react';
import { WorldEntry } from '../../types/launcher';
import { uiSound } from '../../utils/sound';

interface SelectWorldScreenProps {
  worlds: WorldEntry[];
  onAddWorld: (world: WorldEntry) => void;
  onDeleteWorld: (id: string) => void;
  onLaunchWorld: (worldName: string) => void;
  onClose: () => void;
}

export const SelectWorldScreen: React.FC<SelectWorldScreenProps> = ({
  worlds,
  onAddWorld,
  onDeleteWorld,
  onLaunchWorld,
  onClose,
}) => {
  const [selectedId, setSelectedId] = useState<string>(worlds[0]?.id || '');
  const [showCreate, setShowCreate] = useState(false);
  const [worldName, setWorldName] = useState('');
  const [worldMode, setWorldMode] = useState<'Config Sandbox' | 'Creative' | 'Survival'>('Config Sandbox');

  const selectedWorld = worlds.find((w) => w.id === selectedId) || worlds[0];

  const handleCreateWorld = (e: React.FormEvent) => {
    e.preventDefault();
    const name = worldName.trim() || 'Fallen Test World';
    const created: WorldEntry = {
      id: `w-${Date.now()}`,
      name,
      folderName: name.replace(/\s+/g, '_'),
      lastPlayed: 'Just created',
      mode: worldMode,
      cheats: true,
      version: '1.21.4 (Horus)',
    };
    uiSound.playClick(true);
    onAddWorld(created);
    setSelectedId(created.id);
    setWorldName('');
    setShowCreate(false);
  };

  return (
    <div
      className="w-full max-w-[600px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Select Singleplayer World
            </span>
            <span className="px-2 py-0.5 text-[10px] font-mono rounded-md bg-[#1E2127] text-[#8E95A2] border border-[#2C313D]">
              SelectWorldScreen
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            Load a local sandbox to test ClickGUI, TargetHUD, and movement modules
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

      {/* Create World Form */}
      {showCreate && (
        <form
          onSubmit={handleCreateWorld}
          className="px-6 py-4 bg-[#0E1117] border-b border-[#222731] space-y-3"
        >
          <div className="flex items-center justify-between">
            <span className="text-[12px] font-semibold text-[#EBECEF]">Create New Sandbox World</span>
            <div className="flex gap-1">
              {(['Config Sandbox', 'Creative', 'Survival'] as const).map((m) => (
                <button
                  key={m}
                  type="button"
                  onClick={() => setWorldMode(m)}
                  className={`px-2 py-0.5 rounded text-[11px] font-medium transition-colors cursor-pointer ${
                    worldMode === m
                      ? 'bg-[#DE6C14] text-white'
                      : 'bg-[#181C24] text-[#8E95A2] hover:text-white'
                  }`}
                >
                  {m}
                </button>
              ))}
            </div>
          </div>

          <div className="flex gap-2">
            <input
              type="text"
              value={worldName}
              onChange={(e) => setWorldName(e.target.value)}
              placeholder="World name (e.g. KillAura & TargetHUD Lab)..."
              autoFocus
              className="flex-1 bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[8px] px-3 py-1.5 text-[12.5px] text-[#EBECEF] outline-none"
            />
            <button
              type="submit"
              className="horus-pill-accent px-4 py-1.5 rounded-[8px] text-[12px] font-semibold cursor-pointer"
            >
              Create
            </button>
          </div>
        </form>
      )}

      {/* Worlds List */}
      <div className="p-6 space-y-2 max-h-[300px] overflow-y-auto">
        {worlds.map((w) => {
          const isSelected = w.id === selectedId;
          return (
            <div
              key={w.id}
              onClick={() => {
                uiSound.playClick();
                setSelectedId(w.id);
              }}
              onDoubleClick={() => {
                uiSound.playClick(true);
                onLaunchWorld(w.name);
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
                  <Compass className="w-4 h-4 text-[#DE6C14]" />
                </div>
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-[14px] font-semibold text-[#EBECEF] truncate">
                      {w.name}
                    </span>
                    <span className="px-1.5 py-0.2 rounded text-[10px] font-mono bg-[#DE6C14]/15 text-[#F28028] border border-[#DE6C14]/30">
                      {w.mode}
                    </span>
                  </div>
                  <div className="text-[11.5px] text-[#636875] flex items-center gap-2 mt-0.5 font-mono">
                    <FolderGit2 className="w-3 h-3" />
                    <span className="truncate">{w.folderName}</span>
                    <span>•</span>
                    <span>{w.lastPlayed}</span>
                  </div>
                </div>
              </div>

              {worlds.length > 1 && (
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    uiSound.playClick();
                    onDeleteWorld(w.id);
                  }}
                  className="p-2 rounded-[8px] text-[#636875] hover:text-[#F43F5E] hover:bg-[#F43F5E]/10 transition-colors cursor-pointer"
                  title="Delete world"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
            </div>
          );
        })}
      </div>

      {/* Footer */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            setShowCreate((v) => !v);
          }}
          className="horus-pill-dark px-3.5 py-1.5 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
        >
          <Plus className="w-3.5 h-3.5 text-[#DE6C14]" />
          <span>New World</span>
        </button>

        <button
          type="button"
          disabled={!selectedWorld}
          onClick={() => {
            if (!selectedWorld) return;
            uiSound.playClick(true);
            onLaunchWorld(selectedWorld.name);
          }}
          className="horus-pill-accent px-4 py-1.5 rounded-[8px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
        >
          <Play className="w-3.5 h-3.5 fill-current" />
          <span>Play Selected World</span>
        </button>
      </div>
    </div>
  );
};
