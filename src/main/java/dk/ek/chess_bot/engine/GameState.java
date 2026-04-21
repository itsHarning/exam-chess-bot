package dk.ek.chess_bot.engine;

public class GameState {
    private int[] board;
    private boolean blackCastleKingSide;
    private boolean blackCastleQueenSide;
    private boolean whiteCastleKingSide;
    private boolean whiteCastleQueenSide;
    private boolean isWhiteToMove;
    private int enPassantIndex;
    private int totalMoves;
    private int halfMoveClock;

    public GameState(String fen){
        //Set variables by interpreting FEN string
    }

    public GameState(){
        this.board = Board.getFreshBoard();
        this.blackCastleKingSide = true;
        this.blackCastleQueenSide = true;
        this.whiteCastleKingSide = true;
        this.whiteCastleQueenSide = true;
        this.isWhiteToMove = true;
        this.enPassantIndex = -1;
        this.totalMoves = 0;
        this.halfMoveClock = 0;
    }

    public int[] getCurrentBoard() {
        return board;
    }

    public void setCurrentBoard(int[] currentBoard) {
        this.board = currentBoard;
    }

    public boolean isBlackCastleKingSide() {
        return blackCastleKingSide;
    }

    public void setBlackCastleKingSide(boolean blackCastleKingSide) {
        this.blackCastleKingSide = blackCastleKingSide;
    }

    public boolean isBlackCastleQueenSide() {
        return blackCastleQueenSide;
    }

    public void setBlackCastleQueenSide(boolean blackCastleQueenSide) {
        this.blackCastleQueenSide = blackCastleQueenSide;
    }

    public boolean isWhiteCastleKingSide() {
        return whiteCastleKingSide;
    }

    public void setWhiteCastleKingSide(boolean whiteCastleKingSide) {
        this.whiteCastleKingSide = whiteCastleKingSide;
    }

    public boolean isWhiteCastleQueenSide() {
        return whiteCastleQueenSide;
    }

    public void setWhiteCastleQueenSide(boolean whiteCastleQueenSide) {
        this.whiteCastleQueenSide = whiteCastleQueenSide;
    }

    public boolean isWhiteToMove() {
        return isWhiteToMove;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        isWhiteToMove = whiteToMove;
    }

    public int getEnPassantIndex() {
        return enPassantIndex;
    }

    public void setEnPassantIndex(int enPassantIndex) {
        this.enPassantIndex = enPassantIndex;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public void setHalfMoveClock(int halfMoveClock) {
        this.halfMoveClock = halfMoveClock;
    }
}
