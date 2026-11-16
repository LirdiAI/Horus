import React, { useRef, useState } from 'react';
import { Check, RotateCw, Sparkles, Shield, X } from 'lucide-react';
import { AccountProfile, CosmeticsState } from '../../types/launcher';
import { COSMETIC_AURAS, COSMETIC_CAPES } from '../../data/mockData';
import { PixelAvatar } from '../PixelAvatar';
import { HorusLogo } from '../HorusLogo';
import { uiSound } from '../../utils/sound';

interface CosmeticsSelectionScreenProps {
  activeAccount: AccountProfile;
  cosmetics: CosmeticsState;
  onChangeCosmetics: (next: CosmeticsState) => void;
  onClose: () => void;
}

export const CosmeticsSelectionScreen: React.FC<CosmeticsSelectionScreenProps> = ({
  activeAccount,
  cosmetics,
  onChangeCosmetics,
  onClose,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const lastXRef = useRef<number>(0);

  const selectedCape =
    COSMETIC_CAPES.find((c) => c.id === cosmetics.selectedCapeId) || COSMETIC_CAPES[0];
  const selectedAura =
    COSMETIC_AURAS.find((a) => a.id === cosmetics.selectedAuraId) || COSMETIC_AURAS[0];

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    lastXRef.current = e.clientX;
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return;
    const deltaX = e.clientX - lastXRef.current;
    lastXRef.current = e.clientX;
    onChangeCosmetics({
      ...cosmetics,
      rotationY: (cosmetics.rotationY + deltaX * 1.1) % 360,
    });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Determine whether back of model (cape side) is facing viewer
  const normalizedAngle = ((cosmetics.rotationY % 360) + 360) % 360;
  const showingBack = normalizedAngle > 90 && normalizedAngle < 270;

  return (
    <div
      className="w-full max-w-[660px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      {/* Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Cosmetics &amp; Capes
            </span>
            <span className="px-2 py-0.5 text-[10px] font-mono rounded-md bg-[#1E2127] text-[#DE6C14] border border-[#DE6C14]/30">
              CosmeticsSelectionScreen
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            Customize client cape, particle aura, and wings for {activeAccount.username}
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

      {/* Main Content Split: Left Interactive Model Preview, Right Selectors */}
      <div className="grid grid-cols-1 md:grid-cols-12">
        {/* Interactive Player Doll Preview (mouseDragged support) */}
        <div
          onMouseDown={handleMouseDown}
          className="md:col-span-5 bg-[#0B0D10] border-b md:border-b-0 md:border-r border-[#222731] p-5 flex flex-col items-center justify-between relative overflow-hidden cursor-grab active:cursor-grabbing select-none min-h-[330px]"
        >
          {/* Top Tag */}
          <div className="w-full flex items-center justify-between z-10">
            <span className="text-[11px] font-mono text-[#636875]">
              YAW: {Math.round(normalizedAngle)}°
            </span>
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                uiSound.playClick();
                onChangeCosmetics({
                  ...cosmetics,
                  rotationY: showingBack ? 0 : 180,
                });
              }}
              className="px-2 py-1 rounded bg-[#14171D] border border-[#383E4C] hover:border-white text-[11px] text-[#8E95A2] hover:text-white flex items-center gap-1 transition-colors cursor-pointer"
            >
              <RotateCw className="w-3 h-3" />
              <span>{showingBack ? 'View Front' : 'View Cape'}</span>
            </button>
          </div>

          {/* Aura Ring Glow */}
          {selectedAura.id !== 'aura-none' && (
            <div
              className="absolute w-44 h-44 rounded-full blur-2xl opacity-25 pointer-events-none transition-all duration-500"
              style={{
                background: `radial-gradient(circle, ${selectedAura.color} 0%, transparent 70%)`,
              }}
            />
          )}

          {/* 2.5D Minecraft Player Rig + Cape */}
          <div className="my-4 relative flex flex-col items-center justify-center">
            {/* Orbital Aura Ring */}
            {selectedAura.id !== 'aura-none' && (
              <div
                className="absolute -top-3 w-20 h-5 rounded-full border-2 animate-pulse pointer-events-none"
                style={{
                  borderColor: selectedAura.color,
                  boxShadow: `0 0 14px ${selectedAura.color}`,
                }}
              />
            )}

            {/* Nametag above player */}
            <div className="mb-2.5 px-2.5 py-0.5 rounded bg-[#14171D]/90 border border-[#2C313D] text-[11px] font-mono text-[#EBECEF] flex items-center gap-1.5 shadow">
              <span className="text-[#DE6C14] font-bold">[Fallen]</span>
              <span>{activeAccount.username}</span>
            </div>

            {/* Rotatable Rig Container */}
            <div
              className="relative flex flex-col items-center transition-transform duration-75"
              style={{
                transform: `perspective(500px) rotateY(${
                  Math.sin((normalizedAngle * Math.PI) / 180) * 22
                }deg)`,
              }}
            >
              {/* Head */}
              <div className="relative z-20 shadow-lg">
                {showingBack ? (
                  <div
                    className="w-[44px] h-[44px] rounded-[4px] border border-white/10"
                    style={{ backgroundColor: activeAccount.skin.hair }}
                  />
                ) : (
                  <PixelAvatar skin={activeAccount.skin} size={44} />
                )}
              </div>

              {/* Torso + Arms + Cape */}
              <div className="relative flex items-start justify-center mt-0.5">
                {/* Left Arm */}
                <div
                  className="w-[16px] h-[54px] rounded-l-[3px] mr-[2px] border border-white/10"
                  style={{ backgroundColor: '#1E222B' }}
                >
                  <div
                    className="w-full h-[14px] mt-[40px] rounded-b-[2px]"
                    style={{ backgroundColor: activeAccount.skin.skin }}
                  />
                </div>

                {/* Center Body / Torso */}
                <div className="w-[40px] h-[56px] rounded-[2px] bg-[#181B22] border border-[#2F3542] relative flex items-center justify-center overflow-hidden">
                  {!showingBack && (
                    <div className="opacity-45">
                      <HorusLogo size={20} />
                    </div>
                  )}

                  {/* Cape Rendered on Back or peeking behind torso */}
                  {selectedCape.pattern !== 'none' && (
                    <div
                      className={`absolute inset-x-0.5 top-0.5 bottom-0.5 rounded-[3px] border flex flex-col items-center justify-center transition-all duration-300 ${
                        showingBack ? 'z-30 opacity-100 scale-100' : 'z-0 opacity-20'
                      }`}
                      style={{
                        background: `linear-gradient(165deg, ${selectedCape.primaryColor} 0%, ${selectedCape.secondaryColor} 100%)`,
                        borderColor: selectedCape.accentColor,
                        boxShadow: showingBack
                          ? `0 6px 20px -4px ${selectedCape.accentColor}55`
                          : 'none',
                      }}
                    >
                      <div
                        className="w-6 h-6 rounded-full flex items-center justify-center"
                        style={{
                          backgroundColor: `${selectedCape.accentColor}22`,
                          border: `1px solid ${selectedCape.accentColor}`,
                        }}
                      >
                        <HorusLogo size={14} />
                      </div>
                      <span
                        className="text-[7px] font-mono uppercase tracking-widest mt-1 font-bold"
                        style={{ color: selectedCape.accentColor }}
                      >
                        {selectedCape.pattern}
                      </span>
                    </div>
                  )}
                </div>

                {/* Right Arm */}
                <div
                  className="w-[16px] h-[54px] rounded-r-[3px] ml-[2px] border border-white/10"
                  style={{ backgroundColor: '#1E222B' }}
                >
                  <div
                    className="w-full h-[14px] mt-[40px] rounded-b-[2px]"
                    style={{ backgroundColor: activeAccount.skin.skin }}
                  />
                </div>
              </div>

              {/* Legs */}
              <div className="flex gap-[2px] mt-[1px]">
                <div className="w-[19px] h-[50px] bg-[#12151B] border border-[#282D38] rounded-b-[3px]" />
                <div className="w-[19px] h-[50px] bg-[#12151B] border border-[#282D38] rounded-b-[3px]" />
              </div>
            </div>
          </div>

          {/* Drag Hint */}
          <div className="text-[11px] text-[#636875] text-center z-10">
            Drag horizontally to rotate model • Click button to flip
          </div>
        </div>

        {/* Right Side: Cape & Aura Selection */}
        <div className="md:col-span-7 p-5 space-y-5 max-h-[380px] overflow-y-auto">
          {/* Capes Section */}
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-[12px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5">
                <Shield className="w-3.5 h-3.5 text-[#DE6C14]" />
                <span>Client Capes</span>
              </span>
              <span className="text-[11px] text-[#636875]">{selectedCape.name}</span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {COSMETIC_CAPES.map((cape) => {
                const active = cape.id === cosmetics.selectedCapeId;
                return (
                  <button
                    key={cape.id}
                    type="button"
                    onClick={() => {
                      uiSound.playClick(true);
                      onChangeCosmetics({
                        ...cosmetics,
                        selectedCapeId: cape.id,
                        rotationY: cape.pattern !== 'none' ? 180 : cosmetics.rotationY,
                      });
                    }}
                    onMouseEnter={() => uiSound.playHover()}
                    className={`flex items-center justify-between p-2.5 rounded-[10px] border text-left transition-all duration-200 cursor-pointer ${
                      active
                        ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                        : 'bg-[#0E1116] border-[#2C313D] hover:bg-[#191C22] hover:border-white text-[#8E95A2]'
                    }`}
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <span
                        className="w-4 h-5 rounded-[3px] shrink-0 border"
                        style={{
                          backgroundColor: cape.primaryColor,
                          borderColor: cape.accentColor,
                        }}
                      />
                      <div className="min-w-0">
                        <div className="text-[12.5px] font-semibold text-[#EBECEF] truncate">
                          {cape.name}
                        </div>
                        <div className="text-[10px] text-[#636875]">{cape.tier}</div>
                      </div>
                    </div>
                    {active && <Check className="w-3.5 h-3.5 text-[#DE6C14] shrink-0" />}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Auras Section */}
          <div>
            <div className="flex items-center justify-between mb-2.5">
              <span className="text-[12px] font-semibold uppercase tracking-wider text-[#8E95A2] flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-[#DE6C14]" />
                <span>Particle Halo / Aura</span>
              </span>
              <span className="text-[11px] text-[#636875]">{selectedAura.name}</span>
            </div>

            <div className="space-y-1.5">
              {COSMETIC_AURAS.map((aura) => {
                const active = aura.id === cosmetics.selectedAuraId;
                return (
                  <button
                    key={aura.id}
                    type="button"
                    onClick={() => {
                      uiSound.playClick(true);
                      onChangeCosmetics({
                        ...cosmetics,
                        selectedAuraId: aura.id,
                      });
                    }}
                    onMouseEnter={() => uiSound.playHover()}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-[10px] border text-left transition-all duration-200 cursor-pointer ${
                      active
                        ? 'bg-[#1E2127] border-white text-[#EBECEF]'
                        : 'bg-[#0E1116] border-[#2C313D] hover:bg-[#191C22] hover:border-white text-[#8E95A2]'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <span
                        className="w-3 h-3 rounded-full shrink-0"
                        style={{ backgroundColor: aura.color }}
                      />
                      <div>
                        <div className="text-[12.5px] font-semibold text-[#EBECEF]">
                          {aura.name}
                        </div>
                        <div className="text-[11px] text-[#636875]">{aura.description}</div>
                      </div>
                    </div>
                    {active && <Check className="w-3.5 h-3.5 text-[#DE6C14]" />}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <span className="text-[11px] text-[#636875]">
          Synced with Horus Cosmetics Server • Visible to all Fallen users
        </span>
        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="horus-pill-accent px-4 py-1.5 rounded-[8px] text-[12px] font-semibold cursor-pointer"
        >
          Save &amp; Apply
        </button>
      </div>
    </div>
  );
};
