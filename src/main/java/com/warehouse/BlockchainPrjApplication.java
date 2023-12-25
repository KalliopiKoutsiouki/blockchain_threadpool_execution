package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class BlockchainPrjApplication {
    public static void main(String[] args) {

        SpringApplication.run(BlockchainPrjApplication.class, args);
    }

    public static class SharedFlag {
        private AtomicBoolean flag = new AtomicBoolean(false);
        private static final SharedFlag instance = new SharedFlag();
        private SharedFlag() {
        }

        public static SharedFlag getInstance() {
            return instance;
        }

        public boolean isFlagSet() {
            return flag.get();
        }

        public void setFlag(boolean value) {
            flag.set(value);
        }
    }

}
