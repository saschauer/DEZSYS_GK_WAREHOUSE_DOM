package warehouse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "warehouseData")
public class Warehouse {

    @Id
    private String warehouseID;
    private String warehouseName;
    private String timestamp;
    private int warehousePostalCode;
    private String warehouseCity;
    private String warehouseCountry;
    private List<ProductData> productData;

    public Warehouse() {}

    public Warehouse(String warehouseID, String warehouseName, String timestamp, int warehousePostalCode, String warehouseCity, String warehouseCountry, List<ProductData> productData) {
        this.warehouseID = warehouseID;
        this.warehouseName = warehouseName;
        this.timestamp = timestamp;
        this.warehousePostalCode = warehousePostalCode;
        this.warehouseCity = warehouseCity;
        this.warehouseCountry = warehouseCountry;
        this.productData = productData;
    }

    public String getWarehouseID() { return warehouseID; }
    public void setWarehouseID(String warehouseID) { this.warehouseID = warehouseID; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public int getWarehousePostalCode() { return warehousePostalCode; }
    public void setWarehousePostalCode(int warehousePostalCode) { this.warehousePostalCode = warehousePostalCode; }
    public String getWarehouseCity() { return warehouseCity; }
    public void setWarehouseCity(String warehouseCity) { this.warehouseCity = warehouseCity; }
    public String getWarehouseCountry() { return warehouseCountry; }
    public void setWarehouseCountry(String warehouseCountry) { this.warehouseCountry = warehouseCountry; }
    public List<ProductData> getProductData() { return productData; }
    public void setProductData(List<ProductData> productData) { this.productData = productData; }
}