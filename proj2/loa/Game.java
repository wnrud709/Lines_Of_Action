package loa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static loa.Piece.*;
import static loa.Main.*;

/**
 * Represents one game of Lines of Action.
 * @author JuKyung Choi
 */
class Game {

    /** A new series of Games. */
    Game() {
        _randomSource = new Random();

        _players = new Player[2];
        _input = new BufferedReader(new InputStreamReader(System.in));
        _players[0] = new HumanPlayer(BP, this);
        _players[1] = new MachinePlayer(WP, this);
        _playing = false;
    }

    /** Return the current board. */
    Board getBoard() {
        return _board;
    }

    /** Quit the game. */
    private void quit() {
        System.exit(0);
    }

    /**
     * Return a move. Processes any other intervening commands as well. Exits
     * with null if the value of _playing changes.
     */
    Move getMove() {
        try {
            boolean playing0 = _playing;
            while (_playing == playing0) {
                prompt();

                String line = _input.readLine();
                if (line == null) {
                    quit();
                }

                line = line.trim();
                if (!processCommand(line)) {
                    Move move = Move.create(line, _board);
                    if (move == null) {
                        error("invalid move: %s%n", line);
                    } else if (!_playing) {
                        error("game not started");
                    } else if (!_board.isLegal(move)) {
                        error("illegal move: %s%n", line);
                    } else {
                        return move;
                    }
                }
            }
        } catch (IOException excp) {
            error(1, "unexpected I/O error on input");
        }
        return null;
    }

    /** Print a prompt for a move. */
    private void prompt() {
        System.out.print("> ");
        System.out.flush();
    }

    /** Describes a command with up to two arguments. */
    private static final Pattern COMMAND_PATN =
        Pattern.compile("(#|\\S+)\\s*(\\S*)\\s*(\\S*).*");

