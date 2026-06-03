package cooperpay;

import cooperpay.config.AppDataDirectories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CooperpayApplication {

	public static void main(String[] args) {
		AppDataDirectories.ensureAppDataDirectories();
		SpringApplication.run(CooperpayApplication.class, args);
	}

}
