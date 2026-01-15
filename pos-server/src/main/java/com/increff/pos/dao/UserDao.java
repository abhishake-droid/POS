package com.increff.pos.dao;

import com.increff.pos.db.UserPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends AbstractDao<UserPojo> {
    public UserDao(MongoOperations mongoOperations) {
        super(
            new MongoRepositoryFactory(mongoOperations)
                .getEntityInformation(UserPojo.class),
            mongoOperations
        );
    }

    public UserPojo findByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email));
        return mongoOperations.findOne(query, UserPojo.class);
    }

    @Override
    public Page<UserPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
}
