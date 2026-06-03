package gov.maricopa.reports.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "gov.maricopa.reports")
public class PropertyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyServiceApplication.class, args);
    }
}
