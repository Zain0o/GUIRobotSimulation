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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

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
        BorderPane root = new BorderPane();

        initializeArena();           // Moved to a single definition
        initializeMainLayout(root);
        initializeAnimation();

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Robot Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeMouseControls();
        initializeKeyboardControls(scene);
    }

    /**
     * Initializes the main layout by setting up top, center, right, and bottom sections.
     *
     * @param root the BorderPane to set up
     */
    private void initializeMainLayout(BorderPane root) {
        root.setTop(createEnhancedMenuBar());
        root.setCenter(createMainArenaSection());
        root.setRight(createEnhancedStatusPanel());
        root.setBottom(createEnhancedControlPanel());
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#f5f6fa"), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    /**
     * Creates the main arena section with a title and canvas.
     *
     * @return the VBox containing the main arena section
     */
    private VBox createMainArenaSection() {
        VBox arenaSection = new VBox(10);
        arenaSection.setPadding(new Insets(20));
        arenaSection.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Robot Arena Simulation");
        title.setFont(Font.font("System", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#2c3e50"));

        Canvas canvas = arenaView.getCanvas();
        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        canvasWrapper.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.2)));

        arenaSection.getChildren().addAll(title, canvasWrapper);
        return arenaSection;
    }

    /**
     * Creates an enhanced MenuBar with modern styling.
     *
     * @return the enhanced MenuBar
     */
    private MenuBar createEnhancedMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setBackground(new Background(new BackgroundFill(
                Color.web("#2c3e50"), CornerRadii.EMPTY, Insets.EMPTY)));

        Menu fileMenu = createStyledMenu("File");
        fileMenu.getItems().addAll(
                createMenuItem("New Arena", "⭐", e -> resetArena()),
                createMenuItem("Save Arena", "💾", e -> controlPanel.handleSave()),
                createMenuItem("Load Arena", "📂", e -> controlPanel.handleLoad()),
                new SeparatorMenuItem(),
                createMenuItem("Exit", "❌", e -> System.exit(0))
        );

        Menu helpMenu = createStyledMenu("Help");
        helpMenu.getItems().addAll(
                createMenuItem("About", "ℹ️", e -> showEnhancedAboutDialog()), // SINGLE definition
                createMenuItem("Help", "❓", e -> showHelpDialog())             // SINGLE definition
        );

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    /**
     * Creates a styled Menu with white text.
     *
     * @param text the menu title
     * @return the styled Menu
     */
    private Menu createStyledMenu(String text) {
        Menu menu = new Menu();
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));
        menu.setGraphic(label);
        return menu;
    }

    /**
     * Creates a styled MenuItem with an icon and text.
     *
     * @param text    the display text of the menu item
     * @param icon    the icon representing the menu item
     * @param action  the action to perform on selection
     * @return the styled MenuItem
     */
    private MenuItem createMenuItem(String text, String icon, EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem();
        HBox content = new HBox(10);
        Label iconLabel = new Label(icon);
        Label textLabel = new Label(text);
        textLabel.setTextFill(Color.BLACK);
        content.getChildren().addAll(iconLabel, textLabel);
        item.setGraphic(content);
        item.setOnAction(action);
        return item;
    }

    /**
     * Creates an enhanced status panel with robot and obstacle counts,
     * selected item info, and arena metrics.
     *
     * @return the enhanced status panel VBox
     */
    private VBox createEnhancedStatusPanel() {
        VBox statusPanel = new VBox(15);
        statusPanel.setPadding(new Insets(20));
        statusPanel.setPrefWidth(300);
        statusPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        statusPanel.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        Label title = new Label("Arena Status");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(10);

        // Initialize labels
        robotCountLabel = new Label("0");
        robotCountLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        obstacleCountLabel = new Label("0");
        obstacleCountLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));

        // Robot Stats
        statsGrid.add(createStatIcon("🤖"), 0, 0);
        statsGrid.add(robotCountLabel, 1, 0);

        // Obstacle Stats
        statsGrid.add(createStatIcon("🚧"), 0, 1);
        statsGrid.add(obstacleCountLabel, 1, 1);

        // Selected Item Info
        VBox selectedItemInfo = new VBox(5);
        Label selectedLabel = new Label("Selected Item");
        selectedLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        TextArea itemDetails = new TextArea();
        itemDetails.setEditable(false);
        itemDetails.setPrefRowCount(3);
        selectedItemInfo.getChildren().addAll(selectedLabel, itemDetails);

        // Arena Metrics
        VBox metrics = createMetricsPanel();

        statusPanel.getChildren().addAll(
                title,
                new Separator(),
                statsGrid,
                new Separator(),
                selectedItemInfo,
                new Separator(),
                metrics
        );

        return statusPanel;
    }

    /**
     * Creates an icon label for stats.
     *
     * @param icon the emoji/icon string
     * @return the Label with the icon
     */
    private Label createStatIcon(String icon) {
        Label label = new Label(icon);
        label.setFont(Font.font("System", 20));
        return label;
    }

    /**
     * Creates the arena metrics panel.
     *
     * @return the VBox containing arena metrics
     */
    private VBox createMetricsPanel() {
        VBox metrics = new VBox(10);

        Label metricsTitle = new Label("Arena Metrics");
        metricsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        metricsTitle.setTextFill(Color.web("#2c3e50"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        grid.add(new Label("Size:"), 0, 0);
        grid.add(new Label("800x600"), 1, 0);

        grid.add(new Label("Active:"), 0, 1);
        Label activeStatus = new Label("Running");
        activeStatus.setTextFill(Color.GREEN);
        grid.add(activeStatus, 1, 1);

        grid.add(new Label("Speed:"), 0, 2);
        Label speedValue = new Label("1.0x");
        grid.add(speedValue, 1, 2);

        metrics.getChildren().addAll(metricsTitle, grid);
        return metrics;
    }

    /**
     * Creates an enhanced control panel with robot controls and simulation controls.
     *
     * @return the enhanced control panel HBox
     */
    private HBox createEnhancedControlPanel() {
        HBox controlPanel = new HBox(20);
        controlPanel.setPadding(new Insets(15));
        // ------------------ FIX: Use a valid CornerRadii constructor for top corners  -----------------------
        controlPanel.setBackground(new Background(new BackgroundFill(
                Color.WHITE,
                new CornerRadii(10, 10, 0, 0, false),  // <--- Corrected here
                Insets.EMPTY
        )));
        // ----------------------------------------------------------------------------------------------------

        VBox robotControls = createRobotControls();
        VBox simulationControls = createSimulationControls();

        controlPanel.getChildren().addAll(robotControls, simulationControls);
        return controlPanel;
    }

    /**
     * Creates the robot controls section with buttons to add different robot types.
     *
     * @return the VBox containing robot controls
     */
    private VBox createRobotControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));
        controls.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(5), Insets.EMPTY)));

        Label title = new Label("Robot Controls");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        GridPane robotButtons = new GridPane();
        robotButtons.setHgap(10);
        robotButtons.setVgap(10);

        robotButtons.add(createRobotButton("Basic", "🤖", e -> {
            robotArena.addRobot();
            arenaView.render();
            updateStatus("Basic Robot added.");
        }), 0, 0);
        robotButtons.add(createRobotButton("Chaser", "🏃", e -> {
            robotArena.addChaserRobot();
            arenaView.render();
            updateStatus("Chaser Robot added.");
        }), 1, 0);
        robotButtons.add(createRobotButton("Beam", "📡", e -> {
            robotArena.addBeamRobot();
            arenaView.render();
            updateStatus("Beam Robot added.");
        }), 0, 1);
        robotButtons.add(createRobotButton("Bump", "💫", e -> {
            robotArena.addBumpRobot();
            arenaView.render();
            updateStatus("Bump Robot added.");
        }), 1, 1);

        controls.getChildren().addAll(title, robotButtons);
        return controls;
    }

    /**
     * Creates a robot button with specified type and icon.
     *
     * @param type   the type of the robot
     * @param icon   the emoji/icon representing the robot
     * @param action the action to perform on button click
     * @return the styled Button
     */
    private Button createRobotButton(String type, String icon, EventHandler<ActionEvent> action) {
        Button button = new Button();
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", 24));
        Label typeLabel = new Label(type);
        typeLabel.setFont(Font.font("System", 12));

        content.getChildren().addAll(iconLabel, typeLabel);
        button.setGraphic(content);
        button.setOnAction(action);
        button.setPrefSize(80, 80);
        button.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(5), Insets.EMPTY)));
        button.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));

        return button;
    }

    /**
     * Creates the simulation controls section with speed slider and start/stop buttons.
     *
     * @return the VBox containing simulation controls
     */
    private VBox createSimulationControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));

        Label title = new Label("Simulation Controls");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        HBox speedControl = new HBox(10);
        speedControl.setAlignment(Pos.CENTER_LEFT);
        Label speedLabel = new Label("Speed:");
        Slider speedSlider = new Slider(0.5, 2.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setMinorTickCount(4);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Adjust animation speed based on slider value
            animationTimer.stop();
            animationTimer = new AnimationTimer() {
                private long lastUpdate = 0;
                private double speed = newVal.doubleValue();

                @Override
                public void handle(long now) {
                    if (now - lastUpdate >= 16_666_667 / speed) { // Approximately 60 FPS / speed
                        if (isAnimating) {
                            robotArena.moveRobots();
                            arenaView.render();
                            updateStatus();
                        }
                        lastUpdate = now;
                    }
                }
            };
            animationTimer.start();
            updateStatus("Speed set to " + String.format("%.1fx", newVal.doubleValue()));
        });
        speedControl.getChildren().addAll(speedLabel, speedSlider);

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.getChildren().addAll(
                createControlButton("Start", "▶", e -> toggleAnimation(true)),
                createControlButton("Stop", "⏸", e -> toggleAnimation(false))
        );

        controls.getChildren().addAll(title, speedControl, buttons);
        return controls;
    }

    /**
     * Creates a control button with specified text and icon.
     *
     * @param text   the button text
     * @param icon   the emoji/icon representing the action
     * @param action the action to perform on button click
     * @return the styled Button
     */
    private Button createControlButton(String text, String icon, EventHandler<ActionEvent> action) {
        Button button = new Button();
        HBox content = new HBox(5);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        Label textLabel = new Label(text);
        content.getChildren().addAll(iconLabel, textLabel);

        button.setGraphic(content);
        button.setOnAction(action);
        button.setPrefWidth(100);
        button.setPrefHeight(30);
        button.setBackground(new Background(new BackgroundFill(
                Color.web("#3498db"), new CornerRadii(5), Insets.EMPTY)));
        button.setTextFill(Color.WHITE);
        button.setFont(Font.font("System", FontWeight.BOLD, 14));
        button.setEffect(new DropShadow(3, Color.rgb(0, 0, 0, 0.2)));

        return button;
    }

    // ------------------------------------------------------------------------
    // Help-Dialog-Related Methods (SINGLE DEFINITIONS ONLY)
    // ------------------------------------------------------------------------

    /**
     * Displays the Help dialog with multiple tabs for Controls, Robots, and Features.
     */
    private void showHelpDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Robot Simulator Help");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                createControlsHelp(),
                createRobotsHelp(),
                createFeaturesHelp()
        );

        dialog.getDialogPane().setContent(tabs);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(500, 400);

        dialog.showAndWait();
    }

    /**
     * Creates the Controls tab for the Help dialog.
     *
     * @return the Controls Tab
     */
    private Tab createControlsHelp() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                createHelpSection("Keyboard Controls",
                        "• Arrow Keys: Move selected robot\n" +
                                "• Delete: Remove selected robot\n" +
                                "• Space: Pause/Resume simulation"),

                createHelpSection("Mouse Controls",
                        "• Left Click: Select robot\n" +
                                "• Right Click: Show context menu\n" +
                                "• Drag: Move selected robot"),

                createHelpSection("Simulation Controls",
                        "• Speed Slider: Adjust simulation speed\n" +
                                "• Start/Stop: Control animation\n" +
                                "• Clear: Reset arena")
        );

        return new Tab("Controls", content);
    }

    /**
     * Creates the Robots tab for the Help dialog.
     *
     * @return the Robots Tab
     */
    private Tab createRobotsHelp() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                createHelpSection("Basic Robot",
                        "Standard robot with collision avoidance\n" +
                                "Blue color, visible sensors"),

                createHelpSection("Chaser Robot",
                        "Pursues nearest robot\n" +
                                "Red color, dynamic targeting"),

                createHelpSection("Beam Robot",
                        "Uses beam sensors for navigation\n" +
                                "Shows sensor lines in real-time"),

                createHelpSection("Bump Robot",
                        "Advanced collision response\n" +
                                "Visual feedback on impacts")
        );

        return new Tab("Robots", content);
    }

    /**
     * Creates the Features tab for the Help dialog.
     *
     * @return the Features Tab
     */
    private Tab createFeaturesHelp() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                createHelpSection("Arena Management",
                        "• Save/Load arena configurations\n" +
                                "• Add/Remove robots and obstacles\n" +
                                "• Real-time status updates"),

                createHelpSection("Simulation Features",
                        "• Variable speed control\n" +
                                "• Multiple robot types\n" +
                                "• Collision detection\n" +
                                "• Interactive controls")
        );

        return new Tab("Features", content);
    }

    /**
     * Creates a help section with a title and content.
     *
     * @param title       the title of the help section
     * @param contentText the descriptive text of the help section
     * @return the VBox containing the help section
     */
    private VBox createHelpSection(String title, String contentText) {
        VBox section = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setTextFill(Color.web("#34495e"));

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    /**
     * Displays the "About" dialog with project details and features.
     */
    private void showEnhancedAboutDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Robot Simulator");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, null, null)));

        // Title
        Label title = new Label("Robot Arena Simulator v1.0");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));

        // Features section
        VBox features = new VBox(8);
        features.getChildren().addAll(
                createFeatureLabel("Multiple Robot Types", "🤖"),
                createFeatureLabel("Real-time Collision Detection", "💫"),
                createFeatureLabel("Interactive Controls", "🎮"),
                createFeatureLabel("Simulation Management", "⚙️")
        );

        // Credits section
        Label credits = new Label("CS2OP Coursework Project\nDeveloped by [Your Name]");
        credits.setTextFill(Color.web("#34495e"));
        credits.setFont(Font.font("System", FontWeight.NORMAL, 14));

        content.getChildren().addAll(title, new Separator(), features, new Separator(), credits);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    /**
     * Creates a feature label with an icon and text.
     *
     * @param text the feature description
     * @param icon the emoji/icon representing the feature
     * @return the styled Label
     */
    private Label createFeatureLabel(String text, String icon) {
        Label label = new Label(icon + " " + text);
        label.setFont(Font.font("System", 14));
        label.setTextFill(Color.web("#34495e"));
        return label;
    }

    // ------------------------------------------------------------------------
    // Arena and Animation Initialization (SINGLE DEFINITIONS ONLY)
    // ------------------------------------------------------------------------

    /**
     * Initializes the arena by creating robots and obstacles.
     */
    private void initializeArena() {
        robotArena = new RobotArena(800, 600);
        robotArena.addRobot();
        robotArena.addObstacle();
        robotArena.addChaserRobot();
        arenaView = new ArenaView(robotArena);
        // Pass 'this' if needed by ControlPanel
        controlPanel = new ControlPanel(this, robotArena, arenaView);
    }

    /**
     * Initializes the animation timer to move robots and render the arena.
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
     * Initializes mouse controls for selecting robots in the arena.
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
     * Initializes keyboard controls for moving robots and controlling the simulation.
     *
     * @param scene the main Scene
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
     * Moves the selected robot by the specified delta values.
     *
     * @param dx change in x-coordinate
     * @param dy change in y-coordinate
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

            if (!robotArena.isColliding(newX, newY, selectedRobot.getRadius(), selectedRobot.getId())) {
                selectedRobot.setX(newX);
                selectedRobot.setY(newY);
                updateStatus("Moved " + selectedRobot.getClass().getSimpleName() +
                        " to (" + newX + ", " + newY + ")");
            } else {
                updateStatus("Cannot move " + selectedRobot.getClass().getSimpleName() +
                        " to (" + newX + ", " + newY + ") - Collision detected.");
            }
        }
    }

    /**
     * Updates the status panel with current robot and obstacle counts.
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
     * Logs a status message to the console.
     *
     * @param message the status message
     */
    private void updateStatus(String message) {
        System.out.println(message);
        updateStatus(); // Also refresh counts, if desired
    }

    /**
     * Resets the arena to its default state.
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
     * Toggles the animation state between running and paused.
     */
    private void toggleAnimation() {
        if (isAnimating) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    /**
     * Toggles the animation state based on the provided boolean.
     *
     * @param start true to start animation, false to stop
     */
    public void toggleAnimation(boolean start) {
        if (start) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    /**
     * Starts the animation timer.
     */
    private void startAnimation() {
        if (!isAnimating) {
            isAnimating = true;
            animationTimer.start();
            updateStatus("Animation started.");
        }
    }

    /**
     * Stops the animation timer.
     */
    private void stopAnimation() {
        if (isAnimating) {
            isAnimating = false;
            animationTimer.stop();
            updateStatus("Animation stopped.");
        }
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
