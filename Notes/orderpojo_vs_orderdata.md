# OrderPojo vs OrderData - Complete Comparison

## Quick Summary

| Aspect | OrderPojo | OrderData |
|--------|-----------|-----------|
| **Purpose** | Database entity | API response/transfer object |
| **Location** | `pos-server/db/` | `pos-model/model/data/` |
| **Used By** | DAO, API layer | DTO, Controller layer |
| **Sent to Frontend?** | ❌ No | ✅ Yes |
| **Stored in DB?** | ✅ Yes (MongoDB) | ❌ No |
| **Contains** | Database fields + metadata | User-facing fields only |

---

## Detailed Comparison

### OrderPojo (Database Entity)

**File**: `pos-server/src/main/java/com/increff/pos/db/OrderPojo.java`

```java
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "orders")  // ← MongoDB document
public class OrderPojo extends AbstractPojo {
    
    @Indexed(unique = true)
    @Field("orderId")
    private String orderId;        // Business ID (ORD-000001)
    
    @Field("status")
    private String status;         // PLACED, INVOICED, CANCELLED, UNFULFILLABLE
    
    @Field("totalItems")
    private Integer totalItems;
    
    @Field("totalAmount")
    private Double totalAmount;
    
    @Field("orderDate")
    private ZonedDateTime orderDate;
}
```

**Inherited from AbstractPojo**:
```java
public abstract class AbstractPojo {
    @Id
    private String id;              // MongoDB _id (auto-generated)
    
    @CreatedDate
    private ZonedDateTime createdAt;
    
    @LastModifiedDate
    private ZonedDateTime updatedAt;
    
    @Version
    private Long version;           // Optimistic locking
}
```

**Complete OrderPojo fields**:
- `id` - MongoDB document ID (e.g., "6979d60dcdb5f32a9b223890")
- `orderId` - Business ID (e.g., "ORD-000001")
- `status` - Order status
- `totalItems` - Total item count
- `totalAmount` - Total order amount
- `orderDate` - When order was placed
- `createdAt` - When document was created (MongoDB audit)
- `updatedAt` - When document was last modified (MongoDB audit)
- `version` - Version number for optimistic locking

---

### OrderData (API Transfer Object)

**File**: `pos-model/src/main/java/com/increff/pos/model/data/OrderData.java`

```java
@Getter
@Setter
public class OrderData {
    private String id;                          // MongoDB _id
    private String orderId;                     // Business ID
    private String status;
    private Integer totalItems;
    private Double totalAmount;
    private String createdAt;                   // ← String, not ZonedDateTime!
    private Boolean hasInvoice;                 // ← Extra field!
    private List<OrderItemData> items;          // ← Extra field!
    
    // Unfulfillable order tracking
    private Boolean fulfillable;                // ← Extra field!
    private List<UnfulfillableItemData> unfulfillableItems; // ← Extra field!
}
```

