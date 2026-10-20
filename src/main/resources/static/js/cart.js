import { CONFIG } from './config.js';
import toastManager from './toast.js';

class CartManager {
    constructor() {
        this.items = JSON.parse(localStorage.getItem(CONFIG.CART_STORAGE_KEY)) || [];
        this.updateUI();
    }

    addItem(product, quantity = 1) {
        const existingItem = this.items.find(item => item.productId === product.id);
        
        if (existingItem) {
            if (existingItem.quantity + quantity > product.stock) {
                toastManager.warning('Stock Limit', `Only ${product.stock} units available`);
                return false;
            }
            existingItem.quantity += quantity;
        } else {
            if (quantity > product.stock) {
                toastManager.warning('Stock Limit', `Only ${product.stock} units available`);
                return false;
            }
            this.items.push({
                productId: product.id,
                productName: product.name,
                quantity,
                price: product.price,
                imageUrl: product.imageUrl,
                maxStock: product.stock
            });
        }
        
        this.save();
        toastManager.success('Added to Cart', `${product.name} added to your cart`);
        return true;
    }

    removeItem(productId) {
        this.items = this.items.filter(item => item.productId !== productId);
        this.save();
    }

    updateQuantity(productId, quantity) {
        if (quantity <= 0) {
            this.removeItem(productId);
            return;
        }
        
        const item = this.items.find(item => item.productId === productId);
        if (item) {
            if (quantity > item.maxStock) {
                toastManager.warning('Stock Limit', `Only ${item.maxStock} units available`);
                return false;
            }
            item.quantity = quantity;
            this.save();
            return true;
        }
        return false;
    }

    clearCart() {
        this.items = [];
        this.save();
    }

    getTotalItems() {
        return this.items.reduce((total, item) => total + item.quantity, 0);
    }

    getTotalPrice() {
        return this.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    save() {
        localStorage.setItem(CONFIG.CART_STORAGE_KEY, JSON.stringify(this.items));
        this.updateUI();
    }

    updateUI() {
        const cartCount = document.getElementById('cartCount');
        const cartBadge = document.querySelector('.cart-badge');
        
        const count = this.getTotalItems();
        
        if (cartCount) {
            cartCount.textContent = count > 0 ? `(${count})` : '';
        }
        
        if (cartBadge) {
            cartBadge.textContent = count;
            cartBadge.style.display = count > 0 ? 'flex' : 'none';
        }
    }
}

const cartManager = new CartManager();
export default cartManager;
