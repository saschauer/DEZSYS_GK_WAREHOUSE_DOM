package warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import warehouse.repository.ProductRepository;
import warehouse.repository.WarehouseRepository;
import warehouse.model.ProductData;
import warehouse.model.Warehouse;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepo, WarehouseRepository warehouseRepo) {
        return args -> {
            productRepo.deleteAll();
            warehouseRepo.deleteAll();

            String[] categories = {"Getraenk", "Waschmittel", "Tierfutter", "Lebensmittel", "Elektronik", "Kleidung"};
            String[] warehouseIds = {"1", "2", "3", "4", "5"};
            String[] cities = {"Linz", "Wien", "Graz", "Salzburg", "Innsbruck"};

            for (int i = 0; i < warehouseIds.length; i++) {
                warehouseRepo.save(new Warehouse(
                        warehouseIds[i],
                        "Zentrallager " + cities[i],
                        "2026-06-09T10:00:00Z",
                        4000 + (i * 100),
                        cities[i],
                        "Austria",
                        new ArrayList<>()
                ));
            }

            Random random = new Random();
            List<ProductData> products = new ArrayList<>();

            for (int i = 1; i <= 300; i++) {
                String category = categories[random.nextInt(categories.length)];
                String warehouseId = warehouseIds[random.nextInt(warehouseIds.length)];
                String productId = String.format("%02d-%06d", random.nextInt(100), i);
                String name = "Test-Produkt " + i;
                double quantity = Math.round(random.nextDouble() * 5000);

                products.add(new ProductData(warehouseId, productId, name, category, quantity));
            }

            productRepo.saveAll(products);
            System.out.println("✅ DB-Erfolg: 5 Warenhäuser und 300 Produkte geladen!");
        };
    }
}