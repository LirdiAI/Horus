import React, { useState } from 'react';
import {
  Check,
  Dices,
  Plus,
  Search,
  ShieldCheck,
  Star,
  Trash2,
  UserCheck,
  X,
} from 'lucide-react';
import { AccountProfile } from '../../types/launcher';
import { RANDOM_ALT_PREFIXES, SKIN_PRESETS } from '../../data/mockData';
import { PixelAvatar } from '../PixelAvatar';
import { uiSound } from '../../utils/sound';

interface AccountManagerPanelProps {
  accounts: AccountProfile[];
  activeAccountId: string;
  onSelectAccount: (id: string) => void;
  onAddAccount: (account: AccountProfile) => void;
  onDeleteAccount: (id: string) => void;
  onToggleFavorite: (id: string) => void;
  onClose: () => void;
}

export const AccountManagerPanel: React.FC<AccountManagerPanelProps> = ({
  accounts,
  activeAccountId,
  onSelectAccount,
  onAddAccount,
  onDeleteAccount,
  onToggleFavorite,
  onClose,
}) => {
  const [search, setSearch] = useState('');
  const [newUsername, setNewUsername] = useState('');
  const [newType, setNewType] = useState<'Dreamix' | 'Microsoft' | 'Offline'>('Dreamix');
  const [showAddForm, setShowAddForm] = useState(false);

  const filteredAccounts = accounts
    .filter((acc) => acc.username.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => {
      if (a.favorite && !b.favorite) return -1;
      if (!a.favorite && b.favorite) return 1;
      return 0;
    });

  const handleCreateAccount = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = newUsername.trim();
    if (!trimmed) return;
    const skin = SKIN_PRESETS[Math.floor(Math.random() * SKIN_PRESETS.length)];
    const randomUid = String(Math.floor(1000 + Math.random() * 8999));
    const created: AccountProfile = {
      id: `acc-${Date.now()}`,
      username: trimmed.slice(0, 18),
      uid: randomUid,
      type: newType,
      role: newType === 'Dreamix' ? 'Lifetime' : 'Free',
      lastUsed: 'Just added',
      favorite: false,
      skin,
    };
    uiSound.playClick(true);
    onAddAccount(created);
    onSelectAccount(created.id);
    setNewUsername('');
    setShowAddForm(false);
  };

  const handleGenerateRandomAlt = () => {
    uiSound.playClick(true);
    const prefix = RANDOM_ALT_PREFIXES[Math.floor(Math.random() * RANDOM_ALT_PREFIXES.length)];
    const suffix = Math.floor(10 + Math.random() * 989);
    const skin = SKIN_PRESETS[Math.floor(Math.random() * SKIN_PRESETS.length)];
    const created: AccountProfile = {
      id: `acc-${Date.now()}`,
      username: `${prefix}_${suffix}`,
      uid: String(Math.floor(1000 + Math.random() * 8999)),
      type: 'Offline',
      role: 'Free',
      lastUsed: 'Generated alt',
      favorite: false,
      skin,
    };
    onAddAccount(created);
    onSelectAccount(created.id);
  };

  const activeAccount = accounts.find((a) => a.id === activeAccountId) || accounts[0];

  return (
    <div
      className="w-full max-w-[520px] rounded-[16px] bg-[#14171D] border border-[#383E4C] shadow-[0_24px_80px_rgba(0,0,0,0.85)] overflow-hidden"
      onClick={(e) => e.stopPropagation()}
    >
      {/* Top Panel Header */}
      <div className="px-6 pt-5 pb-4 border-b border-[#222731] flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[17px] font-semibold tracking-[-0.015em] text-[#EBECEF]">
              Account Manager
            </span>
            <span className="px-2 py-0.5 text-[10px] font-mono font-medium rounded-md bg-[#1E2127] text-[#8E95A2] border border-[#2C313D]">
              ProfileAPI • {accounts.length} saved
            </span>
          </div>
          <p className="text-[12px] text-[#636875] mt-0.5">
            Swap active session profile or generate a new offline/token alt
          </p>
        </div>

        <button
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="w-8 h-8 rounded-[9px] bg-[#0B0D10] border border-[#383E4C] hover:border-white text-[#8E95A2] hover:text-white flex items-center justify-center transition-all duration-200 cursor-pointer"
          title="Close (ESC)"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Active Profile Summary Strip */}
      {activeAccount && (
        <div className="px-6 py-3 bg-[#0B0D10]/70 border-b border-[#222731] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="relative">
              <PixelAvatar skin={activeAccount.skin} size={32} />
              <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 rounded-full bg-[#10B981] ring-2 ring-[#0B0D10]" />
            </div>
            <div>
              <div className="text-[11px] font-medium text-[#636875] flex items-center gap-1.5">
                <span>Currently logged in as</span>
                <span className="text-[#DE6C14] font-mono">UID #{activeAccount.uid}</span>
              </div>
              <div className="text-[14px] font-semibold text-[#EBECEF] flex items-center gap-2">
                <span>{activeAccount.username}</span>
                <span className="text-[10px] px-1.5 py-0.2 rounded bg-[#DE6C14]/15 text-[#F28028] border border-[#DE6C14]/30 font-medium">
                  {activeAccount.type}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handleGenerateRandomAlt}
              onMouseEnter={() => uiSound.playHover()}
              className="horus-pill-dark px-3 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
              title="Generate random HVH alt"
            >
              <Dices className="w-3.5 h-3.5 text-[#DE6C14]" />
              <span>Random Alt</span>
            </button>
            <button
              type="button"
              onClick={() => {
                uiSound.playClick();
                setShowAddForm((v) => !v);
              }}
              onMouseEnter={() => uiSound.playHover()}
              className="horus-pill-accent px-3 py-1.5 rounded-[9px] text-[12px] font-semibold flex items-center gap-1.5 cursor-pointer"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Account</span>
            </button>
          </div>
        </div>
      )}

      {/* Expandable Add Account Form */}
      {showAddForm && (
        <form
          onSubmit={handleCreateAccount}
          className="px-6 py-4 bg-[#101318] border-b border-[#252A35] space-y-3"
        >
          <div className="flex items-center justify-between">
            <span className="text-[12px] font-semibold text-[#EBECEF]">
              Add New Profile to AccountManagerPanel
            </span>
            <div className="flex items-center gap-1">
              {(['Dreamix', 'Microsoft', 'Offline'] as const).map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => setNewType(t)}
                  className={`px-2 py-0.5 rounded text-[11px] font-medium transition-colors cursor-pointer ${
                    newType === t
                      ? 'bg-[#DE6C14] text-white'
                      : 'bg-[#181C24] text-[#8E95A2] hover:text-white'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>

          <div className="flex gap-2">
            <input
              type="text"
              value={newUsername}
              onChange={(e) => setNewUsername(e.target.value)}
              placeholder="Enter nickname (e.g. yougame_alt)..."
              maxLength={18}
              autoFocus
              className="flex-1 bg-[#0B0D10] border border-[#383E4C] focus:border-white rounded-[9px] px-3.5 py-2 text-[13px] text-[#EBECEF] placeholder-[#636875] outline-none transition-colors font-medium"
            />
            <button
              type="submit"
              className="horus-pill-accent px-4 py-2 rounded-[9px] text-[12px] font-semibold cursor-pointer shrink-0"
            >
              Login & Save
            </button>
          </div>
        </form>
      )}

      {/* Search Filter Bar */}
      <div className="px-6 pt-4 pb-2">
        <div className="relative">
          <Search className="w-4 h-4 text-[#636875] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search saved accounts..."
            className="w-full bg-[#0B0D10] border border-[#2C313D] focus:border-[#FFFFFF] rounded-[10px] pl-9 pr-3.5 py-2 text-[12.5px] text-[#EBECEF] placeholder-[#636875] outline-none transition-colors"
          />
        </div>
      </div>

      {/* Scrollable Accounts List */}
      <div className="px-6 py-2 max-h-[270px] overflow-y-auto space-y-2">
        {filteredAccounts.length === 0 ? (
          <div className="py-8 text-center text-[13px] text-[#636875]">
            No accounts matching &ldquo;{search}&rdquo;
          </div>
        ) : (
          filteredAccounts.map((acc) => {
            const isSelected = acc.id === activeAccountId;
            return (
              <div
                key={acc.id}
                onClick={() => {
                  uiSound.playClick(true);
                  onSelectAccount(acc.id);
                }}
                onMouseEnter={() => uiSound.playHover()}
                className={`group flex items-center justify-between px-3.5 py-2.5 rounded-[11px] border transition-all duration-200 cursor-pointer ${
                  isSelected
                    ? 'bg-[#1E2127] border-[#FFFFFF] shadow-[0_0_0_1px_rgba(255,255,255,0.12)]'
                    : 'bg-[#0E1116] border-[#2B303C] hover:bg-[#191C22] hover:border-[#FFFFFF]'
                }`}
              >
                <div className="flex items-center gap-3 min-w-0">
                  <PixelAvatar skin={acc.skin} size={30} />
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-[13.5px] font-semibold text-[#EBECEF] truncate">
                        {acc.username}
                      </span>
                      {isSelected && (
                        <span className="inline-flex items-center gap-1 px-1.5 py-0.2 rounded text-[10px] font-semibold bg-[#DE6C14] text-white">
                          <Check className="w-2.5 h-2.5" /> Active
                        </span>
                      )}
                      {acc.type === 'Dreamix' && (
                        <span
                          className="inline-flex items-center gap-0.5 text-[10px] text-[#8E95A2] font-mono"
                          title="Dreamix Protection Verified"
                        >
                          <ShieldCheck className="w-3 h-3 text-[#DE6C14]" />
                          UID:{acc.uid}
                        </span>
                      )}
                    </div>
                    <div className="text-[11px] text-[#636875] flex items-center gap-2 mt-0.5">
                      <span>{acc.type} Account</span>
                      <span>•</span>
                      <span>{acc.lastUsed}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      uiSound.playClick();
                      onToggleFavorite(acc.id);
                    }}
                    className={`p-1.5 rounded-[7px] transition-colors cursor-pointer ${
                      acc.favorite
                        ? 'text-[#DE6C14] bg-[#DE6C14]/10'
                        : 'text-[#636875] hover:text-[#EBECEF] hover:bg-[#252A34]'
                    }`}
                    title="Pin account"
                  >
                    <Star className="w-3.5 h-3.5" fill={acc.favorite ? 'currentColor' : 'none'} />
                  </button>

                  {accounts.length > 1 && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        uiSound.playClick();
                        onDeleteAccount(acc.id);
                      }}
                      className="p-1.5 rounded-[7px] text-[#636875] hover:text-[#F43F5E] hover:bg-[#F43F5E]/10 transition-colors cursor-pointer"
                      title="Remove account"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Bottom Footer Bar */}
      <div className="px-6 py-3.5 bg-[#0E1015] border-t border-[#222731] flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-[11px] text-[#636875]">
          <UserCheck className="w-3.5 h-3.5 text-[#8E95A2]" />
          <span>Click any account to hot-swap session</span>
        </div>
        <button
          type="button"
          onClick={() => {
            uiSound.playClick();
            onClose();
          }}
          className="horus-pill-dark px-4 py-1.5 rounded-[8px] text-[12px] font-semibold cursor-pointer"
        >
          Done
        </button>
      </div>
    </div>
  );
};
