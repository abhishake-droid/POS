package com.increff.pos.dao;

import com.increff.pos.db.InventoryPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {

    public InventoryDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(InventoryPojo.class),
                mongoOperations);
    }

    public InventoryPojo findByProductId(String productId) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        return mongoOperations.findOne(query, InventoryPojo.class);
    }

    public InventoryPojo upsertQuantityByProductId(String productId, Integer quantity) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        Update update = new Update()
                .setOnInsert("productId", productId)
                .set("quantity", quantity);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        return mongoOperations.findAndModify(query, update, options, InventoryPojo.class);
    }

    public InventoryPojo incrementQuantityByProductId(String productId, Integer quantityToAdd) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        Update update = new Update()
                .setOnInsert("productId", productId)
                .inc("quantity", quantityToAdd);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        return mongoOperations.findAndModify(query, update, options, InventoryPojo.class);
    }

    public java.util.List<InventoryPojo> findByProductIds(java.util.List<String> productIds) {
        Query query = Query.query(Criteria.where("productId").in(productIds));
        return mongoOperations.find(query, InventoryPojo.class);
    }

    public void bulkUpdateQuantities(java.util.Map<String, Integer> productIdToQuantity) {
        BulkOperations bulkOps = mongoOperations.bulkOps(BulkOperations.BulkMode.UNORDERED, InventoryPojo.class);

        for (java.util.Map.Entry<String, Integer> entry : productIdToQuantity.entrySet()) {
            Query query = Query.query(Criteria.where("productId").is(entry.getKey()));
            Update update = new Update()
                    .setOnInsert("productId", entry.getKey())
                    .set("quantity", entry.getValue());
            bulkOps.upsert(query, update);
        }

        bulkOps.execute();
    }
}