    /**
     * If LINE is a recognized command other than a move, process it and return
     * true. Otherwise, return false.
     */
    private boolean processCommand(String line) {
        if (line.length() == 0) {
            return true;
        }
        Matcher command = COMMAND_PATN.matcher(line);
        if (command.matches()) {
            switch (command.group(1).toLowerCase()) {
            case "clear":
                _playing = false;
                _board.clear();
                return true;
            case "start":
                _playing = true;
                return true;
            case "auto":
                autoCommand(command.group(2).toLowerCase());
                return true;
            case "manual":
                manualCommand(command.group(2).toLowerCase());
                return true;
            case "set":
                String tile = command.group(2);
                String piece = command.group(3);
                setTile(tile, piece);
                _playing = false;
                if (setValueOf(piece).equals(BP)) {
                    _board.setTurn(WP);
                } else {
                    _board.setTurn(BP);
                }
                return true;
            case "dump":
                System.out.println(_board);
                return true;
            case "#":
                return true;
            case "seed":
                seedCommand(command.group(2));
                return true;
            case "quit":
                quit();
                return true;
            case "help":
                help();
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    /** Sets a place on the board to a specific PIECE on TILE. */
    private void setTile(String tile, String piece) {
        Piece pie = setValueOf(piece);
        if (tile.length() == 2) {
            int col = colToNum(tile.charAt(0));
            if (col != -1) {
                int row = rowToNum(tile.charAt(1));
                if (row != -1) {
                    _board.set(col, row, pie, _board.getTurn().opposite());
                } else {
                    error("Invalid row");
                }
            } else {
                error("Invalid column");
            }
        } else {
            error("Invalid set command");
        }
    }

    /** Takes in a COL and returns its corresponding column board. */
    private int colToNum(Character col) {
        switch (col) {
        case 'a':
            return 1;
        case 'b':
            return 2;
        case 'c':
            return 3;
        case 'd':
            return 4;
        case 'e':
            return 5;
        case 'f':
            return 6;
        case 'g':
            return 7;
        case 'h':
            return 8;
        default:
            return -1;
        }
    }

    /** Takes in a ROW and returns its corresponding row on board. */
    private int rowToNum(Character row) {
        switch (row) {
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        default:
            return -1;
        }
    }

    /** Set player PLAYER ("white" or "black") to be a manual player. */
    private void manualCommand(String player) {
        try {
            Piece s = Piece.playerValueOf(player);
            _playing = false;
            _players[s.ordinal()] = new HumanPlayer(s, this);
        } catch (IllegalArgumentException excp) {
            error("unknown player: %s", player);
        }
    }

    /** Set player PLAYER ("white" or "black") to be an automated player. */
    private void autoCommand(String player) {
        try {
            Piece s = Piece.playerValueOf(player);
            _playing = false;
            _players[s.ordinal()] = new MachinePlayer(s, this);
        } catch (IllegalArgumentException excp) {
            error("unknown player: %s", player);
        }
    }

    /** Seed random-number generator with SEED (as a long). */
    private void seedCommand(String seed) {
        try {
            _randomSource.setSeed(Long.parseLong(seed));
        } catch (NumberFormatException excp) {
            error("Invalid number: %s", seed);
        }
    }

    /** Play this game, printing any results. */
    public void play() {
        _board = new Board();
        while (true) {
            int playerInd = _board.turn().ordinal();
            Move next;
            if (_playing) {
                if (_board.gameOver()) {
                    announceWinner();
                    _playing = false;
                    continue;
                }
                next = _players[playerInd].makeMove();
                _win = _players[playerInd].getSide();
                assert !_playing || next != null;
            } else {
                getMove();
                next = null;
            }
            if (next != null) {
                assert _board.isLegal(next);
                _board.makeMove(next);
                if (_board.gameOver()) {
                    announceWinner();
                    _playing = false;
                }
            }
        }
    }

    /** Print an announcement of the winner. */
    private void announceWinner() {
        String opp = _board.getTurn().opposite().fullName();
        String me = _board.getTurn().fullName();
        String both = _win.fullName();
        if (_board.piecesContiguous(_board.getTurn().opposite())
                && _board.piecesContiguous(_board.getTurn())) {
            System.out.println(both.substring(0, 1).toUpperCase()
                    + both.substring(1) + " wins.");
        } else if (_board.piecesContiguous(_board.getTurn().opposite())) {
            System.out.println(opp.substring(0, 1).toUpperCase()
                + opp.substring(1) + " wins.");
        } else if (_board.piecesContiguous(_board.getTurn())) {
            System.out.println(me.substring(0, 1).toUpperCase()
                    + me.substring(1) + " wins.");
        }
    }

    /**
     * Return an integer r, 0 <= r < N, randomly chosen from a uniform
     * distribution using the current random source.
     */
    int randInt(int n) {
        return _randomSource.nextInt(n);
    }

    /** Print a help message. */
    void help() {
        System.out.println("start     Start playing from the "
                + "current position.");
        System.out.println("uv-xy     A move from square uv to square xy."
                + "  Here u and v are column designations (a-h) and v and"
                + "y are row designations (1-8):");
        System.out.println("clear     Stop game and return to initial"
                + " position.");
        System.out.println("seed N    Seed the random number with integer N.");
        System.out.println("auto P    P is white or black; makes P into"
                + " an AI. Stops game.");
        System.out.println("manual P  P is white or black; takes moves for P"
                + " from terminal. Stops game.");
        System.out.println("set cr P  Put P ('w', 'b', or empty) into"
                + " square cr. Stops game.");
        System.out.println("dump      Display the board in standard format.");
        System.out.println("quit      End program.");
        System.out.println("help");
        System.out.println("?         This text.");
    }

    /** The official game board. */
    private Board _board;

    /** The _players of this game. */
    private Player[] _players = new Player[2];

    /**
     * A source of random numbers, primed to deliver the same sequence in any
     * Game with the same seed value.
     */
    private Random _randomSource;

    /** Input source. */
    private BufferedReader _input;

    /**
     * True if actually playing (game started and not stopped or finished).
     */
    private boolean _playing;
    /**
     * True if user wants to quit.
     */
    private boolean _quit;
    /** Winner if both pieces are contiguous. */
    private Piece _win;

}
