module dk.harning.chess_demo {
    requires javafx.controls;
    requires javafx.fxml;

    opens dk.ek.chess_bot.engine to javafx.fxml;
    exports dk.ek.chess_bot.engine;
    exports dk.ek.chess_bot;
    opens dk.ek.chess_bot to javafx.fxml;
}