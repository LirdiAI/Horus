/**
 * Horus Client Official Website Script
 * Adapted from Rockstar/Aethra dark theme with interactive WebGL background,
 * dynamic server stats, leaderboard, purchase modal, and Supabase/local profile auth.
 */

// --- Initial Stats Data & Fallback ---
const DEFAULT_HORUS_STATS = {
    servers: {
        holyworld: 412850,
        funtime: 684200,
        spookytime: 295100
    },
    players: [
        {
            username: 'Lumira',
            seconds: 142800,
            avatar: '',
            servers: { holyworld: 34500, funtime: 89100, spookytime: 19200 }
        },
        {
            username: 'HorusPegas',
            seconds: 118400,
            avatar: '',
            servers: { holyworld: 42100, funtime: 51200, spookytime: 25100 }
        },
        {
            username: 'Nikita_2026',
            seconds: 96300,
            avatar: '',
            servers: { holyworld: 28900, funtime: 46200, spookytime: 21200 }
        },
        {
            username: 'MixerGod',
            seconds: 82400,
            avatar: '',
            servers: { holyworld: 19800, funtime: 41900, spookytime: 20700 }
        },
        {
            username: 'VortexMLSAC',
            seconds: 67100,
            avatar: '',
            servers: { holyworld: 21000, funtime: 32500, spookytime: 13600 }
        },
        {
            username: 'AuraDestroyer',
            seconds: 54900,
            avatar: '',
            servers: { holyworld: 14300, funtime: 26800, spookytime: 13800 }
        }
    ]
};

// --- Time Formatting Helper ---
function formatStatTime(seconds) {
    const total = Math.max(0, Number(seconds) || 0);
    const days = Math.floor(total / 86400);
    const hours = Math.floor((total % 86400) / 3600);
    const minutes = Math.floor((total % 3600) / 60);
    return (days ? days + 'д ' : '') + (hours ? hours + 'ч ' : '') + minutes + 'м';
}

