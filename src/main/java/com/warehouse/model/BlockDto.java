package com.warehouse.model;
import java.util.Date;

public final class BlockDto {
    private String hash;
    private final String previousHash;
    private ProductDto productData;
    private final long timeStamp;
    private int nonce;

    public BlockDto(String previousHash, ProductDto productData, String hash, int nonce) {
        this.previousHash = previousHash;
        this.productData = productData;
        this.timeStamp = new Date().getTime();
        this.hash = hash;
        this.nonce = nonce;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public ProductDto getProductData() {
        return productData;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

}

