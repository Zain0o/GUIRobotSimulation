package GUI;

import RobotSim.RobotArena;
import RobotSim.TextFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * The ControlPanel class provides the user interface controls for the Robot Arena simulation.
 * It includes sections for adding robots, managing arena controls, adjusting simulation speed,
 * and displaying status information. The layout has been updated to a horizontal arrangement
 * separating robot controls and simulation controls for a more organized interface.
 */
public class ControlPanel {
    private final HBox panel;  // Horizontal layout for the entire control panel
    private final RobotArena robotArena;
    private final ArenaView arenaView;
    private final Slider speedControl;
    private final Label statusLabel;

    /**
     * Constructor to initialize the ControlPanel with the given RobotArena and ArenaView.
     *
     * @param robotArena The RobotArena instance to be controlled.
     * @param arenaView  The ArenaView instance for rendering updates.
     */
    public ControlPanel(RobotArena robotArena, ArenaView arenaView) {
        this.robotArena = robotArena;
        this.arenaView = arenaView;
        this.panel = new HBox(20);  // Horizontal layout with spacing
        this.speedControl = createStyledSlider();
        this.statusLabel = new Label("Status: Ready");
        initializePanel();
    }

    /**
     * Initializes the ControlPanel layout and components.
     */
    private void initializePanel() {
        // Panel background and alignment
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setBackground(new Background(
                new BackgroundFill(Color.rgb(245, 245, 245), new CornerRadii(10), Insets.EMPTY)
        ));

        // Left section - Robot Controls
        VBox robotSection = new VBox(10);
        robotSection.setPadding(new Insets(10));
        robotSection.setBackground(
                new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY))
        );
        robotSection.setBorder(
                new Border(new BorderStroke(
                        Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1)
                ))
        );
        robotSection.setPrefWidth(250);
        robotSection.setMaxWidth(250);
        robotSection.setMinWidth(200);

        Label robotLabel = new Label("Robot Controls");
        robotLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        robotLabel.setTextFill(Color.web("#2c3e50"));
        robotLabel.setAlignment(Pos.CENTER);
        robotLabel.setMaxWidth(Double.MAX_VALUE);

        robotSection.getChildren().addAll(robotLabel, new Separator(), createRobotsSection());

        // Right section - Simulation Controls
        VBox simulationSection = new VBox(10);
        simulationSection.setPadding(new Insets(10));
        simulationSection.setBackground(
                new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY))
        );
        simulationSection.setBorder(
                new Border(new BorderStroke(
                        Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1)
                ))
        );
        simulationSection.setPrefWidth(300);
        simulationSection.setMaxWidth(300);
        simulationSection.setMinWidth(250);

        Label simulationLabel = new Label("Simulation Controls");
        simulationLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        simulationLabel.setTextFill(Color.web("#2c3e50"));
        simulationLabel.setAlignment(Pos.CENTER);
        simulationLabel.setMaxWidth(Double.MAX_VALUE);

        simulationSection.getChildren().addAll(
                simulationLabel,
                new Separator(),
                createSimulationSection(),
                createArenaControlsSection(),
                new Separator(),
                createStatusSection()
        );

        // Add both sections to the horizontal panel
        panel.getChildren().addAll(robotSection, simulationSection);
    }

    /**
     * Creates the Robots section with styled buttons for each robot type.
     *
     * @return A GridPane containing robot-related controls.
     */
    private GridPane createRobotsSection() {
        GridPane robotGrid = new GridPane();
        robotGrid.setHgap(10);
        robotGrid.setVgap(10);
        robotGrid.setAlignment(Pos.CENTER);

        // Create robot buttons
        Button basicRobot = createRobotButton("Basic Robot", "🤖");
        Button chaserRobot = createRobotButton("Chaser Robot", "🏃");
        Button beamRobot = createRobotButton("Beam Robot", "📡");
        Button bumpRobot = createRobotButton("Bump Robot", "💫");

        // Add buttons to grid (2 columns)
        robotGrid.add(basicRobot, 0, 0);
        robotGrid.add(chaserRobot, 1, 0);
        robotGrid.add(beamRobot, 0, 1);
        robotGrid.add(bumpRobot, 1, 1);

        return robotGrid;
    }

    /**
     * Creates a styled button for a specific robot type with an emoji.
     *
     * @param robotType The type/name of the robot.
     * @param icon      The emoji representing the robot.
     * @return A styled Button instance.
     */
    private Button createRobotButton(String robotType, String icon) {
        Button button = new Button();

        // Container for the icon and text
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 24));  // Larger emoji for visibility

        Label nameLabel = new Label(robotType.replace(" Robot", ""));
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        content.getChildren().addAll(iconLabel, nameLabel);
        button.setGraphic(content);

        button.setPrefSize(80, 80);

        // Normal (default) background + border
        Background normalBackground = new Background(
                new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)
        );
        Border normalBorder = new Border(
                new BorderStroke(Color.web("#e0e0e0"), BorderStrokeStyle.SOLID,
                        new CornerRadii(5), new BorderWidths(1))
        );

        // Hover background + border
        Background hoverBackground = new Background(
                new BackgroundFill(Color.web("#f5f5f5"), new CornerRadii(5), Insets.EMPTY)
        );

        // Apply defaults
        button.setBackground(normalBackground);
        button.setBorder(normalBorder);

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setBackground(hoverBackground);
            button.setBorder(normalBorder); // keep same border, only background changes
        });
        button.setOnMouseExited(e -> {
            button.setBackground(normalBackground);
            button.setBorder(normalBorder);
        });

        // Click handler
        button.setOnAction(e -> {
            switch (robotType) {
                case "Basic Robot" -> robotArena.addRobot();
                case "Chaser Robot" -> robotArena.addChaserRobot();
                case "Beam Robot" -> robotArena.addBeamRobot();
                case "Bump Robot" -> robotArena.addBumpRobot();
                default -> {
                    showAlert(AlertType.WARNING, "Unknown Robot Type",
                            "The selected robot type is not recognized.");
                    return;
                }
            }
            arenaView.render();
            updateStatus("Added new " + robotType);
        });

        return button;
    }

    /**
     * Creates the Arena Controls section with options to add obstacles, clear arena,
     * save and load configurations.
     *
     * @return A VBox containing arena control buttons.
     */
    private VBox createArenaControlsSection() {
        VBox box = new VBox(10);  // Adjusted spacing
        box.setPadding(new Insets(10));
        box.setBackground(new Background(
                new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)
        ));

        Label sectionLabel = new Label("Arena Controls");
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionLabel.setTextFill(Color.web("#34495e"));

        HBox buttonBox = new HBox(10);  // Adjusted spacing
        buttonBox.setAlignment(Pos.CENTER);

        // Add Obstacle Button with Tooltip
        Button addObstacle = createStyledButton(
                "Add Obstacle",
                "Click to add a new obstacle",
                e -> {
                    robotArena.addObstacle();
                    arenaView.render();
                    updateStatus("Added new obstacle");
                }
        );

        // Clear Arena Button with Tooltip
        Button clearButton = createStyledButton(
                "Clear Arena",
                "Click to remove all items from the arena",
                e -> {
                    robotArena.clearArena();
                    arenaView.render();
                    updateStatus("Arena cleared");
                }
        );

        // Save Arena Button with Tooltip
        Button saveButton = createStyledButton(
                "Save Arena",
                "Click to save the current arena configuration",
                e -> handleSave()
        );

        // Load Arena Button with Tooltip
        Button loadButton = createStyledButton(
                "Load Arena",
                "Click to load a saved arena configuration",
                e -> handleLoad()
        );

        buttonBox.getChildren().addAll(addObstacle, clearButton, saveButton, loadButton);
        box.getChildren().addAll(sectionLabel, buttonBox);
        return box;
    }

    /**
     * Creates the Simulation Controls section with controls to adjust simulation speed and manage animation.
     *
     * @return A VBox containing the simulation speed and animation controls.
     */
    private VBox createSimulationSection() {
        VBox simulationControlsSection = new VBox(10);
        simulationControlsSection.setBackground(new Background(
                new BackgroundFill(Color.WHITE, new CornerRadii(5), Insets.EMPTY)
        ));

        Label sectionLabel = new Label("Simulation Controls");
        sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionLabel.setTextFill(Color.web("#34495e"));

        // Speed slider layout
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER_LEFT);

        Label speedLabel = new Label("Speed:");
        speedBox.getChildren().addAll(speedLabel, speedControl);

        // Animation control buttons (Start/Stop)
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button startButton = createStyledButton(
                "Start",
                "Start simulation",
                e -> MainApp.toggleAnimation(true)
        );
        Button stopButton = createStyledButton(
                "Stop",
                "Stop simulation",
                e -> MainApp.toggleAnimation(false)
        );
        buttonBox.getChildren().addAll(startButton, stopButton);

        simulationControlsSection.getChildren().addAll(sectionLabel, speedBox, buttonBox);
        return simulationControlsSection;
    }

    /**
     * Creates the Status section to display current status messages.
     *
     * @return A VBox containing the status label.
     */
    private VBox createStatusSection() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setBackground(new Background(
                new BackgroundFill(Color.web("#f8f9fa"), new CornerRadii(5), Insets.EMPTY)
        ));

        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#34495e"));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().add(statusLabel);
        return box;
    }

    /**
     * Creates a styled button with hover effects and tooltips (pure Java, no CSS strings).
     *
     * @param text     The text to display on the button.
     * @param tooltip  The tooltip text for the button.
     * @param action   The event handler for the button's action.
     * @return A styled Button instance.
     */
    private Button createStyledButton(String text,
                                      String tooltip,
                                      javafx.event.EventHandler<javafx.event.ActionEvent> action) {

        Button button = new Button(text);
        button.setOnAction(action);
        button.setTooltip(new Tooltip(tooltip));
        button.setPrefHeight(30);  // Adjust as desired
        button.setMaxWidth(Double.MAX_VALUE);

        // Normal background
        Background normalBackground = new Background(
                new BackgroundFill(Color.web("#3498db"), new CornerRadii(4), Insets.EMPTY)
        );
        // Hover background
        Background hoverBackground = new Background(
                new BackgroundFill(Color.web("#2980b9"), new CornerRadii(4), Insets.EMPTY)
        );

        // Setup default (normal) button state
        button.setBackground(normalBackground);
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // Hover effect
        button.setOnMouseEntered(e -> button.setBackground(hoverBackground));
        button.setOnMouseExited(e -> button.setBackground(normalBackground));

        return button;
    }

    /**
     * Creates a styled slider with tick marks, labels, and a listener to adjust simulation speed.
     *
     * @return A styled Slider instance.
     */
    private Slider createStyledSlider() {
        Slider slider = new Slider(0.5, 2.0, 1.0); // Range can be customized
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.1);
        slider.setPrefWidth(150);  // Adjusted width for smaller layout

        // Tooltip for the slider
        Tooltip sliderTooltip = new Tooltip("Adjust the simulation speed.");
        Tooltip.install(slider, sliderTooltip);

        // Listen for value changes
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            robotArena.updateSimulationSpeed(newVal.doubleValue());
            updateStatus(String.format("Simulation speed set to %.1fx", newVal.doubleValue()));
        });

        return slider;
    }

    /**
     * Handles saving the current arena configuration to a file.
     */
    public void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Arena Configuration");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showSaveDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                String state = robotArena.saveArenaState();
                boolean success = TextFile.writeFile(file.getAbsolutePath(), state);
                updateStatus(success ? "Arena saved successfully." : "Failed to save arena.");
            } catch (Exception ex) {
                updateStatus("Error saving: " + ex.getMessage());
            }
        }
    }

    /**
     * Handles loading an arena configuration from a file.
     */
    public void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Arena Configuration");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                String state = TextFile.readFile(file.getAbsolutePath());
                if (state != null && !state.isBlank()) {
                    robotArena.loadArenaState(state);
                    arenaView.render();
                    updateStatus("Arena loaded successfully.");
                } else {
                    updateStatus("Failed to load arena.");
                }
            } catch (Exception ex) {
                updateStatus("Error loading: " + ex.getMessage());
            }
        }
    }

    /**
     * Updates the status label with the given message.
     *
     * @param message The status message to display.
     */
    public void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    /**
     * Displays an alert dialog with the specified type, title, and content.
     *
     * @param alertType The type of alert.
     * @param title     The title of the alert dialog.
     * @param content   The content/message of the alert.
     */
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * Returns the HBox containing all control panel components.
     *
     * @return The HBox panel.
     */
    public HBox getPanel() {
        return panel;
    }
}
