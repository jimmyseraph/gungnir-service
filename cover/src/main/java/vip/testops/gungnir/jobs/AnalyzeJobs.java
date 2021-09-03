package vip.testops.gungnir.jobs;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vip.testops.gungnir.dao.ProjectRepository;
import vip.testops.gungnir.dao.RuntimeRepository;
import vip.testops.gungnir.entities.dto.ProjectInfo;
import vip.testops.gungnir.entities.dto.RuntimeDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class AnalyzeJobs {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${repository.base-dir}")
    private String baseDir;

    @Value("${job.pool-num}")
    private int poolNum;

    @Value("${job.wait-for-timeouts}")
    private long timeout;

    private ProjectRepository projectRepository;
    private RuntimeRepository runtimeRepository;

    @Autowired
    public void setRuntimeRepository(RuntimeRepository runtimeRepository) {
        this.runtimeRepository = runtimeRepository;
    }

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @XxlJob("coverageAnalyzeJobHandler")
    public void coverageAnalyzeJobHandler(){
        String param = XxlJobHelper.getJobParam();
        Iterable<ProjectInfo> projectInfos = null;
        if(param == null || param.equals("")) {
            projectInfos = projectRepository.findAll();
        } else {
            projectInfos = projectRepository.findByProjectName(param);
        }
        ExecutorService pool = Executors.newFixedThreadPool(poolNum);
        projectInfos.forEach(item -> {
            File repoDir = new File(baseDir, item.getProjectName());
            if(item.getRealJar() == null || item.getRealJar().equals("")) {
                return;
            }
            File classes = new File(uploadPath+File.separator+item.getProjectName(), item.getRealJar());
            File sourceDir = new File(repoDir, item.getSrcDir());
            List<RuntimeDTO> runtimeDTOList = runtimeRepository.findByProjectName(item.getProjectName());
            if(runtimeDTOList != null && runtimeDTOList.size() > 0) {
                List<RuntimeDTO> list = runtimeDTOList.stream().filter(runtimeDTO ->
                    item.getAddresses().contains(runtimeDTO.getClient())
                ).collect(Collectors.toList());
                // merge execution data
                Map<Long, ExecutionData> executionDataMap = new HashMap<>();
                for(RuntimeDTO runtimeDTO : list) {
                    Map<Long, ExecutionData> partDataMap = runtimeDTO.getExecutionDatas();
                    for(Map.Entry<Long, ExecutionData> entry : partDataMap.entrySet()){
                        if(!executionDataMap.containsKey(entry.getKey())){
                            executionDataMap.put(entry.getKey(), entry.getValue());
                        }else {
                            executionDataMap.get(entry.getKey()).merge(entry.getValue());
                        }
                    }
                }
                // store to ExecutionDataStore
                ExecutionDataStore dataStore = getDataStore(executionDataMap);
                pool.execute(() -> {
                    try {
                        IBundleCoverage bundleCoverage = analyzeStructure(dataStore, classes, item.getProjectName());
                        item.setBundleCoverage(bundleCoverage);
                        projectRepository.save(item);
                    } catch (IOException e) {
                        XxlJobHelper.log("Cannot analyze project {}.", item.getProjectName());
                        XxlJobHelper.log(e);
                    }
                });
            }
        });
        pool.shutdown();
        try {
            if(pool.awaitTermination(timeout, TimeUnit.MINUTES)) {
                XxlJobHelper.handleSuccess();
            } else {
                XxlJobHelper.handleFail();
            }
        } catch (InterruptedException e) {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail();
        }
    }

    private IBundleCoverage analyzeStructure(ExecutionDataStore dataStore ,File classesFile, String title) throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(dataStore, coverageBuilder);
        analyzer.analyzeAll(classesFile);
        return coverageBuilder.getBundle(title);
    }

    private ExecutionDataStore getDataStore(Map<Long, ExecutionData> executionDatas) {
        ExecutionDataStore dataStore = new ExecutionDataStore();
        executionDatas.forEach((k, v) -> dataStore.put(v));

        return dataStore;
    }
}
