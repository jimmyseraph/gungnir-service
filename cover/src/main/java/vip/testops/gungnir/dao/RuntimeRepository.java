package vip.testops.gungnir.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import vip.testops.gungnir.entities.dto.RuntimeDTO;

import java.util.List;

public interface RuntimeRepository extends PagingAndSortingRepository<RuntimeDTO, String> {
    List<RuntimeDTO> findByProjectName(String projectName);
}
