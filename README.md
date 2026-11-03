# JavaFX Movie Streaming Demo

This repository contains a JavaFX movie-streaming demo.

## Requirements

- JDK 11 or later
- Apache Maven 3.6 or later

Maven downloads the JavaFX dependencies for your platform.

## Run

From the repository root, run:

```powershell
mvn clean javafx:run
```

To build the project without launching the desktop application:

```powershell
mvn clean package
```

The demo includes sample movie listings and a video player for a local video
file. Movie poster images are loaded from the internet.
