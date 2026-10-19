package com.nexusmart.api.service;

import com.nexusmart.api.entity.WebhookEvent;
import com.nexusmart.api.repository.WebhookEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripeWebhookService {

    private final WebhookEventRepository webhookEventRepository;

    public StripeWebhookService(WebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = webhookEventRepository;
    }

    @Transactional
    public void handleWebhook(String eventType, String eventData) {
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setEventType(eventType);
        webhookEvent.setStatus("PENDING");
        webhookEventRepository.save(webhookEvent);

        switch (eventType) {
            case "checkout.session.completed":
                handleCheckoutCompleted(eventData);
                break;
            case "payment_intent.succeeded":
                handlePaymentSucceeded(eventData);
                break;
            default:
                break;
        }
    }

    private void handleCheckoutCompleted(String eventData) {
        // Handle checkout completed logic
    }

    private void handlePaymentSucceeded(String eventData) {
        // Handle payment success logic
    }
}
