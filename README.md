# Stock Order Service

This is Spring Boot application for processing stock exchange orders.
Users can place orders and retrieve order book data via REST API or WebSocket.

## Overview

Stock Order Service is a backend service that provides:
- REST API for placing and retrieving stock orders,
- WebSocket endpoints for real-time order updates,
- Automatic order matching using price-time priority,
- Interactive web client for WebSocket communication.

## Prerequisites

- [Docker](https://www.docker.com/get-started) and Docker Compose
- [Git](https://git-scm.com/downloads)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/RadovanPrijic/Stock-Order-Service.git
cd Stock-Order-Service
```

### Build and Run with Docker Compose

```bash
docker-compose up
```

This command builds and starts the service.

### Access the Application

- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **WebSocket Client Interface**: http://localhost:80

## Testing the API

### REST API

You can test the REST API using Postman or the Swagger UI.

#### Place an Order

- Method: POST
- URL: http://localhost:8080/api/orders/place
- Headers: Content-Type: application/json
- Body:
```json
{
  "orderType": "BUY",
  "price": 150.50,
  "startingAmount": 10
}
```

#### Get Top 10 Buy Orders

- Method: GET
- URL: http://localhost:8080/api/orders/top10/buy

#### Get Top 10 Sell Orders

- Method: GET
- URL: http://localhost:8080/api/orders/top10/sell

### WebSocket Interface

The application provides a user-friendly WebSocket client interface at **http://localhost:80** with the following features:

#### Key Features

- Real-time order book visualization
- Interactive order placement form
- Live updates for top buy and sell orders
- Connection status monitoring
- Detailed activity logs

#### Using the WebSocket Client

1. Open http://localhost:80 in your browser
2. Enter the WebSocket server URL (default: http://localhost:8080/ws-orders)
3. Click "Connect" to establish a WebSocket connection
4. Place orders using the order form:
   - Select order type (BUY/SELL)
   - Enter price and starting amount
   - Click "Place Order"
5. View real-time updates in the tabbed interface:
   - Top 10 Buy Orders (click refresh to update)
   - Top 10 Sell Orders (click refresh to update)
   - Placed Orders
   - Connection Logs

## Order Matching

The service automatically matches buy and sell orders based on:
1. Price compatibility
2. Time priority (first come, first served)

When an order is placed, the system attempts to match it with existing orders. Partial matches are supported, and remaining amounts are tracked.

## Stopping the Application

To stop the service:

```bash
docker-compose down
```

To stop and remove all containers, networks, and volumes:

```bash
docker-compose down -v
```
