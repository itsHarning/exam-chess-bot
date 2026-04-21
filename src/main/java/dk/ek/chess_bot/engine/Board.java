package dk.ek.chess_bot.engine;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Board {

    // A 0x88 integer array representing pieces on a chess board.
    public int[] board;

    // The colour of the current player. true = white, false = black
    public boolean colour;

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
        this.colour = WHITE;
    }

    // Another constructor for class Board.
    // Initialises the board to the positions represented by the FEN string.
    public Board(String fen) {
        this.board = new Fen().parseFenString(fen);
        this.colour = WHITE;
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


    static void printBoard(int[] board){
        boolean whitespace = true;
        for (int i = 7; i > -1; i--) {
            switch (i){
                case 0:
                    System.out.print("1");
                    break;
                case 1:
                    System.out.print("2");
                    break;
                case 2:
                    System.out.print("3");
                    break;
                case 3:
                    System.out.print("4");
                    break;
                case 4:
                    System.out.print("5");
                    break;
                case 5:
                    System.out.print("6");
                    break;
                case 6:
                    System.out.print("7");
                    break;
                case 7:
                    System.out.print("8");
                    break;
            }

            for (int j = 0; j < 8; j++) {
                System.out.print("\t");
                int space = board[(j+(i*16))];

                switch (space){
                    case 1:
                        System.out.print("♙");
                        break;
                    case 2:
                        System.out.print("♖");
                        break;
                    case 3:
                        System.out.print("♘");
                        break;
                    case 4:
                        System.out.print("♗");
                        break;
                    case 5:
                        System.out.print("♕");
                        break;
                    case 6:
                        System.out.print("♔");
                        break;
                    case 9:
                        System.out.print("♟");
                        break;
                    case 10:
                        System.out.print("♜");
                        break;
                    case 11:
                        System.out.print("♞");
                        break;
                    case 12:
                        System.out.print("♝");
                        break;
                    case 13:
                        System.out.print("♛");
                        break;
                    case 14:
                        System.out.print("♚");
                        break;
                    default:
                        if(whitespace){
                            System.out.print("⬜");
                        }else{
                            System.out.print("⬛");
                        }
                }
                whitespace = !whitespace;
            }
            System.out.println();
            whitespace = !whitespace;
        }
        System.out.println("###############################");
    }
}
