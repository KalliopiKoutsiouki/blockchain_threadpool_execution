package com.warehouse.task;

public class HashNonce {

    private String hash;
    private int nonce;

    public HashNonce(String hash, int nonce) {
        this.hash = hash;
        this.nonce = nonce;
    }

    public HashNonce(){};

    public String getHash() {
        return hash;
    }

    public int getNonce() {
        return nonce;
    }
}
