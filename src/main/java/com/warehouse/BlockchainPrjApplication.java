package com.warehouse;

import com.google.gson.Gson;
import com.warehouse.model.BlockDto;
import com.warehouse.model.ProductDto;
import com.warehouse.model.ProductBlock;
import com.warehouse.repository.BlockRepository;
import com.warehouse.service.BlockChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlockchainPrjApplication {

    public static void main(String[] args) {

        SpringApplication.run(BlockchainPrjApplication.class, args);
    }

}
