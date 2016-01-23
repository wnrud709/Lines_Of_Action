package loa;

/** A Player that prompts for moves and reads them from its Game.
 *  @author JuKyung Choi*/
class HumanPlayer extends Player {

    /** A HumanPlayer that plays the SIDE pieces in GAME.  It uses
     *  GAME.getMove() as a source of moves.  */
    HumanPlayer(Piece side, Game game) {
        super(side, game);
        _game = game;
        _piece = side;
    }
    /** Return the side. */
    Piece getSide() {
        return _piece;
    }

    @Override
    Move makeMove() {
        return _game.getMove();
    }
    /** Current game object. */
    private Game _game;
    /** Piece corresponding to turn. */
    private Piece _piece;
}
