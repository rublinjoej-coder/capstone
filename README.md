# RublinMart - Multi-Seller E-Commerce Platform

**RublinMart** is a full-stack, multi-seller e-commerce web application built using **Java 25**, **Java Servlets**, **JDBC**, **H2 Database**, **HikariCP**, **jBCrypt**, **Gson**, **Apache Tomcat 9**, and a modern **HTML5 / CSS3 / Vanilla JavaScript** frontend.

---

## 🌟 Key Features

### 1. User Roles & Security
- **Buyer**: Browse catalog, search, filter by category, view product details, add/remove items from cart, adjust quantities, transactional checkout, view order history, track order status, and post product reviews & ratings.
- **Seller**: Dedicated Seller Dashboard to create, update, and delete own product listings, inspect incoming orders containing their items, and update fulfillment status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED).
- **Admin**: Dedicated Admin Panel to view all registered users (buyers and sellers), moderate/delete inappropriate product listings across the platform, and monitor system-wide orders.
- **Security**:
  - Passwords hashed using **jBCrypt**.
  - **HttpSession** authentication with session ID regeneration upon login (`request.changeSessionId()`).
  - Session inactivity timeout set to 30 minutes.
  - Role-based access control enforced via `AuthFilter`.
  - Full SQL injection prevention via `PreparedStatement` on all queries.
  - XSS output escaping and zero stack trace exposure to clients.

### 2. Shopping Cart & Transactional Checkout
- Database-backed cart (`cart_items`).
- Quantity capping based on live product inventory stock.
- Server-side price calculation and grand total verification (client totals are never trusted).
- Atomic database transactions (`connection.setAutoCommit(false)`): placing an order creates `orders`, inserts `order_items`, decrements product stock, and clears cart items in a single commit.

### 3. Product Reviews & Ratings
- Verified buyers who purchased a product can rate (1 to 5 stars) and write comments.
- Dynamic calculation of average product rating and review count.

### 4. AI Chatbot
- Floating AI assistant widget.
- REST endpoint `POST /api/v1/chat` with configurable provider (`MockChatProvider` or `GeminiChatProvider`).
- Rate limiting (1 request per 2 seconds per IP) and input validation (max 500 characters).

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript, Fetch API (AJAX), FontAwesome Icons |
| **Backend** | Java 25, Java Servlets (`javax.servlet-api:4.0.1`), JDBC |
| **Database** | H2 Database (`jdbc:h2:file:./data/rublinmartdb;DB_CLOSE_DELAY=-1`), HikariCP Connection Pool |
| **Security & Utilities** | jBCrypt (Password Hashing), Gson (JSON Serialization), SLF4J + Logback |
| **Testing** | JUnit 5, Mockito |
| **Build & Container** | Maven, Apache Tomcat 9 WAR |

---

## 📁 Project Architecture & Folder Structure

```
rublinmart/
├── pom.xml
├── README.md
├── .gitignore
├── .github/
│   └── workflows/
│       └── build.yml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/rublin/rublinmart/
    │   │       ├── controller/        # AuthServlet, ProductServlet, CartServlet, OrderServlet, etc.
    │   │       ├── service/           # AuthService, ProductService, CartService, OrderService, ChatService
    │   │       ├── dao/               # DBConnection, UserDAO, ProductDAO, CartDAO, OrderDAO, ReviewDAO
    │   │       ├── model/             # User, Product, Order, OrderItem, CartItem, Review
    │   │       ├── dto/               # UserResponseDTO, ProductResponseDTO, OrderResponseDTO
    │   │       ├── filter/            # AuthFilter, EncodingFilter, LoggingFilter
    │   │       ├── listener/          # DatabaseContextListener
    │   │       ├── util/              # PasswordUtil, ValidationUtil, JsonUtil
    │   │       └── exception/         # AppException
    │   ├── resources/
    │   │   ├── application.properties
    │   │   ├── logback.xml
    │   │   └── db/
    │   │       ├── schema.sql
    │   │       └── seed.sql
    │   └── webapp/
    │       ├── WEB-INF/
    │       │   └── web.xml
    │       ├── css/
    │       ├── js/
    │       └── pages/
    └── test/
        └── java/                      # JUnit 5 & Mockito test suites
```

---

## 📊 System Diagrams & Descriptions

### 1. ER Diagram Description
```
+------------------+       1:N       +------------------+
|      users       |---------------->|     products     |
|------------------|                 |------------------|
| id (PK)          |                 | id (PK)          |
| name             |                 | seller_id (FK)   |
| email (UNIQUE)   |                 | name, price      |
| password_hash    |                 | stock_qty        |
| role             |                 | category, image  |
+------------------+                 +------------------+
       |   |                                  |   |
       |   +--------------+                   |   |
    1:N|                  |1:N             1:N|   |1:N
       v                  v                   v   v
+------------------+ +------------------+ +------------------+ +------------------+
|      orders      | |    cart_items    | |   order_items    | |     reviews      |
|------------------| |------------------| |------------------| |------------------|
| id (PK)          | | id (PK)          | | id (PK)          | | id (PK)          |
| buyer_id (FK)    | | user_id (FK)     | | order_id (FK)    | | product_id (FK)  |
| status           | | product_id (FK)  | | product_id (FK)  | | user_id (FK)     |
| total_amount     | | quantity         | | quantity, price  | | rating, comment  |
+------------------+ +------------------+ +------------------+ +------------------+
```

