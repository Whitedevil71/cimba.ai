# AI Meeting Minutes Generator

An intelligent web application that automatically transcribes meeting audio and generates structured meeting minutes using OpenAI's Whisper and GPT APIs.

## Features

-  Audio transcription using Whisper API
-  Text transcript processing
-  AI-powered summarization with GPT-4
-  Automatic extraction of key decisions
-  Action items with responsible persons
-  Downloadable meeting reports
-  Meeting history tracking
-  Responsive design

## Tech Stack

**Frontend:**

- React 18
- Axios for API calls
- Modern CSS with animations

**Backend:**

- Spring Boot 3.2
- Java 17
- H2 Database (in-memory)
- OpenAI API integration
- RESTful API

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 16+ and npm
- Google Gemini API key (FREE - get from https://makersuite.google.com/app/apikey)
- Hugging Face API key (FREE - get from https://huggingface.co/settings/tokens)

## Installation & Setup

### 1. Install Java and Maven (if not already installed)

Check if Java is installed:

```cmd
java -version
```

If not installed, download from: https://adoptium.net/

Check if Maven is installed:

```cmd
mvn -version
```

If not installed, download from: https://maven.apache.org/download.cgi

### 2. Backend Setup

Navigate to backend folder and configure:

```cmd
cd backend
```

**IMPORTANT:** Edit `src/main/resources/application.properties` and add your API keys:

```properties
# Get FREE Gemini API key from: https://makersuite.google.com/app/apikey
gemini.api.key=your-actual-gemini-api-key-here

# Get FREE Hugging Face API key from: https://huggingface.co/settings/tokens
huggingface.api.key=your-actual-huggingface-api-key-here
```

**⚠️ Security Note:** Never commit your actual API keys to GitHub!

Install dependencies and run:

```cmd
mvn clean install
mvn spring-boot:run
```

Backend will start on: http://localhost:8080

### 3. Frontend Setup

Open a new terminal and navigate to frontend:

```cmd
cd frontend
npm install
npm start
```

Frontend will start on: http://localhost:3000

## Usage

1. **Generate Minutes from Text:**

   - Select "Text Transcript" mode
   - Enter meeting title
   - Paste transcript
   - Click "Generate Minutes"

2. **Generate Minutes from Audio:**

   - Select "Audio File" mode
   - Enter meeting title
   - Upload audio file (mp3, wav, etc.)
   - Click "Transcribe & Generate"

3. **View History:**

   - Click "History" tab
   - View all past meetings
   - Click any card to view details

4. **Download Report:**
   - After generating minutes
   - Click "Download Report" button
   - Get a formatted text file

## API Endpoints

- `POST /api/minutes/transcript` - Process text transcript
- `POST /api/minutes/audio` - Process audio file
- `GET /api/minutes` - Get all meeting minutes
- `GET /api/minutes/{id}` - Get specific meeting minutes

## Project Structure

```
├── backend/
│   ├── src/main/java/com/cimba/meetingminutes/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   └── dto/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── App.js
│   │   └── index.js
│   └── package.json
└── README.md
```

## Configuration

### Backend Configuration (application.properties)

- Server port: 8080
- Database: H2 in-memory
- Max file size: 50MB
- OpenAI API key required

### Frontend Configuration

- API URL: http://localhost:8080/api/minutes
- Development port: 3000

## Notes

- Make sure both backend and frontend are running simultaneously
- The H2 database is in-memory, so data resets on restart
- For production, replace H2 with PostgreSQL or MySQL
- Audio transcription requires valid OpenAI API key with Whisper access
- Large audio files may take longer to process

## Troubleshooting

**Backend won't start:**

- Check Java version (must be 17+)
- Verify Maven is installed
- Check if port 8080 is available

**Frontend won't start:**

- Check Node.js version (16+)
- Delete node_modules and run `npm install` again
- Check if port 3000 is available

**API calls failing:**

- Verify backend is running on port 8080
- Check OpenAI API key is configured
- Check CORS settings in backend

## License

MIT License - feel free to use for your projects!
