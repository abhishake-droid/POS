package com.increff.pos.util;

import com.increff.pos.db.CounterPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class SequenceGenerator {

    @Autowired
    private MongoOperations mongoOperations;

    public long getNextSequence(String key) {

        Query query = new Query(where("_id").is(key));
        Update update = new Update().inc("sequence", 1);

        CounterPojo counter = mongoOperations.findAndModify(
                query,
                update,
                FindAndModifyOptions.options()
                        .returnNew(true)
                        .upsert(true),
                CounterPojo.class
        );

        return counter.getSequence();
    }
}
