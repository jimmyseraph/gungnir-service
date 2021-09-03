package vip.testops.gungnir.entities.vto;

import lombok.Data;

import java.util.List;

@Data
public class GetProjectListReply {
    private List<ProjectInfoData> projects;
    private Pagination pagination;
}
