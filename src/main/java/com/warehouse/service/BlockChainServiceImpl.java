package com.warehouse.service;

import com.google.gson.Gson;
import com.warehouse.model.BlockDto;
import com.warehouse.model.ProductBlock;
import com.warehouse.model.ProductDto;
import com.warehouse.repository.BlockRepository;
import com.warehouse.utils.DateFormatter;
import com.warehouse.utils.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * BlockChain service layer
 */
@Service
public class BlockChainServiceImpl implements BlockChainService{

    private int prefix = 5;

    @Autowired
    BlockRepository blockRepository;

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
    public void save(ProductDto product) throws IllegalArgumentException{
        product.validateProductDtoInput();
        ProductBlock productBlock = getProductBlock(product);
        blockRepository.save(productBlock);
    }

    @Override
    public void saveAll(List<ProductDto> productDtos)  throws IllegalArgumentException{
        productDtos.forEach(this::save);
    }

    @Override
    public List<ProductDto> getAll() {
        List<ProductBlock> productBlocks = blockRepository.findAll();
        List<ProductDto> products = extractProductDtos(productBlocks);
        return products;
    }

//    @Override
//    public ProductBlock getById(String id) {
//        return null;
//    }
//
//    @Override
//    public ProductBlock findLastRecord() {
//        return null;
//    }

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




    public ProductBlock getProductBlock(ProductDto product) {
        addPreviousIdIfExists(product);
        String previousHash = getPreviousHash();
        BlockDto block = new BlockDto(previousHash, product );
        block.mineBlock(prefix);
        String blockJson = new Gson().toJson(block);
        ProductBlock productBlock = new ProductBlock(blockJson);
        return productBlock;
    }

    private StringBuilder propertyQueryParam(String propertyName, String propertyValue) {
        StringBuilder productCodeQuery = new StringBuilder();
        productCodeQuery.append(",");
        productCodeQuery.append("\"").append(propertyName).append("\":");
        productCodeQuery.append("\"").append(propertyValue).append("\",");
        return productCodeQuery;

    }
    private void addPreviousIdIfExists(ProductDto product) {
        StringBuilder productCodeParam = propertyQueryParam("productCode", product.getProductCode());
        List<ProductBlock> allEntriesForProduct = blockRepository.findProductContainingParam(productCodeParam.toString());
        if (!allEntriesForProduct.isEmpty()) {
            ProductBlock pb = allEntriesForProduct.get(0); // the list is ordered by descending id
            BlockDto lastBlockWithProduct = pb.extractBlockDto();
            String previousId = String.valueOf(lastBlockWithProduct.getProductData().getId());
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
