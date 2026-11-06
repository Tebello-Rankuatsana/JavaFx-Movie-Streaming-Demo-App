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

The demo includes sample movie listings. Selecting a movie opens a full-width
details page with cast, director, language, and other metadata from TMDB. The
first movie selection prompts for a TMDB API Key (v3) or API Read Access Token
(v4). The credential stays in memory for the current app session and is not
saved; it will be requested again after restarting the app. Each user should
provide their own TMDB credential. **Play Trailer** looks up the movie's
YouTube trailer and plays it in the app. Trailer lookup requires an internet
connection. If YouTube does not play in the embedded browser, use the browser
icon in the player. Movie poster images are loaded from the internet.
The home screen's notification button opens a dedicated Updates & Terms screen
with app notices and links to TMDB and YouTube terms; the profile control is
beside it in the top bar.
