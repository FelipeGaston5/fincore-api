package com.fincore.fincore_api.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest( 
    @NotBlank(message = "O nome de usuário é obrigatório.")
    @Size(max = 120, message = "O nome de usuário não pode ter mais de 120 caracteres.")
    String name,

    @NotBlank(message = "O E-mail é obrigatório.")
    @Email(message = "O E-mail deve ser válido.")
    @Size(max = 254, message = "O E-mail não pode ter mais de 254 caracteres.")
    String email,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    String password
) {}

