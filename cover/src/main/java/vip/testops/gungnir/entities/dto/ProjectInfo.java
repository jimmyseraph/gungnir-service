package vip.testops.gungnir.entities.dto;

import lombok.Data;
import org.jacoco.core.analysis.IBundleCoverage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document
public class ProjectInfo {
    @Id
    private String id;
    private String projectName;
    private List<String> addresses;
    private String repository;
    private String branch;
    private String originJar;
    private String realJar;
    private String srcDir;
    private IBundleCoverage bundleCoverage;
    private String creator;
    private Date createTime;
    private Date updateTime;
}
