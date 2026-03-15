# Deployment Guide

This project includes a Vue 3 + Element Plus frontend and a Spring Boot backend.

## Requirements

- Node.js 18+
- npm 9+
- JDK 17+
- Maven 3.9+

## Local Development

### Frontend

```bash
cd frontend-elementplus
npm install
npm run dev
```

### Backend

```bash
mvn spring-boot:run
```

## Production Build

### 1. Build frontend

```bash
cd frontend-elementplus
npm install
npm run build
```

### 2. Build backend

```bash
cd ..
mvn clean package -DskipTests
```

After packaging, the runnable JAR will be generated under:

```bash
target/
```

## Notes

- Environment-specific deployment scripts are not included in this GitHub edition.
- You can adapt your own CI/CD workflow based on the standard frontend build + backend package process above.
