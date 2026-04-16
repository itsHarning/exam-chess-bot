package dk.ek.chess_bot.engine;

public class Main {
    public static void main(String[] args) {

        // Initialises board with FEN string
        Board board = new Board();

        for (int square = 0; square < 128; square++) {
            if (square % 16 == 0) {
                System.out.println();
            }
            if (square % 16 < 8) {
                switch (board.board[square]) {
                    case 0:
                        if (square % 32 > 15) {
                            if (square % 2 == 1) {
                                System.out.print("⬛");
                            } else {
                                System.out.print("⬜");
                            }
                        } else {
                            if (square % 2 == 1) {
                                System.out.print("⬜");
                            } else {
                                System.out.print("⬛");
                            }
                        }
                        // System.out.print(square);
                        break;
                    case 1:
                        // white pawn
                        System.out.print("🫡");
                        break;
                    case 2:
                        // white knight
                        System.out.print("🤠");
                        break;
                    case 3:
                        // white bishop
                        System.out.print("😇");
                        break;
                    case 4:
                        // white rook
                        System.out.print("😜");
                        break;
                    case 5:
                        // white queen
                        System.out.print("🥰");
                        break;
                    case 6:
                        // white king
                        System.out.print("😎");
                        break;
                    case 7:
                        // black pawn
                        System.out.print("🐭");
                        break;
                    case 8:
                        // black knight
                        System.out.print("🐴");
                        break;
                    case 9:
                        // black bishop
                        System.out.print("🦊");
                        break;
                    case 10:
                        // black rook
                        System.out.print("🐗");
                        break;
                    case 11:
                        // black queen
                        System.out.print("🐯");
                        break;
                    case 12:
                        // black king
                        System.out.print("🦁");
                        break;
                }
            }
        }
    }
}