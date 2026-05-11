package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTargetRoleAndActiveTrueOrderByCreatedAtDesc(String targetRole);

    List<Notification> findByTargetEmailAndActiveTrueOrderByCreatedAtDesc(String targetEmail);

    Long countByTargetRoleAndReadFalseAndActiveTrue(String targetRole);

    Long countByTargetEmailAndReadFalseAndActiveTrue(String targetEmail);
}