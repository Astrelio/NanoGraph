# NanoGraph

NanoGraph is a desktop academic management application built with JavaFX. It is designed for students who need a centralized tool to organize their coursework, track assignments, take notes, and manage study sessions using the Pomodoro technique.

The application uses a retro-inspired visual theme with the "Press Start 2P" pixel font and a dark color palette, giving it a distinctive and focused aesthetic that reduces visual fatigue during long study sessions.

---

## Features

### User Authentication
- Registration and login system with email-based credentials.
- Persistent local sessions: the application remembers the logged-in user between launches, eliminating the need to re-enter credentials each time.
- Session management with logout functionality.

### Dashboard
- Central hub displaying all registered subjects (materias) and a summary of pending tasks.
- Hover tooltips on each subject show a quick breakdown of pending tasks and saved notes.
- Context menu on subjects allows deletion with confirmation dialogs.
- Quick navigation buttons to all application modules.

### Task Management (Tareas)
- Full CRUD operations for tasks within each subject.
- Each task includes a title, description, and due date.
- Tasks can be marked as completed or pending.
- Filtered views per subject using a dropdown selector.
- Context menu for editing and deleting individual tasks.

### Notes (Notas)
- Create, read, update, and delete notes organized by subject.
- Each note has a title and a free-text content field.
- Notes are loaded asynchronously per subject.
- Context menu with edit and delete options.

### Pomodoro Timer
- Configurable study timer with a default duration of 25 minutes (minimum 10 minutes).
- Start, pause, and reset controls.
- Automatic session logging: when a timer completes, the session is saved to the database with its duration and associated subject.
- Session history per subject with delete support.

---

## Architecture

The project follows the MVC (Model-View-Controller) pattern:

```
src/
  Application/     Entry point (Main.java)
  model/           Data models (Usuario, Materia, Tarea, Nota, SesionPomodoro, Contenido)
  view/            FXML layouts and CSS stylesheet
  controller/      JavaFX controllers for each view
  dao/             Data Access Objects with Supabase REST API integration
    interfaces/    DAO interface contracts
  util/            Utility classes (navigation, alerts, session, configuration)
  assets/          Fonts and static resources
```

### Data Models

- **Contenido** -- Abstract base class for content types. Holds common fields: id, creation date, and subject reference.
- **Tarea** -- Extends Contenido. Represents a task with title, description, due date, and completion status.
- **Nota** -- Extends Contenido. Represents a note with title and free-text content.
- **Materia** -- Represents an academic subject with a name, color code, and owner reference.
- **SesionPomodoro** -- Represents a completed Pomodoro session with duration and date.
- **Usuario** -- Represents a registered user with name, email, and registration date.

### Data Layer

All persistence is handled through Supabase, a hosted PostgreSQL backend accessed via its REST API. The `SupabaseClient` class implements a singleton HTTP client that handles GET, POST, PATCH, and DELETE operations against the Supabase endpoint. Each model has a corresponding DAO class that serializes and deserializes JSON manually using `org.json`.

### View Layer

The interface is built with FXML layouts styled through a unified CSS stylesheet. The visual theme uses the following color palette:

| Role          | Color   |
|---------------|---------|
| Background    | #1E1E2E |
| Base surfaces | #4A4262 |
| Accent/Hover  | #A78BFA |
| Active/Light  | #FDE047 |
| Text          | #F5F5F7 |

---

## Technology Stack

| Component     | Technology                     |
|---------------|--------------------------------|
| Language      | Java                           |
| UI Framework  | JavaFX with FXML               |
| Styling       | CSS (JavaFX dialect)           |
| Font          | Press Start 2P (bundled .ttf)  |
| Backend       | Supabase (PostgreSQL + REST)   |
| HTTP Client   | java.net.http (Java 11+)       |
| JSON Parsing  | org.json                       |
| IDE           | Eclipse                        |

---

## Prerequisites

- Java 11 or higher.
- JavaFX SDK (if not bundled with your JDK distribution).
- A Supabase project with the required tables configured (usuarios, materias, tareas, notas, sesiones_pomodoro).
- The `org.json` library available in the classpath.

---

## Configuration

Database connection settings are located in `src/util/SupabaseConfig.java`. Update the following constants with your own Supabase project credentials before running:

```java
public static final String SUPABASE_URL = "https://your-project.supabase.co";
public static final String SUPABASE_KEY = "your-anon-key";
```

---

## Running the Application

1. Open the project in Eclipse (or any Java IDE with JavaFX support).
2. Ensure JavaFX and org.json are in the build path.
3. Run `Application.Main` as a Java application.
4. The application starts at 800x600 and opens either the login screen or the dashboard, depending on whether a saved session exists.

---

## License

This project does not currently specify a license. All rights reserved by the author.
