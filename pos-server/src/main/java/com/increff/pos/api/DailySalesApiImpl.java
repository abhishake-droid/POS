package com.increff.pos.api;

import com.increff.pos.dao.DailySalesDao;
import com.increff.pos.db.DailySalesPojo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailySalesApiImpl implements DailySalesApi {

    private final DailySalesDao dao;

    public DailySalesApiImpl(DailySalesDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailySalesPojo add(DailySalesPojo pojo) {
        return dao.save(pojo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailySalesPojo update(String id, DailySalesPojo pojo) {
        pojo.setId(id);
        return dao.save(pojo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySalesPojo> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return dao.findByDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySalesPojo> getByDateRangeAndClient(LocalDate startDate, LocalDate endDate, String clientId) {
        return dao.findByDateRangeAndClientId(startDate, endDate, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public DailySalesPojo getByDateAndClient(LocalDate date, String clientId) {
        return dao.findByDateAndClientId(date, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySalesPojo> getByDate(LocalDate date) {
        return dao.findByDate(date);
    }
}