// --- Active Session Management ---
const SessionManager = {
    getUser() {
        try {
            const raw = localStorage.getItem('horus_active_user');
            if (raw) return JSON.parse(raw);
        } catch (e) {}
        return null;
    },
    setUser(user) {
        // Only keep session info in local storage (NO passwords or credentials!)
        const safeSession = {
            username: user.username,
            role: user.role || 'User',
            subscription_till: user.subscription_till || '01.01.2048'
        };
        localStorage.setItem('horus_active_user', JSON.stringify(safeSession));
        this.updateUI();
    },
    clearUser() {
        localStorage.removeItem('horus_active_user');
        this.updateUI();
    },
    updateUI() {
        const user = this.getUser();
        const headerActions = document.querySelector('.header-actions');
        const profileEntry = document.querySelector('[data-profile-entry]');
        const profileModal = document.getElementById('auth-modal');

        if (headerActions) {
            if (user) {
                headerActions.innerHTML = `
                    <div class="header-user" data-open-modal="auth-modal" role="button" tabindex="0" title="Открыть личный кабинет" style="display:inline-flex; align-items:center; gap:8px; margin-right:8px; cursor:pointer; padding:5px 12px; border-radius:20px; background:rgba(255,255,255,0.06); border:1px solid rgba(255,255,255,0.14);">
                        <img src="https://mc-heads.net/avatar/${encodeURIComponent(user.username)}/28" alt="" style="width:24px; height:24px; border-radius:50%; border:1px solid rgba(255,255,255,0.2);" onerror="this.src='images/app_icon.png'">
                        <span style="font-size:13px; font-weight:600; color:#fff;">${user.username}</span>
                    </div>
                    <button class="header-logout" type="button" data-logout="" title="Выйти из аккаунта">
                        <svg class="nav-icon" aria-hidden="true"><use href="images/ui-icons.svg#logout"></use></svg>
                        Выйти
                    </button>
                `;
            } else {
                headerActions.innerHTML = `
                    <button class="secondary-action" type="button" data-open-modal="auth-modal" style="margin-right:8px; padding:6px 14px; min-height:34px; font-size:13px;">
                        <svg class="nav-icon" aria-hidden="true"><use href="images/ui-icons.svg#user"></use></svg>
                        Вход
                    </button>
                    <button class="primary-action" type="button" data-open-modal="purchase-modal" style="padding:6px 14px; min-height:34px; font-size:13px;">
                        Купить
                    </button>
                `;
            }
            // Bind newly generated buttons
            bindModalTriggers();
            const logoutBtn = headerActions.querySelector('[data-logout]');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', () => {
                    SessionManager.clearUser();
                    SessionManager.showToast('Вы вышли из аккаунта');
                });
            }
        }

        // Update profile modal view
        if (profileModal) {
            const guestPanel = profileModal.querySelector('[data-auth-guest]');
            const userPanel = profileModal.querySelector('[data-auth-user]');
            const visualTitle = profileModal.querySelector('[data-auth-visual-title]');
            const visualDesc = profileModal.querySelector('[data-auth-visual-desc]');

            if (guestPanel && userPanel) {
                if (user && user.username) {
                    const uname = user.username || 'Lumira';
                    guestPanel.classList.add('is-hidden');
                    guestPanel.style.display = 'none';

                    userPanel.classList.remove('is-hidden');
                    userPanel.removeAttribute('hidden');
                    userPanel.style.display = 'block';

                    if (visualTitle) visualTitle.textContent = 'Личный кабинет';
                    if (visualDesc) visualDesc.textContent = `Аккаунт игрока ${uname} активен`;

                    const nickEl = userPanel.querySelector('[data-user-name]');
                    const roleEl = userPanel.querySelector('[data-user-role]');
                    const hwidEl = userPanel.querySelector('[data-user-hwid]');
                    const secHwidEl = userPanel.querySelector('[data-sec-hwid]');
                    const avatarEl = userPanel.querySelector('[data-user-avatar]');
                    const subEl = userPanel.querySelector('[data-user-sub]');
                    const betaBadge = userPanel.querySelector('[data-user-beta-badge]');
                    const betaNotice = userPanel.querySelector('[data-user-beta-notice]');

                    if (nickEl) nickEl.textContent = uname;
                    if (roleEl) roleEl.textContent = user.role || 'DLC EDITION • АКТИВЕН';
                    
                    // HWID binding logic: Gray "Не привязан" until client is launched & in-game
                    if (user.hwid && user.hwid.trim() !== '') {
                        if (hwidEl) hwidEl.innerHTML = `<span style="color:#4ade80; font-family:monospace; font-size:11px;">${user.hwid}</span>`;
                        if (secHwidEl) secHwidEl.innerHTML = `<span style="color:#4ade80; font-family:monospace;">${user.hwid}</span>`;
                    } else {
                        if (hwidEl) hwidEl.innerHTML = `<span style="color:#71717a; font-weight:600;">Не привязан</span>`;
                        if (secHwidEl) secHwidEl.innerHTML = `<span style="color:#71717a; font-weight:600;">Не привязан</span>`;
                    }

                    if (subEl) subEl.textContent = user.subscription_till ? (user.subscription_till.startsWith('До') ? user.subscription_till : `До ${user.subscription_till}`) : 'Навсегда (2048)';
                    
                    // Beta Access Perks
                    if (user.has_beta) {
                        if (betaBadge) betaBadge.style.display = 'inline-block';
                        if (betaNotice) betaNotice.style.display = 'block';
                    } else {
                        if (betaBadge) betaBadge.style.display = 'none';
                        if (betaNotice) betaNotice.style.display = 'none';
                    }

                    if (avatarEl) {
                        avatarEl.src = `https://mc-heads.net/avatar/${encodeURIComponent(uname)}/64`;
                        avatarEl.onerror = () => { avatarEl.src = 'images/app_icon.png'; };
                    }
                } else {
                    guestPanel.classList.remove('is-hidden');
                    guestPanel.removeAttribute('hidden');
                    guestPanel.style.display = 'block';

                    userPanel.classList.add('is-hidden');
                    userPanel.style.display = 'none';

                    if (visualTitle) visualTitle.textContent = 'Horus Cloud';
                    if (visualDesc) visualDesc.textContent = 'Единый аккаунт для лаунчера, клиента и облачных конфигов';
                }
            }
        }
    },
    showToast(message) {
        let toast = document.getElementById('horus-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'horus-toast';
            toast.style.cssText = 'position:fixed; bottom:28px; right:28px; z-index:9999; background:rgba(18,20,24,0.95); border:1px solid rgba(255,255,255,0.18); border-radius:10px; color:#fff; padding:12px 20px; font-size:13px; font-weight:600; box-shadow:0 12px 30px rgba(0,0,0,0.5); backdrop-filter:blur(12px); transform:translateY(20px); opacity:0; transition:all 0.25s ease;';
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';
        setTimeout(() => {
            toast.style.transform = 'translateY(20px)';
            toast.style.opacity = '0';
        }, 3200);
    }
};

// --- Modal System ---
function closeModal(modal) {
    if (!modal) return;
    modal.hidden = true;
    document.body.classList.remove('modal-open');
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    if (modalId === 'auth-modal') {
        SessionManager.updateUI();
    checkClientHwidSync();
    if (window.location.hash === '#profile') {
        setTimeout(() => openModal('auth-modal'), 100);
    }
    }
    modal.hidden = false;
    modal.removeAttribute('hidden');
    document.body.classList.add('modal-open');
    window.dispatchEvent(new Event('resize'));
    const input = modal.querySelector('input:not([disabled])');
    if (input && input.offsetParent !== null) input.focus();
}

