package com.increff.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {
    @NotBlank(message = "Email cannot be empty")
    @Size(max = 254, message = "Email address is too long (max 254 characters)")
    @Pattern(regexp = "^[A-Za-z0-9]([A-Za-z0-9._+-]*[A-Za-z0-9])?@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,6}$", message = "Invalid email format. Email must have a valid domain with 2-6 character extension (e.g., user@example.com)")
    @Pattern(regexp = "^(?!.*\\.\\.).*$", message = "Email cannot contain consecutive dots")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
