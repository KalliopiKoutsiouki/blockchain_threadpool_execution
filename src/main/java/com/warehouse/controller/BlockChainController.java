package com.warehouse.controller;

import com.warehouse.model.ProductDto;
import com.warehouse.service.BlockChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
public class BlockChainController {

    @Autowired
    BlockChainService blockChainService;

    @PostMapping("/save")
    public void saveProduct(@RequestBody ProductDto product) throws Exception {
        blockChainService.save(product);
    }

    @PostMapping("/saveAll")
    public void saveProduct(@RequestBody List<ProductDto> products) throws Exception {
         blockChainService.saveAll(products);
    }
    @GetMapping("/getAll")
    public List<ProductDto> getAllProducts() {
        return blockChainService.getAll();
    }

    @PostMapping("/searchFor/{propertyName}/{value}")
    public List<ProductDto> findByPropertyValue(@PathVariable String propertyName, @PathVariable String value) throws Exception {
        return blockChainService.findByProductProperty(propertyName, value);
    }

    @GetMapping("/seePriceShifting/{productCode}")
    public Map<String, String> getPriceThroughTime(@PathVariable String productCode) throws Exception {
        return blockChainService.getPriceThroughTime(productCode);
    }
}
