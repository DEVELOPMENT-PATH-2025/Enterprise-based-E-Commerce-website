const API_BASE_URL = '/api/v1';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('token', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('token');
    }

    async request(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'An error occurred' }));
            throw new Error(error.message || error.detail || 'Request failed');
        }

        return response.json();
    }

    // Auth endpoints
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

    // Product endpoints (public)
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

    // Order endpoints (authenticated)
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

    // Admin endpoints (authenticated, admin only)
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
