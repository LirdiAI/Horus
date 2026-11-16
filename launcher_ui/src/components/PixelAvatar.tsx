import React from 'react';
import { SkinPalette } from '../types/launcher';
import { DEFAULT_STEVE_SKIN } from '../data/mockData';

interface PixelAvatarProps {
  skin?: SkinPalette;
  size?: number;
  className?: string;
}

/**
 * Exact 16x16 coordinate reproduction from MainMenuScreen.java drawChrome():
 * Render2D.processData51(badgeX, badgeY, 16, 16, 1.5, 0xFFB58463);
 * Render2D.processData51(badgeX, badgeY, 16, 4.5, 1.0, 0xFF3B2719);
 * Render2D.processData51(badgeX + 2.2, badgeY + 7.2, 3.6, 2.0, 0.0, 0xFFFFFFFF);
 * Render2D.processData51(badgeX + 10.2, badgeY + 7.2, 3.6, 2.0, 0.0, 0xFFFFFFFF);
 * Render2D.processData51(badgeX + 4.0, badgeY + 7.2, 1.8, 2.0, 0.0, 0xFF4F3E8C);
 * Render2D.processData51(badgeX + 10.2, badgeY + 7.2, 1.8, 2.0, 0.0, 0xFF4F3E8C);
 * Render2D.processData51(badgeX + 6.0, badgeY + 11.2, 4.0, 2.2, 0.0, 0xFF734532);
 */
export const PixelAvatar: React.FC<PixelAvatarProps> = ({
  skin = DEFAULT_STEVE_SKIN,
  size = 28,
  className = '',
}) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 16 16"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`shrink-0 ${className}`}
      style={{ shapeRendering: 'geometricPrecision' }}
    >
      {/* Head Base */}
      <rect x="0" y="0" width="16" height="16" rx="1.5" fill={skin.skin} />
      {/* Hair Top Layer */}
      <rect x="0" y="0" width="16" height="4.5" rx="1.0" fill={skin.hair} />
      {/* Left Sclera */}
      <rect x="2.2" y="7.2" width="3.6" height="2.0" fill={skin.sclera} />
      {/* Right Sclera */}
      <rect x="10.2" y="7.2" width="3.6" height="2.0" fill={skin.sclera} />
      {/* Left Pupil */}
      <rect x="4.0" y="7.2" width="1.8" height="2.0" fill={skin.pupil} />
      {/* Right Pupil */}
      <rect x="10.2" y="7.2" width="1.8" height="2.0" fill={skin.pupil} />
      {/* Mouth / Beard */}
      <rect x="6.0" y="11.2" width="4.0" height="2.2" fill={skin.mouth} />
    </svg>
  );
};
