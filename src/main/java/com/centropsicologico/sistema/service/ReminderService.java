package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.dto.ReminderDTO;

import java.util.List;

public interface ReminderService {
    List<ReminderDTO> getCurrentUserReminders(String email);
}