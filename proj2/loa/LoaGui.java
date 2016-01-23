package loa;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;
import java.awt.event.MouseEvent;

/**
 * A top-level GUI for LineOfAction.
 * @author JuKyung Choi
 */
class LoaGui extends TopLevel {

    /** A new window with given TITLE and displaying BOARD. */
    LoaGui(String title, Board board) {
        super(title, true);
        _board = board;
        addLabel("No Winner Yet", "Winner", new LayoutSpec("y", 0, "x", 0));
        addLabel("Turn: " + _board.getTurn(), "ID",
                new LayoutSpec("y", 1, "x", .5));
        addButton("Quit", "quit", new LayoutSpec("y", 0, "x", 1));
        addButton("Undo", "undoMove", new LayoutSpec("y", 1, "x", 1));
        _display = new GameDisplay(_board);
        add(_display, new LayoutSpec("y", 2, "width", 2));
        _display.setMouseHandler("press", this, "mousePressed");
        _display.setMouseHandler("release", this, "mouseReleased");
        _display.setMouseHandler("drag", this, "mouseDragged");
        display(true);
    }

    /** Respond to "Quit" button. */
    public void quit(String dummy) {
        System.exit(1);
    }

    /**
     * Responds to "undoMove" button.
     */
    public void undoMove(String dummy) {
        _board.retract();
        _display.repaint();
    }

    /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mousePressed(MouseEvent event) {
        clickedX = event.getX();
        clickedY = event.getY();
        numPres = tile(clickedX, clickedY);

    }

    /** Return corresponding tile given X and Y coordinate. */
    private int[] tile(int x, int y) {
        int[] count = new int[2];
        outer: for (int i = CORNER + BORDER; i <= X; i += WIDTH) {
            count[0]++;
            for (int j = CORNER + BORDER; j <= Y; j += WIDTH) {
                count[1]++;
                if (x >= i + PREC && x <= i + WIDTH - PREC
                        && y >= j + PREC && y <= j + WIDTH - PREC) {
                    break outer;
                }
            }
            count[1] = 0;
        }
        count[1] = 9 - count[1];
        return count;
    }

    /**
     * Changes NUM to corresponding alphabet.
     * Return the letter.
     */
    private String toStr(int num) {
        switch (num) {
        case 1:
            return "a";
        case 2:
            return "b";
        case 3:
            return "c";
        case 4:
            return "d";
        case 5:
            return "e";
        case 6:
            return "f";
        case 7:
            return "g";
        case 8:
            return "h";
        default:
            return "k";
        }
    }

    /** Action in response to mouse-released event EVENT. */
    public synchronized void mouseReleased(MouseEvent event) {
        relX = event.getX();
        relY = event.getY();
        numRel = tile(relX, relY);
        String presTile = toStr(numPres[0]) + Integer.toString(numPres[1]);
        String relTile = toStr(numRel[0]) + Integer.toString(numRel[1]);
        String str = presTile + "-" + relTile;
        try {
            Move move = Move.create(str, _board);
            if (_board.isLegal(move)) {
                _board.makeMove(move);
            }
        } catch (NullPointerException e) {
            /* Ignore ILLEGALARGUMENTEXCEPTION. */
        }
        if (_board.piecesContiguous(Piece.BP)
                && _board.piecesContiguous(Piece.WP)) {
            setLabel("Winner", _board.getTurn().opposite().fullName() + " won");
        } else if (_board.piecesContiguous(Piece.BP)) {
            setLabel("Winner", Piece.BP.fullName() + " won");
        } else if (_board.piecesContiguous(Piece.WP)) {
            setLabel("Winner", Piece.WP.fullName() + " won");
        }
        _display.repaint();
        setLabel("ID", "Turn: " + _board.getTurn());
    }

    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
        int dragX = event.getX();
        int dragY = event.getY();
        _display.repaint();
    }

    /** The board widget. */
    private final GameDisplay _display;

    /** Clicked x coord. */
    private static int clickedX;
    /** Clicked y coord. */
    private static int clickedY;
    /** Released x coord. */
    private static int relX;
    /** Released y coord. */
    private static int relY;
    /** String represented by tile. */
    private static int string;
    /** Upper left corner of board. */
    private static final int CORNER = 20;
    /** Width of tile. */
    private static final int WIDTH = 40;
    /** Width of border. */
    private static final int BORDER = 5;
    /** Precision. */
    private static final int PREC = 5;
    /** Pressed tile in number pair. */
    private static int[] numPres;
    /** Released tile in number pair. */
    private static int[] numRel;
    /** current board. */
    private static Board _board;
    /** Upper bound for x. */
    private static final int X = 360;
    /** Upper bound for y. */
    private static final int Y = 370;
}
