package vip.testops.gungnir.gateway.entities.dto;

import lombok.Data;
import java.util.Date;

@Data
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String password;
    private Date createTime;
    private Date updateTime;

}
