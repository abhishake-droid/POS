#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Starting POS System Services${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if MongoDB is running
if ! pgrep -x "mongod" > /dev/null; then
    echo -e "${RED}⚠️  Warning: MongoDB doesn't appear to be running!${NC}"
    echo -e "${RED}   Please start MongoDB first${NC}"
    echo ""
fi

# Kill any existing processes on ports 8080 and 8081
echo -e "${BLUE}Checking for existing processes...${NC}"
PID_8081=$(lsof -ti:8081)
PID_8080=$(lsof -ti:8080)

if [ ! -z "$PID_8081" ]; then
    echo "  Killing existing process on port 8081 (PID: $PID_8081)"
    kill -9 $PID_8081 2>/dev/null
fi

if [ ! -z "$PID_8080" ]; then
    echo "  Killing existing process on port 8080 (PID: $PID_8080)"
    kill -9 $PID_8080 2>/dev/null
fi

echo ""

# Start invoice-app
echo -e "${GREEN}🚀 Starting invoice-app on port 8081...${NC}"
cd invoice-app
mvn spring-boot:run > ../logs/invoice-app.log 2>&1 &
INVOICE_PID=$!
echo "  Invoice-app started with PID: $INVOICE_PID"
cd ..

# Wait a few seconds for invoice-app to start
echo "  Waiting for invoice-app to initialize..."
sleep 8

# Start pos-server
echo -e "${GREEN}🚀 Starting pos-server on port 8080...${NC}"
cd pos-server
mvn spring-boot:run > ../logs/pos-server.log 2>&1 &
POS_PID=$!
echo "  POS-server started with PID: $POS_PID"
cd ..

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ✅ Both services started!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "  📊 Invoice Service: http://localhost:8081"
echo "  🖥️  POS Server:      http://localhost:8080"
echo ""
echo "  📝 Logs are available at:"
echo "     - logs/invoice-app.log"
echo "     - logs/pos-server.log"
echo ""
echo "  To stop both services, run: ./stop-services.sh"
echo ""
