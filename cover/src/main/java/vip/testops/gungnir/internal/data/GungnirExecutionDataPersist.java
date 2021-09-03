package vip.testops.gungnir.internal.data;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import vip.testops.gungnir.dao.ProjectRepository;
import vip.testops.gungnir.dao.RuntimeRepository;
import vip.testops.gungnir.entities.dto.ProjectInfo;
import vip.testops.gungnir.entities.dto.RuntimeDTO;

import java.util.*;

public class GungnirExecutionDataPersist implements IExtraInfoVisitor, ISessionInfoVisitor, IExecutionDataVisitor {

    private RuntimeDTO runtimeDTO;
    private RuntimeRepository runtimeRepository;
    private ProjectRepository projectRepository;
    private String client;

    public GungnirExecutionDataPersist(RuntimeRepository runtimeRepository, ProjectRepository projectRepository, String client) {
        this.runtimeRepository = runtimeRepository;
        this.projectRepository = projectRepository;
        this.client = client;
        runtimeDTO = new RuntimeDTO();
        runtimeDTO.setStartTime(new Date());
        runtimeDTO.setExecutionDatas(new HashMap<>());
    }

    public void stop(){

    }

    @Override
    public void visitClassExecution(ExecutionData data) {
        if (data.hasHits()) {
            if(runtimeDTO.getProjectName() != null){
                runtimeDTO.appendExecutionData(data);
                runtimeRepository.save(runtimeDTO);
            }
        }
    }

    @Override
    public void visitSessionInfo(SessionInfo info) {
        if(runtimeDTO.getSessionInfo() == null) {
            runtimeDTO.setSessionInfo(info);
        }
    }

    @Override
    public void visitExtraInfo(ExtraInfo info) {
        if (runtimeDTO.getProjectName() == null){
            Iterable<ProjectInfo> projectInfos = projectRepository.findByProjectName(info.getProjectName());
            if(projectInfos == null || !projectInfos.iterator().hasNext()) {
                return;
            }
            ProjectInfo projectInfo = projectInfos.iterator().next();
            List<String> addresses = projectInfo.getAddresses();
            if(addresses != null && addresses.contains(this.client)) {
                List<RuntimeDTO> runtimeDTOList = runtimeRepository.findByProjectName(info.getProjectName());
                for(RuntimeDTO item : runtimeDTOList) {
                    if(item.getClient().equals(client)){
                        runtimeRepository.delete(item);
                    }
                }
                runtimeDTO.setProjectName(info.getProjectName());
                runtimeDTO.setClient(this.client);
                runtimeRepository.save(runtimeDTO);
            }
        }
    }

}
