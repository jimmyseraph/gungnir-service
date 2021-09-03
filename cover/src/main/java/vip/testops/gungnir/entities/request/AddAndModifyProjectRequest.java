package vip.testops.gungnir.entities.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddAndModifyProjectRequest {

    @NotNull(message = "projectName")
    private String projectName;

    private String addresses;

    @NotNull(message = "repository")
    private String repository;

    @NotNull(message = "branch")
    private String branch;

    @NotNull(message = "srcDir")
    private String srcDir;
}
