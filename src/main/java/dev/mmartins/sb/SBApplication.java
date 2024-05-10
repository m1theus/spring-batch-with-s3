package dev.mmartins.sb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SBApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SBApplication.class, args)));
    }

}
