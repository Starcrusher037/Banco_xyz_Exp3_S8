package cl.duoc.bancoxyz.mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class MsMensajeriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsMensajeriaApplication.class, args);
    }
}
