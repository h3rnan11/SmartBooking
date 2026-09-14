package h3rnan11.smartbooking.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DtoNewUser(
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String password
) {}
