package com.telconova.suportsuite.entity;

import jakarta.persistence.*;

@Entity
@Table(name ="notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String message;

    @Column(nullable = false)
    private String recipient;

    @Enumerated (EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated (EnumType.STRING)
    private NotificationStatus status;



    @Column (name = "reintentos_count")
    private Integer maxReintentos = 3;

    @Column (name = "error_menssage" )
    private String errorMenssage;

    @ManyToOne
    @JoinColumn(name = "alert_rule_id")
    private  AlertRule alertRule;

    public Notification() {

    }

    public Notification(Long id, String message, String recipient, NotificationChannel channel, NotificationStatus status, Integer maxReintentos, String errorMenssage, AlertRule alertRule) {
        this.id = id;
        this.message = message;
        this.recipient = recipient;
        this.channel = channel;
        this.status = status;
        this.maxReintentos = maxReintentos;
        this.errorMenssage = errorMenssage;
        this.alertRule = alertRule;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Integer getMaxReintentos() {
        return maxReintentos;
    }

    public void setMaxReintentos(Integer maxReintentos) {
        this.maxReintentos = maxReintentos;
    }

    public String getErrorMenssage() {
        return errorMenssage;
    }

    public void setErrorMenssage(String errorMenssage) {
        this.errorMenssage = errorMenssage;
    }

    public AlertRule getAlertRule() {
        return alertRule;
    }

    public void setAlertRule(AlertRule alertRule) {
        this.alertRule = alertRule;
    }

    public enum NotificationStatus{
        PENDING,
        PROCESSING,
        SENT,
        FAILD

    }
    public enum NotificationChannel{
        EMAIL,
        SMS,
        WHATSAPP,
        PUSH

    }
}
