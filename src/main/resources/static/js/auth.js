import { CONFIG } from './config.js';
import apiClient from './api-client.js';

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
        const wasAdmin = this.isAdmin();
        this.user = null;
        localStorage.removeItem(CONFIG.USER_STORAGE_KEY);
        localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
        sessionStorage.clear();
        apiClient.clearToken();
        this.updateUI();
        window.location.replace(wasAdmin ? '/admin-login.html' : '/index.html');
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
            const initials = this.user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
            authLinks.innerHTML = `
                <a href="/cart.html" class="navbar-link">
                    <div class="cart-icon-wrapper">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
                        <span class="cart-badge" style="display: none;">0</span>
                    </div>
                </a>
                ${isAdmin ? '<a href="/admin.html" class="navbar-link">Admin</a>' : ''}
                <div class="relative group" style="margin-left: 0.5rem;">
                    <button class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-slate-200 hover:text-cyan-400 bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 rounded-xl transition">
                        <div class="w-8 h-8 rounded-full bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white font-bold text-xs">
                            ${initials}
                        </div>
                        <span class="hidden sm:inline">${this.user.name}</span>
                        <svg class="w-4 h-4 text-slate-400 group-hover:rotate-180 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                        </svg>
                    </button>
                    <div class="hidden group-hover:block absolute top-full right-0 mt-2 w-48 bg-slate-800/95 backdrop-blur-xl border border-slate-700/80 rounded-xl shadow-2xl p-2 z-50 divide-y divide-slate-700/40">
                        <div class="py-1">
                            <button onclick="window.openEditProfileModal && window.openEditProfileModal()" class="w-full text-left px-3 py-2 text-xs font-semibold text-slate-200 hover:text-cyan-400 hover:bg-slate-700/50 rounded-lg transition flex items-center gap-2">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                                Edit Profile
                            </button>
                            <a href="/account.html" class="w-full text-left px-3 py-2 text-xs font-semibold text-slate-200 hover:text-cyan-400 hover:bg-slate-700/50 rounded-lg transition flex items-center gap-2">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                                My Account
                            </a>
                        </div>
                        <div class="py-1">
                            <button onclick="window.authManager.logout()" class="w-full text-left px-3 py-2 text-xs font-semibold text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition flex items-center gap-2">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
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

// Expose to global window for inline onclick handlers
window.authManager = authManager;

export default authManager;
