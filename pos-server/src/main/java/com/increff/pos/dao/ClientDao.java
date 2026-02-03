package com.increff.pos.dao;

import com.increff.pos.db.ClientPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao(MongoOperations mongoOperations) {
        super(
                new MongoRepositoryFactory(mongoOperations)
                        .getEntityInformation(ClientPojo.class),
                mongoOperations);
    }

    public ClientPojo findByClientId(String clientId) {
        Query query = Query.query(Criteria.where("clientId").is(clientId));
        return mongoOperations.findOne(query, ClientPojo.class);
    }

    public ClientPojo findByPhone(String phone) {
        Query query = Query.query(Criteria.where("phone").is(phone));
        return mongoOperations.findOne(query, ClientPojo.class);
    }

    public ClientPojo findByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email));
        return mongoOperations.findOne(query, ClientPojo.class);
    }

    public ClientPojo findByName(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        return mongoOperations.findOne(query, ClientPojo.class);
    }

    @Override
    public Page<ClientPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }

    public ClientPojo findByNameOrPhoneOrEmail(String name, String phone, String email) {
        Query query = Query.query(new Criteria().orOperator(
                Criteria.where("name").is(name),
                Criteria.where("phone").is(phone),
                Criteria.where("email").is(email)));
        return mongoOperations.findOne(query, ClientPojo.class);
    }
}
