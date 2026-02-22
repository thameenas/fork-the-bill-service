# Project Context

## Overview
The Fork The Bill Service is a Spring Boot backend application designed to facilitate itemized bill splitting, primarily for restaurant expenses. It allows users to create expenses manually or by uploading bill images for AI-driven parsing, manage items, assign them to people, and track individual contributions and payment status. The service uses Java 21, Spring Boot, PostgreSQL for persistence, and integrates with Google Gemini AI for image processing. The application is also designed for containerized deployment using Docker.

## Architecture
The service follows a traditional Model-View-Controller (MVC) pattern, typical for Spring Boot applications, with clear separation of concerns between `controllers` (handling HTTP requests), `services` (encapsulating business logic and calculations), and `repositories` (managing data persistence via JPA). Data models are separated into `entities` for persistence and `DTOs` for API communication. An AI integration (`GeminiService`) acts as a distinct module for external bill parsing, indicating a clear service boundary. Exception handling is centralized through a `GlobalExceptionHandler`. The system design emphasizes anonymous, stateless interactions accessed via human-friendly slugs. The application is packaged into a Docker image using a multi-stage build process, allowing for containerized deployment. For local development, Docker Compose is used to orchestrate both the PostgreSQL database and the application container, simplifying environment setup.

## Core Workflows
1.  **Expense Creation (Manual)**:
    *   A client sends a `POST` request to `/expense` with `ExpenseRequest` payload (payer name, amounts, items, optional people).
    *   `ExpenseController` receives the request and delegates to `ExpenseService`.
    *   `ExpenseService` performs bean validation, custom business validation (e.g., `totalAmount = subtotal + tax + serviceCharge`), generates a unique human-friendly slug using `SlugGenerator`, maps DTOs to entities, calculates initial shares if people are provided, and persists the `Expense` (with associated `Items` and `People`) via `ExpenseRepository`.
    *   A `201 Created` response containing the `ExpenseResponse` (including generated `id`s and `slug`) is returned.

2.  **Expense Creation (Image-based)**:
    *   A client sends a `POST` request to `/expense/upload` with a bill image (binary) and `payerName`.
    *   `ExpenseController` delegates to `ExpenseService`.
    *   `ExpenseService` passes the image and payer name to `GeminiService`.
    *   `GeminiService` interacts with the Google Gemini AI to parse the bill image, extracting items, prices, and total amounts.
    *   The parsed `BillParsedData` is returned to `ExpenseService`.
    *   `ExpenseService` then proceeds with validation, slug generation, entity mapping, and persistence as in the manual creation flow.
    *   A `201 Created` response with `ExpenseResponse` is returned.

3.  **Expense Retrieval**:
    *   A client sends a `GET` request to `/expense/{slug}`.
    *   `ExpenseController` delegates to `ExpenseService`.
    *   `ExpenseService` retrieves the `Expense` entity by `slug` from `ExpenseRepository`.
    *   If not found, a `ResourceNotFoundException` is thrown.
    *   The entity is mapped to an `ExpenseResponse` and returned.

4.  **Item Claiming/Unclaiming**:
    *   To claim: `POST /expense/{slug}/items/{itemId}/claim` with `ClaimItemRequest` (containing `personId`).
    *   To unclaim: `DELETE /expense/{slug}/items/{itemId}/claim/{personId}`.
    *   `ExpenseController` delegates to `ExpenseService`.
    *   `ExpenseService` retrieves the `Expense`, `Item`, and `Person` by their respective IDs/slug.
    *   It updates the `claimedBy` list on the `Item` and `itemsClaimed` list on the `Person`.
    *   Following the update, `ExpenseService` recalculates each `Person`'s `subtotal`, `taxShare`, `serviceChargeShare`, and `totalOwed` based on their claimed items. Tax and service charge are split proportionally by subtotal percentage.
    *   The updated `ExpenseResponse` is returned.

5.  **Person Status Management**:
    *   To mark finished: `PUT /expense/{slug}/people/{personId}/finish`.
    *   To mark pending: `PUT /expense/{slug}/people/{personId}/pending`.
    *   `ExpenseController` delegates to `ExpenseService`.
    *   `ExpenseService` retrieves the `Expense` and `Person`.
    *   It updates the `isFinished` boolean attribute for the specified `Person` and persists the change.
    *   A `200 OK` status is returned.

6.  **Add Person to Expense**:
    *   `POST /expense/{slug}/people` with `PersonRequest` (name).
    *   `ExpenseController` delegates to `ExpenseService`.
    *   `ExpenseService` retrieves the `Expense`, creates a new `Person` entity, assigns a new UUID, and associates it with the expense.
    *   Initial calculations for the new person will be 0, as no items are claimed.
    *   The updated `ExpenseResponse` is returned.

## Data Models & State
The core entities are `Expense`, `Item`, and `Person`, managed via JPA. UUIDs are used for unique identifiers, and slugs provide human-readable, shareable links for `Expense` objects.

