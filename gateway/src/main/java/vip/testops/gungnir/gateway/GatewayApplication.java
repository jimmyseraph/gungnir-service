package vip.testops.gungnir.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import vip.testops.gungnir.gateway.utils.ContainerFetcher;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class GatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
        ContainerFetcher.setApplicationContext(run);
    }
}
