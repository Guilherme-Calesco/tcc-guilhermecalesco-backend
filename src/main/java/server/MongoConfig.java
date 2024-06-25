package server;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Bean
    public SimpleMongoClientDatabaseFactory mongoDbFactory() {
        String connectionString = "mongodb+srv://guilhermecalesco:knIRClvV4IhaN7o7@cluster01.lcjrzbg.mongodb.net/tcc-guilherme?retryWrites=true&w=majority&appName=Cluster01";
        return new SimpleMongoClientDatabaseFactory(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory());
    }
}
