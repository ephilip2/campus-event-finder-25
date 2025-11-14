# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the CS 124 Fall 2025 Machine Project (MP) - an Android application written in Kotlin for event discovery and management. The app displays event summaries retrieved from a mock server and uses a client-server architecture pattern.

## Essential Commands

### Building and Testing
- **Run all tests**: `./gradlew test`
- **Run specific checkpoint**: `./gradlew grade` (grades checkpoint specified in `grade.yaml`)
- **Run single test class**: `./gradlew test --tests "edu.illinois.cs.cs124.ay2025.mp.test.MP0Test"`
- **Clean build**: `./gradlew clean build`

### Code Quality
- **Run linter (ktlint)**: `./gradlew spotlessApply`
- **Run detekt**: `./gradlew detekt`
- **Check time provider usage**: `./gradlew checkTimeProvider`
- **Check path validity**: `./gradlew checkPaths`

### Grading
- **Grade current checkpoint**: `./gradlew grade`
- **Change checkpoint**: Edit `checkpoint:` value in `grade.yaml` (0, 1, 2, or 3), then sync Gradle

## Architecture Overview

### Client-Server Pattern
The app uses a **local MockWebServer** that runs on port 8024 to simulate a backend:

- **Server** (`network/Server.kt`): A MockWebServer Dispatcher that loads event data from `/events.json` and serves filtered summaries. Filters events to show only those starting from today onwards (America/Chicago timezone).
- **Client** (`network/Client.kt`): Makes HTTP requests to the server using OkHttp. Uses a callback pattern with `ResultMightThrow<T>` for async operations.

The server starts automatically in `EventableApplication.onCreate()` except during Robolectric tests.

### Key Components

**Models**:
- `EventData`: Complete event information from JSON (id, seriesId, title, start, location, description, categories, source, url, virtual)
- `Summary`: Lightweight event view (id, title, start, location) used in the UI list

**Activities**:
- `MainActivity`: Displays event summaries in a RecyclerView. Loads summaries on resume, updates UI on the main thread.

**Adapters**:
- `SummaryListAdapter`: RecyclerView adapter that formats and displays summary items. Formats dates as "MMM d • h:mm a • location" and truncates long locations.

**Application**:
- `EventableApplication`: Initializes and starts the server in a separate thread with an 8-second timeout.

### Time Handling

**CRITICAL**: Never use `Instant.now()`, `ZonedDateTime.now()`, or `LocalDateTime.now()` directly in main source code.

Instead, use `Helpers.getTimeProvider().now()` for all time operations. This allows tests to mock time using `setTimeProvider()`. The `checkTimeProvider` Gradle task enforces this rule before tests run.

### Threading Model

- Client network calls execute on a background executor (single-threaded in tests, cached thread pool in production)
- UI updates must use `runOnUiThread()`
- Server runs in a separate thread during startup

## Project Structure

```
app/src/
├── main/kotlin/edu/illinois/cs/cs124/ay2025/mp/
│   ├── activities/        # UI activities (MainActivity)
│   ├── adapters/          # RecyclerView adapters
│   ├── application/       # Application class, constants
│   ├── helpers/           # Utilities (TimeProvider, objectMapper)
│   ├── models/            # Data models (Summary, EventData)
│   └── network/           # Client and Server
├── main/res/              # Android resources
│   ├── layout/            # XML layouts (activity_main, item_summary)
│   └── values/            # Colors, strings
└── test/kotlin/edu/illinois/cs/cs124/ay2025/mp/test/
    ├── MP0Test.kt, etc.   # Checkpoint test suites
    └── helpers/           # Test utilities and helpers
```

## Development Workflow

1. **Select checkpoint**: Update `checkpoint:` in `grade.yaml` (0-3)
2. **Sync Gradle**: File → Sync Project with Gradle Files in Android Studio
3. **Write code**: Implement required functionality
4. **Format code**: `./gradlew spotlessApply` (auto-formats to ktlint standards)
5. **Run tests**: `./gradlew test` or `./gradlew grade`
6. **Check quality**: `./gradlew detekt` (10 points of grade)
7. **Commit changes**: Git commit with descriptive messages

## Important Constraints

### Testing
- Test files (`MP0Test.kt`, etc.) will be **overwritten during official grading** - don't rely on modifications to test files
- Use Robolectric for Android unit tests (not instrumentation tests)
- Tests use adaptive timeouts controlled by environment variables: `TEST_TIMEOUT_MULTIPLIER`, `TEST_TIMEOUT_MIN`, `TEST_TIMEOUT_MAX`

### Code Quality
- Max line length: 120 characters (ktlint)
- Detekt violations reduce grade by up to 10 points
- Main source paths must contain only ASCII characters (checked by `checkPaths` task)
- **Always use braces for if statements**: Never use braceless if statements. Always include braces `{}` even for single-line conditionals.

### Grading System
- Total points: 100
- Detekt: 10 points
- Checkpoints 2 & 3 have early deadlines worth 10 additional points
- Uses `org.cs124.gradlegrader` plugin
- Results posted to `grade.json` and `https://cloud.cs124.org/gradlegrader`

## Dependencies

Key libraries:
- **OkHttp** (5.2.1): HTTP client and MockWebServer
- **Jackson** (2.20.0): JSON serialization with Kotlin module
- **Robolectric** (4.16): Android unit testing framework
- **Espresso** (3.7.0): UI testing
- **RecyclerView** (1.4.0): List display

## Configuration Files

- `build.gradle.kts`: Root project config (Spotless, Java 21, custom tasks)
- `app/build.gradle.kts`: App module config (Android SDK 36, dependencies, detekt, gradlegrader)
- `app/config/detekt/detekt.yml`: Detekt static analysis rules
- `grade.yaml`: Current checkpoint selection for grading
- `settings.gradle.kts`: Project structure and repositories