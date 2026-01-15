# How to Run POS_v1 Application

## Prerequisites
- Java 21 installed
- Maven installed
- MongoDB (local or Atlas)
- Node.js 18+ and npm installed

---

## Step 1: Configure MongoDB

Edit `pos-server/src/main/resources/application.properties`:

**For Local MongoDB:**
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/pos
```

**For MongoDB Atlas:**
```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster-url>/pos?retryWrites=true&w=majority
```

Make sure MongoDB is running (if using local).

---

## Step 2: Run Backend (Spring Boot)

### Option A: Using Maven (Recommended)

1. Open terminal/command prompt
2. Navigate to the project root:
   ```bash
   cd c:\Users\Asus\Downloads\pos-template\pos-template\POS_v1
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the server:
   ```bash
   cd pos-server
   mvn spring-boot:run
   ```

   OR from root directory:
   ```bash
   mvn spring-boot:run -pl pos-server
   ```

### Option B: Using IDE (IntelliJ IDEA / Eclipse)

1. Open the project in your IDE
2. Navigate to: `pos-server/src/main/java/com/increff/pos/config/SpringConfig.java`
3. Right-click on `SpringConfig.java` → Run 'SpringConfig.main()'

### Verify Backend is Running

- Backend should start on: **http://localhost:8080**
- API Documentation (Swagger): **http://localhost:8080/swagger-ui/index.html**
- Test endpoint: **http://localhost:8080/api/client/get-all-paginated** (POST with body: `{"page":0,"size":10}`)

---

## Step 3: Run Frontend (Next.js)

1. Open a **new terminal/command prompt**
2. Navigate to the frontend directory:
   ```bash
   cd c:\Users\Asus\Downloads\pos-template\pos-template\POS_v1\pos-client
   ```

3. Install dependencies (first time only):
   ```bash
   npm install
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

### Verify Frontend is Running

- Frontend should start on: **http://localhost:3000**
- Home page: **http://localhost:3000**
- Clients page: **http://localhost:3000/clients**

---

## Step 4: Access the Application

1. Open browser and go to: **http://localhost:3000**
2. Click on "Clients" button or navigate to: **http://localhost:3000/clients**
3. You should see the Clients page with:
   - "Add Client" button
   - Table displaying all clients
   - Edit functionality for each client

---

## Troubleshooting

### Backend Issues:

1. **Port 8080 already in use:**
   - Change port in `application.properties`: `server.port=8081`

2. **MongoDB connection error:**
   - Verify MongoDB is running (local) or Atlas connection string is correct
   - Check firewall settings

3. **Maven build fails:**
   - Ensure Java 21 is installed: `java -version`
   - Clean and rebuild: `mvn clean install -U`

### Frontend Issues:

1. **Port 3000 already in use:**
   - Next.js will automatically use the next available port (3001, 3002, etc.)

2. **Cannot connect to backend:**
   - Ensure backend is running on http://localhost:8080
   - Check `next.config.js` has correct proxy settings

3. **npm install fails:**
   - Clear cache: `npm cache clean --force`
   - Delete `node_modules` and `package-lock.json`, then run `npm install` again

---

## API Endpoints

Once backend is running, you can test these endpoints:

- **Create Client:** `POST http://localhost:8080/api/client/add`
  ```json
  {
    "id": "client1",
    "name": "Client Name"
  }
  ```

- **Get All Clients:** `POST http://localhost:8080/api/client/get-all-paginated`
  ```json
  {
    "page": 0,
    "size": 10
  }
  ```

- **Get Client by ID:** `GET http://localhost:8080/api/client/get-by-id/{id}`

- **Update Client:** `PUT http://localhost:8080/api/client/update/{id}`
  ```json
  {
    "id": "client1",
    "name": "Updated Name"
  }
  ```

---

## Quick Start Commands Summary

```bash
# Terminal 1 - Backend
cd c:\Users\Asus\Downloads\pos-template\pos-template\POS_v1
mvn clean install
cd pos-server
mvn spring-boot:run

# Terminal 2 - Frontend
cd c:\Users\Asus\Downloads\pos-template\pos-template\POS_v1\pos-client
npm install
npm run dev
```

Then open: **http://localhost:3000/clients**
