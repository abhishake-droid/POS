package com.increff.pos.dao;

import com.increff.pos.db.InventoryPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Repository;

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

    /**
     * Atomically updates quantity for a productId; creates inventory row if
     * missing.
     * This avoids "duplicate key" errors caused by insert-vs-update races.
     */
    public InventoryPojo upsertQuantityByProductId(String productId, Integer quantity) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        Update update = new Update()
                .setOnInsert("productId", productId)
                .set("quantity", quantity);
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        return mongoOperations.findAndModify(query, update, options, InventoryPojo.class);
    }
}
