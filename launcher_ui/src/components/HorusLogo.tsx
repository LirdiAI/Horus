import React from 'react';

interface HorusLogoProps {
  size?: number;
  className?: string;
}

export const HorusLogo: React.FC<HorusLogoProps> = ({ size = 28, className = '' }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 32 32"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`select-none ${className}`}
    >
      {/* Minimalist Fallen / Horus Winged Eye / Geometric Crest */}
      <path
        d="M16 3L28 9.8V22.2L16 29L4 22.2V9.8L16 3Z"
        stroke="#FFFFFF"
        strokeWidth="2.1"
        strokeLinejoin="round"
        strokeOpacity="0.92"
      />
      <path
        d="M8.5 14.2L16 9.5L23.5 14.2L16 18.9L8.5 14.2Z"
        fill="#FFFFFF"
      />
      <path
        d="M11.2 19.6L16 22.6L20.8 19.6"
        stroke="#FFFFFF"
        strokeWidth="2.1"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
};
