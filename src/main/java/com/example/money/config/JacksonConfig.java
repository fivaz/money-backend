package com.example.money.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate5Module() {
        Hibernate6Module module = new Hibernate6Module();
        // Don't force loading lazy-loaded entities during serialization
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        return module;
    }
}
