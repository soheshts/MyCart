package io.github.soheshts.mycart.routes;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.internal.MongoClientImpl;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.SimpleRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MainRoute extends RouteBuilder {
    @Inject
    CamelContext context;

    @Override
    public void configure() throws Exception {
        context.getRegistry().bind("mongobean", MongoClients.create("mongodb://localhost:27017"));
        restConfiguration().component("servlet").host("localhost").port(8080)
                .bindingMode(RestBindingMode.auto);
        rest()
                .get("/health").route().log("Hello there").setBody(constant("Service is UP")).endRest()
                .post("/findall").to("mongodb:mongobean?database=example&collection=names&operation=findAll");
    }
}