### 2. Sequence Diagram (Order Placement Flow)
```
Browser              Servlet               Service                DAO                Database
   |                    |                     |                    |                    |
   |--- POST /orders -->|                     |                    |                    |
   |                    |-- placeOrder() ---->|                    |                    |
   |                    |                     |-- getCartItems() ->|                    |
   |                    |                     |                    |--- SELECT cart --->|
   |                    |                     |<-- List<CartItem>--|                    |
   |                    |                     |                                         |
   |                    |                     |-- createOrderTransactional() ---------->|
   |                    |                     |    1. conn.setAutoCommit(false)         |
   |                    |                     |    2. INSERT INTO orders                |
   |                    |                     |    3. reduceStock() & INSERT items      |
   |                    |                     |    4. DELETE FROM cart_items            |
   |                    |                     |    5. conn.commit()                     |
   |                    |                     |<-- Order Object ------------------------|
   |                    |<-- OrderResponseDTO-|                                         |
   |<-- 201 Created JSON|                     |                                         |
```

---

## 🔑 Default Seed Accounts

> [!WARNING]
> Change default passwords before deploying to a production environment!

- **Admin Account**: `admin@rublinmart.com` / `Password@123`
- **Seller 1 Account**: `seller1@rublinmart.com` / `Password@123`
- **Seller 2 Account**: `seller2@rublinmart.com` / `Password@123`
- **Buyer 1 Account**: `buyer1@rublinmart.com` / `Password@123`
- **Buyer 2 Account**: `buyer2@rublinmart.com` / `Password@123`

---

## 🌐 API Endpoints Specification

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register Buyer or Seller | Public |
| `POST` | `/api/v1/auth/login` | User login & session creation | Public |
| `POST` | `/api/v1/auth/logout` | Terminate session | Authenticated |
| `GET` | `/api/v1/auth/me` | Fetch current session user | Authenticated |
| `GET` | `/api/v1/products` | Browse & search products | Public |
| `GET` | `/api/v1/products/{id}` | View product details | Public |
| `POST` | `/api/v1/products` | Add new product | Seller |
| `PUT` | `/api/v1/products/{id}` | Edit owned product | Seller |
| `DELETE` | `/api/v1/products/{id}` | Delete product | Seller (own) / Admin |
| `GET` | `/api/v1/cart` | View cart summary | Buyer |
| `POST` | `/api/v1/cart/add` | Add product to cart | Buyer |
| `POST` | `/api/v1/cart/update` | Update item quantity | Buyer |
| `POST` | `/api/v1/cart/remove` | Remove item from cart | Buyer |
| `DELETE` | `/api/v1/cart` | Clear entire cart | Buyer |
| `POST` | `/api/v1/orders` | Place order (Checkout) | Buyer |
| `GET` | `/api/v1/orders` | View buyer order history | Buyer |
| `GET` | `/api/v1/reviews?productId={id}` | Fetch product reviews | Public |
| `POST` | `/api/v1/reviews` | Submit product review | Buyer (purchased) |
| `GET` | `/api/v1/seller/products` | View seller listings | Seller |
| `GET` | `/api/v1/seller/orders` | View incoming orders | Seller |
| `POST` | `/api/v1/seller/orders/{id}/status` | Update fulfillment status | Seller |
| `GET` | `/api/v1/admin/users` | View all platform users | Admin |
| `GET` | `/api/v1/admin/orders` | View all system orders | Admin |
| `POST` | `/api/v1/chat` | AI Chatbot interaction | Public |
| `GET` | `/api/v1/health` | System health check | Public |

---

## 🚀 Installation & Setup Instructions

### 1. Prerequisites
- **JDK 25** installed and added to `JAVA_HOME`.
- **Apache Maven** installed.
- **Apache Tomcat 9** installed.
- **VS Code** with *Extension Pack for Java* and *Community Server Connectors* (or Tomcat extension).

### 2. How to Build & Run Tests
Open terminal in project root directory:
```bash
mvn clean test
```

### 3. Packaging WAR for Tomcat
Package the web application into a `.war` file:
```bash
mvn clean package
```
This produces `target/rublinmart.war`.

### 4. Deploying to Apache Tomcat 9
1. Copy `target/rublinmart.war` to Tomcat's `webapps/` folder.
2. Start Apache Tomcat using `bin/startup.bat` (Windows) or `bin/startup.sh` (Linux/macOS).
3. Access the web application at:
   `http://localhost:8080/rublinmart/pages/index.html`
