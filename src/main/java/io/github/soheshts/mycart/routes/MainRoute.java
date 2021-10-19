package io.github.soheshts.mycart.routes;

import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClients;
import io.github.soheshts.mycart.models.Item;
import io.github.soheshts.mycart.models.ItemPrice;
import io.github.soheshts.mycart.models.StockDetails;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MainRoute extends RouteBuilder {
    @Inject
    CamelContext context;

    @Override
    public void configure() throws Exception {
        context.getRegistry().bind("mongobean", MongoClients.create("mongodb://localhost:27017"));

        onException(Exception.class).handled(false).transform().constant("Error occured")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500));

        restConfiguration().component("servlet").host("localhost").port(8080)
                .bindingMode(RestBindingMode.auto);
        rest()
                .get("/health").route().log("Hello there").setBody(constant("Service is UP")).endRest()

                .get("/findall").route().to("mongodb:mongobean?database=MyCart&collection=item&operation=findAll").endRest()

                .get("/findById").route().setBody(header("id")).convertBodyTo(String.class)
                .log("test log")
                .to("mongodb:mongobean?database=MyCart&collection=item&operation=findById").process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Message message = exchange.getMessage();
                        System.out.println("Class: " + message.getBody().getClass());
                        Document document = message.getBody(Document.class);
                        Item item = new Item();
                        item.set_id(document.getString("_id"));
                        item.setItemName(document.getString("itemName"));
                        item.setCategoryName(document.getString("categoryId"));
                        item.setSpecialProduct(document.getBoolean("specialProduct"));
                        Document itemPriceDocument = document.get("itemPrice",Document.class);
                        ItemPrice itemPrice = new ItemPrice();
                        itemPrice.setBasePrice(itemPriceDocument.getInteger("basePrice"));
                        itemPrice.setSellingPrice(itemPriceDocument.getInteger("sellingPrice"));
                        item.setItemPrice(itemPrice);
                        Document stockDetailsDocument = document.get("stockDetails", Document.class);
                        StockDetails stockDetails = new StockDetails();
                        stockDetails.setAvailableStock(stockDetailsDocument.getInteger("availableStock"));
                        stockDetails.setUnitOfMeasure(stockDetailsDocument.getString("unitOfMeasure"));
                        item.setStockDetails(stockDetails);
                        message.setBody(item);
                        exchange.setMessage(message);
                        System.out.println(item.toString());
                    }
                })
                /*.setBody(simple("${body}"))*/.endRest()
                //.setBody(unma).endRest()

                .post("/insert").route().to("mongodb:mongobean?database=MyCart&collection=item&operation=insert")
                .setBody(simple("${body}")).endRest();

    }
}
