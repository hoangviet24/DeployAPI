package product.management.electronic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElectronicApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectronicApplication.class, args);
    }
}
