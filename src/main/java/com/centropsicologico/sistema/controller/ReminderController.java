package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.ReminderDTO;
import com.centropsicologico.sistema.service.ReminderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/me")
    public List<ReminderDTO> getMyReminders(Authentication authentication) {
        return reminderService.getCurrentUserReminders(authentication.getName());
    }
}