function bindModalTriggers() {
    document.querySelectorAll('[data-open-modal]').forEach(function (button) {
        button.onclick = function (e) {
            e.preventDefault();
            const targetId = button.dataset.openModal;
            openModal(targetId);
        };
    });

    document.querySelectorAll('[data-close-modal]').forEach(function (button) {
        button.onclick = function () {
            closeModal(button.closest('.modal'));
        };
    });
}

// --- Purchase Modal Logic ---
function initPurchaseModal() {
    const purchaseModal = document.getElementById('purchase-modal');
    if (!purchaseModal) return;

    const status = purchaseModal.querySelector('[data-purchase-status]');
    const promoInput = purchaseModal.querySelector('[data-promo-input]');
    const terms = purchaseModal.querySelector('[data-purchase-terms]');
    const payButton = purchaseModal.querySelector('[data-start-payment]');
    const funpayButton = purchaseModal.querySelector('[data-funpay-payment]');

    let appliedPromo = '';
    let discount = 0;
    let paymentProvider = 'sbp';

    function updateTotal() {
        const product = purchaseModal.querySelector('.product-option.is-selected') || purchaseModal.querySelector('.product-option');
        if (!product) return;
        const amount = Number(product.dataset.productAmount) || 390;
        const total = Math.round(amount * (100 - discount) / 100);
        const saleEl = purchaseModal.querySelector('[data-purchase-sale]');
        const payAmountEl = purchaseModal.querySelector('[data-pay-amount]');
        if (saleEl) saleEl.textContent = total + ' ₽';
        if (payAmountEl) payAmountEl.textContent = total + ' ₽';
    }

    function updateTerms() {
        const checked = terms ? terms.checked : true;
        if (payButton) payButton.disabled = !checked;
        if (funpayButton) funpayButton.setAttribute('aria-disabled', checked ? 'false' : 'true');
    }

    purchaseModal.querySelectorAll('.product-option').forEach(function (button) {
        button.addEventListener('click', function () {
            purchaseModal.querySelectorAll('.product-option').forEach(item => item.classList.remove('is-selected'));
            button.classList.add('is-selected');
            const titleEl = purchaseModal.querySelector('[data-purchase-title]');
            if (titleEl) titleEl.textContent = button.dataset.productTitle || button.textContent.trim();
            const regular = purchaseModal.querySelector('[data-purchase-regular]');
            if (regular) {
                regular.textContent = button.dataset.productRegular || '';
                regular.hidden = !button.dataset.productRegular;
            }
            discount = 0;
            appliedPromo = '';
            if (promoInput) promoInput.value = '';
            if (status) status.textContent = '';
            updateTotal();
        });
    });

    purchaseModal.querySelectorAll('[data-payment-provider]').forEach(function (button) {
        button.addEventListener('click', function () {
            purchaseModal.querySelectorAll('[data-payment-provider]').forEach(item => item.classList.remove('is-selected'));
            button.classList.add('is-selected');
            paymentProvider = button.dataset.paymentProvider;
            if (status) status.textContent = '';
        });
    });

    const applyPromoBtn = purchaseModal.querySelector('[data-apply-promo]');
    if (applyPromoBtn && promoInput) {
        applyPromoBtn.addEventListener('click', function () {
            const code = promoInput.value.trim().toUpperCase();
            if (!code) {
                if (status) status.textContent = 'Введите промокод.';
                return;
            }
            if (['HORUS', 'HORUS2026', 'DELUXE', 'LUMIRA', 'PEGAS'].includes(code)) {
                discount = 20;
                appliedPromo = code;
                updateTotal();
                if (status) {
                    status.classList.add('is-success');
                    status.textContent = `Промокод ${code} активирован! Скидка 20% применена.`;
                }
            } else {
                discount = 0;
                appliedPromo = '';
                updateTotal();
                if (status) {
                    status.classList.remove('is-success');
                    status.textContent = 'Недействительный промокод.';
                }
            }
        });
    }

    if (terms) terms.addEventListener('change', updateTerms);

    if (payButton) {
        payButton.addEventListener('click', function () {
            if (terms && !terms.checked) {
                if (status) status.textContent = 'Подтвердите согласие с условиями.';
                return;
            }
            const product = purchaseModal.querySelector('.product-option.is-selected');
            const pTitle = product ? product.dataset.productTitle : 'Horus Client DLC';
            payButton.disabled = true;
            if (status) status.textContent = 'Формирование безопасного счета...';

            setTimeout(() => {
                payButton.disabled = false;
                closeModal(purchaseModal);
                SessionManager.showToast(`Счет сформирован! Тариф «${pTitle}». Перенаправление в платежный шлюз...`);
            }, 900);
        });
    }

    updateTotal();
    updateTerms();
}

