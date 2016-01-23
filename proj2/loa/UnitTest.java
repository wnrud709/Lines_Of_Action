package loa;

import static org.junit.Assert.*;
import org.junit.Test;
import static loa.Piece.*;

public class UnitTest {
    /**
     * Test contiguous.
     */
    @Test
    public void contigTest() {
        Board board = new Board(Board.INITIAL_PIECES, BP);
        board.set(1, 1, EMP);
        board.set(2, 1, EMP);
        board.set(3, 1, EMP);
        board.set(4, 1, EMP);
        board.set(5, 1, EMP);
        board.set(6, 1, EMP);
        board.set(7, 1, EMP);
        board.set(8, 1, EMP);
        board.set(3, 3, BP);
        board.set(3, 4, BP);
        board.set(2, 3, BP);
        board.set(2, 6, BP);
        board.set(2, 4, BP);
        board.set(3, 4, BP);
        board.set(5, 7, BP);
        board.set(7, 7, BP);
        assertEquals(board.piecesContiguous(BP), false);
        board.set(3, 6, BP);
        board.set(3, 7, BP);
        assertEquals(board.piecesContiguous(BP), false);
        board.set(3, 5, BP);
        assertEquals(board.piecesContiguous(BP), true);
        board.set(3, 5, EMP);
        assertEquals(board.piecesContiguous(BP), false);
        assertEquals(board.piecesContiguous(WP), false);
        board.set(2, 7, WP);
        board.set(3, 7, WP);
        board.set(4, 7, WP);
        board.set(5, 7, WP);
        board.set(6, 7, WP);
        board.set(7, 7, WP);
        assertEquals(board.piecesContiguous(WP), true);
        board.set(3, 1, WP);
        assertEquals(board.piecesContiguous(WP), false);
        board.set(2, 2, BP);
        assertEquals(board.piecesContiguous(WP), false);
    }
    /** Test if moves are legal. */
    @Test
    public void isLegalTest() {
        Piece black = BP;
        Board board = new Board(Board.INITIAL_PIECES, black);
        String moveString = "b1-b4";
        Move move = Move.create(moveString, board);
        assertEquals(false, board.isLegal(move));
        board.set(2, 2, WP);
        assertEquals(false, board.isLegal(move));
        board.set(2, 3, BP);
        board.setTurn(WP);
        Move move2 = Move.create("a2-d5", board);
        assertEquals(false, board.isLegal(move2));
        board.set(2, 3, WP);
        assertEquals(true, board.isLegal(move2));
        board.set(3, 4, WP);
        assertEquals(false, board.isLegal(move2));
        Move move3 = Move.create("a2-e6", board);
        assertEquals(true, board.isLegal(move3));
    }
    /** main method. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(UnitTest.class));
    }
}
