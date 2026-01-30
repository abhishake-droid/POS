package com.increff.pos.helper;

import com.increff.pos.db.OrderItemPojo;
import com.increff.pos.db.OrderPojo;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderLineForm;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class OrderHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static OrderItemPojo convertLineFormToEntity(OrderLineForm form, String orderId) {
        OrderItemPojo pojo = new OrderItemPojo();
        pojo.setOrderId(orderId);
        pojo.setProductId(form.getProductId());
        pojo.setQuantity(form.getQuantity());
        pojo.setMrp(form.getMrp());
        pojo.setLineTotal(form.getQuantity() * form.getMrp());
        return pojo;
    }

    public static OrderData convertToDto(OrderPojo pojo, boolean hasInvoice) {
        OrderData data = new OrderData();
        data.setId(pojo.getId());
        data.setOrderId(pojo.getOrderId());
        data.setStatus(pojo.getStatus());
        data.setTotalItems(pojo.getTotalItems());
        data.setTotalAmount(pojo.getTotalAmount());
        if (pojo.getOrderDate() != null) {
            data.setCreatedAt(pojo.getOrderDate().format(DATE_FORMATTER));
        }
        data.setHasInvoice(hasInvoice);
        return data;
    }

    public static OrderItemData convertItemToDto(OrderItemPojo pojo) {
        OrderItemData data = new OrderItemData();
        data.setId(pojo.getId());
        data.setOrderId(pojo.getOrderId());
        data.setProductId(pojo.getProductId());
        data.setBarcode(pojo.getBarcode());
        data.setProductName(pojo.getProductName());
        data.setQuantity(pojo.getQuantity());
        data.setMrp(pojo.getMrp());
        data.setLineTotal(pojo.getLineTotal());
        return data;
    }

    public static List<OrderItemData> convertItemsToDtoList(List<OrderItemPojo> pojoList) {
        return pojoList.stream().map(OrderHelper::convertItemToDto).collect(Collectors.toList());
    }
}