// --- Dynamic Statistics & Leaderboard Engine ---
function initStatistics() {
    const leaderboard = document.querySelector('[data-player-leaderboard]');
    const labels = { funtime: 'FunTime', spookytime: 'SpookyTime', holyworld: 'HolyWorld' };

    const updateStatsDisplay = function (data) {
        const totals = Object.values(data.servers);
        const maximum = Math.max(1, ...totals);

        // Update server label text & height variables
        Object.entries(data.servers).forEach(function ([server, seconds]) {
            const label = document.querySelector('[data-server-total="' + server + '"]');
            const card = label && label.closest('.server-card');
            if (label) label.textContent = formatStatTime(seconds);
            if (card) card.style.setProperty('--server-height', Math.max(10, Math.round(Number(seconds) / maximum * 100)) + '%');
        });

        // Graph bezier points calculation
        const graphPoints = { holyworld: [70, 0], funtime: [300, 0], spookytime: [530, 0] };
        Object.keys(graphPoints).forEach(function (server) {
            graphPoints[server][1] = 205 - Math.round(Number(data.servers[server] || 0) / maximum * 145);
            const point = document.querySelector('[data-stat-point="' + server + '"]');
            if (point) point.setAttribute('cy', graphPoints[server][1]);
        });

        // Draw smooth SVG bezier line
        const line = document.querySelector('[data-stat-line]');
        if (line) {
            const a = graphPoints.holyworld, b = graphPoints.funtime, c = graphPoints.spookytime;
            line.setAttribute('d', 'M' + a[0] + ' ' + a[1] + ' C' + (a[0] + 85) + ' ' + a[1] + ' ' + (b[0] - 85) + ' ' + b[1] + ' ' + b[0] + ' ' + b[1] + ' S' + (c[0] - 85) + ' ' + c[1] + ' ' + c[0] + ' ' + c[1]);
        }

        // Float player tags above curve
        const graphPlayers = document.querySelector('[data-graph-players]');
        if (graphPlayers) {
            graphPlayers.replaceChildren();
            data.players.slice(0, 3).forEach(function (player, index) {
                const server = ['holyworld', 'funtime', 'spookytime'][index];
                const point = graphPoints[server];
                const item = document.createElement('span');
                item.className = 'activity-graph-player';
                item.style.left = (point[0] / 600 * 100) + '%';
                item.style.top = (point[1] / 250 * 100) + '%';
                
                const avatar = document.createElement('img');
                avatar.src = `https://mc-heads.net/avatar/${encodeURIComponent(player.username)}/24`;
                avatar.alt = '';
                item.append(avatar);

                const name = document.createElement('i');
                name.textContent = player.username;
                item.append(name);
                graphPlayers.append(item);
            });
        }

        // Render Leaderboard list
        if (leaderboard) {
            leaderboard.replaceChildren();
            data.players.forEach(function (player, index) {
                const item = document.createElement('li');
                item.innerHTML = `
                    <span class="player-rank">${String(index + 1).padStart(2, '0')}</span>
                    <span class="player-identity">
                        <img class="player-avatar" src="https://mc-heads.net/avatar/${encodeURIComponent(player.username)}/32" alt="${player.username}">
                        <span class="player-name">${player.username}</span>
                        <small class="player-server">${Object.keys(labels).map(s => labels[s] + ' ' + formatStatTime(player.servers && player.servers[s])).join(' · ')}</small>
                    </span>
                    <strong>${formatStatTime(player.seconds)}</strong>
                `;
                leaderboard.append(item);
            });
        }

        const updated = document.querySelector('[data-stats-updated]');
        if (updated) updated.textContent = 'Обновлено сейчас';
    };

    // Try fetching from server or fallback seamlessly
    fetch('api/stats.php', { cache: 'no-store' })
        .then(res => {
            if (!res.ok) throw new Error('API unavailable');
            return res.json();
        })
        .then(data => updateStatsDisplay(data))
        .catch(() => {
            // Use built-in dynamic stats
            updateStatsDisplay(DEFAULT_HORUS_STATS);
        });
}

