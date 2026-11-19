# EkoLagja

A digital platform that connects residents with their municipality: notices, complaints, polls and neighbourly help.

The municipality publishes notices for neighbourhoods and runs polls to hear what residents think. Residents submit complaints to the municipality and follow how they are handled, while neighbours can ask for or offer help within their neighbourhood.

Group project for the Software System Design course (Dizajni i Sistemit të Softuerit), UBT College, 2026.

## Team

- Edin Kamberi (project manager)
- Ediar Halili
- Erjon Demiri
- Endrit Abdullahu

## Responsibilities

Each member owns one module: they describe it in the SAD, design it in the DDD and implement it in every layer of the code.

| Module | Member | Responsibility | Packages / classes |
|---|---|---|---|
| Complaints | Edin Kamberi | Submitting a complaint to the municipality, handling by an official, complaint status and its history, internal staff notes and the official response | Defined in the DDD |
| Neighbourly Help | Ediar Halili | Posts requesting or offering help between neighbours, and residents' responses | Defined in the DDD |
| Notices | Erjon Demiri | Publishing official notices for one or more neighbourhoods, urgent notices and expiry | Defined in the DDD |
| Polls | Endrit Abdullahu | Creating polls with options, residents voting, and results | Defined in the DDD |
| Shared work | Whole group | Conceptual model, architecture skeleton, authentication and roles, administration (neighbourhoods, departments, staff accounts) and configuration | Defined in the DDD |

## Documentation

| Document | Status | Location |
|---|---|---|
| Conceptual model | Done | [diagram](docs/Conceptual_Model_EkoLagja.drawio), [description](docs/Conceptual_Model_EkoLagja.md) |
| Software Architecture Document (SAD) | In progress | `docs/` |
| Detailed Design Document (DDD) | Not started | `docs/` |

## Technologies

- **Backend:** Java 17, Spring Boot, Maven
- **Frontend:** React (Vite)
- **Database:** PostgreSQL (the current skeleton still uses an in-memory H2 database until the database setup is done)

## Repository structure

```
.
├── backend/     Spring Boot (Java, Maven)
├── frontend/    React (Vite)
└── docs/        Conceptual model, SAD and DDD
```

The current code is an early skeleton. The package structure and classes will change to follow the DDD.

## Running in development

Backend (http://localhost:8080, with the H2 console at `/h2-console`):

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (http://localhost:5173, forwards `/api` requests to the backend):

```bash
cd frontend
npm install
npm run dev
```
