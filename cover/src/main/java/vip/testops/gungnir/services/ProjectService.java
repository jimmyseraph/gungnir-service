package vip.testops.gungnir.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jacoco.core.analysis.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vip.testops.gungnir.analysis.JavaSourceLocator;
import vip.testops.gungnir.commons.Response;
import vip.testops.gungnir.dao.ProjectRepository;
import vip.testops.gungnir.entities.dto.ProjectInfo;
import vip.testops.gungnir.entities.request.AddAndModifyProjectRequest;
import vip.testops.gungnir.entities.request.GetProjectListRequest;
import vip.testops.gungnir.entities.vto.*;
import vip.testops.gungnir.internal.analysis.SourceParser;
import vip.testops.gungnir.internal.data.GungnirCRC64;
import vip.testops.gungnir.internal.os.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ProjectService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${repository.base-dir}")
    private String baseDir;

    private ProjectRepository projectRepository;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void doAddProject(AddAndModifyProjectRequest request, Response<?> response) {
        File repoDir = new File(baseDir, request.getProjectName());
        // create local repo
        try {
            Git.cloneRepository()
                    .setURI(request.getRepository())
                    .setBranch(request.getBranch())
                    .setDirectory(repoDir).call();
        } catch (GitAPIException e) {
            log.error("create repository {} error.", request.getProjectName(), e);
            response.serviceError("create repository error");
            return;
        }
        ProjectInfo projectInfo = new ProjectInfo();
        if(request.getAddresses() != null && !request.getAddresses().trim().equals("")) {
            List<String> addresses = new ArrayList<>();
            for(String address : request.getAddresses().split("[,，]")) {
                addresses.add(address.trim());
            }
            projectInfo.setAddresses(addresses);
        }
        projectInfo.setProjectName(request.getProjectName());
        projectInfo.setRepository(request.getRepository());
        projectInfo.setBranch(request.getBranch());
        projectInfo.setSrcDir(request.getSrcDir());
        projectInfo.setCreateTime(new Date());
        projectInfo.setUpdateTime(new Date());
        projectRepository.save(projectInfo);
        response.commonSuccess();
    }

    public void doModifyProject(String projectId, AddAndModifyProjectRequest request, Response<?> response) {
        Optional<ProjectInfo> projectInfoOptional = projectRepository.findById(projectId);
        ProjectInfo projectInfo = null;
        if(!projectInfoOptional.isPresent()) {
            response.serviceError("no such project");
            return;
        }
        projectInfo = projectInfoOptional.get();
        if(!projectInfo.getProjectName().equals(request.getProjectName())) {
            File path = new File(baseDir, projectInfo.getProjectName());
            if(path.isDirectory()){
                FileUtil.deleteDirectory(path);
            }
        }
        if(request.getAddresses() != null && !request.getAddresses().trim().equals("")) {
            List<String> addresses = new ArrayList<>();
            for(String address : request.getAddresses().split("[,，]")) {
                addresses.add(address.trim());
            }
            projectInfo.setAddresses(addresses);
        }
        projectInfo.setProjectName(request.getProjectName());
        projectInfo.setRepository(request.getRepository());
        projectInfo.setBranch(request.getBranch());
        projectInfo.setUpdateTime(new Date());
        projectRepository.save(projectInfo);
        response.commonSuccess();
    }

    public void doGetProjectList(GetProjectListRequest request, Response<GetProjectListReply> response){
        GetProjectListReply getProjectListReply = new GetProjectListReply();
        Pageable pageable;
        if (request.getCurrent() == null || request.getCurrent() <= 0
        || request.getPageSize() == null || request.getPageSize() <= 0){
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(request.getCurrent() - 1, request.getPageSize());
        }
        String keyword = request.getKeyword() == null ? "" : request.getKeyword();
        Page<ProjectInfo> pagedProjectInfo = projectRepository.findByProjectNameLike(keyword, pageable);
        List<ProjectInfoData> projectInfoDataList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        pagedProjectInfo.forEach(item -> {
            ProjectInfoData projectInfoData = new ProjectInfoData();
            projectInfoData.setProjectId(item.getId());
            projectInfoData.setProjectName(item.getProjectName());
            projectInfoData.setAddresses(item.getAddresses());
            projectInfoData.setRepository(item.getRepository());
            projectInfoData.setBranch(item.getBranch());
            projectInfoData.setJar(item.getOriginJar());
            IBundleCoverage bundleCoverage = item.getBundleCoverage();
            if(bundleCoverage != null) {
                ICounter counter = bundleCoverage.getLineCounter();
                projectInfoData.setCoverage(String.format("%1$.2f%%", counter.getCoveredRatio() * 100));
            } else {
                projectInfoData.setCoverage("N/A");
            }

            projectInfoData.setCreator(item.getCreator());
            projectInfoData.setCreateTime(simpleDateFormat.format(item.getCreateTime()));
            projectInfoData.setUpdateTime(simpleDateFormat.format(item.getUpdateTime()));
            projectInfoDataList.add(projectInfoData);
        });
        getProjectListReply.setProjects(projectInfoDataList);
        if(pageable.isPaged()) {
            Pagination pagination = new Pagination();
            pagination.setCurrent(request.getCurrent());
            pagination.setPageSize(request.getPageSize());
            pagination.setTotal((int) pagedProjectInfo.getTotalElements());
            getProjectListReply.setPagination(pagination);
        }
        response.dataSuccess(getProjectListReply);
    }

    public void doUpload(String projectId, MultipartFile multipartFile, Response<?> response){
        Optional<ProjectInfo> projectInfoOptional = projectRepository.findById(projectId);
        ProjectInfo projectInfo = null;
        if (projectInfoOptional.isPresent()){
            projectInfo = projectInfoOptional.get();
        } else {
            response.serviceError("project ID is invalid");
            return;
        }

        File path = new File(uploadPath, projectInfo.getProjectName());
        String originFilename = multipartFile.getOriginalFilename();
        String suffix = Objects.requireNonNull(originFilename).substring(originFilename.lastIndexOf('.')+1);;

        String filename = projectId + "." + suffix;
        if(!path.isDirectory()) {
            if(!path.mkdirs()) {
                log.error("create upload path {} failed", path.getAbsolutePath());
                response.serviceError("create upload path failed");
                return;
            }
        }
        try {
            InputStream in = multipartFile.getInputStream();
            FileOutputStream out = new FileOutputStream(new File(path, filename));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            log.error("read upload file failed.", e);
            response.serviceError("upload file failed");
        }

        projectInfo.setRealJar(filename);
        projectInfo.setOriginJar(originFilename);
        projectInfo.setUpdateTime(new Date());
        projectRepository.save(projectInfo);
        response.commonSuccess();
    }

    public void doGetResource(String projectId, Response<List<SourceData>> response) {
        Optional<ProjectInfo> optional = projectRepository.findById(projectId);
        ProjectInfo projectInfo;
        if (optional.isPresent()){
            projectInfo = optional.get();
        } else {
            response.serviceError("project ID is invalid");
            return;
        }
        File path = new File(this.baseDir, projectInfo.getProjectName());
        File srcPath = new File(path, projectInfo.getSrcDir());
        List<SourceData> sourceDataList = new ArrayList<>();
        SourceData root = new SourceData();
        root.setId(projectInfo.getId());
        root.setName(projectInfo.getProjectName()+"/"+projectInfo.getSrcDir());
        root.setType("dir");
        root.setComponents(lookupSource(srcPath));
        sourceDataList.add(root);
        if(projectInfo.getBundleCoverage() != null) {
            stickCoverage(sourceDataList, projectInfo.getBundleCoverage());
        }
        response.dataSuccess(sourceDataList);
    }

    public void doGetFileDetail(String projectId, String packageName, String filename, Response<SourceFileData> response){
        Optional<ProjectInfo> optional = projectRepository.findById(projectId);
        ProjectInfo projectInfo;
        if (optional.isPresent()){
            projectInfo = optional.get();
        } else {
            response.serviceError("project ID is invalid");
            return;
        }
        File path = new File(this.baseDir, projectInfo.getProjectName());
        File srcPath = new File(path, projectInfo.getSrcDir());
        JavaSourceLocator javaSourceLocator = new JavaSourceLocator(srcPath);
        FileReader reader;
        SourceFileData sourceFileData = null;
        try {
            reader = (FileReader) javaSourceLocator.getFileSource(packageName, filename);
        } catch (FileNotFoundException e) {
            log.error("cannot find file.", e);
            response.serviceError("file resource is invalid");
            return;
        }
        BufferedReader bufferedReader = new BufferedReader(reader);
        IBundleCoverage bundleCoverage = projectInfo.getBundleCoverage();
        if (bundleCoverage != null) { // if coverage is valid, store the data to source file
            // get packageCoverage
            Optional<IPackageCoverage> optionalPackageCoverage = bundleCoverage.getPackages()
                    .stream()
                    .filter(item -> item.getName().equals(packageName.replace(".", "/")))
                    .findFirst();
            if (!optionalPackageCoverage.isPresent()) {
                response.serviceError("package is invalid");
                return;
            }
            IPackageCoverage packageCoverage = optionalPackageCoverage.get();
            // get sourceFileCoverage
            Optional<ISourceFileCoverage> optionalSourceFileCoverage = packageCoverage.getSourceFiles()
                    .stream()
                    .filter(item -> item.getName().equals(filename))
                    .findFirst();
            if(!optionalSourceFileCoverage.isPresent()) {
                response.serviceError("class file is invalid");
                return;
            }
            ISourceFileCoverage sourceFileCoverage = optionalSourceFileCoverage.get();
            // get each line of source file
            try {
                sourceFileData = readSourceFile(filename, bufferedReader, sourceFileCoverage);
            } catch (IOException e) {
                log.error("cannot read file {}.", filename, e);
                response.serviceError("cannot read file");
                return;
            }

        } else { // if coverage is invalid
            try {
                sourceFileData = readSourceFile(filename, bufferedReader, null);
            } catch (IOException e) {
                log.error("cannot read file {}.", filename, e);
                response.serviceError("cannot read file");
                return;
            }
        }

        response.dataSuccess(sourceFileData);
    }

    private SourceFileData readSourceFile(String filename, BufferedReader reader, ISourceFileCoverage fileCoverage) throws IOException {
        String line;
        int lineNo = 0;
        SourceFileData sourceFileData = new SourceFileData();
        List<SourceLineData> sourceLines = new LinkedList<>();
        sourceFileData.setName(filename);
        if(fileCoverage != null){
            sourceFileData.setBranchCount(fileCoverage.getBranchCounter().getTotalCount());
            sourceFileData.setBranchCoveredCount(fileCoverage.getBranchCounter().getCoveredCount());
            sourceFileData.setLineCount(fileCoverage.getLineCounter().getTotalCount());
            sourceFileData.setLineCoveredCount(fileCoverage.getLineCounter().getCoveredCount());
            sourceFileData.setInstructionCount(fileCoverage.getInstructionCounter().getTotalCount());
            sourceFileData.setInstructionCoveredCount(fileCoverage.getInstructionCounter().getCoveredCount());
        } else {
            sourceFileData.setBranchCount(0);
            sourceFileData.setBranchCoveredCount(0);
            sourceFileData.setLineCount(0);
            sourceFileData.setLineCoveredCount(0);
            sourceFileData.setInstructionCount(0);
            sourceFileData.setInstructionCoveredCount(0);
        }
        while((line = reader.readLine()) != null) {
            lineNo ++;
            SourceLineData sourceLineData = new SourceLineData();
            sourceLineData.setLineNo(lineNo);
            sourceLineData.setContent(line);
            if(fileCoverage != null) {
                ILine iLine = fileCoverage.getLine(lineNo);
                sourceLineData.setInstructionCount(iLine.getInstructionCounter().getTotalCount());
                sourceLineData.setInstructionCoveredCount(iLine.getInstructionCounter().getCoveredCount());
                sourceLineData.setBranchCount(iLine.getBranchCounter().getTotalCount());
                sourceLineData.setBranchCoveredCount(iLine.getBranchCounter().getCoveredCount());
            } else {
                sourceLineData.setInstructionCount(0);
                sourceLineData.setInstructionCoveredCount(0);
                sourceLineData.setBranchCount(0);
                sourceLineData.setBranchCoveredCount(0);
            }
            sourceLines.add(sourceLineData);
        }
        sourceFileData.setLines(sourceLines);
        return sourceFileData;
    }

    private List<SourceData> lookupSource(File path) {
        List<SourceData> list = new ArrayList<>();
        String[] names = path.list((dir, name) -> {
            if(new File(dir, name).isDirectory()){
                return true;
            } else return name.endsWith(".java");
        });
        if (names == null || names.length == 0) {
            return null;
        }
        for (String name : names) {
            SourceData sourceData = new SourceData();
            sourceData.setName(name);
            long id = GungnirCRC64.classId((path.getAbsolutePath()+"/" + name).getBytes(StandardCharsets.UTF_8));
            sourceData.setId(String.valueOf(id));
            File file = new File(path, name);
            if(file.isDirectory()){
                sourceData.setType("dir");
                sourceData.setComponents(lookupSource(file));
            } else {
                sourceData.setType("code");
                String packageName;
                try {
                    packageName = SourceParser.getPackageFromSource(file);
                } catch (FileNotFoundException e) {
                    log.error("source file not found", e);
                    continue;
                }
                if(packageName == null) {
                    continue;
                }
                sourceData.setPackageName(packageName);
            }
            list.add(sourceData);
        }
        return list;
    }

    private void stickCoverage(List<SourceData> sourceDataList, IBundleCoverage coverage) {
        for(SourceData sourceData : sourceDataList) {
            if (sourceData.getType().equals("code")) {
                String packageName = sourceData.getPackageName();
                String filename = sourceData.getName();
                Optional<IPackageCoverage> optionalPackageCoverage = coverage.getPackages()
                        .stream()
                        .filter(item -> item.getName().equals(packageName.replace(".", "/")))
                        .findFirst();
                if (!optionalPackageCoverage.isPresent()) {
                    sourceData.setLineCount(0);
                    sourceData.setLineCovered(0);
                    sourceData.setCoverage("0.00%");
                    continue;
                }
                IPackageCoverage packageCoverage = optionalPackageCoverage.get();
                Optional<ISourceFileCoverage> optionalSourceFileCoverage = packageCoverage.getSourceFiles()
                        .stream()
                        .filter(item -> item.getName().equals(filename))
                        .findFirst();
                if(!optionalSourceFileCoverage.isPresent()) {
                    sourceData.setLineCount(0);
                    sourceData.setLineCovered(0);
                    sourceData.setCoverage("0.00%");
                    continue;
                }
                ISourceFileCoverage sourceFileCoverage = optionalSourceFileCoverage.get();
                ICounter counter = sourceFileCoverage.getLineCounter();
                sourceData.setLineCount(counter.getTotalCount());
                sourceData.setLineCovered(counter.getCoveredCount());
                sourceData.setCoverage(String.format("%1$.2f%%", counter.getCoveredRatio() * 100));
            } else if (sourceData.getType().equals("dir")) {
                stickCoverage(sourceData.getComponents(), coverage);
                int lineCount = 0, lineCovered = 0;
                for(SourceData sd : sourceData.getComponents()) {
                    lineCount += sd.getLineCount();
                    lineCovered += sd.getLineCovered();
                }
                sourceData.setLineCount(lineCount);
                sourceData.setLineCovered(lineCovered);
                sourceData.setCoverage(String.format("%1$.2f%%", lineCovered * 100.0 / lineCount));
            }
        }
    }

}
