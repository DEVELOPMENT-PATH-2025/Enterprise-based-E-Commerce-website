package com.nexusmart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotent_webhook_events")
public class WebhookEvent {
    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String status;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public WebhookEvent() {
        this.processedAt = LocalDateTime.now();
    }

    public WebhookEvent(String eventId, String eventType, String status) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
