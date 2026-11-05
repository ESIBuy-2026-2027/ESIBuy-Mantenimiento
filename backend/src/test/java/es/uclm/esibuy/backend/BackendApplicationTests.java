package es.uclm.esibuy.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Arranca la aplicación entera con el MongoDB de Testcontainers, no con Atlas:
// el CI no tiene MONGODB_URI y los tests no deben depender de la base real.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
