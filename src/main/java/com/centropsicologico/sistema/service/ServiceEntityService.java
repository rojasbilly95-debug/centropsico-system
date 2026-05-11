package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.ServiceEntity;

import java.util.List;

public interface ServiceEntityService {

    ServiceEntity save(ServiceEntity serviceEntity);

    List<ServiceEntity> findAll();

    List<ServiceEntity> findActiveServices();

    ServiceEntity findById(Long id);

    ServiceEntity update(Long id, ServiceEntity serviceEntity);

    ServiceEntity toggleActive(Long id);

    void delete(Long id);
}