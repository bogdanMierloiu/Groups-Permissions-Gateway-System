# Permissions-System
## Overview
This project is a Java Spring API secured with OAuth 2.0, using a custom Spring Authorization Server as the issuer.
It's updated JSON Web Key (JWK) set at startup and validates tokens for incoming requests to ensure secure access to the resources.
The API is designed to manage users within groups with hierarchical structure and the permissions those groups have to access resources via endpoints (GET, POST, PATCH, DELETE).

## Features
- User Management: Create, update, and delete users.
- Group Management: Organize users into groups.
- Permission Management: Assign permissions to groups to control access to API endpoints.
- OAuth 2.0 Security: Secure access to resources using OAuth 2.0 tokens validated against a custom Spring Authorization Server.

## Technologies Used
- Backend Framework: Spring Boot
- Security: OAuth 2.0, Spring Security
- Authorization Server: Custom Spring Authorization Server
- Database: PostgreSQL
