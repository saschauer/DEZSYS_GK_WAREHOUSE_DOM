package warehouse.controller;

import org.springframework.web.bind.annotation.*;
import warehouse.model.ProductData;
import warehouse.repository.ProductRepository;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ProductData addProduct(@RequestBody ProductData newProduct) {
        return repository.save(newProduct);
    }

    @GetMapping
    public List<ProductData> getAllProducts() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public List<ProductData> getProductByIdAllLocations(@PathVariable String id) {
        return repository.findAll().stream()
                .filter(p -> p.getProductID().equals(id))
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        ProductData productToDelete = repository.findByProductID(id);
        if (productToDelete != null) {
            repository.deleteById(productToDelete.getID());
        }
    }
}