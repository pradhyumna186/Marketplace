package com.marketplace.StoneRidgeMarketplace;

import com.marketplace.StoneRidgeMarketplace.config.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class StoneRidgeMarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(StoneRidgeMarketplaceApplication.class);
		// Register DatabaseInitializer to run before DataSource initialization
		app.addListeners(new DatabaseInitializer());
		app.run(args);
	}

}
