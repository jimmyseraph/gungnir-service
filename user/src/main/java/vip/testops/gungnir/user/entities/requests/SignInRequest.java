package vip.testops.gungnir.user.entities.requests;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SignInRequest {
    @NotNull(message = "email")
    private String email;
    @NotNull(message = "password")
    private String password;
}
