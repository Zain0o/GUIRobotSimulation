# GUI Robot Simulation

A JavaFX application for simulating various autonomous robotic behaviors in an interactive 2D arena. Users can add different robot types, obstacles, and light sources, observing their interactions in real-time.

## Core Features

*   **Interactive Visual Arena:** Drag & drop items, context menus, selection/hover effects.
*   **Diverse Robots:** Includes Basic, Chaser, Beam, Bump, Predator, and Light-Seeking robots, each with unique behaviors.
*   **Environmental Items:** Add Obstacles and Light Sources.
*   **Simulation Controls:** Start/stop, speed adjustment, clear arena.
*   **File Operations:** Save and load arena configurations.
*   **Debugging:** Detailed logs written to `robot_sim_debug.log`.

## Technologies

*   Java 17+
*   JavaFX
*   Maven

## Getting Started

1.  **Prerequisites:** JDK 17+, Maven.
2.  **Clone:** `git clone https://github.com/Zain0o/GUIROBOT.git GUI-Robot-Simulation`
3.  **Navigate:** `cd GUI-Robot-Simulation`
4.  **Build:** `mvn clean package`
5.  **Run:**
    *   From your IDE (run `GUI.MainApp`).
    *   Or via Maven: `mvn javafx:run` (if javafx-maven-plugin is configured).

## Basic Usage

*   Use UI buttons/menus to add robots and items.
*   Left-click to select/drag items. Right-click for item options.
*   Control simulation (start/stop/speed) via UI or keyboard shortcuts (Spacebar, Ctrl+N/S/O/Q).
*   Save/Load arena states via the File menu or control panel.

---

This project showcases object-oriented programming principles and JavaFX GUI development.
