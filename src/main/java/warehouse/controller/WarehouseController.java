package warehouse.controller;

import org.springframework.web.bind.annotation.*;
import warehouse.model.Warehouse;
import warehouse.model.ProductData;
import warehouse.repository.ProductRepository;
import warehouse.repository.WarehouseRepository;
import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public WarehouseController(WarehouseRepository warehouseRepository, ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public Warehouse addWarehouse(@RequestBody Warehouse newWarehouse) {
        return warehouseRepository.save(newWarehouse);
    }

    @GetMapping
    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        for (Warehouse wh : warehouses) {
            List<ProductData> products = productRepository.findByWarehouseID(wh.getWarehouseID());
            wh.setProductData(products);
        }
        return warehouses;
    }

    @GetMapping("/{id}")
    public Warehouse getWarehouseById(@PathVariable String id) {
        Warehouse wh = warehouseRepository.findById(id).orElse(null);
        if (wh != null) {
            wh.setProductData(productRepository.findByWarehouseID(id));
        }
        return wh;
    }

    @DeleteMapping("/{id}")
    public void deleteWarehouse(@PathVariable String id) {
        warehouseRepository.deleteById(id);
        List<ProductData> products = productRepository.findByWarehouseID(id);
        productRepository.deleteAll(products);
    }
}