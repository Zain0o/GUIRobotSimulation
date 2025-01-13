module CWGUI {
    requires javafx.controls;
    requires javafx.graphics;
    requires transitive javafx.base;
    requires java.logging; // Add this to include java.util.logging

    exports GUI;
    exports RobotSim;
}
