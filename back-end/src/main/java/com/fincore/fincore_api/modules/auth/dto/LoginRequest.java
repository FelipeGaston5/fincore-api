package com.fincore.fincore_api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "O E-mail é obrigatório.") 
    @Email (message = "Informe um e-mail válido.")
    String email,

    @NotBlank(message = "A senha é obrigatória.")
    String password

) {} 
