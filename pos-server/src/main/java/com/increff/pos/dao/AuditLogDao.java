package com.increff.pos.dao;

import com.increff.pos.db.AuditLogPojo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Repository
public class AuditLogDao extends AbstractDao<AuditLogPojo> {
    public AuditLogDao(MongoOperations mongoOperations) {
        super(
            new MongoRepositoryFactory(mongoOperations)
                .getEntityInformation(AuditLogPojo.class),
            mongoOperations
        );
    }

    public List<AuditLogPojo> findByOperatorEmail(String operatorEmail) {
        Query query = Query.query(Criteria.where("operatorEmail").is(operatorEmail));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));
        return mongoOperations.find(query, AuditLogPojo.class);
    }

    public Page<AuditLogPojo> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }

    public List<AuditLogPojo> findAll() {
        Query query = new Query();
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"));
        return mongoOperations.find(query, AuditLogPojo.class);
    }
}
