package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class OrderApiImpl implements OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo add(OrderPojo orderPojo) throws ApiException {
        if (orderDao.findByOrderId(orderPojo.getOrderId()) != null) {
            throw new ApiException("Order with ID " + orderPojo.getOrderId() + " already exists");
        }

        if (orderPojo.getOrderDate() == null) {
            orderPojo.setOrderDate(ZonedDateTime.now());
        }

        return orderDao.save(orderPojo);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPojo getCheck(String id) throws ApiException {
        OrderPojo orderPojo = orderDao.findById(id).orElse(null);
        if (orderPojo == null) {
            throw new ApiException("Order with ID " + id + " does not exist");
        }
        return orderPojo;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPojo getCheckByOrderId(String orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.findByOrderId(orderId);
        if (orderPojo == null) {
            throw new ApiException("Order with orderId " + orderId + " does not exist");
        }
        return orderPojo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderPojo> getAll(PageForm form) {
        PageRequest pageRequest = PageRequest.of(form.getPage(), form.getSize(),
                Sort.by(Sort.Direction.DESC, "orderDate"));
        return orderDao.findAll(pageRequest);
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public OrderPojo update(String id, OrderPojo orderPojo) throws ApiException {
        OrderPojo existing = getCheck(id);
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
    @Transactional(readOnly = true)
    public List<OrderPojo> getWithFilters(String orderId, String status, ZonedDateTime fromDate, ZonedDateTime toDate) {
        return orderDao.findWithFilters(orderId, status, fromDate, toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderPojo> getWithFilters(String orderId, String status, ZonedDateTime fromDate, ZonedDateTime toDate,
            Pageable pageable) {
        return orderDao.findWithFilters(orderId, status, fromDate, toDate, pageable);
    }
}
