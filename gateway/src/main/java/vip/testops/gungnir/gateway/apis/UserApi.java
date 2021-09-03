package vip.testops.gungnir.gateway.apis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vip.testops.gungnir.gateway.apis.fallback.UserApiFallbackFactory;
import vip.testops.gungnir.gateway.entities.dto.UserDTO;

@FeignClient(name = "user", fallbackFactory = UserApiFallbackFactory.class)
public interface UserApi {
    @GetMapping("/user/authorize")
    Response<UserDTO> authorize(@RequestParam(value = "token", required = false) String token);
}
