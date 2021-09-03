package vip.testops.gungnir.user.entities.vto;

import lombok.Data;

@Data
public class UserVTO {
    private String name;
    private String email;
    private String token;
}
