#!/bin/bash

# Colors for output
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Stopping POS System Services${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Stop invoice-app (port 8081)
PID_8081=$(lsof -ti:8081)
if [ ! -z "$PID_8081" ]; then
    echo "  Stopping invoice-app (PID: $PID_8081)..."
    kill -9 $PID_8081 2>/dev/null
    echo -e "  ${RED}✓${NC} Invoice-app stopped"
else
    echo "  No process running on port 8081"
fi

# Stop pos-server (port 8080)
PID_8080=$(lsof -ti:8080)
if [ ! -z "$PID_8080" ]; then
    echo "  Stopping pos-server (PID: $PID_8080)..."
    kill -9 $PID_8080 2>/dev/null
    echo -e "  ${RED}✓${NC} POS-server stopped"
else
    echo "  No process running on port 8080"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  All services stopped${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
