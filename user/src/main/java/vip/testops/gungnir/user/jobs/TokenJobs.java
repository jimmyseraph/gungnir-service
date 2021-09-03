package vip.testops.gungnir.user.jobs;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vip.testops.gungnir.user.dao.LoginCacheRepository;
import vip.testops.gungnir.user.entities.dto.LoginCache;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class TokenJobs {

    @Value("${token.expire}")
    private int tokenExpire;

    private LoginCacheRepository loginCacheRepository;

    @Autowired
    public void setLoginCacheRepository(LoginCacheRepository loginCacheRepository) {
        this.loginCacheRepository = loginCacheRepository;
    }

    @XxlJob("cleanExpiredTokenJobHandler")
    public void cleanExpiredTokenJobHandler(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, -1 * tokenExpire);
        List<LoginCache> loginCacheList = loginCacheRepository.findByCreateTimeBefore(calendar.getTime());
        if(loginCacheList != null){
            loginCacheRepository.deleteAll(loginCacheList);
        }
    }
}
