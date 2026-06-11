# SkillSwap - Skill Exchange Platform

A modern platform for connecting people to exchange skills, knowledge, and expertise. Built with React frontend and Spring Boot backend.

##  Features

### Core Features
- **User Authentication & Profiles** - Secure JWT-based authentication with comprehensive user profiles
- **Smart Matching Algorithm** - AI-powered matching based on skills, availability, and preferences
- **Real-time Chat** - WebSocket-based messaging system for seamless communication
- **Session Management** - Book, schedule, and manage skill exchange sessions
- **Rating & Reviews** - Rate sessions and build trust within the community
- **Badge System** - Earn badges and recognition for active participation
- **Notification System** - Stay updated with real-time notifications

### Advanced Features
- **Availability Management** - Set your schedule and find matches that work for you
- **Skill Categories** - Organized skill taxonomy for easy discovery
- **Background Checks** - Optional verification for enhanced trust
- **Certification Tracking** - Showcase your professional certifications
- **Email Notifications** - Automated email updates for important events

##  Architecture

### Frontend (React)
- **Location**: `skill-exchange-frontend/`
- **Tech Stack**: React 18, Vite, Tailwind CSS, Zustand
- **Key Features**:
  - Modern responsive UI with Tailwind CSS
  - State management with Zustand
  - Real-time updates with WebSocket
  - Component-based architecture

### Backend (Spring Boot)
- **Location**: `skill-exchange-platform/`
- **Tech Stack**: Spring Boot 3, PostgreSQL, Redis, WebSocket
- **Key Features**:
  - RESTful API design
  - JWT-based authentication
  - Database migrations with Flyway
  - Real-time messaging with WebSocket
  - Caching with Redis
  - Comprehensive test coverage

##  Getting Started

### Prerequisites
- Node.js 18+ (for frontend)
- Java 17+ (for backend)
- PostgreSQL 13+
- Redis (optional, for caching)

### Frontend Setup

```bash
cd skill-exchange-frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`

### Backend Setup

1. **Database Setup**:
   ```sql
   CREATE DATABASE skillexchange;
   ```

2. **Environment Configuration**:
   ```bash
   cd skill-exchange-platform
   cp .env.example .env
   # Update .env with your database credentials
   ```

3. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend API will be available at `http://localhost:8080`

### Docker Setup (Alternative)

```bash
cd skill-exchange-platform
docker-compose up
```

##  Project Structure

```
SkillSwap/
├── skill-exchange-frontend/          # React frontend application
│   ├── src/
│   │   ├── api/                     # API client functions
│   │   ├── components/              # Reusable UI components
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── pages/                   # Page components
│   │   └── store/                   # State management
│   └── package.json
├── skill-exchange-platform/         # Spring Boot backend
│   ├── src/main/java/com/skillexchange/
│   │   ├── auth/                    # Authentication & JWT
│   │   ├── user/                    # User management
│   │   ├── skill/                   # Skill management
│   │   ├── matching/                # Matching algorithm
│   │   ├── session/                 # Session booking
│   │   ├── chat/                    # Real-time messaging
│   │   ├── rating/                  # Rating & badge system
│   │   ├── notification/            # Notification system
│   │   └── config/                  # Configuration classes
│   └── pom.xml
└── README.md
```

##  API Documentation

Once the backend is running, visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

##  Testing

### Frontend Tests
```bash
cd skill-exchange-frontend
npm test
```

### Backend Tests
```bash
cd skill-exchange-platform
./mvnw test
```

##  Deployment

### Production Build

**Frontend**:
```bash
cd skill-exchange-frontend
npm run build
```

**Backend**:
```bash
cd skill-exchange-platform
./mvnw clean package
```

### Docker Deployment
```bash
cd skill-exchange-platform
docker-compose -f docker-compose.prod.yml up
```

##  Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

##  Environment Variables

### Backend (.env)
```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/skillexchange
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_email_password

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
```

##  Security Features

- JWT-based authentication
- Password encryption with BCrypt
- CORS configuration
- Input validation and sanitization
- SQL injection prevention
- XSS protection

##  Database Schema

The application uses PostgreSQL with the following main entities:
- Users & User Profiles
- Skills & User Skills
- Availability Schedules
- Match Requests & Sessions
- Messages & Conversations
- Ratings & Badges
- Notifications

Database migrations are managed with Flyway and located in `src/main/resources/db/migration/`.

##  Roadmap

- [ ] Mobile app development
- [ ] Video call integration
- [ ] Payment system for premium features
- [ ] Advanced analytics dashboard
- [ ] Machine learning improvements for matching
- [ ] Multi-language support

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

Developed by [Gnana Guru](https://github.com/gnanaguru83)

## 🙏 Acknowledgments

- Spring Boot community
- React ecosystem contributors
- Open source libraries used in this project
