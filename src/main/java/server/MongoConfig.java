package server;

import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientFactoryBean mongo() {
        MongoClientFactoryBean mongo = new MongoClientFactoryBean();
        mongo.setHost("mongodb+srv");
        return mongo;
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(MongoClients.create("mongodb+srv://guilhermecalesco:jrays9932" +
                "@cluster01.lcjrzbg.mongodb.net/" +
                "?retryWrites=true&w=majority&appName=Cluster01"),
                "yourdatabase");
    }
}
