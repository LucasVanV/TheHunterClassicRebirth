# TheHunterClassicRebirth

<img width="101" height="101" alt="image" src="https://github.com/user-attachments/assets/35c19704-f864-40f0-ac46-8d1595df9bde" />

TheHunterClassicRebirth is an open-source preservation project for **theHunter Classic**.

The goal is to rebuild the online services required by the game so that it can remain playable independently of its original infrastructure.

The project will progressively cover authentication, player profiles, inventory, progression, reserves, missions, game sessions, and launcher-related services.

---

## Project status

🚧 **Early development**

The first launcher-side backend services are now implemented.

### Implemented

- User registration and login
- JWT access tokens
- Refresh token rotation and revocation
- Logout
- Roles: `HUNTER`, `ADMIN`
- Email verification with 6-digit codes
- Dynamic `EMAIL_VERIFIED` JWT group
- Email change with re-verification
- Password change
- Account deletion
- Structured API error codes
- PostgreSQL + Flyway migrations
- Automated tests
- Bruno API collection

---

## Tech stack

- Java
- Quarkus
- Gradle
- PostgreSQL
- Hibernate ORM with Panache
- Flyway
- SmallRye JWT
- Quarkus Mailer
- JUnit
- REST Assured
- H2
- Bruno
- Docker Compose

---

## Run locally

### 1. Configure environment

```bash
cp .env.example .env
```

Fill the required values in `.env`.

### 2. Generate JWT keys

```bash
python3 scripts/generate-keys.py
```

### 3. Start PostgreSQL

```bash
docker compose up -d
```

### 4. Start the API

```bash
./gradlew quarkusDev
```

Default API URL:

```text
http://localhost:4000
```

---

## Tests

Run all automated tests with:

```bash
./gradlew test
```

Test emails use Quarkus `MockMailbox`, so no real SMTP server is contacted during automated tests.

---

## Bruno

A Bruno collection is included in:

```text
bruno/TheHunterClassicRebirth/
```

It currently covers:

```text
Launcher
├── Auth
│   ├── Register
│   ├── Login
│   ├── Refresh
│   └── Logout
└── Account
    ├── SendCode
    ├── VerifyCode
    ├── ChangeEmail
    ├── ChangePassword
    └── DeleteAccount
```

The local environment uses:

```text
baseUrl = http://localhost:4000
```

---

## Current API

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

### Account

```text
POST   /api/account/email-verification/send
POST   /api/account/email-verification/verify
PATCH  /api/account/email
PATCH  /api/account/password
DELETE /api/account
```

---

## Architecture

The backend is currently a single Quarkus application with a clear separation between launcher services and future game compatibility services.

```text
fr.cactus
├── controller/launcher
├── dto/launcher
├── service/launcher
├── service/global
├── model/launcher
├── repository/launcher
├── config
└── exception/launcher
```

The original game protocol will later be implemented separately in dedicated `game` packages.

---

## Next steps

The next development phase will focus on the **Hunter** domain:

- Hunter profile
- Player data
- Inventory
- Loadouts
- Wallet
- Skills
- Statistics
- Reserves
- Missions
- Game sessions
- Game client compatibility API

---

## Disclaimer

TheHunterClassicRebirth is an independent community preservation project.

It is not affiliated with, endorsed by, or associated with Expansive Worlds or Avalanche Studios Group.

All trademarks and intellectual property belong to their respective owners.
