# Client Portal SAAS Application - Folder Structure

```
client-portal/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── freelancer/
│   │   │           └── portal/
│   │   │               ├── ClientPortalApplication.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── JwtConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── UserController.java
│   │   │               │   ├── ClientController.java
│   │   │               │   ├── ProjectController.java
│   │   │               │   ├── FileController.java
│   │   │               │   ├── MessageController.java
│   │   │               │   └── InvoiceController.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── RegisterRequest.java
│   │   │               │   │   ├── UserRequest.java
│   │   │               │   │   ├── ClientRequest.java
│   │   │               │   │   ├── ProjectRequest.java
│   │   │               │   │   ├── FileRequest.java
│   │   │               │   │   ├── MessageRequest.java
│   │   │               │   │   └── InvoiceRequest.java
│   │   │               │   └── response/
│   │   │               │       ├── JwtResponse.java
│   │   │               │       ├── UserResponse.java
│   │   │               │       ├── ClientResponse.java
│   │   │               │       ├── ProjectResponse.java
│   │   │               │       ├── FileResponse.java
│   │   │               │       ├── MessageResponse.java
│   │   │               │       └── InvoiceResponse.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── ResourceNotFoundException.java
│   │   │               │   └── UnauthorizedException.java
│   │   │               ├── model/
│   │   │               │   ├── User.java
│   │   │               │   ├── Client.java
│   │   │               │   ├── Project.java
│   │   │               │   ├── File.java
│   │   │               │   ├── Message.java
│   │   │               │   └── Invoice.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── ClientRepository.java
│   │   │               │   ├── ProjectRepository.java
│   │   │               │   ├── FileRepository.java
│   │   │               │   ├── MessageRepository.java
│   │   │               │   └── InvoiceRepository.java
│   │   │               ├── security/
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── UserDetailsServiceImpl.java
│   │   │               └── service/
│   │   │                   ├── AuthService.java
│   │   │                   ├── UserService.java
│   │   │                   ├── ClientService.java
│   │   │                   ├── ProjectService.java
│   │   │                   ├── FileService.java
│   │   │                   ├── MessageService.java
│   │   │                   └── InvoiceService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data.sql
│   └── test/
│       └── java/
│           └── com/
│               └── freelancer/
│                   └── portal/
│                       └── controller/
│                           └── AuthControllerTest.java
├── pom.xml
├── postman_tests.md
└── README.md
```