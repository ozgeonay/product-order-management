# Product Order Management Service

A robust Spring Boot REST API for managing products and orders, built with SOLID principles and Spring Boot best practices. Features comprehensive validation, exception handling, and full test coverage.

## Technology Stack

- **Java**: 21.0.3
- **Spring Boot**: 3.3.5
- **Database**: H2 (in-memory)
- **Build Tool**: Maven 3.8.5
- **API Documentation**: Swagger/OpenAPI 3.0
- **Testing**: JUnit 5, Mockito, MockMvc
- **Lombok**: For reducing boilerplate code

## Features

### Core Functionality
- ✅ Complete CRUD operations for Products
- ✅ Order creation and cancellation
- ✅ Automatic stock management
- ✅ Real-time stock validation
- ✅ Automatic price calculation
- ✅ Stock restoration on order cancellation

### Technical Excellence
- ✅ **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- ✅ **YAGNI Applied**: No over-engineering, only necessary abstractions
- ✅ **DTO Pattern**: Separation of API contracts from domain models
- ✅ **Mapper Pattern**: Clean entity-DTO conversions
- ✅ **Bean Validation**: Input validation with Jakarta Validation
- ✅ **Exception Handling**: Centralized with @RestControllerAdvice
- ✅ **Transaction Management**: Proper @Transactional usage
- ✅ **Logging**: Structured logging with SLF4J
- ✅ **Constants Management**: Centralized error messages
- ✅ **Constructor Injection**: Best practice dependency injection

### Testing
- **76 Tests Total** (All Passing ✅)
  - Unit Tests: 49 (Controller, Service, Mapper layers)
  - Integration Tests: 27 (Full stack, real database)
- **100% Test Coverage** on business logic
- **Best Practices**: Mockito, AssertJ, MockMvc, Nested tests, Descriptive names

## Project Structure

```
product-order-management-service/
├── src/main/java/com/demo/productordermanagement/
│   ├── ProductOrderManagementServiceApplication.java
│   ├── config/
│   │   └── OpenApiConfig.java
│   ├── constants/
│   │   └── ErrorMessages.java
│   ├── controller/
│   │   ├── HomeController.java
│   │   ├── ProductController.java
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   ├── OrderDTO.java
│   │   └── ErrorResponse.java
│   ├── entity/
│   │   ├── Product.java
│   │   └── Order.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── OutOfStockException.java
│   ├── mapper/
│   │   ├── ProductMapper.java
│   │   └── OrderMapper.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   └── OrderRepository.java
│   └── service/
│       ├── ProductService.java
│       └── OrderService.java
├── src/test/java/com/demo/productordermanagement/
│   ├── controller/
│   │   ├── ProductControllerTest.java
│   │   └── OrderControllerTest.java
│   ├── service/
│   │   ├── ProductServiceTest.java
│   │   └── OrderServiceTest.java
│   ├── mapper/
│   │   ├── ProductMapperTest.java
│   │   └── OrderMapperTest.java
│   └── integration/
│       ├── ProductIntegrationTest.java
│       └── OrderIntegrationTest.java
├── src/main/resources/
│   ├── application.properties
│   └── data.sql
└── pom.xml
```

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+

### Run the Application

```bash
# Clone the repository
git clone git@github.com:ozgeonay/product-order-management.git
cd product-order-management

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
# Run all tests (unit + integration)
mvn test

# Run with coverage
mvn test jacoco:report
```

## API Documentation

### Swagger UI
Access interactive API documentation at:
- **URL**: `http://localhost:8080/swagger-ui.html`
- **API Docs JSON**: `http://localhost:8080/api-docs`

The root path (`http://localhost:8080/`) automatically redirects to Swagger UI.

### Products API

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/products` | Get all products | 200 OK |
| GET | `/api/products/{id}` | Get product by ID | 200 OK / 404 Not Found |
| POST | `/api/products` | Create new product | 201 Created / 400 Bad Request |
| PUT | `/api/products/{id}` | Update product | 200 OK / 404 Not Found |
| DELETE | `/api/products/{id}` | Delete product | 204 No Content / 404 Not Found |

### Orders API

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/api/orders` | Get all orders | 200 OK |
| GET | `/api/orders/{id}` | Get order by ID | 200 OK / 404 Not Found |
| POST | `/api/orders` | Create new order | 201 Created / 400 Bad Request / 404 Not Found |
| DELETE | `/api/orders/{id}` | Cancel order | 204 No Content / 404 Not Found |

## API Examples

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tablet",
    "price": 399.99,
    "stock": 15
  }'
```

**Response** (201 Created):
```json
{
  "id": 9,
  "name": "Tablet",
  "price": 399.99,
  "stock": 15
}
```

### Create an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "customerName": "John Doe",
    "quantity": 2
  }'
```

**Response** (201 Created):
```json
{
  "id": 1,
  "productId": 1,
  "customerName": "John Doe",
  "quantity": 2,
  "totalPrice": 1999.98,
  "orderDate": "2026-03-28T21:30:45"
}
```

### Get Product (Stock Decreased)
```bash
curl http://localhost:8080/api/products/1
```

**Response**:
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "stock": 8
}
```
Note: Stock decreased from 10 to 8 (2 units ordered)

### Cancel Order (Stock Restored)
```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

**Response**: 204 No Content

Product stock is automatically restored back to 10.

### Error Handling Examples

