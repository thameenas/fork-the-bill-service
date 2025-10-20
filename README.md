# Fork the Bill Service

This is the backend service for the Fork the Bill application, which allows users to split restaurant bills with itemized expenses.

## API Documentation

### Create Expense

Creates a new expense (bill) with items and optional people.

**Endpoint:** `POST /expense`

**Request Body:**
```json
{
  "payerName": "John Doe",
  "totalAmount": 100.00,
  "subtotal": 80.00,
  "tax": 10.00,
  "serviceCharge": 10.00,
  "items": [
    {
      "name": "Burger",
      "price": 15.00
    },
    {
      "name": "Fries",
      "price": 5.00
    }
  ],
  "people": [
    {
      "name": "Alice"
    },
    {
      "name": "Bob"
    }
  ]
}
```

**Response:** (HTTP 201 Created)
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "slug": "brave-blue-tiger",
  "createdAt": "2023-01-01T12:00:00Z",
  "payerName": "John Doe",
  "totalAmount": 100.00,
  "subtotal": 80.00,
  "tax": 10.00,
  "serviceCharge": 10.00,
  "items": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174001",
      "name": "Burger",
      "price": 15.00,
      "claimedBy": []
    },
    {
      "id": "123e4567-e89b-12d3-a456-426614174002",
      "name": "Fries",
      "price": 5.00,
      "claimedBy": []
    }
  ],
  "people": [
    {
      "name": "Alice",
      "itemsClaimed": [],
      "amountOwed": 0.00,
      "subtotal": 0.00,
      "taxShare": 0.00,
      "serviceChargeShare": 0.00,
      "totalOwed": 0.00,
      "isFinished": false
    },
    {
      "name": "Bob",
      "itemsClaimed": [],
      "amountOwed": 0.00,
      "subtotal": 0.00,
      "taxShare": 0.00,
      "serviceChargeShare": 0.00,
      "totalOwed": 0.00,
      "isFinished": false
    }
  ]
}
```

**Validation:**
- `payerName` is required
- `totalAmount`, `subtotal`, `tax`, and `serviceCharge` are required and must be positive
- `totalAmount` must equal `subtotal + tax + serviceCharge`
- At least one item is required

## Implementation Details

The expense creation endpoint is implemented with the following components:

1. **Models**: Entity classes for Expense, Item, and Person with JPA annotations for database mapping
2. **DTOs**: Request and response DTOs for the API
3. **Repository**: JPA repository for data access
4. **Service**: Business logic for creating expenses and validating requests
5. **Controller**: REST controller for handling HTTP requests
6. **Exception Handling**: Global exception handler for API errors
7. **Validation**: Bean validation for request payloads and custom validation for total amount

## Running the Application

```bash
./gradlew bootRun
```

## Running Tests

```bash
./gradlew test