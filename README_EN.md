# Tencent Cloud CVM Management Platform

A Spring Boot and Vue 3 based Tencent Cloud Cloud Virtual Machine (CVM) management platform that provides instance query, configuration selection, and price calculation functionalities.

## 📋 Project Overview

This project is a frontend-backend separated web application for managing and querying Tencent Cloud CVM instances. Through a user-friendly interface, you can:

- Browse and filter available instance specifications
- Query real-time prices for different configurations
- View and manage cloud server instances
- Export/import configuration templates
- Support multi-language interface (Chinese/English)

## ✨ Features

### Core Features

1. **Credential Management**
   - Dynamic configuration of Tencent Cloud SecretId/SecretKey
   - Support for setting default region
   - Credential validation and status indication

2. **Resource Query**
   - Region list query
   - Availability zone list query
   - Instance specification list (support CPU, memory, GPU filtering)
   - Image list query
   - Instance list query (with pagination support)

3. **Price Calculation**
   - Real-time price query
   - Support for pay-as-you-go and spot instances
   - Automatic calculation of instance, bandwidth, and disk prices
   - Intelligent availability zone selection

4. **Configuration Management**
   - Multi-step configuration wizard
   - Configuration template export/import (JSON format)
   - Local caching mechanism (10-minute validity)

5. **User Experience**
   - Responsive design
   - Multi-language support (Chinese/English)
   - Real-time data refresh
   - Intelligent filtering and search

## 🛠️ Technology Stack

### Backend

- **Framework**: Spring Boot 2.7.18
- **Language**: Java 8
- **Build Tool**: Maven
- **SDK**: Tencent Cloud SDK Java (tencentcloud-sdk-java-cvm 3.1.1290)

### Frontend

- **Framework**: Vue 3.4.38
- **UI Component Library**: Element Plus 2.8.4
- **Build Tool**: Vite 5.4.2
- **State Management**: Vue Composition API

## 📁 Project Structure

```
tengxunCVM/
├── frontend-elementplus/          # Frontend project
│   ├── src/
│   │   ├── App.vue                # Main application component
│   │   ├── api.js                 # API call encapsulation
│   │   ├── cache.js               # Cache management
│   │   ├── main.js                # Entry file
│   │   └── style.css              # Style file
│   ├── index.html                 # HTML template
│   ├── package.json               # Frontend dependency configuration
│   └── vite.config.js             # Vite configuration
│
├── src/main/java/                 # Backend source code
│   └── com/example/tengxuncvm/
│       ├── service/               # Business service layer
│       │   ├── CredentialStore.java          # Credential management
│       │   ├── CvmClientFactory.java         # CVM client factory
│       │   ├── RegionService.java            # Region service
│       │   ├── ZoneService.java              # Zone service
│       │   ├── InstanceTypeService.java      # Instance specification service
│       │   ├── ImageService.java             # Image service
│       │   ├── InstanceService.java          # Instance management service
│       │   ├── CvmPriceService.java          # Price query service
│       │   ├── CvmInstanceCheckService.java  # Instance check service
│       │   ├── CvmInstanceDestroyService.java # Instance destruction service
│       │   └── CvmInstanceRenewService.java  # Instance renewal service
│       └── web/                   # Web controller layer
│           ├── CvmApiController.java         # REST API controller
│           ├── ApiExceptionHandler.java      # Exception handling
│           └── *.java                        # DTO classes
│
├── src/main/resources/
│   ├── application.properties     # Application configuration
│   └── static/                    # Static resources (frontend build output)
│
├── pom.xml                        # Maven configuration
└── README.md                      # Project documentation (Chinese)
```

## 🔧 Prerequisites

- **JDK**: 1.8 or higher
- **Maven**: 3.6 or higher
- **Node.js**: 16 or higher
- **npm**: 8 or higher

## 🚀 Quick Start

### 1. Clone the Project

```bash
git clone <repository-url>
cd tengxunCVM
```

### 2. Configure Backend

Edit `src/main/resources/application.properties`:

