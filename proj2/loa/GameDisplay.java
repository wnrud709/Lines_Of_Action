package loa;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import java.io.InputStream;
import java.io.IOException;

/**
 * A widget that displays Linesof Action.
 * @author JuKyung Choi
 */
class GameDisplay extends Pad {

    /** Color of display field. */
    private static final Color BACKGROUND_COLOR = Color.white;

    /* Coordinates and lengths in pixels unless otherwise stated. */

    /** Preferred dimensions of the playing surface. */
    private static final int BOARD_WIDTH = 400, BOARD_HEIGHT = 400;
    /** Height of the board boundaries */
    /** Displayed dimensions of a card image. */
    private static final int PIECE_HEIGHT = 30, PIECE_WIDTH = 30;

    /** A graphical representation of BOARD. */
    public GameDisplay(Board board) {
        _board = board;
        setPreferredSize(BOARD_WIDTH, BOARD_HEIGHT);
    }

    /** Return an Image read from the resource named NAME. */
    private Image getImage(String name) {
        InputStream in = getClass().getResourceAsStream("/loa/resources/"
            + name);
        try {
            return ImageIO.read(in);
        } catch (IOException excp) {
            return null;
        }
    }

    /** Draw PIECE at X, Y on G. */
    private void paintPiece(Graphics2D g, Piece piece, int x, int y) {
        if (piece != null) {
            g.drawImage(getPieceImage(piece), x, y, PIECE_WIDTH,
                PIECE_HEIGHT, null);
        }
    }

    /** Return an Image of PIECE. */
    private Image getPieceImage(Piece piece) {
        return getImage("playing-cards/" + piece + ".png");
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BACKGROUND_COLOR);
        Rectangle b = g.getClipBounds();
        g.fillRect(0, 0, b.width, b.height);
        Piece bp = Piece.BP;
        Piece wp = Piece.WP;
        for (int i = 0; i < 9; i++) {
            g.drawImage(getImage("playing-cards/" + "vertLine.png"),
                CORNER + WIDTH * i, CORNER, BORDER, LINEWIDTH, null);
            g.drawImage(getImage("playing-cards/" + "horLine.png"),
                CORNER, CORNER + WIDTH * i, LINEHEIGHT,  BORDER, null);
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                if (_board.get(i, j).equals(bp)) {
                    paintPiece(g, Piece.BP, CORNER + BORDER + WIDTH
                            * (i - 1), CORNER + BORDER + WIDTH * (M - j));
                } else if (_board.get(i, j).equals(wp)) {
                    paintPiece(g, Piece.WP, CORNER + BORDER + WIDTH
                            * (i - 1), CORNER + BORDER + WIDTH * (M - j));
                }
            }
        }
    }

    /** Upper left corner of board. */
    private static final int CORNER = 20;
    /** Width of tile. */
    private static final int WIDTH = 40;
    /** Width of border. */
    private static final int BORDER = 5;
    /** Precision. */
    private static final int PREC = 5;
    /** Line height. */
    private static final int LINEHEIGHT = 330;
    /** Line width. */
    private static final int LINEWIDTH = 320;
    /** Number of rows. */
    private static final int M = 8;
    /** Board. */
    private static Board _board;
}
