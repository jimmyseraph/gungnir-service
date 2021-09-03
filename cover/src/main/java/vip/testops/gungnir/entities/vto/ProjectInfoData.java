package vip.testops.gungnir.entities.vto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectInfoData {
    private String projectId;
    private String projectName;
    private List<String> addresses;
    private String repository;
    private String branch;
    private String srcDir;
    private String jar;
    private String coverage;
    private String creator;
    private String createTime;
    private String updateTime;
}
