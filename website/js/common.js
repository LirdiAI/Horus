// Horus Client - Shared Common Script
const SUPABASE_URL = "https://swarweprhwoelirrmztv.supabase.co";
const SUPABASE_KEY = "sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy";

// Clear any legacy mock accounts or passwords from local storage
try {
    localStorage.removeItem("horus_users");
    localStorage.removeItem("lumira_accounts");
} catch(e) {}

function getActiveUser() {
    try {
        const raw = localStorage.getItem("horus_active_user");
        if (raw) {
            if (raw.startsWith("{")) {
                return JSON.parse(raw);
            }
            return { username: raw };
        }
    } catch(e) {}
    return null;
}

function setActiveUser(userObj) {
    if (!userObj) {
        localStorage.removeItem("horus_active_user");
    } else {
        // Only store username and session display info (NO passwords, NO credentials)
        const safeSession = {
            username: userObj.username,
            role: userObj.role || "User",
            subscription_till: userObj.subscription_till || userObj.subscription || "01.01.2048"
        };
        localStorage.setItem("horus_active_user", JSON.stringify(safeSession));
    }
}

function horusLogout() {
    localStorage.removeItem("horus_active_user");
    window.location.href = "login.html";
}

// Fetch live profile from Supabase
async function fetchSupabaseProfile(username) {
    try {
        const param = username.includes("@") ? `email=eq.${encodeURIComponent(username)}` : `username=eq.${encodeURIComponent(username)}`;
        const res = await fetch(`${SUPABASE_URL}/rest/v1/profiles?${param}&select=*`, {
            headers: { "apikey": SUPABASE_KEY, "Authorization": `Bearer ${SUPABASE_KEY}` }
        });
        if (res.ok) {
            const rows = await res.json();
            if (rows && rows.length > 0) return rows[0];
        }
    } catch(e) {}
    return null;
}

// Update Topbar Navigation depending on login state
document.addEventListener("DOMContentLoaded", function() {
    const user = getActiveUser();
    const rightBar = document.querySelector(".topbar .right");
    if (rightBar) {
        if (user) {
            rightBar.innerHTML = `
                <div class="user-info" onclick="location.href='index.html#profile'" style="cursor: pointer;">
                    <div class="mini-avatar">
                        <img src="images/user_avatar.png" alt="${user.username}" onerror="this.src='images/logo.png'">
                    </div>
                    <span class="username-text" style="font-weight: 600; color: #fff;">${user.username}</span>
                </div>
                <button class="logout-btn" onclick="horusLogout()" title="Выйти" style="background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; padding: 6px 12px; cursor: pointer;">
                    <i class="fas fa-sign-out-alt"></i> Выйти
                </button>
            `;
        }
    }

    // Highlight current nav item
    const currentPath = window.location.pathname.split("/").pop() || "index.html";
    const navLinks = document.querySelectorAll(".topbar .center a");
    navLinks.forEach(link => {
        const href = link.getAttribute("href");
        if (href === currentPath || (currentPath === "index.html" && (href === "/" || href === "index.html"))) {
            link.classList.add("active");
        } else {
            link.classList.remove("active");
        }
    });
});
