package vip.testops.gungnir.entities.vto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SourceData {
    private String id;
    private String name;
    @JsonProperty("package")
    private String packageName;
    private String type;
    private Integer lineCount;
    private Integer lineCovered;
    private String coverage;
    private List<SourceData> components;
}
