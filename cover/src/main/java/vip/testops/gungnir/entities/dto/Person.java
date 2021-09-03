package vip.testops.gungnir.entities.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Person {
    @Id
    private String id;
    private String name;
    private int age;
}
