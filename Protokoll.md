# Protokoll – GK8.2 Document-Oriented Middleware
**Technologie:** MongoDB · Spring Boot · Docker  
**Datum:** 5.5.2026
**Name:** Stefan Aschauer

---

## 1. Projektübersicht

Das Projekt implementiert eine **dokumentenorientierte Middleware** auf Basis von MongoDB und Spring Boot. Sie verwaltet Lagerstandorte und Produkte über eine REST-API und demonstriert die vollständigen CRUD-Operationen sowie komplexe Aggregations-Pipelines.

---

## 2. REST-Schnittstellen-Spezifikation (API)

Die Middleware stellt folgende Endpunkte bereit, um die vollständigen CRUD-Anforderungen zu erfüllen:

| Methode | Endpunkt | Beschreibung | Payload / Response |
|---|---|---|---|
| `POST` | `/warehouse` | Fügt einen neuen Lagerstandort hinzu | JSON-Lagerobjekt (ohne Produkte) |
| `GET` | `/warehouse` | Abrufen aller Lagerstandorte inkl. aggregiertem Lagerbestand | Array aus hierarchischen Warenhäusern |
| `GET` | `/warehouse/{id}` | Abrufen eines spezifischen Lagers mit dessen Produkten | Einzelnes hierarchisches Lagerobjekt |
| `DELETE` | `/warehouse/{id}` | Löscht ein Lager und kaskadierend alle zugehörigen Produkte | Keine |
| `POST` | `/product` | Fügt ein Produkt zu einem spezifischen Lagerstandort hinzu | JSON-Produktobjekt |
| `GET` | `/product` | Abrufen aller existierenden Produkt-Lagerbestände (flach) | Array aus flachen Produktobjekten |
| `GET` | `/product/{id}` | Sucht ein Produkt über seine fachliche `productID` über alle Lager | Array aller Standorte, an denen das Produkt liegt |
| `DELETE` | `/product/{id}` | Löscht ein Produkt anhand seiner fachlichen ID vom Lager | Keine |

---

## 3. Technologie-Stack und Implementierung

### 3.1 Infrastruktur (`docker-compose.yml`)

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:latest
    container_name: mongo_warehouse
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    restart: always

volumes:
  mongo_data:
