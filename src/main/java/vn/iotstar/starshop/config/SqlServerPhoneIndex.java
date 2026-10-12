package vn.iotstar.starshop.config;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
@Component @RequiredArgsConstructor
@org.springframework.core.annotation.Order(-100)
public class SqlServerPhoneIndex implements ApplicationRunner {
    private final DataSource dataSource;
    @Override public void run(ApplicationArguments arguments) throws Exception {
        try(var connection=dataSource.getConnection()) {
            if (!connection.getMetaData().getDatabaseProductName().equals("Microsoft SQL Server")) return;
            try(var input=new ClassPathResource("db/sqlserver-phone-index.sql").getInputStream();
                var statement=connection.createStatement()) {
                statement.execute(new String(input.readAllBytes(),StandardCharsets.UTF_8));
            }
        }
    }
}
