package GUI;

import RobotSim.Obstacle;
import RobotSim.Robot;
import RobotSim.RobotArena;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;

/**
 * The MainApp class serves as the entry point for the Robot Simulator application.
 * It initializes the GUI components, sets up the main layout, integrates menus and toolbars,
 * manages animations, and handles user interactions.
 */
public class MainApp extends Application {

    private RobotArena robotArena;
    private ArenaView arenaView;
    private ControlPanel controlPanel;
    private Label robotCountLabel;
    private Label obstacleCountLabel;
    private static AnimationTimer animationTimer;
    private static boolean isAnimating = true;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the main layout using BorderPane
        BorderPane root = new BorderPane();

        // Replace setStyle(...) with a background fill
        root.setBackground(
                new Background(
                        new BackgroundFill(Color.web("#f5f6fa"), CornerRadii.EMPTY, Insets.EMPTY)
                )
        );

        // Create and set up the menu bar
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Initialize the Robot Arena and related components
        initializeArena();

        // Initialize and set up GUI components (ArenaView, ControlPanel, Status Panel)
        initializeGUIComponents(root);

        // Initialize and start the animation loop
        initializeAnimation();

        // Create the main scene and set it on the primary stage
        Scene scene = new Scene(root, 1200, 800); // Increased window size for better visibility

        primaryStage.setTitle("Robot Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize mouse and keyboard controls for user interactions
        initializeMouseControls();
        initializeKeyboardControls(scene);
    }

