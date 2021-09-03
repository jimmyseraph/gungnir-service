package vip.testops.gungnir.user.entities.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
public class LoginCache {
    @Id
    private String id;
    private String userId;
    private String token;
    private Date createTime;
    private Date updateTime;
}
