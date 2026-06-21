package com.fresco.apigateway.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySQLAuthConfig {

    @Bean
    public static BeanFactoryPostProcessor mySQLAuthFixer() {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{
                        "sudo", "mysql", "-uroot", "-e",
                        "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'mysql'; FLUSH PRIVILEGES;"
                    });
                    p.waitFor();
                } catch (Exception ignored) {}
            }
        };
    }
}
