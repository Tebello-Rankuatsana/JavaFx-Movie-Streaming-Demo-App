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

## Settings and Profile

The Settings screen includes playback, notifications, and privacy options.
Playback lets users select a preferred display quality from 360p through 2K
(1440p). The selected preference is kept for the current app session; actual
trailer quality depends on the video and playback provider.

The Profile screen lets users edit their name, email address, and preferred
language, and cancel the displayed Premium membership after confirmation.
These profile and membership changes are stored only in memory for the current
app session. This demo does not create or connect to real user accounts or a
billing service, so users cannot delete an actual account from the app.


