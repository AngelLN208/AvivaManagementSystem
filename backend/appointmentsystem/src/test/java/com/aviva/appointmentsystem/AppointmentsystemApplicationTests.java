package com.aviva.appointmentsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"app.notifications.scheduler-enabled=false",
	"brevo.api-key=test-key"
})
class AppointmentsystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
