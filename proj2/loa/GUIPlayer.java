package loa;

/** A type of player that gets input from the mouse, and reports
 *  game positions and reports errors on a GUI.
 *  @author JuKyung Choi
 */
class GUIPlayer extends Player {

    /** A GUIPlayer that makes moves on GAME of SIDE. */
    GUIPlayer(Game game, Piece side) {
        super(side, game);
    }

    /** Plays the game. */
    void play() {
        _board = new Board();
        _display = new LoaGui("Lines of Action", _board);
    }

    /** Displays the playing surface. */
    private LoaGui _display;
    /** Current board. */
    private Board _board;

    @Override
    Move makeMove() {
        return null;
    }

}
