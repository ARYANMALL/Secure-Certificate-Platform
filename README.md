# SecureCert – Digital Certificate Issuance & Verification System

SecureCert is a secure web-based platform for **digital certificate issuance, approval, management, verification, and revocation**.

The system provides a complete certificate lifecycle involving **Students, Institutions, a Central Issuer, and Administrators**. Certificates are protected using **SHA-256 hashing and RSA digital signatures** and can be downloaded as professionally formatted PDF documents.

---

## Features

### Student

- Self-registration
- Secure login
- Institution selection
- Certificate application
- Certificate request tracking
- View issued certificates
- Download certificate PDF
- Public certificate verification

### Institution

- Self-registration
- Institution information management
- Registration number
- Official email and address
- Administrator approval
- Institution activation

### Central Issuer

- View certificate requests from multiple institutions
- Accept student certificate requests
- Select institution
- Select active student
- Prepare certificates for any supported field
- Issue certificates for multiple institutions
- View issued certificates
- Download certificates
- Revoke certificates
- Provide revocation reasons

### Administrator

- Manage institutions
- Activate institutions
- Review certificate requests
- Approve certificates
- Reject certificate requests
- Maintain final approval authority

### Certificate Security

- Unique certificate number
- SHA-256 integrity hashing
- RSA digital signatures
- Certificate status management
- Revocation support
- Revocation reason tracking
- Public verification
- PDF certificate generation

---

## Certificate Lifecycle

```text
Student / Issuer
       │
       ▼
Certificate Request
       │
       ▼
Central Issuer
       │
       ▼
Issuer Processing
       │
       ▼
Administrator Approval
       │
       ├──────────────► Reject
       │
       ▼
Certificate Generation
       │
       ▼
SHA-256 Hash
       │
       ▼
RSA Digital Signature
       │
       ▼
ACTIVE Certificate
       │
       ├──────────────► PDF Download
       │
       ├──────────────► Public Verification
       │
       └──────────────► Revocation
```

---

## Security Architecture

```text
User
  │
  ▼
Login
  │
  ▼
Authentication
  │
  ▼
JWT Token
  │
  ▼
Role Verification
  │
  ▼
Authorized API
  │
  ▼
Database
```

### Roles

```text
ADMIN
ISSUER
STUDENT
```

---

## Certificate Security

```text
Certificate Data
       │
       ▼
Canonical Certificate Data
       │
       ▼
SHA-256 Hash
       │
       ▼
RSA Digital Signature
       │
       ▼
Stored Certificate
       │
       ▼
Public Verification
       │
       ▼
Hash + Signature Validation
```

---

## Technology Stack

### Frontend

- React.js
- JavaScript
- HTML5
- CSS3
- Vite

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- JWT

### Database

- PostgreSQL
- Flyway

### Security

- JWT Authentication
- Role-Based Authorization
- SHA-256
- RSA Digital Signatures

### Infrastructure

- Docker
- Docker Compose

---

## System Architecture

```text
                    ┌───────────────────────┐
                    │      Web Browser      │
                    │   React / JavaScript  │
                    └───────────┬───────────┘
                                │
                          REST / HTTP
                                │
                                ▼
                    ┌───────────────────────┐
                    │     Spring Boot       │
                    │       Backend         │
                    ├───────────────────────┤
                    │ Authentication        │
                    │ Authorization         │
                    │ Certificate Services  │
                    │ Request Services      │
                    │ Verification Services │
                    │ PDF Services          │
                    └───────────┬───────────┘
                                │
                          JPA / Hibernate
                                │
                                ▼
                    ┌───────────────────────┐
                    │      PostgreSQL       │
                    │       Database        │
                    └───────────────────────┘
```

---

## Project Structure

