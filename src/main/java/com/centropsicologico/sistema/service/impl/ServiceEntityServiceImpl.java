package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.ServiceEntity;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.ServiceRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.ServiceEntityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceEntityServiceImpl implements ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ServiceEntityServiceImpl(
            ServiceRepository serviceRepository,
            NotificationService notificationService,
            AuditLogService auditLogService) {

        this.serviceRepository = serviceRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    public ServiceEntity save(ServiceEntity serviceEntity) {
        validateServiceData(serviceEntity);

        serviceEntity.setName(clean(serviceEntity.getName()));
        serviceEntity.setDescription(clean(serviceEntity.getDescription()));
        serviceEntity.setActive(true);

        ServiceEntity saved = serviceRepository.save(serviceEntity);

        notifyAdmin(
                "Servicio registrado",
                "Se registró el servicio " + getServiceName(saved)
                        + " con costo S/ " + formatPrice(saved.getPrice())
                        + " y duración de " + saved.getDurationMinutes() + " minutos.",
                "SERVICIO_CREADO"
        );

        auditLogService.record(
                "SERVICIOS",
                "REGISTRO DE SERVICIO",
                "ServiceEntity",
                saved.getId(),
                "Se registró el servicio "
                        + getServiceName(saved)
                        + " con costo S/ "
                        + formatPrice(saved.getPrice())
                        + " y duración de "
                        + saved.getDurationMinutes()
                        + " minutos"
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

        currentService.setName(clean(serviceEntity.getName()));
        currentService.setDescription(clean(serviceEntity.getDescription()));
        currentService.setPrice(serviceEntity.getPrice());
        currentService.setDurationMinutes(serviceEntity.getDurationMinutes());

        if (serviceEntity.getActive() != null) {
            currentService.setActive(serviceEntity.getActive());
        }

        ServiceEntity updated = serviceRepository.save(currentService);

        notifyAdmin(
                "Servicio actualizado",
                "Se actualizaron los datos del servicio " + getServiceName(updated)
                        + ". Costo actual: S/ " + formatPrice(updated.getPrice())
                        + ". Duración: " + updated.getDurationMinutes() + " minutos.",
                "SERVICIO_EDITADO"
        );

        auditLogService.record(
                "SERVICIOS",
                "ACTUALIZACIÓN DE SERVICIO",
                "ServiceEntity",
                updated.getId(),
                "Se actualizaron los datos del servicio "
                        + getServiceName(updated)
                        + ". Costo actual: S/ "
                        + formatPrice(updated.getPrice())
                        + ". Duración: "
                        + updated.getDurationMinutes()
                        + " minutos"
        );

        return updated;
    }

    @Override
    public ServiceEntity toggleActive(Long id) {
        ServiceEntity serviceEntity = findById(id);
        serviceEntity.setActive(!Boolean.TRUE.equals(serviceEntity.getActive()));

        ServiceEntity updated = serviceRepository.save(serviceEntity);

        String status = Boolean.TRUE.equals(updated.getActive())
                ? "reactivado"
                : "desactivado";

        notifyAdmin(
                "Estado de servicio modificado",
                "El servicio " + getServiceName(updated) + " fue " + status + ".",
                "SERVICIO_ESTADO"
        );

        auditLogService.record(
                "SERVICIOS",
                "CAMBIO DE ESTADO DE SERVICIO",
                "ServiceEntity",
                updated.getId(),
                "El servicio "
                        + getServiceName(updated)
                        + " fue "
                        + status
        );

        return updated;
    }

    @Override
    public void delete(Long id) {
        ServiceEntity serviceEntity = findById(id);
        serviceEntity.setActive(false);

        ServiceEntity updated = serviceRepository.save(serviceEntity);

        notifyAdmin(
                "Servicio desactivado",
                "El servicio " + getServiceName(updated) + " fue desactivado.",
                "SERVICIO_ELIMINADO"
        );

        auditLogService.record(
                "SERVICIOS",
                "DESACTIVACIÓN DE SERVICIO",
                "ServiceEntity",
                updated.getId(),
                "El servicio "
                        + getServiceName(updated)
                        + " fue desactivado"
        );
    }

    private void validateServiceData(ServiceEntity serviceEntity) {
        if (serviceEntity == null) {
            throw new BusinessRuleException("Los datos del servicio son obligatorios");
        }

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

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String getServiceName(ServiceEntity serviceEntity) {
        if (serviceEntity == null || serviceEntity.getName() == null || serviceEntity.getName().trim().isEmpty()) {
            return "Servicio";
        }

        return serviceEntity.getName().trim();
    }

    private String formatPrice(Double price) {
        if (price == null) {
            return "0.00";
        }

        return String.format("%.2f", price);
    }

    private void notifyAdmin(String title, String message, String type) {
        notificationService.createForRole(
                title,
                message,
                type,
                "ADMIN"
        );
    }
}