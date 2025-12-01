# Backend Commands Reference

## Starting the Backend

### Option 1: Using the startup script (Recommended)
```cmd
cd D:\CIMBA
start-backend.cmd
```

### Option 2: Manual start
```cmd
cd D:\CIMBA\backend
set JAVA_HOME=C:\src\jdk-21.0.1
mvnw.cmd spring-boot:run
```

### Option 3: Using PowerShell
```powershell
cd D:\CIMBA\backend
$env:JAVA_HOME="C:\src\jdk-21.0.1"
.\mvnw.cmd spring-boot:run
```

## Building the Backend

### Clean and build
```cmd
cd D:\CIMBA\backend
set JAVA_HOME=C:\src\jdk-21.0.1
mvnw.cmd clean install
```

### Build without tests
```cmd
mvnw.cmd clean install -DskipTests
```

### Package as JAR
```cmd
mvnw.cmd clean package
```

## Running the JAR

After building, you can run the JAR directly:
```cmd
cd D:\CIMBA\backend\target
java -jar meeting-minutes-1.0.0.jar
```

## Useful Maven Commands

### Check dependencies
```cmd
mvnw.cmd dependency:tree
```

### Update dependencies
```cmd
mvnw.cmd versions:display-dependency-updates
```

### Run tests
```cmd
mvnw.cmd test
```

## Configuration

### Application Properties Location
```
backend\src\main\resources\application.properties
```

### Key Settings
- Server Port: 8080
- Database: H2 in-memory
- OpenAI API Key: Set in application.properties

## Accessing H2 Database Console

While backend is running, visit:
```
http://localhost:8080/h2-console
```

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:meetingdb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Base URL
```
http://localhost:8080/api/minutes
```

### Test API with curl

**Process Text Transcript:**
```cmd
curl -X POST http://localhost:8080/api/minutes/transcript ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Test Meeting\",\"transcript\":\"This is a test transcript.\"}"
```

**Get All Minutes:**
```cmd
curl http://localhost:8080/api/minutes
```

**Get Specific Minutes:**
```cmd
curl http://localhost:8080/api/minutes/1
```

## Stopping the Backend

Press `Ctrl + C` in the terminal where backend is running.

## Logs Location

Logs are displayed in the console. To save logs to a file:
```cmd
mvnw.cmd spring-boot:run > backend.log 2>&1
```

## Common Issues

### Port 8080 already in use
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### JAVA_HOME not set
```cmd
set JAVA_HOME=C:\src\jdk-21.0.1
echo %JAVA_HOME%
```

### Maven wrapper not executable
```cmd
icacls mvnw.cmd /grant Everyone:F
```

## Environment Variables

### Set for current session
```cmd
set JAVA_HOME=C:\src\jdk-21.0.1
set OPENAI_API_KEY=sk-your-key-here
```

### Set permanently (Windows)
```cmd
setx JAVA_HOME "C:\src\jdk-21.0.1"
setx OPENAI_API_KEY "sk-your-key-here"
```

## Development Mode

### Enable hot reload (requires spring-boot-devtools)
Add to pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

## Production Deployment

### Build production JAR
```cmd
mvnw.cmd clean package -Pprod
```

### Run in production
```cmd
java -jar target/meeting-minutes-1.0.0.jar --spring.profiles.active=prod
```

## Health Check

### Check if backend is running
```cmd
curl http://localhost:8080/actuator/health
```

Or open in browser:
```
http://localhost:8080/actuator/health
```
