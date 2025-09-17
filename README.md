# Parking Lot Management System

A comprehensive Spring Boot application for managing parking lots with multi-floor support, gate-aware slot allocation, and OAuth2 authentication.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Running the Application
1. **Optional Environment Variables** (defaults provided in `application.yml`):
   ```bash
   export GOOGLE_CLIENT_ID=your_google_client_id
   export GOOGLE_CLIENT_SECRET=your_google_client_secret
   export ADMIN_EMAILS=admin1@example.com,admin2@example.com
   ```

2. **Start the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the application**:
   - Application: http://localhost:8080
   - H2 Database Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:parkingdb`
     - Username: `sa`
     - Password: (leave empty)

## 🔐 Authentication & Authorization

The system uses **Google OAuth2** for authentication:
- All users get `ROLE_USER` by default
- Users with emails listed in `ADMIN_EMAILS` also get `ROLE_ADMIN`
- All API endpoints under `/api/**` require authentication
- Admin endpoints under `/api/admin/**` require `ROLE_ADMIN`

## 📋 API Documentation

### User APIs (Requires `ROLE_USER` or `ROLE_ADMIN`)

#### 1. Vehicle Entry
```http
POST /api/tickets/enter
Content-Type: application/json

{
  "plateNo": "KA01AB1234",
  "vehicleType": "CAR",
  "ownerEmail": "user@example.com",
  "parkingLotId": 1,
  "gateId": 1,
  "gateFloor": 0
}
```

**Response:**
```json
{
  "ticketId": 1,
  "slotId": 5,
  "floor": 0,
  "number": 1,
  "plateNo": "KA01AB1234",
  "entryTime": "2024-01-15T10:30:00"
}
```

#### 2. Amount Preview
```http
GET /api/tickets/amount/{ticketId}
```

**Response:**
```json
25.50
```

#### 3. Pay and Exit
```http
POST /api/tickets/exit
Content-Type: application/json

{
  "ticketId": 1
}
```

**Response:**
```json
{
  "ticketId": 1,
  "amount": 25.50,
  "status": "SUCCESS"
}
```

#### 4. Receipt Details
```http
GET /api/tickets/receipt/{ticketId}
```

**Response:**
```json
{
  "ticketId": 1,
  "plateNo": "KA01AB1234",
  "entryTime": "2024-01-15T10:30:00",
  "exitTime": "2024-01-15T12:30:00",
  "amount": 25.50
}
```

### Admin APIs (Requires `ROLE_ADMIN`)

#### 1. Add Parking Slot
```http
POST /api/admin/slots?parkingLotId=1&floor=0&number=1&type=CAR
```

**Response:**
```json
{
  "id": 1,
  "floor": 0,
  "number": 1,
  "type": "CAR",
  "status": "AVAILABLE",
  "parkingLot": {
    "id": 1,
    "location": "Main Parking Lot"
  }
}
```

#### 2. List All Slots
```http
GET /api/admin/slots
```

#### 3. Delete Slot
```http
DELETE /api/admin/slots/{id}
```

#### 4. Create/Update Pricing Rule
```http
POST /api/admin/pricing?type=CAR&freeMinutes=120&ratePerHour=20
```

**Response:**
```json
{
  "id": 1,
  "type": "CAR",
  "freeMinutes": 120,
  "ratePerHour": 20.00
}
```

#### 5. List Pricing Rules
```http
GET /api/admin/pricing
```

## 🏗️ System Architecture

### Key Features
- **Multi-floor Parking Lots**: Support for multiple floors with different slot types
- **Gate-aware Allocation**: Nearest slot allocation based on entry gate location
- **Concurrent Safety**: Pessimistic locking prevents double allocation
- **Flexible Pricing**: Configurable pricing rules per vehicle type
- **Atomic Payments**: Transactional payment processing with rollback on failure
- **Duplicate Prevention**: Prevents multiple active tickets for the same vehicle

### Technology Stack
- **Framework**: Spring Boot 3.5.5
- **Database**: H2 (in-memory)
- **Security**: Spring Security with OAuth2
- **Build Tool**: Maven
- **Java Version**: 17

### Core Components
- **Entities**: `ParkingLot`, `Gate`, `ParkingSlot`, `Vehicle`, `Ticket`, `Payment`, `PricingRule`
- **Services**: `TicketService`, `PaymentService`, `PricingService`, `AllocationService`
- **Strategies**: `NearestAllocationStrategy`, `LevelWiseAllocationStrategy`
- **Security**: OAuth2 with Google provider, role-based access control

## 🔧 Configuration

### Allocation Strategy
The system supports configurable slot allocation strategies:
- **Default**: `nearestStrategy` - Allocates nearest slot by floor distance
- **Alternative**: `levelWiseStrategy` - Allocates by floor then slot number

To change strategy, set environment variable:
```bash
export ALLOCATION_STRATEGY=levelWiseStrategy
```

### Database Configuration
- **Type**: H2 in-memory database
- **Schema**: Auto-created on startup
- **Data**: Pre-seeded with sample data via `data.sql`

## 🧪 Testing

### Postman Collection
Import the provided Postman collection:
- File: `postman/parking-lot.postman_collection.json`
- Environment variables: `baseUrl` (http://localhost:8080), `ticketId`

### Sample Test Flow
1. Authenticate with Google OAuth2
2. Add parking slots (Admin)
3. Set pricing rules (Admin)
4. Enter vehicle (User)
5. Check amount (User)
6. Pay and exit (User)
7. View receipt (User)

## 📊 Database Schema

### Key Tables
- **parking_lot**: Parking lot information
- **gate**: Entry/exit gates with floor information
- **parking_slot**: Individual parking slots with type and status
- **vehicle**: Vehicle information with unique plate numbers
- **ticket**: Parking tickets with entry/exit times
- **payment**: Payment records with status tracking
- **pricing_rule**: Configurable pricing per vehicle type

### Concurrency Control
- **Optimistic Locking**: Used on `ParkingSlot` and `Ticket` entities
- **Pessimistic Locking**: Used during slot allocation to prevent double booking
- **Transactional Safety**: Payment operations are fully transactional

## 🚨 Error Handling

The system includes comprehensive error handling:
- **400 Bad Request**: Invalid input data
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate entry attempts
- **500 Internal Server Error**: System errors

## 🔄 Business Logic

### Slot Allocation
1. Find nearest available slot based on gate floor
2. Use pessimistic locking to prevent concurrent allocation
3. Atomically claim slot if still available
4. Create ticket with allocated slot

### Payment Processing
1. Calculate amount based on duration and pricing rules
2. Initiate payment (simulated gateway)
3. On success: mark ticket as paid, set exit time, free slot
4. On failure: rollback transaction, keep slot occupied

### Duplicate Prevention
- Check for active tickets before creating new ones
- Prevent multiple entries for the same vehicle plate
- Database constraints ensure data integrity
