package loa;
import java.util.HashMap;

/**
 * An automated Player.
 * @author JuKyung Choi
 */
class MachinePlayer extends Player {
    /** Depth of tree. */
    private static final int DEPTH = 2;
    /** Worst possible move value. */
    private static final double WORST = -100;
    /** Current turn. */
    private Piece _side;
    /** Links a move to its value. */
    private HashMap<Move, Double> _map = new HashMap<Move, Double>();
    /** Current game. */
    private Game _game;

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
        _side = side;
        _game = game;
    }

    @Override
    Move makeMove() {
        Board copy = new Board(getBoard());
        Move m = findBestMove(_side, copy, DEPTH, Double.MAX_VALUE);
        if (m == null) {
            m = getBoard().legalMoves().next();
        }
        System.out.println(_side.abbrev().toUpperCase() + "::" + m);
        return m;
    }

    /** Return best move DEPTH steps ahead on board START of turn SIDE
     * using CUTOFF to prune. */
    Move findBestMove(Piece side, Board start, int depth, double cutoff) {
        if (start.piecesContiguous(side)) {
            _map.put(null, Double.MAX_VALUE);
            return null;
        } else if (start.piecesContiguous(side.opposite())) {
            _map.put(null, WORST);
            return null;
        } else if (depth == 0) {
            return guessBestMove(side, start);
        }
        double value = WORST;
        Move bestFar = null;
        for (Move move : start) {
            Board copy = makeCopyMove(start, move);
            Move response = findBestMove(side.opposite(),
                    copy, depth - 1, value);
            if (-_map.get(response) > value) {
                value = -_map.get(response);
                bestFar = move;
                _map.put(bestFar, value);
                if (value >= cutoff) {
                    break;
                }
            }
        }
        return bestFar;
    }

    /** Return best move at depth 0 on BOARD of SIDE. */
    private Move guessBestMove(Piece side, Board board) {
        Move bestFar = null;
        double val = WORST;
        for (Move move : board) {
            board.makeMove(move);
            double eval = eval(board, side);
            if (eval > val) {
                bestFar = move;
                val = eval;
            }
            board.retract();
        }
        _map.put(bestFar, val);
        return bestFar;
    }

    /** Return evaluation of BOARD of turn SIDE. */
    private static double eval(Board board, Piece side) {
        int[] com = com(board, side);
        int colCom = com[0];
        int rowCom = com[1];
        int count = com[2];
        int distFromCom = distFromCOM(board, colCom, rowCom, side);
        int empSq = distFromCom - minDist(count, colCom, rowCom);
        int central = centralize(board, side);
        double evalSide = 1.0 / empSq + 1.0 / central;
        Piece sideOp = side.opposite();
        int[] comOp = com(board, sideOp);
        int colComOp = comOp[0];
        int rowComOp = comOp[1];
        int countOp = comOp[2];
        int distFromComOp = distFromCOM(board, colComOp, rowComOp, sideOp);
        int empSqOp = distFromComOp - minDist(countOp, colComOp, rowComOp);
        int centralOp = centralize(board, sideOp);
        double evalOp = 1.0 / empSqOp + 1.0 / centralOp;
        return evalSide - evalOp;
    }

    /** Return the center of mass of SIDE on BOARD. */
    private static int[] com(Board board, Piece side) {
        int[] com = new int[] {0, 0, 0};
        int count = 0;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (board.get(i, j).equals(side)) {
                    com[0] += i;
                    com[1] += j;
                    count++;
                }
            }
        }
        com[0] /= count;
        com[1] /= count;
        com[2] = count;
        return com;
    }

    /**
     * Assigns values to piece on BOARD of SIDE depending on how far it is from
     * center. Further pieces get less numbers. Return sum of the pieces
     */
    private static int centralize(Board board, Piece side) {
        int sum = 0;
        int min = 0;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (board.get(i, j).equals(side)) {
                    int colDif = (int) Math.abs(COMCOL - i);
                    int rowDif = (int) Math.abs(COMROW - j);
                    min = (int) Math.min(colDif, rowDif);
                    sum += rowDif + colDif - min;
                }
            }
        }
        return sum;
    }

    /**
     * Return sum of distance between the center of mass and all SIDE pieces
     * from COMX, COMY on BOARD.
     */
    private static int distFromCOM(Board board, int comX, int comY,
            Piece side) {
        int sum = 0;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (board.get(i, j).equals(side)) {
                    int xDif = Math.abs(i - comX);
                    int yDif = Math.abs(j - comY);
                    int min = Math.min(xDif, yDif);
                    sum += xDif + yDif - min;
                }
            }
        }
        return sum;
    }

    /**
     * Return the minimum number of squares COUNT number of pieces could be from
     * COMX, COMY.
     */
    public static int minDist(int count, int comX, int comY) {
        count -= 1;
        if ((comX == 1 && comY == 1) || (comX == 1 && comY == 8)
                || (comX == 8 && comY == 1)
                || (comX == 8 && comY == 8)) {
            return count;
        } else if (comX == 0 || comY == 0) {
            if (count <= 5) {
                return count;
            } else {
                return 5 + 2 * (count - 5);
            }
        } else {
            if (count <= 8) {
                return count;
            } else {
                return 8 + 2 * (count - 8);
            }
        }
    }

    /** Return the board after making MOVE on BOARD. */
    private Board makeCopyMove(Board board, Move move) {
        board.makeMove(move);
        Board copy = new Board(board);
        board.retract();
        return copy;
    }
    /** Return the side. */
    Piece getSide() {
        return _side;
    }
    /** Column center of board. */
    private static final double COMCOL = 4.5;
    /** Row center of board. */
    private static final double COMROW = 4.5;
}