```properties
# Tencent Cloud credential configuration (optional, can also be configured via frontend interface)
tencentcloud.secret-id=your-secret-id
tencentcloud.secret-key=your-secret-key
tencentcloud.default-region=ap-guangzhou

# Server configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

### 3. Build and Run Backend

```bash
# Build with Maven
mvn clean package

# Run Spring Boot application
mvn spring-boot:run

# Or run the jar file
java -jar target/tengxunCVM-0.0.1-SNAPSHOT.jar
```

The backend service runs by default at `http://localhost:8080`

### 4. Build and Run Frontend

```bash
# Enter frontend directory
cd frontend-elementplus

# Install dependencies
npm install

# Run in development mode
npm run dev

# Build production version (output to src/main/resources/static)
npm run build
```

The frontend development server runs by default at `http://localhost:5173`

### 5. Access the Application

- Development mode: Access `http://localhost:5173`
- Production mode: Access `http://localhost:8080` (frontend packaged into backend static resources)

## 📡 API Documentation

### Base Path

All APIs have the base path: `/api`

### API List

#### 1. Update Credentials

**POST** `/api/credentials`

Update Tencent Cloud credential information.

**Request Body:**
```json
{
  "secretId": "your-secret-id",
  "secretKey": "your-secret-key",
  "defaultRegion": "ap-guangzhou"
}
```

**Response:**
```json
{
  "status": "ok"
}
```

#### 2. Get Region List

**GET** `/api/regions`

Get all available region lists.

**Response:**
```json
["ap-guangzhou", "ap-shanghai", "ap-beijing", ...]
```

#### 3. Get Zone List

**GET** `/api/zones?region={region}`

Get the availability zone list for the specified region.

**Parameters:**
- `region` (required): Region code, e.g., `ap-guangzhou`

**Response:**
```json
["ap-guangzhou-1", "ap-guangzhou-2", "ap-guangzhou-3", ...]
```

#### 4. Get Instance Type List

**GET** `/api/instance-types?region={region}`

Get the instance specification list for the specified region.

**Parameters:**
- `region` (required): Region code

**Response:**
```json
[
  {
    "instanceType": "S1.SMALL1",
    "instanceFamily": "S1",
    "cpu": 1,
    "memory": 1,
    "gpu": 0,
    "zone": "ap-guangzhou-1"
  },
  ...
]
```

#### 5. Get Image List

**GET** `/api/images?region={region}`

Get the image list for the specified region.

**Parameters:**
- `region` (required): Region code

**Response:**
```json
[
  {
    "imageId": "img-xxx",
    "imageName": "Ubuntu Server 20.04",
    "imageOsName": "Ubuntu"
  },
  ...
]
```

#### 6. Query Price

**POST** `/api/price`

Query the price for the specified configuration.

**Request Body:**
```json
{
  "region": "ap-guangzhou",
  "zone": "ap-guangzhou-1",
  "instanceType": "S1.SMALL1",
  "imageId": "img-xxx",
  "systemDiskGb": 50,
  "dataDiskGb": 100,
  "bandwidthMbps": 5,
  "bandwidthChargeType": "BANDWIDTH_POSTPAID_BY_HOUR",
  "instanceChargeType": "POSTPAID_BY_HOUR",
  "scheduledDestroy": false
}
```

**Response:**
```json
{
  "instancePrice": 0.12,
  "bandwidthPrice": 0.25,
  "systemDiskPrice": 0.0,
  "dataDiskPrice": 0.0,
  "totalPrice": 0.37,
  "currency": null,
  "zone": "ap-guangzhou-1",
  "scheduledDestroy": false
}
```

#### 7. Get Instance List

**GET** `/api/instances?region={region}&page={page}&size={size}`

Get the instance list for the specified region (paginated).

**Parameters:**
- `region` (required): Region code
- `page` (optional): Page number, default 1
- `size` (optional): Items per page, default 10, maximum 50

**Response:**
```json
{
  "totalCount": 10,
  "page": 1,
  "size": 10,
  "items": [
    {
      "instanceId": "ins-xxx",
      "instanceName": "test-instance",
      "instanceType": "S1.SMALL1",
      "cpu": 1,
      "memory": 1,
      "zone": "ap-guangzhou-1",
      "instanceChargeType": "POSTPAID_BY_HOUR",
      "instanceState": "RUNNING"
    },
    ...
  ]
}
```

