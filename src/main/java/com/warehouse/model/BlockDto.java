package com.warehouse.model;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public final class BlockDto {
    private String hash;
    private final String previousHash;
    private ProductDto productData;
    private final long timeStamp;
    private int nonce;

    public BlockDto(String previousHash, ProductDto productData) {
        this.previousHash = previousHash;
        this.productData = productData;
        this.timeStamp = new Date().getTime();
        this.hash = calculateBlockHash();
    }

    public String calculateBlockHash(){
        String dataToHash = previousHash+String.valueOf(timeStamp)
                +productData+String.valueOf(nonce);
        MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }

    public String mineBlock(int prefix){
        String prefixString = new String(new char[prefix]).replace('\0','0');
        while (!hash.substring(0,prefix).equals(prefixString)){
            nonce++;
            hash = calculateBlockHash();
        }
        return hash;
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

//    public ProductDto extractProductDto () {
//        return new Gson().fromJson(this.getProductData(), ProductDto.class);
//    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

}

