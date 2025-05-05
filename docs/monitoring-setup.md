# Freelancer Portal - Monitoring and Logging Setup Guide

This guide provides instructions on setting up the ELK Stack (Elasticsearch, Logstash, Kibana) and Grafana for the Freelancer Portal application.

## Table of Contents
1. [Overview](#overview)
2. [ELK Stack Setup](#elk-stack-setup)
3. [Prometheus Setup](#prometheus-setup)
4. [Grafana Dashboard Setup](#grafana-dashboard-setup)
5. [Alerting Configuration](#alerting-configuration)

## Overview

Our monitoring solution consists of the following components:

- **Structured Logging**: Using SLF4J and Logback for application logging
- **Centralized Log Collection**: Using ELK Stack (Elasticsearch, Logstash, Kibana) 
- **Metrics Collection**: Using Micrometer and Prometheus
- **Dashboard Visualization**: Using Grafana
- **Distributed Tracing**: Using Zipkin
- **Automated Alerts**: Configured for critical errors and performance issues

## ELK Stack Setup

### Prerequisites
- Docker and Docker Compose
- At least 4GB of RAM available for the ELK stack

### Setup Instructions

1. Create a `docker-compose.yml` file with the following content:

```yaml
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.12.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - elastic

  logstash:
    image: docker.elastic.co/logstash/logstash:8.12.0
    ports:
      - "5044:5044"
      - "5000:5000/tcp"
      - "5000:5000/udp"
      - "9600:9600"
    environment:
      LS_JAVA_OPTS: "-Xmx256m -Xms256m"
    volumes:
      - ./logstash/config/logstash.yml:/usr/share/logstash/config/logstash.yml:ro
      - ./logstash/pipeline:/usr/share/logstash/pipeline:ro
    networks:
      - elastic
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.12.0
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_URL: http://elasticsearch:9200
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
    networks:
      - elastic
    depends_on:
      - elasticsearch

networks:
  elastic:
    driver: bridge

volumes:
  elasticsearch-data:
```

2. Create the Logstash configuration:

Create directory structure:
```
mkdir -p logstash/config logstash/pipeline
```

Create `logstash/config/logstash.yml`:
```yaml
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
```

Create `logstash/pipeline/freelancer-portal.conf`:
```
input {
  tcp {
    port => 5000
    codec => json_lines
  }
  udp {
    port => 5000
    codec => json_lines
  }
  file {
    path => "/path/to/logs/freelancer-portal*.log"
    start_position => "beginning"
  }
}

filter {
  if [message] =~ /^{.*}$/ {
    json {
      source => "message"
    }
  }
  
  if ![timestamp] {
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
      target => "@timestamp"
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "freelancer-portal-%{+YYYY.MM.dd}"
  }
}
```

3. Start the ELK stack:

```bash
docker-compose up -d
```

4. Access Kibana at http://localhost:5601

## Prometheus Setup

1. Create a `prometheus.yml` file:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'freelancer-portal'
    metrics_path: '/api/v1/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

2. Run Prometheus:

```bash
docker run -d --name prometheus -p 9090:9090 \
  -v /path/to/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

## Grafana Dashboard Setup

1. Run Grafana:

```bash
docker run -d --name grafana -p 3000:3000 grafana/grafana
```

2. Access Grafana at http://localhost:3000 (default credentials: admin/admin)

3. Add Prometheus as a data source:
   - Go to Configuration > Data Sources > Add data source
   - Select Prometheus
   - URL: http://host.docker.internal:9090
   - Click "Save & Test"

4. Import sample dashboards:
   - Go to Create > Import
   - Enter dashboard ID: 4701 (for Spring Boot Statistics)
   - Select your Prometheus data source
   - Click "Import"

5. Create custom Freelancer Portal dashboard:
   - Create a new dashboard with the following panels:
     - API Request Rate
     - Error Rate
     - Response Time (95th percentile)
     - Database Query Time
     - JVM Memory Usage
     - Project Creation Rate
     - User Login Count
     - Project Completion Rate

## Custom Dashboard Panels

### API Request Rate
- Query: `rate(http_server_requests_seconds_count[1m])`
- Panel Type: Graph
- Description: Shows the rate of API requests per minute

### Error Rate
- Query: `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) / sum(rate(http_server_requests_seconds_count[1m]))`
- Panel Type: Graph
- Format: Percentage
- Description: Shows the percentage of requests resulting in errors

### Response Time (95th percentile)
- Query: `http_server_requests_seconds{quantile="0.95"}`
- Panel Type: Graph
- Format: Time (ms)
- Description: Shows the 95th percentile of response times

### JVM Memory Usage
- Query: `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}`
- Panel Type: Gauge
- Format: Percentage
- Description: Shows current JVM heap memory usage

## Alerting Configuration

The application includes built-in alerts for:
- High error rates (above 5%)
- Slow API response times (95th percentile > 500ms)
- High memory usage (above 85%)

Configure alert receivers in the application properties:

```yaml
# Add to application.yml
alerts:
  enabled: true
  slack:
    webhook: "https://hooks.slack.com/services/your/slack/webhook"
  email:
    to: "alerts@yourcompany.com"
```

## Additional Configuration

To enable centralized log shipping to the ELK stack, add the following to your `application.yml`:

```yaml
elk:
  logstash:
    host: localhost
    port: 5000
    enabled: true
```

To enable distributed tracing with Zipkin:

```yaml
spring:
  zipkin:
    baseUrl: http://localhost:9411/
    enabled: true
  sleuth:
    sampler:
      probability: 1.0
```