```text
Secure Certificate Platform/
│
├── backend/
│   └── certificate-api/
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/
│       │       │       └── securecertificate/
│       │       │           └── api/
│       │       │               ├── config/
│       │       │               ├── controller/
│       │       │               ├── dto/
│       │       │               ├── entity/
│       │       │               ├── exception/
│       │       │               ├── repository/
│       │       │               ├── security/
│       │       │               └── service/
│       │       │
│       │       └── resources/
│       │           ├── db/
│       │           │   └── migration/
│       │           └── application.properties
│       │
│       ├── pom.xml
│       ├── mvnw
│       └── mvnw.cmd
│
├── frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── main.jsx
│   │   ├── RegisterPage.jsx
│   │   ├── InstitutionRegisterPage.jsx
│   │   └── IssuerIssueCertificate.jsx
│   │
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── docs/
│
└── README.md
```

---

## Requirements

Before running the project, install:

- Java 17
- Maven
- Node.js
- npm
- Docker Desktop
- PostgreSQL or Docker-based PostgreSQL

Verify installations:

```powershell
java -version
mvn -version
node -v
npm -v
docker --version
```

---

## Database

The project uses PostgreSQL.

The development environment uses a PostgreSQL database similar to:

```text
Database: certificate_db
Port: 5433
```

The database schema is managed through Flyway migrations.

Flyway automatically validates and applies the required database migrations when the backend starts.

---

## Running the Backend

Open PowerShell:

```powershell
cd "C:\Users\ary67\OneDrive\Secure Certificate Platform\backend\certificate-api"
```

Build the backend:

```powershell
mvn clean compile
```

Run the application:

```powershell
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/api/health
```

---

## Running the Frontend

Open another terminal:

```powershell
cd "C:\Users\ary67\OneDrive\Secure Certificate Platform\frontend"
```

Install dependencies:

```powershell
npm install
```

Start the development server:

```powershell
npm run dev
```

The frontend is available at:

```text
http://localhost:3000
```

---

## Production Build

Build the frontend:

```powershell
npm run build
```

The generated production files are placed in the Vite distribution directory.

---

## User Workflow

### Student Workflow

```text
Register
   ↓
Login
   ↓
Select Institution
   ↓
Request Certificate
   ↓
Track Request
   ↓
Certificate Approved
   ↓
View Certificate
   ↓
Download PDF
   ↓
Verify Certificate
```

### Central Issuer Workflow

```text
Login
   ↓
View Certificate Requests
   ↓
Review Requests
   ↓
Accept Request

OR

Issue Certificate
   ↓
Select Institution
   ↓
Select Student
   ↓
Enter Course
   ↓
Submit for Admin Approval
```

The central issuer can work with students from multiple institutions.

### Administrator Workflow

```text
Login
   ↓
Manage Institutions
   ↓
Activate Institutions
   ↓
Review Certificate Approvals
   ↓
Approve / Reject
```

### Verification Workflow

```text
Certificate Number
       ↓
Public Verification
       ↓
Certificate Lookup
       ↓
Status Check
       ↓
SHA-256 Validation
       ↓
RSA Signature Validation
       ↓
Verification Result
```

---

## Certificate Status

Certificates can have different lifecycle states.

### Active

```text
ACTIVE
```

The certificate is currently valid.

### Revoked

```text
REVOKED
```

The certificate is no longer valid.

The system maintains:

- Revocation date
- Revocation reason

Example:

```text
ACTIVE
  ↓
REVOKED
```

---

## Example Certificate

A generated certificate contains information such as:

```text
Certificate Number
Student Name
Institution
Course / Program
Issuer
Issue Date
Certificate Status
Digital Verification Information
```

The certificate is generated as a formatted PDF suitable for printing and physical use.

---

## Public Verification

Certificates can be verified without logging into the platform.

Example:

```text
Certificate Number:
SC-2026-000008
```

The verification system can display:

```text
Student
Institution
Issuer
Course
Issue Date
Status
Integrity
Digital Signature
```

Possible verification results include:

```text
CERTIFICATE AUTHENTIC
```

```text
CERTIFICATE REVOKED
```

```text
INVALID CERTIFICATE
```

---

## Example Certificate Data

```text
Student:
lucky

Institution:
DIT

Course:
Data Science

Issuer:
Dr. Amit Verma

Certificate:
SC-2026-000008

Status:
ACTIVE

Integrity:
VERIFIED

Digital Signature:
VERIFIED
```