    /**
     * Creates the application menu bar with File and Help menus,
     * using setGraphic(...) on Menu/MenuItems to style text color in pure Java.
     *
     * @return A MenuBar instance with configured menus.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Set a background color for the MenuBar
        menuBar.setBackground(
                new Background(
                        new BackgroundFill(Color.web("#34495e"), CornerRadii.EMPTY, Insets.EMPTY)
                )
        );

        // === FILE MENU ===
        Menu fileMenu = createColoredMenu("File", Color.WHITE);

        MenuItem newArenaItem = createColoredMenuItem("New Arena", Color.BLACK);
        newArenaItem.setOnAction(e -> {
            resetArena();
            updateStatus("New arena initialized.");
        });

        MenuItem saveArenaItem = createColoredMenuItem("Save Arena", Color.BLACK);
        saveArenaItem.setOnAction(e -> controlPanel.handleSave());

        MenuItem loadArenaItem = createColoredMenuItem("Load Arena", Color.BLACK);
        loadArenaItem.setOnAction(e -> controlPanel.handleLoad());

        MenuItem exitItem = createColoredMenuItem("Exit", Color.BLACK);
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(
                newArenaItem,
                saveArenaItem,
                loadArenaItem,
                new SeparatorMenuItem(),
                exitItem
        );

        // === HELP MENU ===
        Menu helpMenu = createColoredMenu("Help", Color.WHITE);

        MenuItem aboutItem = createColoredMenuItem("About", Color.BLACK);
        aboutItem.setOnAction(e -> showAboutDialog());

        MenuItem helpItem = createColoredMenuItem("Help", Color.BLACK);
        helpItem.setOnAction(e -> showHelpDialog());

        helpMenu.getItems().addAll(aboutItem, helpItem);

        // Add File and Help menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    /**
     * Helper method to create a Menu with a given text and text color,
     * avoiding direct CSS usage by setting a Label as the graphic.
     */
    private Menu createColoredMenu(String menuText, Color textColor) {
        Menu menu = new Menu();
        Label lbl = new Label(menuText);
        lbl.setTextFill(textColor);
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 14));
        menu.setGraphic(lbl);
        return menu;
    }

    /**
     * Helper method to create a MenuItem with a given text and text color.
     */
    private MenuItem createColoredMenuItem(String itemText, Color textColor) {
        MenuItem item = new MenuItem();
        Label lbl = new Label(itemText);
        lbl.setTextFill(textColor);
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 14));
        item.setGraphic(lbl);
        return item;
    }

    /**
     * Initializes the RobotArena with default robots and obstacles.
     */
    private void initializeArena() {
        // You might rely on ArenaView's default canvas size or let it be dynamic
        robotArena = new RobotArena(800, 600);
        robotArena.addRobot();
        robotArena.addObstacle();
        robotArena.addChaserRobot();
        arenaView = new ArenaView(robotArena);
        controlPanel = new ControlPanel(robotArena, arenaView);
    }

    /**
     * Initializes and arranges the GUI components within the main layout.
     *
     * @param root The BorderPane serving as the main layout.
     */
    private void initializeGUIComponents(BorderPane root) {
        // === CENTER: Title + Arena (Canvas) in a VBox ===
        VBox centerPanel = new VBox(10);
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.setPadding(new Insets(20));

        Label titleLabel = new Label("Robot Arena Simulation");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.BLACK);

        // Add title + canvas
        Canvas arenaCanvas = arenaView.getCanvas();
        centerPanel.getChildren().addAll(titleLabel, arenaCanvas);
        root.setCenter(centerPanel);

        // === RIGHT: Status Panel in a VBox ===
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(20));

        // White background with rounded corners
        statusPanel.setBackground(
                new Background(
                        new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)
                )
        );
        statusPanel.setPrefWidth(300);

        Label statusTitle = new Label("Arena Status");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        statusTitle.setTextFill(Color.web("#2c3e50"));

        robotCountLabel = new Label("Robots: 0");
        robotCountLabel.setFont(Font.font("System", 16));
        robotCountLabel.setTextFill(Color.BLACK);

        obstacleCountLabel = new Label("Obstacles: 0");
        obstacleCountLabel.setFont(Font.font("System", 16));
        obstacleCountLabel.setTextFill(Color.BLACK);

        statusPanel.getChildren().addAll(
                statusTitle,
                robotCountLabel,
                obstacleCountLabel
        );
        root.setRight(statusPanel);

        // === BOTTOM: Adaptive Layout with a FlowPane ===
        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(15);
        bottomPane.setVgap(10);
        bottomPane.setPadding(new Insets(10));
        bottomPane.setAlignment(Pos.CENTER_LEFT);

        // Add your existing ControlPanel (which is an HBox internally) to the FlowPane
        bottomPane.getChildren().add(controlPanel.getPanel());

        // Place this FlowPane at the bottom of the BorderPane
        root.setBottom(bottomPane);
    }

    /**
     * Initializes the animation loop for the simulation using AnimationTimer.
     */
    private void initializeAnimation() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isAnimating) {
                    robotArena.moveRobots();
                    arenaView.render();
                    updateStatus();
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Initializes mouse controls for selecting robots by clicking on them.
     */
    private void initializeMouseControls() {
        arenaView.getCanvas().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                robotArena.setSelectedItem(robotArena.findItemAt(e.getX(), e.getY()));
                arenaView.render();
                if (robotArena.getSelectedItem() != null) {
                    updateStatus("Selected: " + robotArena.getSelectedItemInfo());
                } else {
                    updateStatus("No item selected.");
                }
            }
        });
    }

    /**
     * Initializes keyboard controls for moving the selected robot and toggling animation.
     *
     * @param scene The main application scene.
     */
    private void initializeKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> moveSelectedRobot(0, -10);
                case DOWN -> moveSelectedRobot(0, 10);
                case LEFT -> moveSelectedRobot(-10, 0);
                case RIGHT -> moveSelectedRobot(10, 0);
                case SPACE -> toggleAnimation();
                case DELETE -> {
                    robotArena.deleteSelectedItem();
                    arenaView.render();
                    updateStatus("Selected item deleted.");
                }
            }
            arenaView.render();
        });
    }

    /**
     * Moves the selected robot by the specified deltas.
     *
     * @param dx Change in X-coordinate.
     * @param dy Change in Y-coordinate.
     */
    private void moveSelectedRobot(double dx, double dy) {
        if (robotArena.getSelectedItem() instanceof Robot selectedRobot) {
            double newX = Math.max(
                    selectedRobot.getRadius(),
                    Math.min(
                            robotArena.getWidth() - selectedRobot.getRadius(),
                            selectedRobot.getX() + dx
                    )
            );
            double newY = Math.max(
                    selectedRobot.getRadius(),
                    Math.min(
                            robotArena.getHeight() - selectedRobot.getRadius(),
                            selectedRobot.getY() + dy
                    )
            );

            // Check for collisions before moving
            if (!robotArena.isColliding(newX, newY, selectedRobot.getRadius(), selectedRobot.getId())) {
                selectedRobot.setX(newX);
                selectedRobot.setY(newY);
                // Updated line using reflection to get the class name
                updateStatus("Moved " + selectedRobot.getClass().getSimpleName() +
                        " to (" + newX + ", " + newY + ")");
            } else {
                // Updated line using reflection to get the class name
                updateStatus("Cannot move " + selectedRobot.getClass().getSimpleName() +
                        " to (" + newX + ", " + newY + ") - Collision detected.");
            }
        }
    }

    /**
     * Updates the status panel with the current number of robots and obstacles.
     */
    private void updateStatus() {
        long robotCount = robotArena.getItems().stream()
                .filter(item -> item instanceof Robot)
                .count();
        long obstacleCount = robotArena.getItems().stream()
                .filter(item -> item instanceof Obstacle)
                .count();

        robotCountLabel.setText("Robots: " + robotCount);
        obstacleCountLabel.setText("Obstacles: " + obstacleCount);
    }

    /**
     * Resets the arena to its default state with initial robots and obstacles.
     */
    private void resetArena() {
        robotArena.clearArena();
        robotArena.addRobot();
        robotArena.addObstacle();
        robotArena.addChaserRobot();
        arenaView.render();
        updateStatus("Arena reset to default state.");
    }

    /**
     * Starts the animation loop.
     */
    private void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animationTimer.start();
            updateStatus("Animation started.");
        }
    }

    /**
     * Stops the animation loop.
     */
    private void stopAnimation() {
        if (isAnimating) {
            isAnimating = false;
            animationTimer.stop();
            updateStatus("Animation stopped.");
        }
    }

    /**
     * Toggles the animation between running and paused states.
     */
    private void toggleAnimation() {
        if (isAnimating) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    /**
     * Displays the About dialog with application information.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Robot Simulator");
        alert.setHeaderText("Robot Simulator v1.0");
        alert.setContentText(
                "Developed by [Your Name]\n" +
                        "University of [Your University]\n" +
                        "Course: CS2OP\n\n" +
                        "A simulation of various robots interacting within an arena."
        );
        alert.showAndWait();
    }

    /**
     * Displays the Help dialog with usage instructions.
     */
    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help - Robot Simulator");
        alert.setHeaderText("How to Use the Simulator");
        alert.setContentText(
                "• **Adding Robots:** Use the 'Robot Controls' section in the control panel " +
                        "to select and add different types of robots to the arena.\n" +
                        "• **Adding Obstacles:** Click 'Add Obstacle' in the control panel to place " +
                        "obstacles within the arena.\n" +
                        "• **Clearing Arena:** Use 'Clear Arena' to remove all items and reset the " +
                        "arena to its default state.\n" +
                        "• **Saving and Loading:** Save your current arena configuration using " +
                        "'Save Arena' and load a saved configuration with 'Load Arena' in the File menu.\n" +
                        "• **Adjusting Speed:** Slide the simulation speed slider to control the pace " +
                        "of the simulation.\n" +
                        "• **Interacting with Items:** Click on items within the arena to select them. " +
                        "Use arrow keys to move the selected robot or press DELETE to remove it.\n" +
                        "• **Animation Control:** Press SPACE to pause or resume the simulation.\n" +
                        "• **About:** Access information about the application and its developers " +
                        "through the About menu item."
        );
        alert.showAndWait();
    }

    /**
     * Updates the status label with the given message.
     *
     * @param message The status message to display.
     */
    private void updateStatus(String message) {
        controlPanel.updateStatus(message);
    }

    /**
     * Main entry point for the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Toggles the animation state.
     *
     * @param start True to start the animation, false to stop.
     */
    public static void toggleAnimation(boolean start) {
        if (start) {
            isAnimating = true;
            if (animationTimer != null) {
                animationTimer.start();
            }
        } else {
            isAnimating = false;
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
    }
}