```

### 3.2 Middleware-Konfiguration (`application.properties`)

```properties
server.port=8080
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=testdb
```

### 3.3 Testdatengenerierung (Vertiefung)

Über eine `CommandLineRunner`-Bean in `Application.java` wird beim Systemstart automatisch geprüft, ob Daten vorliegen. Das System generiert vollautomatisch:

- **5 Warenhäuser** (Linz, Wien, Graz, Salzburg, Innsbruck)
- **300 Produkte** verteilt auf 6 Kategorien (Getraenk, Waschmittel, Tierfutter, Lebensmittel, Elektronik, Kleidung) mit zufälligen Beständen zwischen 0 und 5000 Stück

---

## 4. CRUD-Operationen (Mongo Shell)

### 1. CREATE – Dokument einfügen

```javascript
db.warehouseData.insertOne({
  warehouseID: "99",
  warehouseName: "Testlager Klagenfurt",
  warehouseCity: "Klagenfurt",
  productData: []
})
```

**Ergebnis:**
```json
{
  "acknowledged": true,
  "insertedId": ObjectId("6a27e11f978f0b096f0ff001")
}
```

### 2. READ – Dokument suchen

```javascript
db.warehouseData.find({ "warehouseID": "99" }).pretty()
```

**Ergebnis:**
```json
{
  "_id": ObjectId("6a27e11f978f0b096f0ff001"),
  "warehouseID": "99",
  "warehouseName": "Testlager Klagenfurt",
  "warehouseCity": "Klagenfurt",
  "productData": []
}
```

### 3. UPDATE – Stammdaten ändern

```javascript
db.warehouseData.updateOne(
  { "warehouseID": "99" },
  { $set: { "warehouseName": "Zentrallager Kärnten" } }
)
```

**Ergebnis:**
```json
{ "acknowledged": true, "matchedCount": 1, "modifiedCount": 1 }
```

### 4. UPDATE / PUSH – Eingebettetes Array erweitern

```javascript
db.warehouseData.updateOne(
  { "warehouseID": "99" },
  { $push: { "productData": {
      "productID": "99-999999",
      "productName": "Energy Drink",
      "productQuantity": 750.0
  }}}
)
```

**Ergebnis:**
```json
{ "acknowledged": true, "matchedCount": 1, "modifiedCount": 1 }
```

### 5. DELETE – Dokument löschen

```javascript
db.warehouseData.deleteOne({ "warehouseID": "99" })
```

**Ergebnis:**
```json
{ "acknowledged": true, "deletedCount": 1 }
```

---

## 5. Berichtswesen und Aggregationen (Vertiefung)

### Fragestellung 1: Kumulierter Gesamtbestand eines Produktes über alle Standorte

```javascript
db.warehouseData.aggregate([
  { $unwind: "$productData" },
  { $match: { "productData.productID": "40-000009" } },
  { $group: {
      _id: "$productData.productID",
      Gesamtbestand: { $sum: "$productData.productQuantity" }
  }}
])
```

**Ergebnis:**
```json
[ { "_id": "40-000009", "Gesamtbestand": 2319 } ]
```

### Fragestellung 2: Bestand eines Produktes an einem bestimmten Standort

```javascript
db.warehouseData.aggregate([
  { $match: { "warehouseID": "1" } },
  { $unwind: "$productData" },
  { $match: { "productData.productID": "40-000009" } },
  { $project: {
      _id: 0,
      warehouseName: 1,
      "productData.productID": 1,
      "productData.productQuantity": 1
  }}
])
```

**Ergebnis:**
```json
[{
  "warehouseName": "Zentrallager Linz",
  "productData": { "productID": "40-000009", "productQuantity": 2319 }
}]
```

### Fragestellung 3: Produkte mit kritischem Gesamtbestand < 500 Stück

```javascript
db.warehouseData.aggregate([
  { $unwind: "$productData" },
  { $group: {
      _id: { id: "$productData.productID", name: "$productData.productName" },
      Gesamtbestand: { $sum: "$productData.productQuantity" }
  }},
  { $match: { Gesamtbestand: { $lt: 500 } } }
])
```

---

## 6. Theoretische Fragestellungen

### 6.1 Vier Vorteile von NoSQL gegenüber relationalen DBMS

1. **Schema-Flexibilität:** Dokumente können unterschiedliche Felder besitzen; Strukturen lassen sich ohne `ALTER TABLE` zur Laufzeit erweitern.
2. **Horizontale Skalierbarkeit:** MongoDB ist nativ für Sharding ausgelegt – Daten verteilen sich über Standard-Server-Cluster statt teurer vertikaler Aufrüstung.
3. **Performance durch Denormalisierung:** Eingebettete Kind-Objekte vermeiden rechenintensive JOINs; ein Lesezugriff liefert das gesamte Aggregat.
4. **Impedance Match:** JSON/BSON-Speicherung entspricht direkt der Struktur moderner Java-Objekte – kein ORM-Mapper (wie Hibernate) notwendig.

### 6.2 Vier Nachteile von NoSQL gegenüber relationalen DBMS

1. **Keine standardisierte Abfragesprache:** Jede NoSQL-DB (MongoDB, Redis, Neo4j) hat eine eigene proprietäre Syntax → Vendor Lock-in.
2. **Schwächere ACID-Garantien:** Transaktionssicherheit über mehrere Collections wird für Performance und Verfügbarkeit gelockert (Eventual Consistency).
3. **Hohe Datenredundanz:** Denormalisierte Speicherung belegt deutlich mehr Speicherplatz, da identische Informationen mehrfach existieren können.
4. **Komplexere Datenkonsistenz:** Ändert sich ein zentraler Wert (z. B. Produktname), muss dieser manuell an vielen Stellen nachgezogen werden.

### 6.3 Schwierigkeiten bei der Datenzusammenführung

In NoSQL-Systemen gibt es keine datenbankseitig erzwungene **referenzielle Integrität** (keine Foreign Key Constraints). Beim Löschen oder Ändern eines Lagers garantiert die Datenbank nicht, dass Produkte mitgezogen werden. Die vollständige Verantwortung für Konsistenz, Kaskadierung und saubere Zusammenführung liegt in der **Middleware (Spring Boot)**.

### 6.4 Arten von NoSQL-Datenbanken

| Typ | Beschreibung | Vertreter |
|---|---|---|
| **Dokumentenorientiert** | Speichert hierarchische JSON/BSON-Dokumente | MongoDB, CouchDB |
| **Key-Value-Store** | Einfachste Struktur aus Schlüssel-Wert-Paaren; extrem schnell | Redis, DynamoDB |
| **Spaltenorientiert** | Optimiert für zeilenunabhängige Lesezugriffe (Big Data) | Apache Cassandra, HBase |
| **Graphdatenbank** | Knoten und Kanten für hochvernetzte Daten | Neo4j |

### 6.5 CAP-Theorem

Das CAP-Theorem besagt, dass ein verteiltes System bei einer **Netzwerk-Partitionierung** nur eine der beiden Eigenschaften garantieren kann:

| Eigenschaft | Bedeutung |
|---|---|
| **C** – Consistency | Alle Knoten sehen zu jeder Zeit exakt dieselben Daten |
| **A** – Availability | Jede Anfrage erhält immer eine Antwort (ohne Aktualitätsgarantie) |
| **P** – Partition Tolerance | Das System funktioniert trotz Netzwerkausfällen |

- **CA** – Konsistenz + Verfügbarkeit: Keine Partitionstoleranz (klassische relationale DBs ohne Sharding)
- **CP** – Konsistenz + Partitionstoleranz: Bei Netzwerkausfall verweigern Knoten die Antwort (→ **MongoDB** im Cluster-Verbund)
- **AP** – Verfügbarkeit + Partitionstoleranz: Immer Antwort, ggf. veraltete Daten (→ **Cassandra**)

---

## 7. Abgabe-Leitfaden (Drehbuch für die Live-Demo)

### Schritt 1: Docker-Container zeigen

```bash
docker ps
```

> „Die MongoDB läuft isoliert im Docker-Container auf Port 27017. Die Daten werden über ein Volume persistent auf der Festplatte gesichert."

### Schritt 2: Spring Boot starten

```bash
./gradlew clean bootRun
```

Im Log auf folgende Zeile warten:

```
DB-Erfolg: 5 Warenhäuser und 300 Produkte geladen!
```

> „Hier greift die Vertiefungsanforderung: Die Applikation generiert beim Start vollautomatisch 5 Warenhäuser und exakt 300 Testprodukte in 6 Kategorien."

### Schritt 3: REST-API im Browser vorführen

Browser öffnen: `http://localhost:8080/warehouse`