## 💡 Usage Guide

### First-time Use

1. **Configure Credentials**
   - Click the "Credentials" button in the top right corner
   - Enter Tencent Cloud SecretId and SecretKey
   - Set default region (optional)
   - Click "Save"

2. **Select Region and Billing Method**
   - Select region in Step 1
   - Choose instance billing method (pay-as-you-go/spot)

3. **Select Instance Specification**
   - Browse instance specification list in Step 2
   - Use filter conditions (keywords, specification family, CPU, memory, GPU) to filter
   - Click table row to select instance specification

4. **Configure Storage and Network**
   - Select image in Step 3
   - Configure system disk and data disk size
   - Configure bandwidth and billing method

5. **View Price and Export Configuration**
   - Click "Calculate Price" in Step 4 to view total price
   - Can export configuration template or import existing template

### Advanced Features

- **Configuration Template**: Export current configuration as JSON file for easy reuse
- **Caching Mechanism**: Frontend automatically caches API responses, valid for 10 minutes
- **Multi-language**: Support Chinese and English switching
- **Real-time Refresh**: Click "Refresh List" or "Refresh Price" to update data

## ⚠️ Important Notes

1. **Credential Security**
   - Credential information is stored in browser local storage
   - Production environments should use more secure credential management methods
   - Do not commit configuration files containing credentials to version control systems

2. **API Limitations**
   - Tencent Cloud API has call frequency limits
   - Frontend has implemented caching mechanism to reduce API calls
   - Recommend reasonable use of refresh functionality

3. **Price Query**
   - Price query automatically tries multiple availability zones
   - If the specified availability zone does not support the instance specification, other zones will be automatically selected
   - Price information is for reference only, actual prices are subject to Tencent Cloud console

4. **Browser Compatibility**
   - Recommended to use modern browsers (Chrome, Firefox, Edge, etc.)
   - Requires support for ES6+ and LocalStorage

## 🐳 Docker Deployment

### 1. Using Docker Compose (Recommended)

Create `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "8080:8080"
    environment:
      - TENCENTCLOUD_SECRET_ID=${TENCENTCLOUD_SECRET_ID}
      - TENCENTCLOUD_SECRET_KEY=${TENCENTCLOUD_SECRET_KEY}
      - TENCENTCLOUD_DEFAULT_REGION=${TENCENTCLOUD_DEFAULT_REGION:-ap-guangzhou}
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend-elementplus
      dockerfile: Dockerfile.frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend
    restart: unless-stopped
```

### 2. Create Dockerfiles

**Backend Dockerfile (`Dockerfile.backend`):**
```dockerfile
FROM maven:3.8.4-openjdk-8-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile (`frontend-elementplus/Dockerfile.frontend`):**
```dockerfile
FROM node:16-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3. Environment Variables Configuration

Create `.env` file:

```env
TENCENTCLOUD_SECRET_ID=your-secret-id
TENCENTCLOUD_SECRET_KEY=your-secret-key
TENCENTCLOUD_DEFAULT_REGION=ap-guangzhou
```

### 4. Start Services

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 5. Access the Application

- Backend API: http://localhost:8080
- Frontend interface: http://localhost:5173
- Production mode (Nginx): http://localhost:80

## 🔧 Environment Variables Configuration

In addition to configuring in `application.properties`, environment variables are also supported:

