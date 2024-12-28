package GUI;

import RobotSim.RobotArena;
import RobotSim.TextFile;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;

public class ControlPanel {
    private final HBox panel;
    private final RobotArena robotArena;
    private final ArenaView arenaView;
    private final FileChooser fileChooser = new FileChooser();

    public ControlPanel(RobotArena robotArena, ArenaView arenaView) {
        this.robotArena = robotArena;
        this.arenaView = arenaView;
        panel = new HBox(5);
        initializeButtons();
    }

    private void initializeButtons() {
        Button addRobotButton = createButton("Add Robot",
                "Add a new robot with whisker sensor", e -> {
                    robotArena.addRobot();
                    arenaView.render();
                });

        Button addChaserButton = createButton("Add Chaser Robot",
                "Add a robot that follows others", e -> {
                    robotArena.addChaserRobot();
                    arenaView.render();
                });

        Button addObstacleButton = createButton("Add Obstacle",
                "Add obstacle to arena", e -> {
                    robotArena.addObstacle();
                    arenaView.render();
                });

        Button deleteButton = createButton("Delete Item",
                "Delete selected item", e -> {
                    robotArena.deleteSelectedItem();
                    arenaView.render();
                });

        Button saveButton = createButton("Save Arena",
                "Save arena to file", e -> handleSave());

        Button loadButton = createButton("Load Arena",
                "Load arena from file", e -> handleLoad());

        panel.getChildren().addAll(
                addRobotButton, addChaserButton, addObstacleButton,
                deleteButton, saveButton, loadButton
        );
    }

    private Button createButton(String label, String tooltip,
                                javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(label);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(action);
        return button;
    }

    public void handleSave() {
        File file = fileChooser.showSaveDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                String state = robotArena.saveArenaState();
                boolean success = TextFile.writeFile(file.getAbsolutePath(), state);
                if (success) {
                    showAlert(AlertType.INFORMATION, "Success",
                            "Arena saved successfully.");
                } else {
                    showAlert(AlertType.ERROR, "Error",
                            "Failed to save arena.");
                }
            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "Error",
                        "Error saving: " + ex.getMessage());
            }
        }
    }

    public void handleLoad() {
        File file = fileChooser.showOpenDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                String state = TextFile.readFile(file.getAbsolutePath());
                if (state != null) {
                    robotArena.loadArenaState(state);
                    arenaView.render();
                    showAlert(AlertType.INFORMATION, "Success",
                            "Arena loaded successfully.");
                } else {
                    showAlert(AlertType.ERROR, "Error",
                            "Failed to load arena.");
                }
            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "Error",
                        "Error loading: " + ex.getMessage());
            }
        }
    }

    public HBox getPanel() {
        return panel;
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}