**Out of Stock**:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "customerName": "Jane", "quantity": 100}'
```

**Response** (400 Bad Request):
```json
{
  "timestamp": "2026-03-28T21:30:45",
  "status": 400,
  "error": "Out of Stock",
  "message": "Not enough stock for product: Laptop. Available: 10, Requested: 100",
  "path": "/api/orders"
}
```

**Product Not Found**:
```bash
curl http://localhost:8080/api/products/999
```

**Response** (404 Not Found):
```json
{
  "timestamp": "2026-03-28T21:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/products/999"
}
```

## H2 Database Console

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection Settings**:
- **JDBC URL**: `jdbc:h2:mem:productordermanagementdb`
- **Username**: `sa`
- **Password**: (leave empty)

**Note**: This is an in-memory database. Data is reset when the application restarts.

## Sample Data

The application comes with 8 pre-loaded products:

| ID | Product | Price | Stock |
|----|---------|-------|-------|
| 1 | Laptop | $999.99 | 10 |
| 2 | Smartphone | $599.99 | 25 |
| 3 | Headphones | $79.99 | 50 |
| 4 | Keyboard | $49.99 | 30 |
| 5 | Mouse | $29.99 | 40 |
| 6 | Monitor | $299.99 | 15 |
| 7 | Webcam | $89.99 | 20 |
| 8 | USB Cable | $9.99 | 100 |

## Architecture & Design Patterns

### Layered Architecture
```
┌─────────────────────────────────────┐
│       Controller Layer              │  ← REST Endpoints, HTTP handling
├─────────────────────────────────────┤
│         Service Layer               │  ← Business logic, transactions
├─────────────────────────────────────┤
│       Repository Layer              │  ← Database operations
├─────────────────────────────────────┤
│       Database (H2)                 │  ← Data persistence
└─────────────────────────────────────┘
```

### Design Patterns Applied
- **DTO Pattern**: Separate API contracts from domain entities
- **Mapper Pattern**: Clean object transformations
- **Repository Pattern**: Abstraction over data access
- **Service Pattern**: Encapsulated business logic
- **Exception Handler Pattern**: Centralized error handling
- **Builder Pattern**: Fluent object creation (Lombok @Builder)

### SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Proper inheritance and contracts
- **Interface Segregation**: No unnecessary dependencies
- **Dependency Inversion**: Depend on abstractions via Spring DI

## Validation Rules

### Product Validation
- **Name**: Required, cannot be blank
- **Price**: Required, must be ≥ 0
- **Stock**: Required, must be ≥ 0

### Order Validation
- **Product ID**: Required, must exist
- **Customer Name**: Required, cannot be blank
- **Quantity**: Required, must be ≥ 1
- **Stock Availability**: Requested quantity must not exceed available stock

## Business Rules

### Order Creation
1. Validate product exists
2. Check stock availability
3. Calculate total price (price × quantity)
4. Decrease product stock
5. Set order date automatically
6. Save order to database

### Order Cancellation
1. Validate order exists
2. Find associated product
3. Restore product stock
4. Delete order from database

## Testing

### Unit Tests (49 tests)
- **Mapper Tests**: Entity ↔ DTO conversions
- **Service Tests**: Business logic with mocked dependencies
- **Controller Tests**: API endpoints with mocked services

### Integration Tests (27 tests)
- **Full Stack Tests**: Controller → Service → Repository → Database
- **End-to-End Flows**: Complete CRUD operations
- **Stock Management**: Multi-order scenarios with stock tracking
- **Error Scenarios**: 404, 400, validation errors

### Test Execution
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"
```

## Configuration

### Application Properties
```properties
# Database
spring.datasource.url=jdbc:h2:mem:productordermanagementdb
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
```

## Postman Collection

A complete Postman collection is included in `postman_collection.json` with all API endpoints and example requests.

Import it into Postman and start testing immediately!

## Development

### Build Project
```bash
mvn clean install
```

### Package as JAR
```bash
mvn clean package
java -jar target/product-order-management-service-1.0.0.jar
```

### Code Style
- Constructor-based dependency injection
- Lombok for boilerplate reduction
- SLF4J for logging
- Clear naming conventions
- Comprehensive JavaDoc on public APIs

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-03-28T21:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/products/999",
  "validationErrors": null
}
```

Validation errors include field-specific details:

```json
{
  "timestamp": "2026-03-28T21:30:45",
  "status": 400,
  "error": "Validation Error",
  "message": "Validation failed for request",
  "path": "/api/products",
  "validationErrors": [
    {
      "field": "name",
      "message": "Product name is required"
    },
    {
      "field": "price",
      "message": "Price must be greater than or equal to 0"
    }
  ]
}
```

## Demo Scenario

### Complete Order Flow

```bash
# 1. Check initial stock
curl http://localhost:8080/api/products/1
# Response: {"id":1,"name":"Laptop","price":999.99,"stock":10}

# 2. Create an order for 3 laptops
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "customerName": "Alice", "quantity": 3}'
# Response: {"id":1,"productId":1,"customerName":"Alice","quantity":3,"totalPrice":2999.97}

# 3. Check stock decreased
curl http://localhost:8080/api/products/1
# Response: {"id":1,"name":"Laptop","price":999.99,"stock":7}

# 4. Cancel the order
curl -X DELETE http://localhost:8080/api/orders/1
# Response: 204 No Content

# 5. Check stock restored
curl http://localhost:8080/api/products/1
# Response: {"id":1,"name":"Laptop","price":999.99,"stock":10}
```

## Contributing

This is a production-ready template demonstrating:
- Modern Spring Boot development
- Enterprise-grade error handling
- Comprehensive testing strategies
- Clean architecture principles
- API documentation best practices

Feel free to use this as a starting point for your own projects!

## License

This is a demo project for educational and production purposes.
