package com.entando.hub.catalog;

import com.entando.hub.catalog.persistence.OrganisationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class EntandoHubCatalogApplication {
	public static void main(String[] args) {
		SpringApplication.run(EntandoHubCatalogApplication.classError, args);
	}

}
