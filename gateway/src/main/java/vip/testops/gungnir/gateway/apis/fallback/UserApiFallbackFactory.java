package vip.testops.gungnir.gateway.apis.fallback;

import feign.hystrix.FallbackFactory;
import vip.testops.gungnir.gateway.apis.UserApi;
import vip.testops.gungnir.gateway.commons.Response;
import vip.testops.gungnir.gateway.entities.dto.UserDTO;

public class UserApiFallbackFactory implements FallbackFactory<UserApi> {
    @Override
    public UserApi create(Throwable throwable) {
        return token -> {
            Response<UserDTO> response = new Response<>();
            response.serviceError("Internal service is not available");
            return response;
        };
    }
}
