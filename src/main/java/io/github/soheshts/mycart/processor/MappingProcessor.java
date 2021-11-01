package io.github.soheshts.mycart.processor;

import io.github.soheshts.mycart.models.Item;
import io.github.soheshts.mycart.models.ItemPrice;
import io.github.soheshts.mycart.models.Review;
import io.github.soheshts.mycart.models.StockDetails;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MappingProcessor implements Processor {
    Logger logger = LoggerFactory.getLogger(MappingProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("\n### processor class: " + exchange.getMessage().getBody().getClass() + "\n");
        Document document = exchange.getMessage().getBody(Document.class);
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
        List<Review> review = new ArrayList<>();
        if (document.get("review") != null) {
            List<Document> reviewDocuments = document.get("review", List.class);
            reviewDocuments.forEach(document1 -> {
                Review review1 = new Review();
                review1.setRating(document1.getString("rating"));
                review1.setComment(document1.getString("comment"));
                review.add(review1);
            });
        }
        item.setReview(review);

        exchange.getMessage().setBody(item);
    }
}
