package warehouse.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.model.ProductData;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<ProductData, String> {
    ProductData findByProductID(String productID);
    List<ProductData> findByWarehouseID(String warehouseID);
}