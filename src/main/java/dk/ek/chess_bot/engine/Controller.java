package dk.ek.chess_bot.engine;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.ResourceBundle;

import java.net.URL;

import static dk.ek.chess_bot.engine.Pieces.*;

public class Controller implements Initializable {

	@FXML
	private GridPane grid;
	@FXML
	private Button fromFEN;
	@FXML
	private TextField fenField;

	private Pane paneToMove;
	private String paneColour = "";
	private String pieceToMove = "";
	private int from;
	private int to;

	private Board board;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.board = new Board();

		initBoard();
	}

	@FXML
	public void FENToBoard() {
		System.out.println(fenField.getText());
		String fenString = fenField.getText();
		// Example fen string
		// 5r2/q3k3/b1pp4/2n1p1b1/2P1P3/1p1P1Q1P/1P2NPP1/3RK2R w - - 0 1
		this.board = new Board(fenString);

		initBoard();
	}

	@FXML
	public void boardToFEN() {
		// TODO: get fen string from current board
	}

	@FXML
	public void initBoard() {
		// start.setVisible(false);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int[] board = this.board.board;
				System.out.println("(" + i + ", " + j + ")");
				int square = convert0x88(i, j);
				System.out.println(square);
				int piece = board[square];
				System.out.println(piece);

				Pane pane = new Pane();

				pane.setOnMousePressed((MouseEvent event) -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						if (paneToMove == null) {
							// System.out.println(pane.getStyle());
							String[] splitCSS = pane.getStyle().split("(?<=;)");
							// System.out.println(Arrays.toString(splitCSS));

							if (splitCSS.length > 1) {
								paneColour = splitCSS[0];
								pieceToMove = splitCSS[1] + splitCSS[2];
								splitCSS[0] = "-fx-background-color: #FF0000;";
								pane.setStyle(splitCSS[0] + splitCSS[1] + splitCSS[2]);
							} else {
								paneToMove = null;
								return;
							}
							paneToMove = pane;
							from = square;

							System.out.println(from);

							// System.out.println(paneColour);
						} else {
							to = square;

							System.out.println(to);

							pane.setStyle(pane.getStyle() + pieceToMove);

							paneToMove.setStyle(paneColour);
							paneToMove = null;
						}
					}
				});

				String light = "-fx-background-color: #e8ceab;";
				String dark = "-fx-background-color: #bc7944;";
				String bc = "-fx-background-size: 80 80;" + "-fx-background-position: center;";

				if (piece != 0) {
					if (i % 2 == 0) {
						if (j % 2 == 0) {
							// light
							pane.setStyle(light
									+ "-fx-background-image: url('" + getClass().getResource("/pieces/" + pieceImages[piece]).toExternalForm() + "');"
									+ bc);
						} else {
							// dark
							pane.setStyle(
									dark
									+ "-fx-background-image: url('" + getClass().getResource("/pieces/" + pieceImages[piece]).toExternalForm() + "');"
									+ bc
							);
						}
					} else {
						if (j % 2 != 0) {
							pane.setStyle(
									light
									+ "-fx-background-image: url('" + getClass().getResource("/pieces/" + pieceImages[piece]).toExternalForm() + "');"
									+ bc
							);
						} else {
							pane.setStyle(
									dark
									+ "-fx-background-image: url('" + getClass().getResource("/pieces/" + pieceImages[piece]).toExternalForm() + "');"
									+ bc
							);
						}
					}
				} else {
					if (i % 2 == 0) {
						if (j % 2 == 0) {
							pane.setStyle(light);
						} else {
							pane.setStyle(dark);
						}
					} else {
						if (j % 2 != 0) {
							pane.setStyle(light);
						} else {
							pane.setStyle(dark);
						}
					}

				}

				grid.add(pane, j, i);
			}
		}
	}

	public int convert0x88(int y, int x) {
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
