package vip.testops.gungnir.user.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import vip.testops.gungnir.user.entities.dto.UserDTO;

import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<UserDTO, String> {
    List<UserDTO> findByEmail(String email);
    UserDTO findByEmailAndPassword(String email, String password);
    boolean existsByEmailOrName(String name, String email);
}
