package xyz.anomatver.blps.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSource() {
        return new AtomikosDataSourceBean();
    }
}