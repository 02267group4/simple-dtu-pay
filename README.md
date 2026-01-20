# Simple DTU Pay — Quick Start

Prereq: Docker (Desktop/WSL on Windows). From repo root:

Run full stack (recommended)
- `docker compose up --build -d`  # or `docker-compose up --build -d`
- `docker-compose logs -f dtu-pay rabbitmq`
- `docker-compose down --volumes --remove-orphans`

Run client tests (uses running stack)
- Linux/WSL/Git Bash:
    - `export BANK_API_KEY="eagle0192"`
    - `mvn -f simple_dtu_pay_client test`
- PowerShell:
    - `$env:BANK_API_KEY="eagle0192"`
    - `mvn -f simple_dtu_pay_client test`

Run service locally (optional)
- `cd simple_dtu_pay_service`
- Unix: `./mvnw quarkus:dev`
- Windows: `mvnw.cmd quarkus:dev`

Quick checks
- `curl http://localhost:8080/payments` (use `curl.exe` in PowerShell)
- Watch for logs: `Publishing PaymentRequested`, `token.validated` / `token.rejected`, and SOAP `POST /services/BankService`

Note
- In-memory token repo now treats `null` tokens safely (returns empty) so rejects are clean.
