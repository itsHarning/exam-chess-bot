module dk.ek.chess_bot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens dk.ek.chess_bot.engine to javafx.fxml;
    exports dk.ek.chess_bot.engine;
    exports dk.ek.chess_bot;
    opens dk.ek.chess_bot to javafx.fxml;
}