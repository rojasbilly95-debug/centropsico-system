package com.centropsicologico.sistema.config;

import com.centropsicologico.sistema.entity.*;
import com.centropsicologico.sistema.enums.AppointmentStatus;
import com.centropsicologico.sistema.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class DatabaseInitializer {

    @Bean
    CommandLineRunner initDatabase(
            PatientRepository patientRepository,
            PsychologistRepository psychologistRepository,
            ServiceRepository serviceRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository expenseCategoryRepository,
            ClinicalHistoryRepository clinicalHistoryRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {

            // =========================
            // USUARIOS
            // =========================
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("Principal");
                admin.setEmail("admin@centro.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole("ADMIN");
                admin.setActive(true);

                User recepcion = new User();
                recepcion.setFirstName("Rosa");
                recepcion.setLastName("Paredes");
                recepcion.setEmail("recepcion@centro.com");
                recepcion.setPassword(passwordEncoder.encode("123456"));
                recepcion.setRole("RECEPCIONISTA");
                recepcion.setActive(true);

                User psicologo = new User();
                psicologo.setFirstName("Luis");
                psicologo.setLastName("Gomez");
                psicologo.setEmail("psicologo@centro.com");
                psicologo.setPassword(passwordEncoder.encode("123456"));
                psicologo.setRole("PSICOLOGO");
                psicologo.setActive(true);

                userRepository.save(admin);
                userRepository.save(recepcion);
                userRepository.save(psicologo);
            }

            // =========================
            // PACIENTES, PSICÓLOGOS, SERVICIOS Y CITAS
            // =========================
            if (patientRepository.count() == 0) {

                Patient p1 = createPatient("Carlos", "Ramirez", "12345678", "1995-04-12", "MASCULINO", "999111222",
                        "carlos@gmail.com", "Av. Los Olivos 123", "Marta Ramirez", "999888777");
                Patient p2 = createPatient("Ana", "Lopez", "87654321", "1998-08-22", "FEMENINO", "988777666",
                        "ana@gmail.com", "Jr. Lima 456", "Pedro Lopez", "988111222");
                Patient p3 = createPatient("Rodrigo", "Cornejo", "74241521", "1992-01-18", "MASCULINO", "941223541",
                        "rodrigo@gmail.com", "Av. Arequipa 789", "Lucia Cornejo", "941000111");
                Patient p4 = createPatient("Rene", "Juarez", "74142564", "1989-11-03", "MASCULINO", "914227845",
                        "rene5741@gmail.com", "Calle San Martin 321", "Elena Juarez", "914555888");
                Patient p5 = createPatient("Lucia", "Mendoza", "70654312", "2001-06-14", "FEMENINO", "956321478",
                        "lucia@gmail.com", "Urb. Primavera 222", "Carla Mendoza", "956888999");
                Patient p6 = createPatient("Mario", "Vargas", "73451289", "1985-02-07", "MASCULINO", "987654321",
                        "mario@gmail.com", "Av. Central 909", "Rosa Vargas", "987222333");

                patientRepository.save(p1);
                patientRepository.save(p2);
                patientRepository.save(p3);
                patientRepository.save(p4);
                patientRepository.save(p5);
                patientRepository.save(p6);

                Psychologist ps1 = createPsychologist("Luis", "Gomez", "Ansiedad y estrés", "999000111",
                        "psicologo@centro.com");
                Psychologist ps2 = createPsychologist("Maria", "Torres", "Terapia de pareja", "977888999",
                        "maria@centro.com");
                Psychologist ps3 = createPsychologist("Sofia", "Herrera", "Psicología infantil", "966777555",
                        "sofia@centro.com");

                psychologistRepository.save(ps1);
                psychologistRepository.save(ps2);
                psychologistRepository.save(ps3);

                ServiceEntity s1 = createService("Terapia Individual", "Sesión personalizada para un paciente", 80.0,
                        60);
                ServiceEntity s2 = createService("Terapia de Pareja", "Sesión orientada a parejas", 120.0, 90);
                ServiceEntity s3 = createService("Evaluación Psicológica", "Evaluación inicial y diagnóstico", 150.0,
                        90);
                ServiceEntity s4 = createService("Terapia Infantil", "Sesión especializada para niños", 100.0, 60);
                ServiceEntity s5 = createService("Orientación Familiar", "Sesión de orientación familiar", 130.0, 90);

                serviceRepository.save(s1);
                serviceRepository.save(s2);
                serviceRepository.save(s3);
                serviceRepository.save(s4);
                serviceRepository.save(s5);

                LocalDate today = LocalDate.now();

                Appointment a1 = createAppointment(p1, ps1, s1, today, 9, 0, 10, 0, AppointmentStatus.PROGRAMADA,
                        "Ansiedad laboral", "Primera sesión");
                Appointment a2 = createAppointment(p2, ps2, s2, today, 10, 30, 12, 0, AppointmentStatus.ATENDIDA,
                        "Problemas de pareja", "Sesión atendida");
                Appointment a3 = createAppointment(p3, ps1, s3, today, 12, 30, 14, 0, AppointmentStatus.CANCELADA,
                        "Evaluación inicial", "Paciente canceló");
                Appointment a4 = createAppointment(p4, ps3, s4, today, 15, 0, 16, 0, AppointmentStatus.PROGRAMADA,
                        "Conducta infantil", "Acude con apoderado");

                Appointment a5 = createAppointment(p5, ps1, s1, today.plusDays(1), 9, 0, 10, 0,
                        AppointmentStatus.PROGRAMADA, "Estrés académico", "Seguimiento");
                Appointment a6 = createAppointment(p6, ps2, s5, today.plusDays(1), 11, 0, 12, 30,
                        AppointmentStatus.PROGRAMADA, "Conflicto familiar", "Primera sesión");
                Appointment a7 = createAppointment(p1, ps1, s1, today.minusDays(3), 10, 0, 11, 0,
                        AppointmentStatus.ATENDIDA, "Seguimiento ansiedad", "Mejor evolución");
                Appointment a8 = createAppointment(p2, ps2, s2, today.minusDays(5), 12, 0, 13, 30,
                        AppointmentStatus.NO_ASISTIO, "Terapia de pareja", "No asistió");
                Appointment a9 = createAppointment(p3, ps1, s3, today.minusDays(10), 16, 0, 17, 30,
                        AppointmentStatus.ATENDIDA, "Evaluación psicológica", "Proceso completado");
                Appointment a10 = createAppointment(p4, ps3, s4, today.minusDays(15), 9, 30, 10, 30,
                        AppointmentStatus.ATENDIDA, "Terapia infantil", "Participación activa");

                markAsPaid(a2, new BigDecimal("120.00"), "YAPE", "YAPE-874521", "Pago confirmado por recepción",
                        "Rosa Paredes");
                markAsPaid(a7, new BigDecimal("80.00"), "EFECTIVO", "EFECTIVO", "Pago en caja", "Rosa Paredes");
                markAsPaid(a9, new BigDecimal("150.00"), "TRANSFERENCIA", "BCP-99887766", "Transferencia verificada",
                        "Admin Principal");
                markAsPaid(a10, new BigDecimal("100.00"), "PLIN", "PLIN-552211", "Pago por apoderado", "Rosa Paredes");

                appointmentRepository.save(a1);
                appointmentRepository.save(a2);
                appointmentRepository.save(a3);
                appointmentRepository.save(a4);
                appointmentRepository.save(a5);
                appointmentRepository.save(a6);
                appointmentRepository.save(a7);
                appointmentRepository.save(a8);
                appointmentRepository.save(a9);
                appointmentRepository.save(a10);

                createClinicalHistory(clinicalHistoryRepository, p1, "Ansiedad laboral", "Ansiedad leve",
                        "Paciente refiere mejora en manejo del estrés.", "Aplicar técnicas de respiración diaria.",
                        "Luis Gomez");
                createClinicalHistory(clinicalHistoryRepository, p2, "Conflictos de pareja",
                        "Dificultad de comunicación", "Se identifican patrones de discusión repetitivos.",
                        "Realizar ejercicios de escucha activa.", "Maria Torres");
                createClinicalHistory(clinicalHistoryRepository, p3, "Evaluación psicológica", "Evaluación inicial",
                        "Paciente colaborador durante evaluación.", "Continuar con entrevistas clínicas.",
                        "Luis Gomez");
                createClinicalHistory(clinicalHistoryRepository, p4, "Conducta infantil",
                        "Dificultades de regulación emocional", "Menor participa con acompañamiento.",
                        "Trabajo conjunto con familia.", "Sofia Herrera");
            }

            // =========================
            // FINANZAS
            // =========================
            if (expenseCategoryRepository.count() == 0) {
                ExpenseCategory c1 = createExpenseCategory("Alquiler", "Pago mensual del local");
                ExpenseCategory c2 = createExpenseCategory("Servicios", "Luz, agua, internet y telefonía");
                ExpenseCategory c3 = createExpenseCategory("Marketing", "Publicidad y campañas");
                ExpenseCategory c4 = createExpenseCategory("Materiales", "Útiles, papelería y recursos clínicos");
                ExpenseCategory c5 = createExpenseCategory("Mantenimiento", "Limpieza y reparaciones");

                expenseCategoryRepository.save(c1);
                expenseCategoryRepository.save(c2);
                expenseCategoryRepository.save(c3);
                expenseCategoryRepository.save(c4);
                expenseCategoryRepository.save(c5);

                LocalDate today = LocalDate.now();

                expenseRepository.save(createExpense(c1, "Alquiler del consultorio", new BigDecimal("1200.00"),
                        today.withDayOfMonth(1), "Admin Principal"));
                expenseRepository.save(createExpense(c2, "Pago de internet", new BigDecimal("180.00"),
                        today.withDayOfMonth(3), "Rosa Paredes"));
                expenseRepository.save(createExpense(c2, "Servicio de luz", new BigDecimal("220.00"),
                        today.withDayOfMonth(5), "Rosa Paredes"));
                expenseRepository.save(createExpense(c3, "Publicidad en redes sociales", new BigDecimal("350.00"),
                        today.withDayOfMonth(8), "Admin Principal"));
                expenseRepository.save(createExpense(c4, "Material para evaluaciones", new BigDecimal("140.00"),
                        today.withDayOfMonth(10), "Rosa Paredes"));
                expenseRepository.save(createExpense(c5, "Limpieza del local", new BigDecimal("250.00"),
                        today.withDayOfMonth(12), "Admin Principal"));
            }

            if (incomeRepository.count() == 0) {
                LocalDate today = LocalDate.now();

                incomeRepository.save(createIncome("Pago de cita #2 - Ana Lopez | Método: YAPE | Op: YAPE-874521",
                        new BigDecimal("120.00"), today, "YAPE"));
                incomeRepository.save(createIncome("Pago de cita #7 - Carlos Ramirez | Método: EFECTIVO",
                        new BigDecimal("80.00"), today.minusDays(3), "EFECTIVO"));
                incomeRepository.save(
                        createIncome("Pago de cita #9 - Rodrigo Cornejo | Método: TRANSFERENCIA | Op: BCP-99887766",
                                new BigDecimal("150.00"), today.minusDays(10), "TRANSFERENCIA"));
                incomeRepository.save(createIncome("Pago de cita #10 - Rene Juarez | Método: PLIN | Op: PLIN-552211",
                        new BigDecimal("100.00"), today.minusDays(15), "PLIN"));

                incomeRepository.save(createIncome("Taller grupal de manejo de estrés", new BigDecimal("850.00"),
                        today.minusDays(6), "TRANSFERENCIA"));
                incomeRepository.save(createIncome("Evaluación psicológica externa", new BigDecimal("600.00"),
                        today.minusDays(2), "TARJETA"));
                incomeRepository.save(createIncome("Paquete mensual de terapia individual", new BigDecimal("1200.00"),
                        today.minusDays(4), "YAPE"));
                incomeRepository.save(createIncome("Convenio empresarial - sesiones psicológicas",
                        new BigDecimal("1500.00"), today.minusDays(8), "TRANSFERENCIA"));
            }

            System.out.println("🔥 BASE DE DATOS INICIALIZADA CON DATOS DE NEGOCIO");
        };
    }

    private Patient createPatient(String firstName, String lastName, String dni, String birthDate, String gender,
            String phone, String email, String address, String emergencyContact, String emergencyPhone) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDni(dni);
        patient.setBirthDate(LocalDate.parse(birthDate));
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setEmail(email);
        patient.setAddress(address);
        patient.setEmergencyContact(emergencyContact);
        patient.setEmergencyPhone(emergencyPhone);
        patient.setActive(true);
        return patient;
    }

    private Psychologist createPsychologist(String firstName, String lastName, String specialty, String phone,
            String email) {
        Psychologist psychologist = new Psychologist();
        psychologist.setFirstName(firstName);
        psychologist.setLastName(lastName);
        psychologist.setSpecialty(specialty);
        psychologist.setPhone(phone);
        psychologist.setEmail(email);
        psychologist.setActive(true);
        return psychologist;
    }

    private ServiceEntity createService(String name, String description, Double price, Integer duration) {
        ServiceEntity service = new ServiceEntity();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationMinutes(duration);
        service.setActive(true);
        return service;
    }

    private Appointment createAppointment(Patient patient, Psychologist psychologist, ServiceEntity service,
            LocalDate date, int sh, int sm, int eh, int em,
            AppointmentStatus status, String reason, String observation) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setPsychologist(psychologist);
        appointment.setService(service);
        appointment.setDate(date);
        appointment.setStartTime(LocalTime.of(sh, sm));
        appointment.setEndTime(LocalTime.of(eh, em));
        appointment.setStatus(status);
        appointment.setReason(reason);
        appointment.setObservation(observation);
        appointment.setPaid(false);
        return appointment;
    }

    private void markAsPaid(Appointment appointment, BigDecimal amount, String method, String operationCode,
            String observation, String registeredBy) {
        appointment.setPaid(true);
        appointment.setPaidAmount(amount);
        appointment.setPaymentMethod(method);
        appointment.setPaymentDate(LocalDate.now());
        appointment.setPaymentDateTime(LocalDateTime.now());
        appointment.setOperationCode(operationCode);
        appointment.setPaymentObservation(observation);
        appointment.setPaymentRegisteredBy(registeredBy);
    }

    private Income createIncome(String description, BigDecimal amount, LocalDate date, String method) {
        Income income = new Income();
        income.setDescription(description);
        income.setAmount(amount);
        income.setDate(date);
        income.setPaymentMethod(method);
        income.setActive(true);
        return income;
    }

    private ExpenseCategory createExpenseCategory(String name, String description) {
        ExpenseCategory category = new ExpenseCategory();
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        return category;
    }

    private Expense createExpense(ExpenseCategory category, String description, BigDecimal amount, LocalDate date,
            String responsible) {
        Expense expense = new Expense();
        expense.setCategory(category);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setResponsible(responsible);
        expense.setActive(true);
        return expense;
    }

    private void createClinicalHistory(ClinicalHistoryRepository repository, Patient patient, String reason,
            String diagnosis, String evolution, String recommendations, String psychologistName) {
        ClinicalHistory history = new ClinicalHistory();
        history.setPatient(patient);
        history.setDate(LocalDateTime.now());
        history.setReason(reason);
        history.setDiagnosis(diagnosis);
        history.setEvolution(evolution);
        history.setRecommendations(recommendations);
        history.setPsychologistName(psychologistName);
        history.setActive(true);

        repository.save(history);
    }
}