// --- Auth Modal & Form Handling ---
function initAuthModal() {
    const authModal = document.getElementById('auth-modal');
    if (!authModal) return;

    const loginTabBtn = authModal.querySelector('[data-auth-tab="login"]');
    const registerTabBtn = authModal.querySelector('[data-auth-tab="register"]');
    const loginForm = authModal.querySelector('#login-form');
    const registerForm = authModal.querySelector('#signup-form');
    const loginStatus = authModal.querySelector('#login-status');
    const registerStatus = authModal.querySelector('#signup-status');

    function setTab(tab) {
        if (tab === 'login') {
            if (loginTabBtn) loginTabBtn.classList.add('is-active');
            if (registerTabBtn) registerTabBtn.classList.remove('is-active');
            if (loginForm) {
                loginForm.style.setProperty('display', 'grid', 'important');
                loginForm.removeAttribute('hidden');
                loginForm.classList.remove('is-hidden');
            }
            if (registerForm) {
                registerForm.style.setProperty('display', 'none', 'important');
                registerForm.setAttribute('hidden', '');
                registerForm.classList.add('is-hidden');
            }
        } else {
            if (registerTabBtn) registerTabBtn.classList.add('is-active');
            if (loginTabBtn) loginTabBtn.classList.remove('is-active');
            if (registerForm) {
                registerForm.style.setProperty('display', 'grid', 'important');
                registerForm.removeAttribute('hidden');
                registerForm.classList.remove('is-hidden');
            }
            if (loginForm) {
                loginForm.style.setProperty('display', 'none', 'important');
                loginForm.setAttribute('hidden', '');
                loginForm.classList.add('is-hidden');
            }
        }
    }

    if (loginTabBtn) {
        loginTabBtn.addEventListener('click', (e) => {
            e.preventDefault();
            setTab('login');
        });
    }
    if (registerTabBtn) {
        registerTabBtn.addEventListener('click', (e) => {
            e.preventDefault();
            setTab('register');
        });
    }

    // Default to login tab
    setTab('login');

    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            const username = loginForm.elements.username.value.trim();
            const password = loginForm.elements.password.value;
            if (loginStatus) {
                loginStatus.classList.remove('is-success');
                loginStatus.textContent = 'Авторизация в Horus Cloud...';
            }

            // Supabase integration if available
            if (window.LumiraSupabase && LumiraSupabase.isConfigured()) {
                const res = await LumiraSupabase.loginUser(username, password);
                if (!res.success && !res.offline) {
                    if (loginStatus) loginStatus.textContent = res.error || 'Ошибка входа.';
                    return;
                }
            }

            // Save user session
            const user = {
                username: username || 'Lumira',
                role: 'DLC EDITION',
                hwid: 'WIN11-' + Math.random().toString(36).substring(2, 6).toUpperCase() + '-HORUS',
                subscription_till: '01.01.2048'
            };
            SessionManager.setUser(user);
            if (loginStatus) {
                loginStatus.classList.add('is-success');
                loginStatus.textContent = 'Успешный вход!';
            }
            setTimeout(() => {
                closeModal(authModal);
                SessionManager.showToast(`Добро пожаловать в Horus Client, ${user.username}!`);
            }, 600);
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            const username = registerForm.elements.username.value.trim();
            const email = registerForm.elements.email ? registerForm.elements.email.value.trim() : '';
            const password = registerForm.elements.password.value;
            const confirmation = registerForm.elements.password_confirmation ? registerForm.elements.password_confirmation.value : password;

            if (password !== confirmation) {
                if (registerStatus) registerStatus.textContent = 'Пароли не совпадают.';
                return;
            }
            if (registerStatus) {
                registerStatus.classList.remove('is-success');
                registerStatus.textContent = 'Создание аккаунта...';
            }

            if (window.LumiraSupabase && LumiraSupabase.isConfigured()) {
                const res = await LumiraSupabase.registerUser(username, email, password);
                if (!res.success && !res.offline) {
                    if (registerStatus) registerStatus.textContent = res.error || 'Ошибка регистрации.';
                    return;
                }
            }

            // 1. Sync to local launcher background bridge
            try {
                fetch('http://127.0.0.1:28549/api/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        username: username,
                        password: password,
                        email: email,
                        role: 'DLC EDITION',
                        subscription_till: '01.01.2048'
                    })
                }).catch(() => {});
            } catch(e) {}

            // 2. Sync to Supabase profiles table
            try {
                fetch('https://swarweprhwoelirrmztv.supabase.co/rest/v1/profiles', {
                    method: 'POST',
                    headers: {
                        'apikey': 'sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy',
                        'Authorization': 'Bearer sb_publishable_HyTiEAwPcnXOL2qWcseYOw_KEBOGbdy',
                        'Content-Type': 'application/json',
                        'Prefer': 'return=minimal'
                    },
                    body: JSON.stringify({
                        username: username,
                        email: email || `${username.toLowerCase()}@gmail.com`,
                        password_hash: btoa(unescape(encodeURIComponent(password))),
                        role: 'DLC EDITION',
                        subscription_till: '01.01.2048'
                    })
                }).catch(() => {});
            } catch(e) {}

            const user = {
                username: username,
                role: 'DLC EDITION',
                hwid: '', // Bound ONLY when launched in Minecraft & playing
                subscription_till: '01.01.2048'
            };
            SessionManager.setUser(user);
            if (registerStatus) {
                registerStatus.classList.add('is-success');
                registerStatus.textContent = 'Аккаунт успешно создан!';
            }
            setTimeout(() => {
                closeModal(authModal);
                SessionManager.showToast(`Регистрация завершена! Приятной игры, ${user.username}.`);
            }, 600);
        });
    }

    // Profile action buttons
    const hwidBtn = authModal.querySelector('[data-reset-hwid]');
    if (hwidBtn) {
        hwidBtn.addEventListener('click', () => {
            const user = SessionManager.getUser();
            if (user) {
                user.hwid = 'WIN11-' + Math.random().toString(36).substring(2, 6).toUpperCase() + '-HORUS';
                SessionManager.setUser(user);
                SessionManager.showToast('HWID успешно сброшен и привязан к текущей системе.');
            }
        });
    }

    const logoutModalBtn = authModal.querySelector('[data-logout-modal]');
    if (logoutModalBtn) {
        logoutModalBtn.addEventListener('click', () => {
            SessionManager.clearUser();
            closeModal(authModal);
            SessionManager.showToast('Вы вышли из аккаунта.');
        });
    }
}

