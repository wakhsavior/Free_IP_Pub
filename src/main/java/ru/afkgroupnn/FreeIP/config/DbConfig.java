package ru.afkgroupnn.FreeIP.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.afkgroupnn.FreeIP.model.IPClass;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@Data
@RequiredArgsConstructor

@ConfigurationProperties(prefix = "db.connection")
public class DbConfig {
    private String dbDriver;
    private String dbUrl;
    private String dbName;
    private String dbUserName;
    private String dbPass;
    private String dbPort;
    private String dbEncoding;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(dbDriver));
        if (!dbPort.equals("")){
            dbPort = ":" + dbPort;
        }
        dataSource.setUrl(dbUrl + dbPort + "/" + dbName);
        dataSource.setUsername(dbUserName);
        dataSource.setPassword(dbPass);

        return dataSource;
    }
    @Bean
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }
}