**OrderData fields**:
- `id` - MongoDB document ID (copied from Pojo)
- `orderId` - Business ID (copied from Pojo)
- `status` - Order status (copied from Pojo)
- `totalItems` - Total item count (copied from Pojo)
- `totalAmount` - Total order amount (copied from Pojo)
- `createdAt` - **String** representation of order date (formatted for frontend)
- `hasInvoice` - **Computed field** (not in database)
- `items` - **Nested data** (order items, loaded separately)
- `fulfillable` - **Computed field** (for unfulfillable orders)
- `unfulfillableItems` - **Computed field** (items that couldn't be fulfilled)

---

## Key Differences

### 1. **Purpose & Layer**

```
┌─────────────────────────────────────────────────────────┐
│                      FRONTEND                           │
│                    (React/TypeScript)                   │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ JSON
                          │ OrderData
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                      │
│              (OrderController.java)                     │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ OrderData
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     DTO LAYER                           │
│                (OrderDto.java)                          │
│         Converts: OrderPojo ↔ OrderData                 │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ OrderPojo
                          ▼
┌─────────────────────────────────────────────────────────┐
│                   FLOW/API LAYER                        │
│            (OrderFlow, OrderApi)                        │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ OrderPojo
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     DAO LAYER                           │
│                 (OrderDao.java)                         │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ OrderPojo
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     DATABASE                            │
│                    (MongoDB)                            │
└─────────────────────────────────────────────────────────┘
```

### 2. **Field Type Differences**

| Field | OrderPojo | OrderData | Why Different? |
|-------|-----------|-----------|----------------|
| `createdAt` | `ZonedDateTime` | `String` | Frontend needs formatted string |
| `orderDate` | `ZonedDateTime` | N/A | Mapped to `createdAt` string |
| `hasInvoice` | N/A | `Boolean` | Computed at runtime |
| `items` | N/A | `List<OrderItemData>` | Loaded separately, nested for frontend |
| `fulfillable` | N/A | `Boolean` | Computed for unfulfillable orders |
| `unfulfillableItems` | N/A | `List<...>` | Computed for unfulfillable orders |
| `version` | `Long` | N/A | Internal DB field, not exposed |
| `updatedAt` | `ZonedDateTime` | N/A | Internal DB field, not exposed |

### 3. **Conversion Process**

**OrderHelper.convertToDto()** converts OrderPojo → OrderData:

```java
public static OrderData convertToDto(OrderPojo pojo, boolean hasInvoice) {
    OrderData data = new OrderData();
    
    // Copy basic fields
    data.setId(pojo.getId());
    data.setOrderId(pojo.getOrderId());
    data.setStatus(pojo.getStatus());
    data.setTotalItems(pojo.getTotalItems());
    data.setTotalAmount(pojo.getTotalAmount());
    
    // Convert ZonedDateTime → String
    if (pojo.getOrderDate() != null) {
        data.setCreatedAt(pojo.getOrderDate().format(DATE_FORMATTER));
    }
    
    // Add computed field
    data.setHasInvoice(hasInvoice);
    
    return data;
}
```

**Example Conversion**:

**OrderPojo (from MongoDB)**:
```json
{
  "_id": "6979d60dcdb5f32a9b223890",
  "orderId": "ORD-000001",
  "status": "INVOICED",
  "totalItems": 1,
  "totalAmount": 1299.0,
  "orderDate": ISODate("2026-01-28T09:25:33.047Z"),
  "createdAt": ISODate("2026-01-28T09:25:33.048Z"),
  "updatedAt": ISODate("2026-01-28T09:25:59.852Z"),
  "version": 1,
  "_class": "com.increff.pos.db.OrderPojo"
}
```

**OrderData (sent to frontend)**:
```json
{
  "id": "6979d60dcdb5f32a9b223890",
  "orderId": "ORD-000001",
  "status": "INVOICED",
  "totalItems": 1,
  "totalAmount": 1299.0,
  "createdAt": "2026-01-28T14:55:33.047+05:30",
  "hasInvoice": true,
  "items": [
    {
      "id": "...",
      "productName": "iPhone 15",
      "quantity": 1,
      "mrp": 1299.0,
      "lineTotal": 1299.0
    }
  ],
  "fulfillable": null,
  "unfulfillableItems": null
}
```

---

## Why Two Separate Classes?

### 1. **Separation of Concerns**

- **OrderPojo**: Database representation (persistence layer)
- **OrderData**: API representation (presentation layer)

### 2. **Security & Encapsulation**

**Don't expose internal fields**:
- `version` - Internal optimistic locking, frontend doesn't need it
- `updatedAt` - Internal audit field, frontend doesn't need it
- `_class` - MongoDB metadata, frontend doesn't need it

### 3. **Flexibility**

**Add computed fields without changing database**:
- `hasInvoice` - Computed by checking if invoice exists
- `items` - Loaded from separate collection, nested for convenience
- `fulfillable` - Computed based on inventory check

### 4. **Data Type Conversion**

**Frontend-friendly formats**:
- `ZonedDateTime` → `String` (JavaScript can parse it)
- Nested objects (items) for easier frontend consumption

### 5. **API Evolution**

**Change API without changing database**:
- Add new fields to OrderData without DB migration
- Rename fields for frontend without changing DB schema
- Hide sensitive fields from API response

---

## Real-World Example

### Creating an Order

```java
// 1. OrderFlow creates OrderPojo
OrderPojo pojo = new OrderPojo();
pojo.setOrderId("ORD-000001");
pojo.setStatus("PLACED");
pojo.setTotalItems(5);
pojo.setTotalAmount(6495.0);
pojo.setOrderDate(ZonedDateTime.now());

// 2. Save to MongoDB
OrderPojo savedPojo = orderDao.save(pojo);
// MongoDB now has: id, orderId, status, totalItems, totalAmount, 
//                  orderDate, createdAt, updatedAt, version

// 3. Convert to OrderData for API response
boolean hasInvoice = invoiceApi.existsByOrderId(savedPojo.getOrderId());
OrderData data = OrderHelper.convertToDto(savedPojo, hasInvoice);

// 4. Load order items
List<OrderItemPojo> itemPojos = orderItemDao.findByOrderId(savedPojo.getOrderId());
List<OrderItemData> itemData = OrderHelper.convertItemsToDtoList(itemPojos);
data.setItems(itemData);

// 5. Return to frontend
return data;
```

### What Frontend Receives

```typescript
interface OrderData {
  id: string;
  orderId: string;
  status: 'PLACED' | 'INVOICED' | 'CANCELLED' | 'UNFULFILLABLE';
  totalItems: number;
  totalAmount: number;
  createdAt: string;  // "2026-01-28T14:55:33.047+05:30"
  hasInvoice: boolean;
  items: OrderItemData[];
  fulfillable?: boolean;
  unfulfillableItems?: UnfulfillableItemData[];
}
```

---

## Summary

| When to Use | OrderPojo | OrderData |
|-------------|-----------|-----------|
| **Saving to DB** | ✅ Use OrderPojo | ❌ Don't use OrderData |
| **Reading from DB** | ✅ Use OrderPojo | ❌ Don't use OrderData |
| **API Response** | ❌ Don't use OrderPojo | ✅ Use OrderData |
| **Business Logic** | ✅ Use OrderPojo | ❌ Don't use OrderData |
| **Frontend Display** | ❌ Don't use OrderPojo | ✅ Use OrderData |

**Key Principle**: 
- **OrderPojo** = Internal representation (database)
- **OrderData** = External representation (API)
- **Never expose Pojo directly to frontend!**
