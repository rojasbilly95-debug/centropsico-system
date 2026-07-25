package com.centropsicologico.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {

    @NotBlank(
            message = "El token de recuperación es obligatorio"
    )
    @Size(
            max = 200,
            message = "El token de recuperación no es válido"
    )
    private String token;

    @NotBlank(
            message = "La nueva contraseña es obligatoria"
    )
    @Size(
            min = 10,
            max = 72,
            message = "La contraseña debe tener entre 10 y 72 caracteres"
    )
    private String newPassword;

    @NotBlank(
            message = "Debe confirmar la nueva contraseña"
    )
    private String confirmPassword;
}