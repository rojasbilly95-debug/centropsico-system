package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Psychologist;
import com.centropsicologico.sistema.entity.PsychologistAvailability;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.PsychologistAvailabilityRepository;
import com.centropsicologico.sistema.repository.PsychologistRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.PsychologistAvailabilityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PsychologistAvailabilityServiceImpl implements PsychologistAvailabilityService {

    private final PsychologistAvailabilityRepository availabilityRepository;
    private final PsychologistRepository psychologistRepository;
    private final NotificationService notificationService;

    public PsychologistAvailabilityServiceImpl(
            PsychologistAvailabilityRepository availabilityRepository,
            PsychologistRepository psychologistRepository,
            NotificationService notificationService) {

        this.availabilityRepository = availabilityRepository;
        this.psychologistRepository = psychologistRepository;
        this.notificationService = notificationService;
    }

    @Override
    public PsychologistAvailability save(PsychologistAvailability availability) {
        validateAvailabilityData(availability);

        Psychologist psychologist = findPsychologistOrFail(availability.getPsychologist().getId());

        validateActivePsychologist(psychologist);

        validateAvailabilityOverlap(
                psychologist,
                availability,
                null
        );

        availability.setPsychologist(psychologist);
        availability.setActive(true);

        PsychologistAvailability saved = availabilityRepository.save(availability);

        notifyAdmin(
                "Disponibilidad registrada",
                "Se registró disponibilidad para " + getPsychologistFullName(psychologist)
                        + " el día " + translateDay(saved.getDayOfWeek().name())
                        + " de " + formatTime(saved.getStartTime())
                        + " a " + formatTime(saved.getEndTime()) + ".",
                "DISPONIBILIDAD_CREADA"
        );

        return saved;
    }

    @Override
    public List<PsychologistAvailability> findAll() {
        return availabilityRepository.findAll();
    }

    @Override
    public List<PsychologistAvailability> findActive() {
        return availabilityRepository.findByActiveTrue();
    }

    @Override
    public List<PsychologistAvailability> findByPsychologist(Long psychologistId) {
        return availabilityRepository.findByPsychologistIdAndActiveTrue(psychologistId);
    }

    @Override
    public PsychologistAvailability findById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidad no encontrada con id: " + id));
    }

    @Override
    public PsychologistAvailability update(Long id, PsychologistAvailability availability) {
        validateAvailabilityData(availability);

        PsychologistAvailability currentAvailability = findById(id);

        Psychologist psychologist = findPsychologistOrFail(availability.getPsychologist().getId());

        validateActivePsychologist(psychologist);

        validateAvailabilityOverlap(
                psychologist,
                availability,
                id
        );

        currentAvailability.setPsychologist(psychologist);
        currentAvailability.setDayOfWeek(availability.getDayOfWeek());
        currentAvailability.setStartTime(availability.getStartTime());
        currentAvailability.setEndTime(availability.getEndTime());

        if (availability.getActive() != null) {
            currentAvailability.setActive(availability.getActive());
        }

        PsychologistAvailability updated = availabilityRepository.save(currentAvailability);

        notifyAdmin(
                "Disponibilidad actualizada",
                "Se actualizó disponibilidad de " + getPsychologistFullName(psychologist)
                        + " para " + translateDay(updated.getDayOfWeek().name())
                        + " de " + formatTime(updated.getStartTime())
                        + " a " + formatTime(updated.getEndTime()) + ".",
                "DISPONIBILIDAD_EDITADA"
        );

        return updated;
    }

    @Override
    public PsychologistAvailability toggleActive(Long id) {
        PsychologistAvailability availability = findById(id);
        availability.setActive(!Boolean.TRUE.equals(availability.getActive()));

        PsychologistAvailability updated = availabilityRepository.save(availability);

        String status = Boolean.TRUE.equals(updated.getActive())
                ? "reactivada"
                : "desactivada";

        notifyAdmin(
                "Estado de disponibilidad modificado",
                "La disponibilidad de " + getPsychologistFullName(updated.getPsychologist())
                        + " para " + translateDay(updated.getDayOfWeek().name())
                        + " fue " + status + ".",
                "DISPONIBILIDAD_ESTADO"
        );

        return updated;
    }

    @Override
    public void delete(Long id) {
        PsychologistAvailability availability = findById(id);
        availability.setActive(false);

        PsychologistAvailability updated = availabilityRepository.save(availability);

        notifyAdmin(
                "Disponibilidad desactivada",
                "La disponibilidad de " + getPsychologistFullName(updated.getPsychologist())
                        + " para " + translateDay(updated.getDayOfWeek().name())
                        + " fue desactivada.",
                "DISPONIBILIDAD_ELIMINADA"
        );
    }

    @Override
    public long countActiveByPsychologist(Long psychologistId) {
        return availabilityRepository.countByPsychologistIdAndActiveTrue(psychologistId);
    }

    private void validateAvailabilityData(PsychologistAvailability availability) {
        if (availability == null) {
            throw new BusinessRuleException("Los datos de disponibilidad son obligatorios");
        }

        if (availability.getPsychologist() == null || availability.getPsychologist().getId() == null) {
            throw new BusinessRuleException("Debe seleccionar un psicólogo");
        }

        if (availability.getDayOfWeek() == null) {
            throw new BusinessRuleException("Debe seleccionar un día de la semana");
        }

        if (availability.getStartTime() == null) {
            throw new BusinessRuleException("Debe ingresar la hora de inicio");
        }

        if (availability.getEndTime() == null) {
            throw new BusinessRuleException("Debe ingresar la hora de fin");
        }

        if (!availability.getStartTime().isBefore(availability.getEndTime())) {
            throw new BusinessRuleException("La hora de inicio debe ser menor que la hora de fin");
        }
    }

    private Psychologist findPsychologistOrFail(Long psychologistId) {
        return psychologistRepository.findById(psychologistId)
                .orElseThrow(() -> new ResourceNotFoundException("Psicólogo no encontrado"));
    }

    private void validateActivePsychologist(Psychologist psychologist) {
        if (Boolean.FALSE.equals(psychologist.getActive())) {
            throw new BusinessRuleException("No se puede registrar o actualizar disponibilidad para un psicólogo inactivo");
        }
    }

    private void validateAvailabilityOverlap(
            Psychologist psychologist,
            PsychologistAvailability availability,
            Long currentAvailabilityId) {

        boolean existsOverlap;

        if (currentAvailabilityId == null) {
            existsOverlap = availabilityRepository
                    .existsByPsychologistAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
                            psychologist,
                            availability.getDayOfWeek(),
                            availability.getEndTime(),
                            availability.getStartTime()
                    );
        } else {
            existsOverlap = availabilityRepository
                    .existsByPsychologistAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrueAndIdNot(
                            psychologist,
                            availability.getDayOfWeek(),
                            availability.getEndTime(),
                            availability.getStartTime(),
                            currentAvailabilityId
                    );
        }

        if (existsOverlap) {
            throw new BusinessRuleException("El psicólogo ya tiene disponibilidad registrada en ese rango horario");
        }
    }

    private String getPsychologistFullName(Psychologist psychologist) {
        if (psychologist == null) return "Psicólogo";

        String firstName = psychologist.getFirstName() != null ? psychologist.getFirstName() : "";
        String lastName = psychologist.getLastName() != null ? psychologist.getLastName() : "";

        String fullName = (firstName + " " + lastName).trim();

        return fullName.isEmpty() ? "Psicólogo" : fullName;
    }

    private String translateDay(String day) {
        if (day == null) return "día no definido";

        return switch (day) {
            case "MONDAY" -> "lunes";
            case "TUESDAY" -> "martes";
            case "WEDNESDAY" -> "miércoles";
            case "THURSDAY" -> "jueves";
            case "FRIDAY" -> "viernes";
            case "SATURDAY" -> "sábado";
            case "SUNDAY" -> "domingo";
            default -> day;
        };
    }

    private String formatTime(Object time) {
        if (time == null) return "--:--";

        String value = time.toString();

        return value.length() >= 5 ? value.substring(0, 5) : value;
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