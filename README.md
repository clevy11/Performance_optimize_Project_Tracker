# Project Tracker

## Overview

Project Tracker is a comprehensive Spring Boot application designed for **BuildMaster** to efficiently manage projects, tasks, and developers. It provides a robust backend system with RESTful APIs for all core functionalities, along with advanced query capabilities, audit logging, and high-performance optimizations.

## Key Features

*   **Entity Management**: Full CRUD (Create, Read, Update, Delete) operations for Projects, Tasks, and Developers.
*   **Task Assignment**: Assign and unassign tasks to developers.
*   **Pagination & Sorting**: Efficiently browse through lists of projects, developers, and tasks with support for pagination and sorting by various attributes.
*   **Transactional Operations**: Ensures data integrity using Spring's transaction management.
*   **High-Performance Caching**: Improves performance with Caffeine cache for frequently accessed data, with TTL and size-based eviction.
*   **Lightweight DTOs**: Optimized data transfer with minimal payload size for better performance.
*   **Metrics & Monitoring**: Comprehensive metrics collection with Spring Boot Actuator and Prometheus integration.
*   **Audit Logging**: All C/U/D operations on major entities (Projects, Tasks, Developers) are logged to MongoDB for auditing purposes.
*   **Advanced Queries & Endpoints**:
    *   Retrieve overdue tasks.
    *   Get the top N developers based on the number of completed tasks.
    *   Find projects that currently have no tasks.
    *   Aggregate task counts by their status (overall and per project).
*   **API Documentation**: Integrated Swagger UI for interactive API documentation and testing.
*   **Email Notifications**: Automated email alerts for overdue tasks, with configurable SMTP settings. Notifications are sent daily at 8 AM and can also be triggered manually via API.
*   **Performance Testing**: JMeter test plans for load testing and performance validation.

## Technologies Used

*   **Backend**: Java 17, Spring Boot 3.3.1
*   **Data Persistence**:
    *   Spring Data JPA with Hibernate (for relational data)
    *   PostgreSQL (Primary RDBMS)
    *   Spring Data MongoDB (for audit logs)
    *   MongoDB
*   **API & Web**: Spring Web (RESTful APIs)
*   **Performance Optimization**:
    *   Caffeine Cache with TTL and metrics
    *   MapStruct for efficient object mapping
    *   Lightweight DTOs for API responses
    *   JVM tuning parameters
*   **Monitoring & Metrics**:
    *   Spring Boot Actuator
    *   Micrometer with Prometheus
    *   Grafana dashboards
*   **Utilities**: Lombok
*   **Build Tool**: Apache Maven
*   **API Documentation**: SpringDoc OpenAPI (Swagger UI)
*   **Development Tools**: Spring Boot DevTools
*   **Performance Testing**: Apache JMeter

## Prerequisites

Before you begin, ensure you have met the following requirements:

*   Java Development Kit (JDK) 17 or later
*   Apache Maven 3.6.x or later
*   PostgreSQL server installed and running
*   MongoDB server installed and running
*   Docker and Docker Compose (for running with Prometheus and Grafana)

## Setup and Installation

1.  **Clone the repository**:
    ```bash
    git clone <your-repository-url>
    cd ProjectTracker
    ```
2.  **Configure `application.properties`**:
    Open `src/main/resources/application.properties` and update the following properties with your local database credentials and settings:
    *   `spring.datasource.url` (e.g., `jdbc:postgresql://localhost:5432/your_db_name`)
    *   `spring.datasource.username`
    *   `spring.datasource.password`
    *   `spring.data.mongodb.uri` (e.g., `mongodb://localhost:27017/your_audit_db_name`)

3.  **Build the project using Maven**:
    ```bash
    mvn clean install
    ```

## Running the Application

1.  **Using Maven Spring Boot plugin**:
    ```bash
    mvn spring-boot:run
    ```
2.  **From your IDE**:
    Locate the `ProjectTrackerApplication.java` class and run it as a Java application.

3.  **Using Docker Compose** (with Prometheus and Grafana):
    ```bash
    docker-compose up
    ```

Once started, the application will be accessible at `http://localhost:8080`.

## Performance Optimization

The application implements several performance optimizations:

### 1. Caching Strategy
- **Caffeine Cache**: High-performance, in-memory caching with TTL and size limits
- **Cache Configuration**: See `CacheConfig.java` for detailed cache settings
- **Cache Metrics**: Real-time cache statistics available via Actuator

### 2. API Response Optimization
- **Lightweight DTOs**: Reduced payload sizes with optimized data transfer objects:
  - `ProjectSummaryDto`: Slim version of Project for list views
  - `TaskSummaryDto`: Minimal task information for better performance
- **Efficient Object Mapping**: MapStruct for high-performance bean mappings

### 3. Monitoring & Metrics
- **Spring Boot Actuator**: Comprehensive metrics and monitoring endpoints:
  - `/actuator/health`: Application health information
  - `/actuator/metrics`: Detailed metrics on application performance
  - `/actuator/prometheus`: Metrics in Prometheus format
  - `/actuator/caches`: Cache statistics
- **Custom Metrics**: Cache hit/miss ratios, response times for key operations

### 4. JVM Tuning
- **Memory Settings**: Optimized heap size configuration
- **GC Options**: Improved garbage collection settings in Dockerfile
- **Thread Management**: Proper connection pooling and thread configuration

## Performance Testing

JMeter test plans are provided in the `jmeter` directory for load testing:

1. **Run JMeter tests**:
   ```bash
   jmeter -n -t jmeter/project-tracker-test-plan.jmx -l results.jtl
   ```

2. **View results**:
   ```bash
   jmeter -g results.jtl -o report
   ```

## Monitoring with Prometheus and Grafana

1. **Start the monitoring stack**:
   ```bash
   docker-compose up
   ```

2. **Access monitoring tools**:
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (default login: admin/admin)

3. **Predefined Dashboards**: Import from `grafana/provisioning/dashboards/`

## API Endpoints & Documentation

### Performance-related Endpoints

- `GET /actuator/metrics` - View all available metrics
- `GET /actuator/metrics/{metric.name}` - View specific metric details
- `GET /actuator/prometheus` - Metrics in Prometheus format
- `GET /actuator/caches` - Cache statistics
- `GET /actuator/health` - Application health status
- `GET /api/projects/summary` - Lightweight project listings
- `GET /api/tasks/summary` - Lightweight task listings

### Notification Endpoints

- `POST /api/tasks/notifications/send-overdue` - Manually trigger overdue task notifications
- `GET /api/tasks/overdue` - Get a paginated list of overdue tasks
- `GET /api/tasks/overdue/list` - Get a complete list of overdue tasks (non-paginated)

All API endpoints are documented using Swagger/OpenAPI.

*   **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI v3 Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Database Schema (ERD)

![img_2.png](img_2.png)

## Sequence diagram:
![img_3.png](img_3.png)

## High-Level System Architecture Diagram

![img_1.png](img_1.png)

## Performance Optimization Architecture

![img.png](img.png)

## To create a Docker container for MongoDB and create a database:
```bash
docker run -d --name mongodb -p 27017:27017 -v mongodb-data:/data/db mongo:6.0
```
•  Connect again anytime with: docker exec -it mongodb mongosh
•  Use specific database: use your_database_name
•  Create collections: db.your_collection.insertOne({your: "data"})
•  View collections: show collections
•  Query data: db.your_collection.find()

## Contributing

Contributions are welcome! Please follow the standard fork-and-pull request workflow. Ensure your code adheres to the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Caleb levy Buntu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
