package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Board {

    // A 0x88 integer array representing pieces on a chess board.
    public int[] board;

    // The constructor for class Board.
    // Initialises the board to the chess starting position and sets the current player colour to white.
    public Board() {
        this.board = new int[] {
                WROOK, WKNIGHT, WBISHOP, WQUEEN, WKING, WBISHOP, WKNIGHT, WROOK, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BROOK, BKNIGHT, BBISHOP, BQUEEN, BKING, BBISHOP, BKNIGHT, BROOK, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };
    }

    // Another constructor for class Board.
    // Initialises the board to the positions represented by the FEN string.
    public Board(String fen) {
        GameState gameState = Translator.gameStateFromFEN(fen);
        this.board = gameState.getCurrentBoard();
    }

    static int[] getFreshBoard(){
        return new int[] {
                WROOK, WKNIGHT, WBISHOP, WQUEEN, WKING, WBISHOP, WKNIGHT, WROOK,    EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN, WPAWN,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN, BPAWN,             EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
                BROOK, BKNIGHT, BBISHOP, BQUEEN, BKING, BBISHOP, BKNIGHT, BROOK,    EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };

    }

    static int[] getSetup(){
        //Build a function here that contacts the GUI to either get a clean board or a FEN board


        return getFreshBoard();
    }
}
