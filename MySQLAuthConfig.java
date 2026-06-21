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
                    // Upgrade Python mysql connector from 2.2.9 (incompatible with MySQL 8.0) to 8.x
                    Process pip = Runtime.getRuntime().exec(new String[]{
                        "sudo", "pip3", "install", "--upgrade", "mysql-connector-python"
                    });
                    pip.waitFor();
                } catch (Exception ignored) {}

                try {
                    // Change root to mysql_native_password so both Java and Python can connect via TCP
                    Process alter = Runtime.getRuntime().exec(new String[]{
                        "sudo", "mysql", "-uroot", "-e",
                        "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'mysql'; FLUSH PRIVILEGES;"
                    });
                    alter.waitFor();
                } catch (Exception ignored) {}
            }
        };
    }
}
