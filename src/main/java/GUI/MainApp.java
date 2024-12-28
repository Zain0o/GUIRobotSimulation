package GUI;

import RobotSim.RobotArena;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;

/**
 * MainApp serves as the entry point for the JavaFX Robot Simulator application.
 * It initializes the RobotArena, ArenaView, and ControlPanel, and sets up the main GUI layout.
 */
public class MainApp extends Application {

    private RobotArena robotArena;       // The logic for managing the arena
    private ArenaView arenaView;         // GUI component to render the arena
    private ControlPanel controlPanel;   // User controls for interacting with the arena
    private Label robotCountLabel;       // Label to display the count of robots
    private Label obstacleCountLabel;    // Label to display the count of obstacles
    private TextArea statusArea;         // TextArea to display real-time status
    private static AnimationTimer animationTimer; // Animation timer for robot movement

    @Override
    public void start(Stage primaryStage) {
        initializeArena();
        initializeGUIComponents(primaryStage);
        initializeAnimation(); // Start animation by default
    }

    /**
     * Initializes the RobotArena and populates it with default items (so there's a "default populated arena").
     */
    private void initializeArena() {
        // Create the arena (logic)
        robotArena = new RobotArena(600, 400);

        // Add a couple of default robots/obstacles to show a populated arena.
        robotArena.addRobot();
        robotArena.addObstacle();
        robotArena.addChaserRobot();

        // Create the arena view
        arenaView = new ArenaView(robotArena);

        // Create the control panel
        controlPanel = new ControlPanel(robotArena, arenaView);
    }

    /**
     * Initializes the main GUI layout and stage.
     */
    private void initializeGUIComponents(Stage primaryStage) {
        // Create labels for counts
        robotCountLabel = new Label("Robots: 0");
        obstacleCountLabel = new Label("Obstacles: 0");

        // Create status area
        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefHeight(200);

        // Put the labels and status in a VBox
        VBox infoPanel = new VBox(10, robotCountLabel, obstacleCountLabel,
                new Label("Arena Status:"), statusArea);

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(arenaView.getCanvas());    // ArenaView in the center
        root.setBottom(controlPanel.getPanel());  // ControlPanel at the bottom
        root.setRight(infoPanel);                 // Info panel on the right
        root.setTop(createMenuBar());             // Menu at the top

        // Set up the scene and stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Robot Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Request focus on the Canvas to capture key events
        arenaView.getCanvas().requestFocus();

        // Add keyboard controls
        addKeyboardControls(scene);

        // Add mouse click detection for selecting items
        arenaView.getCanvas().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                double mouseX = e.getX();
                double mouseY = e.getY();
                // Let the arena find which item was clicked
                robotArena.setSelectedItem(
                        robotArena.findItemAt(mouseX, mouseY)
                );
                // Redraw so we can highlight or do other visuals if desired
                arenaView.render();
            }
        });
    }

    /**
     * Initializes and starts the animation timer.
     */
    private void initializeAnimation() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                robotArena.moveRobots(); // Move robots
                arenaView.render();      // Refresh the arena
                updateCounts();          // Update counters
                updateStatus();          // Update the status area
            }
        };
        animationTimer.start();
    }

    /**
     * Builds the MenuBar: File (New, Save, Load, Exit), Help (About, Help),
     * plus Start/Stop animation in the File menu.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ---------------- File Menu ----------------
        Menu fileMenu = new Menu("File");

        // New Arena
        MenuItem newItem = new MenuItem("New Arena");
        newItem.setOnAction(e -> {
            // Clears and re-initializes the arena
            robotArena.clearArena();
            // Add some default items again
            robotArena.addRobot();
            robotArena.addObstacle();
            robotArena.addChaserRobot();
            arenaView.render();
        });

        // Start Animation
        MenuItem startAnimItem = new MenuItem("Start Animation");
        startAnimItem.setOnAction(e -> {
            if (animationTimer != null) animationTimer.start();
        });

        // Stop Animation
        MenuItem stopAnimItem = new MenuItem("Stop Animation");
        stopAnimItem.setOnAction(e -> {
            if (animationTimer != null) animationTimer.stop();
        });

        // Save
        MenuItem saveItem = new MenuItem("Save...");
        saveItem.setOnAction(e -> controlPanel.handleSave());

        // Load
        MenuItem loadItem = new MenuItem("Load...");
        loadItem.setOnAction(e -> controlPanel.handleLoad());

        // Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            // Graceful shutdown
            if (animationTimer != null) {
                animationTimer.stop();
            }
            // Close the application
            System.exit(0);
        });

        fileMenu.getItems().addAll(newItem, startAnimItem, stopAnimItem,
                new SeparatorMenuItem(), saveItem, loadItem,
                new SeparatorMenuItem(), exitItem);

        // ---------------- Help Menu ----------------
        Menu helpMenu = new Menu("Help");

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION,
                "About", "Robot Simulator",
                "Created by [Your Name].\nVersion 1.0\nThis is a JavaFX-based robot simulator project."));

        MenuItem helpItem = new MenuItem("Help");
        helpItem.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION,
                "Help", "How to Use the Simulator",
                "1. Use 'Add Robot' or 'Add Chaser Robot' to place robots.\n"
                        + "2. Use 'Add Obstacle' for obstacles.\n"
                        + "3. Use the arrow keys to move the selected robot.\n"
                        + "4. Click on an item to select it.\n"
                        + "5. Save/Load the arena under File menu."));

        helpMenu.getItems().addAll(aboutItem, helpItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    /**
     * Adds keyboard controls for moving the currently selected robot.
     */
    private void addKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> moveSelectedRobot(0, -10);
                case DOWN -> moveSelectedRobot(0, 10);
                case LEFT -> moveSelectedRobot(-10, 0);
                case RIGHT -> moveSelectedRobot(10, 0);
                default -> { /* No action for other keys */ }
            }
            arenaView.render(); // Refresh the arena view
        });
    }

    /**
     * Moves the selected robot (if any) by the specified delta values.
     */
    private void moveSelectedRobot(double dx, double dy) {
        if (robotArena.getSelectedItem() instanceof RobotSim.Robot selectedRobot) {
            selectedRobot.setX(Math.max(selectedRobot.getRadius(),
                    Math.min(robotArena.getWidth() - selectedRobot.getRadius(),
                            selectedRobot.getX() + dx)));
            selectedRobot.setY(Math.max(selectedRobot.getRadius(),
                    Math.min(robotArena.getHeight() - selectedRobot.getRadius(),
                            selectedRobot.getY() + dy)));
        }
    }

    /**
     * Updates the labels for robot and obstacle counts.
     */
    private void updateCounts() {
        long robotCount = robotArena.getItems().stream()
                .filter(item -> item instanceof RobotSim.Robot).count();
        long obstacleCount = robotArena.getItems().stream()
                .filter(item -> item instanceof RobotSim.Obstacle).count();

        robotCountLabel.setText("Robots: " + robotCount);
        obstacleCountLabel.setText("Obstacles: " + obstacleCount);
    }

    /**
     * Updates the status area with a description of all items in the arena.
     */
    private void updateStatus() {
        statusArea.setText(robotArena.getStatus());
    }

    /**
     * Utility method to show an alert box.
     */
    private void showAlert(Alert.AlertType type, String title,
                           String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Main method to launch the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
