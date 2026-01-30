# Invoice Generation Architecture - Complete Flow Explanation

## 🎯 Overview: Why Two Invoice Controllers?

You have **TWO separate applications**, each with its own InvoiceController, but they serve **DIFFERENT purposes**:

1. **pos-server InvoiceController** - Entry point for frontend, orchestrates the flow
2. **invoice-app InvoiceController** - Actual PDF generation microservice

Think of it like ordering food:
- **pos-server** = Restaurant front desk (takes your order)
- **invoice-app** = Kitchen (prepares the food)

---

## 📊 Complete Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                         │
│                     http://localhost:5173                        │
└───────────────────────┬─────────────────────────────────────────┘
                        │ HTTP POST
                        │ /api/invoice/generate/{orderId}
                        ↓
┌─────────────────────────────────────────────────────────────────┐
│                      POS-SERVER (Port 8080)                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceController (pos-server)                          │   │
│  │ /api/invoice/generate/{orderId}                         │   │
│  │ - Receives frontend request                             │   │
│  │ - Validates order exists                                │   │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │ Calls                                       │
│                   ↓                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceDto                                              │   │
│  │ - Business logic layer                                  │   │
│  │ - Gets order + order items from database               │   │
│  │ - Prepares invoice data                                 │   │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │ Calls                                       │
│                   ↓                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceClientWrapper                                    │   │
│  │ - Wrapper around InvoiceClient                          │   │
│  │ - Abstraction layer                                     │   │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │ Uses                                        │
│                   ↓                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceClient (from invoice-app dependency)             │   │
│  │ - HTTP client using RestTemplate                        │   │
│  │ - Makes REST call to invoice-app                        │   │
│  └────────────────┬────────────────────────────────────────┘   │
└───────────────────┼─────────────────────────────────────────────┘
                    │ HTTP POST
                    │ http://localhost:8081/api/invoice/generate
                    ↓
┌─────────────────────────────────────────────────────────────────┐
│                    INVOICE-APP (Port 8081)                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceController (invoice-app)                         │   │
│  │ /api/invoice/generate                                   │   │
│  │ - Receives HTTP request from pos-server                 │   │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │ Calls                                       │
│                   ↓                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceService (InvoiceApiImpl)                         │   │
│  │ - Business logic for invoice generation                 │   │
│  │ - Calls PDF generator                                   │   │
│  │ - Saves to database                                     │   │
│  └────┬────────────────────────────────┬───────────────────┘   │
│       │                                │                        │
│       ↓                                ↓                        │
│  ┌─────────────────┐          ┌──────────────────┐            │
│  │ PdfService      │          │  InvoiceDao       │            │
│  │ - Apache FOP    │          │  - MongoDB access │            │
│  │ - XSL-FO        │          │  - Save invoice   │            │
│  │ - PDF creation  │          └──────────────────┘            │
│  └─────────────────┘                                            │
└─────────────────────────────────────────────────────────────────┘
                    │
                    ↓
              ┌──────────────┐
              │   MongoDB    │
              │   Port 27017 │
              └──────────────┘
```

---

## 📁 File-by-File Explanation

### **POS-SERVER FILES**

#### 1. `InvoiceController` (pos-server)
**Location:** `/pos-server/src/main/java/com/increff/pos/controller/InvoiceController.java`

**Purpose:** Entry point for frontend requests

**What it does:**
```java
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {
    
    @PostMapping("/generate/{orderId}")
    public void generateInvoice(@PathVariable String orderId) {
        // Just delegates to DTO layer
        invoiceDto.generateInvoice(orderId);
    }
    
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) {
        // Gets PDF bytes and sends to frontend
        byte[] pdfBytes = invoiceDto.downloadInvoice(orderId);
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .body(pdfBytes);
    }
}
```

**Why it exists:**
- Frontend needs a single endpoint to call
- Follows REST API design patterns
- Handles HTTP request/response
- Returns data in format frontend expects

---

#### 2. `InvoiceDto`
**Location:** `/pos-server/src/main/java/com/increff/pos/dto/InvoiceDto.java`

**Purpose:** Business logic orchestration layer

**What it does:**
```java
@Service
public class InvoiceDto {
    
