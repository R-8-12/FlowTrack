# FlowTrack

FlowTrack is a Spring Boot inventory and onboarding platform with role-based access, vendor/borrower flows, reporting, verification, and chatbot-assisted support.

This is the single source of truth for setup and day-to-day development across IDEs.

## Tech Stack

- Java 11 (project target in `pom.xml`)
- Spring Boot 2.7.18
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- MySQL 8+
- Flyway migrations
- Thymeleaf + Spring MVC

## Requirements

- JDK 11 or newer installed
- MySQL running locally (default host/port in config: `localhost:3306`)
- Git (optional but recommended)

## Quick Start (Any IDE or Terminal)

1. Clone and open the project.
2. Create database `ims` in MySQL.
3. Update credentials/secrets in `src/main/resources/application.properties`.
4. Build and run with Maven Wrapper:

```bash
# Windows PowerShell / CMD
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw clean install
./mvnw spring-boot:run
```

5. Open the app at `http://localhost:8087` (or the value set by `PORT`).

Default seed credentials:

- Username: `admin`
- Password: `admin123`

## IDE-Agnostic Setup Notes

Use whichever IDE you prefer (IntelliJ, VS Code, Eclipse, NetBeans).

- Import as a Maven project.
- Set project SDK/JDK to 11+.
- Ensure Maven uses wrapper or local Maven 3.6+.
- Run main class: `com.example.IMS.ImsApApplication`.

## Runtime Configuration

Main application config is in `src/main/resources/application.properties`.

Key defaults:

- Server port: `server.port=${PORT:8087}`
- Database URL: `jdbc:mysql://localhost:3306/ims?...`
- Flyway: enabled (`spring.flyway.enabled=true`)
- JPA schema mode: `validate`

## Sensitive Config (Important)

The repository currently contains placeholder and/or environment-specific credentials in `application.properties`.

For team-safe usage:

1. Replace hardcoded secrets with environment variables.
2. Keep only safe defaults in tracked config.
3. Rotate any exposed keys before production usage.

Recommended environment variables:

- `PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `GEMINI_API_KEY`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

## Database and Migrations

FlowTrack uses Flyway migrations under:

- `src/main/resources/db/migration`

On startup, migrations are applied automatically (based on Flyway settings).

## Useful Commands

```bash
# Run tests
mvnw.cmd test

# Build without tests
mvnw.cmd clean package -DskipTests

# Run packaged jar
java -jar target/IMS-AP-0.0.1-SNAPSHOT.jar
```

## Project Layout

- `src/main/java` - application code
- `src/main/resources` - config, templates, static files, DB migrations
- `src/test/java` - tests
- `target` - build outputs

## Documentation Map

Detailed and specialized docs are listed in `DOCUMENTATION_INDEX.md`.

## Troubleshooting

Start with:

- `TROUBLESHOOTING.md`
- `MYSQL_SETUP_GUIDE.md`
- `MANUAL_TESTING_GUIDE.md`
