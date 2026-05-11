package com.centropsicologico.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PublicAvailabilityDto {

    private Long psychologistId;

    private String psychologistName;

    private String specialty;

    private String date;

    private List<String> slots;
}