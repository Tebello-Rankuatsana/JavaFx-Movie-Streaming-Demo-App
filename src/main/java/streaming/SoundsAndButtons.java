package streaming;

import java.net.URL;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;

public class SoundsAndButtons {

    private final AudioClip clickSound;
    private final AudioClip toggleSound;
    private final AudioClip playSound;
    private boolean soundOn = true;

    public SoundsAndButtons() {
        clickSound = loadSound("/sounds/click.wav");
        toggleSound = loadSound("/sounds/toggle.wav");
        playSound = loadSound("/sounds/play.wav");
    }

    // Loads a sound file if it exists; returns null if missing so the app still runs
    private AudioClip loadSound(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }
        try {
            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    public boolean isSoundOn() {
        return soundOn;
    }

    public void playClick() {
        play(clickSound);
    }

    public void playToggle() {
        play(toggleSound);
    }

    public void playPlay() {
        play(playSound);
    }

    private void play(AudioClip clip) {
        if (soundOn && clip != null) {
            clip.play();
        }
    }

    // Creates a custom Play/Pause button using a Label
    public StackPane createPlayPauseControl(boolean startPlaying, Consumer<Boolean> onToggle) {
        Label label = new Label(startPlaying ? "❚❚ Pause" : "▶ Play");
        label.getStyleClass().add("custom-play-pause-label");

        StackPane button = new StackPane(label);
        button.getStyleClass().add("custom-play-pause");
        button.setAlignment(Pos.CENTER);
        button.setFocusTraversable(true);
        button.setTooltip(new Tooltip(startPlaying ? "Pause" : "Play"));

        final boolean[] isPlaying = { startPlaying };

        Runnable toggleAction = () -> {
            isPlaying[0] = !isPlaying[0];
            boolean playingNow = isPlaying[0];

            label.setText(playingNow ? "Pause" : "Play");
            button.setTooltip(new Tooltip(playingNow ? "Pause" : "Play"));

            button.getStyleClass().remove("custom-play-pause-playing");
            if (playingNow) {
                button.getStyleClass().add("custom-play-pause-playing");
                playPlay();
            } else {
                playToggle();
            }

            if (onToggle != null) {
                onToggle.accept(playingNow);
            }
        };

        button.setOnMouseClicked(e -> toggleAction.run());
        button.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                toggleAction.run();
                e.consume();
            }
        });

        return button;
    }

    // Makes a movie poster card clickable with sound and keyboard support
    public void makePosterClickable(Node posterCard, Runnable onClick) {
        posterCard.setFocusTraversable(true);

        posterCard.setOnMouseClicked(e -> {
            playClick();
            if (onClick != null) {
                onClick.run();
            }
        });

        posterCard.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                playClick();
                if (onClick != null) {
                    onClick.run();
                }
                e.consume();
            }
        });
    }

    // Adds a click or toggle sound to existing buttons without changing what they do
    public void addButtonSound(ButtonBase button, boolean useToggleSound) {
        button.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (useToggleSound) {
                playToggle();
            } else {
                playClick();
            }
        });
    }
}