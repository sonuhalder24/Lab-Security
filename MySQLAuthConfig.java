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
                    // Write MySQL config to force native password + skip SSL (needed for old Python connector 2.x)
                    Process writeConf = Runtime.getRuntime().exec(new String[]{
                        "sudo", "bash", "-c",
                        "printf '[mysqld]\\ndefault_authentication_plugin=mysql_native_password\\nskip_ssl\\n' > /etc/mysql/conf.d/native_auth.cnf"
                    });
                    writeConf.waitFor();

                    // Restart MySQL so the config takes effect
                    Process restart = Runtime.getRuntime().exec(new String[]{"sudo", "service", "mysql", "restart"});
                    restart.waitFor();

                    // Now change root auth plugin to mysql_native_password with known password
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
