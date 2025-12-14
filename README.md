# 🏸 SportsNet Backend

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

**A comprehensive sports networking platform backend built with Spring Boot**

[Features](#-features) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started) • [API Documentation](#-api-documentation) • [Architecture](#-architecture)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Architecture](#-architecture)
- [Security](#-security)
- [Database Schema](#-database-schema)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**SportsNet** is a full-featured backend system designed to connect sports enthusiasts, manage clubs, organize tournaments, and facilitate social interactions within the sports community. The platform supports badminton clubs, events, tournaments, and provides a comprehensive social networking experience.

### Key Capabilities

- **Club Management**: Create and manage sports clubs with member hierarchies
- **Event Organization**: Plan and manage club events with participant tracking
- **Tournament System**: Full tournament management with brackets, matches, and payments
- **Social Network**: Posts, comments, likes, friendships, and real-time messaging
- **Reputation System**: Track player ratings and club reputation scores
- **Payment Integration**: VNPay payment gateway for tournament fees
- **Real-time Communication**: WebSocket-based messaging and notifications

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based authentication with refresh tokens
- Firebase authentication integration
- OTP email verification
- Role-based access control (RBAC)
- Password reset functionality
- Multi-device session management

### 👥 User Management
- User profiles with detailed information
- Reputation scoring system (default: 100 points)
- Player rating tracking
- User schedules and availability
- Account verification and activation

### 🏢 Club Management
- Create and manage sports clubs
- Club visibility settings (public/private)
- Member management with roles (owner, admin, member)
- Club invitations and join requests
- Member notes and warnings system
- Club event organization
- Club reputation tracking

### 🎪 Event Management
- Create and manage club events
- Participant registration and tracking
- Event cancellation with reasons
- Player ratings for events
- Level-based filtering (min/max skill level)
- Category support (singles, doubles, mixed)
- Fee management
- Absence reason tracking

### 🏆 Tournament System
- Tournament creation and management
- Multiple category support
- Bracket generation and management
- Match scheduling and results
- Participant registration
- Team formation (doubles)
- Partner invitations
- Payment processing via VNPay
- Tournament history tracking

### 📱 Social Features
- Post creation with media support
- Comments and likes
- Friend requests and friendships
- User tagging in posts
- Club-based posts
- Event-related posts

### 💬 Real-time Communication
- WebSocket-based messaging
- One-on-one and group conversations
- Message status tracking (sent, delivered, seen)
- Typing indicators
- Conversation participants management
- Chat roles and permissions

### 🔔 Notifications
- Real-time notification system
- Multiple notification types
- Notification recipients tracking
- Firebase push notifications support

### 🏟️ Facility Management
- Sports facility registration
- Facility details and amenities
- Location-based search
- Facility association with clubs and events

### 💳 Payment Integration
- VNPay payment gateway integration
- Tournament fee processing
- Payment status tracking
- Payment history

### 📁 File Management
- Image upload support
- Avatar management
- Club logo uploads
- Event image uploads
- Facility image uploads
- Post media attachments

---

## 🛠️ Tech Stack

### Core Framework
- **Spring Boot 3.5.4**: Main application framework
- **Java 21**: Programming language
- **Maven**: Build tool and dependency management

### Database & Persistence
- **MySQL 8.0**: Relational database
- **Spring Data JPA**: Data access layer
- **Hibernate**: ORM framework
- **Hibernate Types 60**: Advanced type support

### Security
- **Spring Security**: Authentication and authorization
- **OAuth2 Resource Server**: JWT token handling
- **BCrypt**: Password encryption
- **Firebase Admin SDK**: Firebase authentication

### Communication
- **Spring WebSocket**: Real-time bidirectional communication
- **Spring Mail**: Email service (Gmail SMTP)
- **RESTful APIs**: HTTP-based API architecture

### Utilities
- **Lombok**: Boilerplate code reduction
- **ICU4J**: Internationalization and text processing
- **Jackson**: JSON serialization/deserialization

### Development Tools
- **Maven Compiler Plugin**: Java compilation
- **Spring Boot Maven Plugin**: Application packaging

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git** (for version control)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Optional
- **Firebase Project** (for Firebase authentication)
- **VNPay Merchant Account** (for payment integration)
- **Gmail Account** (for email service)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd sportsnet_backend
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE sports_net CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Application

Update `src/main/resources/application.yaml` with your configuration:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sports_net
    username: your_username
    password: your_password
  
  mail:
    username: your_email@gmail.com
    password: your_app_password

jwt:
  base64-secret: your_base64_secret_key

app:
  file:
    upload-dir: uploads/
    base-url: http://localhost:8080/uploads

vnpay:
  tmnCode: your_vnpay_tmn_code
  hashSecret: your_vnpay_hash_secret
  returnUrl: http://localhost:3000/payment/vnpay-return
```

### 4. Firebase Configuration

Place your Firebase service account key file at:
```
src/main/resources/serviceAccountKey.json
```

### 5. Build the Project

```bash
# Using Maven Wrapper (Windows)
mvnw.cmd clean install

# Using Maven Wrapper (Linux/Mac)
./mvnw clean install

# Or using Maven directly
mvn clean install
```

### 6. Run the Application

```bash
# Using Maven Wrapper
mvnw.cmd spring-boot:run

# Or using Maven
mvn spring-boot:run

# Or run the JAR file
java -jar target/sportsnet_backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 7. Verify Installation

Check if the application is running:

```bash
curl http://localhost:8080/
```

---

## ⚙️ Configuration

### Application Properties

Key configuration sections in `application.yaml`:

#### Server Configuration
```yaml
server:
  port: 8080
  tomcat:
    max-threads: 500
    max-connections: 1000
```

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sports_net
    hikari:
      maximum-pool-size: 50
  jpa:
    hibernate:
      ddl-auto: update
```

#### JWT Configuration
```yaml
jwt:
  base64-secret: your_secret_key
  token-create-validity-in-seconds: 6400
  token-verify-validity-in-seconds: 8640000
```

#### File Upload Configuration
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new account | No |
| POST | `/api/auth/login` | Login with email/password | No |
| POST | `/api/auth/login/firebase` | Login with Firebase | No |
| POST | `/api/auth/verify` | Verify OTP code | No |
| GET | `/api/auth/send-otp/{email}` | Send OTP to email | No |
| GET | `/api/auth/refresh` | Refresh access token | No |
| POST | `/api/auth/logout` | Logout user | Yes |
| POST | `/api/auth/forget/{email}` | Reset password | No |
| PUT | `/api/auth/update-password` | Update password | Yes |

### Club Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/clubs` | Get all clubs | No |
| GET | `/api/clubs/{id}` | Get club by ID | No |
| POST | `/api/clubs` | Create new club | Yes |
| PUT | `/api/clubs/{id}` | Update club | Yes |
| DELETE | `/api/clubs/{id}` | Delete club | Yes |
| GET | `/api/clubs/{id}/members` | Get club members | Yes |
| POST | `/api/clubs/{id}/join` | Join club | Yes |

### Event Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/club-event` | Get all events | No |
| GET | `/api/club-event/{id}` | Get event by ID | No |
| POST | `/api/club-event` | Create event | Yes |
| PUT | `/api/club-event/{id}` | Update event | Yes |
| POST | `/api/club-event/{id}/register` | Register for event | Yes |
| DELETE | `/api/club-event/{id}/cancel` | Cancel event | Yes |

### Tournament Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/tournaments` | Get all tournaments | No |
| GET | `/api/tournaments/{id}` | Get tournament by ID | No |
| POST | `/api/tournaments` | Create tournament | Yes |
| PUT | `/api/tournaments/{id}` | Update tournament | Yes |
| POST | `/api/tournaments/{id}/register` | Register for tournament | Yes |
| GET | `/api/tournaments/{id}/brackets` | Get tournament brackets | Yes |

### Social Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/posts` | Get all posts | Yes |
| POST | `/api/posts` | Create post | Yes |
| GET | `/api/posts/{id}` | Get post by ID | Yes |
| POST | `/api/posts/{id}/like` | Like post | Yes |
| POST | `/api/posts/{id}/comment` | Comment on post | Yes |
| GET | `/api/friends` | Get friends list | Yes |
| POST | `/api/friends/request` | Send friend request | Yes |

### Messaging Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/conversations` | Get conversations | Yes |
| POST | `/api/conversations` | Create conversation | Yes |
| GET | `/api/messages/{conversationId}` | Get messages | Yes |
| POST | `/api/messages` | Send message | Yes |
| WebSocket | `/ws` | WebSocket connection | Yes |

### Payment Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/tournament-payment/create` | Create payment | Yes |
| GET | `/api/tournament-payment/callback` | Payment callback | No |

---

## 🏗️ Architecture

### Project Structure

```
sportsnet_backend/
├── src/
│   ├── main/
│   │   ├── java/com/tlcn/sportsnet_backend/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Enumeration types
│   │   │   ├── error/           # Custom exceptions
│   │   │   ├── payload/         # Request/Response payloads
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.yaml # Application configuration
│   │       └── serviceAccountKey.json # Firebase config
│   └── test/                    # Test files
├── uploads/                      # Uploaded files directory
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

### Design Patterns

- **Layered Architecture**: Controller → Service → Repository
- **DTO Pattern**: Data Transfer Objects for API communication
- **Repository Pattern**: Data access abstraction
- **Builder Pattern**: Entity construction (via Lombok)
- **Strategy Pattern**: Authentication strategies (JWT, Firebase)

### Key Components

#### Controllers
- Handle HTTP requests and responses
- Request validation
- Authentication checks
- Response formatting

#### Services
- Business logic implementation
- Transaction management
- External service integration
- Data transformation

#### Repositories
- Database operations
- Custom query methods
- Entity relationships

#### Entities
- JPA entity definitions
- Database table mapping
- Relationship management
- Audit fields (createdAt, updatedAt)

---

## 🔒 Security

### Authentication Flow

1. **Registration**: User registers → OTP sent → Account verified
2. **Login**: Credentials validated → JWT tokens generated
3. **Token Refresh**: Refresh token used to get new access token
4. **Authorization**: JWT token validated on each request

### Security Features

- **JWT Tokens**: Stateless authentication
- **Refresh Tokens**: Long-lived tokens for session management
- **Password Encryption**: BCrypt hashing
- **CORS Configuration**: Cross-origin resource sharing
- **CSRF Protection**: Disabled for stateless API
- **Role-Based Access**: Fine-grained permissions
- **HttpOnly Cookies**: Secure token storage
- **Firebase Integration**: Third-party authentication

### Security Configuration

Public endpoints (no authentication required):
- `/api/auth/**` (login, register, verify)
- `/api/clubs/*` (public club info)
- `/api/tournaments` (public tournament list)
- `/uploads/**` (static files)

Protected endpoints require valid JWT token in `Authorization` header:
```
Authorization: Bearer <access_token>
```

---

## 🗄️ Database Schema

### Core Entities

- **Account**: User accounts and authentication
- **UserInfo**: Extended user profile information
- **Club**: Sports clubs
- **ClubMember**: Club membership relationships
- **ClubEvent**: Club-organized events
- **Tournament**: Tournament information
- **TournamentParticipant**: Tournament registrations
- **Post**: Social media posts
- **Message**: Chat messages
- **Conversation**: Chat conversations
- **Friendship**: User friendships
- **Notification**: System notifications
- **Facility**: Sports facilities

### Key Relationships

- Account ↔ UserInfo (One-to-One)
- Account ↔ Clubs (One-to-Many, owner)
- Club ↔ ClubMembers (One-to-Many)
- Club ↔ ClubEvents (One-to-Many)
- Tournament ↔ TournamentParticipants (One-to-Many)
- Account ↔ Posts (One-to-Many, author)
- Account ↔ Messages (One-to-Many, sender)

---

## 🚢 Deployment

### Build for Production

```bash
mvn clean package -DskipTests
```

### Environment Variables

Set the following environment variables for production:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/sports_net
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_BASE64_SECRET=your_secret_key
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
```

### Docker Deployment (Optional)

Create a `Dockerfile`:

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/sportsnet_backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t sportsnet-backend .
docker run -p 8080:8080 sportsnet-backend
```

### Production Considerations

- Use environment-specific `application.yaml` files
- Configure proper database connection pooling
- Set up SSL/TLS certificates
- Configure proper CORS origins
- Set up logging and monitoring
- Configure backup strategies
- Use environment variables for sensitive data

---

## 🧪 Testing

Run tests:

```bash
mvn test
```

### Test Coverage

- Unit tests for services
- Integration tests for controllers
- Repository tests
- Security tests

---

## 📝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok annotations where appropriate
- Add Javadoc comments for public methods
- Write unit tests for new features
- Ensure all tests pass before submitting PR

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Authors

**TLCN Team**

- SportsNet Backend Development

---

## 🙏 Acknowledgments

- Spring Boot community
- MySQL team
- Firebase team
- VNPay for payment integration
- All contributors and users

---

## 📞 Support

For support, email support@sportsnet.com or open an issue in the repository.

---

## 🔮 Future Enhancements

- [ ] GraphQL API support
- [ ] Advanced analytics and reporting
- [ ] Mobile app API optimizations
- [ ] WebSocket cluster support
- [ ] Advanced caching strategies
- [ ] Microservices architecture migration
- [ ] API rate limiting
- [ ] Advanced search functionality
- [ ] Multi-language support
- [ ] Video streaming support

---

<div align="center">

**Made with ❤️ by the SportsNet Team**

⭐ Star this repo if you find it helpful!

</div>

