package vip.testops.gungnir;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import vip.testops.gungnir.services.ExecutionDataService;

import java.io.IOException;

@SpringBootApplication
@EnableEurekaClient
@Slf4j
public class GungnirApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GungnirApplication.class, args);
        ExecutionDataService service = context.getBean(ExecutionDataService.class);
        try {
            service.receiver();
        } catch (IOException e) {
            log.error("system error.", e);
        }
    }

}
