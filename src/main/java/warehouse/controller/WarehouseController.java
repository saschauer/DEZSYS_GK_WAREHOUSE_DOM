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
        // Da die Daten jetzt direkt im Dokument eingebettet sind, reicht ein einfaches findAll()
        return warehouseRepository.findAll();
    }

    @GetMapping("/{id}")
    public Warehouse getWarehouseById(@PathVariable String id) {
        return warehouseRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deleteWarehouse(@PathVariable String id) {
        warehouseRepository.deleteById(id);
        // Kaskadierendes Löschen der flachen Produkt-Einträge
        List<ProductData> products = productRepository.findByWarehouseID(id);
        productRepository.deleteAll(products);
    }
}