package com.increff.pos.api;

import com.increff.pos.db.DailySalesPojo;
import java.time.LocalDate;
import java.util.List;

public interface DailySalesApi {
    DailySalesPojo add(DailySalesPojo pojo);

    DailySalesPojo update(String id, DailySalesPojo pojo);

    DailySalesPojo getByDateAndClient(LocalDate date, String clientId);

    List<DailySalesPojo> getByDate(LocalDate date);
}
