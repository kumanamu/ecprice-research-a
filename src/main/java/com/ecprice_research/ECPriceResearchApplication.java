package com.ecprice_research;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ecprice_research.config")
public class ECPriceResearchApplication {
	public static void main(String[] args) {
		SpringApplication.run(ECPriceResearchApplication.class, args);
	}
}