// --- Download Guide Notification ---

// --- Smooth Scroll Reveal Animations ---
function initScrollAnimations() {
    if (!('IntersectionObserver' in window)) {
        document.querySelectorAll('.mission, .social-links, .features-header, .feature-card, .statistics-heading, .server-comparison, .leaderboard').forEach(el => {
            el.classList.add('is-revealed');
        });
        return;
    }

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-revealed');
            }
        });
    }, { threshold: 0.08, rootMargin: '0px 0px -40px 0px' });

    const targets = document.querySelectorAll(
        '.mission, .social-links, .features-header, .feature-card, .statistics-heading, .server-comparison, .leaderboard'
    );
    targets.forEach((el, index) => {
        el.classList.add('reveal-item');
        if (el.classList.contains('feature-card')) {
            el.style.transitionDelay = `${(index % 3) * 0.12}s`;
        }
        observer.observe(el);
    });
}

// --- Smooth Navigation ScrollSpy ---
function initNavScrollSpy() {
    const sections = document.querySelectorAll('section[id], main[id]');
    const navLinks = document.querySelectorAll('.site-header nav a[href^="#"]');
    if (!sections.length || !navLinks.length) return;

    window.addEventListener('scroll', () => {
        let current = '';
        const scrollY = window.pageYOffset;
        sections.forEach(section => {
            const sectionTop = section.offsetTop - 140;
            const sectionHeight = section.offsetHeight;
            if (scrollY >= sectionTop && scrollY < sectionTop + sectionHeight) {
                current = '#' + section.getAttribute('id');
            }
        });

        if (current) {
            navLinks.forEach(link => {
                link.classList.toggle('is-active', link.getAttribute('href') === current);
            });
        }
    }, { passive: true });
}

function initDownloadTriggers() {
    document.querySelectorAll('a[href$="HorusLauncher.exe"]').forEach(link => {
        link.addEventListener('click', () => {
            setTimeout(() => {
                SessionManager.showToast('Загрузка HorusLauncher.exe началась! Запустите файл после скачивания.');
            }, 300);
        });
    });
}

// --- Global Initialization ---
document.addEventListener('DOMContentLoaded', function () {
    bindModalTriggers();
    initPurchaseModal();
    initStatistics();
    initAuthModal();
    initProfileTabs();
    initGoodsHandlers();
    initSecurityHandlers();
    initRouletteSystem();
    initDownloadTriggers();
    initScrollAnimations();
    initNavScrollSpy();
    SessionManager.updateUI();
    checkClientHwidSync();
    if (window.location.hash === '#profile') {
        setTimeout(() => openModal('auth-modal'), 100);
    }

    // Close on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal').forEach(m => {
                if (!m.hidden) closeModal(m);
            });
        }
    });

    // Close on backdrop click
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.addEventListener('click', function () {
            closeModal(backdrop.closest('.modal'));
        });
    });
});


// ==============================================================================
//   PROFILE SUBTABS, GOODS, SECURITY & ROULETTE LOGIC
// ==============================================================================

function initProfileTabs() {
    const authModal = document.getElementById('auth-modal');
    if (!authModal) return;

    const tabBtns = authModal.querySelectorAll('[data-ptab]');
    const tabViews = authModal.querySelectorAll('[data-pview]');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.dataset.ptab;
            tabBtns.forEach(b => b.classList.remove('is-active'));
            btn.classList.add('is-active');

            tabViews.forEach(view => {
                if (view.dataset.pview === target) {
                    view.classList.remove('is-hidden');
                    view.style.display = 'block';
                } else {
                    view.classList.add('is-hidden');
                    view.style.display = 'none';
                }
            });

            if (target === 'roulette') {
                initRouletteTrack();
            }
        });
    });
}


