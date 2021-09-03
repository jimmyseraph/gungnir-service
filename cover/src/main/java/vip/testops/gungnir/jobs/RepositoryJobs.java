package vip.testops.gungnir.jobs;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vip.testops.gungnir.dao.ProjectRepository;
import vip.testops.gungnir.entities.dto.ProjectInfo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class RepositoryJobs {

    private ProjectRepository projectRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${repository.base-dir}")
    private String baseDir;

    @Value("${job.pool-num}")
    private int poolNum;

    @Value("${job.wait-for-timeouts}")
    private long timeout;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @XxlJob("gitJobHandler")
    public void gitJobHandler(){
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
            pool.execute(() -> {
                try {
                    Git git = new Git(new FileRepository(new File(repoDir, ".git")));
                    PullResult result =  git.pull().setRemoteBranchName(item.getBranch()).call();
                    if(!result.isSuccessful()){
                        XxlJobHelper.log("project {} repo pull failed", item.getProjectName());
                    }
                } catch (IOException e) {
                    XxlJobHelper.log("Cannot visit git-dir {}.", repoDir.getAbsolutePath());
                    XxlJobHelper.log(e);
                } catch (Exception e) {
                    XxlJobHelper.log("project {} repo pull failed", item.getProjectName());
                    XxlJobHelper.log(e);
                }
            });
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

}
