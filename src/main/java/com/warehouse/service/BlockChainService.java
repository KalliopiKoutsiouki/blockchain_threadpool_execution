package com.warehouse.service;

import com.warehouse.model.BlockDto;
import com.warehouse.model.ProductDto;
import com.warehouse.model.ProductBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BlockChainService {

    void save(ProductDto product) throws Exception;

    void saveAll(List<ProductDto> productDtos) throws Exception;


    List<ProductDto> getAll();

    List<ProductDto> findByProductProperty(String propertyName, String propertyValue) throws Exception;

    Map<String, String> getPriceThroughTime(String productCode) throws Exception;
}

