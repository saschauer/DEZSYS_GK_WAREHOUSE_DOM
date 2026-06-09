package warehouse.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.model.Warehouse;

@Repository
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {
}