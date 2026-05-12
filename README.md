# FreshMart Grocery Management System

A full-stack grocery management desktop application built with Java, JavaFX, Spring Boot, and SQLite, featuring AI-powered grocery list generation via the Google Gemini API.

## Team

| Name | Role |
|------|------|
| Alex Zirilli | Backend Development |
| Jake Dunn | Backend Development |
| Emir Usta | Frontend Development |
| Jomar Lubin | Frontend Development |
| Jonathan Salvador | AI Integration & Full Stack |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Java 21 + JavaFX 21 |
| Backend | Spring Boot 3.2 |
| Database | SQLite |
| AI | Google Gemini API (gemini-2.5-flash) |
| Build | Maven |

## Default Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@freshmart.com | admin123 |
| Customer | john.hughes@email.com | john123 |
| Customer | mary.brumly@email.com | mary123 |
| Customer | catie.diane@email.com | catie123 |
| Customer | kelsi.texas@email.com | kelsi123 |
| Customer | lela.moon@email.com | lela123 |

## Prerequisites

- Java 21+
- Maven 3.8+
- A Google Gemini API key (free at [aistudio.google.com](https://aistudio.google.com))

## Setup

### 1. Configure application.properties

Create `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlite:grocery.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.id.new_generator_mappings=true
server.port=8080
spring.security.user.name=admin
spring.security.user.password=admin
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

### 2. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`

### 3. Start the Frontend

Open the `frontend` folder as a Maven project in IntelliJ and run `MainApp.java`, or:

```bash
cd frontend
mvn javafx:run
```

## Features

### Customer
- Browse products in a visual card grid with images
- Search and filter by category
- Add items to cart with quantity management
- Checkout with balance validation
- View order history with receipt popup (double-click an order)
- Top up wallet balance
- **AI Grocery List** — describe a meal and get an AI-generated shopping list matched to store inventory
  - Dietary filters: Vegan, Vegetarian, Gluten-Free, Dairy-Free, Halal, Kosher
  - Budget constraint: set a max spend and the list stays within it
  - Add AI-suggested items directly to cart

### Admin
- Dashboard with revenue, order count, product count, customer count
- Full product management: add, edit, delete, upload product images
- Customer account management: enable, disable, delete accounts
- View all orders with receipt popup (double-click an order)

### Authentication
- Email/password registration with confirmation
- Password requirements enforced (8+ chars, uppercase, number, special character)
- Show/hide password toggle on login and register
- Email format validation
- Case-insensitive email login

## Project Structure

```
FreshMart/
├── backend/
│   ├── src/main/java/com/grocery/
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic (including AIService)
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Spring Data repositories
│   │   ├── dto/            # Data transfer objects
│   │   └── config/         # Security, DataSeeder
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   ├── src/main/java/com/grocery/ui/
│   │   ├── controllers/    # JavaFX controllers
│   │   ├── services/       # ApiService (HTTP client)
│   │   └── util/           # SessionManager
│   └── src/main/resources/
│       ├── fxml/           # UI layouts
│       ├── css/            # Stylesheet
│       └── com/grocery/ui/images/  # Product images
└── README.md
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Login |
| POST | /api/auth/register | Register |
| GET | /api/products | Get all products |
| POST | /api/products | Create product |
| PUT | /api/products/{id} | Update product |
| DELETE | /api/products/{id} | Delete product |
| POST | /api/orders/purchase | Place order |
| GET | /api/orders/user/{id} | Get user orders |
| GET | /api/orders | Get all orders (admin) |
| POST | /api/ai/grocery-list | Generate AI grocery list |
| GET | /api/users/customers | Get all customers |
| POST | /api/users/topup | Top up balance |

SRS Documentation
https://1drv.ms/w/c/15ed0e2948a7d115/IQDbI2BbKew0TrvwOvt_oBo1AWSDS0fMNMny7NKKclduXXM?e=Vsn4B3 
