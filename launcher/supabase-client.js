// ==============================================================================
//   LUMIRA CREATED / DELUXE CLIENT EDITION - SUPABASE CLIENT INTEGRATION
// ==============================================================================

const LumiraSupabase = (() => {
  // Default storage keys & project credentials
  const STORAGE_URL_KEY = 'lumira_supabase_url';
  const STORAGE_KEY_KEY = 'lumira_supabase_anon_key';
  const DEFAULT_SUPABASE_URL = 'https://swarweprhwoelirrmztv.supabase.co';
  const DEFAULT_SUPABASE_KEY = 'sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy';

  // Configured or fallback credentials
  let client = null;
  let isConnected = false;

  function getStoredCredentials() {
    return {
      url: localStorage.getItem(STORAGE_URL_KEY) || DEFAULT_SUPABASE_URL,
      key: localStorage.getItem(STORAGE_KEY_KEY) || DEFAULT_SUPABASE_KEY
    };
  }

  function setCredentials(url, key) {
    if (url) localStorage.setItem(STORAGE_URL_KEY, url.trim().replace(/\/+$/, ''));
    if (key) localStorage.setItem(STORAGE_KEY_KEY, key.trim());
    init();
  }

  function clearCredentials() {
    localStorage.removeItem(STORAGE_URL_KEY);
    localStorage.removeItem(STORAGE_KEY_KEY);
    client = null;
    isConnected = false;
  }

  function isConfigured() {
    const { url, key } = getStoredCredentials();
    return Boolean(url && key && url.startsWith('http') && key.length > 20);
  }

  function init() {
    const { url, key } = getStoredCredentials();
    if (window.supabase && url && key) {
      try {
        client = window.supabase.createClient(url, key, {
          auth: { persistSession: true, autoRefreshToken: true }
        });
        checkConnection();
      } catch (err) {
        console.warn('[Supabase] Init error:', err);
        client = null;
        isConnected = false;
      }
    } else {
      client = null;
      isConnected = false;
    }
  }

  async function checkConnection() {
    if (!client) {
      isConnected = false;
      return false;
    }
    try {
      const { data, error } = await client.from('launcher_accounts').select('id', { head: true, count: 'exact' });
      if (!error || (error && error.code === 'PGRST205')) {
        isConnected = true;
        return true;
      }
      isConnected = false;
      return false;
    } catch (err) {
      isConnected = false;
      return false;
    }
  }

  // --- Profile Operations ---
  async function saveProfile(profile) {
    if (!client || !isConfigured()) return { success: false, offline: true };
    try {
      const { data, error } = await client.from('profiles').upsert({
        username: profile.username,
        email: profile.email || `${profile.username.toLowerCase()}@lumira.client`,
        role: profile.role || 'ELITE',
        balance: profile.balance || 1250,
        hwid: profile.hwid || 'WIN11-D8A4-9F2B-HORUS',
        subscription_till: profile.subscription_till || '01.01.2048',
        avatar_url: `https://mc-heads.net/avatar/${encodeURIComponent(profile.username)}/128`,
        updated_at: new Date().toISOString()
      }, { onConflict: 'username' });

      if (error) throw error;
      return { success: true, data };
    } catch (err) {
      console.warn('[Supabase] saveProfile error:', err);
      return { success: false, error };
    }
  }

  async function getProfile(username) {
    if (!client || !isConfigured()) return null;
    try {
      const { data, error } = await client.from('profiles').select('*').eq('username', username).single();
      if (error) return null;
      return data;
    } catch (err) {
      return null;
    }
  }

  // --- Auth Operations ---
  async function registerUser(username, email, password) {
    if (!client || !isConfigured()) {
      return { success: true, offline: true, username };
    }
    try {
      // Check existing
      const { data: existing } = await client.from('profiles').select('username').eq('username', username).single();
      if (existing) {
        return { success: false, error: 'Пользователь с таким никнеймом уже зарегистрирован в базе!' };
      }

      // Upsert profile
      const { error } = await client.from('profiles').insert({
        username,
        email,
        password_hash: btoa(password), // Simple hash for database demo
        role: 'ELITE',
        balance: 1000,
        hwid: 'WIN11-' + Math.random().toString(36).substring(2, 6).toUpperCase() + '-HORUS',
        subscription_till: '01.01.2048'
      });
      if (error) throw error;
      await logEvent(username, 'REGISTER', `Новый аккаунт создан: ${username}`);
      return { success: true, username };
    } catch (err) {
      console.warn('[Supabase] registerUser error:', err);
      return { success: false, error: err.message };
    }
  }

  async function loginUser(username, password) {
    if (!client || !isConfigured()) {
      return { success: true, offline: true, username };
    }
    try {
      const { data, error } = await client.from('profiles').select('*').eq('username', username).single();
      if (error || !data) {
        return { success: false, error: 'Пользователь не найден в базе данных Supabase!' };
      }
      if (data.password_hash && data.password_hash !== btoa(password)) {
        return { success: false, error: 'Неверный пароль!' };
      }
      await logEvent(username, 'LOGIN', `Вход в аккаунт: ${username}`);
      return { success: true, profile: data };
    } catch (err) {
      return { success: false, error: err.message };
    }
  }

  // --- Cloud Configs Operations ---
  async function saveConfig(username, configName, configData, serverTag = 'All Servers', fileSize = '12.4 KB') {
    if (!client || !isConfigured()) return { success: false, offline: true };
    try {
      const { data, error } = await client.from('cloud_configs').upsert({
        username,
        config_name: configName,
        config_data: configData,
        server_tag: serverTag,
        file_size: fileSize,
        share_code: `HORUS-CFG-${encodeURIComponent(configName).substring(0, 8).toUpperCase()}-${Math.floor(1000 + Math.random() * 9000)}`,
        updated_at: new Date().toISOString()
      }, { onConflict: 'username,config_name' });

      if (error) throw error;
      await logEvent(username, 'CONFIG_SAVE', `Сохранен конфиг ${configName}`);
      return { success: true, data };
    } catch (err) {
      console.warn('[Supabase] saveConfig error:', err);
      return { success: false, error };
    }
  }

  async function getConfigs(username) {
    if (!client || !isConfigured()) return [];
    try {
      const { data, error } = await client.from('cloud_configs').select('*').or(`username.eq.${username},is_public.eq.true`).order('updated_at', { ascending: false });
      if (error) return [];
      return data.map(c => ({
        name: c.config_name,
        size: c.file_size || '12 KB',
        modified: new Date(c.updated_at).toLocaleDateString('ru-RU'),
        server: c.server_tag,
        downloads: c.downloads || 0,
        shareCode: c.share_code,
        data: c.config_data
      }));
    } catch (err) {
      return [];
    }
  }

  async function deleteConfig(username, configName) {
    if (!client || !isConfigured()) return { success: false, offline: true };
    try {
      const { error } = await client.from('cloud_configs').delete().eq('username', username).eq('config_name', configName);
      if (error) throw error;
      return { success: true };
    } catch (err) {
      return { success: false, error };
    }
  }

  // --- Cosmetics Operations ---
  async function saveCosmetics(username, cosmetics) {
    if (!client || !isConfigured()) return { success: false, offline: true };
    try {
      const { error } = await client.from('user_cosmetics').upsert({
        username,
        cosmetics,
        updated_at: new Date().toISOString()
      }, { onConflict: 'username' });
      if (error) throw error;
      return { success: true };
    } catch (err) {
      return { success: false, error };
    }
  }

  async function getCosmetics(username) {
    if (!client || !isConfigured()) return null;
    try {
      const { data, error } = await client.from('user_cosmetics').select('cosmetics').eq('username', username).single();
      if (error || !data) return null;
      return data.cosmetics;
    } catch (err) {
      return null;
    }
  }

  // --- Alt Accounts Operations ---
  async function saveAltAccounts(ownerUsername, altsList) {
    if (!client || !isConfigured()) return { success: false, offline: true };
    try {
      await client.from('alt_accounts').delete().eq('owner_username', ownerUsername);
      const rows = altsList.map(a => ({
        owner_username: ownerUsername,
        username: a.username,
        server: a.server || 'Whitegrief',
        rank: a.rank || 'игᴘоᴋ',
        uuid: a.uuid || ''
      }));
      const { error } = await client.from('alt_accounts').insert(rows);
      if (error) throw error;
      return { success: true };
    } catch (err) {
      return { success: false, error };
    }
  }

  async function getAltAccounts(ownerUsername) {
    if (!client || !isConfigured()) return [];
    try {
      const { data, error } = await client.from('alt_accounts').select('*').eq('owner_username', ownerUsername);
      if (error || !data) return [];
      return data;
    } catch (err) {
      return [];
    }
  }

  // --- Audit Log Operations ---
  async function logEvent(username, eventType, details) {
    if (!client || !isConfigured()) return;
    try {
      await client.from('launcher_logs').insert({
        username,
        event_type: eventType,
        details,
        client_version: '2.0'
      });
    } catch (e) {}
  }

  // Self init
  if (typeof window !== 'undefined') {
    window.addEventListener('DOMContentLoaded', init);
  }

  return {
    init,
    isConfigured,
    isConnected: () => isConnected,
    checkConnection,
    getStoredCredentials,
    setCredentials,
    clearCredentials,
    saveProfile,
    getProfile,
    registerUser,
    loginUser,
    saveConfig,
    getConfigs,
    deleteConfig,
    saveCosmetics,
    getCosmetics,
    saveAltAccounts,
    getAltAccounts,
    logEvent
  };
})();

// Attach to window
if (typeof window !== 'undefined') {
  window.LumiraSupabase = LumiraSupabase;
}
