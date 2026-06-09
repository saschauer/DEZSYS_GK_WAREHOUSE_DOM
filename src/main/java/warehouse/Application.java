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
            // Datenbank komplett leeren, um Seiteneffekte zu vermeiden
            productRepo.deleteAll();
            warehouseRepo.deleteAll();

            String[] categories = {"Getraenk", "Waschmittel", "Tierfutter", "Lebensmittel", "Elektronik", "Kleidung"};
            String[] warehouseIds = {"1", "2", "3", "4", "5"};
            String[] cities = {"Linz", "Wien", "Graz", "Salzburg", "Innsbruck"};

            // 1. Die 5 Warenhäuser im Speicher instanziieren
            List<Warehouse> warehouses = new ArrayList<>();
            for (int i = 0; i < warehouseIds.length; i++) {
                warehouses.add(new Warehouse(
                        warehouseIds[i],
                        "Zentrallager " + cities[i],
                        "2026-06-09T16:00:00Z",
                        4000 + (i * 100),
                        cities[i],
                        "Austria",
                        new ArrayList<>() // Leeres Array für eingebettete Produkte
                ));
            }

            Random random = new Random();
            List<ProductData> allProductsFlattened = new ArrayList<>();

            // 2. 300 Produkte generieren und direkt in die Warenhäuser einbetten
            for (int i = 1; i <= 300; i++) {
                String category = categories[random.nextInt(categories.length)];

                // Zufälliges Warenhaus aus unserer Liste auswählen
                int warehouseIndex = random.nextInt(warehouses.size());
                Warehouse targetWarehouse = warehouses.get(warehouseIndex);

                String productId = String.format("%02d-%06d", random.nextInt(100), i);
                String name = "Test-Produkt " + i;
                double quantity = Math.round(random.nextDouble() * 5000);

                ProductData newProduct = new ProductData(targetWarehouse.getWarehouseID(), productId, name, category, quantity);

                // Wichtig: Direkt in das Array des Ziel-Warenhauses pushen (Embedded Modell)
                targetWarehouse.getProductData().add(newProduct);

                // Für den flachen /product Endpunkt merken
                allProductsFlattened.add(newProduct);
            }

            // 3. Daten in MongoDB persistieren
            warehouseRepo.saveAll(warehouses); // Speichert Warenhäuser inkl. Arrays in "warehouseData"
            productRepo.saveAll(allProductsFlattened); // Speichert flache Produkte in "productData"

            System.out.println("✅ DB-Erfolg: 5 Warenhäuser und 300 Produkte geladen!");
        };
    }
}