package io.github.soheshts.mycart.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Projections;
import io.github.soheshts.mycart.jaxb.Inventory;
import io.github.soheshts.mycart.jaxb.Reviews;
import io.github.soheshts.mycart.models.Item;
import io.github.soheshts.mycart.models.ItemPrice;
import io.github.soheshts.mycart.models.Items;
import io.github.soheshts.mycart.models.StockDetails;
import io.github.soheshts.mycart.processor.MappingProcessor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

@ApplicationScoped
public class MainRoute extends RouteBuilder {
    @Inject
    CamelContext context;
    Logger logger = LoggerFactory.getLogger(MainRoute.class);

    @Override
    public void configure() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        context.getRegistry().bind("mongobean", MongoClients.create("mongodb://localhost:27017"));
        context.getRegistry().bind("amqfactory", factory);
        JacksonDataFormat format = new JacksonDataFormat(Items.class);
        format.setAllowUnmarshallType(true);
        context.getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
        getContext().getGlobalOptions().put("CamelJacksonTypeConverterToPojo", "true");

        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(Inventory.class.getPackage().getName());


        onException(Exception.class).handled(false).transform().constant("Error occured")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500));

        restConfiguration().component("servlet").host("localhost").port(8080)
                .bindingMode(RestBindingMode.json);
        rest()
                .get("/health").route().log("Hello there").setBody(constant("Service is UP")).endRest()

                .get("/findall").route().to("direct:findall").endRest()
                /*REQUIREMENT 1*/
                .get("/findById").route().setBody(header("id")).convertBodyTo(String.class)
                .to("mongodb:mongobean?database=MyCart&collection=item&operation=findById").process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Message message = exchange.getMessage();
                        System.out.println("Class: " + message.getBody().getClass());
                        Document document = message.getBody(Document.class);
                        Item item = new Item();
                        item.set_id(document.getString("_id"));
                        item.setItemName(document.getString("itemName"));
                        item.setCategoryId(document.getString("categoryId"));
                        item.setSpecialProduct(document.getBoolean("specialProduct"));
                        Document itemPriceDocument = document.get("itemPrice", Document.class);
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
                }).endRest()
                /*REQUIREMENT 2*/
                .get("/items/{category_id}").route().process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String catId = exchange.getIn().getHeader("category_id").toString();
                        String special = null;
                        String searchString = null;
                        boolean includeSpecial = false;
                        if (exchange.getIn().getHeader("includeSpecial") != null) {
                            special = exchange.getIn().getHeader("includeSpecial").toString();
                            if (special.equalsIgnoreCase("true")) {
                                includeSpecial = true;
                            }
                            searchString = "{ \"categoryId\":\"" + catId + "\",\"specialProduct\":" + includeSpecial + "}";
                        } else {
                            searchString = "{ \"categoryId\":\"" + catId + "\"}";
                        }
                        logger.info("searchString : " + searchString);
                        exchange.getIn().setBody(BasicDBObject.parse(searchString));

                    }
                }).log("**** TEST")
                .to("mongodb:mongobean?database=MyCart&collection=item&operation=findAll").endRest()
                /*REQUIREMENT 3*/
                .post("/insert").route().to("direct:insertRoute").endRest()
                .post("/updateInventory").route().process(exchange -> {
                    Items items = new Items();
                    items = exchange.getMessage().getBody(Items.class);
                    logger.info("Items Mapped: " + items.toString());

                    exchange.getIn().setBody(items);
                }).split(simple("${body.items}"))
                .log("JSON : ${body}").endRest()
                .post("/amq").route().to("activemq:queue:mycart?connectionFactory=amqfactory&exchangePattern=InOnly").endRest();

        from("direct:insertRoute").routeId("direct:insertRoute").setProperty("ORIGINAL", body()).setBody(simple("${body[_id]}")).process(exchange -> {
                    exchange.getIn().setHeader(MongoDbConstants.FIELDS_PROJECTION, Projections.exclude("review", "lastUpdateDate"));
                })
                .to("mongodb:mongobean?database=MyCart&collection=item&operation=findById")
                .choice()
                .when(simple("${body[_id]} != null"))
                .log("response : " + body()).setBody(body())
                .otherwise()
                .setBody(exchange -> exchange.getProperty("ORIGINAL"))
                .to("mongodb:mongobean?database=MyCart&collection=item&operation=insert")
                .setBody(simple("${body}")).endChoice();

        //from("activemq:queue:mycart?connectionFactory=amqfactory").routeId("AMQ ROUTE").log("message received : ${body}").to("direct:insertRoute");
        from("cron:tab?schedule=0/15 * * * * ?").routeId("CRON JOB").setBody().constant("event")
                .to("direct:findall").process(exchange -> {
                    Item item = new Item();
                    List<Document> items = exchange.getMessage().getBody(List.class);
                    exchange.getMessage().setBody(items);
                }).split(body()).process(new MappingProcessor()).log("Mapped: ${body}").multicast().to("direct:StoreFrontApp","direct:ItemTrendAnalyzer", "direct:ItemReviewAggregator");
        from("direct:findall").routeId("direct findall").to("mongodb:mongobean?database=MyCart&collection=item&operation=findAll").log("Findall: ${body}");
        from("direct:ItemTrendAnalyzer").routeId("ItemTrendAnalyzer").process(exchange -> {
            Inventory inventory = new Inventory();
            Item item = exchange.getMessage().getBody(Item.class);
            Inventory.Category.Item item1 = new Inventory.Category.Item();
            item1.setItemId(item.get_id());
            item1.setCategoryId(item.getCategoryId());
            item1.setAvailableStock(BigInteger.valueOf(item.getStockDetails().getAvailableStock()));
            item1.setSellingPrice(BigInteger.valueOf(item.getItemPrice().getSellingPrice()));
            Inventory.Category category = new Inventory.Category();
            Inventory.Category.CategoryName categoryName = new Inventory.Category.CategoryName();
            categoryName.setName(item.getCategoryId());
            category.setCategoryName(categoryName);
            inventory.getCategory().add(category);
            inventory.getCategory().get(0).getItem().add(item1);
            exchange.getMessage().setBody(inventory);

        }).marshal(jaxbDataFormat).log("Unmarshalled: ${body}")
                .to("sftp://ftpuser@localhost/ItemTrendAnalyzer?password=ftp123&exchangePattern=InOnly&strictHostKeyChecking=no");
        from("direct:ItemReviewAggregator").routeId("ItemReviewAggregator").process(exchange -> {
            Reviews reviews = new Reviews();
            Item item = exchange.getMessage().getBody(Item.class);
            Reviews.Item item1 = new Reviews.Item();
            item1.setId(item.get_id());
            logger.info("$$$### Review lIST: " + item.getReview());
            if (item.getReview() != null && item.getReview().size() > 0) {
                item.getReview().forEach(review -> {
                    logger.info("####### Inside loop");
                    Reviews.Item.Review review1 = new Reviews.Item.Review();
                    review1.setReviewrating(Short.valueOf(review.getRating().substring(0, 1)));
                    review1.setReviewcomment(review.getComment());
                    item1.getReview().add(review1);
                });
            }
            reviews.getItem().add(item1);
            logger.info("###OBJECT: " + new ObjectMapper().writeValueAsString(reviews));
            exchange.getMessage().setBody(reviews);
        }).marshal(jaxbDataFormat).log("ItemReviewAggregator marshalled:  ${body}")
                .to("sftp://ftpuser@localhost/ItemReviewAggregator?password=ftp123&exchangePattern=InOnly&strictHostKeyChecking=no");
        from("direct:StoreFrontApp").routeId("StoreFrontApp").process(exchange -> {
            Item item = exchange.getMessage().getBody(Item.class);
            if (item.getReview()!= null){
                item.setReview(null);
            }
            exchange.getMessage().setBody(item);
        }).marshal().json().to("sftp://ftpuser@localhost/StoreFrontApp?password=ftp123&exchangePattern=InOnly&strictHostKeyChecking=no");
    }
}
