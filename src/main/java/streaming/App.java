package streaming;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // Theme Constants
    private static final String TEAL_BG = "#0B1A26";
    private static final String TEAL_PANEL = "#132A38";
    private static final String CYAN_ACCENT = "#00D2FF";
    private static final String GOLD_ACCENT = "#FCA311";
    private static final String TEXT_MUTED = "#8B9EB0";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // Every row shows exactly 5 thumbnails; the rest are reached by scrolling
    private static final int VISIBLE_THUMBNAILS = 5;
    private static final double CARD_GAP = 20;
    private static final double POSTER_ASPECT = 1.5;
    private static final double MIN_CARD_WIDTH = 110;
    private static final double MAX_CARD_WIDTH = 300;
    // Row padding (8 + 8), the gaps between the 5 cards, and a few pixels for the scroll pane border
    private static final double ROW_EXTRA_WIDTH = 16 + (VISIBLE_THUMBNAILS - 1) * CARD_GAP + 4;
    // Horizontal padding around each row (36 + 36)
    private static final double ROW_OUTER_PADDING = 72;

    // Width of a thumbnail card. Everything that depends on it is bound to this property,
    // so resizing the window resizes the thumbnails and the rows keep showing 5 of them.
    private final DoubleProperty cardWidth = new SimpleDoubleProperty(180);

    // Sounds & Custom Controls Helper
    private final SoundsAndButtons soundsAndButtons = new SoundsAndButtons();

    // Layout Managers
    private BorderPane root;
    private StackPane contentArea;
    private VBox sidebar;
    private VBox homeSearchArea;
    private ScrollPane homeSearchResultsPane;

    // Screens
    private StackPane homeScreen;
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
    private boolean detailIsTvShow;
    private long detailRequestId;
    private VBox watchlistScreen;
    private VBox categoriesScreen;
    private VBox settingsScreen;
    private VBox notificationsScreen;
    private CustomButton notificationsBackButton;
    private VBox tvShowsScreen;
    private VBox moviesScreen;
    private BorderPane mediaPlayerScreen;

    // Data Models
    private final List<Movie> allMovies = new ArrayList<>();
    private final List<Movie> allTvShows = new ArrayList<>();
    private final List<Movie> watchlist = new ArrayList<>();
    private final List<Movie> trendingMovies = new ArrayList<>();
    private final List<Movie> recentlyAdded = new ArrayList<>();
    private final List<Movie> popularMovies = new ArrayList<>();
    private final Map<String, MovieMetadata> movieMetadataCache = new HashMap<>();

    // Profile, Settings & Media Player State
    private Stage mainStage;
    private String profileName = "Alexander";
    private String profileEmail = "alexander@example.com";
    private String preferredLanguage = "English";
    private boolean membershipActive = true;
    private CustomButton homeProfileButton;
    private Text homeProfileInitial;
    private String playbackQuality = "1080p";
    private StackPane videoContainer;
    private Region playerScrim;
    private Label videoPlaceholder;
    private Label playbackErrorLabel;
    private Label playerTitleLabel;
    private String previousWindowTitle;
    private Movie currentPlayerMovie;
    private CustomButton fullscreenButton;
    private CustomButton openTrailerButton;
    private StackPane customPlayPauseButton;
    private WebView trailerWebView;
    private String tmdbApiKey;
    private String currentYoutubeKey;
    private long trailerRequestId;
    private FadeTransition loadingPulse;
    private PauseTransition playerLoadTimeout;
    private boolean playerClosing;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initData();
        buildUI(primaryStage);
    }

    private void initData() {
        addMovie("The Dark Knight", "Action", "/images/Movies/Actions/The dark night.jpg");
        addMovie("The Beekeeper", "Action", "/images/Movies/Actions/The Beekeeper.jpg");
        addMovie("John Wick 4", "Action", "/images/Movies/Actions/John Wick 4.jpg");
        addMovie("Inception", "Action", "/images/Movies/Actions/inception.jpg");
        addMovie("Hitman", "Action", "/images/Movies/Actions/hitman.jpg");
        addMovie("Hell or High Water", "Action", "/images/Movies/Actions/Hell or High Water.jpg");
        addMovie("Extraction", "Action", "/images/Movies/Actions/Extraction.jpg");
        addMovie("Central Intelligence", "Action", "/images/Movies/Actions/Central Intelligence.jpg");

        addMovie("Uncharted", "Adventure", "/images/Movies/Adventure/uncharted.jpg");
        addMovie("The Legend of Tarzan", "Adventure", "/images/Movies/Adventure/trazen.jpg");
        addMovie("The Tomorrow War", "Adventure", "/images/Movies/Adventure/The Tomorrow War.jpg");
        addMovie("Shang-Chi and the Legend of the Ten Rings", "Adventure", "/images/Movies/Adventure/SHANG-CHI AND THE LEGEND OF THE TEN RINGS.jpg");
        addMovie("Mission: Impossible", "Adventure", "/images/Movies/Adventure/mission impossible.jpg");
        addMovie("Jurassic Park", "Adventure", "/images/Movies/Adventure/jurassic park.jpg");
        addMovie("Jumanji", "Adventure", "/images/Movies/Adventure/jumanji.jpg");
        addMovie("Dawn of the Planet of the Apes", "Adventure", "/images/Movies/Adventure/Dawn of the planet of apes.jpg");

        addMovie("Tron: Ares", "Sci-Fi", "/images/Movies/Sci-Fi/Tron Ares.jpg");
        addMovie("The Maze Runner: The Death Cure", "Sci-Fi", "/images/Movies/Sci-Fi/The Maze Runner 3 The Death Cure.jpg");
        addMovie("Tenet", "Sci-Fi", "/images/Movies/Sci-Fi/Tenet.jpg");
        addMovie("RoboCop", "Sci-Fi", "/images/Movies/Sci-Fi/Robocop.jpg");
        addMovie("Pacific Rim", "Sci-Fi", "/images/Movies/Sci-Fi/pacific rim.jpg");
        addMovie("Oblivion", "Sci-Fi", "/images/Movies/Sci-Fi/oblivion.jpg");
        addMovie("Elysium", "Sci-Fi", "/images/Movies/Sci-Fi/Movie Elysium.jpg");
        addMovie("Avengers: Endgame", "Sci-Fi", "/images/Movies/Sci-Fi/avengers endgame.jpg");

        addTvShow("Reacher", "Action", "/images/Tv Shows/Action/jack-reacher.jpg");
        addTvShow("The Night Agent", "Action", "/images/Tv Shows/Action/the-night-agent.jpg");
        addTvShow("The Terminal List", "Action", "/images/Tv Shows/Action/the-terminal-list.jpg");
        addTvShow("Daredevil", "Action", "/images/Tv Shows/Action/dare-devil.jpg");
        addTvShow("The Punisher", "Action", "/images/Tv Shows/Action/the-punisher.jpg");
        addTvShow("Jack Ryan", "Action", "/images/Tv Shows/Action/jack-ryan.jpg");
        addTvShow("24", "Action", "/images/Tv Shows/Action/24.jpg");
        addTvShow("The Recruit", "Action", "/images/Tv Shows/Action/the-recruit.jpg");

        addTvShow("One Piece", "Adventure", "/images/Tv Shows/Adventure/one-piece.jpg");
        addTvShow("The Mandalorian", "Adventure", "/images/Tv Shows/Adventure/the-mandolorian.jpg");
        addTvShow("The Witcher", "Adventure", "/images/Tv Shows/Adventure/the-witcher.jpg");
        addTvShow("Lost", "Adventure", "/images/Tv Shows/Adventure/lost.jpg");
        addTvShow("Avatar: The Last Airbender", "Adventure", "/images/Tv Shows/Adventure/avatar-the-last-airbender.jpg");
        addTvShow("Loki", "Adventure", "/images/Tv Shows/Adventure/loki.jpg");
        addTvShow("The Last Kingdom", "Adventure", "/images/Tv Shows/Adventure/the-last-kingdom.jpg");
        addTvShow("The 100", "Adventure", "/images/Tv Shows/Adventure/the-100.jpg");

        addTvShow("Stranger Things", "Sci-Fi", "/images/Tv Shows/Sci-Fi/stranger-things.jpg");
        addTvShow("The Expanse", "Sci-Fi", "/images/Tv Shows/Sci-Fi/the-expanse.jpg");
        addTvShow("Foundation", "Sci-Fi", "/images/Tv Shows/Sci-Fi/foundation.jpg");
        addTvShow("Severance", "Sci-Fi", "/images/Tv Shows/Sci-Fi/severance.jpg");
        addTvShow("Dark", "Sci-Fi", "/images/Tv Shows/Sci-Fi/dark.jpg");
        addTvShow("Black Mirror", "Sci-Fi", "/images/Tv Shows/Sci-Fi/balck-mirror.jpg");
        addTvShow("Westworld", "Sci-Fi", "/images/Tv Shows/Sci-Fi/west-world.jpg");
        addTvShow("Silo", "Sci-Fi", "/images/Tv Shows/Sci-Fi/silo.jpg");

        trendingMovies.addAll(categoryMovies("Action"));
        popularMovies.addAll(categoryMovies("Adventure"));
        recentlyAdded.addAll(categoryMovies("Sci-Fi"));
    }

    private void addMovie(String title, String genre, String imagePath) {
        allMovies.add(new Movie(title, genre, "Feature", "PG-13", "", imagePath, 8.0));
    }

    private void addTvShow(String title, String genre, String imagePath) {
        allTvShows.add(new Movie(title, genre, "TV Series", "TV-14", "", imagePath, 8.0, true));
    }

    private List<Movie> categoryMovies(String category) {
        List<Movie> movies = new ArrayList<>();
        for (Movie movie : allMovies) {
            if (movie.genre.equals(category)) {
                movies.add(movie);
            }
        }
        return movies;
    }

    private List<Movie> tvShowsByGenre(String genre) {
        List<Movie> tvShows = new ArrayList<>();
        for (Movie tvShow : allTvShows) {
            if (tvShow.genre.equals(genre)) {
                tvShows.add(tvShow);
            }
        }
        return tvShows;
    }

    private void buildUI(Stage stage) {
        mainStage = stage;
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        sidebar = createSidebar();
        root.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        root.setCenter(contentArea);

        // Resize the thumbnails whenever the space next to the sidebar changes
        ReadOnlyDoubleProperty contentWidth = contentArea.widthProperty();
        contentWidth.addListener((ObservableValue<? extends Number> prop, Number oldVal, Number newVal) ->
                updateCardWidth(newVal.doubleValue()));

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

        URL cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        URL logoUrl = getClass().getResource("/images/logo.jpeg");
        stage.setTitle("StreamFlix - Premium Stream");
        if (logoUrl != null) {
            stage.getIcons().add(new Image(logoUrl.toExternalForm()));
        }
        stage.setScene(scene);
        stage.setMinWidth(Math.min(800, windowWidth));
        stage.setMinHeight(Math.min(500, windowHeight));
        stage.show();
    }

    // Works out the card width so that 5 cards exactly fill the available row width
    private void updateCardWidth(double availableWidth) {
        if (availableWidth <= 0) {
            return;
        }
        double width = (availableWidth - ROW_OUTER_PADDING - ROW_EXTRA_WIDTH) / VISIBLE_THUMBNAILS;
        cardWidth.set(Math.max(MIN_CARD_WIDTH, Math.min(MAX_CARD_WIDTH, width)));
    }

    // Sidebar
    private VBox createSidebar() {
        VBox side = new VBox(10);
        side.getStyleClass().add("sidebar");
        side.setPrefWidth(86);
        side.setMinWidth(86);
        side.setMaxWidth(86);
        side.setPadding(new Insets(22, 14, 22, 14));

        StackPane logoIcon = new StackPane();
        URL logoUrl = getClass().getResource("/images/logo.jpeg");
        if (logoUrl != null) {
            ImageView logoImage = new ImageView(new Image(logoUrl.toExternalForm()));
            logoImage.setFitWidth(48);
            logoImage.setFitHeight(48);
            logoImage.setPreserveRatio(true);
            Rectangle logoClip = new Rectangle(48, 48);
            logoClip.setArcWidth(16);
            logoClip.setArcHeight(16);
            logoImage.setClip(logoClip);
            logoIcon.getChildren().add(logoImage);
        }
        Tooltip.install(logoIcon, new Tooltip("StreamFlix"));
        logoIcon.setPadding(new Insets(0, 0, 24, 0));

        VBox navLinks = new VBox(14);
        navLinks.setAlignment(Pos.TOP_CENTER);

        CustomButton btnHome = createNavButton("Home");
        CustomButton btnMovies = createNavButton("Movies");
        CustomButton btnTVShows = createNavButton("TV Shows");
        CustomButton btnCategories = createNavButton("Categories");
        CustomButton btnWatchlist = createNavButton("Watchlist");
        CustomButton btnSettings = createNavButton("Settings");

        updateNavButtonStyle(btnHome, true);

        btnHome.setOnAction(e -> {
            showHomeScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnHome, true);
        });
        btnMovies.setOnAction(e -> {
            showMoviesScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnMovies, true);
        });
        btnTVShows.setOnAction(e -> {
            showTVShowsScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnTVShows, true);
        });
        btnCategories.setOnAction(e -> {
            showCategoriesScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnCategories, true);
        });
        btnWatchlist.setOnAction(e -> {
            showWatchlistScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnWatchlist, true);
        });
        btnSettings.setOnAction(e -> {
            showSettingsScreen();
            resetNavs(btnHome, btnMovies, btnTVShows, btnCategories, btnWatchlist, btnSettings);
            updateNavButtonStyle(btnSettings, true);
        });

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

    private CustomButton createNavButton(String text) {
        String icon = navigationIcon(text);
        CustomButton btn = new CustomButton();
        btn.getStyleClass().add("nav-button");
        btn.setGraphic(createPlayerIcon(icon));
        btn.setAccessibleText(text);
        btn.setFocusTraversable(true);
        addButtonSound(btn, false);
        return btn;
    }

    private VBox createNavItem(CustomButton button, String text) {
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

    private void updateNavButtonStyle(CustomButton btn, boolean active) {
        btn.getStyleClass().removeAll("nav-button", "nav-button-active");
        btn.getStyleClass().add(active ? "nav-button-active" : "nav-button");
        setNavIconColor(btn, active);
    }

    private void setNavIconColor(CustomButton btn, boolean active) {
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

    private void resetNavs(CustomButton... buttons) {
        for (CustomButton b : buttons) {
            b.getStyleClass().remove("nav-button-active");
            if (!b.getStyleClass().contains("nav-button")) {
                b.getStyleClass().add("nav-button");
            }
            setNavIconColor(b, false);
        }
    }

    // Top bar (search, notifications, profile)
    private StackPane createTopBar() {
        StackPane topBar = new StackPane();
        topBar.getStyleClass().add("home-top-bar");
        topBar.setPadding(new Insets(18, 38, 18, 38));

        VBox searchArea = new VBox(6);
        searchArea.setMaxWidth(520);
        homeSearchArea = searchArea;

        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M10 3a7 7 0 1 0 0 14a7 7 0 1 0 0-14 M15 15l6 6");
        searchIcon.setFill(Color.TRANSPARENT);
        searchIcon.setStroke(Color.web(TEXT_MUTED));
        searchIcon.setStrokeWidth(2.2);
        searchIcon.setStrokeLineCap(StrokeLineCap.ROUND);
        searchIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
        searchIcon.setMouseTransparent(true);

        StackPane searchIconBox = new StackPane(searchIcon);
        searchIconBox.setMinSize(24, 24);
        searchIconBox.setPrefSize(24, 24);
        searchIconBox.setMaxSize(24, 24);
        searchIconBox.setMouseTransparent(true);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search movies and TV shows by title or genre...");
        searchField.setPrefWidth(390);
        searchField.setFocusTraversable(true);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIconBox, searchField);

        VBox searchResults = new VBox(2);
        searchResults.getStyleClass().add("search-results");

        ScrollPane searchResultsPane = new ScrollPane(searchResults);
        searchResultsPane.getStyleClass().add("search-results-pane");
        searchResultsPane.setFitToWidth(true);
        searchResultsPane.setMaxHeight(520);
        searchResultsPane.setVisible(false);
        searchResultsPane.setManaged(false);
        homeSearchResultsPane = searchResultsPane;

        searchArea.getChildren().add(searchBox);
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
                updateSearchResults(newValue, searchResults, searchResultsPane, searchField));

        searchField.setOnAction(e -> {
            if (!searchResults.getChildren().isEmpty()
                    && searchResults.getChildren().get(0) instanceof CustomButton) {
                ((CustomButton) searchResults.getChildren().get(0)).fire();
            }
        });

        CustomButton notifBtn = new CustomButton();
        notifBtn.getStyleClass().add("notification-button");
        notifBtn.setGraphic(createPlayerIcon("M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9zm-8 12h4a2 2 0 0 1-4 0z"));
        notifBtn.setTooltip(new Tooltip("Notifications"));
        notifBtn.setAccessibleText("Notifications: updates and service terms");
        addButtonSound(notifBtn, false);
        notifBtn.setOnAction(e -> showNotifications(this::showHomeScreen, "Back to Home"));

        homeProfileButton = new CustomButton();
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
        addButtonSound(homeProfileButton, false);
        homeProfileButton.setOnAction(e -> showProfileInfo(this::showHomeScreen));

        HBox actions = new HBox(12, notifBtn, homeProfileButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region leftSpacer = new Region();
        leftSpacer.minWidthProperty().bind(actions.widthProperty());
        leftSpacer.prefWidthProperty().bind(actions.widthProperty());
        leftSpacer.maxWidthProperty().bind(actions.widthProperty());

        BorderPane topBarLayout = new BorderPane();
        topBarLayout.setLeft(leftSpacer);
        topBarLayout.setCenter(searchArea);
        topBarLayout.setRight(actions);

        topBar.getChildren().add(topBarLayout);
        return topBar;
    }

    private void updateHomeProfileButton() {
        homeProfileInitial.setText(profileInitials(profileName));
        homeProfileButton.setTooltip(new Tooltip("Profile for " + profileName));
        homeProfileButton.setAccessibleText("Profile for " + profileName);
    }

    private String profileInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.ROOT);
    }

    private void showNotifications(Runnable backAction, String backLabel) {
        notificationsBackButton.setOnAction(e -> backAction.run());
        notificationsBackButton.setTooltip(new Tooltip(backLabel));
        notificationsBackButton.setAccessibleText(backLabel);
        root.setLeft(sidebar);
        transitionToScreen(notificationsScreen);
        mainStage.setTitle("StreamFlix - Updates & Terms");
    }

    private void createNotificationsScreen() {
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
        updates.getChildren().addAll(
                notificationItem("Movie and TV details",
                        "Cast, creators, languages, and ratings are provided by TMDB."),
                notificationItem("Trailer playback",
                        "Trailers are embedded from YouTube. If playback is unavailable in the app, use the external-link button in the player."),
                notificationItem("Your TMDB credential",
                        "Your API key or read token stays in memory for this app session and is not saved."),
                notificationItem("Terms of service",
                        "This demo is provided under the repository's MIT License. TMDB and YouTube services are subject to their own terms."));

        Label termsHeading = new Label("External service terms");
        termsHeading.getStyleClass().add("detail-section-heading");
        Hyperlink tmdbTerms = new Hyperlink("TMDB Terms of Use");
        tmdbTerms.setOnAction(e -> {
            soundsAndButtons.playClick();
            getHostServices().showDocument("https://www.themoviedb.org/terms-of-use");
        });
        Hyperlink youtubeTerms = new Hyperlink("YouTube Terms of Service");
        youtubeTerms.setOnAction(e -> {
            soundsAndButtons.playClick();
            getHostServices().showDocument("https://www.youtube.com/t/terms");
        });
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
        CustomButton backButton = createPlayerButton(
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
        CustomButton saveButton = createPrimaryButton("Save profile changes");
        saveButton.setOnAction(e -> saveProfileChanges(nameField, emailField, languageField, returnAction));

        VBox profileDetails = new VBox(18, detailsTitle, profileForm, saveButton,
                membershipTitle, membershipDetails);
        if (membershipActive) {
            CustomButton cancelMembershipButton = new CustomButton("Cancel membership");
            cancelMembershipButton.getStyleClass().add("profile-danger-button");
            addButtonSound(cancelMembershipButton, true);
            cancelMembershipButton.setOnAction(e -> confirmMembershipCancellation(returnAction));
            profileDetails.getChildren().add(cancelMembershipButton);
        }

        content.getChildren().addAll(backButton, title, identity, profileDetails);
        scrollPane.setContent(content);
        screen.getChildren().add(scrollPane);
        transitionToScreen(screen);
    }

    private VBox profileField(String labelText, TextField input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("profile-field-label");
        return new VBox(6, label, input);
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

    private void updateSearchResults(String query, VBox results, ScrollPane resultsPane,
            TextField searchField) {
        results.getChildren().clear();
        String normalizedQuery = query == null ? ""
                : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            resultsPane.setVisible(false);
            resultsPane.setManaged(false);
            return;
        }

        List<Movie> searchableMedia = new ArrayList<>(allMovies);
        searchableMedia.addAll(allTvShows);
        List<Movie> matches = new ArrayList<>();
        for (Movie media : searchableMedia) {
            if (media.title.toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    || media.genre.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                matches.add(media);
            }
        }

        if (matches.isEmpty()) {
            Label emptyResult = new Label("Not available in this app");
            emptyResult.getStyleClass().add("search-empty-result");
            results.getChildren().add(emptyResult);
        } else {
            Label resultHeading = new Label(matches.size() + " available in this app");
            resultHeading.getStyleClass().add("search-results-heading");
            results.getChildren().add(resultHeading);
            for (Movie media : matches) {
                String mediaType = media.isTvShow ? "TV Show" : "Movie";
                CustomButton result = new CustomButton(mediaType + "  |  " + media.title + "  -  " + media.genre);
                result.getStyleClass().add("search-result-button");
                result.setMaxWidth(Double.MAX_VALUE);
                result.setMinHeight(60);
                result.setPrefHeight(60);
                result.setAlignment(Pos.CENTER_LEFT);
                addButtonSound(result, false);
                result.setOnAction(e -> {
                    searchField.clear();
                    resultsPane.setVisible(false);
                    resultsPane.setManaged(false);
                    openPlayer(media);
                });
                results.getChildren().add(result);
            }
        }

        resultsPane.setPrefViewportHeight(Math.min(600, 68 + matches.size() * 68));
        resultsPane.setVvalue(0);
        resultsPane.setManaged(true);
        resultsPane.setVisible(true);
    }

    // Home screen
    private void createHomeScreen() {
        homeScreen = new StackPane();
        homeScreen.getStyleClass().add("screen");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox contentBox = new VBox(26);
        contentBox.setPadding(new Insets(16, 0, 40, 0));

        Text moviesHeading = new Text("Movies");
        moviesHeading.getStyleClass().add("section-title");
        VBox.setMargin(moviesHeading, new Insets(0, 36, -12, 36));

        VBox trendingRow = createMovieRow("Trending Now", trendingMovies);
        VBox popularRow = createMovieRow("Popular in Adventure", popularMovies);
        VBox recentlyRow = createMovieRow("Recently Added in Sci-Fi", recentlyAdded);

        Text tvHeading = new Text("TV Series");
        tvHeading.getStyleClass().add("section-title");
        VBox.setMargin(tvHeading, new Insets(12, 36, -12, 36));

        VBox actionTvRow = createMovieRow("Action TV Shows", tvShowsByGenre("Action"));
        VBox adventureTvRow = createMovieRow("Adventure TV Shows", tvShowsByGenre("Adventure"));
        VBox sciFiTvRow = createMovieRow("Sci-Fi TV Shows", tvShowsByGenre("Sci-Fi"));

        contentBox.getChildren().addAll(
                moviesHeading, trendingRow, popularRow, recentlyRow,
                tvHeading, actionTvRow, adventureTvRow, sciFiTvRow);
        scrollPane.setContent(contentBox);

        StackPane topBar = createTopBar();
        VBox homeLayout = new VBox(0, topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        homeScreen.getChildren().add(homeLayout);

        StackPane.setAlignment(homeSearchResultsPane, Pos.TOP_CENTER);
        StackPane.setMargin(homeSearchResultsPane, new Insets(82, 0, 0, 0));
        homeSearchResultsPane.minWidthProperty().bind(homeSearchArea.widthProperty());
        homeSearchResultsPane.prefWidthProperty().bind(homeSearchArea.widthProperty());
        homeSearchResultsPane.maxWidthProperty().bind(homeSearchArea.widthProperty());
        homeScreen.getChildren().add(homeSearchResultsPane);
    }

    private VBox createMovieRow(String title, List<Movie> movies) {
        VBox section = new VBox(15);
        section.setAlignment(Pos.TOP_LEFT);
        section.setPadding(new Insets(0, 36, 0, 36));

        Text titleText = new Text(title);
        titleText.getStyleClass().add("section-title");

        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("scroll-pane");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.prefWidthProperty().bind(cardWidth.multiply(VISIBLE_THUMBNAILS).add(ROW_EXTRA_WIDTH));
        scroll.maxWidthProperty().bind(scroll.prefWidthProperty());

        HBox cardBox = new HBox(20);
        cardBox.setPadding(new Insets(12, 8, 26, 8));

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
        card.prefWidthProperty().bind(cardWidth);
        card.minWidthProperty().bind(cardWidth);
        card.maxWidthProperty().bind(cardWidth);

        StackPane posterPane = new StackPane();
        posterPane.getStyleClass().add("poster-frame");
        posterPane.prefWidthProperty().bind(cardWidth);
        posterPane.minWidthProperty().bind(cardWidth);
        posterPane.maxWidthProperty().bind(cardWidth);
        posterPane.prefHeightProperty().bind(cardWidth.multiply(POSTER_ASPECT));
        posterPane.minHeightProperty().bind(cardWidth.multiply(POSTER_ASPECT));
        posterPane.maxHeightProperty().bind(cardWidth.multiply(POSTER_ASPECT));

        // The poster keeps its 2:3 shape, so the crop in createPosterImageView stays valid at any size
        ImageView posterImage = createPosterImageView(movie.imageUrl, 2, 3);
        posterImage.fitWidthProperty().bind(cardWidth);
        posterImage.fitHeightProperty().bind(cardWidth.multiply(POSTER_ASPECT));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cardWidth);
        clip.heightProperty().bind(cardWidth.multiply(POSTER_ASPECT));
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        posterPane.setClip(clip);

        Label ratingBadge = new Label(String.valueOf(movie.ratingScore));
        ratingBadge.getStyleClass().add("rating-badge");
        StackPane.setAlignment(ratingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(ratingBadge, new Insets(10));

        StackPane playOverlay = new StackPane();
        playOverlay.getStyleClass().add("play-overlay");
        playOverlay.maxWidthProperty().bind(cardWidth);
        playOverlay.maxHeightProperty().bind(cardWidth.multiply(POSTER_ASPECT));
        playOverlay.setOpacity(0);
        playOverlay.setMouseTransparent(true);

        Text playIcon = new Text("Play");
        playIcon.getStyleClass().add("play-icon");
        playOverlay.getChildren().add(playIcon);

        posterPane.getChildren().addAll(posterImage, ratingBadge, playOverlay);

        Text movieTitle = new Text(movie.title);
        movieTitle.getStyleClass().add("movie-title");
        movieTitle.wrappingWidthProperty().bind(cardWidth);

        Text movieGenre = new Text(movie.genre);
        movieGenre.getStyleClass().add("movie-genre");

        card.getChildren().addAll(posterPane, movieTitle, movieGenre);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), card);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        scaleIn.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), playOverlay);
        fadeIn.setToValue(1);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), playOverlay);
        fadeOut.setToValue(0);

        ColorAdjust posterBrightness = new ColorAdjust();
        posterImage.setEffect(posterBrightness);
        Timeline brightenPoster = new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(posterBrightness.brightnessProperty(), 0.08, Interpolator.EASE_BOTH)));
        Timeline restorePosterBrightness = new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(posterBrightness.brightnessProperty(), 0, Interpolator.EASE_BOTH)));

        card.setOnMouseEntered(e -> {
            soundsAndButtons.playHoverToggle();
            playOverlay.setMouseTransparent(false);
            scaleIn.playFromStart();
            fadeIn.playFromStart();
            brightenPoster.playFromStart();
            card.setEffect(new DropShadow(20, Color.web(CYAN_ACCENT, 0.3)));
            card.setViewOrder(-1);
        });
        card.setOnMouseExited(e -> {
            playOverlay.setMouseTransparent(true);
            scaleOut.playFromStart();
            fadeOut.playFromStart();
            restorePosterBrightness.playFromStart();
            card.setEffect(null);
            card.setViewOrder(0);
        });

        soundsAndButtons.makePosterHoverable(card);
        soundsAndButtons.makePosterClickable(card, () -> openPlayer(movie));

        return card;
    }

    // Media player screen
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

        customPlayPauseButton = soundsAndButtons.createPlayPauseControl(false, playing -> {
            if (trailerWebView != null && trailerWebView.getEngine() != null) {
                String jsCommand = playing
                        ? "var v = document.querySelector('video'); if (v) v.play();"
                        : "var v = document.querySelector('video'); if (v) v.pause();";
                trailerWebView.getEngine().executeScript(jsCommand);
            }
        });

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

        HBox playerFooter = new HBox(12, customPlayPauseButton, sourceLabel, openTrailerButton, footerSpacer, fullscreenButton);
        playerFooter.setAlignment(Pos.CENTER_LEFT);
        playerFooter.getStyleClass().add("player-controls");

        videoContainer.getChildren().addAll(trailerWebView, videoPlaceholder, playbackErrorLabel);
        trailerWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED
                    && trailerWebView.isVisible() && currentYoutubeKey != null) {
                stopPlayerLoading();
                videoPlaceholder.setVisible(false);
            } else if (newState == Worker.State.FAILED
                    && trailerWebView.isVisible() && currentYoutubeKey != null) {
                showPlaybackError("The embedded YouTube player could not load. Use Open in browser to watch this trailer.");
            }
        });
        StackPane.setAlignment(videoPlaceholder, Pos.CENTER);
        StackPane.setAlignment(playbackErrorLabel, Pos.TOP_CENTER);
        StackPane.setMargin(playbackErrorLabel, new Insets(18));

        CustomButton backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", "Back");
        backButton.setOnAction(e -> closePlayer());
        playerTitleLabel = new Label("Trailer");
        playerTitleLabel.getStyleClass().add("player-title");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        CustomButton detailsButton = createSecondaryButton("Details");
        detailsButton.setOnAction(e -> openDetailsFromPlayer());
        HBox playerHeader = new HBox(18, backButton, playerTitleLabel, headerSpacer, detailsButton);
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

    private CustomButton createPlayerButton(String iconPath, String styleClass, String tooltip) {
        CustomButton button = new CustomButton();
        button.getStyleClass().add(styleClass);
        button.setGraphic(createPlayerIcon(iconPath));
        button.setTooltip(new Tooltip(tooltip));
        button.setAccessibleText(tooltip);
        addButtonSound(button, false);
        return button;
    }

    private StackPane createPlayerIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.WHITE);
        icon.setMouseTransparent(true);
        double iconWidth = icon.getBoundsInLocal().getWidth();
        double iconHeight = icon.getBoundsInLocal().getHeight();
        double maxDim = Math.max(iconWidth, iconHeight);
        double scale = maxDim > 0 ? 20.0 / maxDim : 1.0;
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
        playerClosing = false;
        currentPlayerMovie = movie;
        root.setLeft(null);
        previousWindowTitle = mainStage.getTitle();
        trailerRequestId++;
        trailerWebView.getEngine().load("about:blank");
        trailerWebView.setVisible(false);
        currentYoutubeKey = null;
        openTrailerButton.setDisable(true);
        playerTitleLabel.setText(movie.title);
        startPlayerLoading("Looking up the trailer for " + movie.title + "...");
        playbackErrorLabel.setVisible(false);

        StackPane.setAlignment(playerScrim, Pos.CENTER);
        StackPane.setAlignment(mediaPlayerScreen, Pos.CENTER);
        playerScrim.setOpacity(0);
        mediaPlayerScreen.setOpacity(0);
        contentArea.getChildren().removeAll(playerScrim, mediaPlayerScreen);
        contentArea.getChildren().addAll(playerScrim, mediaPlayerScreen);
        ParallelTransition playerEntrance = new ParallelTransition(
                fadeIn(playerScrim), fadeIn(mediaPlayerScreen));
        playerEntrance.play();
        mainStage.setTitle("StreamFlix - " + movie.title);

        if (tmdbApiKey == null) {
            tmdbApiKey = requestTmdbApiKey();
            if (tmdbApiKey == null) {
                stopPlayerLoading();
                videoPlaceholder.setText("A TMDB API key is required to fetch trailers.");
                return;
            }
        }
        fetchTrailer(movie, tmdbApiKey);
    }

    private String requestTmdbApiKey() {
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
        CompletableFuture.supplyAsync(() -> lookupTrailer(movie, apiKey))
                .whenComplete((youtubeKey, error) -> Platform.runLater(() -> {
                    if (requestId != trailerRequestId) {
                        return;
                    }
                    if (error != null) {
                        Throwable cause = error instanceof CompletionException
                                ? error.getCause() : error;
                        if (cause instanceof InvalidTmdbApiKeyException
                                && apiKey.equals(tmdbApiKey)) {
                            tmdbApiKey = null;
                        }
                        showPlaybackError(cause.getMessage());
                        return;
                    }
                    playbackErrorLabel.setVisible(false);
                    currentYoutubeKey = youtubeKey;
                    openTrailerButton.setDisable(false);
                    startPlayerLoading("Loading trailer...");
                    trailerWebView.setVisible(true);
                    playerLoadTimeout = new PauseTransition(Duration.seconds(30));
                    playerLoadTimeout.setOnFinished(timeout -> {
                        if (currentYoutubeKey != null && trailerWebView.isVisible()) {
                            showPlaybackError("The embedded YouTube player is taking too long to load. Use Open in browser to watch this trailer.");
                        }
                    });
                    playerLoadTimeout.playFromStart();
                    trailerWebView.getEngine().load(
                            "https://www.youtube-nocookie.com/embed/" + youtubeKey
                                    + "?autoplay=1&rel=0");
                }));
    }

    private String lookupTrailer(Movie media, String apiKey) {
        String mediaId = findTmdbMediaId(media, apiKey);
        String mediaType = media.isTvShow ? "tv" : "movie";
        JsonObject videosResponse = getTmdbJson(
                "https://api.themoviedb.org/3/" + mediaType + "/" + mediaId + "/videos", apiKey);
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
            throw new TrailerLookupException("TMDB has no YouTube trailer available for "
                    + media.title + ".");
        }

        String youtubeKey = jsonString(firstTrailer, "key");
        if (!youtubeKey.matches("[A-Za-z0-9_-]+")) {
            throw new TrailerLookupException("TMDB returned an invalid YouTube trailer ID.");
        }

        return youtubeKey;
    }

    private MovieMetadata lookupMovieMetadata(Movie media, String apiKey) {
        String mediaId = findTmdbMediaId(media, apiKey);
        String mediaType = media.isTvShow ? "tv" : "movie";
        JsonObject details = getTmdbJson(
                "https://api.themoviedb.org/3/" + mediaType + "/" + mediaId
                        + "?append_to_response=credits",
                apiKey);
        JsonObject credits = details.has("credits") && details.get("credits").isJsonObject()
                ? details.getAsJsonObject("credits") : new JsonObject();

        return new MovieMetadata(
                jsonString(details, "overview"),
                joinJsonValues(credits, "cast", "name", 6),
                media.isTvShow
                        ? joinJsonValues(details, "created_by", "name", 6)
                        : joinCrewDirectors(credits),
                joinJsonValues(details, "spoken_languages", "name", 0),
                joinJsonValues(details, "genres", "name", 0),
                jsonString(details, media.isTvShow ? "first_air_date" : "release_date"),
                media.isTvShow ? tvSeriesLength(details)
                        : details.has("runtime") && !details.get("runtime").isJsonNull()
                                ? details.get("runtime").getAsString() + " min" : "",
                details.has("vote_average") && !details.get("vote_average").isJsonNull()
                        ? String.format(Locale.ROOT, "%.1f / 10",
                                details.get("vote_average").getAsDouble()) : "");
    }

    private String findTmdbMediaId(Movie media, String apiKey) {
        String encodedTitle = URLEncoder.encode(media.title, StandardCharsets.UTF_8);
        String mediaType = media.isTvShow ? "tv" : "movie";
        String titleField = media.isTvShow ? "name" : "title";
        JsonObject searchResponse = getTmdbJson(
                "https://api.themoviedb.org/3/search/" + mediaType + "?query=" + encodedTitle,
                apiKey);
        JsonArray results = searchResponse.has("results")
                ? searchResponse.getAsJsonArray("results") : new JsonArray();
        if (results.size() == 0) {
            throw new TrailerLookupException("TMDB did not find a "
                    + (media.isTvShow ? "TV show" : "movie") + " titled \""
                    + media.title + "\".");
        }

        JsonObject selectedMedia = results.get(0).getAsJsonObject();
        for (JsonElement result : results) {
            if (result.isJsonObject()
                    && media.title.equalsIgnoreCase(
                            jsonString(result.getAsJsonObject(), titleField))) {
                selectedMedia = result.getAsJsonObject();
                break;
            }
        }
        if (!selectedMedia.has("id")) {
            throw new TrailerLookupException("TMDB returned a "
                    + (media.isTvShow ? "TV show" : "movie") + " without an ID.");
        }
        return selectedMedia.get("id").getAsString();
    }

    private static String tvSeriesLength(JsonObject details) {
        List<String> values = new ArrayList<>();
        int seasons = details.has("number_of_seasons")
                ? details.get("number_of_seasons").getAsInt() : 0;
        int episodes = details.has("number_of_episodes")
                ? details.get("number_of_episodes").getAsInt() : 0;
        if (seasons > 0) {
            values.add(seasons + (seasons == 1 ? " season" : " seasons"));
        }
        if (episodes > 0) {
            values.add(episodes + (episodes == 1 ? " episode" : " episodes"));
        }
        if (details.has("episode_run_time") && details.get("episode_run_time").isJsonArray()
                && details.getAsJsonArray("episode_run_time").size() > 0) {
            JsonElement runtime = details.getAsJsonArray("episode_run_time").get(0);
            if (runtime.isJsonPrimitive()) {
                values.add(runtime.getAsString() + " min per episode");
            }
        }
        return String.join(", ", values);
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
        soundsAndButtons.playClick();
        getHostServices().showDocument("https://www.youtube.com/watch?v=" + currentYoutubeKey);
    }

    private void closePlayer() {
        if (playerClosing) {
            return;
        }
        playerClosing = true;
        trailerRequestId++;
        stopPlayerLoading();
        trailerWebView.getEngine().load("about:blank");
        trailerWebView.setVisible(false);
        currentYoutubeKey = null;
        openTrailerButton.setDisable(true);
        root.setLeft(sidebar);
        if (mainStage.isFullScreen()) {
            mainStage.setFullScreen(false);
        }
        mainStage.setTitle(previousWindowTitle == null
                ? "StreamFlix - Premium Stream" : previousWindowTitle);
        ParallelTransition playerExit = new ParallelTransition(
                fadeOut(mediaPlayerScreen), fadeOut(playerScrim));
        playerExit.setOnFinished(e -> {
            contentArea.getChildren().removeAll(mediaPlayerScreen, playerScrim);
            mediaPlayerScreen.setOpacity(1);
            playerScrim.setOpacity(1);
        });
        playerExit.play();
    }

    // Closes the player and opens the details page (cast, overview, watchlist) of the current trailer
    private void openDetailsFromPlayer() {
        Movie movie = currentPlayerMovie;
        closePlayer();
        if (movie != null) {
            showMovieDetails(movie);
        }
    }

    private void showPlaybackError(String message) {
        stopPlayerLoading();
        playbackErrorLabel.setText(message == null || message.isEmpty()
                ? "Unable to load this trailer." : message);
        playbackErrorLabel.setVisible(true);
        videoPlaceholder.setVisible(false);
        trailerWebView.setVisible(false);
    }

    private void toggleFullscreen() {
        soundsAndButtons.playToggle();
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    private void startPlayerLoading(String message) {
        stopPlayerLoading();
        videoPlaceholder.setText(message);
        videoPlaceholder.setOpacity(1.0);
        videoPlaceholder.setVisible(true);
        loadingPulse = new FadeTransition(Duration.millis(800), videoPlaceholder);
        loadingPulse.setFromValue(1.0);
        loadingPulse.setToValue(0.4);
        loadingPulse.setAutoReverse(true);
        loadingPulse.setCycleCount(Animation.INDEFINITE);
        loadingPulse.setInterpolator(Interpolator.EASE_BOTH);
        loadingPulse.play();
    }

    private void stopPlayerLoading() {
        if (loadingPulse != null) {
            loadingPulse.stop();
            loadingPulse = null;
        }
        if (playerLoadTimeout != null) {
            playerLoadTimeout.stop();
            playerLoadTimeout = null;
        }
        if (videoPlaceholder != null) {
            videoPlaceholder.setOpacity(1.0);
        }
    }

    private static FadeTransition fadeIn(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(200), node);
        transition.setFromValue(0.0);
        transition.setToValue(1.0);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private static FadeTransition fadeOut(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(180), node);
        transition.setFromValue(1.0);
        transition.setToValue(0.0);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }

    private void transitionToScreen(Node screen) {
        root.setLeft(sidebar);
        screen.setOpacity(0.0);
        contentArea.getChildren().setAll(screen);
        fadeIn(screen).play();
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

    // Movie details screen
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
        detailIsTvShow = movie.isTvShow;

        CustomButton backButton = createPlayerButton(
                "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z",
                "player-back-button", movie.isTvShow ? "Back to TV Shows" : "Back to Home");
        backButton.setOnAction(e -> {
            if (movie.isTvShow) {
                showTVShowsScreen();
            } else {
                showHomeScreen();
            }
        });

        CustomButton playButton = createPrimaryButton("Play Trailer");
        playButton.setOnAction(e -> openPlayer(movie));
        CustomButton watchlistButton = createSecondaryButton(
                watchlist.contains(movie) ? "In Watchlist" : "Add to Watchlist");
        watchlistButton.setOnAction(e -> {
            soundsAndButtons.playToggle();
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
        ImageView posterImage = createPosterImageView(movie.imageUrl, 300, 450);
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
                + String.format(Locale.ROOT, "%.1f", movie.ratingScore) + " / 10");
        detailsMeta.getStyleClass().add("detail-meta");

        detailOverviewLabel = new Label(movie.description == null || movie.description.isEmpty()
                ? "Loading overview..." : movie.description);
        detailOverviewLabel.getStyleClass().add("detail-synopsis");
        detailOverviewLabel.setWrapText(true);
        detailOverviewLabel.setMaxWidth(Double.MAX_VALUE);

        Label creditsHeading = new Label(movie.isTvShow ? "Cast & creators" : "Cast & crew");
        creditsHeading.getStyleClass().add("detail-section-heading");
        detailCastLabel = createDetailValue("Cast", "Loading...");
        detailDirectorLabel = createDetailValue(
                movie.isTvShow ? "Creators" : "Director", "Loading...");
        detailLanguagesLabel = createDetailValue("Languages", "Loading...");
        detailGenresLabel = createDetailValue("Genres", movie.genre);
        detailReleaseLabel = createDetailValue(
                movie.isTvShow ? "First aired" : "Release", "Loading...");
        detailRuntimeLabel = createDetailValue(
                movie.isTvShow ? "Seasons / episodes" : "Runtime", movie.duration);
        detailRatingLabel = createDetailValue("TMDB rating", "Loading...");
        detailMetadataStatusLabel = new Label("Loading "
                + (movie.isTvShow ? "TV show" : "movie") + " information from TMDB...");
        detailMetadataStatusLabel.getStyleClass().add("detail-metadata-status");
        detailMetadataStatusLabel.setWrapText(true);

        infoBox.getChildren().addAll(title, detailsMeta, detailOverviewLabel, creditsHeading,
                detailCastLabel, detailDirectorLabel, detailLanguagesLabel,
                detailGenresLabel, detailReleaseLabel, detailRuntimeLabel,
                detailRatingLabel, detailMetadataStatusLabel);
        hero.getChildren().addAll(posterImage, infoBox);
        movieDetailsContainer.getChildren().addAll(pageHeader, hero);

        transitionToScreen(movieDetailsScreen);
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
        String metadataKey = metadataCacheKey(movie);
        MovieMetadata cachedMetadata = movieMetadataCache.get(metadataKey);
        if (cachedMetadata != null) {
            applyMovieMetadata(cachedMetadata);
            return;
        }

        if (tmdbApiKey == null) {
            tmdbApiKey = requestTmdbApiKey();
        }
        if (tmdbApiKey == null) {
            detailMetadataStatusLabel.setText(
                    "TMDB key not entered. Select this "
                            + (movie.isTvShow ? "TV show" : "movie")
                            + " again to load its cast and details.");
            setDetailLoadingValues("Not available");
            return;
        }

        String apiKey = tmdbApiKey;
        CompletableFuture.supplyAsync(() -> lookupMovieMetadata(movie, apiKey))
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
                    movieMetadataCache.put(metadataKey, metadata);
                    applyMovieMetadata(metadata);
                }));
    }

    private static String metadataCacheKey(Movie media) {
        return (media.isTvShow ? "tv:" : "movie:") + media.title;
    }

    private void setDetailLoadingValues(String value) {
        detailCastLabel.setText("Cast: " + value);
        detailDirectorLabel.setText((detailIsTvShow ? "Creators: " : "Director: ") + value);
        detailLanguagesLabel.setText("Languages: " + value);
        detailGenresLabel.setText("Genres: " + value);
        detailReleaseLabel.setText((detailIsTvShow ? "First aired: " : "Release: ") + value);
        detailRuntimeLabel.setText(
                (detailIsTvShow ? "Seasons / episodes: " : "Runtime: ") + value);
        detailRatingLabel.setText("TMDB rating: " + value);
    }

    private void applyMovieMetadata(MovieMetadata metadata) {
        detailOverviewLabel.setText(metadata.overview.isEmpty()
                ? "No overview is available for this "
                        + (detailIsTvShow ? "TV show." : "movie.") : metadata.overview);
        detailCastLabel.setText("Cast: " + displayOrUnavailable(metadata.cast));
        detailDirectorLabel.setText((detailIsTvShow ? "Creators: " : "Director: ")
                + displayOrUnavailable(metadata.directors));
        detailLanguagesLabel.setText("Languages: " + displayOrUnavailable(metadata.languages));
        detailGenresLabel.setText("Genres: " + displayOrUnavailable(metadata.genres));
        detailReleaseLabel.setText((detailIsTvShow ? "First aired: " : "Release: ")
                + displayOrUnavailable(metadata.releaseDate));
        detailRuntimeLabel.setText((detailIsTvShow ? "Seasons / episodes: " : "Runtime: ")
                + displayOrUnavailable(metadata.runtime));
        detailRatingLabel.setText("TMDB rating: " + displayOrUnavailable(metadata.rating));
        detailMetadataStatusLabel.setText("");
    }

    private static String displayOrUnavailable(String value) {
        return value == null || value.isEmpty() ? "Not available" : value;
    }

    // Other screens
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
        if (container == null) {
            return;
        }
        container.getChildren().clear();

        if (watchlist.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(100, 0, 100, 0));
            Text emptyText = new Text("Your watchlist is empty");
            emptyText.getStyleClass().add("empty-title");
            Text emptySub = new Text("Movies and TV shows you add will appear here.");
            emptySub.getStyleClass().add("empty-subtitle");
            emptyBox.getChildren().addAll(emptyText, emptySub);
            container.getChildren().add(emptyBox);
        } else {
            for (Movie movie : new ArrayList<>(watchlist)) {
                HBox row = new HBox(25);
                row.getStyleClass().add("watchlist-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15, 20, 15, 20));

                ImageView miniPoster = createPosterImageView(movie.imageUrl, 70, 105);
                Rectangle clip = new Rectangle(70, 105);
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                miniPoster.setClip(clip);

                VBox info = new VBox(8);
                Text mTitle = new Text(movie.title);
                mTitle.getStyleClass().add("watchlist-title");
                Text mGenre = new Text(movie.genre + "  |  " + movie.duration);
                mGenre.getStyleClass().add("watchlist-genre");
                info.getChildren().addAll(mTitle, mGenre);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                CustomButton playBtn = createPrimaryButton("Play");
                playBtn.setPrefWidth(100);
                playBtn.setPrefHeight(40);
                playBtn.setOnAction(e -> openPlayer(movie));

                CustomButton removeBtn = new CustomButton("Remove");
                removeBtn.getStyleClass().add("remove-button");
                removeBtn.setPrefWidth(100);
                removeBtn.setPrefHeight(40);
                addButtonSound(removeBtn, true);
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

        VBox content = new VBox(26);
        content.setPadding(new Insets(16, 0, 40, 0));
        Text title = new Text("Browse Categories");
        title.getStyleClass().add("screen-title");
        VBox.setMargin(title, new Insets(0, 36, -12, 36));

        Text moviesHeading = new Text("Movies");
        moviesHeading.getStyleClass().add("section-title");
        VBox.setMargin(moviesHeading, new Insets(0, 36, -12, 36));
        content.getChildren().add(title);
        content.getChildren().add(moviesHeading);
        content.getChildren().add(createMovieRow("Action", categoryMovies("Action")));
        content.getChildren().add(createMovieRow("Adventure", categoryMovies("Adventure")));
        content.getChildren().add(createMovieRow("Sci-Fi", categoryMovies("Sci-Fi")));

        Text tvShowsHeading = new Text("TV Series");
        tvShowsHeading.getStyleClass().add("section-title");
        VBox.setMargin(tvShowsHeading, new Insets(12, 36, -12, 36));
        content.getChildren().add(tvShowsHeading);
        content.getChildren().add(createMovieRow("Action TV Shows", tvShowsByGenre("Action")));
        content.getChildren().add(createMovieRow("Adventure TV Shows", tvShowsByGenre("Adventure")));
        content.getChildren().add(createMovieRow("Sci-Fi TV Shows", tvShowsByGenre("Sci-Fi")));

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

        item.getChildren().add(textBox);
        item.setOnMouseEntered(e -> item.getStyleClass().add("setting-item-hover"));
        item.setOnMouseExited(e -> item.getStyleClass().remove("setting-item-hover"));
        item.setOnMouseClicked(e -> {
            soundsAndButtons.playClick();
            action.run();
        });
        item.setFocusTraversable(true);
        item.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                soundsAndButtons.playClick();
                action.run();
            }
        });
        return item;
    }

    private void showPlaybackSettings() {
        Label description = new Label("Choose your preferred video quality. The quality available during playback depends on the video and playback provider.");
        description.getStyleClass().add("detail-synopsis");
        description.setWrapText(true);

        Label qualityHeading = new Label("Preferred video quality");
        qualityHeading.getStyleClass().add("detail-section-heading");
        FlowPane qualityOptions = new FlowPane(12, 12);
        for (String quality : List.of("360p", "480p", "720p", "1080p", "2K (1440p)")) {
            CustomButton option = new CustomButton(quality);
            option.getStyleClass().add("quality-option");
            if (quality.equals(playbackQuality)) {
                option.getStyleClass().add("quality-option-selected");
            }
            option.setAccessibleText("Preferred video quality: " + quality);
            addButtonSound(option, true);
            option.setOnAction(e -> {
                playbackQuality = quality;
                showPlaybackSettings();
            });
            qualityOptions.getChildren().add(option);
        }

        VBox details = new VBox(18, description, qualityHeading, qualityOptions);
        VBox settings = createSettingsDetailScreen("Playback", details);
        transitionToScreen(settings);
    }

    private void showPrivacySettings() {
        Label introduction = new Label("Please review this notice before using the StreamFlix demo. By continuing to use the app, you acknowledge how it handles information and that connected services have their own terms.");
        introduction.getStyleClass().add("detail-synopsis");
        introduction.setWrapText(true);

        VBox details = new VBox(14,
                notificationItem("Information and credentials",
                        "Movie and TV searches and details are requested from TMDB. Any TMDB API key or read token entered for this app remains in memory for the current session and is not saved."),
                notificationItem("Watchlist and playback preference",
                        "Your watchlist and selected playback quality are held in app memory for the current run. This demo does not create an account or process payments."),
                notificationItem("Third-party services",
                        "Trailers may be embedded from YouTube, and movie and TV information comes from TMDB. These providers may handle connection or usage data under their own privacy policies and terms."),
                notificationItem("Your choice",
                        "If you do not agree with these conditions, do not use the features that connect to TMDB or YouTube. You can close the app at any time."));

        Label termsHeading = new Label("Provider terms");
        termsHeading.getStyleClass().add("detail-section-heading");
        Hyperlink tmdbTerms = new Hyperlink("TMDB Terms of Use");
        tmdbTerms.setOnAction(e -> {
            soundsAndButtons.playClick();
            getHostServices().showDocument("https://www.themoviedb.org/terms-of-use");
        });
        Hyperlink youtubeTerms = new Hyperlink("YouTube Terms of Service");
        youtubeTerms.setOnAction(e -> {
            soundsAndButtons.playClick();
            getHostServices().showDocument("https://www.youtube.com/t/terms");
        });
        HBox termsLinks = new HBox(16, tmdbTerms, youtubeTerms);

        details.getChildren().addAll(termsHeading, termsLinks);
        VBox privacyContent = new VBox(18, introduction, details);
        VBox settings = createSettingsDetailScreen("Privacy & Service Terms", privacyContent);
        transitionToScreen(settings);
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

        CustomButton backButton = createScreenBackButton("Back to Settings", this::showSettingsScreen);
        Text screenTitle = new Text(title);
        screenTitle.getStyleClass().add("screen-title");
        content.getChildren().addAll(backButton, screenTitle, details);
        scrollPane.setContent(content);
        screen.getChildren().add(scrollPane);
        return screen;
    }

    private CustomButton createScreenBackButton(String label, Runnable action) {
        CustomButton backButton = createPlayerButton(
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
        content.setPadding(new Insets(40, 0, 60, 0));
        content.setAlignment(Pos.TOP_LEFT);
        Text title = new Text("TV Shows");
        title.getStyleClass().add("screen-title");
        VBox.setMargin(title, new Insets(0, 36, 0, 36));

        content.getChildren().add(title);

        for (String genre : new String[] {"Action", "Adventure", "Sci-Fi"}) {
            content.getChildren().add(createMovieRow(genre + " TV Shows", tvShowsByGenre(genre)));
        }
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
        content.setPadding(new Insets(40, 0, 60, 0));
        content.setAlignment(Pos.TOP_LEFT);
        Text title = new Text("Movies");
        title.getStyleClass().add("screen-title");
        VBox.setMargin(title, new Insets(0, 36, 0, 36));

        content.getChildren().add(title);
        for (String genre : new String[] {"Action", "Adventure", "Sci-Fi"}) {
            content.getChildren().add(createMovieRow(genre + " Movies", categoryMovies(genre)));
        }
        scrollPane.setContent(content);
        moviesScreen.getChildren().add(scrollPane);
    }

    // Screen switching and reusable buttons
    private void showHomeScreen() {
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(homeScreen);
    }

    private void showMoviesScreen() {
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(moviesScreen);
    }

    private void showTVShowsScreen() {
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(tvShowsScreen);
    }

    private void showCategoriesScreen() {
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(categoriesScreen);
    }

    private void showWatchlistScreen() {
        root.setLeft(sidebar);
        refreshWatchlistScreen();
        contentArea.getChildren().setAll(watchlistScreen);
    }

    private void showSettingsScreen() {
        root.setLeft(sidebar);
        contentArea.getChildren().setAll(settingsScreen);
    }

    private Image loadPoster(String resourcePath) {
        URL posterResource = getClass().getResource(resourcePath);
        return posterResource == null
                ? new WritableImage(1, 1)
                : new Image(posterResource.toExternalForm(), true);
    }

    private ImageView createPosterImageView(String resourcePath, double width, double height) {
        Image image = loadPoster(resourcePath);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Runnable cropToFrame = () -> {
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            double frameRatio = width / height;
            double imageRatio = imageWidth / imageHeight;
            double viewportWidth = imageWidth;
            double viewportHeight = imageHeight;
            if (imageRatio > frameRatio) {
                viewportWidth = imageHeight * frameRatio;
            } else {
                viewportHeight = imageWidth / frameRatio;
            }

            imageView.setViewport(new Rectangle2D(
                    (imageWidth - viewportWidth) / 2,
                    (imageHeight - viewportHeight) / 2,
                    viewportWidth,
                    viewportHeight));
        };
        cropToFrame.run();
        image.widthProperty().addListener((observable, oldWidth, newWidth) -> cropToFrame.run());
        image.heightProperty().addListener((observable, oldHeight, newHeight) -> cropToFrame.run());
        return imageView;
    }

    private CustomButton createPrimaryButton(String text) {
        CustomButton btn = new CustomButton(text);
        btn.getStyleClass().add("primary-button");
        btn.setPrefHeight(45);
        btn.setPadding(new Insets(0, 30, 0, 30));
        addButtonSound(btn, false);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), btn);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), btn);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

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

    private CustomButton createSecondaryButton(String text) {
        CustomButton btn = new CustomButton(text);
        btn.getStyleClass().add("secondary-button");
        btn.setPrefHeight(45);
        btn.setPadding(new Insets(0, 30, 0, 30));
        addButtonSound(btn, false);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), btn);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), btn);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

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

    // Plays the click sound (or the toggle sound when toggle is true) whenever the button fires
    private void addButtonSound(CustomButton button, boolean toggle) {
        button.addEventHandler(ActionEvent.ACTION, e -> {
            if (toggle) {
                soundsAndButtons.playToggle();
            } else {
                soundsAndButtons.playClick();
            }
        });
    }

    // Custom button built on Label (text and/or graphic), so it is not a javafx.scene.control.Button
    private static class CustomButton extends Label {
        private static final PseudoClass PRESSED = PseudoClass.getPseudoClass("pressed");

        CustomButton() {
            this("");
        }

        CustomButton(String text) {
            super(text);
            getStyleClass().add("custom-button");
            setAlignment(Pos.CENTER);
            setPadding(new Insets(4, 9, 4, 9));
            setMinWidth(Region.USE_PREF_SIZE);
            setCursor(Cursor.HAND);
            setFocusTraversable(true);

            addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.getButton() == MouseButton.PRIMARY && !isDisabled()) {
                    pseudoClassStateChanged(PRESSED, true);
                    requestFocus();
                }
            });
            addEventHandler(MouseEvent.MOUSE_RELEASED, e -> pseudoClassStateChanged(PRESSED, false));
            addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    fire();
                }
            });
            addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    fire();
                    e.consume();
                }
            });
        }

        void setOnAction(EventHandler<ActionEvent> handler) {
            setEventHandler(ActionEvent.ACTION, handler);
        }

        void fire() {
            if (!isDisabled()) {
                fireEvent(new ActionEvent(this, this));
            }
        }
    }

    // Data classes
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
        boolean isTvShow;

        Movie(String title, String genre, String duration, String rating, String description,
                String imageUrl, double ratingScore) {
            this(title, genre, duration, rating, description, imageUrl, ratingScore, false);
        }

        Movie(String title, String genre, String duration, String rating, String description,
                String imageUrl, double ratingScore, boolean isTvShow) {
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.rating = rating;
            this.description = description;
            this.imageUrl = imageUrl;
            this.ratingScore = ratingScore;
            this.isTvShow = isTvShow;
        }
    }
}