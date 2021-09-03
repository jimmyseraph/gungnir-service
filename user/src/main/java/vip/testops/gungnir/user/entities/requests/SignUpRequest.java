package vip.testops.gungnir.user.entities.requests;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignUpRequest {
    @NotNull(message = "name")
    private String name;
    @NotNull(message = "email")
    private String email;
    @NotNull(message = "password")
    private String password;
}
