
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Formatter;
import java.util.NoSuchElementException;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Direction.*;

/**
 * Represents the state of a game of Lines of Action.
 * @author JuKyung Choi
 */
class Board implements Iterable<Move> {

    /** Contents of the board. */
    private Piece[][] _content = new Piece[8][8];
    /** Size of a board. */
    static final int M = 8;
    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /**
     * A Board whose initial contents are taken from INITIALCONTENTS and in
     * which the player playing TURN is to move. The resulting Board has
     * get(col, row) == INITIALCONTENTS[row-1][col-1] Assumes that PLAYER is not
     * null and INITIALCONTENTS is MxM.
     *
     * CAUTION: The natural written notation for arrays initializers puts the
     * BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        clear();
    }

    /**
     * A Board whose initial contents and state are copied from BOARD.
     */
    Board(Board board) {
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int i = 0; i < M; i += 1) {
            for (int j = 0; j < M; j += 1) {
                _content[i][j] = contents[i][j];
            }
        }
        _turn = side;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board._moves);
        _turn = board._turn;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                this._content[i][j] = board._content[i][j];
            }
        }
    }

    /**
     * Return the contents of column C, row R, where 1 <= C,R <= 8, where column
     * 1 corresponds to column 'a' in the standard notation.
     */
    Piece get(int c, int r) {
        return _content[r - 1][c - 1];
    }

    /**
     * Return the contents of the square SQ. SQ must be the standard printed
     * designation of a square (having the form cr, where c is a letter from a-h
     * and r is a digit from 1-8).
     */
    Piece get(String sq) {
        return get(col(sq), row(sq));
    }

    /**
     * Return the column number (a value in the range 1-8) for SQ. SQ is as for
     * {@link get(String)}.
     */
    static int col(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(0) - 'a' + 1;
    }

    /**
     * Return the row number (a value in the range 1-8) for SQ. SQ is as for
     * {@link get(String)}.
     */
    static int row(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(1) - '0';
    }

    /**
     * Set the square at column C, row R to V, and make NEXT the next side to
     * move, if it is not null.
     */
    void set(int c, int r, Piece v, Piece next) {
        _content[r - 1][c - 1] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at column C, row R to V. */
    void set(int c, int r, Piece v) {
        set(c, r, v, null);
    }

    /** Assuming isLegal(MOVE), make MOVE. */
    void makeMove(Move move) {
        assert isLegal(move);
        _moves.add(move);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        if (replaced != EMP) {
            set(c1, r1, EMP);
        }
        set(c1, r1, move.movedPiece());
        set(c0, r0, EMP);
        _turn = _turn.opposite();
    }

    /**
     * Retract (unmake) one move, returning to the state immediately before that
     * move. Requires that movesMade () > 0.
     */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Piece replaced = move.replacedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        Piece movedPiece = move.movedPiece();
        set(c1, r1, replaced);
        set(c0, r0, movedPiece);
        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff MOVE is legal for the player currently on move. */
    boolean isLegal(Move move) {
        if (get(move.getCol0(), move.getRow0()).equals(_turn)) {
            if (!get(move.getCol1(), move.getRow1()).equals(_turn)) {
                if (move.length() == pieceCountAlong(move)) {
                    if (move.length() == 1) {
                        return true;
                    }
                    Direction dir = getDir(move);
                    int dr = dir.dr;
                    int dc = dir.dc;
                    int col = move.getCol0();
                    int row = move.getRow0();
                    for (int i = 1; i < move.length(); i++) {
                        col += dc;
                        row += dr;
                        if (!get(col, row).equals(EMP)
                            && !get(col, row).equals(_turn)) {
                            return false;
                        } else {
                            if (i == move.length() - 1) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Return a sequence of all legal moves from this position. */
    Iterator<Move> legalMoves() {
        return new MoveIterator();
    }

    @Override
    public Iterator<Move> iterator() {
        return legalMoves();
    }

    /**
     * Return true if there is at least one legal move for the player on move.
     */
    public boolean isLegalMove() {
        return iterator().hasNext();
    }

    /** Return true iff either player has all his pieces continguous. */
    boolean gameOver() {
        return piecesContiguous(BP) || piecesContiguous(WP);
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        Board cop = new Board(this);
        int[] firstP = firstPiece(cop, side);
        removeCont(cop, firstP, side);
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                if (cop._content[i][j].equals(side)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return indices of the position of first occurrence SIDE of BOARD. */
    static int[] firstPiece(Board board, Piece side) {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                if (board._content[i][j].equals(side)) {
                    return new int[] { i, j };
                }
            }
        }
        return new int[] { -1, -1 };
    }

    /** Return board after removing continuous pieces.
     * Inside BOARD, of piece SIDE, and INDICES.
     */
    static Board removeCont(Board board, int[] indices, Piece side) {
        Piece[][] content = board._content;
        int i = indices[0];
        int j = indices[1];
        if (content[i][j].equals(side)) {
            content[i][j] = EMP;
            if (i != 0 && j != 0 && content[i - 1][j - 1].equals(side)) {
                removeCont(board, new int[] { i - 1, j - 1 }, side);
            }
            if (i != 0 && content[i - 1][j].equals(side)) {
                removeCont(board, new int[] { i - 1, j }, side);
            }
            if (i != 0 && j != M - 1 && content[i - 1][j + 1].equals(side)) {
                removeCont(board, new int[] { i - 1, j + 1 }, side);
            }
            if (j != M - 1 && content[i][j + 1].equals(side)) {
                removeCont(board, new int[] { i, j + 1 }, side);
            }
            if (i != M - 1 && j != M - 1
                && content[i + 1][j + 1].equals(side)) {
                removeCont(board, new int[] { i + 1, j + 1 }, side);
            }
            if (i != M - 1 && content[i + 1][j].equals(side)) {
                removeCont(board, new int[] { i + 1, j }, side);
            }
            if (i != M - 1 && j != 0 && content[i + 1][j - 1].equals(side)) {
                removeCont(board, new int[] { i + 1, j - 1 }, side);
            }
            if (j != 0 && content[i][j - 1].equals(side)) {
                removeCont(board, new int[] { i, j - 1 }, side);
            }
        }
        return board;
    }

    /**
     * Return the total number of moves that have been made (and not retracted).
     * Each valid call to makeMove with a normal move increases this number by
     * 1.
     */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return _content.equals(b._content);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_content);
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = M; r >= 1; r -= 1) {
            out.format("    ");
            for (int c = 1; c <= M; c += 1) {
                out.format("%s ", get(c, r).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return the number of pieces in the line of action indicated by MOVE. */
    private int pieceCountAlong(Move move) {
        return pieceCountAlong(move.getCol0(), move.getRow0(), getDir(move));
    }

    /** Return the direction of MOVE. */
    private Direction getDir(Move move) {
        int hor = move.getCol1() - move.getCol0();
        int vert = move.getRow1() - move.getRow0();
        if (vert != 0) {
            int temp = vert;
            vert /= Math.abs(temp);
            hor /= Math.abs(temp);
        } else if (hor != 0) {
            int temp = hor;
            vert /= Math.abs(temp);
            hor /= Math.abs(temp);
        }
        String s = "" + hor + vert;
        Direction dir = NOWHERE;
        switch (s) {
        case "11":
            return NE;
        case "10":
            return E;
        case "1-1":
            return SE;
        case "0-1":
            return S;
        case "-1-1":
            return SW;
        case "-10":
            return W;
        case "-11":
            return NW;
        case "01":
            return N;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return the number of pieces in the line of action in direction DIR and
     * containing the square at column C and row R.
     */
    private int pieceCountAlong(int c, int r, Direction dir) {
        int count = 0;
        int tempC = c;
        int tempR = r;
        if (!this.get(c, r).equals(EMP)) {
            count += 1;
        }
        while (r > 0 && r < M + 1 && c < M + 1 && c > 0) {
            r -= dir.dr;
            c -= dir.dc;
            if (inBounds(c, r) && !this.get(c, r).equals(EMP)) {
                count += 1;
            }
        }
        while (tempR > 0 && tempR < M + 1 && tempC < M + 1 && tempC > 0) {
            tempR += dir.dr;
            tempC += dir.dc;
            if (inBounds(tempC, tempR) && !this.get(tempC, tempR).equals(EMP)) {
                count += 1;
            }
        }
        return count;
    }

    /** Return true if combination of R, C are in bounds. */
    private static boolean inBounds(int c, int r) {
        if (c > 0 && c < M + 1 && r > 0 && r < M + 1) {
            return true;
        }
        return false;
    }

    /**
     * Return true iff MOVE is blocked by an opposing piece or by a friendly
     * piece on the target square.
     */
    private boolean blocked(Move move) {
        if (!this.get(move.getCol1(), move.getRow1()).equals(EMP)) {
            return true;
        }
        return false;
    }

    /** The standard initial configuration for Lines of Action. */
    static final Piece[][] INITIAL_PIECES =
        { { EMP, BP, BP, BP, BP, BP, BP, EMP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { WP, EMP, EMP, EMP, EMP, EMP, EMP, WP },
        { EMP, BP, BP, BP, BP, BP, BP, EMP } };

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;

    /** An iterator returning the legal moves from the current board. */
    private class MoveIterator implements Iterator<Move> {
        /** Current piece under consideration. */
        private int _c, _r;
        /** Next direction of current piece to return. */
        private Direction _dir;
        /** Next move. */
        private Move _move;
        /** Current board. */
        private Board board = Board.this;
        /** Arraylist of possible moves. */
        private ArrayList<Move> moves = new ArrayList<Move>();

        /** A new move iterator for turn(). */
        MoveIterator() {
            _c = 1;
            _r = 1;
            _dir = NOWHERE;
            incr();
        }

        /** Creates list of all moves. */
        private void moveList() {
            for (int c = 1; c < 9; c++) {
                for (int r = 1; r < 9; r++) {
                    if (Board.this.get(c, r).equals(board._turn)) {
                        while (_dir.succ() != null) {
                            _dir = _dir.succ();
                            int pieces = pieceCountAlong(c, r, _dir);
                            Move move = Move.create(c, r, pieces, _dir, board);
                            if (move != null) {
                                if (board.isLegal(move)) {
                                    System.out.println(c + "  " + r + " _dir: "
                                            + _dir + " pieces: " + pieces);
                                    moves.add(move);
                                }
                            }
                        }
                        _dir = NOWHERE;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return _move != null;
        }

        @Override
        public Move next() {
            if (_move == null) {
                throw new NoSuchElementException("no legal move");
            }
            Move move = _move;
            incr();
            return move;
        }

        @Override
        public void remove() {
        }

        /** Advance to the next legal move. */
        private void incr() {
            Move curr = _move;
            while (!(_c == M && _r == M && _dir == NW)) {
                if (_dir != NW) {
                    _dir = _dir.succ();
                } else {
                    if (_c != M) {
                        _c += 1;
                        _dir = N;
                    } else {
                        _r += 1;
                        _c = 1;
                        _dir = N;
                    }
                }
                int pieces = pieceCountAlong(_c, _r, _dir);
                Move moveTemp = Move.create(_c, _r, pieces, _dir, board);
                if (moveTemp != null) {
                    if (board.isLegal(moveTemp)) {
                        _move = moveTemp;
                        break;
                    }
                }
            }
            if (_c == M && _r == M && _dir == NW && _move.equals(curr)) {
                _move = null;
            }
        }
    }

    /** Sets turn of piece P. */
    public void setTurn(Piece p) {
        _turn = p;
    }
    /** Return turn. */
    public Piece getTurn() {
        return _turn;
    }
}
