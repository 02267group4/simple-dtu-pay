# Installation Guide – Simple DTU Pay

DTU – 02267 Software Engineering, Group 4  
This document describes how to set up and access the Simple DTU Pay application.

## Introduction

This guide provides step-by-step instructions for cloning, building, and accessing the Simple DTU Pay application.
The project is hosted on the DTU Linux VM and uses Jenkins for CI/CD.

## Prerequisites

- Git
- JDK 21 (Java)
- Maven
- Docker Engine and Docker Compose

## Cloning the Repository

```bash
mkdir simple-dtu-pay
cd simple-dtu-pay
git clone https://github.com/02267group4/simple-dtu-pay.git .
```

## Launch Services and Run Tests

Export required env vars before building/running:

```bash
export BANK_API_KEY="eagle0192"
```

Start services:

```bash
docker compose up -d --build
```

Run tests:

```bash
cd simple_dtu_pay_client
mvn clean test
```

## Jenkins – Continuous Integration

- URL: http://fm-04.compute.dtu.dk:8282/
- Username: huba
- Password: 1234

## Accessing the Group 4 Linux VM

```bash
ssh -i /path/to/your/fm-04-key huba@fm-04.compute.dtu.dk
```

- Username: huba
- Password: 1234
