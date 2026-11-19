# JavaFX Movie and TV Streaming Demo

This repository contains a JavaFX streaming demo with movie and TV series
catalogs, searchable titles, TMDB-powered details, and YouTube trailer playback.

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

## Catalog and Screens

- **Home** shows horizontally scrolling movie shelves and separate Action,
  Adventure, and Sci-Fi TV series shelves. Each TV genre shelf contains eight
  titles.
- **Movies** and **TV Shows** screens group their cards by genre and show up to
  five titles per genre.
- **Categories** keeps movie and TV series shelves separate, with horizontal
  rows for Action, Adventure, and Sci-Fi.
- Movie and TV cards use the same style and open a details page when selected.
  TV details include cast, creators, languages, first-air date, seasons and
  episodes, and rating when available from TMDB.

## Search

Use the search bar at the top of the Home screen to search the movies and TV
shows included in the app. Search matches partial titles and genres without
regard to capitalization. Results identify each item as a movie or TV show and
can be scrolled when the list is long. Select a result to open its details.
If there are no matches in the app's catalog, the results panel displays
“Not available in this app.” Search uses the local catalog and does not search
the full TMDB library.

## TMDB and Trailers

Selecting a movie or TV show opens a details page with metadata from TMDB.
The first details or trailer request prompts for a TMDB API Key (v3) or API
Read Access Token (v4). The credential stays in memory for the current app
session and is not saved; it will be requested again after restarting the app.
Each user should provide their own TMDB credential.

**Play Trailer** looks up the selected movie's or TV show's YouTube trailer and
plays it in the app. Trailer lookup requires an internet connection. If YouTube
does not play in the embedded browser, use the browser icon in the player.
TMDB and YouTube are external services and subject to their own terms.

## Poster Images

Poster files are loaded from the application resources:

- Movies: `src/main/resources/images/Movies/Actions/`,
  `src/main/resources/images/Movies/Adventure/`, and
  `src/main/resources/images/Movies/Sci-Fi/`
- TV shows: `src/main/resources/images/Tv Shows/Action/`,
  `src/main/resources/images/Tv Shows/Adventure/`, and
  `src/main/resources/images/Tv Shows/Sci-Fi/`

The image paths and filenames are defined in the application catalog in
`src/main/java/streaming/App.java`. Keep those names in sync when replacing
or adding poster files.

The Home screen's notification button opens a dedicated Updates & Terms screen
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
