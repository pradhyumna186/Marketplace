package com.marketplace.StoneRidgeMarketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StoneRidgeMarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoneRidgeMarketplaceApplication.class, args);
	}

}
