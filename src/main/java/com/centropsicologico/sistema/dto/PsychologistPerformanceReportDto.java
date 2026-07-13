package com.centropsicologico.sistema.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PsychologistPerformanceReportDto {

    private Long psychologistId;
    private String psychologistName;
    private Long totalPatients;
    private Long totalAppointments;
    private List<TherapySummaryDto> therapies;

    public PsychologistPerformanceReportDto(
            Long psychologistId,
            String psychologistName,
            Long totalPatients,
            Long totalAppointments,
            List<TherapySummaryDto> therapies
    ) {
        this.psychologistId = psychologistId;
        this.psychologistName = psychologistName;
        this.totalPatients = totalPatients;
        this.totalAppointments = totalAppointments;
        this.therapies = therapies;
    }

    @Getter
    @Setter
    public static class TherapySummaryDto {

        private String serviceName;
        private Long total;

        public TherapySummaryDto(String serviceName, Long total) {
            this.serviceName = serviceName;
            this.total = total;
        }
    }
}