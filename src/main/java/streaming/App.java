package streaming;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // --- Theme Constants ---
    private static final String TEAL_BG = "#0B1A26";
    private static final String TEAL_PANEL = "#132A38";
    private static final String CYAN_ACCENT = "#00D2FF";
    private static final String GOLD_ACCENT = "#FCA311";
    private static final String TEXT_MUTED = "#8B9EB0";

    // --- Layout Managers ---
    private BorderPane root;
    private StackPane contentArea;

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
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private Label videoTitleLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initData();
        buildUI(primaryStage);
    }

    private void initData() {
        allMovies.add(new Movie("Jungle Cruise", "Adventure, Fantasy", "2h 15m", "PG-13",
                "Dr. Lily Houghton enlists the aid of wisecracking skipper Frank Wolff to take her down the Amazon in his ramshackle boat.",
                "https://picsum.photos/seed/jungle/400/600", 8.7));
        allMovies.add(new Movie("Mortal Kombat 2", "Action, Fantasy", "1h 52m", "R",
                "The fate of the universe is at stake as Earthrealm's champions prepare for the ultimate tournament.",
                "https://picsum.photos/seed/mortal/400/600", 7.9));
        allMovies.add(new Movie("The Last Duel", "Drama, History", "2h 05m", "R",
                "A tale of betrayal and vengeance set against the brutality of 14th century France.",
                "https://picsum.photos/seed/duel/400/600", 8.2));
        allMovies.add(new Movie("Dune: Part Two", "Sci-Fi, Epic", "2h 45m", "PG-13",
                "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                "https://picsum.photos/seed/dune/400/600", 9.1));
        allMovies.add(new Movie("Oppenheimer", "Biography, Drama", "3h 00m", "R",
                "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                "https://picsum.photos/seed/oppenheimer/400/600", 8.9));
        allMovies.add(new Movie("The Batman", "Action, Crime", "2h 56m", "PG-13",
                "When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate.",
                "https://picsum.photos/seed/batman/400/600", 8.5));
        allMovies.add(new Movie("Interstellar", "Sci-Fi, Adventure", "2h 49m", "PG-13",
                "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                "https://picsum.photos/seed/interstellar/400/600", 9.3));
        allMovies.add(new Movie("Gladiator II", "Action, Drama", "2h 20m", "R",
                "Years after witnessing the death of the revered hero Maximus, Lucius is forced to enter the Colosseum.",
                "https://picsum.photos/seed/gladiator/400/600", 8.1));

        trendingMovies.addAll(allMovies.subList(0, 5));
        popularMovies.addAll(allMovies.subList(2, 7));
        recentlyAdded.addAll(allMovies.subList(3, 8));
        watchlist.add(allMovies.get(1));
        watchlist.add(allMovies.get(4));
    }

    private void buildUI(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Sidebar
        VBox sidebar = createSidebar();
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

        VBox trendingRow = createMovieRow("Trending Now", trendingMovies);
        VBox popularRow = createMovieRow("Popular Movies", popularMovies);
        VBox recentlyRow = createMovieRow("Recently Added", recentlyAdded);

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

        ImageView heroImage = new ImageView(new Image("https://picsum.photos/seed/jungle-hero/1600/800", true));
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

        Text heroTitle = new Text("Jungle Cruise");
        heroTitle.getStyleClass().add("hero-title");

        HBox metaInfo = new HBox(15);
        metaInfo.setAlignment(Pos.CENTER_LEFT);
        Text metaText = new Text("2021  |  2h 15m  |  Adventure, Fantasy  |  8.7");
        metaText.getStyleClass().add("hero-meta");
        metaInfo.getChildren().add(metaText);

        Text heroDesc = new Text("Dr. Lily Houghton enlists the aid of wisecracking skipper Frank Wolff to take her down the Amazon in his ramshackle boat. Together, they search for an ancient tree that holds the power to heal.");
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
        String[] categories = {"All", "Action", "Adventure", "Sci-Fi", "Comedy", "Drama", "Fantasy"};

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
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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
        posterPane.setPrefWidth(180);
        posterPane.setPrefHeight(260);

        ImageView posterImage = new ImageView(new Image(movie.imageUrl, true));
        posterImage.setFitWidth(180);
        posterImage.setFitHeight(260);
        posterImage.setPreserveRatio(false);

        // Strict Clipping
        Rectangle clip = new Rectangle(180, 260);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        posterImage.setClip(clip);

        Label ratingBadge = new Label(String.valueOf(movie.ratingScore));
        ratingBadge.getStyleClass().add("rating-badge");
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(10));

        StackPane playOverlay = new StackPane();
        playOverlay.getStyleClass().add("play-overlay");
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
        mediaPlayerScreen = new BorderPane();
        mediaPlayerScreen.getStyleClass().add("screen");
        mediaPlayerScreen.setStyle("-fx-background-color: " + TEAL_BG + ";");

        // Top Bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(20, 40, 20, 40));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("< Back");
        backBtn.getStyleClass().add("secondary-button");
        backBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
            showHomeScreen();
        });

        videoTitleLabel = new Label("Now Playing");
        videoTitleLabel.getStyleClass().add("section-title");
        videoTitleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        topBar.getChildren().addAll(backBtn, videoTitleLabel);

        // Video Area
        StackPane videoContainer = new StackPane();
        videoContainer.setStyle("-fx-background-color: #000000;");

        Text placeholder = new Text("Video Playback Area\n(Place a valid .mp4 file to play)");
        placeholder.setFill(Color.web(TEXT_MUTED));
        placeholder.setStyle("-fx-font-size: 20px; -fx-text-alignment: center;");
        videoContainer.getChildren().add(placeholder);

        // Controls Box
        VBox controlsBox = new VBox(15);
        controlsBox.setPadding(new Insets(20, 40, 30, 40));
        controlsBox.setStyle("-fx-background-color: " + TEAL_PANEL + "; -fx-background-radius: 15 15 0 0;");

        HBox progressBox = new HBox(15);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressSlider = new Slider(0, 100, 0);
        progressSlider.getStyleClass().add("slider");
        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");
        progressBox.getChildren().addAll(progressSlider, timeLabel);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button playPauseBtn = createPrimaryButton("Play");
        playPauseBtn.setPrefWidth(120);
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playPauseBtn.setText("Play");
                } else {
                    mediaPlayer.play();
                    playPauseBtn.setText("Pause");
                }
            }
        });

        Button stopBtn = createSecondaryButton("Stop");
        stopBtn.setPrefWidth(100);
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                playPauseBtn.setText("Play");
                progressSlider.setValue(0);
            }
        });

        HBox volumeBox = new HBox(10);
        volumeBox.setAlignment(Pos.CENTER_LEFT);
        Text volIcon = new Text("Vol");
        volIcon.setStyle("-fx-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.getStyleClass().add("slider");
        volumeSlider.setPrefWidth(150);
        volumeBox.getChildren().addAll(volIcon, volumeSlider);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(playPauseBtn, stopBtn, spacer, volumeBox);
        controlsBox.getChildren().addAll(progressBox, buttonBox);

        mediaPlayerScreen.setTop(topBar);
        mediaPlayerScreen.setCenter(videoContainer);
        mediaPlayerScreen.setBottom(controlsBox);
    }

    private void playVideo(String path, String title) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            videoTitleLabel.setText(title);

            File file = new File(path);
            if (!file.exists()) {
                return;
            }

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // Center video container automatically handles scaling now
            mediaView.fitWidthProperty().bind(mediaPlayerScreen.widthProperty());
            mediaView.fitHeightProperty().bind(mediaPlayerScreen.heightProperty().subtract(200));

            StackPane videoContainer = (StackPane) mediaPlayerScreen.getCenter();
            videoContainer.getChildren().setAll(mediaView);

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100);
                }
                timeLabel.setText(formatTime(newTime) + " / " + formatTime(mediaPlayer.getTotalDuration()));
            });

            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (progressSlider.isValueChanging()) {
                    mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newVal.doubleValue() / 100));
                }
            });

            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) mediaPlayer.setVolume(newVal.doubleValue() / 100);
            });

            mediaPlayer.setOnReady(() -> mediaPlayer.play());

            contentArea.getChildren().setAll(mediaPlayerScreen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTime(Duration duration) {
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
        movieDetailsContainer.getChildren().clear();

        HBox mainLayout = new HBox(50);
        mainLayout.setAlignment(Pos.TOP_LEFT);

        ImageView posterImage = new ImageView(new Image(movie.imageUrl, true));
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
        playBtn.setOnAction(e -> {
            String localPath = "C:/videos/" + movie.title.replace(" ", "_") + ".mp4";
            playVideo(localPath, movie.title);
        });

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

                ImageView miniPoster = new ImageView(new Image(movie.imageUrl, true));
                miniPoster.setFitWidth(70);
                miniPoster.setFitHeight(100);
                Rectangle clip = new Rectangle(70, 100);
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

        FlowPane grid = new FlowPane(20, 20);
        String[] categories = {"Action", "Sci-Fi", "Drama", "Comedy", "Thriller",
                "Romance", "Horror", "Fantasy", "Documentary", "Animation"};

        for (String cat : categories) {
            VBox catCard = new VBox(10);
            catCard.getStyleClass().add("category-card");
            catCard.setAlignment(Pos.CENTER);
            catCard.setPrefWidth(220);
            catCard.setPrefHeight(120);

            Text name = new Text(cat);
            name.getStyleClass().add("category-name");
            catCard.getChildren().add(name);

            catCard.setOnMouseEntered(e -> {
                catCard.getStyleClass().add("category-card-hover");
                name.setFill(Color.web(TEAL_BG));
            });
            catCard.setOnMouseExited(e -> {
                catCard.getStyleClass().remove("category-card-hover");
                name.setFill(Color.web("#FFFFFF"));
            });
            grid.getChildren().add(catCard);
        }
        content.getChildren().addAll(title, grid);
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