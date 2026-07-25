package com.centropsicologico.sistema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequestDto {

    @NotBlank(
            message = "Debe ingresar su correo electrónico"
    )
    @Email(
            message = "Debe ingresar un correo electrónico válido"
    )
    @Size(
            max = 120,
            message = "El correo electrónico es demasiado largo"
    )
    private String email;
}