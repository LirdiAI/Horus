export const SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co";
export const SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy";

export interface SupabaseProfile {
  id: string;
  username: string;
  email?: string | null;
  role?: string | null;
  balance?: number | null;
  hwid?: string | null;
  subscription_till?: string | null;
  avatar_url?: string | null;
  updated_at?: string | null;
}

export async function fetchSupabaseProfile(username: string): Promise<SupabaseProfile | null> {
  if (!username) return null;
  try {
    const url = `${SUPABASE_URL}/rest/v1/profiles?username=ilike.${encodeURIComponent(username.trim())}&select=*`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        apikey: SUPABASE_KEY,
        Authorization: `Bearer ${SUPABASE_KEY}`,
        Accept: 'application/json',
      },
    });
    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        return data[0];
      }
    }
  } catch (err) {
    console.warn('[Supabase] Failed to fetch profile for', username, err);
  }
  return null;
}

export async function upsertSupabaseProfile(username: string, role = 'Lifetime'): Promise<SupabaseProfile | null> {
  if (!username) return null;
  try {
    const existing = await fetchSupabaseProfile(username);
    if (existing) return existing;

    const payload = {
      username: username.trim(),
      role: role,
      balance: 1000,
      subscription_till: '01.01.2048',
      hwid: 'HORUS-CLIENT-PC',
    };

    const res = await fetch(`${SUPABASE_URL}/rest/v1/profiles`, {
      method: 'POST',
      headers: {
        apikey: SUPABASE_KEY,
        Authorization: `Bearer ${SUPABASE_KEY}`,
        'Content-Type': 'application/json',
        Prefer: 'return=representation',
      },
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      const created = await res.json();
      if (Array.isArray(created) && created.length > 0) {
        return created[0];
      }
    }
  } catch (err) {
    console.warn('[Supabase] Failed to upsert profile for', username, err);
  }
  return null;
}
