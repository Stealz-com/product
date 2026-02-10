@echo off
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/product_db?createDatabaseIfNotExist=true
set SPRING_DATA_REDIS_HOST=localhost
set BARGAINING_SERVICE_URL=http://localhost:8091
mvn spring-boot:run
