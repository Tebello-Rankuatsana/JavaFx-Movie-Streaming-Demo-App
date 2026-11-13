package streaming;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.web.WebView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App extends Application {

    // --- Theme Constants ---
    private static final String TEAL_BG = "#0B1A26";
    private static final String TEAL_PANEL = "#132A38";
    private static final String CYAN_ACCENT = "#00D2FF";
    private static final String GOLD_ACCENT = "#FCA311";
    private static final String TEXT_MUTED = "#8B9EB0";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // --- Layout Managers ---
    private BorderPane root;
    private StackPane contentArea;
    private VBox sidebar;

    // --- Screens ---
    private VBox homeScreen;
    private VBox movieDetailsScreen;
    private VBox movieDetailsContainer;
    private Label detailOverviewLabel;
    private Label detailGenresLabel;
    private Label detailReleaseLabel;
    private Label detailRuntimeLabel;
    private Label detailRatingLabel;
    private Label detailCastLabel;
    private Label detailDirectorLabel;
    private Label detailLanguagesLabel;
    private Label detailMetadataStatusLabel;
    private long detailRequestId;
    private VBox watchlistScreen;
    private VBox categoriesScreen;
    private VBox settingsScreen;
    private VBox notificationsScreen;
    private Button notificationsBackButton;
    private VBox tvShowsScreen;
    private VBox moviesScreen;
    private BorderPane mediaPlayerScreen;
    // --- Data Models ---
    private final List<Movie> allMovies = new ArrayList<>();
    private final List<Movie> watchlist = new ArrayList<>();
    private final List<Movie> trendingMovies = new ArrayList<>();
    private final List<Movie> recentlyAdded = new ArrayList<>();
    private final List<Movie> popularMovies = new ArrayList<>();
    private final Map<String, MovieMetadata> movieMetadataCache = new HashMap<>();

    // --- Media Player State ---
    private Stage mainStage;
    // Demo profile and playback preferences live only for this app run; there is no account backend.
    private String profileName = "Alexander";
    private String profileEmail = "alexander@example.com";
    private String preferredLanguage = "English";
    private boolean membershipActive = true;
    private Button homeProfileButton;
    private Text homeProfileInitial;
    private String playbackQuality = "1080p";
    private StackPane videoContainer;
    private Region playerScrim;
    private Label videoPlaceholder;
    private Label playbackErrorLabel;
    private Label playerTitleLabel;
    private String previousWindowTitle;
    private Button fullscreenButton;
    private Button openTrailerButton;
    private WebView trailerWebView;
    // Kept only in memory so one key prompt serves all trailer lookups in this app run.
    // It is cleared if TMDB rejects it and is never written to disk or bundled with the app.
    private String tmdbApiKey;
    private String currentYoutubeKey;
    private long trailerRequestId;

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
        createNotificationsScreen();
        createTVShowsScreen();
        createMoviesScreen();
        createMediaPlayerScreen();

        showHomeScreen();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth = Math.min(1440, screenBounds.getWidth() * 0.9);
        double windowHeight = Math.min(900, screenBounds.getHeight() * 0.9);
        Scene scene = new Scene(root, windowWidth, windowHeight);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("StreamFlix - Premium Stream");
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
        side.setPrefWidth(86);
        side.setMinWidth(86);
        side.setMaxWidth(86);
        side.setPadding(new Insets(22, 14, 22, 14));

        StackPane logoIcon = new StackPane();
        Rectangle logoTile = new Rectangle(48, 48, Color.web(CYAN_ACCENT));
        logoTile.setArcWidth(16);
        logoTile.setArcHeight(16);
        logoTile.setStroke(Color.web(GOLD_ACCENT));
        logoTile.setStrokeWidth(1.5);
        logoTile.setEffect(new DropShadow(12, Color.web(CYAN_ACCENT, 0.3)));
        logoIcon.getChildren().add(logoTile);
        Text logoLetter = new Text("SF");
        logoLetter.getStyleClass().add("sidebar-logo-letter");
        logoIcon.getChildren().add(logoLetter);
        Rectangle logoAccent = new Rectangle(18, 2.5, Color.web(GOLD_ACCENT));
        logoAccent.setArcWidth(3);
        logoAccent.setArcHeight(3);
        StackPane.setAlignment(logoAccent, Pos.BOTTOM_CENTER);
        StackPane.setMargin(logoAccent, new Insets(0, 0, 9, 0));
        logoIcon.getChildren().add(logoAccent);
        Tooltip.install(logoIcon, new Tooltip("StreamFlix"));
        logoIcon.setPadding(new Insets(0, 0, 24, 0));

        VBox navLinks = new VBox(14);
        navLinks.setAlignment(Pos.TOP_CENTER);

        Button btnHome = createNavButton("Home");
        Button btnMovies = createNavButton("Movies");
        Button btnTVShows = createNavButton("TV Shows");
        Button btnCategories = createNavButton("Categories");
        Button btnWatchlist = createNavButton("Watchlist");
        Button btnSettings = createNavButton("Settings");

        updateNavButtonStyle(btnHome, true);

        btnHome.setOnAction(e -> { showHomeScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnHome, true); });
        btnMovies.setOnAction(e -> { showMoviesScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnMovies, true); });
        btnTVShows.setOnAction(e -> { showTVShowsScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnTVShows, true); });
        btnCategories.setOnAction(e -> { showCategoriesScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnCategories, true); });
        btnWatchlist.setOnAction(e -> { showWatchlistScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnWatchlist, true); });
        btnSettings.setOnAction(e -> { showSettingsScreen(); resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings); updateNavButtonStyle(btnSettings, true); });

        navLinks.getChildren().addAll(
                createNavItem(btnHome, "Home"),
                createNavItem(btnMovies, "Movies"),
                createNavItem(btnTVShows, "TV Shows"),
                createNavItem(btnCategories, "Categories"),
                createNavItem(btnWatchlist, "Watchlist"),
                createNavItem(btnSettings, "Settings"));

        side.getChildren().addAll(logoIcon, navLinks);
        return side;
    }

    private Button createNavButton(String text) {
        String icon = navigationIcon(text);
        Button btn = new Button();
        btn.getStyleClass().add("nav-button");
        btn.setGraphic(createPlayerIcon(icon));
        btn.setAccessibleText(text);
        btn.setFocusTraversable(true);
        return btn;
    }

    //Added the hover effect to the navigation items, so that when the user hovers over a button, the label appears.
    private VBox createNavItem(Button button, String text) {
        Label label = new Label(text);
        label.getStyleClass().add("nav-label");
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setVisible(false);
        label.setManaged(false);

        VBox item = new VBox(2, button, label);
        item.setAlignment(Pos.CENTER);
        item.hoverProperty().addListener((observable, wasHovered, isHovered) -> {
            label.setVisible(isHovered);
            label.setManaged(isHovered);
        });
        return item;
    }

    private void updateNavButtonStyle(Button btn, boolean active) {
        btn.getStyleClass().removeAll("nav-button", "nav-button-active");
        btn.getStyleClass().add(active ? "nav-button-active" : "nav-button");
        setNavIconColor(btn, active);
    }

    private void setNavIconColor(Button btn, boolean active) {
        StackPane iconBox = (StackPane) btn.getGraphic();
        SVGPath icon = (SVGPath) iconBox.getChildren().get(0);
        icon.setFill(active ? Color.web(GOLD_ACCENT) : Color.web(TEXT_MUTED));
    }

    private String navigationIcon(String name) {
        switch (name) {
            case "Home":
                return "M3 10.5 12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5H14v-6h-4v6H4.5A1.5 1.5 0 0 1 3 19.5z";
            case "Movies":
                return "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zm1 2v3h3V6zm5 0v3h4V6zm6 0v3h3V6zM5 15v3h3v-3zm5 0v3h4v-3zm6 0v3h3v-3z";
            case "TV Shows":
                return "M3 5h18v13H3z M8 21h8 M12 18v3 M9 2l3 3 3-3";
            case "Categories":
                return "M3 4h8v8H3z M14 4h7v5h-7z M14 12h7v8h-7z M3 15h8v5H3z";
            case "Watchlist":
                return "M5 3h14v18l-7-4-7 4z";
            case "Settings":
                return "M3 5h18v2H3z M3 11h18v2H3z M3 17h18v2H3z M7 3h3v6H7z M14 9h3v6h-3z M9 15h3v6H9z";
            default:
                return "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z";
        }
    }

    private void resetNavs(Button... buttons) {
        for (Button b : buttons) {
            b.getStyleClass().remove("nav-button-active");
            if (!b.getStyleClass().contains("nav-button")) {
                b.getStyleClass().add("nav-button");
            }
            setNavIconColor(b, false);
        }
    }

    // =========================================================================
    // TOP BAR (Search, Profile)
    // =========================================================================
    private StackPane createTopBar() {
        StackPane topBar = new StackPane();
        topBar.getStyleClass().add("home-top-bar");
        topBar.setPadding(new Insets(18, 38, 18, 38));

        VBox searchArea = new VBox(6);
        searchArea.setMaxWidth(520);
        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M10 3a7 7 0 1 0 0 14a7 7 0 1 0 0-14 M15 15l6 6");
        searchIcon.setFill(Color.TRANSPARENT);
        searchIcon.setStroke(Color.web(TEXT_MUTED));
        searchIcon.setStrokeWidth(2.2);
        searchIcon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        searchIcon.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        searchIcon.setMouseTransparent(true);
        StackPane searchIconBox = new StackPane(searchIcon);
        searchIconBox.setMinSize(24, 24);
        searchIconBox.setPrefSize(24, 24);
        searchIconBox.setMaxSize(24, 24);
        searchIconBox.setMouseTransparent(true);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search movies by title or genre...");
        searchField.setPrefWidth(390);
        searchField.setFocusTraversable(false);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIconBox, searchField);

        VBox searchResults = new VBox(2);
        searchResults.getStyleClass().add("search-results");
        searchResults.setMaxHeight(300);
        searchResults.setVisible(false);
        searchResults.setManaged(false);
        searchArea.getChildren().addAll(searchBox, searchResults);
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                updateSearchResults(newValue, searchResults, searchField));
        searchField.setOnAction(e -> {
            if (!searchResults.getChildren().isEmpty()
                    && searchResults.getChildren().get(0) instanceof Button) {
                ((Button) searchResults.getChildren().get(0)).fire();
            }
        });

        Button notifBtn = new Button();
        notifBtn.getStyleClass().add("notification-button");
        notifBtn.setGraphic(createPlayerIcon("M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9zm-8 12h4a2 2 0 0 1-4 0z"));
        notifBtn.setTooltip(new Tooltip("Notifications"));
        notifBtn.setAccessibleText("Notifications: updates and service terms");
        notifBtn.setOnAction(e -> showNotifications(this::showHomeScreen, "Back to Home"));

        // Keep the Home profile control compact: show only initials, never membership details.
        homeProfileButton = new Button();
        homeProfileButton.getStyleClass().add("profile-button");
        Circle profileAvatar = new Circle(17, Color.web(TEAL_PANEL));
        profileAvatar.setStroke(Color.web(CYAN_ACCENT));
        profileAvatar.setStrokeWidth(1.5);
        homeProfileInitial = new Text();
        homeProfileInitial.setFill(Color.WHITE);
        homeProfileInitial.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        StackPane avatar = new StackPane(profileAvatar, homeProfileInitial);
        homeProfileButton.setGraphic(avatar);
        updateHomeProfileButton();
        homeProfileButton.setOnAction(e -> showProfileInfo(this::showHomeScreen));

        HBox actions = new HBox(12, notifBtn, homeProfileButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setAlignment(actions, Pos.CENTER_RIGHT);
        StackPane.setAlignment(searchArea, Pos.CENTER);
        topBar.getChildren().addAll(searchArea, actions);
        return topBar;
    }

    private void updateHomeProfileButton() {
        homeProfileInitial.setText(profileInitials(profileName));
        homeProfileButton.setTooltip(new Tooltip("Profile for " + profileName));
        homeProfileButton.setAccessibleText("Profile for " + profileName);
    }

    private String profileInitials(String name) {
        // Use the first and last initials when available, falling back to one initial for a single name.
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showNotifications(Runnable backAction, String backLabel) {
        // Reuse the app's main content area so notices appear as a normal screen, not a popup.
        // The shared screen returns to the caller, whether it was opened from Home or Settings.
        notificationsBackButton.setOnAction(e -> backAction.run());
        notificationsBackButton.setTooltip(new Tooltip(backLabel));
        notificationsBackButton.setAccessibleText(backLabel);
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(notificationsScreen);
        mainStage.setTitle("StreamFlix - Updates & Terms");
    }

    private void createNotificationsScreen() {
        // Keep service updates and terms in a scrollable screen that follows the Settings layout.
        notificationsScreen = new VBox(0);
        notificationsScreen.getStyleClass().add("screen");
        notificationsScreen.getStyleClass().add("notifications-screen");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(24);
        content.setPadding(new Insets(40, 50, 60, 50));
        notificationsBackButton = createScreenBackButton("Back to Home", this::showHomeScreen);
        content.getChildren().add(notificationsBackButton);
        Label title = new Label("Updates & Terms");
        title.getStyleClass().add("screen-title");
        Label intro = new Label("Service notices and information about the services used by StreamFlix.");
        intro.getStyleClass().add("detail-synopsis");
        intro.setWrapText(true);

        VBox updates = new VBox(14);
        // These notices describe the external services and credential handling used by the app.
        updates.getChildren().addAll(
                notificationItem("Movie details",
                        "Cast, directors, languages, and ratings are provided by TMDB."),
                notificationItem("Trailer playback",
                        "Trailers are embedded from YouTube. If playback is unavailable in the app, use the external-link button in the player."),
                notificationItem("Your TMDB credential",
                        "Your API key or read token stays in memory for this app session and is not saved."),
                notificationItem("Terms of service",
                        "This demo is provided under the repository's MIT License. TMDB and YouTube services are subject to their own terms."));

        Label termsHeading = new Label("External service terms");
        termsHeading.getStyleClass().add("detail-section-heading");
        // Open provider terms in the user's default browser rather than embedding third-party pages.
        Hyperlink tmdbTerms = new Hyperlink("TMDB Terms of Use");
        tmdbTerms.setOnAction(e -> getHostServices().showDocument(
                "https://www.themoviedb.org/terms-of-use"));
        Hyperlink youtubeTerms = new Hyperlink("YouTube Terms of Service");
        youtubeTerms.setOnAction(e -> getHostServices().showDocument(
                "https://www.youtube.com/t/terms"));
        HBox termsLinks = new HBox(16, tmdbTerms, youtubeTerms);

        content.getChildren().addAll(title, intro, updates, termsHeading, termsLinks);
        scrollPane.setContent(content);
        notificationsScreen.getChildren().add(scrollPane);
    }

    private VBox notificationItem(String heading, String message) {
        Label title = new Label(heading);
        title.getStyleClass().add("notification-item-title");
        Label description = new Label(message);
        description.getStyleClass().add("notification-item-description");
        description.setWrapText(true);
        VBox item = new VBox(4, title, description);
        item.getStyleClass().add("notification-item");
        return item;
    }

    private void showProfileInfo(Runnable returnAction) {
        root.setLeft(sidebar);
        mainStage.setTitle("StreamFlix - Profile");

        VBox screen = new VBox(0);
        screen.getStyleClass().add("screen");
        screen.getStyleClass().add("profile-screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(24);
        content.setPadding(new Insets(40, 50, 60, 50));
        Button backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", "Back");
        backButton.setOnAction(e -> returnAction.run());

        Label title = new Label("Profile");
        title.getStyleClass().add("screen-title");

        Circle avatarCircle = new Circle(32, Color.web(TEAL_PANEL));
        avatarCircle.setStroke(Color.web(CYAN_ACCENT));
        avatarCircle.setStrokeWidth(1.5);
        Text avatarInitial = new Text(profileInitials(profileName));
        avatarInitial.setFill(Color.WHITE);
        avatarInitial.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        StackPane avatar = new StackPane(avatarCircle, avatarInitial);

        Label name = new Label(profileName);
        name.getStyleClass().add("profile-name");
        Label membership = new Label(membershipActive ? "Premium Member" : "Membership cancelled");
        membership.getStyleClass().add("profile-membership");
        VBox profileHeading = new VBox(6, name, membership);
        HBox identity = new HBox(18, avatar, profileHeading);
        identity.setAlignment(Pos.CENTER_LEFT);
        identity.getStyleClass().add("notification-item");

        Label detailsTitle = new Label("Profile details");
        detailsTitle.getStyleClass().add("detail-section-heading");
        // These editable fields update the demo's in-memory profile, not a remote account.
        TextField nameField = new TextField(profileName);
        TextField emailField = new TextField(profileEmail);
        TextField languageField = new TextField(preferredLanguage);
        nameField.setPromptText("Full name");
        emailField.setPromptText("Email address");
        languageField.setPromptText("Preferred language");
        nameField.getStyleClass().add("profile-form-field");
        emailField.getStyleClass().add("profile-form-field");
        languageField.getStyleClass().add("profile-form-field");

        VBox profileForm = new VBox(10,
                profileField("Full name", nameField),
                profileField("Email address", emailField),
                profileField("Preferred language", languageField));
        Label membershipTitle = new Label("Membership");
        membershipTitle.getStyleClass().add("detail-section-heading");
        VBox membershipDetails = new VBox(14,
                notificationItem("Plan", "Premium"),
                notificationItem("Status", membershipActive ? "Active" : "Cancelled"),
                notificationItem("Billing", "This demo does not connect to billing. Membership changes only affect the current app session."));
        Button saveButton = createPrimaryButton("Save profile changes");
        saveButton.setOnAction(e -> saveProfileChanges(nameField, emailField, languageField, returnAction));

        VBox profileDetails = new VBox(18, detailsTitle, profileForm, saveButton,
                membershipTitle, membershipDetails);
        if (membershipActive) {
            Button cancelMembershipButton = new Button("Cancel membership");
            cancelMembershipButton.getStyleClass().add("profile-danger-button");
            cancelMembershipButton.setOnAction(e -> confirmMembershipCancellation(returnAction));
            profileDetails.getChildren().add(cancelMembershipButton);
        }

        content.getChildren().addAll(backButton, title, identity, profileDetails);
        scrollPane.setContent(content);
        screen.getChildren().add(scrollPane);
        contentArea.getChildren().setAll(screen);
    }

    private VBox profileField(String labelText, TextField input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("profile-field-label");
        VBox field = new VBox(6, label, input);
        return field;
    }

    private void saveProfileChanges(TextField nameField, TextField emailField,
            TextField languageField, Runnable returnAction) {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newLanguage = languageField.getText().trim();
        if (newName.isEmpty() || newEmail.isEmpty() || newLanguage.isEmpty()
                || !newEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Enter a name, a valid email address, and a preferred language.",
                    ButtonType.OK);
            alert.initOwner(mainStage);
            alert.setHeaderText("Unable to save profile");
            alert.showAndWait();
            return;
        }

        profileName = newName;
        profileEmail = newEmail;
        preferredLanguage = newLanguage;
        updateHomeProfileButton();
        showProfileInfo(returnAction);
    }

    private void confirmMembershipCancellation(Runnable returnAction) {
        // Confirm the local status change and make clear that no billing service is contacted.
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel the Premium membership? This demo will only change the displayed status for this app session; it will not contact a billing service.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.initOwner(mainStage);
        confirmation.setHeaderText("Cancel membership");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                membershipActive = false;
                showProfileInfo(returnAction);
            }
        });
    }

    private void updateSearchResults(String query, VBox results, TextField searchField) {
        results.getChildren().clear();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) {
            results.setVisible(false);
            results.setManaged(false);
            return;
        }

        int matchCount = 0;
        for (Movie movie : allMovies) {
            if (movie.title.toLowerCase().contains(normalizedQuery)
                    || movie.genre.toLowerCase().contains(normalizedQuery)) {
                Button result = new Button(movie.title + "  -  " + movie.genre);
                result.getStyleClass().add("search-result-button");
                result.setMaxWidth(Double.MAX_VALUE);
                result.setAlignment(Pos.CENTER_LEFT);
                result.setOnAction(e -> {
                    searchField.clear();
                    results.setVisible(false);
                    results.setManaged(false);
                    showMovieDetails(movie);
                });
                results.getChildren().add(result);
                matchCount++;
                if (matchCount == 8) {
                    break;
                }
            }
        }

        if (matchCount == 0) {
            Label emptyResult = new Label("No matching movies");
            emptyResult.getStyleClass().add("search-empty-result");
            results.getChildren().add(emptyResult);
        }
        results.setManaged(true);
        results.setVisible(true);
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

        // Put poster shelves near the top, like the reference, without losing StreamFlix styling.
        VBox contentBox = new VBox(26);
        contentBox.setPadding(new Insets(16, 0, 40, 0));
        VBox trendingRow = createMovieRow("Trending Now", trendingMovies);
        VBox popularRow = createMovieRow("Popular in Adventure", popularMovies);
        VBox recentlyRow = createMovieRow("Recently Added in Sci-Fi", recentlyAdded);

        contentBox.getChildren().addAll(trendingRow, popularRow, recentlyRow);

        scrollPane.setContent(contentBox);
        homeScreen.getChildren().addAll(createTopBar(), scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
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
        section.setPadding(new Insets(0, 36, 0, 36));

        Text titleText = new Text(title);
        titleText.getStyleClass().add("section-title");

        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);

        HBox cardBox = new HBox(20);
        cardBox.setPadding(new Insets(8, 2, 20, 2));

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
        mediaPlayerScreen = new BorderPane();
        mediaPlayerScreen.getStyleClass().add("player-screen");
        playerScrim = new Region();
        playerScrim.getStyleClass().add("player-scrim");

        videoContainer = new StackPane();
        videoContainer.getStyleClass().add("video-container");

        videoPlaceholder = new Label("Enter your TMDB API key to find a trailer");
        videoPlaceholder.getStyleClass().add("video-placeholder");

        playbackErrorLabel = new Label();
        playbackErrorLabel.getStyleClass().add("video-error");
        playbackErrorLabel.setWrapText(true);
        playbackErrorLabel.setMaxWidth(520);
        playbackErrorLabel.setVisible(false);

        trailerWebView = new WebView();
        trailerWebView.setContextMenuEnabled(false);
        trailerWebView.setVisible(false);
        trailerWebView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label sourceLabel = new Label("Trailer playback provided by YouTube");
        sourceLabel.getStyleClass().add("player-time-label");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        openTrailerButton = createSecondaryButton("");
        openTrailerButton.setGraphic(createPlayerIcon(
                "M14 3h7v7h-2V6.41l-9.29 9.3-1.42-1.42 9.3-9.29H14V3z "
                        + "M5 5h5v2H5v12h12v-5h2v5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z"));
        openTrailerButton.setTooltip(new Tooltip("Open trailer in browser"));
        openTrailerButton.setAccessibleText("Open trailer in browser");
        openTrailerButton.setDisable(true);
        openTrailerButton.setOnAction(e -> openTrailerInBrowser());
        fullscreenButton = createPlayerButton(
                "M5 5h5v2H7v3H5z M14 5h5v5h-2V7h-3z M5 14h2v3h3v2H5z M17 14h2v5h-5v-2h3z",
                "player-volume-button", "Enter fullscreen");
        fullscreenButton.setOnAction(e -> toggleFullscreen());
        HBox playerFooter = new HBox(12, sourceLabel, openTrailerButton, footerSpacer, fullscreenButton);
        playerFooter.setAlignment(Pos.CENTER_LEFT);
        playerFooter.getStyleClass().add("player-controls");

        videoContainer.getChildren().addAll(trailerWebView, videoPlaceholder, playbackErrorLabel);
        trailerWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED
                    && trailerWebView.isVisible()) {
                showPlaybackError("The embedded YouTube player could not load. Use Open in browser to watch this trailer.");
            }
        });
        StackPane.setAlignment(videoPlaceholder, Pos.CENTER);
        StackPane.setAlignment(playbackErrorLabel, Pos.TOP_CENTER);
        StackPane.setMargin(playbackErrorLabel, new Insets(18));

        Button backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", "Back");
        backButton.setOnAction(e -> closePlayer());
        playerTitleLabel = new Label("Movie Trailer");
        playerTitleLabel.getStyleClass().add("player-title");
        HBox playerHeader = new HBox(18, backButton, playerTitleLabel);
        playerHeader.setAlignment(Pos.CENTER_LEFT);
        playerHeader.getStyleClass().add("player-header");

        mediaPlayerScreen.setTop(playerHeader);
        mediaPlayerScreen.setCenter(videoContainer);
        mediaPlayerScreen.setBottom(playerFooter);
        mediaPlayerScreen.setMinSize(0, 0);
        mediaPlayerScreen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mediaPlayerScreen.prefWidthProperty().bind(contentArea.widthProperty());
        mediaPlayerScreen.prefHeightProperty().bind(contentArea.heightProperty());

        mainStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
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
        root.setLeft(null);
        previousWindowTitle = mainStage.getTitle();
        trailerRequestId++;
        trailerWebView.getEngine().load("about:blank");
        trailerWebView.setVisible(false);
        currentYoutubeKey = null;
        openTrailerButton.setDisable(true);
        playerTitleLabel.setText(movie.title);
        videoPlaceholder.setText("Looking up the trailer for " + movie.title + "...");
        videoPlaceholder.setVisible(true);
        playbackErrorLabel.setVisible(false);
        StackPane.setAlignment(playerScrim, Pos.CENTER);
        StackPane.setAlignment(mediaPlayerScreen, Pos.CENTER);
        contentArea.getChildren().addAll(playerScrim, mediaPlayerScreen);
        mainStage.setTitle("StreamFlix - " + movie.title);

        // Ask on the first trailer request only; cancelling leaves the key unset so it can be retried.
        if (tmdbApiKey == null) {
            tmdbApiKey = requestTmdbApiKey();
            if (tmdbApiKey == null) {
                videoPlaceholder.setText("A TMDB API key is required to fetch movie trailers.");
                return;
            }
        }
        fetchTrailer(movie, tmdbApiKey);
    }

    private String requestTmdbApiKey() {
        // A PasswordField masks entry; the value is returned to the caller but is not persisted.
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(mainStage);
        dialog.setTitle("TMDB API Key");
        dialog.setHeaderText("Enter your TMDB API key or Read Access Token");

        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("TMDB API Key (v3) or Read Access Token (v4)");
        VBox content = new VBox(10,
                new Label("Use the API Key (v3) or API Read Access Token (v4) from your TMDB account. "
                        + "It stays in memory for this session and is not saved."),
                apiKeyField);
        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK
                ? apiKeyField.getText().trim() : null);

        return dialog.showAndWait().filter(key -> !key.isEmpty()).orElse(null);
    }

    private void fetchTrailer(Movie movie, String apiKey) {
        long requestId = ++trailerRequestId;
        // Keep network latency off the JavaFX thread; ignore results for a player that has since changed.
        CompletableFuture.supplyAsync(() -> lookupTrailer(movie.title, apiKey))
                .whenComplete((youtubeKey, error) -> Platform.runLater(() -> {
                    if (requestId != trailerRequestId) {
                        return;
                    }
                    if (error != null) {
                        Throwable cause = error instanceof CompletionException
                                ? error.getCause() : error;
                        // Clear only the rejected key. The next trailer attempt will prompt for a replacement.
                        if (cause instanceof InvalidTmdbApiKeyException
                                && apiKey.equals(tmdbApiKey)) {
                            tmdbApiKey = null;
                        }
                        showPlaybackError(cause.getMessage());
                        return;
                    }
                    videoPlaceholder.setVisible(false);
                    playbackErrorLabel.setVisible(false);
                    currentYoutubeKey = youtubeKey;
                    openTrailerButton.setDisable(false);
                    trailerWebView.setVisible(true);
                    // Only the trailer ID goes to YouTube; the TMDB API key is not included in this URL.
                    trailerWebView.getEngine().load(
                            "https://www.youtube-nocookie.com/embed/" + youtubeKey
                                    + "?autoplay=1&rel=0");
                }));
    }

    private String lookupTrailer(String title, String apiKey) {
        String movieId = findTmdbMovieId(title, apiKey);
        JsonObject videosResponse = getTmdbJson(
                "https://api.themoviedb.org/3/movie/" + movieId + "/videos", apiKey);
        JsonArray videos = videosResponse.has("results")
                ? videosResponse.getAsJsonArray("results") : new JsonArray();
        JsonObject firstTrailer = null;
        for (JsonElement element : videos) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject video = element.getAsJsonObject();
            if ("YouTube".equalsIgnoreCase(jsonString(video, "site"))
                    && "Trailer".equalsIgnoreCase(jsonString(video, "type"))
                    && !jsonString(video, "key").isEmpty()) {
                if (firstTrailer == null) {
                    firstTrailer = video;
                }
                if (video.has("official") && video.get("official").getAsBoolean()) {
                    firstTrailer = video;
                    break;
                }
            }
        }
        if (firstTrailer == null) {
            throw new TrailerLookupException("TMDB has no YouTube trailer available for " + title + ".");
        }

        String youtubeKey = jsonString(firstTrailer, "key");
        if (!youtubeKey.matches("[A-Za-z0-9_-]+")) {
            throw new TrailerLookupException("TMDB returned an invalid YouTube trailer ID.");
        }
        // TMDB provides a YouTube video ID, not a direct media URL; the player embeds it below.
        return youtubeKey;
    }

    private MovieMetadata lookupMovieMetadata(String title, String apiKey) {
        String movieId = findTmdbMovieId(title, apiKey);
        JsonObject details = getTmdbJson(
                "https://api.themoviedb.org/3/movie/" + movieId + "?append_to_response=credits",
                apiKey);
        JsonObject credits = details.has("credits") && details.get("credits").isJsonObject()
                ? details.getAsJsonObject("credits") : new JsonObject();

        return new MovieMetadata(
                jsonString(details, "overview"),
                joinJsonValues(credits, "cast", "name", 6),
                joinCrewDirectors(credits),
                joinJsonValues(details, "spoken_languages", "name", 0),
                joinJsonValues(details, "genres", "name", 0),
                jsonString(details, "release_date"),
                details.has("runtime") && !details.get("runtime").isJsonNull()
                        ? details.get("runtime").getAsString() + " min" : "",
                details.has("vote_average") && !details.get("vote_average").isJsonNull()
                        ? String.format(java.util.Locale.ROOT, "%.1f / 10",
                                details.get("vote_average").getAsDouble()) : "");
    }

    private String findTmdbMovieId(String title, String apiKey) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        JsonObject searchResponse = getTmdbJson(
                "https://api.themoviedb.org/3/search/movie?query=" + encodedTitle, apiKey);
        JsonArray results = searchResponse.has("results")
                ? searchResponse.getAsJsonArray("results") : new JsonArray();
        if (results.size() == 0) {
            throw new TrailerLookupException("TMDB did not find a movie titled \"" + title + "\".");
        }

        JsonObject selectedMovie = results.get(0).getAsJsonObject();
        for (JsonElement result : results) {
            if (result.isJsonObject()
                    && title.equalsIgnoreCase(jsonString(result.getAsJsonObject(), "title"))) {
                selectedMovie = result.getAsJsonObject();
                break;
            }
        }
        if (!selectedMovie.has("id")) {
            throw new TrailerLookupException("TMDB returned a movie without an ID.");
        }
        return selectedMovie.get("id").getAsString();
    }

    private static String joinJsonValues(JsonObject object, String arrayName,
            String propertyName, int limit) {
        if (!object.has(arrayName) || !object.get(arrayName).isJsonArray()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray(arrayName)) {
            if (element.isJsonObject()) {
                String value = jsonString(element.getAsJsonObject(), propertyName);
                if (!value.isEmpty() && !values.contains(value)) {
                    values.add(value);
                    if (limit > 0 && values.size() == limit) {
                        break;
                    }
                }
            }
        }
        return String.join(", ", values);
    }

    private static String joinCrewDirectors(JsonObject credits) {
        if (!credits.has("crew") || !credits.get("crew").isJsonArray()) {
            return "";
        }
        List<String> directors = new ArrayList<>();
        for (JsonElement element : credits.getAsJsonArray("crew")) {
            if (element.isJsonObject()) {
                JsonObject crewMember = element.getAsJsonObject();
                if ("Director".equalsIgnoreCase(jsonString(crewMember, "job"))) {
                    String name = jsonString(crewMember, "name");
                    if (!name.isEmpty() && !directors.contains(name)) {
                        directors.add(name);
                    }
                }
            }
        }
        return String.join(", ", directors);
    }

    private JsonObject getTmdbJson(String url, String apiKey) {
        try {
            // TMDB accepts either a v3 key as a query parameter or a v4 token as a Bearer credential.
            boolean readAccessToken = isReadAccessToken(apiKey);
            String requestUrl = readAccessToken ? url
                    : url + (url.contains("?") ? "&" : "?")
                            + "api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(requestUrl))
                    .timeout(java.time.Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .GET();
            if (readAccessToken) {
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            }
            HttpResponse<String> response = HTTP_CLIENT.send(
                    requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 401) {
                String detail = tmdbErrorDetail(response.body());
                throw new InvalidTmdbApiKeyException(response.statusCode(), detail);
            }
            if (response.statusCode() == 403) {
                String detail = tmdbErrorDetail(response.body());
                throw new TrailerLookupException("TMDB denied this request (HTTP 403)"
                        + (detail.isEmpty() ? "." : ": " + detail));
            }
            if (response.statusCode() != 200) {
                String detail = tmdbErrorDetail(response.body());
                throw new TrailerLookupException(
                        "TMDB request failed (HTTP " + response.statusCode() + ")"
                                + (detail.isEmpty() ? "." : ": " + detail));
            }
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InvalidTmdbApiKeyException | TrailerLookupException e) {
            throw e;
        } catch (IOException e) {
            throw new TrailerLookupException("Could not connect to TMDB: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TrailerLookupException("The TMDB request was interrupted.", e);
        } catch (RuntimeException e) {
            throw new TrailerLookupException("TMDB returned an unreadable response.", e);
        }
    }

    private static boolean isReadAccessToken(String credential) {
        return credential.split("\\.", -1).length == 3;
    }

    private static String tmdbErrorDetail(String responseBody) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            return response.has("status_message")
                    ? response.get("status_message").getAsString() : "";
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static String jsonString(JsonObject object, String property) {
        JsonElement value = object.get(property);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private void openTrailerInBrowser() {
        if (currentYoutubeKey == null) {
            return;
        }
        getHostServices().showDocument("https://www.youtube.com/watch?v=" + currentYoutubeKey);
    }

    private void closePlayer() {
        trailerRequestId++;
        trailerWebView.getEngine().load("about:blank");
        trailerWebView.setVisible(false);
        currentYoutubeKey = null;
        openTrailerButton.setDisable(true);
        if (mainStage.isFullScreen()) {
            mainStage.setFullScreen(false);
        }
        contentArea.getChildren().removeAll(mediaPlayerScreen, playerScrim);
        mainStage.setTitle(previousWindowTitle == null
                ? "StreamFlix - Premium Stream" : previousWindowTitle);
    }

    private void showPlaybackError(String message) {
        playbackErrorLabel.setText(message == null || message.isEmpty()
                ? "Unable to load this trailer." : message);
        playbackErrorLabel.setVisible(true);
        videoPlaceholder.setVisible(false);
        trailerWebView.setVisible(false);
    }

    private void toggleFullscreen() {
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    private static class TrailerLookupException extends RuntimeException {
        TrailerLookupException(String message) {
            super(message);
        }

        TrailerLookupException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class InvalidTmdbApiKeyException extends RuntimeException {
        InvalidTmdbApiKeyException(int statusCode, String detail) {
            super("TMDB rejected the credential (HTTP " + statusCode + ")"
                    + (detail == null || detail.isEmpty() ? "." : ": " + detail)
                    + " Use the API Key (v3) or API Read Access Token (v4) from TMDB.");
        }
    }

    // =========================================================================
    // MOVIE DETAILS SCREEN
    // =========================================================================
    private void createMovieDetailsScreen() {
        movieDetailsScreen = new VBox(0);
        movieDetailsScreen.getStyleClass().add("movie-details-screen");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        movieDetailsContainer = new VBox(24);
        movieDetailsContainer.setPadding(new Insets(30, 48, 48, 48));
        scrollPane.setContent(movieDetailsContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        movieDetailsScreen.getChildren().add(scrollPane);
    }

    private void showMovieDetails(Movie movie) {
        root.setLeft(null);
        mainStage.setTitle("StreamFlix - " + movie.title);
        movieDetailsContainer.getChildren().clear();

        Button backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", "Back to movies");
        backButton.setOnAction(e -> showHomeScreen());

        Button playButton = createPrimaryButton("Play Trailer");
        playButton.setOnAction(e -> openPlayer(movie));
        Button watchlistButton = createSecondaryButton(
                watchlist.contains(movie) ? "In Watchlist" : "Add to Watchlist");
        watchlistButton.setOnAction(e -> {
            if (watchlist.contains(movie)) {
                watchlist.remove(movie);
                watchlistButton.setText("Add to Watchlist");
            } else {
                watchlist.add(movie);
                watchlistButton.setText("In Watchlist");
            }
        });

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox pageHeader = new HBox(14, backButton, headerSpacer, playButton, watchlistButton);
        pageHeader.setAlignment(Pos.CENTER_LEFT);
        pageHeader.getStyleClass().add("movie-detail-actions");

        HBox hero = new HBox(42);
        hero.setAlignment(Pos.TOP_LEFT);
        hero.getStyleClass().add("movie-detail-hero");
        hero.setPadding(new Insets(30));
        ImageView posterImage = new ImageView(loadPoster(movie.imageUrl));
        posterImage.setFitWidth(300);
        posterImage.setFitHeight(450);
        posterImage.setPreserveRatio(true);
        Rectangle clip = new Rectangle(300, 450);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        posterImage.setClip(clip);
        posterImage.setSmooth(true);

        VBox infoBox = new VBox(20);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        Label title = new Label(movie.title);
        title.getStyleClass().add("detail-title");
        Label detailsMeta = new Label(movie.genre + "  |  " + movie.rating + "  |  "
                + String.format("%.1f", movie.ratingScore) + " / 10");
        detailsMeta.getStyleClass().add("detail-meta");

        detailOverviewLabel = new Label(movie.description == null || movie.description.isEmpty()
                ? "Loading overview..." : movie.description);
        detailOverviewLabel.getStyleClass().add("detail-synopsis");
        detailOverviewLabel.setWrapText(true);
        detailOverviewLabel.setMaxWidth(Double.MAX_VALUE);

        Label creditsHeading = new Label("Cast & crew");
        creditsHeading.getStyleClass().add("detail-section-heading");
        detailCastLabel = createDetailValue("Cast", "Loading...");
        detailDirectorLabel = createDetailValue("Director", "Loading...");
        detailLanguagesLabel = createDetailValue("Languages", "Loading...");
        detailGenresLabel = createDetailValue("Genres", movie.genre);
        detailReleaseLabel = createDetailValue("Release", "Loading...");
        detailRuntimeLabel = createDetailValue("Runtime", movie.duration);
        detailRatingLabel = createDetailValue("TMDB rating", "Loading...");
        detailMetadataStatusLabel = new Label("Loading movie information from TMDB...");
        detailMetadataStatusLabel.getStyleClass().add("detail-metadata-status");
        detailMetadataStatusLabel.setWrapText(true);

        infoBox.getChildren().addAll(title, detailsMeta, detailOverviewLabel, creditsHeading,
                detailCastLabel, detailDirectorLabel, detailLanguagesLabel,
                detailGenresLabel, detailReleaseLabel, detailRuntimeLabel,
                detailRatingLabel, detailMetadataStatusLabel);
        hero.getChildren().addAll(posterImage, infoBox);
        movieDetailsContainer.getChildren().addAll(pageHeader, hero);

        contentArea.getChildren().setAll(movieDetailsScreen);
        loadMovieDetails(movie);
    }

    private Label createDetailValue(String label, String value) {
        Label result = new Label(label + ": " + value);
        result.getStyleClass().add("detail-meta");
        result.setWrapText(true);
        return result;
    }

    private void loadMovieDetails(Movie movie) {
        long requestId = ++detailRequestId;
        MovieMetadata cachedMetadata = movieMetadataCache.get(movie.title);
        if (cachedMetadata != null) {
            applyMovieMetadata(cachedMetadata);
            return;
        }

        if (tmdbApiKey == null) {
            tmdbApiKey = requestTmdbApiKey();
        }
        if (tmdbApiKey == null) {
            detailMetadataStatusLabel.setText(
                    "TMDB key not entered. Select this movie again to load its cast and details.");
            setDetailLoadingValues("Not available");
            return;
        }

        String apiKey = tmdbApiKey;
        CompletableFuture.supplyAsync(() -> lookupMovieMetadata(movie.title, apiKey))
                .whenComplete((metadata, error) -> Platform.runLater(() -> {
                    if (requestId != detailRequestId) {
                        return;
                    }
                    if (error != null) {
                        Throwable cause = error instanceof CompletionException
                                ? error.getCause() : error;
                        if (cause instanceof InvalidTmdbApiKeyException && apiKey.equals(tmdbApiKey)) {
                            tmdbApiKey = null;
                        }
                        detailMetadataStatusLabel.setText(cause.getMessage());
                        setDetailLoadingValues("Not available");
                        return;
                    }
                    movieMetadataCache.put(movie.title, metadata);
                    applyMovieMetadata(metadata);
                }));
    }

    private void setDetailLoadingValues(String value) {
        detailCastLabel.setText("Cast: " + value);
        detailDirectorLabel.setText("Director: " + value);
        detailLanguagesLabel.setText("Languages: " + value);
        detailGenresLabel.setText("Genres: " + value);
        detailReleaseLabel.setText("Release: " + value);
        detailRuntimeLabel.setText("Runtime: " + value);
        detailRatingLabel.setText("TMDB rating: " + value);
    }

    private void applyMovieMetadata(MovieMetadata metadata) {
        detailOverviewLabel.setText(metadata.overview.isEmpty()
                ? "No overview is available for this movie." : metadata.overview);
        detailCastLabel.setText("Cast: " + displayOrUnavailable(metadata.cast));
        detailDirectorLabel.setText("Director: " + displayOrUnavailable(metadata.directors));
        detailLanguagesLabel.setText("Languages: " + displayOrUnavailable(metadata.languages));
        detailGenresLabel.setText("Genres: " + displayOrUnavailable(metadata.genres));
        detailReleaseLabel.setText("Release: " + displayOrUnavailable(metadata.releaseDate));
        detailRuntimeLabel.setText("Runtime: " + displayOrUnavailable(metadata.runtime));
        detailRatingLabel.setText("TMDB rating: " + displayOrUnavailable(metadata.rating));
        detailMetadataStatusLabel.setText("");
    }

    private static String displayOrUnavailable(String value) {
        return value == null || value.isEmpty() ? "Not available" : value;
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
        // Keep each Settings entry wired to its corresponding detail screen or shared app screen.
        settingsList.getChildren().add(createSettingItem("Account", "Manage your account details",
                () -> showProfileInfo(this::showSettingsScreen)));
        settingsList.getChildren().add(createSettingItem("Playback", "Video quality and playback settings",
                this::showPlaybackSettings));
        settingsList.getChildren().add(createSettingItem("Notifications", "Configure notification preferences",
                () -> showNotifications(this::showSettingsScreen, "Back to Settings")));
        settingsList.getChildren().add(createSettingItem("Privacy", "Control your privacy settings",
                this::showPrivacySettings));

        content.getChildren().addAll(title, settingsList);
        scrollPane.setContent(content);
        settingsScreen.getChildren().add(scrollPane);
    }

    private HBox createSettingItem(String title, String subtitle, Runnable action) {
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
        item.setOnMouseClicked(e -> action.run());
        item.setFocusTraversable(true);
        item.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER
                    || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                action.run();
            }
        });
        return item;
    }

    private void showPlaybackSettings() {
        // The selected value is a session preference; the provider still controls actual stream quality.
        Label description = new Label("Choose your preferred video quality. The quality available during playback depends on the video and playback provider.");
        description.getStyleClass().add("detail-synopsis");
        description.setWrapText(true);

        Label qualityHeading = new Label("Preferred video quality");
        qualityHeading.getStyleClass().add("detail-section-heading");
        FlowPane qualityOptions = new FlowPane(12, 12);
        for (String quality : List.of("360p", "480p", "720p", "1080p", "2K (1440p)")) {
            Button option = new Button(quality);
            option.getStyleClass().add("quality-option");
            if (quality.equals(playbackQuality)) {
                option.getStyleClass().add("quality-option-selected");
            }
            option.setAccessibleText("Preferred video quality: " + quality);
            option.setOnAction(e -> {
                playbackQuality = quality;
                showPlaybackSettings();
            });
            qualityOptions.getChildren().add(option);
        }

        VBox details = new VBox(18, description, qualityHeading, qualityOptions);
        VBox settings = createSettingsDetailScreen("Playback", details);
        contentArea.getChildren().setAll(settings);
    }

    private void showPrivacySettings() {
        // Explain this demo's data handling and link directly to the connected providers' terms.
        Label introduction = new Label("Please review this notice before using the StreamFlix demo. By continuing to use the app, you acknowledge how it handles information and that connected services have their own terms.");
        introduction.getStyleClass().add("detail-synopsis");
        introduction.setWrapText(true);

        VBox details = new VBox(14,
                notificationItem("Information and credentials",
                        "Movie searches and details are requested from TMDB. Any TMDB API key or read token entered for this app remains in memory for the current session and is not saved."),
                notificationItem("Watchlist and playback preference",
                        "Your watchlist and selected playback quality are held in app memory for the current run. This demo does not create an account or process payments."),
                notificationItem("Third-party services",
                        "Trailers may be embedded from YouTube, and movie information comes from TMDB. These providers may handle connection or usage data under their own privacy policies and terms."),
                notificationItem("Your choice",
                        "If you do not agree with these conditions, do not use the features that connect to TMDB or YouTube. You can close the app at any time."));

        Label termsHeading = new Label("Provider terms");
        termsHeading.getStyleClass().add("detail-section-heading");
        Hyperlink tmdbTerms = new Hyperlink("TMDB Terms of Use");
        tmdbTerms.setOnAction(e -> getHostServices().showDocument(
                "https://www.themoviedb.org/terms-of-use"));
        Hyperlink youtubeTerms = new Hyperlink("YouTube Terms of Service");
        youtubeTerms.setOnAction(e -> getHostServices().showDocument(
                "https://www.youtube.com/t/terms"));
        HBox termsLinks = new HBox(16, tmdbTerms, youtubeTerms);

        details.getChildren().addAll(termsHeading, termsLinks);
        VBox privacyContent = new VBox(18, introduction, details);
        VBox settings = createSettingsDetailScreen("Privacy & Service Terms", privacyContent);
        contentArea.getChildren().setAll(settings);
    }

    private VBox createSettingsDetailScreen(String title, VBox details) {
        VBox screen = new VBox(0);
        screen.getStyleClass().add("screen");
        screen.getStyleClass().add("settings-detail-screen");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(24);
        content.setPadding(new Insets(40, 50, 60, 50));
        // Detail screens opened from Settings return to the Settings list, not Home.
        Button backButton = createScreenBackButton("Back to Settings", this::showSettingsScreen);
        Text screenTitle = new Text(title);
        screenTitle.getStyleClass().add("screen-title");
        content.getChildren().addAll(backButton, screenTitle, details);
        scrollPane.setContent(content);
        screen.getChildren().add(scrollPane);
        return screen;
    }

    private Button createScreenBackButton(String label, Runnable action) {
        Button backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", label);
        backButton.setOnAction(e -> action.run());
        return backButton;
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
    private void showHomeScreen() { root.setLeft(sidebar); contentArea.getChildren().setAll(homeScreen); }
    private void showMoviesScreen() { root.setLeft(sidebar); contentArea.getChildren().setAll(moviesScreen); }
    private void showTVShowsScreen() { root.setLeft(sidebar); contentArea.getChildren().setAll(tvShowsScreen); }
    private void showCategoriesScreen() { root.setLeft(sidebar); contentArea.getChildren().setAll(categoriesScreen); }
    private void showWatchlistScreen() { root.setLeft(sidebar); refreshWatchlistScreen(); contentArea.getChildren().setAll(watchlistScreen); }
    private void showSettingsScreen() { root.setLeft(sidebar); contentArea.getChildren().setAll(settingsScreen); }

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
    private static class MovieMetadata {
        final String overview;
        final String cast;
        final String directors;
        final String languages;
        final String genres;
        final String releaseDate;
        final String runtime;
        final String rating;

        MovieMetadata(String overview, String cast, String directors, String languages,
                String genres, String releaseDate, String runtime, String rating) {
            this.overview = overview;
            this.cast = cast;
            this.directors = directors;
            this.languages = languages;
            this.genres = genres;
            this.releaseDate = releaseDate;
            this.runtime = runtime;
            this.rating = rating;
        }
    }

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
