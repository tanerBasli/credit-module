# Loan API - Spring Boot Application

This is a backend Loan API for a bank that allows employees to create, list, and pay loans for their customers.

## Features

- **Create Loan**: Create a new loan for a given customer, amount, interest rate, and number of installments.
- **List Loans**: List loans for a given customer with optional filters (e.g., number of installments, is paid).
- **List Installments**: List installments for a given loan.
- **Pay Loan**: Pay installments for a given loan and amount.

## Requirements

- Java 21 or higher
- Maven 3.x
- H2 Database (in-memory)

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/test-repo/loan-api.git
   cd loan-api
2. Build the Docker Image
   ```bash
   docker-compose build
   docker-compose up

## Usage
for usage of code please check unit and integration  tests
under the
- [Integration tests](src/test/java/com/banktest/loanapi/controller)
- [Unit test](src/test/java/com/banktest/loanapi/service)