package vip.testops.gungnir.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import vip.testops.gungnir.converters.*;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?,?>> converterList = new ArrayList<>();
        converterList.add(new ExecutionDataReadConverter());
        converterList.add(new BundleCoverageReadConverter());

        return new MongoCustomConversions(converterList);
    }
}
