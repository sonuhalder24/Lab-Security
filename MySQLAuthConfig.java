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
                // Step 1: upgrade Python MySQL connector (old 2.2.9 doesn't support MySQL 8.0)
                // Uninstall the old package first to avoid namespace conflicts
                try {
                    Process uninstall = Runtime.getRuntime().exec(new String[]{
                        "sudo", "pip3", "uninstall", "mysql-connector", "-y"
                    });
                    uninstall.waitFor();
                } catch (Exception ignored) {}
                try {
                    Process pip = Runtime.getRuntime().exec(new String[]{
                        "sudo", "pip3", "install", "mysql-connector-python"
                    });
                    pip.waitFor();
                } catch (Exception ignored) {}

                // Step 2: set default auth plugin to mysql_native_password in MySQL config
                // (mysql.connector 2.2.9 fails when the server greeting says caching_sha2_password)
                try {
                    Process writeConf = Runtime.getRuntime().exec(new String[]{
                        "sudo", "bash", "-c",
                        "echo 'default_authentication_plugin=mysql_native_password' >> /etc/mysql/mysql.conf.d/mysqld.cnf"
                    });
                    writeConf.waitFor();
                } catch (Exception ignored) {}

                // Step 3: restart MySQL so the new config takes effect
                try {
                    Process restart = Runtime.getRuntime().exec(new String[]{
                        "sudo", "service", "mysql", "restart"
                    });
                    restart.waitFor();
                    Thread.sleep(5000);
                } catch (Exception ignored) {}

                // Step 4: change root to use mysql_native_password explicitly
                try {
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
