package vip.testops.gungnir.entities.dto;

import lombok.Data;

@Data
public class TempExecutionData {
    private String _id;
    private String name;
    private boolean[] probes;
}
