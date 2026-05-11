package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.ServiceRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.ServiceEntityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceEntityServiceImpl implements ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;

    public ServiceEntityServiceImpl(
            ServiceRepository serviceRepository,
            NotificationService notificationService) {
        this.serviceRepository = serviceRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ServiceEntity save(ServiceEntity serviceEntity) {
        validateServiceData(serviceEntity);

        serviceEntity.setActive(true);

        ServiceEntity saved = serviceRepository.save(serviceEntity);

        notificationService.createForRole(
                "Servicio registrado",
                "Se registró el servicio " + saved.getName()
                        + " con costo S/ " + saved.getPrice()
                        + " y duración de " + saved.getDurationMinutes() + " minutos.",
                "SERVICIO_CREADO",
                "ADMIN"
        );

        return saved;
    }

    @Override
    public List<ServiceEntity> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public List<ServiceEntity> findActiveServices() {
        return serviceRepository.findByActiveTrue();
    }

    @Override
    public ServiceEntity findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));
    }

    @Override
    public ServiceEntity update(Long id, ServiceEntity serviceEntity) {
        validateServiceData(serviceEntity);

        ServiceEntity currentService = findById(id);

        currentService.setName(serviceEntity.getName());
        currentService.setDescription(serviceEntity.getDescription());
        currentService.setPrice(serviceEntity.getPrice());
        currentService.setDurationMinutes(serviceEntity.getDurationMinutes());

        if (serviceEntity.getActive() != null) {
            currentService.setActive(serviceEntity.getActive());
        }

        ServiceEntity updated = serviceRepository.save(currentService);

        notificationService.createForRole(
                "Servicio actualizado",
                "Se actualizaron los datos del servicio " + updated.getName() + ".",
                "SERVICIO_EDITADO",
                "ADMIN"
        );

        return updated;
    }

    @Override
    public ServiceEntity toggleActive(Long id) {
        ServiceEntity serviceEntity = findById(id);
        serviceEntity.setActive(!Boolean.TRUE.equals(serviceEntity.getActive()));

        ServiceEntity updated = serviceRepository.save(serviceEntity);

        String status = Boolean.TRUE.equals(updated.getActive()) ? "reactivado" : "desactivado";

        notificationService.createForRole(
                "Estado de servicio modificado",
                "El servicio " + updated.getName() + " fue " + status + ".",
                "SERVICIO_ESTADO",
                "ADMIN"
        );

        return updated;
    }

    @Override
    public void delete(Long id) {
        ServiceEntity serviceEntity = findById(id);
        serviceEntity.setActive(false);

        serviceRepository.save(serviceEntity);

        notificationService.createForRole(
                "Servicio desactivado",
                "El servicio " + serviceEntity.getName() + " fue desactivado.",
                "SERVICIO_ELIMINADO",
                "ADMIN"
        );
    }

    private void validateServiceData(ServiceEntity serviceEntity) {
        if (serviceEntity.getName() == null || serviceEntity.getName().trim().isEmpty()) {
            throw new BusinessRuleException("El nombre del servicio es obligatorio");
        }

        if (serviceEntity.getPrice() == null || serviceEntity.getPrice() < 0) {
            throw new BusinessRuleException("El costo del servicio debe ser válido");
        }

        if (serviceEntity.getDurationMinutes() == null || serviceEntity.getDurationMinutes() <= 0) {
            throw new BusinessRuleException("La duración del servicio debe ser mayor a cero");
        }
    }
}