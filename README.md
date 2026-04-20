# FreshMart Grocery Management System

A full-stack grocery management system built with Java, JavaFX, Spring Boot, and SQLite.

## Tech Stack
- **Frontend:** Java 17 + JavaFX 21
- **Backend:** Spring Boot 3.2
- **Database:** SQLite

## Default Accounts

| Role     | Email                    | Password  |
|----------|--------------------------|-----------|
| Admin    | admin@freshmart.com      | admin123  |
| Customer | john.hughes@email.com    | john123   |
| Customer | mary.brumly@email.com    | mary123   |
| Customer | catie.diane@email.com    | catie123  |
| Customer | kelsi.texas@email.com    | kelsi123  |
| Customer | lela.moon@email.com      | lela123   |

## How to Run

### Requirements
- Java 17+
- Maven 3.8+

### Step 1 — Start the Backend
```bash
cd backend
mvn spring-boot:run
```
The backend starts on `http://localhost:8080`

### Step 2 — Start the Frontend
```bash
cd frontend
mvn javafx:run
```

## Features

### Admin/Seller
- Dashboard with revenue, orders, product and customer counts
- Full CRUD on products (name, category, price, stock, description)
- Manage customer accounts (enable/disable/delete)
- View all orders with customer details and totals

### Customer/Buyer
- Browse all products with prices and stock levels
- Search and filter by category
- Buy products (balance checked before purchase)
- Insufficient balance notification
- Top up wallet balance
- View personal order history
