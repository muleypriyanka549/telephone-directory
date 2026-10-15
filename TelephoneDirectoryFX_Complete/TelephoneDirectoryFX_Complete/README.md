# Telephone Directory Management System

A complete JavaFX + MySQL Maven project with login, dashboard and contact management.

## Features
- Login authentication
- SHA-256 password verification
- Dashboard
- Add, update and delete contacts
- Search by name, phone, email or address
- Input validation
- Duplicate phone-number prevention
- Contact count and status messages
- Logout and exit confirmation
- MySQL database integration

## Default Login
- Username: `admin`
- Password: `admin123`

## Requirements
- JDK 17 or later
- Eclipse IDE with Maven support
- MySQL Server

## Database Setup
1. Start MySQL Server.
2. Open MySQL Workbench or MySQL Command Line.
3. Run `database.sql`.
4. If your MySQL password is not `root`, edit `DBConnection.java`.

## Run in Eclipse
1. Import as **Existing Maven Project**.
2. Right-click project -> **Maven -> Update Project**.
3. Right-click project -> **Run As -> Maven build...**
4. Goals: `clean javafx:run`

## Important
The application connects to:
`jdbc:mysql://localhost:3306/telephone_directory`

If MySQL uses another password, update the `PASSWORD` constant in `DBConnection.java`.
