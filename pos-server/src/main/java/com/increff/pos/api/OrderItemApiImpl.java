package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemApiImpl implements OrderItemApi {
    private static final Logger logger = LoggerFactory.getLogger(OrderItemApiImpl.class);

    private final OrderItemDao orderItemDao;

    public OrderItemApiImpl(OrderItemDao orderItemDao) {
        this.orderItemDao = orderItemDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderItemPojo add(OrderItemPojo orderItemPojo) throws ApiException {
        logger.info("Creating order item for orderId: {}", orderItemPojo.getOrderId());
        return orderItemDao.save(orderItemPojo);
    }

    @Override
    public OrderItemPojo get(String id) throws ApiException {
        OrderItemPojo orderItemPojo = orderItemDao.findById(id).orElse(null);
        if (orderItemPojo == null) {
            throw new ApiException("OrderItem with ID " + id + " does not exist");
        }
        return orderItemPojo;
    }

    @Override
    public List<OrderItemPojo> getByOrderId(String orderId) {
        return orderItemDao.findByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<OrderItemPojo> addBulk(List<OrderItemPojo> orderItemPojos) throws ApiException {
        logger.info("Creating {} order items", orderItemPojos.size());
        return orderItemDao.saveAll(orderItemPojos);
    }
}
