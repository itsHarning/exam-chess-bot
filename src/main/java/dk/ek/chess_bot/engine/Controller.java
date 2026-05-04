package dk.ek.chess_bot.engine;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.LinkedList;
import java.util.ResourceBundle;

import java.net.URL;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Controller implements Initializable {

	@FXML
	private GridPane grid;

	@FXML
	private Toggle whiteTurn;
	@FXML
	private Toggle blackTurn;

	@FXML
	private TextField fenField;

	private Pane paneToMove;
	private String paneColour = "";
	private String pieceToMove = "";
    private int from;
	private int to;

    Pane clickedPane;
    int clickedPiece;
    int fromIndex;

	private GameState gameState;
    private LinkedList<String> history = new LinkedList<>();
    private int historyPointer = 0;

	@FXML
	public void setTurnWhite() {
		gameState.setWhiteToMove(!gameState.isWhiteToMove());
        blackTurn.setSelected(!whiteTurn.isSelected());
	}

	@FXML
	public void setTurnBlack() {
		gameState.setWhiteToMove(!gameState.isWhiteToMove());
        whiteTurn.setSelected(!blackTurn.isSelected());
	}

    public void swapTurn(){
        System.out.println("Swapping the turn!");
        if (gameState.isWhiteToMove()){
            whiteTurn.setSelected(true);
            blackTurn.setSelected(false);
        }else{
            blackTurn.setSelected(true);
            whiteTurn.setSelected(false);
        }
    }

	@FXML
	public void FENToBoard() {
		System.out.println(fenField.getText());
		String fenString = fenField.getText();

		// Example fen string
		// 5r2/q3k3/b1pp4/2n1p1b1/2P1P3/1p1P1Q1P/1P2NPP1/3RK2R w - - 0 1

		gameState = Translator.gameStateFromFEN(fenString);

		initBoard();
	}

    @FXML
    public void makeMove(){
        gameState = Bot.getNextMove(gameState, 3000000);
        history.add(Translator.gameStateToFEN(gameState));
        historyPointer++;
        swapTurn();

        renderBoard();
    }

	@FXML
	public void boardToFEN() {
		printBoard(gameState.getCurrentBoard());

		System.out.println(Translator.gameStateToFEN(gameState));
	}

	private static void printBoard(int[] board) {
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				int index = rank * 16 + file;
				int piece = board[index];

				if (piece == EMPTY) {
					System.out.print(". ");
				} else {
					System.out.print(piece + " ");
				}
			}
			System.out.println();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.gameState = new GameState();
        history.add(Translator.gameStateToFEN(gameState));
		whiteTurn.setSelected(gameState.isWhiteToMove());
		blackTurn.setSelected(!gameState.isWhiteToMove());

        populateGrid();
		buildBoardRep();
        renderBoard();
	}

    public void populateGrid(){
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // sets up coordinates on side of the board
                if (j == 0 || j == 9 || i == 0 || i == 9) {
                    Text text = new Text();
                    if (j == 0 || j == 9) {
                        text.setText(String.valueOf(9-i));
                    }
                    if (i == 0 || i == 9) {
                        char letter = 0;
                        switch (j) {
                            case 1 -> letter = 'A';
                            case 2 -> letter = 'B';
                            case 3 -> letter = 'C';
                            case 4 -> letter = 'D';
                            case 5 -> letter = 'E';
                            case 6 -> letter = 'F';
                            case 7 -> letter = 'G';
                            case 8 -> letter = 'H';
                        };
                        text.setText("\t" + letter);

                        // TODO get center alignment to work
                        text.setTextAlignment(TextAlignment.CENTER);
                        // text.setWrappingWidth(50);
                        text.wrappingWidthProperty().bind(grid.prefWidthProperty());
                    }
                    grid.add(text, j, i);
                    continue;
                }

                // changes i/j to fit board coordinates
                i--;
                j--;

                Pane pane = panes[i][j];

                i++;
                j++;
                grid.add(pane, j, i);
            }
        }
    }

    Pane[][] panes = {
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
            {new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane(), new Pane()},
    };


    @FXML
    public void buildBoardRep(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Pane pane = panes[i][j];
                pane.getStyleClass().add("boardSquare");
                pane.setPrefSize(80, 80);

                int[] board = this.gameState.getCurrentBoard();

                int square = convertTo0x88(i, j);

                int finalI = i;
                int finalJ = j;
                pane.setOnMousePressed((MouseEvent event) -> {
                    if (clickedPane == null){
                        System.out.println("You clicked a pane");
                        clickedPiece = board[convertTo0x88(finalI, finalJ)];
                        if(clickedPiece != 0){
                            System.out.println("You clicked a piece");
                            fromIndex = convertTo0x88(finalI, finalJ);
                            clickedPane = pane;
                            int[] moves = new int[64];
                            int counter = MoveController.getMoves(this.gameState.isWhiteToMove(), square, board, moves, 0);

                            for (int k = 0; k < counter; k++) {
                                int[] targetXY = convertFrom0x88(IntegerEncoder.decodeToSquare(moves[k]));
                                System.out.println("Found moves");
                                System.out.println((targetXY[0] +1 ) + ", " + (targetXY[1]+1));
                                panes[7-targetXY[1]][targetXY[0]].getStyleClass().add("target");
                            }

                            setSelectedPaneStyling();
                        }
                    }
                    else{
                        System.out.println("You cliced away from a pane");
                        clickedPane.getStyleClass().remove("clicked");

                        board[convertTo0x88(finalI,finalJ)] = clickedPiece;
                        board[fromIndex] = 0;

                        gameState.setCurrentBoard(board);
                        history.add(Translator.gameStateToFEN(gameState));
                        historyPointer++;
                        gameState.setWhiteToMove(!gameState.isWhiteToMove());
                        swapTurn();

                        clickedPane = null;
                        clickedPiece = 0;
                        renderBoard();

                    }
                });
            }
        }
    }

    public void setSelectedPaneStyling(){
        clickedPane.getStyleClass().add("clicked");
    }

	@FXML
	public void initBoard() {

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {

				int[] board = this.gameState.getCurrentBoard();

				int square = convertTo0x88(i, j);
				int piece = board[square];

				Pane pane = panes[i][j];

				String imageSettings = "-fx-background-size: 80 80;" + "-fx-background-position: center;";

				pane.setOnMousePressed((MouseEvent event) -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						if (paneToMove == null) {
                            int[] moves = new int[64];
                            int counter = MoveController.getMoves(this.gameState.isWhiteToMove(), square, board, moves, 0);

                            for (int k = 0; k < counter; k++) {
                                int[] targetXY = convertFrom0x88(IntegerEncoder.decodeToSquare(moves[k]));
                                System.out.println("Found moves");
                                System.out.println(targetXY[0] + ", " + targetXY[1]);
                                panes[7-targetXY[1]][targetXY[0]].setStyle("-fx-stroke: #FF0000; -fx-stroke-width: 5");
                            }

							String[] splitCSS = pane.getStyle().split("(?<=;)");

							if (splitCSS.length > 1) {
								paneColour = splitCSS[0];
								pieceToMove = splitCSS[1];
								splitCSS[0] = "-fx-background-color: #FF0000;";
								pane.setStyle(splitCSS[0] + splitCSS[1] + imageSettings);
							} else {
								paneToMove = null;
								return;
							}
							paneToMove = pane;
							from = square;

							System.out.println("from " + from);
							System.out.println("piece type " + board[to]);

						} else {
							to = square;

							System.out.println("to " + to);

							board[to] = board[from]; // set the piece in its new location
							board[from] = 0; // sets the place where piece was to empty

							pane.setStyle(
									pane.getStyle().split("(?<=;)")[0]
									+ pieceToMove
									+ imageSettings
							);

							paneToMove.setStyle(paneColour);
							paneToMove = null;

							gameState.setWhiteToMove(!gameState.isWhiteToMove());
							whiteTurn.setSelected(gameState.isWhiteToMove());
							blackTurn.setSelected(!gameState.isWhiteToMove());
						}

						gameState.setCurrentBoard(board);
					}
				});
			}
		}
        renderBoard();
	}

    @FXML
    public void revertGameState(){
        System.out.println("Reverting game state");
        if (historyPointer>0) {
            historyPointer--;
            gameState = Translator.gameStateFromFEN(history.get(historyPointer));
            history.removeLast();
            buildBoardRep();
            renderBoard();
        }
        else {
            System.out.println("You have reached the beginning of the history.");
        }
    }

    @FXML
    public void renderBoard(){
        System.out.println("Rendering board");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // changes i/j to fit board coordinates
                int[] board = this.gameState.getCurrentBoard();
                int square = convertTo0x88(i, j);
                int piece = board[square];

                Pane pane = panes[i][j];

                pane.getStyleClass().clear();
                pane.getStyleClass().add("boardSquare");

                if (piece != 0) {
                    if (i % 2 == 0) {
                        if (j % 2 == 0) {
                            // light
                            pane.getStyleClass().add("light");
                        } else {
                            pane.getStyleClass().add("dark");
                        }
                    } else {
                        if (j % 2 != 0) {
                            pane.getStyleClass().add("light");
                        } else {
                            pane.getStyleClass().add("dark");
                        }
                    }

                    //pane.setStyle(pane.getStyle() + "-fx-background-image: url('" + getClass().getResource("/pieces/" + getPieceTypeString(piece)) + "');");
                    pane.getStyleClass().add(getPieceTypeString(piece));

                    pane.setId(String.valueOf(piece));
                } else {
                    pane.getStyleClass().clear();
                    if (i % 2 == 0) {
                        if (j % 2 == 0) {
                            pane.getStyleClass().add("light");
                        } else {
                            pane.getStyleClass().add("dark");
                        }
                    } else {
                        if (j % 2 != 0) {
                            pane.getStyleClass().add("light");
                        } else {
                            pane.getStyleClass().add("dark");
                        }
                    }
                    pane.setId("0");
                }
            }
        }
    }

    String getPieceTypeString(int pieceNr){
        switch (pieceNr){
            case 1:
                return "wPawn";
            case 2:
                return "wKnight";
            case 3:
                return "wBishop";
            case 4:
                return "wRook";
            case 5:
                return "wQueen";
            case 6:
                return "wKing";
            case 9:
                return "bPawn";
            case 10:
                return "bKnight";
            case 11:
                return "bBishop";
            case 12:
                return "bRook";
            case 13:
                return "bQueen";
            case 14:
                return "bKing";
        }
        return "";
    }

    int[] convertFrom0x88(int i) {
        // The row is the index divided by 16
        int y = i >> 4;

        // The column is the index modulo 16
        int x = i & 0x07;

        return new int[]{x, y};
    }

	public int convertTo0x88(int y, int x) {
		int index = 0;
		switch (y + 1) {
		case 1:
			index = ((y * 16) + x) + 112;
			break;
		case 2:
			index = ((y * 16) + x) + 80;
			break;
		case 3:
			index = ((y * 16) + x) + 48;
			break;
		case 4:
			index = ((y * 16) + x) + 16;
			break;
		case 5:
			index = ((y * 16) + x) - 16;
			break;
		case 6:
			index = ((y * 16) + x) - 48;
			break;
		case 7:
			index = ((y * 16) + x) - 80;
			break;
		case 8:
			index = ((y * 16) + x) - 112;
			break;
		}
		return index;
	}
}
