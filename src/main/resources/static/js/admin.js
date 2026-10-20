import apiClient from './api.js';
import toastManager from './toast.js';

class AdminManager {
    constructor() {
        this.products = [];
        this.stats = {};
    }

    async loadStats() {
        try {
            this.stats = await apiClient.getAdminStats();
            this.renderStats();
        } catch (error) {
            console.error('Failed to load stats:', error);
        }
    }

    async loadProducts() {
        try {
            const response = await apiClient.getProducts();
            this.products = response.content || response;
            this.renderProducts();
        } catch (error) {
            console.error('Failed to load products:', error);
        }
    }

    renderStats() {
        const revenueEl = document.getElementById('totalRevenue');
        const ordersEl = document.getElementById('totalOrders');
        const lowStockEl = document.getElementById('lowStockCount');

        if (revenueEl) {
            revenueEl.textContent = `$${(this.stats.totalRevenue || 0).toFixed(2)}`;
        }
        if (ordersEl) {
            ordersEl.textContent = this.stats.totalOrders || 0;
        }
        if (lowStockEl) {
            lowStockEl.textContent = this.stats.lowStockCount || 0;
        }
    }

    renderProducts() {
        const container = document.getElementById('productsTableBody');
        if (!container) return;

        container.innerHTML = this.products.map(product => `
            <tr class="product-row">
                <td>
                    <div class="product-thumbnail">
                        <img src="${product.imageUrl}" alt="${product.name}" />
                    </div>
                </td>
                <td>
                    <div class="font-mono">${product.id.substring(0, 8)}...</div>
                </td>
                <td>${product.name}</td>
                <td class="font-mono">$${product.price.toFixed(2)}</td>
                <td>
                    <span class="stock-badge ${this.getStockClass(product.stock)}">
                        ${product.stock} units
                    </span>
                </td>
                <td>${product.category || 'N/A'}</td>
                <td>
                    <div class="action-buttons">
                        <button onclick="adminManager.editProduct('${product.id}')" class="cyber-button-secondary" style="padding: 0.5rem;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>
                        </button>
                        <button onclick="adminManager.deleteProduct('${product.id}')" class="cyber-button-danger" style="padding: 0.5rem;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    getStockClass(stock) {
        if (stock === 0) return 'stock-depleted';
        if (stock < 5) return 'stock-low';
        return 'stock-optimal';
    }

    async createProduct(data) {
        try {
            await apiClient.createProduct(data);
            toastManager.success('Product Created', 'Product has been added to the catalog');
            await this.loadProducts();
            await this.loadStats();
            this.closeModal();
        } catch (error) {
            toastManager.error('Creation Failed', error.message);
        }
    }

    async updateProduct(id, data) {
        try {
            await apiClient.updateProduct(id, data);
            toastManager.success('Product Updated', 'Product has been updated successfully');
            await this.loadProducts();
            await this.loadStats();
            this.closeModal();
        } catch (error) {
            toastManager.error('Update Failed', error.message);
        }
    }

    async deleteProduct(id) {
        if (!confirm('Are you sure you want to delete this product?')) return;
        
        try {
            await apiClient.deleteProduct(id);
            toastManager.success('Product Deleted', 'Product has been removed from the catalog');
            await this.loadProducts();
            await this.loadStats();
        } catch (error) {
            toastManager.error('Deletion Failed', error.message);
        }
    }

    editProduct(id) {
        const product = this.products.find(p => p.id === id);
        if (!product) return;

        const modal = document.getElementById('productModal');
        if (modal) {
            document.getElementById('modalTitle').textContent = 'Edit Product';
            document.getElementById('productId').value = product.id;
            document.getElementById('productName').value = product.name;
            document.getElementById('productDescription').value = product.description || '';
            document.getElementById('productPrice').value = product.price;
            document.getElementById('productStock').value = product.stock;
            document.getElementById('productCategory').value = product.category || '';
            document.getElementById('productImageUrl').value = product.imageUrl || '';
            modal.classList.add('active');
        }
    }

    openCreateModal() {
        const modal = document.getElementById('productModal');
        if (modal) {
            document.getElementById('modalTitle').textContent = 'Add New Product';
            document.getElementById('productId').value = '';
            document.getElementById('productName').value = '';
            document.getElementById('productDescription').value = '';
            document.getElementById('productPrice').value = '';
            document.getElementById('productStock').value = '';
            document.getElementById('productCategory').value = '';
            document.getElementById('productImageUrl').value = '';
            modal.classList.add('active');
        }
    }

    closeModal() {
        const modal = document.getElementById('productModal');
        if (modal) {
            modal.classList.remove('active');
        }
    }

    handleFormSubmit(event) {
        event.preventDefault();
        
        const id = document.getElementById('productId').value;
        const data = {
            name: document.getElementById('productName').value,
            description: document.getElementById('productDescription').value,
            price: parseFloat(document.getElementById('productPrice').value),
            stock: parseInt(document.getElementById('productStock').value),
            category: document.getElementById('productCategory').value,
            imageUrl: document.getElementById('productImageUrl').value
        };

        if (id) {
            this.updateProduct(id, data);
        } else {
            this.createProduct(data);
        }
    }
}

const adminManager = new AdminManager();
export default adminManager;
