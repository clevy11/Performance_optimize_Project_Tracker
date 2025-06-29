# Project Tracker Performance Guidelines

## Performance Optimization Overview

This document outlines the performance optimizations implemented in the Project Tracker application and provides guidance for monitoring, testing, and further tuning.

## Implemented Optimizations

### 1. Caching Architecture
- **Caffeine Cache**: High-performance caching library replacing Spring's default cache
- **Optimized Cache Configuration**:
  - TTL (Time-To-Live): 10 minutes for all cached data
  - Maximum Size: 500 entries per cache to prevent memory issues
  - Statistics enabled for monitoring

### 2. DTO Pattern Optimization
- **Lightweight DTOs**: Implemented using MapStruct
  - `ProjectSummaryDto`: Smaller payload for list views
  - `TaskSummaryDto`: Reduces over-fetching in task listings
- **Efficient Mapping**: Using MapStruct for zero-reflection, compile-time mapping generation

### 3. JVM Tuning
- **Garbage Collection**: G1GC configured for predictable pause times
- **Memory Settings**: 
  - Initial Heap: 256MB
  - Maximum Heap: 512MB
  - GC Tuning: MaxGCPauseMillis=200ms
- **Monitoring**: HeapDump generation on OutOfMemoryError

### 4. Observability
- **Spring Boot Actuator**: Comprehensive endpoints exposed
- **Prometheus Integration**: Metrics available at /actuator/prometheus
- **Custom Metrics**: Service timings, cache hit ratios, API response times
- **Grafana Dashboard**: Pre-configured monitoring visualizations

## Performance Testing

### JMeter Test Plan
A JMeter test plan is provided in the `jmeter` directory for performance testing. It includes:

1. **Authentication Test**: Obtains JWT token for subsequent tests
2. **GET /projects**: Tests project listing performance with 100 concurrent users
3. **POST /tasks**: Tests task creation with 50 concurrent users
4. **GET /users/{id}/tasks**: Tests user task fetching with 100 concurrent users

### Running Performance Tests

1. Start the application with performance profile:
   ```bash
   docker-compose up -d
   ```

2. Run JMeter tests:
   ```bash
   jmeter -n -t jmeter/project-tracker-test-plan.jmx -l results.jtl
   ```

3. Generate performance report:
   ```bash
   jmeter -g results.jtl -o performance-report
   ```

## Monitoring

### Prometheus
Prometheus is configured to scrape metrics from the application every 5 seconds. Access the Prometheus UI at:
```
http://localhost:9090
```

### Grafana
A pre-configured Grafana dashboard is available to visualize metrics. Access Grafana at:
```
http://localhost:3000
```

Default credentials:
- Username: admin
- Password: admin

### Key Metrics to Monitor

1. **JVM Metrics**:
   - Heap memory usage
   - Garbage collection frequency and duration
   - Thread counts

2. **API Performance**:
   - Request rate
   - 95th percentile response time
   - Error rates

3. **Cache Performance**:
   - Hit ratio
   - Eviction rate
   - Cache size

4. **Database Metrics**:
   - Connection pool utilization
   - Query execution time

## Further Tuning Recommendations

1. **Database Optimization**:
   - Consider adding indexes for common queries
   - Review and optimize complex JPA queries
   - Configure connection pool sizing based on load testing

2. **API Endpoint Optimization**:
   - Implement pagination for all list endpoints
   - Use projections for read-heavy operations
   - Consider request compression for large responses

3. **Advanced Caching Strategies**:
   - Redis for distributed caching in clustered environments
   - Cache warming on application startup
   - Custom eviction policies based on access patterns

4. **Thread Pool Tuning**:
   - Adjust Tomcat connection settings based on observed concurrency
   - Configure task executor thread pools for background processing

## Common Performance Issues and Solutions

| Issue | Possible Cause | Solution |
|-------|----------------|----------|
| High response times | Database queries | Review slow queries, add indexes |
| Memory leaks | Object reference retention | Take heap dumps, analyze with JProfiler |
| CPU spikes | Inefficient algorithms | Profile hot methods, optimize algorithms |
| Connection timeouts | Insufficient connection pool | Increase pool size, implement connection validation |

## How to Take Thread Dumps

For diagnosing concurrency issues:

```bash
# Using JDK tools
jcmd <pid> Thread.print > thread_dump.txt

# Using Docker
docker exec -it project-tracker jcmd 1 Thread.print > thread_dump.txt
```

## How to Take Heap Dumps

For diagnosing memory issues:

```bash
# Using JDK tools
jcmd <pid> GC.heap_dump /path/to/heap_dump.hprof

# Using Docker
docker exec -it project-tracker jcmd 1 GC.heap_dump /tmp/heap_dump.hprof
docker cp project-tracker:/tmp/heap_dump.hprof ./heap_dump.hprof
```

## Analyzing Performance Data

1. Use [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html) or [VisualVM](https://visualvm.github.io/) to analyze heap and thread dumps
2. Examine Grafana dashboards for trends and correlations
3. Review JMeter test results for throughput and latency patterns 