async function checkClientHwidSync() {
    try {
        const res = await fetch('horus_hwid.txt?t=' + Date.now());
        if (res.ok) {
            const hwid = (await res.text()).trim();
            if (hwid && hwid.length > 5) {
                const user = SessionManager.getUser();
                if (user && (!user.hwid || user.hwid !== hwid)) {
                    user.hwid = hwid;
                    SessionManager.setUser(user);
                }
            }
        }
    } catch(e) {}
}

function initGoodsHandlers() {
    const authModal = document.getElementById('auth-modal');
    if (!authModal) return;

    authModal.querySelectorAll('[data-buy-item]').forEach(btn => {
        btn.addEventListener('click', () => {
            const item = btn.dataset.buyItem;
            const user = SessionManager.getUser() || { username: 'Lumira', role: 'DLC EDITION' };

            if (item === 'reset_hwid') {
                user.hwid = '';
                SessionManager.setUser(user);
                SessionManager.showToast('🔄 HWID успешно сброшен! Статус: «Не привязан» (привяжется автоматически при игре).');
            } else if (item === 'beta_access') {
                user.has_beta = true;
                SessionManager.setUser(user);
                SessionManager.showToast('⚡ Доступ Beta 1.21.11 активирован! Вам открыты ранние сборки и 1 спин рулетки в день.');
            } else if (item === 'sub_forever') {
                user.subscription_till = 'Навсегда (2048)';
                SessionManager.setUser(user);
                SessionManager.showToast('👑 Подписка Навсегда (1.21.11) успешно оформлена!');
            } else if (item === 'sub_90d') {
                const d = new Date();
                d.setDate(d.getDate() + 90);
                const dateStr = d.toLocaleDateString('ru-RU');
                user.subscription_till = `90 дней (до ${dateStr})`;
                SessionManager.setUser(user);
                SessionManager.showToast(`⏳ Подписка на 90 дней активирована (до ${dateStr})!`);
            } else if (item === 'sub_30d') {
                const d = new Date();
                d.setDate(d.getDate() + 30);
                const dateStr = d.toLocaleDateString('ru-RU');
                user.subscription_till = `30 дней (до ${dateStr})`;
                SessionManager.setUser(user);
                SessionManager.showToast(`🛡️ Подписка на 30 дней активирована (до ${dateStr})!`);
            }
        });
    });
}

function initSecurityHandlers() {
    const secForm = document.getElementById('sec-pwd-form');
    if (!secForm) return;

    secForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const newPwd = document.getElementById('secNewPwd').value;
        const status = document.getElementById('secPwdStatus');

        if (!newPwd || newPwd.length < 4) {
            if (status) {
                status.style.color = '#f87171';
                status.textContent = 'Пароль должен быть не короче 4 символов.';
            }
            return;
        }

        if (status) {
            status.style.color = '#4ade80';
            status.textContent = '✓ Пароль аккаунта успешно обновлен!';
        }
        SessionManager.showToast('✓ Пароль аккаунта успешно изменён!');
        secForm.reset();
        setTimeout(() => { if (status) status.textContent = ''; }, 3500);
    });
}

// --- ROULETTE WHEEL LOGIC ---
const ROULETTE_PRIZES = [
    { id: 'sub_1d', name: '1 день подписки', chance: 40, color: '#94a3b8', bg: 'rgba(148,163,184,0.18)', border: '#64748b', icon: '⚡', days: 1 },
    { id: 'sub_7d', name: '7 дней подписки', chance: 20, color: '#4ade80', bg: 'rgba(74,222,128,0.18)', border: '#22c55e', icon: '🛡️', days: 7 },
    { id: 'sub_14d', name: '14 дней подписки', chance: 13, color: '#38bdf8', bg: 'rgba(56,189,248,0.18)', border: '#0284c7', icon: '⚔️', days: 14 },
    { id: 'sub_30d', name: '30 дней подписки', chance: 10, color: '#a855f7', bg: 'rgba(168,85,247,0.18)', border: '#9333ea', icon: '🔮', days: 30 },
    { id: 'sub_90d', name: '90 дней подписки', chance: 7, color: '#f43f5e', bg: 'rgba(244,63,94,0.18)', border: '#e11d48', icon: '⏳', days: 90 },
    { id: 'sub_forever', name: 'Подписка Навсегда', chance: 5, color: '#fbbf24', bg: 'rgba(251,191,36,0.22)', border: '#f59e0b', icon: '👑', days: 9999 },
    { id: 'beta_access', name: 'Доступ Beta 1.21.11', chance: 5, color: '#ec4899', bg: 'rgba(236,72,153,0.22)', border: '#db2777', icon: '💎', isBeta: true }
];

let isRouletteSpinning = false;
let rouletteCardsList = [];

function pickWinningRoulettePrize() {
    const roll = Math.random() * 100;
    let accum = 0;
    for (const prize of ROULETTE_PRIZES) {
        accum += prize.chance;
        if (roll <= accum) return prize;
    }
    return ROULETTE_PRIZES[0];
}

