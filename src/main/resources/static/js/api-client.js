import { CONFIG } from './config.js';
import toastManager from './toast.js';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem(CONFIG.TOKEN_STORAGE_KEY);
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem(CONFIG.TOKEN_STORAGE_KEY, token);
    }

    getToken() {
        return this.token || localStorage.getItem(CONFIG.TOKEN_STORAGE_KEY);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem(CONFIG.TOKEN_STORAGE_KEY);
    }

    async request(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint}`, {
                ...options,
                headers,
            });

            if (response.status === 429) {
                const retryAfter = response.headers.get('Retry-After') || 60;
                window.dispatchEvent(new CustomEvent('rate-limit-triggered', { 
                    detail: { retryAfter: parseInt(retryAfter) } 
                }));
                throw new Error('Rate limit exceeded. Please wait before retrying.');
            }

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'An error occurred' }));
                throw new Error(error.message || error.detail || 'Request failed');
            }

            return response.json();
        } catch (error) {
            if (error.message.includes('Rate limit')) {
                throw error;
            }
            toastManager.error('API Error', error.message);
            throw error;
        }
    }

    async login(data) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async register(data) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async updateProfile(data) {
        return this.request('/auth/profile', {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    async uploadImage(formData) {
        const headers = {};
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const response = await fetch(`${CONFIG.API_BASE_URL}/admin/products/upload`, {
            method: 'POST',
            headers,
            body: formData,
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'Upload failed' }));
            throw new Error(error.message || error.detail || 'Upload failed');
        }

        return response.text();
    }

    async getProducts(params = {}) {
        const searchParams = new URLSearchParams();
        if (params.page) searchParams.append('page', params.page);
        if (params.size) searchParams.append('size', params.size);
        if (params.category) searchParams.append('category', params.category);
        if (params.inStockOnly) searchParams.append('inStockOnly', 'true');

        const queryString = searchParams.toString();
        return this.request(`/products${queryString ? `?${queryString}` : ''}`);
    }

    async getProduct(id) {
        return this.request(`/products/${id}`);
    }

    async searchProducts(query) {
        return this.request(`/products/search?q=${encodeURIComponent(query)}`);
    }

    async checkout(data) {
        return this.request('/orders/checkout', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async getOrders() {
        return this.request('/orders');
    }

    async getOrder(id) {
        return this.request(`/orders/${id}`);
    }

    async createProduct(data) {
        return this.request('/admin/products', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async updateProduct(id, data) {
        return this.request(`/admin/products/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    async deleteProduct(id) {
        return this.request(`/admin/products/${id}`, {
            method: 'DELETE',
        });
    }

    async getAdminStats() {
        return this.request('/admin/products/stats');
    }
}

const apiClient = new ApiClient();
export default apiClient;
