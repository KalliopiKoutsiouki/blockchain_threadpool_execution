# Blockchain and Multithreading - V.2

The application's main task is adding products to a blockchain. Each block is stored in a h2 database for reference.
The web api is exposing the following services:
- Search for a product by any of its property and value. /api/blockchain/searchFor/{propertyName}/{value}
- Save a product. Mines a new block with the product's data and adds it to the blockchain. /api/blockchain/save
- Save more than one products. /api/blockchain/saveAll
- Monitor the shifting of a products price through time. /api/blockchain/seePriceShifting/{productCode}


# Optimization of Mining process

Mining for the next block is a time consuming operation, hence the goal of this logic is to mine the nonce for a given product in a parallelized manner. 
The mining process is divided into multiple tasks, each one responsible for a subrange of nonce values. 
This version of the application implements the ExecutorService to manage and execute these tasks concurrently with a volatile flag whose purpose is to terminate the mining tasks as soon as a thread finds the nonce.

# JMH Benchmarking 

For measuring the results of parallelism, jmh has been integrated to the application.

Sequential implementation: 
![sequencial_benchmarking](https://github.com/KalliopiKoutsiouki/blockchain_threadpool_execution/assets/59616356/b9b8de87-90db-49d1-8010-d1f72bb91cc0)

3 threads with a nonce step of 10.000 searches per task:
![v2_multithreading_benchmarking](https://github.com/KalliopiKoutsiouki/blockchain_threadpool_execution/assets/59616356/5d2b6eb6-fd66-46bb-8be1-9125551c5f2c)

# Written with
- Spring Boot 3 
- H2 Database 
- JMH 
- OpenAPI 3
