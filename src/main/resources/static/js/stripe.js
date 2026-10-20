import { CONFIG } from './config.js';
import apiClient from './api-client.js';
import toastManager from './toast.js';

class StripeManager {
    constructor() {
        this.stripe = null;
        this.elements = null;
        this.cardElement = null;
        this.clientSecret = null;
    }

    init() {
        if (typeof Stripe === 'undefined') {
            console.error('Stripe.js not loaded');
            return;
        }

        this.stripe = Stripe(CONFIG.STRIPE_PUBLISHABLE_KEY);
    }

    async createPaymentIntent(items) {
        try {
            const response = await apiClient.checkout({ items });
            this.clientSecret = response.clientSecret;
            return response;
        } catch (error) {
            toastManager.error('Payment Error', error.message);
            throw error;
        }
    }

    mountCardElement(elementId) {
        if (!this.stripe) {
            console.error('Stripe not initialized');
            toastManager.error('Payment Error', 'Stripe not initialized');
            return;
        }

        if (!this.clientSecret) {
            console.error('No client secret available');
            toastManager.error('Payment Error', 'Payment intent not created');
            return;
        }

        const appearance = {
            theme: 'night',
            variables: {
                colorBackground: '#050813',
                colorText: '#ffffff',
                colorDanger: '#ff0055',
                fontFamily: 'Plus Jakarta Sans, sans-serif',
                fontSizeBase: '16px'
            }
        };

        this.elements = this.stripe.elements({ 
            clientSecret: this.clientSecret,
            appearance 
        });

        const cardElement = this.elements.create('payment', {
            layout: 'tabs'
        });

        const element = document.getElementById(elementId);
        if (!element) {
            console.error(`Element with id '${elementId}' not found`);
            toastManager.error('Payment Error', 'Card element container not found');
            return;
        }

        cardElement.mount(`#${elementId}`);
        this.cardElement = cardElement;

        cardElement.on('change', (event) => {
            const displayError = document.getElementById('card-errors');
            if (event.error) {
                displayError.textContent = event.error.message;
                displayError.style.display = 'block';
            } else {
                displayError.textContent = '';
                displayError.style.display = 'none';
            }
        });

        console.log('Card element mounted successfully');
    }

    async confirmPayment() {
        if (!this.stripe || !this.elements) {
            toastManager.error('Payment Error', 'Payment system not initialized');
            return { success: false };
        }

        const { error, paymentIntent } = await this.stripe.confirmPayment({
            elements: this.elements,
            confirmParams: {
                return_url: window.location.origin + '/checkout-success.html',
            },
            redirect: 'if_required'
        });

        if (error) {
            toastManager.error('Payment Failed', error.message);
            return { success: false, error };
        }

        if (paymentIntent && paymentIntent.status === 'succeeded') {
            toastManager.success('Payment Successful', 'Your order has been placed');
            return { success: true, paymentIntent };
        }

        return { success: false };
    }

    async processPayment(amount, checkoutRequest) {
        try {
            this.init();
            
            const response = await this.createPaymentIntent(checkoutRequest.items);
            this.clientSecret = response.clientSecret;
            
            // Mount card element if not already mounted
            if (!this.elements || !this.cardElement) {
                this.mountCardElement('card-element');
            }
            
            const { error, paymentIntent } = await this.stripe.confirmPayment({
                elements: this.elements,
                confirmParams: {
                    return_url: window.location.origin + '/checkout-success.html',
                },
                redirect: 'if_required'
            });

            if (error) {
                toastManager.error('Payment Failed', error.message);
                throw new Error(error.message);
            }

            if (paymentIntent && paymentIntent.status === 'succeeded') {
                toastManager.success('Payment Successful', 'Your order has been placed');
                return { success: true, paymentIntent };
            }

            return { success: false };
        } catch (error) {
            toastManager.error('Payment Error', error.message);
            throw error;
        }
    }

    async handleCardPayment(clientSecret, cardElement) {
        if (!this.stripe) {
            toastManager.error('Payment Error', 'Stripe not initialized');
            return { success: false };
        }

        const { error, paymentIntent } = await this.stripe.confirmCardPayment(clientSecret, {
            payment_method: {
                card: cardElement,
            }
        });

        if (error) {
            toastManager.error('Payment Failed', error.message);
            return { success: false, error };
        }

        return { success: true, paymentIntent };
    }
}

const stripeManager = new StripeManager();
export default stripeManager;
