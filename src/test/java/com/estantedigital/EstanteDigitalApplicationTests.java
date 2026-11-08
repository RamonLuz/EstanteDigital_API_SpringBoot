package com.estantedigital;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.security.username=test-user",
        "app.security.password=test-password",
        "app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class EstanteDigitalApplicationTests {

	@Test
	void contextLoads() {
	}

}
