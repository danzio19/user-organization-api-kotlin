# User Organization API

A RESTful API for managing users, organizations, and their memberships, built with Kotlin and Spring Boot.

This project covers a complete set of CRUD operations, a role-based authorization system, email notifications, and automated API documentation.

## Tech Stack

-   **Backend:** Kotlin, Spring Boot, Spring Data JPA
-   **Database:** H2 (In-Memory), PostgreSQL compatible
-   **Build:** Maven
-   **API Docs:** OpenAPI (Swagger UI)
-   **Testing:** MailHog on Docker

## Quick Start

### Prerequisites
-   Java 21+
-   Maven 3.8+
-   Docker

### 1. Run Services
Start the local email testing server:

docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog

You can view captured emails at **`http://localhost:8025`**.

### 2. Run The Application
Clone the repository and use the Maven wrapper to run the app:

git clone https://github.com/danzio19/user-organization-api-kotlin.git
cd user-organization-api-kotlin
./mvnw spring-boot:run

## API Documentation

Once running, the interactive API documentation is available via Swagger UI:

-   **Swagger UI:** **`http://localhost:8080/swagger-ui.html`**

Authentication is simulated using an `X-User-ID` header on protected endpoints. **Note:** The very first user created (without a header) automatically becomes an `ADMIN`.

## Key Architectural Decisions

-   **DTOs for API Layer:** The API returns DTOs, not JPA entities, to prevent lazy-loading errors and decouple the API from the database schema.
-   **Centralized Authorization:** A dedicated `AuthorizationService` handles all permission checks, looking up user roles from the database based on the `X-User-ID`.
-   **RESTful Nested Resources:** Endpoints are structured logically, such as `GET /users/{id}/organizations` to show a user's memberships.
-   **Invitation-Managed Relationships:** The Many-to-Many link between a user and an organization is only created when an invitation is formally accepted.
