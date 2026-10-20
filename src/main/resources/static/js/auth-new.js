import { CONFIG } from './config.js';
import apiClient from './api.js';

class AuthManager {
    constructor() {
        this.user = JSON.parse(localStorage.getItem(CONFIG.USER_STORAGE_KEY)) || null;
        this.updateUI();
    }

    setUser(user, token) {
        this.user = user;
        localStorage.setItem(CONFIG.USER_STORAGE_KEY, JSON.stringify(user));
        apiClient.setToken(token);
        this.updateUI();
    }

    logout() {
        this.user = null;
        localStorage.removeItem(CONFIG.USER_STORAGE_KEY);
        localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
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

    getUser() {
        return this.user;
    }

    updateUI() {
        const authLinks = document.getElementById('authLinks');
        if (!authLinks) return;

        if (this.user) {
            const isAdmin = this.isAdmin();
            authLinks.innerHTML = `
                <a href="/cart.html" class="navbar-link">
                    <div class="cart-icon-wrapper">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
                        <span class="cart-badge" style="display: none;">0</span>
                    </div>
                </a>
                <a href="/account.html" class="navbar-link">${this.user.name}</a>
                ${isAdmin ? '<a href="/admin.html" class="navbar-link">Admin</a>' : ''}
                <button onclick="authManager.logout()" class="cyber-button-secondary">Logout</button>
            `;
        } else {
            authLinks.innerHTML = `
                <a href="/login.html" class="navbar-link">Sign In</a>
                <a href="/register.html" class="cyber-button">Register</a>
            `;
        }
    }
}

const authManager = new AuthManager();
export default authManager;
