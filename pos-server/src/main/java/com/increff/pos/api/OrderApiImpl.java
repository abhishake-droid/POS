package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderApiImpl implements OrderApi {
    private static final Logger logger = LoggerFactory.getLogger(OrderApiImpl.class);

    private final OrderDao orderDao;

    public OrderApiImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo add(OrderPojo orderPojo) throws ApiException {
        logger.info("Creating order with orderId: {}", orderPojo.getOrderId());

        // Check if orderId already exists
        if (orderDao.findByOrderId(orderPojo.getOrderId()) != null) {
            throw new ApiException("Order with ID " + orderPojo.getOrderId() + " already exists");
        }

        if (orderPojo.getOrderDate() == null) {
            orderPojo.setOrderDate(Instant.now());
        }

        return orderDao.save(orderPojo);
    }

    @Override
    public OrderPojo get(String id) throws ApiException {
        OrderPojo orderPojo = orderDao.findById(id).orElse(null);
        if (orderPojo == null) {
            throw new ApiException("Order with ID " + id + " does not exist");
        }
        return orderPojo;
    }

    @Override
    public OrderPojo getByOrderId(String orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.findByOrderId(orderId);
        if (orderPojo == null) {
            throw new ApiException("Order with orderId " + orderId + " does not exist");
        }
        return orderPojo;
    }

    @Override
    public Page<OrderPojo> getAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        return orderDao.findAll(pageRequest);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo update(String id, OrderPojo orderPojo) throws ApiException {
        OrderPojo existing = get(id);
        
        // Update fields
        if (orderPojo.getStatus() != null) {
            existing.setStatus(orderPojo.getStatus());
        }
        if (orderPojo.getTotalItems() != null) {
            existing.setTotalItems(orderPojo.getTotalItems());
        }
        if (orderPojo.getTotalAmount() != null) {
            existing.setTotalAmount(orderPojo.getTotalAmount());
        }

        return orderDao.save(existing);
    }

    @Override
    public List<OrderPojo> getWithFilters(String orderId, String status, Instant fromDate, Instant toDate) {
        return orderDao.findWithFilters(orderId, status, fromDate, toDate);
    }
}