*   **Expense** (Entity & DTO: `ExpenseRequest`/`ExpenseResponse`)
    *   `id`: `UUID` (primary key)
    *   `slug`: `String` (unique, human-readable identifier, e.g., "brave-blue-tiger")
    *   `createdAt`: `OffsetDateTime`
    *   `payerName`: `String` (required)
    *   `totalAmount`: `BigDecimal` (required, calculated as `subtotal + tax + serviceCharge`)
    *   `subtotal`: `BigDecimal` (required)
    *   `tax`: `BigDecimal` (required)
    *   `serviceCharge`: `BigDecimal` (required, referred to as "tip" in some high-level context, but `apidoc.yaml` and DTOs use `serviceCharge`)
    *   `items`: `List<Item>` (One-to-Many relationship with `Item` entity)
    *   `people`: `List<Person>` (One-to-Many relationship with `Person` entity)

*   **Item** (Entity & DTO: `ItemRequest`/`ItemResponse`)
    *   `id`: `UUID` (primary key)
    *   `name`: `String` (required)
    *   `price`: `BigDecimal` (required)
    *   `claimedBy`: `Set<Person>` (Many-to-Many relationship with `Person` entity, represents which persons have claimed this item). Stored as a `List<UUID>` in DTOs.

*   **Person** (Entity & DTO: `PersonRequest`/`PersonResponse`)
    *   `id`: `UUID` (primary key)
    *   `name`: `String` (required)
    *   `itemsClaimed`: `Set<Item>` (Many-to-Many relationship with `Item` entity, represents items claimed by this person). Stored as a `List<UUID>` in DTOs.
    *   `subtotal`: `BigDecimal` (calculated sum of prices of `itemsClaimed`)
    *   `taxShare`: `BigDecimal` (calculated share of the expense's `tax`, proportional to `subtotal`)
    *   `serviceChargeShare`: `BigDecimal` (calculated share of the expense's `serviceCharge`, proportional to `subtotal`)
    *   `totalOwed`: `BigDecimal` (calculated as `subtotal + taxShare + serviceChargeShare`)
    *   `isFinished`: `boolean` (state: `false` (pending) or `true` (finished/paid))

*   **BillParsedData** (DTO): Internal DTO for AI service response. Contains parsed `payerName`, `totalAmount`, `subtotal`, `tax`, `serviceCharge`, and a list of `ItemRequest` objects.

## API & Interfaces
The service exposes a RESTful API as defined in `apidoc.yaml`. All endpoints currently lack authentication. The server defaults to running on port `8080`, configurable via the `PORT` environment variable.

*   **`POST /expense`**: Creates a new expense.
    *   Request: `ExpenseRequest` (JSON)
    *   Response: `ExpenseResponse` (201 Created)
*   **`POST /expense/upload`**: Creates an expense by parsing an uploaded bill image.
    *   Request: `multipart/form-data` with `bill` (binary image file) and `payerName` (string).
    *   Response: `ExpenseResponse` (201 Created)
*   **`GET /expense/{slug}`**: Retrieves an expense by its unique slug.
    *   Path parameter: `slug` (string)
    *   Response: `ExpenseResponse` (200 OK) or `ApiError` (404 Not Found)
*   **`PUT /expense/{slug}`**: Updates an existing expense by its slug. Replaces all fields with new values.
    *   Path parameter: `slug` (string)
    *   Request: `ExpenseRequest` (JSON)
    *   Response: `ExpenseResponse` (200 OK)
*   **`POST /expense/{slug}/items/{itemId}/claim`**: Claims a specific item for a person.
    *   Path parameters: `slug` (string), `itemId` (string/UUID)
    *   Request: `ClaimItemRequest` (JSON) containing `personId` (string/UUID)
    *   Response: `ExpenseResponse` (200 OK)
*   **`DELETE /expense/{slug}/items/{itemId}/claim/{personId}`**: Unclaims an item for a person.
    *   Path parameters: `slug` (string), `itemId` (string/UUID), `personId` (string/UUID)
    *   Response: `ExpenseResponse` (200 OK)
*   **`PUT /expense/{slug}/people/{personId}/finish`**: Marks a person as finished with their payment.
    *   Path parameters: `slug` (string), `personId` (string/UUID)
    *   Response: 200 OK
*   **`PUT /expense/{slug}/people/{personId}/pending`**: Marks a person as pending payment.
    *   Path parameters: `slug` (string), `personId` (string/UUID)
    *   Response: 200 OK
*   **`POST /expense/{slug}/people`**: Adds a new person to an existing expense.
    *   Path parameter: `slug` (string)
    *   Request: `PersonRequest` (JSON)
    *   Response: `ExpenseResponse` (200 OK)

Error responses uniformly use the `ApiError` schema:
*   `timestamp`: `string` (date-time)
*   `status`: `integer` (HTTP status code)
*   `error`: `string` (error type, e.g., "Bad Request")
*   `message`: `string` (detailed error description)
*   `path`: `string` (API path)

## Key Components
*   `com.forkthebill.service.ForkTheBillServiceApplication`: The main Spring Boot application entry point.
*   `com.forkthebill.service.config.SecurityConfig`: Spring Security configuration, currently configured without explicit authentication for API endpoints, but the component is present for potential future security features.
*   `com.forkthebill.service.controllers.ExpenseController`: Handles incoming HTTP requests, maps them to service methods, and constructs HTTP responses.
*   `com.forkthebill.service.exceptions.*`: Defines custom exception types (`ResourceNotFoundException`, `ValidationException`) and a `GlobalExceptionHandler` to centralize error responses and map them to `ApiError` DTOs.
*   `com.forkthebill.service.models.dto.*`: Contains Data Transfer Objects for request and response payloads, clearly defining API contracts.
*   `com.forkthebill.service.models.entities.*`: Defines JPA entities (`Expense`, `Item`, `Person`) for object-relational mapping to the database.
*   `com.forkthebill.service.repositories.ExpenseRepository`: Spring Data JPA repository interface for `Expense` entity, providing basic CRUD operations and query methods.
*   `com.forkthebill.service.services.ExpenseService`: Contains the core business logic for creating, retrieving, updating expenses, managing item claims, person status, and performing financial calculations. It orchestrates interactions with repositories and the AI service.
*   `com.forkthebill.service.services.GeminiService`: Dedicated service for integrating with the Google Gemini AI for parsing bill images and extracting structured data.
*   `com.forkthebill.service.utils.SlugGenerator`: Utility class responsible for generating unique, human-readable slugs (e.g., "brave-blue-tiger") for `Expense` objects from a predefined list of words (`words.txt`).

## Dependencies & Environment
*   **Core Framework**: Spring Boot 3.x (with `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-actuator`).
*   **Database**: PostgreSQL (`org.postgresql:postgresql`) for production/development. H2 Database (`com.h2database:h2`) for testing.
*   **ORM**: Hibernate (via Spring Data JPA).
*   **AI Integration**: Google GenAI (`com.google.genai:google-genai`).
*   **Utilities**: Lombok (`org.projectlombok:lombok`) for boilerplate code reduction, Apache Commons Lang3 (`org.apache.commons:commons-lang3`) for common utilities.
*   **JSON Processing**: Jackson (`com.fasterxml.jackson.core:jackson-databind`).
*   **Build Tool**: Gradle (version 8.8).
*   **Java Version**: Java 21, with `eclipse-temurin:21-jdk-alpine` used for the Docker build stage and `eclipse-temurin:21-jre-alpine` for the runtime Docker image.
*   **Configuration**: `application.properties` (or `application.yml`) for server port, database connections, etc. The server port is configurable via the `PORT` environment variable. A `local` Spring profile is used in conjunction with Docker Compose for local database setup.
*   **Local Development Tools**: Docker and Docker Compose for containerization and orchestration of local services (e.g., PostgreSQL database) and the application itself.

## Development Notes
*   **Validation**: Leverages Spring Boot's Bean Validation (`@Valid` annotations on DTOs) and includes custom business logic validation within `ExpenseService`, such as verifying that `totalAmount` equals the sum of `subtotal`, `tax`, and `serviceCharge`.
*   **Error Handling**: A centralized `GlobalExceptionHandler` ensures consistent API error responses, mapping various exceptions (e.g., `ResourceNotFoundException`, `ValidationException`) to a standardized `ApiError` DTO.
*   **Slugs**: Human-friendly slugs are generated for each expense, utilizing a list of words from `src/main/resources/words.txt` via the `SlugGenerator` utility. Slugs are expected to be unique and are the primary external identifier for expenses.
*   **Financial Calculations**: The `ExpenseService` is responsible for calculating individual `subtotal`, `taxShare`, `serviceChargeShare`, and `totalOwed` for each person. Tax and service charge are always distributed proportionally based on each person's claimed item subtotal. Precision for financial calculations should ideally use `BigDecimal` to avoid floating-point errors.
*   **Authentication**: Although `spring-boot-starter-security` is included and `SecurityConfig` exists, `apidoc.yaml` explicitly states that "Currently, this API does not require authentication. All endpoints are publicly accessible." This implies the security configuration might be minimal or a placeholder for future enhancements.
*   **Real-time Updates**: The current backend design expects clients to poll the `GET /expense/{slug}` endpoint for updates, rather than using WebSockets.
*   **Local Development Setup**: For local development, the PostgreSQL database is typically started via `docker compose up -d`. The application can then be run either directly via `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` or as a Docker container (e.g., via Docker Compose). The default server port for the application is `8080`, but it can be customized using the `PORT` environment variable.
