package com.increff.pos.dao;

import com.increff.pos.db.ProductPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductDao extends AbstractDao<ProductPojo> {

    public ProductDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(ProductPojo.class),
                mongoOperations);
    }

    public Optional<ProductPojo> findByBarcode(String barcode) {
        Query query = Query.query(Criteria.where("barcode").is(barcode));
        return Optional.ofNullable(mongoOperations.findOne(query, ProductPojo.class));
    }

    public ProductPojo updateFieldsById(String id, String clientId, String name, Double mrp, String imageUrl) {
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update();
        if (clientId != null) {
            update.set("clientId", clientId);
        }
        if (name != null) {
            update.set("name", name);
        }
        if (mrp != null) {
            update.set("mrp", mrp);
        }
        if (imageUrl != null) {
            update.set("imageUrl", imageUrl);
        }

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        return mongoOperations.findAndModify(query, update, options, ProductPojo.class);
    }

    public List<ProductPojo> findByClientId(String clientId) {
        Query query = Query.query(Criteria.where("clientId").is(clientId));
        return mongoOperations.find(query, ProductPojo.class);
    }

    public List<ProductPojo> findByIds(@NonNull List<String> ids) {
        Query query = Query.query(Criteria.where("_id").in(ids));
        return mongoOperations.find(query, ProductPojo.class);
    }

    public List<String> findBarcodesByBarcodes(@NonNull List<String> barcodes) {
        Query query = Query.query(Criteria.where("barcode").in(barcodes));
        query.fields().include("barcode");
        List<ProductPojo> products = mongoOperations.find(query, ProductPojo.class);
        return products.stream().map(ProductPojo::getBarcode).toList();
    }

    public List<ProductPojo> findByBarcodes(@NonNull List<String> barcodes) {
        Query query = Query.query(Criteria.where("barcode").in(barcodes));
        return mongoOperations.find(query, ProductPojo.class);
    }
}