    public void generateInvoice(String orderId) throws ApiException {
        // 1. Get order from database
        OrderPojo order = orderApi.get(orderId);
        
        // 2. Validate order exists
        if (order == null) {
            throw new ApiException("Order not found");
        }
        
        // 3. Get all order items
        List<OrderItemPojo> orderItems = orderItemApi.getByOrderId(orderId);
        
        // 4. Build invoice request with all data
        InvoiceRequest request = prepareInvoiceRequest(order, orderItems);
        
        // 5. Call invoice microservice via wrapper
        invoiceClientWrapper.generateInvoice(request);
        
        // 6. Update order status to INVOICED
        order.setStatus("INVOICED");
        orderApi.update(order);
    }
}
```

**Why it exists:**
- Separates business logic from HTTP handling
- Gets data from pos-server's database
- Transforms data into format invoice-app needs
- Updates order status after invoice generation

---

#### 3. `InvoiceClientWrapper`
**Location:** `/pos-server/src/main/java/com/increff/pos/wrapper/InvoiceClientWrapper.java`

**Purpose:** Abstraction layer around InvoiceClient

**What it does:**
```java
@Component
public class InvoiceClientWrapper {
    private final InvoiceClient invoiceClient;
    
    public InvoiceClientWrapper(@Value("${invoice.service.url}") String url) {
        this.invoiceClient = new InvoiceClient();
        // Configure URL using reflection
        setUrlViaReflection(invoiceClient, url);
    }
    
    public InvoicePojo generateInvoice(InvoiceRequest request) {
        return invoiceClient.generateInvoice(request);
    }
}
```

**Why it exists:**
- Provides clean interface for pos-server code
- Hides implementation details of InvoiceClient
- Makes it easy to swap HTTP client if needed
- Handles configuration (URL injection)

---

#### 4. `InvoiceClient` (from invoice-app dependency)
**Location:** `/invoice-app/src/main/java/com/increff/invoice/client/InvoiceClient.java`

**Purpose:** HTTP client to call invoice-app REST API

**What it does:**
```java
@Component
public class InvoiceClient {
    private RestTemplate restTemplate;
    
    @Value("${invoice.service.url:http://localhost:8081}")
    private String invoiceServiceUrl;
    
    public InvoicePojo generateInvoice(InvoiceRequest request) {
        String url = invoiceServiceUrl + "/api/invoice/generate";
        
        // Make HTTP POST request
        ResponseEntity<InvoicePojo> response = 
            restTemplate.postForEntity(url, request, InvoicePojo.class);
        
        return response.getBody();
    }
}
```

**Why it exists:**
- Makes HTTP calls to invoice-app microservice
- Handles network communication
- Part of invoice-app library (Maven dependency)
- Can be reused by any service that needs invoices

---

### **INVOICE-APP FILES**

#### 5. `InvoiceController` (invoice-app)
**Location:** `/invoice-app/src/main/java/com/increff/invoice/controller/InvoiceController.java`

**Purpose:** REST API endpoint for invoice microservice

**What it does:**
```java
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {
    
    @PostMapping("/generate")
    public InvoicePojo generateInvoice(@RequestBody InvoiceRequest request) {
        // Just delegates to service layer
        return invoiceService.generateInvoice(request);
    }
    
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String orderId) {
        byte[] pdfBytes = invoiceService.getPdfBytes(orderId);
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .body(pdfBytes);
    }
}
```

**Why it exists:**
- Provides REST API for invoice microservice
- Accepts HTTP requests from any client (pos-server, other services)
- Microservice architecture pattern
- Can be called independently

---

#### 6. `InvoiceService` (InvoiceApiImpl)
**Location:** `/invoice-app/src/main/java/com/increff/invoice/api/InvoiceApiImpl.java`

**Purpose:** Core business logic for invoice generation

**What it does:**
```java
@Service
public class InvoiceApiImpl implements InvoiceApi {
    
