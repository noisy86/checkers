import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Checkers extends JPanel {

    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers");
        Checkers content = new Checkers();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2,
                (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    private JButton newGameButton;  // Button for starting a new game.

    private JLabel message;  // Label for displaying messages to the user.

    public Checkers() {

        setLayout(null);  // I will do the layout myself.
        setPreferredSize( new Dimension(350,250) );
//        Create board
        Board board = new Board();
        add(board);
        add(newGameButton);
        add(message);

      /* Set the position and size of each component by calling
       its setBounds() method. */

        board.setBounds(20,20,164,164); // Note:  size MUST be 164-by-164 !
        newGameButton.setBounds(210, 60, 120, 30);
        message.setBounds(0, 200, 350, 30);

    } // end constructor

    private static class CheckersMove {
        int fromRow, fromCol;  // Position of piece to be moved.
        int toRow, toCol;      // Square it is to move to.
        CheckersMove(int r1, int c1, int r2, int c2) {
            // Constructor.  Just set the values of the instance variables.
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
        }
        boolean isJump() {
            // Test whether this move is a jump.  It is assumed that
            // the move is legal.  In a jump, the piece moves two
            // rows.  (In a regular move, it only moves one row.)
            return (fromRow - toRow == 2 || fromRow - toRow == -2);
        }
    }  // end class CheckersMove.

    private class Board extends JPanel implements ActionListener, MouseListener {


        CheckersData board;  // The data for the checkers board is kept here.
        //    This board is also responsible for generating
        //    lists of legal moves.

        boolean gameInProgress; // Is a game currently in progress?

        /* The next three variables are valid only when the game is in progress. */

        int currentPlayer;      // Whose turn is it now?  The possible values
        //    are CheckersData.RED and CheckersData.BLACK.

        int selectedRow, selectedCol;

        CheckersMove[] legalMoves;  // An array containing the legal moves for the
        //   current player.
        Board() {
            setBackground(Color.BLACK);
            addMouseListener(this);
            newGameButton = new JButton("New Game");
            newGameButton.addActionListener(this);
            message = new JLabel("",JLabel.CENTER);
            message.setFont(new  Font("Serif", Font.BOLD, 14));
            message.setForeground(Color.green);
            board = new CheckersData();
            doNewGame();
        }


        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == newGameButton)
                doNewGame();
        }


//        Start a new game
        void doNewGame() {
            if (gameInProgress == true) {
                // This should not be possible, but it doesn't hurt to check.
                message.setText("Finish the current game first!");
                return;
            }
            board.setUpGame();   // Set up the pieces.
            currentPlayer = CheckersData.RED;   // RED moves first.
            legalMoves = board.getOkmoves(CheckersData.RED);  // Get RED's legal moves.
            selectedRow = -1;   // RED has not yet selected a piece to move.
            message.setText("Red:  Make your move.");
            gameInProgress = true;
            newGameButton.setEnabled(false);
            repaint();
        }


        void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            gameInProgress = false;
        }



        void doClickSquare(int row, int col) {

         /* If the player clicked on one of the pieces that the player
          can move, mark this row and col as selected and return.  (This
          might change a previous selection.)  Reset the message, in
          case it was previously displaying an error message. */

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
                    selectedRow = row;
                    selectedCol = col;
                    if (currentPlayer == CheckersData.RED)
                        message.setText("RED:  Make your move.");
                    else
                        message.setText("BLACK:  Make your move.");
                    repaint();
                    return;
                }

         /* If no piece has been selected to be moved, the user must first
          select a piece.  Show an error message and return. */

            if (selectedRow < 0) {
                message.setText("Click the piece you want to move.");
                return;
            }

         /* If the user clicked on a square where the selected piece can be
          legally moved, then make the move and return. */

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
                        && legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
                    doMakeMove(legalMoves[i]);
                    return;
                }

         /* If we get to this point, there is a piece selected, and the square where
          the user just clicked is not one where that piece can be legally moved.
          Show an error message. */

            message.setText("Click the square you want to move to.");

        }  // end doClickSquare()

        void doMakeMove(CheckersMove move) {

            board.makeMove(move);

         /* If the move was a jump, it's possible that the player has another
          jump.  Check for legal jumps starting from the square that the player
          just moved to.  If there are any, the player must jump.  The same
          player continues moving.
          */

            if (move.isJump()) {
                legalMoves = board.getLegalJumpsFrom(currentPlayer,move.toRow,move.toCol);
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.RED)
                        message.setText("RED:  You must continue jumping.");
                    else
                        message.setText("BLACK:  You must continue jumping.");
                    selectedRow = move.toRow;  // Since only one piece can be moved, select it.
                    selectedCol = move.toCol;
                    repaint();
                    return;
                }
            }

         /* The current player's turn is ended, so change to the other player.
          Get that player's legal moves.  If the player has no legal moves,
          then the game ends. */

            if (currentPlayer == CheckersData.RED) {
                currentPlayer = CheckersData.BLACK;
                legalMoves = board.getOkmoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("BLACK has no moves.  RED wins.");
                else if (legalMoves[0].isJump())
                    message.setText("BLACK:  Make your move.  You must jump.");
                else
                    message.setText("BLACK:  Make your move.");
            }
            else {
                currentPlayer = CheckersData.RED;
                legalMoves = board.getOkmoves(currentPlayer);
                if (legalMoves == null)
                    gameOver("RED has no moves.  BLACK wins.");
                else if (legalMoves[0].isJump())
                    message.setText("RED:  Make your move.  You must jump.");
                else
                    message.setText("RED:  Make your move.");
            }

         /* Set selectedRow = -1 to record that the player has not yet selected
          a piece to move. */

            selectedRow = -1;

         /* As a courtesy to the user, if all legal moves use the same piece, then
          select that piece automatically so the user won't have to click on it
          to select it. */

            if (legalMoves != null) {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow
                            || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }

            /* Make sure the board is redrawn in its new state. */

            repaint();

        }  // end doMakeMove();


        public void paintComponent(Graphics g) {

            /* Draw a two-pixel black border around the edges of the canvas. */

            g.setColor(Color.black);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);

            /* Draw the squares of the checkerboard and the checkers. */

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.GRAY);
                    g.fillRect(2 + col*20, 2 + row*20, 20, 20);
                    switch (board.pieceAt(row,col)) {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                    }
                }
            }


            if (gameInProgress) {
                /* First, draw a 2-pixel cyan border around the pieces that can be moved. */
                g.setColor(Color.cyan);
                for (int i = 0; i < legalMoves.length; i++) {
                    g.drawRect(2 + legalMoves[i].fromCol*20, 2 + legalMoves[i].fromRow*20, 19, 19);
                    g.drawRect(3 + legalMoves[i].fromCol*20, 3 + legalMoves[i].fromRow*20, 17, 17);
                }
               /* draw a 2-pixel white border around that piece and draw green borders
                around each square that that piece can be moved to. */
                if (selectedRow >= 0) {
                    g.setColor(Color.white);
                    g.drawRect(2 + selectedCol*20, 2 + selectedRow*20, 19, 19);
                    g.drawRect(3 + selectedCol*20, 3 + selectedRow*20, 17, 17);
                    g.setColor(Color.green);
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
                            g.drawRect(2 + legalMoves[i].toCol*20, 2 + legalMoves[i].toRow*20, 19, 19);
                            g.drawRect(3 + legalMoves[i].toCol*20, 3 + legalMoves[i].toRow*20, 17, 17);
                        }
                    }
                }
            }

        }  // end paintComponent()


        public void mousePressed(MouseEvent evt) {
            if (gameInProgress == false)
                message.setText("Click \"New Game\" to start a new game.");
            else {
                int col = (evt.getX() - 2) / 20;
                int row = (evt.getY() - 2) / 20;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
            }
        }


        public void mouseReleased(MouseEvent evt) { }
        public void mouseClicked(MouseEvent evt) { }
        public void mouseEntered(MouseEvent evt) { }
        public void mouseExited(MouseEvent evt) { }


    }  // end class Board



    private static class CheckersData {



        static final int
                EMPTY = 0,
                RED = 1,
                RED_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;


        int[][] board;  // board[r][c] is the contents of row r, column c.



        CheckersData() {
            board = new int[8][8];
            setUpGame();
        }



        void setUpGame() {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 ) {
                        if (row < 3)
                            board[row][col] = BLACK;
                        else if (row > 4)
                            board[row][col] = RED;
                        else
                            board[row][col] = EMPTY;
                    }
                    else {
                        board[row][col] = EMPTY;
                    }
                }
            }
        }  // end setUpGame()

        int pieceAt(int row, int col) {
            return board[row][col];
        }

        void makeMove(CheckersMove move) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }


        void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = EMPTY;
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
                // The move is a jump.  Remove the jumped piece from the board.
                int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
                int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
                board[jumpRow][jumpCol] = EMPTY;
            }
            if (toRow == 0 && board[toRow][toCol] == RED)
                board[toRow][toCol] = RED_KING;
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_KING;
        }


        CheckersMove[] getOkmoves(int player) {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;  // The constant representing a King belonging to player.
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();  // Moves will be stored in this list.



            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                            moves.add(new CheckersMove(row, col, row+2, col+2));
                        if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                            moves.add(new CheckersMove(row, col, row-2, col+2));
                        if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                            moves.add(new CheckersMove(row, col, row+2, col-2));
                        if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                            moves.add(new CheckersMove(row, col, row-2, col-2));
                    }
                }
            }



            if (moves.size() == 0) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == player || board[row][col] == playerKing) {
                            if (canMove(player,row,col,row+1,col+1))
                                moves.add(new CheckersMove(row,col,row+1,col+1));
                            if (canMove(player,row,col,row-1,col+1))
                                moves.add(new CheckersMove(row,col,row-1,col+1));
                            if (canMove(player,row,col,row+1,col-1))
                                moves.add(new CheckersMove(row,col,row+1,col-1));
                            if (canMove(player,row,col,row-1,col-1))
                                moves.add(new CheckersMove(row,col,row-1,col-1));
                        }
                    }
                }
            }

         /* If no legal moves have been found, return null. */

            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }

        }  // end getLegalMoves


        CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
            if (player != RED && player != BLACK)
                return null;
            int playerKing;  // The constant representing a King belonging to player.
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;
            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();  // The legal jumps will be stored in this list.
            if (board[row][col] == player || board[row][col] == playerKing) {
                if (canJump(player, row, col, row+1, col+1, row+2, col+2))
                    moves.add(new CheckersMove(row, col, row+2, col+2));
                if (canJump(player, row, col, row-1, col+1, row-2, col+2))
                    moves.add(new CheckersMove(row, col, row-2, col+2));
                if (canJump(player, row, col, row+1, col-1, row+2, col-2))
                    moves.add(new CheckersMove(row, col, row+2, col-2));
                if (canJump(player, row, col, row-1, col-1, row-2, col-2))
                    moves.add(new CheckersMove(row, col, row-2, col-2));
            }
            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }  // end getLegalMovesFrom()


        private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {

            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;  // (r3,c3) is off the board.

            if (board[r3][c3] != EMPTY)
                return false;  // (r3,c3) already contains a piece.

            if (player == RED) {
                if (board[r1][c1] == RED && r3 > r1)
                    return false;  // Regular red piece can only move up.
                if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
                    return false;  // There is no black piece to jump.
                return true;  // The jump is legal.
            }
            else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false;  // Regular black piece can only move downn.
                if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
                    return false;  // There is no red piece to jump.
                return true;  // The jump is legal.
            }

        }

        private boolean canMove(int player, int r1, int c1, int r2, int c2) {

            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false;  // (r2,c2) is off the board.

            if (board[r2][c2] != EMPTY)
                return false;  // (r2,c2) already contains a piece.

            if (player == RED) {
                if (board[r1][c1] == RED && r2 > r1)
                    return false;  // Regular red piece can only move down.
                return true;  // The move is legal.
            }
            else {
                if (board[r1][c1] == BLACK && r2 < r1)
                    return false;  // Regular black piece can only move up.
                return true;  // The move is legal.
            }

        }


    }


}
