package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.db.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ProductApiImpl implements ProductApi {

    private final ProductDao productDao;

    public ProductApiImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public ProductPojo add(ProductPojo pojo) throws ApiException {
        return productDao.save(pojo);
    }

    @Override
    public List<ProductPojo> addBulk(List<ProductPojo> pojos) throws ApiException {
        return productDao.saveAll(pojos);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPojo getCheck(String id) throws ApiException {
        return productDao.findById(id)
                .orElseThrow(() -> new ApiException("Product with ID " + id + " does not exist"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPojo getCheckByBarcode(String barcode) throws ApiException {
        return productDao.findByBarcode(barcode)
                .orElseThrow(() -> new ApiException("Product with barcode " + barcode + " does not exist"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPojo> getAll(PageForm form) {
        Pageable pageable = PageRequest.of(form.getPage(), form.getSize(), Sort.by("id").descending());
        return productDao.findAll(pageable);
    }

    @Override
    public ProductPojo update(String id, ProductPojo pojo) throws ApiException {
        ProductPojo existing = getCheck(id);
        existing.setName(pojo.getName());
        existing.setMrp(pojo.getMrp());
        existing.setImageUrl(pojo.getImageUrl());
        return productDao.save(existing);
    }
}
