package io.github.soheshts.mycart.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MainRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet").host("localhost").port(8080)
                .bindingMode(RestBindingMode.auto);
        rest("/hello")
                .get().route().log("Hello there")
                .transform().constant("Hello there").endRest()
                .post().route().transform().constant("POST Received");
    }
}
