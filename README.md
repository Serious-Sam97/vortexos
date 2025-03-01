# GameCache / PixelVault

A personal project to track and record your gaming journey, including platforms, progress, and media uploads. Designed for gamers to manage their game library and track their adventures seamlessly.

## Features
- **Game Library**: Add games manually or fetch data using an API.
- **Progress Tracker**: Track game completion, playtime, and notes.
- **Media Uploads**: Save gameplay videos and screenshots.
- **Wishlist**: Organize games you want to play.
- **Analytics**: Visualize gaming habits and trends.

## Tech Stack
- **Backend**: Spring Boot (Java) with SQLite.
- **Frontend**: React.js with Tailwind CSS.
- **Database**: SQLite for standalone deployment.
- **Deployment**: Dockerized backend and frontend for easy deployment.

## Installation

### Prerequisites
- Java 17 or higher.
- Node.js (for the frontend).
- Docker (optional for containerized setup).

### Backend Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/gamecache.git
   cd vortexos/backend
   ```
2. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The application will be available at `http://localhost:8080`.

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd ../frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```
4. Access the frontend at `http://localhost:3000`.

## Usage
1. Add games manually or use an API integration to fetch details.
2. Track your progress, upload media, and view analytics.
3. Organize your wishlist and completed games.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue to discuss any changes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

---

**Happy Gaming!** 🎮
