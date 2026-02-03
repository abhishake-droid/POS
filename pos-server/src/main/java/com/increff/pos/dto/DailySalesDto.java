package com.increff.pos.dto;

import com.increff.pos.flow.DailySalesFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DailySalesDto {

    @Autowired
    private DailySalesFlow dailySalesFlow;

    public void aggregateDailySales() {
        dailySalesFlow.aggregateDailySales();
    }
}
