package vip.testops.gungnir.entities.vto;

import lombok.Data;

@Data
public class Pagination {
    private Integer current;
    private Integer pageSize;
    private Integer total;
}
