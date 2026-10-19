class CartManager {
    constructor() {
        this.items = JSON.parse(localStorage.getItem('cart')) || [];
        this.updateUI();
    }

    addItem(product, quantity = 1) {
        const existingItem = this.items.find(item => item.productId === product.id);
        
        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            this.items.push({
                productId: product.id,
                productName: product.name,
                quantity,
                price: product.price,
                imageUrl: product.imageUrl
            });
        }
        
        this.save();
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
            item.quantity = quantity;
            this.save();
        }
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
        localStorage.setItem('cart', JSON.stringify(this.items));
        this.updateUI();
    }

    updateUI() {
        const cartCount = document.getElementById('cartCount');
        if (cartCount) {
            const count = this.getTotalItems();
            cartCount.textContent = count > 0 ? `(${count})` : '';
        }
    }
}

const cartManager = new CartManager();
