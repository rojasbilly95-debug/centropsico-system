package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.dto.AvailablePsychologistDto;
import com.centropsicologico.sistema.dto.PaymentRequestDto;
import com.centropsicologico.sistema.entity.Appointment;
import com.centropsicologico.sistema.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    /*
     * =========================================================
     * REGISTRAR CITA
     * =========================================================
     */

    @PostMapping
    public Appointment save(
            @RequestBody Appointment appointment
    ) {
        return appointmentService.save(appointment);
    }

    /*
     * =========================================================
     * LISTAR TODAS LAS CITAS
     * =========================================================
     */

    @GetMapping
    public List<Appointment> findAll() {
        return appointmentService.findAll();
    }

    /*
     * =========================================================
     * BUSCAR CITAS POR FECHA
     * =========================================================
     */

    @GetMapping("/by-date")
    public List<Appointment> getByDate(
            @RequestParam String date
    ) {
        return appointmentService.findByDate(
                LocalDate.parse(date)
        );
    }

    /*
     * =========================================================
     * BUSCAR PSICÓLOGOS POR SERVICIO
     * =========================================================
     *
     * Ejemplo:
     * GET /api/appointments/psychologists-by-service?serviceId=1
     */

    @GetMapping("/psychologists-by-service")
    public List<AvailablePsychologistDto>
    findPsychologistsByService(
            @RequestParam Long serviceId
    ) {
        return appointmentService
                .findPsychologistsByService(serviceId);
    }

    /*
     * =========================================================
     * CITAS DEL PSICÓLOGO AUTENTICADO
     * =========================================================
     */

    @GetMapping("/my")
    public List<Appointment> findMyAppointments(
            Principal principal
    ) {
        return appointmentService
                .findByPsychologistEmail(
                        principal.getName()
                );
    }

    /*
     * =========================================================
     * CITAS DEL PSICÓLOGO POR FECHA
     * =========================================================
     */

    @GetMapping("/my/by-date")
    public List<Appointment>
    findMyAppointmentsByDate(
            @RequestParam String date,
            Principal principal
    ) {
        return appointmentService
                .findByPsychologistEmailAndDate(
                        principal.getName(),
                        LocalDate.parse(date)
                );
    }

    /*
     * =========================================================
     * BUSCAR CITA POR ID
     * =========================================================
     */

    @GetMapping("/{id}")
    public Appointment findById(
            @PathVariable Long id
    ) {
        return appointmentService.findById(id);
    }

    /*
     * =========================================================
     * ACTUALIZAR CITA
     * =========================================================
     */

    @PutMapping("/{id}")
    public Appointment update(
            @PathVariable Long id,
            @RequestBody Appointment appointment
    ) {
        return appointmentService.update(
                id,
                appointment
        );
    }

    /*
     * =========================================================
     * REGISTRAR PAGO
     * =========================================================
     */

    @PutMapping("/{id}/pay")
    public Appointment payAppointment(
            @PathVariable Long id,
            @RequestBody PaymentRequestDto paymentRequest
    ) {
        return appointmentService.payAppointment(
                id,
                paymentRequest
        );
    }

    /*
     * =========================================================
     * ACTUALIZAR ESTADO DE CITA
     * =========================================================
     */

    @PutMapping("/{id}/status")
    public Appointment updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal,
            Authentication authentication
    ) {

        /*
         * Cuando el usuario es únicamente psicólogo,
         * solo podrá actualizar sus propias citas.
         */
        if (hasAuthority(
                authentication,
                "PSICOLOGO"
        )
                && !hasAuthority(
                        authentication,
                        "ADMIN"
                )
                && !hasAuthority(
                        authentication,
                        "RECEPCIONISTA"
                )) {

            return appointmentService
                    .updateStatusForPsychologist(
                            id,
                            status,
                            principal.getName()
                    );
        }

        /*
         * Administrador y recepción pueden actualizar
         * el estado de las citas normalmente.
         */
        return appointmentService.updateStatus(
                id,
                status
        );
    }

    /*
     * =========================================================
     * CANCELAR CITA
     * =========================================================
     */

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        appointmentService.delete(id);
    }

    /*
     * =========================================================
     * VALIDACIÓN DE ROLES
     * =========================================================
     */

    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication
                .getAuthorities()
                .stream()
                .anyMatch(item ->
                        authority.equals(
                                item.getAuthority()
                        )
                );
    }
}