---

## API Overview

The backend provides REST APIs for major platform operations.

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

### Health

```text
GET /api/health
```

### Student

```text
GET /api/student/certificates
```

### Certificate Requests

```text
POST /api/certificate-requests
GET  /api/certificate-requests/my
GET  /api/certificate-requests/issuer
GET  /api/certificate-requests/admin
```

### Issuer Management

```text
GET  /api/issuer/manage/institutions
GET  /api/issuer/manage/students
POST /api/issuer/manage/certificate-requests
```

### Certificate Management

```text
GET   /api/certificates
PATCH /api/certificates/{id}/revoke
```

### Public Verification

```text
GET /api/public/certificates/{certificateNumber}/verify
```

> Exact endpoints may evolve as the application is extended.

---

## Testing

The completed system supports testing of:

- Student registration
- User login
- Invalid login
- Institution registration
- Institution activation
- Certificate requests
- Central issuer processing
- Multi-institution issuance
- Administrator approval
- Certificate generation
- SHA-256 integrity verification
- RSA signature verification
- PDF generation
- Public certificate verification
- Certificate revocation
- Revocation reason tracking

---

## Security

SecureCert uses multiple security mechanisms:

### Password Protection

Passwords are stored using secure password hashing rather than plain text.

### JWT Authentication

Authenticated requests use JWT tokens.

### Role-Based Authorization

Access is controlled using:

```text
ADMIN
ISSUER
STUDENT
```

### SHA-256

SHA-256 is used to verify certificate data integrity.

### RSA

RSA digital signatures are used to verify certificate authenticity.

### Database Protection

Database access is handled through Spring Data JPA and controlled backend services.

---

## Advantages

- Secure certificate lifecycle
- Centralized certificate management
- Multi-institution support
- Student self-registration
- Central issuer support
- Administrator approval
- Tamper detection
- Digital signatures
- Public verification
- Certificate revocation
- Professional PDF certificates
- Reduced manual paperwork

---

## Future Enhancements

Potential future improvements include:

- Email notifications
- Android and iOS applications
- Advanced QR verification
- Blockchain integration
- Multiple issuer levels
- Custom certificate templates
- Analytics dashboards
- Automated backups
- Cloud deployment
- Advanced identity verification
- External API integration
- International digital credential standards

---

## Troubleshooting

### Port 8080 already in use

Check the process:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Then identify the process:

```powershell
Get-Process -Id <PID>
```

Stop it if necessary:

```powershell
Stop-Process -Id <PID> -Force
```

Then restart:

```powershell
mvn spring-boot:run
```

### Frontend build errors

Run:

```powershell
npm install
npm run build
```

### Backend compilation errors

Run:

```powershell
mvn clean compile
```

### Database problems

Check that PostgreSQL/Docker is running and that the database configuration in the backend configuration file matches the local environment.

---

## Development Notes

The system is designed around a controlled certificate lifecycle.

The central issuer can manage requests from multiple institutions, while the student's actual institution remains associated with the certificate.

Administrator approval is maintained as the final approval stage before a certificate becomes active.

This separation provides a clear distinction between:

```text
Student
   ↓
Request

Issuer
   ↓
Prepare / Process

Administrator
   ↓
Approve

System
   ↓
Generate + Sign

Public
   ↓
Verify
```

---

## License

This project is developed as an academic/software engineering project.

For production deployment, appropriate licensing, security review, data protection policies, key management, backup procedures, and infrastructure controls should be established.

---

## Project Summary

**SecureCert** provides a complete digital certificate lifecycle:

```text
Registration
     ↓
Institution Association
     ↓
Certificate Request
     ↓
Central Issuer
     ↓
Administrator Approval
     ↓
Certificate Generation
     ↓
SHA-256 Hashing
     ↓
RSA Digital Signature
     ↓
PDF Certificate
     ↓
Public Verification
     ↓
Revocation if Required
```

**SecureCert – Secure. Verifiable. Digital.**
