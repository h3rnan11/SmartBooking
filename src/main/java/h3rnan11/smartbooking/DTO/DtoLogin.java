package h3rnan11.smartbooking.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DtoLogin(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
