package com.increff.pos.api;

import com.increff.pos.db.DailySalesPojo;
import java.time.LocalDate;
import java.util.List;

public interface DailySalesApi {
    DailySalesPojo add(DailySalesPojo pojo);

    DailySalesPojo update(String id, DailySalesPojo pojo);

    List<DailySalesPojo> getByDateRange(LocalDate startDate, LocalDate endDate);

    List<DailySalesPojo> getByDateRangeAndClient(LocalDate startDate, LocalDate endDate, String clientId);

    DailySalesPojo getByDateAndClient(LocalDate date, String clientId);

    List<DailySalesPojo> getByDate(LocalDate date);
}
