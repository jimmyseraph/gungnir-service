package vip.testops.gungnir.user.entities.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("user")
@Data
public class UserDTO {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private Date createTime;
    private Date updateTime;

}