    public InvoicePojo generateInvoice(InvoiceRequest request) {
        // 1. Generate unique invoice ID
        String invoiceId = generateInvoiceId();
        
        // 2. Create invoice POJO
        InvoicePojo invoice = new InvoicePojo();
        invoice.setInvoiceId(invoiceId);
        invoice.setOrderId(request.getOrderId());
        invoice.setCreatedAt(Instant.now());
        
        // 3. Generate PDF using Apache FOP
        byte[] pdfBytes = pdfService.generateInvoicePdf(request);
        
        // 4. Save PDF to file system
        String filePath = savePdfToFile(invoiceId, pdfBytes);
        invoice.setFilePath(filePath);
        
        // 5. Save invoice record to MongoDB
        invoiceDao.save(invoice);
        
        return invoice;
    }
}
```

**Why it exists:**
- Handles the actual invoice generation logic
- Coordinates PDF creation and database storage
- Business logic separate from HTTP handling
- Reusable by other parts of invoice-app

---

#### 7. `PdfService`
**Location:** `/invoice-app/src/main/java/com/increff/invoice/service/PdfService.java`

**Purpose:** Generates PDF using Apache FOP

**What it does:**
```java
@Service
public class PdfService {
    
    public byte[] generateInvoicePdf(InvoiceRequest request) {
        // 1. Load XSL-FO template
        String xslTemplate = loadTemplate("invoice-template.xsl");
        
        // 2. Convert request to XML
        String xmlData = convertToXml(request);
        
        // 3. Transform XML + XSL → PDF using Apache FOP
        FopFactory fopFactory = FopFactory.newInstance();
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        
        Fop fop = fopFactory.newFop(
            MimeConstants.MIME_PDF, 
            pdfStream
        );
        
        Transformer transformer = getTransformer(xslTemplate);
        transformer.transform(
            new StreamSource(new StringReader(xmlData)),
            new SAXResult(fop.getDefaultHandler())
        );
        
        return pdfStream.toByteArray();
    }
}
```

**Why it exists:**
- Encapsulates PDF generation complexity
- Uses Apache FOP library
- Template-based approach (XSL-FO)
- Reusable for different invoice formats

---

#### 8. `InvoiceDao`
**Location:** `/invoice-app/src/main/java/com/increff/invoice/dao/InvoiceDao.java`

**Purpose:** Database access for invoice records

**What it does:**
```java
@Repository
public interface InvoiceDao extends MongoRepository<InvoicePojo, String> {
    
    // Find invoice by order ID
    InvoicePojo findByOrderId(String orderId);
    
    // All standard CRUD operations from MongoRepository:
    // - save()
    // - findById()
    // - delete()
    // etc.
}
```

**Why it exists:**
- Abstracts MongoDB operations
- Provides clean interface for data access
- Spring Data MongoDB handles implementation
- Stores invoice metadata (not the PDF itself)

---

#### 9. `InvoicePojo`
**Location:** `/invoice-app/src/main/java/com/increff/invoice/db/InvoicePojo.java`

**Purpose:** Database entity for invoice records

**What it stores:**
```java
@Document(collection = "invoices")
public class InvoicePojo {
    @Id
    private String invoiceId;        // Unique invoice ID
    private String orderId;          // Associated order ID
    private String filePath;         // Path to PDF file
    private Instant createdAt;       // When invoice was created
    // ... getters/setters
}
```

**Why it exists:**
- Represents invoice in MongoDB
- Tracks invoice metadata
- Links invoice to order
- Stores PDF file location

---

## 🔄 Complete Flow: Step-by-Step

### **Scenario: User clicks "Generate Invoice" button**

```
STEP 1: Frontend Request
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Frontend → POST http://localhost:8080/api/invoice/generate/ORD123

Data sent: Just the order ID in URL


STEP 2: POS-Server InvoiceController receives request
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
InvoiceController.generateInvoice("ORD123")
↓ Delegates to
InvoiceDto.generateInvoice("ORD123")


STEP 3: InvoiceDto orchestrates data gathering
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Get order from pos-server database:
   OrderPojo order = orderApi.get("ORD123")
   
