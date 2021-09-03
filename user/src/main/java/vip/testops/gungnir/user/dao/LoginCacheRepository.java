package vip.testops.gungnir.user.dao;

import org.springframework.data.repository.PagingAndSortingRepository;
import vip.testops.gungnir.user.entities.dto.LoginCache;

import java.util.Date;
import java.util.List;

public interface LoginCacheRepository extends PagingAndSortingRepository<LoginCache, String> {
    LoginCache findByUserIdAndAndToken(String userId, String token);
    List<LoginCache> findByCreateTimeBefore(Date date);
}
