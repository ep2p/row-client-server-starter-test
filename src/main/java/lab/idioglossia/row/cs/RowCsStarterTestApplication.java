package lab.idioglossia.row.cs;

import lab.idioglossia.row.server.annotations.EnableRowPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRowPublisher
public class RowCsStarterTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RowCsStarterTestApplication.class, args);
	}

}
