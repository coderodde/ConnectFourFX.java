package com.github.coderodde.game.connect4fx;

import com.github.coderodde.game.connect4.ConnectFourBoard;
import static com.github.coderodde.game.connect4.ConnectFourBoard.COLUMNS;
import static com.github.coderodde.game.connect4.ConnectFourBoard.ROWS;
import com.github.coderodde.game.connect4.ConnectFourHeuristicFunction;
import com.github.coderodde.game.zerosum.PlayerType;
import com.github.coderodde.game.zerosum.SearchEngine;
import com.github.coderodde.game.zerosum.impl.AlphaBetaPruningSearchEngine;
import java.awt.Point;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

/**
 * This class implements the canvas for drawing the game board.
 * 
 * @version 1.0.0 (Jun 6, 2024)
 * @since 1.0.0 (Jun 6, 2024)
 */
public final class ConnectFourFXCanvas extends Canvas {
    
    private static final Color BACKGROUND_COLOR = Color.valueOf("#295c9e");
    private static final Color AIM_COLOR        = Color.valueOf("#167a0f");
    
    private static final Color HUMAN_PLAYER_CELL_COLOR = 
            Color.valueOf("#bfa730");
    
    private static final Color AI_PLAYER_CELL_COLOR = 
            Color.valueOf("#b33729");
    
    private static final double CELL_LENGTH_SUBSTRACT = 10.0;
    private static final double RADIUS_SUBSTRACTION_DELTA = 10.0;
    private static final int CELL_Y_NOT_FOUND = -1;
    private static final int INITIAL_AIM_X = 3;
    private static final int SEARCH_DEPTH = 8;
    
    private final SearchEngine<ConnectFourBoard> engine = 
            new AlphaBetaPruningSearchEngine<>(
                    new ConnectFourHeuristicFunction());
    
    private int previousAimX = INITIAL_AIM_X;
    private ConnectFourBoard board = new ConnectFourBoard();
    private double cellLength;
    
