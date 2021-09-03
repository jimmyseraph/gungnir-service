package vip.testops.gungnir.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vip.testops.gungnir.commons.Response;
import vip.testops.gungnir.entities.request.AddAndModifyProjectRequest;
import vip.testops.gungnir.entities.request.GetProjectListRequest;
import vip.testops.gungnir.entities.vto.GetProjectListReply;
import vip.testops.gungnir.entities.vto.SourceData;
import vip.testops.gungnir.entities.vto.SourceFileData;
import vip.testops.gungnir.services.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/project")
@Slf4j
public class ProjectController {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/{id}/upload")
    @ResponseBody
    public Response<?> uploadJar(
            @PathVariable String id,
            @RequestParam(value = "jar", required = false) MultipartFile multipartFile
    ){
        Response<?> response = new Response<>();
        if(multipartFile == null || multipartFile.isEmpty()) {
            response.paramIllegalError("jar");
            return response;
        }
        log.info("original name: {}", multipartFile.getOriginalFilename());
        log.info("size: {}", multipartFile.getSize());
        String originalFilename = multipartFile.getOriginalFilename();
        if(originalFilename.lastIndexOf('.') == -1 || originalFilename.lastIndexOf('.') == originalFilename.length() - 1) {
            response.paramIllegalError("jar");
            log.info("upload file with no suffix.");
            return response;
        }
        if(id == null){
            response.paramMissError("id");
            return response;
        }
        projectService.doUpload(id, multipartFile, response);
        return response;
    }

    @PostMapping("/list")
    public Response<GetProjectListReply> getProjects(@RequestBody GetProjectListRequest request) {
        Response<GetProjectListReply> response = new Response<>();
        projectService.doGetProjectList(request, response);
        return response;
    }

    @PostMapping("/add")
    public Response<?> addProject(@Validated @RequestBody AddAndModifyProjectRequest request) {
        Response<?> response = new Response<>();
        projectService.doAddProject(request, response);
        return response;
    }

    @PostMapping("/{id}/modify")
    public Response<?> modifyProject(@PathVariable String id, @Validated @RequestBody AddAndModifyProjectRequest request) {
        Response<?> response = new Response<>();
        projectService.doModifyProject(id, request, response);
        return response;
    }

    @GetMapping("/{id}/files")
    public Response<List<SourceData>> getResourceFiles(@PathVariable String id) {
        Response<List<SourceData>> response = new Response<>();
        projectService.doGetResource(id, response);
        return response;
    }

    @GetMapping("/{id}/file_detail")
    public Response<?> fileDetail(
            @PathVariable String id,
            @RequestParam("package") String packageName,
            @RequestParam("file") String filename
    ){
        Response<SourceFileData> response = new Response<>();

        projectService.doGetFileDetail(id, packageName, filename, response);
        return response;
    }

}
