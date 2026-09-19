/* RublinMart Auth Client */
const API_BASE = '/rublinmart/api/v1';

async function checkAuth() {
    try {
        const res = await fetch(`${API_BASE}/auth/me`);

        if (res.status === 401 || res.status === 403) {
            updateNavbarUser(null);
            return null;
        }

        const json = await res.json();
        if (json.success && json.data) {
            updateNavbarUser(json.data);
            return json.data;
        }
    } catch (e) {
        // Ignore expected unauthenticated state and transient fetch issues.
    }
    updateNavbarUser(null);
    return null;
}

function updateNavbarUser(user) {
    const navLinks = document.getElementById('nav-links');
    if (!navLinks) return;

    if (user) {
        let roleDashboard = '';
        if (user.role === 'SELLER') {
            roleDashboard = `<li><a href="/rublinmart/pages/seller-dashboard.html"><i class="fas fa-store"></i> Seller Studio</a></li>`;
        } else if (user.role === 'ADMIN') {
            roleDashboard = `<li><a href="/rublinmart/pages/admin-dashboard.html"><i class="fas fa-user-shield"></i> Admin Panel</a></li>`;
        } else {
            roleDashboard = `<li><a href="/rublinmart/pages/orders.html"><i class="fas fa-box"></i> My Orders</a></li>`;
        }

        navLinks.innerHTML = `
            <li><a href="/rublinmart/pages/index.html">Home</a></li>
            <li><a href="/rublinmart/pages/products.html">Browse Products</a></li>
            ${roleDashboard}
            <li>
                <a href="/rublinmart/pages/cart.html" class="cart-icon-btn">
                    <i class="fas fa-shopping-cart"></i> Cart
                    <span id="cart-badge-count" class="cart-badge">0</span>
                </a>
            </li>
            <li><span style="font-weight:600; color:var(--primary);">Hello, ${user.name}</span></li>
            <li><button onclick="handleLogout()" class="btn btn-sm btn-secondary">Logout</button></li>
        `;
        updateCartBadge();
    } else {
        navLinks.innerHTML = `
            <li><a href="/rublinmart/pages/index.html">Home</a></li>
            <li><a href="/rublinmart/pages/products.html">Browse Products</a></li>
            <li><a href="/rublinmart/pages/login.html" class="btn btn-sm btn-outline">Login</a></li>
            <li><a href="/rublinmart/pages/register.html" class="btn btn-sm btn-primary">Register</a></li>
        `;
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const errorAlert = document.getElementById('auth-error');

    if (errorAlert) errorAlert.style.display = 'none';

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const json = await res.json();

        if (json.success) {
            const user = json.data;
            if (user.role === 'ADMIN') {
                window.location.href = '/rublinmart/pages/admin-dashboard.html';
            } else if (user.role === 'SELLER') {
                window.location.href = '/rublinmart/pages/seller-dashboard.html';
            } else {
                window.location.href = '/rublinmart/pages/products.html';
            }
        } else {
            if (errorAlert) {
                errorAlert.textContent = json.error ? json.error.message : 'Invalid credentials';
                errorAlert.style.display = 'block';
            }
        }
    } catch (e) {
        if (errorAlert) {
            errorAlert.textContent = 'Server connection error. Please try again.';
            errorAlert.style.display = 'block';
        }
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const confirmPassword = document.getElementById('reg-confirm-password').value;
    const roleRadio = document.querySelector('input[name="role"]:checked');
    const role = roleRadio ? roleRadio.value : 'BUYER';

    const errorAlert = document.getElementById('auth-error');
    if (errorAlert) errorAlert.style.display = 'none';

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, confirmPassword, role })
        });
        const json = await res.json();

        if (json.success) {
            alert('Account created successfully! Please login.');
            window.location.href = '/rublinmart/pages/login.html';
        } else {
            if (errorAlert) {
                errorAlert.textContent = json.error ? json.error.message : 'Registration failed';
                errorAlert.style.display = 'block';
            }
        }
    } catch (e) {
        if (errorAlert) {
            errorAlert.textContent = 'Server connection error. Please try again.';
            errorAlert.style.display = 'block';
        }
    }
}

async function handleLogout() {
    try {
        await fetch(`${API_BASE}/auth/logout`, { method: 'POST' });
        window.location.href = '/rublinmart/pages/index.html';
    } catch (e) {
        window.location.href = '/rublinmart/pages/login.html';
    }
}

document.addEventListener('DOMContentLoaded', checkAuth);
