package com.increff.pos.dao;

import com.increff.pos.db.ClientPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    public java.util.List<ClientPojo> findByClientIds(@NonNull java.util.List<String> clientIds) {
        Query query = Query.query(Criteria.where("clientId").in(clientIds));
        return mongoOperations.find(query, ClientPojo.class);
    }

    public Page<ClientPojo> findWithFilters(String clientId, String name, String email, Pageable pageable) {
        Query query = buildFilterQuery(clientId, name, email);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        long total = mongoOperations.count(query, ClientPojo.class);
        query.with(pageable);
        List<ClientPojo> clients = mongoOperations.find(query, ClientPojo.class);

        return new PageImpl<>(clients, pageable, total);
    }

    private Query buildFilterQuery(String clientId, String name, String email) {
        Criteria criteria = new Criteria();

        if (clientId != null) {
            criteria = criteria.and("clientId").regex(clientId, "i");
        }
        if (name != null) {
            criteria = criteria.and("name").regex(name, "i");
        }
        if (email != null) {
            criteria = criteria.and("email").regex(email, "i");
        }

        return Query.query(criteria);
    }
}
