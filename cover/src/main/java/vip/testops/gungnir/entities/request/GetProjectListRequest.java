package vip.testops.gungnir.entities.request;

import lombok.Data;

@Data
public class GetProjectListRequest {
    private String keyword;
    private Integer current;
    private Integer pageSize;
}
