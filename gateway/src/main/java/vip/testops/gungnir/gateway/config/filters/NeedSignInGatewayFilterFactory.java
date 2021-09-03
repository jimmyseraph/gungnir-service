package vip.testops.gungnir.gateway.config.filters;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import vip.testops.gungnir.gateway.commons.Response;
import vip.testops.gungnir.gateway.utils.FilterUtil;
import vip.testops.gungnir.gateway.utils.StringUtil;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class NeedSignInGatewayFilterFactory extends AbstractGatewayFilterFactory<NeedSignInGatewayFilterFactory.Config> {

    private static final String AUTHORIZE_TOKEN = "Access-Token";
    private static Gson gson = new Gson();

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("needAuth");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("--> Starting check access token.....");
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            HttpHeaders headers = request.getHeaders();

            if(!StringUtil.isEmptyOrNull(config.getNeedAuth()) && Boolean.parseBoolean(config.getNeedAuth())) {
                String token = headers.getFirst(AUTHORIZE_TOKEN);
                Response<?> response = new Response<>();
                if(StringUtil.isEmptyOrNull(token)) {
                    serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.unauthorizedError();
                    return FilterUtil.failedReturn(gson.toJson(response), serverHttpResponse);
                }
            }
            return chain.filter(exchange);
        };
    }

    public static class Config{
        private String needAuth;

        public Config(String needAuth) {
            this.needAuth = needAuth;
        }

        public Config() {
        }

        public String getNeedAuth() {
            return needAuth;
        }

        public void setNeedAuth(String needAuth) {
            this.needAuth = needAuth;
        }
    }
}
