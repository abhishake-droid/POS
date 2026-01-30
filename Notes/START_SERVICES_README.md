# POS System - Quick Start Guide

## Starting Both Services Together

To start both **invoice-app** (port 8081) and **pos-server** (port 8080) together:

```bash
./start-services.sh
```

This script will:
- Kill any existing processes on ports 8080 and 8081
- Start invoice-app on port 8081
- Start pos-server on port 8080
- Save logs to `logs/` directory

## Stopping Services

To stop both services:

```bash
./stop-services.sh
```

## Viewing Logs

Logs are saved in the `logs/` directory:
- `logs/invoice-app.log` - Invoice microservice logs
- `logs/pos-server.log` - POS server logs

To tail logs in real-time:
```bash
tail -f logs/invoice-app.log
tail -f logs/pos-server.log
```

## Service URLs

- **Invoice Service**: http://localhost:8081
- **POS Server**: http://localhost:8080

## Prerequisites

Make sure MongoDB is running before starting the services:
```bash
brew services start mongodb-community
# or
mongod --dbpath /path/to/data
```