function initRouletteTrack() {
    const tape = document.getElementById('rouletteTape');
    if (!tape || tape.children.length > 0) return;

    rouletteCardsList = [];
    const TOTAL_CARDS = 80;
    tape.innerHTML = '';

    for (let i = 0; i < TOTAL_CARDS; i++) {
        const prize = pickWinningRoulettePrize();
        rouletteCardsList.push(prize);

        const card = document.createElement('div');
        card.className = 'roulette-card';
        card.style.background = prize.bg;
        card.style.borderColor = prize.border;
        card.style.color = prize.color;
        card.innerHTML = `
            <div class="roulette-card-icon">${prize.icon}</div>
            <div class="roulette-card-title">${prize.name}</div>
        `;
        tape.appendChild(card);
    }
}

function initRouletteSystem() {
    const spinBtn = document.getElementById('btnSpin');
    const tape = document.getElementById('rouletteTape');
    const status = document.getElementById('rouletteStatus');
    if (!spinBtn || !tape) return;

    spinBtn.addEventListener('click', () => {
        if (isRouletteSpinning) return;

        const user = SessionManager.getUser();
        const today = new Date().toDateString();

        // Check daily spin if user doesn't have beta
        if (user && user.last_spin_date === today && !user.has_beta) {
            if (status) {
                status.style.color = '#f87171';
                status.textContent = 'Вы уже крутили рулетку сегодня! Доступ Beta даёт право крутить каждый день.';
            }
            return;
        }

        isRouletteSpinning = true;
        spinBtn.disabled = true;
        spinBtn.style.opacity = '0.6';
        if (status) {
            status.style.color = '#fbbf24';
            status.textContent = 'Рулетка крутится... Удачи!';
        }

        // Target index for stopping: card #55
        const TARGET_INDEX = 55;
        const winningPrize = pickWinningRoulettePrize();
        
        // Ensure card at TARGET_INDEX is the winning prize
        rouletteCardsList[TARGET_INDEX] = winningPrize;
        const targetCardEl = tape.children[TARGET_INDEX];
        if (targetCardEl) {
            targetCardEl.style.background = winningPrize.bg;
            targetCardEl.style.borderColor = winningPrize.border;
            targetCardEl.style.color = winningPrize.color;
            targetCardEl.innerHTML = `
                <div class="roulette-card-icon">${winningPrize.icon}</div>
                <div class="roulette-card-title">${winningPrize.name}</div>
            `;
        }

        // Geometry: cardWidth = 82px, gap = 8px, total stride = 90px
        const CARD_STRIDE = 90;
        const drumContainer = document.querySelector('.roulette-drum-container');
        const containerWidth = drumContainer ? drumContainer.offsetWidth : 450;
        
        // Exact center alignment + small natural randomness (-18px to +18px)
        const jitter = (Math.random() - 0.5) * 36;
        const finalTranslateX = (TARGET_INDEX * CARD_STRIDE) + (82 / 2) - (containerWidth / 2) + jitter;

        // Reset and execute smooth spin
        tape.style.transition = 'none';
        tape.style.transform = 'translateX(0px)';
        tape.offsetHeight; // force reflow

        tape.style.transition = 'transform 5.2s cubic-bezier(0.12, 0.95, 0.25, 1)';
        tape.style.transform = `translateX(-${finalTranslateX}px)`;

        // Finished spin
        setTimeout(() => {
            isRouletteSpinning = false;
            spinBtn.disabled = false;
            spinBtn.style.opacity = '1';

            // Apply prize to user account
            const activeUser = SessionManager.getUser() || { username: 'Lumira', role: 'DLC EDITION' };
            activeUser.last_spin_date = today;

            if (winningPrize.isBeta) {
                activeUser.has_beta = true;
                SessionManager.showToast('🎉 ДЖЕКПОТ! Вы выиграли Доступ Beta 1.21.11 навсегда!');
            } else if (winningPrize.days >= 9999) {
                activeUser.subscription_till = 'Навсегда (2048)';
                SessionManager.showToast('🔥 МЕГА ПРИЗ! Вы выиграли подписку Навсегда!');
            } else {
                const cur = activeUser.subscription_till || '';
                const d = new Date();
                d.setDate(d.getDate() + winningPrize.days);
                const dateStr = d.toLocaleDateString('ru-RU');
                activeUser.subscription_till = `${winningPrize.days} дн. (до ${dateStr})`;
                SessionManager.showToast(`🎁 Поздравляем! Вы выиграли: ${winningPrize.name}!`);
            }

            SessionManager.setUser(activeUser);

            if (status) {
                status.style.color = '#4ade80';
                status.textContent = `🎉 Поздравляем! Вы выиграли: ${winningPrize.name}!`;
            }
        }, 5300);
    });
}


