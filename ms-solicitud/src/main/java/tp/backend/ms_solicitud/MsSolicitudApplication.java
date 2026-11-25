package tp.backend.ms_solicitud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class MsSolicitudApplication {

	public static void main(String[] args) {
		// Force JVM timezone to UTC to avoid invalid TimeZone negotiation with Postgres
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		System.setProperty("user.timezone", "UTC");
		SpringApplication.run(MsSolicitudApplication.class, args);
	}

}
