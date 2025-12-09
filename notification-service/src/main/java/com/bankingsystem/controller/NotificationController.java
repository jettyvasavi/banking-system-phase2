package com.bankingsystem.controller;

import com.bankingsystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;


    @PostMapping("/send")
    public String sendNotification(@RequestBody String message) {
        notificationService.sendNotification(message);
        return "Notification Logged Successfully";
    }
}