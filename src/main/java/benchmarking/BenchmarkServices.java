package benchmarking;

import com.warehouse.BlockchainPrjApplication;
import com.warehouse.model.ProductDto;
import com.warehouse.repository.BlockRepository;
import com.warehouse.service.BlockChainService;
import com.warehouse.service.BlockChainServiceImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 4)
public class BenchmarkServices {

    private BlockRepository repository;

    private BlockChainService service;

    private ConfigurableApplicationContext context;
    private ProductDto genesisProductData;
    private ProductDto secondProductData;
    private List<ProductDto> productDtos;

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BenchmarkServices.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @TearDown
    public void closeContext(){
        context.close();
    }
    @Setup
    public void setUp() {
        context = SpringApplication.run(BlockchainPrjApplication.class);
        context.registerShutdownHook();
        repository = context.getBean(BlockRepository.class);
        service = new BlockChainServiceImpl(repository);
        genesisProductData= new ProductDto("123", "name0", 3, "this is for benchmarking", "benchmarking");
        secondProductData = new ProductDto("1234", "name1", 2.5, "this is for benchmarking", "benchmarking");
        productDtos = Arrays.asList(generateData());
    }


    private ProductDto[] generateData () {
    ProductDto productData2 = new ProductDto("123", "name2", 4, "this is for benchmarking", "benchmarking");
    ProductDto productData3 = new ProductDto("12345", "name3", 1, "this is for benchmarking", "benchmarking");
    ProductDto productData4 = new ProductDto("12", "name4", 0, "this is for benchmarking", "benchmarking");
        ProductDto products[] = new ProductDto[]{productData2,productData3,productData4};
    return products;
    }
    @Benchmark
    public void saveBlock() throws Exception {
        saveBlock(service, genesisProductData);
    }

    @Benchmark
    public void saveAll() throws Exception {

        saveAll(service, productDtos);
    }

    private static void saveBlock(BlockChainService service, ProductDto productData) throws Exception {
        service.save(productData);
    }

    private static void saveAll(BlockChainService service, List<ProductDto> productDtoList) throws Exception {
        service.saveAll(productDtoList);
    }

}
