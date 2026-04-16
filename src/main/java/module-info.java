module dk.harning.chess_demo {
    requires javafx.controls;
    requires javafx.fxml;
    //requires dk.harning.chess_demo;
    //requires dk.harning.chess_demo;
    //requires dk.harning.chess_demo;

    opens dk.harning.chess_demo.engine to javafx.fxml;
    exports dk.harning.chess_demo.engine;
    exports dk.harning.chess_demo;
    opens dk.harning.chess_demo to javafx.fxml;
}