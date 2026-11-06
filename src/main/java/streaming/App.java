package streaming;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // --- Theme Constants ---
    private static final double PLAYER_WINDOW_WIDTH = 800;
    private static final double PLAYER_WINDOW_HEIGHT = 520;
    private static final String TEAL_BG = "#0B1A26";
    private static final String TEAL_PANEL = "#132A38";
    private static final String CYAN_ACCENT = "#00D2FF";
    private static final String GOLD_ACCENT = "#FCA311";
    private static final String TEXT_MUTED = "#8B9EB0";

    // --- Layout Managers ---
    private BorderPane root;
    private StackPane contentArea;
    private VBox sidebar;

    // --- Screens ---
    private VBox homeScreen;
    private VBox movieDetailsScreen;
    private VBox movieDetailsContainer;
    private VBox watchlistScreen;
    private VBox categoriesScreen;
    private VBox settingsScreen;
    private VBox tvShowsScreen;
    private VBox moviesScreen;
    private BorderPane mediaPlayerScreen;
    // --- Data Models ---
    private final List<Movie> allMovies = new ArrayList<>();
    private final List<Movie> watchlist = new ArrayList<>();
    private final List<Movie> trendingMovies = new ArrayList<>();
    private final List<Movie> recentlyAdded = new ArrayList<>();
    private final List<Movie> popularMovies = new ArrayList<>();

    // --- Media Player State ---
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage mainStage;
    private StackPane videoContainer;
    private Region playerScrim;
    private Label videoPlaceholder;
    private Label playbackErrorLabel;
    private Label playerTitleLabel;
    private String previousWindowTitle;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Button playPauseButton;
    private Button muteButton;
    private Button fullscreenButton;
    private double previousVolume = 0.5;
    private boolean muted;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initData();
        buildUI(primaryStage);
    }

    private void initData() {
        addMovie("The Dark Knight", "Action", "/images/Actions/The dark night.jpg");
        addMovie("The Beekeeper", "Action", "/images/Actions/The Beekeeper.jpg");
        addMovie("John Wick 4", "Action", "/images/Actions/John Wick 4.jpg");
        addMovie("Inception", "Action", "/images/Actions/inception.jpg");
        addMovie("Hitman", "Action", "/images/Actions/hitman.jpg");
        addMovie("Hell or High Water", "Action", "/images/Actions/Hell or High Water.jpg");
        addMovie("Extraction", "Action", "/images/Actions/Extraction.jpg");
        addMovie("Central Intelligence", "Action", "/images/Actions/Central Intelligence.jpg");

        addMovie("Uncharted", "Adventure", "/images/Adventure/uncharted.jpg");
        addMovie("Trazen", "Adventure", "/images/Adventure/trazen.jpg");
        addMovie("The Tomorrow War", "Adventure", "/images/Adventure/The Tomorrow War.jpg");
        addMovie("Shang-Chi and the Legend of the Ten Rings", "Adventure", "/images/Adventure/SHANG-CHI AND THE LEGEND OF THE TEN RINGS.jpg");
        addMovie("Mission: Impossible", "Adventure", "/images/Adventure/mission impossible.jpg");
        addMovie("Jurassic Park", "Adventure", "/images/Adventure/jurassic park.jpg");
        addMovie("Jumanji", "Adventure", "/images/Adventure/jumanji.jpg");
        addMovie("Dawn of the Planet of the Apes", "Adventure", "/images/Adventure/Dawn of the planet of apes.jpg");

        addMovie("Tron: Ares", "Sci-Fi", "/images/Sci-fi/Tron Ares.jpg");
        addMovie("The Maze Runner: The Death Cure", "Sci-Fi", "/images/Sci-fi/The Maze Runner 3 The Death Cure.jpg");
        addMovie("Tenet", "Sci-Fi", "/images/Sci-fi/Tenet.jpg");
        addMovie("RoboCop", "Sci-Fi", "/images/Sci-fi/Robocop.jpg");
        addMovie("Pacific Rim", "Sci-Fi", "/images/Sci-fi/pacific rim.jpg");
        addMovie("Oblivion", "Sci-Fi", "/images/Sci-fi/oblivion.jpg");
        addMovie("Elysium", "Sci-Fi", "/images/Sci-fi/Movie Elysium.jpg");
        addMovie("Avengers: Endgame", "Sci-Fi", "/images/Sci-fi/avengers endgame.jpg");

        trendingMovies.addAll(categoryMovies("Action"));
        popularMovies.addAll(categoryMovies("Adventure"));
        recentlyAdded.addAll(categoryMovies("Sci-Fi"));
    }

    private void addMovie(String title, String genre, String imagePath) {
        allMovies.add(new Movie(title, genre, "Feature", "PG-13", "", imagePath, 8.0));
    }

    private List<Movie> categoryMovies(String category) {
        List<Movie> movies = new ArrayList<>();
        for (Movie movie : allMovies) {
            if (movie.genre.equals(category)) movies.add(movie);
        }
        return movies;
    }

    private void buildUI(Stage stage) {
        mainStage = stage;
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Content Area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);

        // Initialize Screens
        createHomeScreen();
        createMovieDetailsScreen();
        createWatchlistScreen();
        createCategoriesScreen();
        createSettingsScreen();
        createTVShowsScreen();
        createMoviesScreen();
        createMediaPlayerScreen();

        showHomeScreen();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth = Math.min(1440, screenBounds.getWidth() * 0.9);
        double windowHeight = Math.min(900, screenBounds.getHeight() * 0.9);
        Scene scene = new Scene(root, windowWidth, windowHeight);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("StreamFlix - Premium Streaming");
        stage.setScene(scene);
        stage.setMinWidth(Math.min(800, windowWidth));
        stage.setMinHeight(Math.min(500, windowHeight));
        stage.show();
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private VBox createSidebar() {
        VBox side = new VBox(10);
        side.getStyleClass().add("sidebar");
        side.setPrefWidth(250);
        side.setMinWidth(250);
        side.setMaxWidth(250);
        side.setPadding(new Insets(30, 20, 30, 20));

        // Logo Area
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 35, 5));

        StackPane logoIcon = new StackPane();
        Circle c1 = new Circle(12, Color.web(CYAN_ACCENT));
        Circle c2 = new Circle(7, Color.web(TEAL_PANEL));
        logoIcon.getChildren().addAll(c1, c2);

        Text logo = new Text("STREAMFLIX");
        logo.getStyleClass().add("logo-text");

        headerBox.getChildren().addAll(logoIcon, logo);

        // Navigation Links
        VBox navLinks = new VBox(6);

        Button btnHome = createNavButton("Home");
        Button btnMovies = createNavButton("Movies");
        Button btnTVShows = createNavButton("TV Shows");
        Button btnCategories = createNavButton("Categories");
        Button btnWatchlist = createNavButton("Watchlist");

        updateNavButtonStyle(btnHome, true);

        btnHome.setOnAction(e -> { showHomeScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist); updateNavButtonStyle(btnHome, true); });
        btnMovies.setOnAction(e -> { showMoviesScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist); updateNavButtonStyle(btnMovies, true); });
        btnTVShows.setOnAction(e -> { showTVShowsScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist); updateNavButtonStyle(btnTVShows, true); });
        btnCategories.setOnAction(e -> { showCategoriesScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist); updateNavButtonStyle(btnCategories, true); });
        btnWatchlist.setOnAction(e -> { showWatchlistScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist); updateNavButtonStyle(btnWatchlist, true); });

        navLinks.getChildren().addAll(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Bottom Links & User Profile
        Button btnSettings = createNavButton("Settings");
        btnSettings.setOnAction(e -> { showSettingsScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnSettings, true); });

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(15, 12, 15, 12));
        userBox.getStyleClass().add("user-box");

        Circle avatar = new Circle(18, Color.web(CYAN_ACCENT));
        Text avatarInitials = new Text("A");
        avatarInitials.setFill(Color.web(TEAL_BG));
        avatarInitials.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        StackPane avatarPane = new StackPane(avatar, avatarInitials);

        VBox userInfo = new VBox(3);
        Text userName = new Text("Alexander");
        userName.getStyleClass().add("user-name");
        Text userRole = new Text("Premium Member");
        userRole.getStyleClass().add("user-role");
        userInfo.getChildren().addAll(userName, userRole);

        userBox.getChildren().addAll(avatarPane, userInfo);

        side.getChildren().addAll(headerBox, navLinks, spacer, btnSettings, userBox);
        return side;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setPrefHeight(45);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void updateNavButtonStyle(Button btn, boolean active) {
        btn.getStyleClass().removeAll("nav-button", "nav-button-active");
        btn.getStyleClass().add(active ? "nav-button-active" : "nav-button");
    }

    private void resetNavs(Button... buttons) {
        for (Button b : buttons) {
            b.getStyleClass().remove("nav-button-active");
            if (!b.getStyleClass().contains("nav-button")) {
                b.getStyleClass().add("nav-button");
            }
        }
    }

    // =========================================================================
    // TOP BAR (Search, Profile)
    // =========================================================================
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(25, 50, 20, 50));

        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Text searchIcon = new Text("Q");
        searchIcon.getStyleClass().add("search-icon");

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search movies, shows...");
        searchField.setPrefWidth(260);
        searchField.setFocusTraversable(false);

        searchBox.getChildren().addAll(searchIcon, searchField);

        Button notifBtn = new Button("!");
        notifBtn.getStyleClass().add("notification-button");

        Button moviesNavBtn = new Button("Movies v");
        moviesNavBtn.getStyleClass().add("top-nav-pill");

        topBar.getChildren().addAll(moviesNavBtn, searchBox, notifBtn);
        return topBar;
    }

    // =========================================================================
    // HOME SCREEN
    // =========================================================================
    private void createHomeScreen() {
        homeScreen = new VBox(0);
        homeScreen.getStyleClass().add("screen");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox contentBox = new VBox(30);

        contentBox.getChildren().add(createTopBar());
        contentBox.getChildren().add(createHeroSection());
        contentBox.getChildren().add(createCategoriesPillRow());

        VBox trendingRow = createMovieRow("Action", trendingMovies);
        VBox popularRow = createMovieRow("Adventure", popularMovies);
        VBox recentlyRow = createMovieRow("Sci-Fi", recentlyAdded);

        contentBox.getChildren().addAll(trendingRow, popularRow, recentlyRow);
        VBox.setMargin(recentlyRow, new Insets(0, 0, 60, 0));

        scrollPane.setContent(contentBox);
        homeScreen.getChildren().add(scrollPane);
    }

    private StackPane createHeroSection() {
        StackPane hero = new StackPane();
        hero.getStyleClass().add("hero");
        hero.setMinHeight(450);
        hero.setMaxHeight(520);

        ImageView heroImage = new ImageView(loadPoster(allMovies.get(0).imageUrl));
        heroImage.setPreserveRatio(false);
        heroImage.fitWidthProperty().bind(hero.widthProperty());
        heroImage.fitHeightProperty().bind(hero.heightProperty());

        Rectangle overlay = new Rectangle();
        overlay.widthProperty().bind(hero.widthProperty());
        overlay.heightProperty().bind(hero.heightProperty());

        // Blends perfectly into the TEAL_BG from bottom to top
        overlay.setFill(new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(TEAL_BG)),
                new Stop(0.4, Color.web(TEAL_BG, 0.85)),
                new Stop(0.9, Color.web(TEAL_BG, 0.1))));

        VBox heroContent = new VBox(15);
        heroContent.setAlignment(Pos.BOTTOM_LEFT);
        heroContent.setPadding(new Insets(40, 50, 50, 50));
        heroContent.setMaxWidth(700);
        StackPane.setAlignment(heroContent, Pos.BOTTOM_LEFT);

        HBox badge = new HBox(10);
        badge.setAlignment(Pos.CENTER_LEFT);
        Label featuredLabel = new Label("NEW");
        featuredLabel.getStyleClass().add("badge-gold");
        badge.getChildren().add(featuredLabel);

        Text heroTitle = new Text("The Dark Knight");
        heroTitle.getStyleClass().add("hero-title");

        HBox metaInfo = new HBox(15);
        metaInfo.setAlignment(Pos.CENTER_LEFT);
        Text metaText = new Text("Action  |  Feature  |  8.0");
        metaText.getStyleClass().add("hero-meta");
        metaInfo.getChildren().add(metaText);

        Text heroDesc = new Text("Explore the Action, Adventure, and Sci-Fi collections, with eight poster titles in each category.");
        heroDesc.getStyleClass().add("hero-description");
        heroDesc.setWrappingWidth(600);

        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button playBtn = createPrimaryButton("Play Now");
        playBtn.setOnAction(e -> showMovieDetails(allMovies.get(0)));

        Button watchlistBtn = createSecondaryButton("+ Watchlist");
        watchlistBtn.setOnAction(e -> {
            if (!watchlist.contains(allMovies.get(0))) {
                watchlist.add(allMovies.get(0));
                watchlistBtn.setText("Added");
            }
        });

        buttonBox.getChildren().addAll(playBtn, watchlistBtn);
        heroContent.getChildren().addAll(badge, heroTitle, metaInfo, heroDesc, buttonBox);

        hero.getChildren().addAll(heroImage, overlay, heroContent);
        return hero;
    }

    private VBox createCategoriesPillRow() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(0, 50, 10, 50));

        Text title = new Text("Select Categories");
        title.getStyleClass().add("section-title");

        HBox pills = new HBox(12);
        String[] categories = {"Action", "Adventure", "Sci-Fi"};

        for (int i = 0; i < categories.length; i++) {
            Button pill = new Button(categories[i]);
            pill.getStyleClass().add("category-pill");
            if (i == 0) pill.getStyleClass().add("category-pill-active");

            pill.setOnAction(e -> {
                for (javafx.scene.Node node : pills.getChildren()) {
                    node.getStyleClass().remove("category-pill-active");
                }
                pill.getStyleClass().add("category-pill-active");
            });

            pills.getChildren().add(pill);
        }

        section.getChildren().addAll(title, pills);
        return section;
    }

    private VBox createMovieRow(String title, List<Movie> movies) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(0, 50, 0, 50));

        Text titleText = new Text(title);
        titleText.getStyleClass().add("section-title");

        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);

        HBox cardBox = new HBox(20);
        cardBox.setPadding(new Insets(10, 0, 20, 0));

        for (Movie movie : movies) {
            cardBox.getChildren().add(createMovieCard(movie));
        }

        scroll.setContent(cardBox);
        section.getChildren().addAll(titleText, scroll);
        return section;
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(180);
        card.setMinWidth(180);
        card.setMaxWidth(180);

        StackPane posterPane = new StackPane();
        posterPane.getStyleClass().add("poster-frame");
        posterPane.setPrefWidth(180);
        posterPane.setPrefHeight(270);
        posterPane.setMinWidth(180);
        posterPane.setMinHeight(270);

        ImageView posterImage = new ImageView(loadPoster(movie.imageUrl));
        posterImage.setFitWidth(180);
        posterImage.setFitHeight(270);
        posterImage.setPreserveRatio(true);

        // Strict Clipping
        Rectangle clip = new Rectangle(180, 270);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        posterPane.setClip(clip);

        Label ratingBadge = new Label(String.valueOf(movie.ratingScore));
        ratingBadge.getStyleClass().add("rating-badge");
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(10));

        StackPane playOverlay = new StackPane();
        playOverlay.getStyleClass().add("play-overlay");
        playOverlay.setMaxSize(180, 270);
        playOverlay.setOpacity(0);

        Text playIcon = new Text("Play");
        playIcon.getStyleClass().add("play-icon");
        playOverlay.getChildren().add(playIcon);

        posterPane.getChildren().addAll(posterImage, ratingBadge, playOverlay);

        Text movieTitle = new Text(movie.title);
        movieTitle.getStyleClass().add("movie-title");
        movieTitle.setWrappingWidth(180);

        Text movieGenre = new Text(movie.genre);
        movieGenre.getStyleClass().add("movie-genre");

        card.getChildren().addAll(posterPane, movieTitle, movieGenre);

        // Hover Animations
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), card);
        scaleIn.setToX(1.05); scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), playOverlay);
        fadeIn.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), playOverlay);
        fadeOut.setToValue(0);

        card.setOnMouseEntered(e -> {
            scaleIn.play(); fadeIn.play();
            card.setEffect(new DropShadow(20, Color.web(CYAN_ACCENT, 0.3)));
            card.setViewOrder(-1);
        });
        card.setOnMouseExited(e -> {
            scaleOut.play(); fadeOut.play();
            card.setEffect(null);
            card.setViewOrder(0);
        });
        card.setOnMouseClicked(e -> showMovieDetails(movie));

        return card;
    }

    // =========================================================================
    // MEDIA PLAYER SCREEN
    // =========================================================================
    private void createMediaPlayerScreen() {
        // Keep playback inside the app's existing content area and scene.
        mediaPlayerScreen = new BorderPane();
        mediaPlayerScreen.getStyleClass().add("player-screen");
        playerScrim = new Region();
        playerScrim.getStyleClass().add("player-scrim");

        videoContainer = new StackPane();
        videoContainer.getStyleClass().add("video-container");

        videoPlaceholder = new Label("Choose a video to start playback");
        videoPlaceholder.getStyleClass().add("video-placeholder");

        playbackErrorLabel = new Label();
        playbackErrorLabel.getStyleClass().add("video-error");
        playbackErrorLabel.setWrapText(true);
        playbackErrorLabel.setMaxWidth(520);
        playbackErrorLabel.setVisible(false);

        // Center the transport controls over the video while keeping the timeline below.
        Button skipBackButton = createPlayerButton(
                "M5 5h2v14H5z M19 5v14l-10-7z",
                "player-skip-button", "Skip back 5 seconds");
        skipBackButton.setOnAction(e -> seekBy(-5));

        playPauseButton = createPlayerButton(
                "M8 5v14l11-7z", "player-play-button", "Play");
        playPauseButton.setOnAction(e -> togglePlayback());

        Button skipForwardButton = createPlayerButton(
                "M17 5h2v14h-2z M5 5v14l10-7z",
                "player-skip-button", "Skip forward 5 seconds");
        skipForwardButton.setOnAction(e -> seekBy(5));

        HBox transportControls = new HBox(38, skipBackButton, playPauseButton, skipForwardButton);
        transportControls.setAlignment(Pos.CENTER);
        transportControls.getStyleClass().add("player-transport-controls");

        progressSlider = new Slider(0, 100, 0);
        progressSlider.getStyleClass().add("player-progress-slider");
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        progressSlider.setOnMouseReleased(e -> seekToSliderPosition());
        progressSlider.setOnKeyReleased(e -> seekToSliderPosition());

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.getStyleClass().add("player-time-label");
        HBox progressRow = new HBox(14, progressSlider, timeLabel);
        progressRow.setAlignment(Pos.CENTER);

        muteButton = createPlayerButton(
                "M3 9v6h4l5 5V4L7 9H3z M16 9.5a4 4 0 0 1 0 5 M14 3.23v2.06a7 7 0 0 1 0 13.42v2.06a9 9 0 0 0 0-17.54z",
                "player-volume-button", "Mute");
        muteButton.setOnAction(e -> toggleMute());

        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.getStyleClass().add("player-volume-slider");
        volumeSlider.setPrefWidth(125);
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100);
            }
            if (newValue.doubleValue() > 0) {
                previousVolume = newValue.doubleValue() / 100;
                muted = false;
            } else {
                muted = true;
            }
            updateMuteIcon();
        });

        HBox volumeControls = new HBox(10, muteButton, volumeSlider);
        volumeControls.setAlignment(Pos.CENTER_LEFT);

        Region controlsSpacer = new Region();
        HBox.setHgrow(controlsSpacer, Priority.ALWAYS);
        fullscreenButton = createPlayerButton(
                "M5 5h5v2H7v3H5z M14 5h5v5h-2V7h-3z M5 14h2v3h3v2H5z M17 14h2v5h-5v-2h3z",
                "player-volume-button", "Enter fullscreen");
        fullscreenButton.setOnAction(e -> toggleFullscreen());

        HBox bottomRow = new HBox(volumeControls, controlsSpacer, fullscreenButton);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // Anchor time, volume, and fullscreen controls to the bottom edge of the video.
        VBox playerControls = new VBox(12, progressRow, bottomRow);
        playerControls.getStyleClass().add("player-controls");
        playerControls.setMaxWidth(Double.MAX_VALUE);

        videoContainer.getChildren().addAll(videoPlaceholder, playbackErrorLabel, transportControls);
        StackPane.setAlignment(videoPlaceholder, Pos.CENTER);
        StackPane.setAlignment(playbackErrorLabel, Pos.TOP_CENTER);
        StackPane.setMargin(playbackErrorLabel, new Insets(18));
        StackPane.setAlignment(transportControls, Pos.CENTER);

        // Return to the exact screen that was visible before the player opened.
        Button backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", "Back");
        backButton.setOnAction(e -> closePlayer());
        playerTitleLabel = new Label("Now Playing");
        playerTitleLabel.getStyleClass().add("player-title");
        HBox playerHeader = new HBox(18, backButton, playerTitleLabel);
        playerHeader.setAlignment(Pos.CENTER_LEFT);
        playerHeader.getStyleClass().add("player-header");

        mediaPlayerScreen.setTop(playerHeader);
        mediaPlayerScreen.setCenter(videoContainer);
        mediaPlayerScreen.setBottom(playerControls);
        mediaPlayerScreen.setMinSize(0, 0);
        mediaPlayerScreen.setMaxSize(PLAYER_WINDOW_WIDTH, PLAYER_WINDOW_HEIGHT);
        mediaPlayerScreen.prefWidthProperty().bind(Bindings.min(
                contentArea.widthProperty(), PLAYER_WINDOW_WIDTH));
        mediaPlayerScreen.prefHeightProperty().bind(Bindings.min(
                contentArea.heightProperty(), PLAYER_WINDOW_HEIGHT));

        // Fullscreen applies to the primary app window, including this in-scene player.
        mainStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (isFullScreen) {
                mediaPlayerScreen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                mediaPlayerScreen.prefWidthProperty().bind(contentArea.widthProperty());
                mediaPlayerScreen.prefHeightProperty().bind(contentArea.heightProperty());
            } else {
                mediaPlayerScreen.setMaxSize(PLAYER_WINDOW_WIDTH, PLAYER_WINDOW_HEIGHT);
                mediaPlayerScreen.prefWidthProperty().bind(Bindings.min(
                        contentArea.widthProperty(), PLAYER_WINDOW_WIDTH));
                mediaPlayerScreen.prefHeightProperty().bind(Bindings.min(
                        contentArea.heightProperty(), PLAYER_WINDOW_HEIGHT));
            }
            fullscreenButton.setGraphic(createPlayerIcon(isFullScreen
                    ? "M5 5h2v3h3v2H5z M17 5v5h-5V8h3V5z M5 14h5v5H8v-3H5z M14 14h5v5h-2v-3h-3z"
                    : "M5 5h5v2H7v3H5z M14 5h5v5h-2V7h-3z M5 14h2v3h3v2H5z M17 14h2v5h-5v-2h3z"));
            fullscreenButton.setTooltip(new Tooltip(isFullScreen ? "Exit fullscreen" : "Enter fullscreen"));
            fullscreenButton.setAccessibleText(isFullScreen ? "Exit fullscreen" : "Enter fullscreen");
        });
    }

    private Button createPlayerButton(String iconPath, String styleClass, String tooltip) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        button.setGraphic(createPlayerIcon(iconPath));
        button.setTooltip(new Tooltip(tooltip));
        button.setAccessibleText(tooltip);
        return button;
    }

    private StackPane createPlayerIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.WHITE);
        icon.setMouseTransparent(true);
        double iconWidth = icon.getBoundsInLocal().getWidth();
        double iconHeight = icon.getBoundsInLocal().getHeight();
        double scale = 20 / Math.max(iconWidth, iconHeight);
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        StackPane graphic = new StackPane(icon);
        graphic.setMinSize(24, 24);
        graphic.setPrefSize(24, 24);
        graphic.setMaxSize(24, 24);
        graphic.setMouseTransparent(true);
        return graphic;
    }

    private void openPlayer(Movie movie) {
        // Layer the compact player over the current view instead of replacing app content.
        previousWindowTitle = mainStage.getTitle();
        disposeMediaPlayer();
        playerTitleLabel.setText(movie.title);
        videoPlaceholder.setText("Choose a video file to play " + movie.title);
        videoPlaceholder.setVisible(true);
        playbackErrorLabel.setVisible(false);
        timeLabel.setText("00:00 / 00:00");
        progressSlider.setValue(0);
        StackPane.setAlignment(playerScrim, Pos.CENTER);
        StackPane.setAlignment(mediaPlayerScreen, Pos.CENTER);
        contentArea.getChildren().addAll(playerScrim, mediaPlayerScreen);
        mainStage.setTitle("StreamFlix - " + movie.title);

        File defaultVideo = new File("C:\\videos\\" + movie.title.replace(" ", "_") + ".mp4");
        if (defaultVideo.isFile()) {
            loadVideo(defaultVideo);
        } else {
            videoPlaceholder.setText("Choose a video file to play " + movie.title);
            Platform.runLater(() -> chooseVideoFile());
        }
    }

    private void chooseVideoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a video");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Video files", "*.mp4", "*.m4v", "*.mov", "*.mkv"));
        File videoFile = fileChooser.showOpenDialog(mainStage);
        if (videoFile != null) {
            loadVideo(videoFile);
        }
    }

    private void loadVideo(File file) {
        try {
            disposeMediaPlayer();
            playbackErrorLabel.setVisible(false);
            videoPlaceholder.setVisible(true);
            timeLabel.setText("00:00 / 00:00");
            progressSlider.setValue(0);

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
            mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
            // Add video beneath the placeholder and controls, which remain layered above it.
            videoContainer.getChildren().add(0, mediaView);

            MediaPlayer activePlayer = mediaPlayer;
            activePlayer.volumeProperty().set(volumeSlider.getValue() / 100);
            // Keep the seek bar and displayed time synchronized with the active media.
            activePlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                Duration total = activePlayer.getTotalDuration();
                if (!progressSlider.isValueChanging() && isKnownDuration(total)) {
                    progressSlider.setValue(newTime.toMillis() / total.toMillis() * 100);
                }
                updateTimeLabel(newTime, total);
            });
            activePlayer.totalDurationProperty().addListener((obs, oldDuration, newDuration) -> {
                updateTimeLabel(activePlayer.getCurrentTime(), newDuration);
            });
            activePlayer.setOnReady(() -> {
                videoPlaceholder.setVisible(false);
                activePlayer.play();
                updatePlayPauseIcon();
            });
            activePlayer.setOnError(() -> showPlaybackError(activePlayer.getError() == null
                    ? "Unable to play this video." : activePlayer.getError().getMessage()));
            media.setOnError(() -> showPlaybackError(media.getError() == null
                    ? "Unable to load this video." : media.getError().getMessage()));
            activePlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> updatePlayPauseIcon());
        } catch (Exception e) {
            showPlaybackError("Unable to open video: " + e.getMessage());
        }
    }

    private void disposeMediaPlayer() {
        // Release the native media resources before leaving or replacing the player view.
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (mediaView != null) {
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            videoContainer.getChildren().remove(mediaView);
            mediaView = null;
        }
        if (playPauseButton != null) {
            updatePlayPauseIcon();
        }
    }

    private void closePlayer() {
        disposeMediaPlayer();
        if (mainStage.isFullScreen()) {
            mainStage.setFullScreen(false);
        }
        contentArea.getChildren().removeAll(mediaPlayerScreen, playerScrim);
        mainStage.setTitle(previousWindowTitle == null
                ? "StreamFlix - Premium Streaming" : previousWindowTitle);
    }

    private void showPlaybackError(String message) {
        playbackErrorLabel.setText(message == null || message.isEmpty()
                ? "Unable to play this video." : message);
        playbackErrorLabel.setVisible(true);
        videoPlaceholder.setVisible(false);
    }

    private void togglePlayback() {
        if (mediaPlayer == null) {
            chooseVideoFile();
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
        updatePlayPauseIcon();
    }

    private void updatePlayPauseIcon() {
        boolean playing = mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        playPauseButton.setGraphic(createPlayerIcon(playing
                ? "M6 5h4v14H6z M14 5h4v14h-4z"
                : "M8 5v14l11-7z"));
        playPauseButton.setTooltip(new Tooltip(playing ? "Pause" : "Play"));
        playPauseButton.setAccessibleText(playing ? "Pause" : "Play");
    }

    private void seekBy(double seconds) {
        if (mediaPlayer != null && isKnownDuration(mediaPlayer.getTotalDuration())) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            if (seconds < 0 && currentTime.toSeconds() <= Math.abs(seconds)) {
                return;
            }
            seekTo(currentTime.add(Duration.seconds(seconds)));
        }
    }

    private void seekToSliderPosition() {
        if (mediaPlayer != null && isKnownDuration(mediaPlayer.getTotalDuration())) {
            seekTo(mediaPlayer.getTotalDuration().multiply(progressSlider.getValue() / 100));
        }
    }

    private void seekTo(Duration position) {
        Duration total = mediaPlayer.getTotalDuration();
        double clampedMillis = Math.max(0, Math.min(position.toMillis(), total.toMillis()));
        mediaPlayer.seek(Duration.millis(clampedMillis));
    }

    private void toggleMute() {
        if (muted) {
            double restoredVolume = previousVolume > 0 ? previousVolume : 0.5;
            volumeSlider.setValue(restoredVolume * 100);
            muted = false;
        } else {
            previousVolume = volumeSlider.getValue() > 0 ? volumeSlider.getValue() / 100 : previousVolume;
            volumeSlider.setValue(0);
            muted = true;
        }
        updateMuteIcon();
    }

    private void updateMuteIcon() {
        muteButton.setGraphic(createPlayerIcon(muted
                ? "M3 9v6h4l5 5V4L7 9H3z M16 9l1.4-1.4 2.1 2.1 2.1-2.1L23 9l-2.1 2.1 2.1 2.1-1.4 1.4-2.1-2.1-2.1 2.1-1.4-1.4 2.1-2.1z"
                : "M3 9v6h4l5 5V4L7 9H3z M16 9.5a4 4 0 0 1 0 5 M14 3.23v2.06a7 7 0 0 1 0 13.42v2.06a9 9 0 0 0 0-17.54z"));
        muteButton.setTooltip(new Tooltip(muted ? "Unmute" : "Mute"));
        muteButton.setAccessibleText(muted ? "Unmute" : "Mute");
    }

    private void toggleFullscreen() {
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    private boolean isKnownDuration(Duration duration) {
        return duration != null && !duration.isUnknown() && !duration.isIndefinite()
                && duration.toMillis() > 0;
    }

    private void updateTimeLabel(Duration current, Duration total) {
        timeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown() || duration.isIndefinite()) {
            return "00:00";
        }
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // =========================================================================
    // MOVIE DETAILS SCREEN
    // =========================================================================
    private void createMovieDetailsScreen() {
        movieDetailsScreen = new VBox(0);
        movieDetailsScreen.getStyleClass().add("screen");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(30, 50, 60, 50));

        Button backBtn = createSecondaryButton("Back to Home");
        backBtn.setOnAction(e -> showHomeScreen());

        movieDetailsContainer = new VBox(20);

        content.getChildren().addAll(backBtn, movieDetailsContainer);
        scrollPane.setContent(content);
        movieDetailsScreen.getChildren().add(scrollPane);
    }

    private void showMovieDetails(Movie movie) {
        mainStage.setTitle("StreamFlix - " + movie.title);
        movieDetailsContainer.getChildren().clear();

        HBox mainLayout = new HBox(50);
        mainLayout.setAlignment(Pos.TOP_LEFT);

        ImageView posterImage = new ImageView(loadPoster(movie.imageUrl));
        posterImage.setFitWidth(320);
        posterImage.setFitHeight(480);
        posterImage.setPreserveRatio(false);
        Rectangle clip = new Rectangle(320, 480);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        posterImage.setClip(clip);

        VBox infoBox = new VBox(20);
        infoBox.setMaxWidth(800);

        Text title = new Text(movie.title);
        title.getStyleClass().add("detail-title");

        HBox metaRow = new HBox(20);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Text genre = new Text(movie.genre);
        genre.getStyleClass().add("detail-genre");
        Text duration = new Text(movie.duration);
        duration.getStyleClass().add("detail-meta");
        Text rating = new Text(movie.rating + "  |  " + movie.ratingScore + " / 10");
        rating.getStyleClass().add("detail-meta");
        metaRow.getChildren().addAll(genre, duration, rating);

        Text synopsis = new Text(movie.description);
        synopsis.getStyleClass().add("detail-synopsis");
        synopsis.setWrappingWidth(700);

        HBox buttonBox = new HBox(20);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button playBtn = createPrimaryButton("Play Movie");
        playBtn.setPrefWidth(200);
        playBtn.setOnAction(e -> openPlayer(movie));

        Button watchlistBtn = createSecondaryButton(watchlist.contains(movie) ? "In Watchlist" : "Add to Watchlist");
        watchlistBtn.setOnAction(e -> {
            if (!watchlist.contains(movie)) {
                watchlist.add(movie);
                watchlistBtn.setText("In Watchlist");
            } else {
                watchlist.remove(movie);
                watchlistBtn.setText("Add to Watchlist");
            }
        });

        buttonBox.getChildren().addAll(playBtn, watchlistBtn);
        infoBox.getChildren().addAll(title, metaRow, synopsis, buttonBox);

        mainLayout.getChildren().addAll(posterImage, infoBox);
        movieDetailsContainer.getChildren().add(mainLayout);

        contentArea.getChildren().setAll(movieDetailsScreen);
    }

    // =========================================================================
    // OTHER SCREENS
    // =========================================================================
    private void createWatchlistScreen() {
        watchlistScreen = new VBox(0);
        watchlistScreen.getStyleClass().add("screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 50, 60, 50));

        Text title = new Text("My Watchlist");
        title.getStyleClass().add("screen-title");

        VBox listContainer = new VBox(15);
        listContainer.setId("watchlistContainer");

        content.getChildren().addAll(title, listContainer);
        scrollPane.setContent(content);
        watchlistScreen.getChildren().add(scrollPane);
    }

    private void refreshWatchlistScreen() {
        VBox container = (VBox) watchlistScreen.lookup("#watchlistContainer");
        if (container == null) return;
        container.getChildren().clear();

        if (watchlist.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(100, 0, 100, 0));
            Text emptyText = new Text("Your watchlist is empty");
            emptyText.getStyleClass().add("empty-title");
            Text emptySub = new Text("Movies you add will appear here.");
            emptySub.getStyleClass().add("empty-subtitle");
            emptyBox.getChildren().addAll(emptyText, emptySub);
            container.getChildren().add(emptyBox);
        } else {
            for (Movie movie : new ArrayList<>(watchlist)) {
                HBox row = new HBox(25);
                row.getStyleClass().add("watchlist-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15, 20, 15, 20));

                ImageView miniPoster = new ImageView(loadPoster(movie.imageUrl));
                miniPoster.setFitWidth(70);
                miniPoster.setFitHeight(105);
                miniPoster.setPreserveRatio(true);
                Rectangle clip = new Rectangle(70, 105);
                clip.setArcWidth(10); clip.setArcHeight(10);
                miniPoster.setClip(clip);

                VBox info = new VBox(8);
                Text mTitle = new Text(movie.title);
                mTitle.getStyleClass().add("watchlist-title");
                Text mGenre = new Text(movie.genre + "  |  " + movie.duration);
                mGenre.getStyleClass().add("watchlist-genre");
                info.getChildren().addAll(mTitle, mGenre);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button playBtn = createPrimaryButton("Play");
                playBtn.setPrefWidth(100);
                playBtn.setPrefHeight(40);
                playBtn.setOnAction(e -> showMovieDetails(movie));

                Button removeBtn = new Button("Remove");
                removeBtn.getStyleClass().add("remove-button");
                removeBtn.setPrefWidth(100);
                removeBtn.setPrefHeight(40);
                removeBtn.setOnAction(e -> {
                    watchlist.remove(movie);
                    refreshWatchlistScreen();
                });

                row.getChildren().addAll(miniPoster, info, spacer, playBtn, removeBtn);
                container.getChildren().add(row);
            }
        }
    }

    private void createCategoriesScreen() {
        categoriesScreen = new VBox(0);
        categoriesScreen.getStyleClass().add("screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 50, 60, 50));
        Text title = new Text("Browse Categories");
        title.getStyleClass().add("screen-title");

        content.getChildren().add(title);
        content.getChildren().add(createMovieRow("Action", categoryMovies("Action")));
        content.getChildren().add(createMovieRow("Adventure", categoryMovies("Adventure")));
        content.getChildren().add(createMovieRow("Sci-Fi", categoryMovies("Sci-Fi")));
        scrollPane.setContent(content);
        categoriesScreen.getChildren().add(scrollPane);
    }

    private void createSettingsScreen() {
        settingsScreen = new VBox(0);
        settingsScreen.getStyleClass().add("screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 50, 60, 50));
        Text title = new Text("Settings");
        title.getStyleClass().add("screen-title");

        VBox settingsList = new VBox(15);
        settingsList.getChildren().add(createSettingItem("Account", "Manage your account details"));
        settingsList.getChildren().add(createSettingItem("Playback", "Video quality and playback settings"));
        settingsList.getChildren().add(createSettingItem("Notifications", "Configure notification preferences"));
        settingsList.getChildren().add(createSettingItem("Privacy", "Control your privacy settings"));

        content.getChildren().addAll(title, settingsList);
        scrollPane.setContent(content);
        settingsScreen.getChildren().add(scrollPane);
    }

    private HBox createSettingItem(String title, String subtitle) {
        HBox item = new HBox(15);
        item.getStyleClass().add("setting-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(20));

        VBox textBox = new VBox(5);
        Text titleText = new Text(title);
        titleText.getStyleClass().add("setting-title");
        Text subtitleText = new Text(subtitle);
        subtitleText.getStyleClass().add("setting-subtitle");
        textBox.getChildren().addAll(titleText, subtitleText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text arrow = new Text(">");
        arrow.getStyleClass().add("setting-arrow");

        item.getChildren().addAll(textBox, spacer, arrow);
        item.setOnMouseEntered(e -> item.getStyleClass().add("setting-item-hover"));
        item.setOnMouseExited(e -> item.getStyleClass().remove("setting-item-hover"));
        return item;
    }

    private void createTVShowsScreen() {
        tvShowsScreen = new VBox(0);
        tvShowsScreen.getStyleClass().add("screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 50, 60, 50));
        Text title = new Text("TV Shows");
        title.getStyleClass().add("screen-title");

        FlowPane grid = new FlowPane(20, 20);
        for (Movie movie : allMovies.subList(0, 6)) {
            grid.getChildren().add(createMovieCard(movie));
        }
        content.getChildren().addAll(title, grid);
        scrollPane.setContent(content);
        tvShowsScreen.getChildren().add(scrollPane);
    }

    private void createMoviesScreen() {
        moviesScreen = new VBox(0);
        moviesScreen.getStyleClass().add("screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 50, 60, 50));
        Text title = new Text("Movies");
        title.getStyleClass().add("screen-title");

        FlowPane grid = new FlowPane(20, 20);
        for (Movie movie : allMovies) {
            grid.getChildren().add(createMovieCard(movie));
        }
        content.getChildren().addAll(title, grid);
        scrollPane.setContent(content);
        moviesScreen.getChildren().add(scrollPane);
    }

    // =========================================================================
    // SCREEN SWITCHING & REUSABLE BUTTONS
    // =========================================================================
    private void showHomeScreen() { contentArea.getChildren().setAll(homeScreen); }
    private void showMoviesScreen() { contentArea.getChildren().setAll(moviesScreen); }
    private void showTVShowsScreen() { contentArea.getChildren().setAll(tvShowsScreen); }
    private void showCategoriesScreen() { contentArea.getChildren().setAll(categoriesScreen); }
    private void showWatchlistScreen() { refreshWatchlistScreen(); contentArea.getChildren().setAll(watchlistScreen); }
    private void showSettingsScreen() { contentArea.getChildren().setAll(settingsScreen); }

    private Image loadPoster(String resourcePath) {
        return new Image(getClass().getResource(resourcePath).toExternalForm(), true);
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("primary-button");
        btn.setPrefHeight(45);
        btn.setPadding(new Insets(0, 30, 0, 30));

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), btn);
        scaleIn.setToX(1.05); scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), btn);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            btn.getStyleClass().add("primary-button-hover");
            scaleIn.play();
            btn.setEffect(new DropShadow(15, Color.web(GOLD_ACCENT, 0.5)));
        });
        btn.setOnMouseExited(e -> {
            btn.getStyleClass().remove("primary-button-hover");
            scaleOut.play();
            btn.setEffect(null);
        });
        return btn;
    }

    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("secondary-button");
        btn.setPrefHeight(45);
        btn.setPadding(new Insets(0, 30, 0, 30));

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), btn);
        scaleIn.setToX(1.05); scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), btn);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            btn.getStyleClass().add("secondary-button-hover");
            scaleIn.play();
        });
        btn.setOnMouseExited(e -> {
            btn.getStyleClass().remove("secondary-button-hover");
            scaleOut.play();
        });
        return btn;
    }

    // =========================================================================
    // DATA CLASS
    // =========================================================================
    private static class Movie {
        String title, genre, duration, rating, description, imageUrl;
        double ratingScore;

        Movie(String title, String genre, String duration, String rating, String description, String imageUrl, double ratingScore) {
            this.title = title; this.genre = genre; this.duration = duration;
            this.rating = rating; this.description = description; this.imageUrl = imageUrl;
            this.ratingScore = ratingScore;
        }
    }
}
