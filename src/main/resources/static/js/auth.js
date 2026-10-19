class AuthManager {
    constructor() {
        this.user = JSON.parse(localStorage.getItem('user')) || null;
        this.updateUI();
    }

    setUser(user) {
        this.user = user;
        localStorage.setItem('user', JSON.stringify(user));
        this.updateUI();
    }

    logout() {
        this.user = null;
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        apiClient.clearToken();
        this.updateUI();
        window.location.href = '/index.html';
    }

    isAuthenticated() {
        return !!this.user;
    }

    isAdmin() {
        return this.user && this.user.role === 'ROLE_ADMIN';
    }

    updateUI() {
        const authLinks = document.getElementById('authLinks');
        if (!authLinks) return;

        if (this.user) {
            authLinks.innerHTML = `
                <a href="/cart.html">
                    Cart <span id="cartCount"></span>
                </a>
                <a href="/account.html">${this.user.name}</a>
                <button onclick="authManager.logout()" class="btn btn-secondary">Logout</button>
            `;
        } else {
            authLinks.innerHTML = `
                <a href="/login.html">Sign In</a>
                <a href="/register.html" class="btn btn-primary">Register</a>
            `;
        }
    }
}

const authManager = new AuthManager();
