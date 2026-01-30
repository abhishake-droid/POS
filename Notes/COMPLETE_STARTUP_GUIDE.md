# Complete POS System Startup Guide

## ⚠️ FIRST TIME SETUP (Do this ONCE when you make code changes)

### Step 1: Build invoice-app and Install to Maven Repository
```bash
cd /Users/abhisheksudhir/Downloads/POS_v2/invoice-app
mvn clean install -DskipTests
cd ..
```

**Why?** This creates the invoice-app JAR that pos-server depends on and installs it to your local Maven repository (~/.m2/repository).

### Step 2: Build pos-server
```bash
cd pos-server
mvn clean compile -DskipTests
cd ..
```

**Why?** This compiles pos-server and pulls in the invoice-app dependency we just installed.

---

## 🚀 STARTING THE SYSTEM (Do this EVERY TIME you want to run the app)

### Prerequisites Check
Make sure MongoDB is running:
```bash
# Check if MongoDB is running
pgrep -x mongod

# If not running, start it:
brew services start mongodb-community@7.0
# OR
mongod --dbpath /path/to/your/data
```

### Option 1: Use the Startup Script (RECOMMENDED)
```bash
./start-services.sh
```

This automatically:
1. Stops any existing services on ports 8080 and 8081
2. Starts invoice-app on port 8081
3. Starts pos-server on port 8080
4. Saves logs to `logs/` directory

### Option 2: Manual Start (if you prefer)
**Terminal 1 - Start invoice-app:**
```bash
cd invoice-app
mvn spring-boot:run
```

**Terminal 2 - Start pos-server:**
```bash
cd pos-server
mvn spring-boot:run
```

---

## 🛑 STOPPING THE SYSTEM

### Using the Stop Script (RECOMMENDED)
```bash
./stop-services.sh
```

### Manual Stop
```bash
# Find and kill processes
lsof -ti:8081 | xargs kill -9  # Kill invoice-app
lsof -ti:8080 | xargs kill -9  # Kill pos-server
```

---

## 📋 COMPLETE WORKFLOW FROM SCRATCH

### Scenario: You just made code changes and want to test

```bash
# 1. Stop any running services
./stop-services.sh

# 2. Rebuild invoice-app (since pos-server depends on it)
cd invoice-app
mvn clean install -DskipTests
cd ..

# 3. Rebuild pos-server
cd pos-server
mvn clean compile -DskipTests
cd ..

# 4. Start both services
./start-services.sh

# 5. Check logs if needed
tail -f logs/invoice-app.log
tail -f logs/pos-server.log
```

---

## 🔍 VERIFICATION

### Check if services are running:
```bash
lsof -ti:8081  # Should show invoice-app PID
lsof -ti:8080  # Should show pos-server PID
```

### Test invoice-app directly:
```bash
curl http://localhost:8081/actuator/health
```

### Test pos-server directly:
```bash
curl http://localhost:8080/api/health  # or whatever endpoint you have
```

---

## 📝 VIEW LOGS

Logs are saved in the `logs/` directory:
```bash
# View last 50 lines of invoice-app log
tail -50 logs/invoice-app.log

# View last 50 lines of pos-server log
tail -50 logs/pos-server.log

# Follow logs in real-time
tail -f logs/invoice-app.log
tail -f logs/pos-server.log
```

---

## 🎯 WHEN TO RUN WHAT

| Scenario | Commands Required |
|----------|------------------|
| **First time ever** | `cd invoice-app && mvn clean install -DskipTests && cd ../pos-server && mvn clean compile -DskipTests && cd .. && ./start-services.sh` |
| **Made changes to invoice-app code** | Rebuild invoice-app → Rebuild pos-server → Restart both |
| **Made changes to pos-server code only** | Rebuild pos-server → Restart both |
| **Just want to run the app** | `./start-services.sh` |
| **Want to stop the app** | `./stop-services.sh` |

---

## ⚡ QUICK COMMANDS

```bash
# Full rebuild and restart (use after code changes)
./stop-services.sh && cd invoice-app && mvn clean install -DskipTests && cd ../pos-server && mvn clean compile -DskipTests && cd .. && ./start-services.sh

# Just restart (if no code changes)
./stop-services.sh && ./start-services.sh
```

---

## 🌐 Access URLs

- **Invoice Service**: http://localhost:8081
- **POS Server API**: http://localhost:8080
- **POS Frontend**: http://localhost:5173 (if using Vite dev server)

---

## ❓ TROUBLESHOOTING

### "Connection refused" error when generating invoice
→ Invoice-app is not running. Run `./start-services.sh`

### "Port already in use" error
→ Stop existing services: `./stop-services.sh`

### Invoice-app not found error in pos-server
→ Rebuild invoice-app: `cd invoice-app && mvn clean install -DskipTests`

### Changes not reflecting
→ Do a full rebuild:
```bash
./stop-services.sh
cd invoice-app && mvn clean install -DskipTests && cd ..
cd pos-server && mvn clean compile -DskipTests && cd ..
./start-services.sh
```
