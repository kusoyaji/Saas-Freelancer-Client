# Freelancer Portal - SaaS Platform

A comprehensive freelancer management platform built with Spring Boot and modern web technologies.

## 🚀 Features

- **User Management**: Registration, authentication, and profile management with JWT tokens
- **Profile Pictures**: Upload and manage user profile pictures (up to 5MB)
- **Project Management**: Create and manage freelance projects
- **Time Tracking**: Track time spent on projects
- **Invoicing**: Generate and manage invoices
- **Payments**: Process and track payments
- **Messaging**: Real-time messaging between users
- **File Management**: Upload and manage project files
- **Notifications**: Real-time notifications via WebSocket
- **Health Monitoring**: Actuator endpoints for application health checks

## 🛠️ Technology Stack

### Backend
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **WebSocket** for real-time features
- **Maven** for dependency management

### Additional Technologies
- **HikariCP** for connection pooling
- **SLF4J & Logback** for logging
- **Spring Actuator** for monitoring
- **Lombok** for reducing boilerplate code

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0+
- Maven 3.6+ (or use included Maven wrapper)
- Git

## 🔧 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/kusoyaji/Saas-Freelancer-Client.git
cd Saas-Freelancer-Client
```

### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE freelancerporal;
```

### 3. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` or set environment variables:

```properties
# Database
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Secret (generate a secure random string)
JWT_SECRET=your_secure_jwt_secret_key_here
```

### 4. Update Application Configuration

Edit `src/main/resources/application.properties` if needed:

```properties
# Database (uses environment variables)
spring.datasource.url=jdbc:mysql://localhost:3306/freelancerporal?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}

# JWT
app.security.jwt.secret=${JWT_SECRET}
```

### 5. Build the Project

Using Maven wrapper (recommended):

```bash
./mvnw clean install
```

Or with Maven:

```bash
mvn clean install
```

### 6. Run the Application

```bash
./mvnw spring-boot:run
```

Or:

```bash
java -jar target/freelancer-portal-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📡 API Endpoints

### Public Endpoints

- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token
- `GET /actuator/health` - Health check
- `GET /uploads/**` - Access uploaded files

### Protected Endpoints (Require Authentication)

All other endpoints require JWT token in header:
```
Authorization: Bearer <your_jwt_token>
```

**User Management:**
- `GET /users/me` - Get current user profile
- `PUT /users/me` - Update current user profile
- `POST /users/me/profile-picture` - Upload profile picture
- `DELETE /users/me/profile-picture` - Delete profile picture

**Projects:**
- `GET /projects` - Get all projects
- `POST /projects` - Create new project
- `GET /projects/{id}` - Get project by ID
- `PUT /projects/{id}` - Update project
- `DELETE /projects/{id}` - Delete project

**And more...** See [API_ENDPOINTS.md](API_ENDPOINTS.md) for complete API documentation.

## 🧪 Testing

### Run Tests

```bash
./mvnw test
```

### Test with PowerShell Script

```powershell
.\test-api.ps1
```

This will test:
- Health check
- User registration
- User login
- Authenticated endpoints

### Manual API Testing

Using curl:

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "role": "FREELANCER"
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

## 📁 Project Structure

```
Saas-Freelancer-Client/
├── src/
│   ├── main/
│   │   ├── java/com/freelancer/portal/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── model/           # Entity classes
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── security/        # Security configuration
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/                    # Test classes
├── uploads/                     # User uploaded files (gitignored)
├── docs/                        # Documentation
├── pom.xml                      # Maven configuration
├── .gitignore
├── .env.example                 # Environment variables template
└── README.md
```

## 🔐 Security

- JWT-based authentication
- BCrypt password encryption
- CORS configuration for frontend integration
- File upload validation (size, type)
- SQL injection prevention via Spring Data JPA
- XSS protection via Spring Security

**Important Security Notes:**
- Never commit `.env` files or sensitive credentials
- Always use environment variables for production
- Change default JWT secret in production
- Restrict CORS origins in production

## 📊 Monitoring & Health Checks

Access health check endpoints:

```bash
# Overall health
http://localhost:8080/actuator/health

# Application info
http://localhost:8080/actuator/info

# Metrics
http://localhost:8080/actuator/metrics
```

## 🚀 Deployment

### Production Configuration

1. **Environment Variables**: Set all required environment variables
2. **Database**: Use production database credentials
3. **JWT Secret**: Generate a strong random secret
4. **CORS**: Update allowed origins to your frontend domain
5. **HTTPS**: Enable SSL/TLS in production
6. **Logging**: Configure appropriate log levels

### Build for Production

```bash
./mvnw clean package -DskipTests
```

The JAR file will be in `target/` directory.

### Run in Production

```bash
java -jar target/freelancer-portal-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

## 📚 Documentation

- [API Endpoints](API_ENDPOINTS.md) - Complete API documentation
- [Application Status](APPLICATION_STATUS.md) - Current application status
- [Profile Picture Integration](PROFILE_PICTURE_FRONTEND_INTEGRATION.md) - Frontend integration guide
- [Deployment Checklist](DEPLOYMENT_CHECKLIST.md) - Pre-deployment checklist

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Mehdi** - Initial work

## 🐛 Issues & Support

For issues and support, please use the GitHub issue tracker.

## 📞 Contact

- GitHub: [@kusoyaji](https://github.com/kusoyaji)
- Repository: [Saas-Freelancer-Client](https://github.com/kusoyaji/Saas-Freelancer-Client)

---

**Happy Coding!** 🎉
