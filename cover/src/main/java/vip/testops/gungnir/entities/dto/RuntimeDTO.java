package vip.testops.gungnir.entities.dto;

import lombok.Data;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Document("runtimeData")
@Data
public class RuntimeDTO {
    @Id
    private String id;
    private String projectName;
    private String client;
    private Date startTime;
    private SessionInfo sessionInfo;
    private Map<Long, ExecutionData> executionDatas;
//    private Integer status;

    public void appendExecutionData(ExecutionData executionData) {
        executionDatas.put(executionData.getId(), executionData);
    }
}
