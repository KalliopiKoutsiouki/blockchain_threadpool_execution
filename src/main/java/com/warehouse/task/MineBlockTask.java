package com.warehouse.task;

import com.warehouse.BlockchainPrjApplication;
import com.warehouse.model.ProductDto;
import com.warehouse.utils.AppConstants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MineBlockTask implements Callable {

    private int startNonce;
    private int end;
    BlockchainPrjApplication.SharedFlag stopFlag = BlockchainPrjApplication.SharedFlag.getInstance();
    private String hash;
    private String previousHash;
    private ProductDto productData;
    private long timeStamp;

    public MineBlockTask(int startNonce, int end, String previousHash, ProductDto productData) {
        this.startNonce = startNonce;
        this.end = end;
        this.previousHash = previousHash;
        this.productData = productData;

    }

    @Override
    public HashNonce call() throws Exception {
        String prefixString = new String(new char[AppConstants.PREFIX]).replace('\0', '0');
        while (!stopFlag.isFlagSet()) {
            if (startNonce > end) {
                return null;
            }
            String hash = calculateBlockHash(startNonce);
            startNonce++;
            if (hash.substring(0, AppConstants.PREFIX).equals(prefixString)) {
                System.out.println("Condition met by thread: " + Thread.currentThread().getId());
                System.out.println("Nonce value: " + startNonce);
                System.out.println("Hash: " + hash);
                stopFlag.setFlag(true);
                return new HashNonce(hash, startNonce);
            }
        }
        return null;
    }

    public String calculateBlockHash(int nonce){
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


}
