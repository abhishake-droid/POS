package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class OrderItemApiImpl implements OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderItemPojo add(OrderItemPojo orderItemPojo) throws ApiException {
        return orderItemDao.save(orderItemPojo);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemPojo getCheck(String id) throws ApiException {
        return orderItemDao.findById(id)
                .orElseThrow(() -> new ApiException("OrderItem with ID " + id + " does not exist"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemPojo> getByOrderId(String orderId) {
        return orderItemDao.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemPojo> getByOrderIds(List<String> orderIds) {
        return orderItemDao.findByOrderIdIn(orderIds);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public List<OrderItemPojo> addBulk(List<OrderItemPojo> orderItemPojos) throws ApiException {
        return orderItemDao.saveAll(orderItemPojos);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public void delete(String id) throws ApiException {
        orderItemDao.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public void deleteByOrderId(String orderId) {
        orderItemDao.deleteByOrderId(orderId);
    }
}
