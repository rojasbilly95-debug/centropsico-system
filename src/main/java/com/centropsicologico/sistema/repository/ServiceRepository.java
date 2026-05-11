package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByActiveTrue();
}