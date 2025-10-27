package com.telconova.suportsuite.controller;


import com.telconova.suportsuite.DTO.CreateNotificationRequest;
import com.telconova.suportsuite.DTO.NotificationDTO;
import com.telconova.suportsuite.DTO.NotificationStatusDTO;
import com.telconova.suportsuite.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://telco-nova-p7-f4-front.vercel.app")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor // Genera el constructor para el campo 'final'
public class NotificationsController {

    private final NotificationService notificationService;

    // POST /api/v1/notifications
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationDTO notification = notificationService.createNotification(request);
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }



    // GET /api/v1/notifications/stats
    @GetMapping("/stats")
    public ResponseEntity<NotificationStatusDTO> getEstadisticas(){
        NotificationStatusDTO stast = notificationService.getEstadisticas();
        return ResponseEntity.ok(stast);
    }

}
