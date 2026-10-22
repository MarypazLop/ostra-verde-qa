package cr.ac.utn.ostraverde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de arranque de Spring Boot. La lógica de negocio está en el paquete servicio. */
@SpringBootApplication
public class OstraVerdeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OstraVerdeApplication.class, args);
    }
}
