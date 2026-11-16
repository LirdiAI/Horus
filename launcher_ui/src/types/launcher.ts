export type OverlayType =
  | 'none'
  | 'accounts'
  | 'cosmetics'
  | 'proxies'
  | 'options'
  | 'singleplayer'
  | 'multiplayer'
  | 'launcher'
  | 'ingame';

export interface SkinPalette {
  skin: string;
  hair: string;
  sclera: string;
  pupil: string;
  mouth: string;
}

export interface AccountProfile {
  id: string;
  username: string;
  uid: string;
  type: 'Dreamix' | 'Microsoft' | 'Offline';
  role: 'Lifetime' | 'Beta' | 'Developer' | 'Free';
  lastUsed: string;
  favorite?: boolean;
  skin: SkinPalette;
}

export interface ProxyPreset {
  id: string;
  label: string;
  country: string;
  host: string;
  port: number;
  protocol: 'SOCKS5' | 'SOCKS4' | 'HTTP';
  ping: number;
}

export interface ProxyConfig {
  enabled: boolean;
  protocol: 'SOCKS5' | 'SOCKS4' | 'HTTP';
  host: string;
  port: string;
  username: string;
  password: string;
  activePresetId: string | null;
  lastPing: number | null;
}

export interface CosmeticCape {
  id: string;
  name: string;
  tier: 'Official' | 'Exclusive' | 'Partner' | 'Custom';
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  pattern: 'horus' | 'fallen' | 'dreamix' | 'yougame' | 'obsidian' | 'none';
}

export interface CosmeticAura {
  id: string;
  name: string;
  color: string;
  description: string;
}

export interface CosmeticsState {
  selectedCapeId: string;
  selectedAuraId: string;
  showWings: boolean;
  rotationY: number;
  rotationX: number;
}

export interface ServerEntry {
  id: string;
  name: string;
  address: string;
  motd: string;
  players: number;
  maxPlayers: number;
  ping: number;
  version: string;
  tag: 'Anarchy' | 'HvH' | 'AntiCheat Test' | 'MiniGames';
}

export interface WorldEntry {
  id: string;
  name: string;
  folderName: string;
  lastPlayed: string;
  mode: 'Creative' | 'Survival' | 'Config Sandbox';
  cheats: boolean;
  version: string;
}

export interface AccentPreset {
  id: string;
  name: string;
  base: string;
  hover: string;
}

export interface ClientSettings {
  guiScale: number; // 0.85, 1.0, 1.15
  accentId: string;
  soundEnabled: boolean;
  ambientGlow: boolean;
  showWindowChrome: boolean;
  showJavaDebugHud: boolean;
  ramAllocationMb: number;
  selectedBranch: 'Fallen 1.21.4 (Stable)' | 'Horus 1.21.4-dev (Nightly)' | 'Fallen 1.16.5 (Legacy)';
  language: 'en' | 'ru';
}
