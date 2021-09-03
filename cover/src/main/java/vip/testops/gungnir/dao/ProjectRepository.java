package vip.testops.gungnir.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import vip.testops.gungnir.entities.dto.ProjectInfo;

public interface ProjectRepository extends PagingAndSortingRepository<ProjectInfo, String> {
    Page<ProjectInfo> findByProjectNameLike(String keyword, Pageable pageable);
    Iterable<ProjectInfo> findByProjectName(String name);
}