2. Get order items from pos-server database:
   List<OrderItemPojo> items = orderItemApi.getByOrderId("ORD123")
   
3. Build invoice request:
   InvoiceRequest request = {
       orderId: "ORD123",
       orderDate: "2026-01-28",
       items: [
           { name: "Product A", quantity: 2, price: 100 },
           { name: "Product B", quantity: 1, price: 50 }
       ],
       totalAmount: 250
   }


STEP 4: Call invoice microservice
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
InvoiceDto → InvoiceClientWrapper → InvoiceClient

InvoiceClient makes HTTP POST:
POST http://localhost:8081/api/invoice/generate
Body: { "orderId": "ORD123", "items": [...], ... }


STEP 5: Invoice-App InvoiceController receives request
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
InvoiceController.generateInvoice(request)
↓ Delegates to
InvoiceService.generateInvoice(request)


STEP 6: InvoiceService generates PDF
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Generate invoice ID: "INV-20260128-001"

2. Call PdfService to create PDF:
   byte[] pdf = pdfService.generateInvoicePdf(request)
   
   PdfService uses Apache FOP:
   - Loads XSL-FO template
   - Fills in order data
   - Generates PDF bytes

3. Save PDF to file system:
   Path: /invoices/INV-20260128-001.pdf
   
4. Create database record:
   InvoicePojo invoice = {
       invoiceId: "INV-20260128-001",
       orderId: "ORD123",
       filePath: "/invoices/INV-20260128-001.pdf",
       createdAt: "2026-01-28T11:54:25"
   }
   
5. Save to MongoDB:
   invoiceDao.save(invoice)


STEP 7: Response flows back
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
InvoiceService → InvoiceController → 
HTTP Response → InvoiceClient → 
InvoiceClientWrapper → InvoiceDto


STEP 8: Update order status
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
InvoiceDto updates order in pos-server database:
order.setStatus("INVOICED")
orderApi.update(order)


STEP 9: Response to frontend
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
POS-Server InvoiceController → HTTP 200 OK → Frontend

Frontend shows success message!
```

---

## 🎨 Why This Architecture?

### **Microservices Benefits:**

1. **Separation of Concerns**
   - POS-server: Order management, business logic
   - Invoice-app: PDF generation, invoice storage

2. **Scalability**
   - Can scale invoice-app independently
   - If PDF generation is slow, add more invoice-app instances

3. **Maintainability**
   - Invoice logic isolated in one service
   - Changes to PDF format don't affect POS code

4. **Reusability**
   - Other services can use invoice-app
   - InvoiceClient can be shared across projects

5. **Technology Freedom**
   - Could rewrite invoice-app in Python if needed
   - POS-server wouldn't need to change

---

## 🔑 Key Takeaways

| Component | Lives In | Purpose |
|-----------|----------|---------|
| **InvoiceController (pos)** | pos-server | Frontend API endpoint |
| **InvoiceDto** | pos-server | Get order data, orchestrate flow |
| **InvoiceClientWrapper** | pos-server | Abstraction layer |
| **InvoiceClient** | invoice-app (as library) | HTTP client for REST calls |
| **InvoiceController (invoice-app)** | invoice-app | Microservice API endpoint |
| **InvoiceService** | invoice-app | PDF generation business logic |
| **PdfService** | invoice-app | Apache FOP PDF creation |
| **InvoiceDao** | invoice-app | MongoDB data access |
| **InvoicePojo** | invoice-app | Database entity |

---

## 💡 Think of it Like a Restaurant

- **Frontend** = Customer
- **POS-Server InvoiceController** = Waiter (takes order)
- **InvoiceDto** = Waiter writes down order details
- **InvoiceClientWrapper/Client** = Phone call to kitchen
- **Invoice-App InvoiceController** = Kitchen phone
- **InvoiceService** = Head chef
- **PdfService** = Cooking equipment
- **InvoiceDao** = Recipe book storage
- **MongoDB** = Recipe book

The customer doesn't call the kitchen directly - they talk to the waiter, who coordinates everything!
