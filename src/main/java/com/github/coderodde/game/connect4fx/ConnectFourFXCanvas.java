package com.github.coderodde.game.connect4fx;

import com.github.coderodde.game.connect4.ConnectFourBoard;
import static com.github.coderodde.game.connect4.ConnectFourBoard.COLUMNS;
import static com.github.coderodde.game.connect4.ConnectFourBoard.ROWS;
import com.github.coderodde.game.connect4.ConnectFourHeuristicFunction;
import com.github.coderodde.game.zerosum.PlayerType;
import com.github.coderodde.game.zerosum.impl.ConnectFourAlphaBetaPruningSearchEngine;
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
    
    private static final Color WINNING_PATTERN_COLOR = Color.BLACK;
    
    private static final double CELL_LENGTH_SUBSTRACT = 10.0;
    private static final double RADIUS_SUBSTRACTION_DELTA = 10.0;
    private static final int CELL_Y_NOT_FOUND = -1;
    private static final int INITIAL_AIM_X = 3;
    private static final int SEARCH_DEPTH = 9;
    
    private final ConnectFourAlphaBetaPruningSearchEngine engine =
                    new ConnectFourAlphaBetaPruningSearchEngine(
                            new ConnectFourHeuristicFunction());
    
//            new ConnectFourAlphaBetaPruningSearchEngine(
//                    new ConnectFourHeuristicFunction());
    
    private int previousAimX = INITIAL_AIM_X;
    private ConnectFourBoard board = new ConnectFourBoard();
    private double cellLength;
    
    public ConnectFourFXCanvas() {
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
        int y = getEmptyCellYForX(x);
        
        if (y == CELL_Y_NOT_FOUND) {
            // The column at X-index of x is full:
            return;
        }
        
        previousAimX = x;
        board.makePly(x, PlayerType.MINIMIZING_PLAYER);
        
        paintBackground();
        paintBoard();
        
        if (board.isTerminal()) {
            paintBackground();
            paintBoard();
            reportEndResult();
            return;
        }
        
        board = engine.search(board, SEARCH_DEPTH);
        
        if (board.isTerminal()) {
            paintBackground();
            paintBoard();
            reportEndResult();
            return;
        }
        
        paintBackground();
        paintBoard();
        
        y = getEmptyCellYForX(x);
        
        if (y != CELL_Y_NOT_FOUND) {
            paintCell(AIM_COLOR, x, y);
        }
    }
    
    private static Alert getEndResultReportAlert(final String contentText) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
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
            
            colorWinningPattern(PlayerType.MINIMIZING_PLAYER,
                                board.getWinningPattern());
            
            final Optional<ButtonType> optional = 
                    getEndResultReportAlert(
                            "You won! Do you want to play again?")
                            .showAndWait();
            
            processEndOfGameOptional(optional);
            
        } else if (board.isWinningFor(PlayerType.MAXIMIZING_PLAYER)) {
            
            colorWinningPattern(PlayerType.MAXIMIZING_PLAYER,
                                board.getWinningPattern());
            
            final Optional<ButtonType> optional = 
                    getEndResultReportAlert(
                            "You lost! Do you want to play again?")
                            .showAndWait();
            
            processEndOfGameOptional(optional);
        }
    }
    
    private void colorWinningPattern(final PlayerType playerType,
                                     final List<Point> winningPattern) {
        
        for (final Point point : winningPattern) {
            paintCell(WINNING_PATTERN_COLOR, point.x, point.y);
            paintInnerCell(playerType, point.x, point.y);
        }
    }
    
    private void paintInnerCell(final PlayerType playerType,
                                final int x, 
                                final int y) {
        final double topLeftX = 
                cellLength * x + 2.0 * RADIUS_SUBSTRACTION_DELTA;
        
        final double topLeftY = 
                cellLength * y + 2.0 * RADIUS_SUBSTRACTION_DELTA;
        
        final double diameter = cellLength - 4.0 * RADIUS_SUBSTRACTION_DELTA;
        
        this.getGraphicsContext2D().setFill(getColor(playerType));
        this.getGraphicsContext2D().fillOval(topLeftX,
                                             topLeftY,
                                             diameter, 
                                             diameter);
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
                final Color color = getColor(playerType);
                
                paintCell(color, x, y);
            }
        }
    }
    
    private static Color getColor(final PlayerType playerType) {
        if (playerType == null) {
            return Color.WHITE;
        } else switch (playerType) {
            case MAXIMIZING_PLAYER -> {
                return AI_PLAYER_CELL_COLOR;
            }
                
            case MINIMIZING_PLAYER -> {
                return HUMAN_PLAYER_CELL_COLOR;
            }
                
            default -> throw new IllegalStateException(
                        "Unknown PlayerType: " + playerType);
        }
    }
    
    private void paintCell(final Color color, 
                           final int x,
                           final int y) {
        
        final double topLeftX = cellLength * x + RADIUS_SUBSTRACTION_DELTA;
        final double topLeftY = cellLength * y + RADIUS_SUBSTRACTION_DELTA;
        final double innerWidth = cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA;
        
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