    public ConnectFourFXCanvas() {
        board = board.makePly(2, PlayerType.MINIMIZING_PLAYER);
        board = board.makePly(4, PlayerType.MAXIMIZING_PLAYER);
        
        setSize();
        paintBackground();
        
        this.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            processMouseMoved(event);
        });
        
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            processMouseClicked(event);
        });
    }
    
    public void hit(final int x) {
        final int y = getEmptyCellYForX(x);
        
        if (y == CELL_Y_NOT_FOUND) {
            // The column at X-index of x is full:
            return;
        }
        
        board = board.makePly(x, PlayerType.MINIMIZING_PLAYER);
        
        paintBackground();
        paintBoard();
        
        if (board.isTerminal()) {
            reportEndResult();
            return;
        }
        
        board = engine.search(board, SEARCH_DEPTH);
        
        if (board.isTerminal()) {
            reportEndResult();
            return;
        }
        
        paintBackground();
        paintBoard();
    }
    
    private static Alert getEndResultReportAlert(final String contentText) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        return alert;
    }
    
    private void processEndOfGameOptional(final Optional<ButtonType> optional) {
        if (optional.isPresent() && optional.get().equals(ButtonType.YES)) {
            board = new ConnectFourBoard();
        } else {
            Platform.exit();
            System.out.println("After Platform.exit();");
            System.exit(0);
        }
    }
    
    private void reportEndResult() {
        if (board.isTie()) {
            
            final Optional<ButtonType> optional =
                    getEndResultReportAlert(
                            "It's a tie! Do you want to play again?")
                            .showAndWait();
            
            processEndOfGameOptional(optional);
            
        } else if (board.isWinningFor(PlayerType.MINIMIZING_PLAYER)) {
            
            colorWinningPattern(board.getWinningPattern(), 
                                PlayerType.MINIMIZING_PLAYER);
            
            final Optional<ButtonType> optional = 
                    getEndResultReportAlert(
                            "You won! Do you want to play again?")
                            .showAndWait();
            
            processEndOfGameOptional(optional);
            
        } else if (board.isWinningFor(PlayerType.MAXIMIZING_PLAYER)) {
            
            colorWinningPattern(board.getWinningPattern(), 
                                PlayerType.MINIMIZING_PLAYER);
            
            final Optional<ButtonType> optional = 
                    getEndResultReportAlert(
                            "You lost! Do you want to play again?")
                            .showAndWait();
            
            processEndOfGameOptional(optional);
        }
    }
    
    private void colorWinningPattern(final List<Point> winningPattern,
                                     final PlayerType playerType) {
        final Color color;
        
        switch (playerType) {
            case MINIMIZING_PLAYER:
                color = HUMAN_PLAYER_CELL_COLOR;
                break;
                
            case MAXIMIZING_PLAYER:
                color = AI_PLAYER_CELL_COLOR;
                break;
                
            default:
                throw new IllegalStateException(
                        "Unknown PlayerType: " + playerType);
        }
    }
    
    private void processMouseClicked(final MouseEvent mouseEvent) {
        final double mouseX = mouseEvent.getSceneX();
        final int cellX = (int)(mouseX / cellLength);
        
        hit(cellX);
    }
    
    private void processMouseMoved(final MouseEvent mouseEvent) {
        final double mouseX = mouseEvent.getSceneX();
        final int cellX = (int)(mouseX / cellLength);
        final int emptyCellY = getEmptyCellYForX(cellX);
        
        if (cellX == previousAimX) {
            // Nothing changed.
            return;
        }
        
        previousAimX = cellX;
        
        if (emptyCellY == CELL_Y_NOT_FOUND) {
            return;
        }
        
        paintBackground();
        paintBoard();
        paintCellSelection(cellX, emptyCellY);
    }
    
    private void paintBoard() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                final PlayerType playerType = board.get(x, y);
                
                paintCell(playerType, x, y);
            }
        }
    }
    
    private void paintCell(final PlayerType playerType, 
                           final int x,
                           final int y) {
        
        final double topLeftX = cellLength * x + RADIUS_SUBSTRACTION_DELTA;
        final double topLeftY = cellLength * y + RADIUS_SUBSTRACTION_DELTA;
        final double innerWidth = cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA;
        final Color color;
        
        if (playerType == null) {
            color = Color.WHITE;
        } else if (playerType == PlayerType.MINIMIZING_PLAYER) {
            color = HUMAN_PLAYER_CELL_COLOR;
        } else if (playerType == PlayerType.MAXIMIZING_PLAYER) {
            color = AI_PLAYER_CELL_COLOR;
        } else {
            throw new IllegalStateException(
                    "Unknown player type: " + playerType);
        }
        
        this.getGraphicsContext2D().setFill(color);
        this.getGraphicsContext2D()
            .fillOval(topLeftX, 
                      topLeftY, 
                      innerWidth, 
                      innerWidth);
    }
    
    private void paintCellSelection(final int cellX, final int cellY) {
        final double topLeftX = cellLength * cellX + RADIUS_SUBSTRACTION_DELTA;
        final double topLeftY = cellLength * cellY + RADIUS_SUBSTRACTION_DELTA;
        final double innerWidth = cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA;
        
        this.getGraphicsContext2D().setFill(AIM_COLOR);
        this.getGraphicsContext2D()
                .fillOval(
                        topLeftX, 
                        topLeftY,
                        innerWidth,
                        innerWidth);
    }
    
    private int getEmptyCellYForX(final int cellX) {
        for (int y = ROWS - 1; y >= 0; y--) {
            if (board.get(cellX, y) == null) {
                return y;
            }
        }
        
        return CELL_Y_NOT_FOUND;
    }
    
    private void setSize() {
        final Rectangle2D primaryScreenBounds =
                Screen.getPrimary().getVisualBounds();
        
        final double verticalLength =  
                primaryScreenBounds.getHeight() / ConnectFourBoard.ROWS;
        
        final double horizontalLength = 
                primaryScreenBounds.getWidth() / ConnectFourBoard.COLUMNS;
        
        final double selectedLength = Math.min(verticalLength,
                                               horizontalLength) 
                                    - CELL_LENGTH_SUBSTRACT;
        
        this.cellLength = selectedLength;
        
        this.setWidth(ConnectFourBoard.COLUMNS * selectedLength);
        this.setHeight(ConnectFourBoard.ROWS * selectedLength);
    }
    
    private void paintBackground() {
        final double width = this.getWidth();
        final double height = this.getHeight();
        
        this.getGraphicsContext2D().setFill(BACKGROUND_COLOR);
        this.getGraphicsContext2D().fillRect(0.0, 0.0, width, height);
    }
}
