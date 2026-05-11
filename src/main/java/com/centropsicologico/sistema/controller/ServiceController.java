package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.service.ServiceEntityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceEntityService serviceEntityService;

    public ServiceController(ServiceEntityService serviceEntityService) {
        this.serviceEntityService = serviceEntityService;
    }

    @PostMapping
    public ServiceEntity save(@RequestBody ServiceEntity serviceEntity) {
        return serviceEntityService.save(serviceEntity);
    }

    @GetMapping
    public List<ServiceEntity> findAll() {
        return serviceEntityService.findAll();
    }

    @GetMapping("/active")
    public List<ServiceEntity> findActiveServices() {
        return serviceEntityService.findActiveServices();
    }

    @GetMapping("/{id}")
    public ServiceEntity findById(@PathVariable Long id) {
        return serviceEntityService.findById(id);
    }

    @PutMapping("/{id}")
    public ServiceEntity update(@PathVariable Long id, @RequestBody ServiceEntity serviceEntity) {
        return serviceEntityService.update(id, serviceEntity);
    }

    @PatchMapping("/{id}/toggle-active")
    public ServiceEntity toggleActive(@PathVariable Long id) {
        return serviceEntityService.toggleActive(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceEntityService.delete(id);
    }
}