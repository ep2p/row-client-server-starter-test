package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.callback.GeneralCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {
    @Bean
    public GeneralCallback<String> generalCallback(){
        return new GeneralCallback<String>() {
            @Override
            public Class<String> getClassOfCallback() {
                return String.class;
            }

            @Override
            public void onMessage(String e) {
                System.out.println("General Callback Called: " + e);
            }
        };
    }

}
