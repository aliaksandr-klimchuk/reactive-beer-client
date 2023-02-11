package alex.klimchuk.reactive.beer.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Copyright Alex Klimchuk (c) 2023.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ReactiveBeerClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveBeerClientApplication.class, args);
    }

}
