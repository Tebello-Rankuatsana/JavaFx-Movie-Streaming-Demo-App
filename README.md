# StreamFlix: JavaFX Movie and TV Streaming Demo

StreamFlix is a demo video streaming application built with JavaFX. It offers a
Netflix-style catalog of movies and TV series, searchable titles, metadata from
The Movie Database (TMDB), and in-app trailer playback streamed directly from
YouTube. No video files are stored in the project.

This project was developed as a group assignment for the Diploma in Multimedia
with Software Engineering (BSc in Software Engineering and Multimedia),
Faculty of Information and Communication Technology.

## Contents

1. [Requirements](#requirements)
2. [Running the Application](#running-the-application)
3. [Assignment Requirements and Implementation](#assignment-requirements-and-implementation)
4. [Using the Application](#using-the-application)
5. [TMDB and Trailer Playback](#tmdb-and-trailer-playback)
6. [Project Structure](#project-structure)
7. [Poster Images](#poster-images)
8. [Known Limitations](#known-limitations)
9. [Team](#team)

## Requirements

- JDK 11 or later
- Apache Maven 3.6 or later
- An internet connection (for trailers and TMDB metadata)
- A TMDB API Key (v3) or API Read Access Token (v4)

Maven downloads the JavaFX and Gson dependencies for your platform.

## Running the Application

From the repository root, run:

```powershell
mvn clean javafx:run
```

To build the project without launching the desktop application:

```powershell
mvn clean package
```

## Assignment Requirements and Implementation

| Requirement | Implementation |
| --- | --- |
| At least three categories with at least eight videos each | Movies and TV series are each organized into Action, Adventure, and Sci-Fi, with eight titles per genre (24 movies and 24 TV series). |
| Categories properly labeled and clearly visible | Every row has a bold heading, for example "Action TV Shows" or "Popular in Adventure". |
| Thumbnails with a title for each video | Each card shows a poster image, the title, and the genre. |
| Visually appealing hover effect on thumbnails | On hover a card scales up, the poster brightens, a cyan glow appears, and a "Play" overlay fades in. |
| Five thumbnails per category with scrolling to the rest | Every row shows exactly five cards and scrolls horizontally to reveal the remaining three. Card size is bound to the window width, so five cards fill the row at any window size. |
| Trailer shown from YouTube when a title or thumbnail is clicked | Selecting a thumbnail or a search result opens the player and loads the YouTube trailer straight away. |
| Trailer played directly from YouTube, nothing stored locally | The trailer is embedded from YouTube inside a JavaFX `WebView`. No video assets exist in the project. |
| Play and pause controls | A custom play/pause control drives the embedded player. |
| Custom animation while the video loads | A loading animation is shown in the player while the trailer is looked up and loaded. |
| No buttons from the `Button` class | All buttons are instances of a custom `CustomButton` class that extends `Label` and is built from text and/or SVG icons. |
| Sound effects | Sound effects play on hovering over thumbnails and on clicking buttons and sections. |

## Using the Application

### Browsing

- **Home** shows horizontally scrolling movie shelves (Trending Now, Popular
  in Adventure, Recently Added in Sci-Fi) and separate Action, Adventure, and
  Sci-Fi TV series shelves.
- **Movies** and **TV Shows** group titles by genre in scrolling rows.
- **Categories** lists every genre for movies and TV series in separate
  sections.
- Use the horizontal scroll bar beneath a row to reach the titles beyond the
  first five.

### Watching a trailer

Select a thumbnail, a search result, or the **Play** button in the watchlist to
open the player. The player provides:

- play and pause control
- fullscreen toggle
- an "open in browser" button, for the cases where YouTube does not play in the
  embedded browser
- a **Details** button that closes the player and opens the details page

### Details page

The details page shows the overview, cast, director or creators, languages,
genres, release or first-air date, runtime or seasons and episodes, and the TMDB
rating. From here a title can be added to or removed from the watchlist.

### Search

Use the search bar at the top of the Home screen to search the titles included
in the app. Search matches partial titles and genres without regard to
capitalization. Results identify each item as a movie or a TV show and can be
scrolled when the list is long. Selecting a result opens its trailer. If there
are no matches, the panel displays "Not available in this app." Search uses the
local catalog only and does not search the full TMDB library.

### Watchlist

Titles added from the details page appear on the Watchlist screen, where they
can be played or removed. The watchlist is kept in memory for the current
session.

### Settings and profile

- **Settings** includes playback, notifications, and privacy options. Playback
  lets users choose a preferred display quality from 360p through 2K (1440p).
  The preference is kept for the current session.
- **Profile** lets users edit their name, email address, and preferred language,
  and cancel the displayed Premium membership after confirmation.
- The notification button on the Home screen opens an Updates and Terms screen
  with service notices and links to the TMDB and YouTube terms.

Profile and membership changes are stored only in memory for the current
session. The demo does not create or connect to real accounts or a billing
service.

## TMDB and Trailer Playback

Titles in the app are matched to TMDB to retrieve their metadata and the ID of
their official YouTube trailer. The trailer itself is then streamed from YouTube
in the embedded player.

The first details or trailer request prompts for a TMDB API Key (v3) or API
Read Access Token (v4). The credential stays in memory for the current session
and is not saved, so it is requested again after the app restarts. Each user
should supply their own credential, and credentials must never be committed to
the repository.

Trailer lookup requires an internet connection. TMDB and YouTube are external
services and are subject to their own terms of use.

## Project Structure

```
src/main/java/streaming/
    App.java               Main application: screens, navigation, player,
                           TMDB client, and the CustomButton control
    SoundsAndButtons.java  Sound effects and custom interactive controls
src/main/resources/
    style.css              Application theme and component styles
    images/                Poster images and the application logo
pom.xml                    Maven build and dependency configuration
```

### Design notes

- **Custom controls.** `CustomButton` extends `Label` and reproduces button
  behavior (click, keyboard activation, pressed and disabled states, tooltips)
  without using the `Button` class.
- **Responsive layout.** Thumbnail size is held in a single property that is
  updated by a listener on the content area's width. Posters, clips, overlays,
  and rows are bound to it.
- **Consistent theme.** A dark navy and gold palette is defined in `style.css`
  and applied through style classes.
- **Background work.** TMDB requests run asynchronously with
  `CompletableFuture`, and results are applied on the JavaFX application thread.

## Poster Images

Poster files are loaded from the application resources:

- Movies: `src/main/resources/images/Movies/Actions/`,
  `src/main/resources/images/Movies/Adventure/`, and
  `src/main/resources/images/Movies/Sci-Fi/`
- TV shows: `src/main/resources/images/Tv Shows/Action/`,
  `src/main/resources/images/Tv Shows/Adventure/`, and
  `src/main/resources/images/Tv Shows/Sci-Fi/`

The image paths and filenames are defined in the catalog in
`src/main/java/streaming/App.java`. Keep those names in sync when replacing or
adding poster files.

## Known Limitations

- A TMDB credential is required for trailers and details and must be entered
  each time the app starts.
- Embedded YouTube playback depends on the JavaFX `WebView` engine. If a trailer
  does not play in the app, use the browser button in the player.
- The profile, membership, and playback quality settings are demonstrations
  only and are not persisted. The quality preference does not change the
  quality of the embedded trailer.
- The ratings shown on thumbnails are placeholder values. Accurate ratings are
  shown on the details page when available from TMDB.
- Standard JavaFX dialogs are used for the TMDB credential prompt and for
  confirmations.

## Team

| Member | Responsibilities |
| --- | --- |
| Pitso Matlali| UI Design |
| Katleho Ramohlabi | Movie Categories and Thumbnails |
| Tsiu Sehloho | Video Player and Tmdb Integration |
| Katleho Serutla | Animations and Effects |
| Rorisang Molapo | Custom Controls and Sound effects |
| Lehakoe Panyane | UI Design |
| Tebello Rankuatsana | Responsiveness, Custom Button creation, Application integration and Code Structure, Testing, Github and Documentation |

Lecturers: Mr Thokoana and Mr Takalimane
