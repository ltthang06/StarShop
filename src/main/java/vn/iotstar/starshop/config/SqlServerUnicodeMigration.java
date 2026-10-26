package vn.iotstar.starshop.config;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @org.springframework.core.annotation.Order(-110)
public class SqlServerUnicodeMigration implements ApplicationRunner {
    private final DataSource dataSource;
    @Override public void run(ApplicationArguments arguments) throws Exception {
        try(var connection=dataSource.getConnection()) {
            if(!connection.getMetaData().getDatabaseProductName().equals("Microsoft SQL Server")) return;
            try(var input=new ClassPathResource("db/sqlserver-unicode.sql").getInputStream();var statement=connection.createStatement()) {
                statement.execute(new String(input.readAllBytes(),StandardCharsets.UTF_8));
            }
        }
    }
}
