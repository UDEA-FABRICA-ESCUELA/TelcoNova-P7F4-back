package com.telconova.suportsuite.DTO;

import com.telconova.suportsuite.entity.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertRuleAuditDto {

    private Long id;
    private Long ruleId;
    private String ruleName;
    private String action;
    private String performedBy; //  Quién
    private LocalDateTime timestamp;
    private String changes; // Qué cambió
    private String ipAddress;
}