> „Obwohl die Daten in MongoDB zur Performance-Optimierung getrennt verwaltet werden, fügt die Middleware die Bestände dynamisch zusammen. Der Client sieht eine saubere, hierarchische Objektstruktur."

### Schritt 4: Live-Abfragen in der Mongo-Shell

```bash
docker exec -it mongo_warehouse mongosh
use testdb
show collections
```

Aggregation-Pipeline aus Abschnitt 5 live einfügen und Ergebnis demonstrieren.

---

## 8. Antizipierte Prüferfragen

### Frage 1: „Warum zwei getrennte Collections statt einem einzigen Dokument?"

> MongoDB hat ein hartes Limit von **16 MB pro Dokument**. Bei vielen Produkten und minütlichen Bestandsupdates müsste bei jedem Update das gesamte Warehouse-Dokument gesperrt und modifiziert werden. Durch die Trennung in zwei Collections bleiben Schreiboperationen flach und schnell. Die hierarchische Struktur generiert die Middleware erst dynamisch beim Lese-Call im Controller – das ist echtes Middleware-Engineering.

### Frage 2: „Was passiert mit Produkten, wenn ein Lager gelöscht wird?"

> Da NoSQL keine Fremdschlüssel-Kaskadierung kennt, ist das im `WarehouseController` manuell implementiert: Beim Löschen eines Lagers sucht das `productRepository` sofort nach allen Produkten mit dieser `warehouseID` und löscht diese mit `deleteAll()`. Damit werden verwaiste Dokumente (Datenleichen) verhindert.

### Frage 3: „Was macht `$unwind` genau und warum ist er notwendig?"

> Da Produkte innerhalb eines Lagers als Array vorliegen, kann MongoDB Berechnungen nicht direkt auf der Liste ausführen. `$unwind` bricht das Array auf und dupliziert das Hauptdokument für jedes Array-Element. Aus einem Lager mit 50 Produkten werden temporär 50 einzelne Dokumente. Erst dadurch können nachfolgende Stages wie `$match` oder `$group` die einzelnen Werte mathematisch verarbeiten.
