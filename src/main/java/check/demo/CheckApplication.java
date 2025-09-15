package check.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class CheckApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(CheckApplication.class, args);
		Environment env = ctx.getEnvironment();

		log.info("spring.datasource.url={}", env.getProperty("spring.datasource.url"));
		log.info("spring.datasource.username={}", env.getProperty("spring.datasource.username"));
		log.info("spring.datasource.driverClassName={}", env.getProperty("spring.datasource.driverClassName"));
		log.info("spring.datasource.type={}", env.getProperty("spring.datasource.type=com.zaxxer.hikari.HikariDataSource"));
	}

}