| Environment Variable | Description | Default Value |
|---------------------|-------------|---------------|
| `TENCENTCLOUD_SECRET_ID` | Tencent Cloud SecretId | None |
| `TENCENTCLOUD_SECRET_KEY` | Tencent Cloud SecretKey | None |
| `TENCENTCLOUD_DEFAULT_REGION` | Default region | `ap-guangzhou` |
| `SERVER_PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |

### Configuration Priority

1. Environment variables (highest priority)
2. `application.properties` file
3. Frontend interface configuration

## 🐛 Troubleshooting

### Common Issues

#### 1. API Call Failure
- **Symptoms**: Frontend displays "Network error" or "API call failed"
- **Possible causes**:
  - Invalid or expired credentials
  - Network connection issues
  - Tencent Cloud API service exception
- **Solutions**:
  1. Check if credentials are correct
  2. Verify network connection
  3. Check browser console error messages
  4. Check backend logs

#### 2. Price Query Returns Empty Value
- **Symptoms**: Price calculation shows 0 or empty
- **Possible causes**:
  - Instance specification not available in selected region
  - Tencent Cloud price API returns exception
- **Solutions**:
  1. Try other regions
  2. Check if instance specification is supported
  3. Check detailed errors in backend logs

#### 3. Frontend Cache Issues
- **Symptoms**: Data not updating or showing old data
- **Solutions**:
  1. Clear browser cache
  2. Click "Refresh List" button
  3. Wait for cache to expire (10 minutes)

#### 4. Docker Container Startup Failure
- **Symptoms**: `docker-compose up` fails
- **Solutions**:
  1. Check Docker and Docker Compose versions
  2. Ensure `.env` file exists and is correctly configured
  3. Check container logs: `docker-compose logs [service-name]`

### Log Viewing

#### Backend Logs
```bash
# View Spring Boot application logs
docker-compose logs backend

# View logs in real-time
docker-compose logs -f backend

# Enter container to view logs
docker-compose exec backend tail -f /app/logs/application.log
```

#### Frontend Logs
- Browser Developer Tools (F12) → Console tab
- Browser Developer Tools → Network tab to view API requests

## 🧪 Testing Guide

### Backend Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TengxunCvmApplicationTests

# Generate test coverage report
mvn clean test jacoco:report
```

### Test Structure

```
src/test/java/com/example/tengxuncvm/
└── TengxunCvmApplicationTests.java    # Application context tests
```

### Frontend Testing

The current project does not have a frontend testing framework configured. It is recommended to add:

1. **Unit Testing**: Using [Vitest](https://vitest.dev/)
2. **Component Testing**: Using [Vue Test Utils](https://test-utils.vuejs.org/)
3. **E2E Testing**: Using [Cypress](https://www.cypress.io/) or [Playwright](https://playwright.dev/)

### Adding Test Examples

**Install Vitest:**
```bash
cd frontend-elementplus
npm install -D vitest @vue/test-utils jsdom
```

**Create test file `src/__tests__/api.test.js`:**
```javascript
import { describe, it, expect } from 'vitest'
import { calculateTotalPrice } from '../api.js'

describe('API Function Tests', () => {
  it('should correctly calculate total price', () => {
    const instancePrice = 0.12
    const bandwidthPrice = 0.25
    const total = calculateTotalPrice(instancePrice, bandwidthPrice)
    expect(total).toBe(0.37)
  })
})
```

## 🔨 Development Guide

### Backend Development

```bash
# Run tests
mvn test

# Package
mvn clean package

# Package skipping tests
mvn clean package -DskipTests
```

### Frontend Development

```bash
# Enter frontend directory
cd frontend-elementplus

# Install dependencies
npm install

# Development mode (hot reload)
npm run dev

# Build production version
npm run build

# Preview build results
npm run preview
```

### Code Standards

- Backend follows Java coding standards
- Frontend follows Vue 3 best practices
- Uses Spring Boot standard project structure

## 📝 Changelog

### v0.0.1-SNAPSHOT

- Initial version
- Implement basic functions: region, zone, instance specification, image query
- Implement price query function
- Implement instance list query
- Frontend multi-step configuration wizard
- Support configuration template import/export
- Multi-language support

## 🤝 Contributing

Welcome to submit Issues and Pull Requests!

## 📄 License

This project uses the MIT License.

## 🔗 Related Links

- [Tencent Cloud CVM Documentation](https://cloud.tencent.com/document/product/213)
- [Tencent Cloud SDK Java](https://github.com/TencentCloud/tencentcloud-sdk-java)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vue 3 Documentation](https://vuejs.org/)
- [Element Plus Documentation](https://element-plus.org/)

## 📧 Contact

If you have questions or suggestions, please provide feedback through Issues.

---

**Note**: Using this tool requires a valid Tencent Cloud account and API credentials. Please keep your credential information secure.
