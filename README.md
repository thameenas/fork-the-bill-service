# Fork-the-Bill Service

A Spring Boot RESTful API for splitting bills among participants, tracking items, and managing shared expenses. Features include participant/item management, bill creation, and automatic calculation of shares.

## Features
- Create and manage bills
- Add/remove participants and items
- Assign items to participants
- Retrieve all bills, participants, and items
- OpenAPI/Swagger documentation

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA (SQLite)
- Spring Validation
- Swagger/OpenAPI (Springdoc)

## Getting Started

### Prerequisites
- Java 17+
- Gradle

### Setup & Run
```sh
git clone <repo-url>
cd fork-the-bill/fork-the-bill-service
gradle bootRun
```
The service will run at `http://localhost:8081`.

### API Documentation
- Visit [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) for Swagger UI.
- Example endpoints:
  - `POST /bill` – Create a bill
  - `GET /bill/{billId}` – Get a bill by ID
  - `POST /participant` – Add a participant
  - `POST /item` – Add an item

See Swagger UI for full details and try out requests interactively.

## Contributing
PRs welcome! Please add tests for new features.

## License
MIT