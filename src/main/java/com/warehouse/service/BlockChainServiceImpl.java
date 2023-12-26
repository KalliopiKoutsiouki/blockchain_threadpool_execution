package com.warehouse.service;

import com.google.gson.Gson;
import com.warehouse.BlockchainPrjApplication;
import com.warehouse.model.BlockDto;
import com.warehouse.model.ProductBlock;
import com.warehouse.model.ProductDto;
import com.warehouse.repository.BlockRepository;
import com.warehouse.task.HashNonce;
import com.warehouse.task.MineBlockTask;
import com.warehouse.utils.AppConstants;
import com.warehouse.utils.DateFormatter;
import com.warehouse.utils.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * BlockChain service layer
 */
@Service
public class BlockChainServiceImpl implements BlockChainService{

    @Autowired
    BlockRepository blockRepository;

   // overloaded for running benchmarking
    public BlockChainServiceImpl(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public BlockChainServiceImpl() {
    }

    /**
     * Perfoms checks and saves a new block into the blockchain db
     * @param product
     */
    @Override
    public void save(ProductDto product) throws Exception {
        product.validateProductDtoInput();
        try {
            ProductBlock productBlock = createProductBlock(product);
            if (productBlock!=null) {
                blockRepository.save(productBlock);
            } else {
                throw new Exception("Error in mining block");
            }
        } catch (RuntimeException re) {
            throw new Exception(re.getMessage());
        }
    }

    @Override
    public void saveAll(List<ProductDto> productDtos) throws Exception {
        for (ProductDto product : productDtos) {
            try {
                System.out.println(product.toString());
                save(product);
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    @Override
    public List<ProductDto> getAll() {
        List<ProductBlock> productBlocks = blockRepository.findAll();
        List<ProductDto> products = extractProductDtos(productBlocks);
        return products;
    }

    /**
     * Searches in db to find any products with the given property name and value also converts them to ProductDto
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws Exception
     */
    @Override
    public List<ProductDto> findByProductProperty(String propertyName, String propertyValue) throws Exception {
        validatePropertyInputs(propertyName, propertyValue);
        List<String> propertyList = getProductPropertyNames();
        Optional<String> property = propertyList.stream().filter(pr -> pr.equals(propertyName)).findAny();
        if(property.isPresent()) {
            StringBuilder buildQueryParam = propertyQueryParam(property.get(),propertyValue);
            List<ProductBlock> productsWithParam = blockRepository.findProductContainingParam(buildQueryParam.toString());
            if (!productsWithParam.isEmpty()) {
                return extractProductDtos(productsWithParam);
            } else throw new Exception("No product with " + propertyName + "=" + propertyValue +" found");
        }else throw new Exception("No such property");
    }

    @Override
    public Map<String, String> getPriceThroughTime(String productCode) throws Exception {
       if (!InputValidator.isValidString(productCode)) {
           throw new IllegalArgumentException("Invalid input data");
       }
        StringBuilder productCodeParam = propertyQueryParam("productCode", productCode);
        List<ProductBlock> allEntriesForProduct = blockRepository.findProductContainingParam(productCodeParam.toString());
        if (!allEntriesForProduct.isEmpty()) {
            Map<String, String> priceTimeMap = new HashMap<>();
            List<BlockDto> blockDtos = extractBlockDto(allEntriesForProduct);
            for (BlockDto block : blockDtos) {
                ProductDto productData = block.getProductData();
                String formattedDate = DateFormatter.getFormattedDate(block.getTimeStamp());
                priceTimeMap.put(formattedDate, String.valueOf(productData.getPrice()));
            }
            return priceTimeMap;
        } else throw new Exception("No records found for product with product code: " + productCode);
    }

    public ProductBlock createProductBlock(ProductDto product) throws RuntimeException, ExecutionException, InterruptedException {
        addPreviousIdIfExists(product);
        String previousHash = getPreviousHash();
        HashNonce hashNonce = new HashNonce();
        hashNonce = startParallelismToMineHash(product, previousHash, hashNonce);
        if (hashNonce!=null) {
            BlockDto block = new BlockDto(previousHash, product, hashNonce.getHash(), hashNonce.getNonce());
            String blockJson = new Gson().toJson(block);
            ProductBlock productBlock = new ProductBlock(blockJson);
            return productBlock;
        }
        return null;
    }

    /**
     * A method that uses parallelism to break down the mining process to a certain number of threads.
     * Each thread is searching for the nonce in a certain range of integers with a step of 10000.
     * When the right nonce is found, the thread sets a volatile flag variable to inform the other threads to stop their running process.
     * @param product
     * @param previousHash
     * @param hashNonce
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private HashNonce startParallelismToMineHash(ProductDto product, String previousHash, HashNonce hashNonce) throws ExecutionException, InterruptedException {
        ExecutorService executorService = resetFlagAndStartExecutorService();
        List<Future<HashNonce>> futures = new ArrayList<>();
        for (int i = 0; i <= AppConstants.TARGET_VALUE; i += AppConstants.INCREMENT_PER_THREAD) {
            int startNonce = i;
            int end = i + AppConstants.INCREMENT_PER_THREAD - 1;
            futures.add(executorService.submit(new MineBlockTask(startNonce, end, previousHash, product)));
        }
        executorService.shutdown();
        for (Future<HashNonce> future : futures) {
            try {
                if (future.get() != null) {
                    hashNonce = future.get();
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
            return hashNonce;
    }

    private static ExecutorService resetFlagAndStartExecutorService() {
        BlockchainPrjApplication.SharedFlag.getInstance().setFlag(false);
        ExecutorService executorService = Executors.newFixedThreadPool(AppConstants.NUM_THREADS);
        return executorService;
    }

    private StringBuilder propertyQueryParam(String propertyName, String propertyValue) {
        StringBuilder productCodeQuery = new StringBuilder();
        productCodeQuery.append(",");
        productCodeQuery.append("\"").append(propertyName).append("\":");
        productCodeQuery.append("\"").append(propertyValue).append("\",");
        return productCodeQuery;

    }
    private void addPreviousIdIfExists(ProductDto product) {
        String previousId = "0";
        StringBuilder productCodeParam = propertyQueryParam("productCode", product.getProductCode());
        List<ProductBlock> allEntriesForProduct = blockRepository.findProductContainingParam(productCodeParam.toString());
        if (!allEntriesForProduct.isEmpty()) {
            ProductBlock pb = allEntriesForProduct.get(0); // the list is ordered by descending id
            BlockDto lastBlockWithProduct = pb.extractBlockDto();
            previousId = String.valueOf(lastBlockWithProduct.getProductData().getId());
            product.setPreviousID(previousId);
        }
    }

    private String getPreviousHash() {
        String previousHash = "0";
        ProductBlock previousBlock = blockRepository.findFirstByOrderByIdDesc();
        if (previousBlock != null) {
            BlockDto previous = previousBlock.extractBlockDto();
            previousHash = previous.getHash();
        }
        return previousHash;
    }

    private static List<String> getProductPropertyNames() {
        List<String> propertyNames = new ArrayList<>();
        Field[] fields = ProductDto.class.getDeclaredFields();
        for (Field field : fields) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                propertyNames.add(field.getName());
            }
        }
        return propertyNames;
    }

    private static List<ProductDto> extractProductDtos(List<ProductBlock> productBlocks) {
        List<ProductDto> products = productBlocks.stream().map(ProductBlock::extractBlockDto).map(BlockDto::getProductData).toList();
        return products;
    }

    private static List<BlockDto> extractBlockDto(List<ProductBlock> productBlocks) {
        List<BlockDto> products = productBlocks.stream().map(ProductBlock::extractBlockDto).toList();
        return products;
    }

    private static void validatePropertyInputs(String propertyName, String propertyValue) {
        if (!InputValidator.isValidString(propertyName) || !InputValidator.isValidString(propertyValue) ) {
            throw new IllegalArgumentException("Invalid input data");
        }
    }

}
