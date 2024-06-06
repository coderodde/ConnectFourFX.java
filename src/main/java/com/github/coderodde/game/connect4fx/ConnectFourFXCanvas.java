package com.github.coderodde.game.connect4fx;

import com.github.coderodde.game.connect4.ConnectFourBoard;
import static com.github.coderodde.game.connect4.ConnectFourBoard.COLUMNS;
import static com.github.coderodde.game.connect4.ConnectFourBoard.ROWS;
import com.github.coderodde.game.zerosum.PlayerType;
import java.awt.Point;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
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
    
    private int previousAimX = INITIAL_AIM_X;
    private ConnectFourBoard board = new ConnectFourBoard();
    private double cellLength;
    
    public ConnectFourFXCanvas() {
        board = board.makePly(2, PlayerType.MINIMIZING_PLAYER);
        board = board.makePly(4, PlayerType.MAXIMIZING_PLAYER);
        
        setSize();
        paintBackground();
        
        this.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            processMouseMoved(e);
        });
        
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        
        });
    }
    
    public void hit(final int x) {
        
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
        
        if (playerType == null) {
            return;
        }
        
        final double topLeftX = cellLength * x;
        final double topLeftY = cellLength * y;
        final Color color = 
                playerType == PlayerType.MINIMIZING_PLAYER ?
                            HUMAN_PLAYER_CELL_COLOR :
                            AI_PLAYER_CELL_COLOR;
                            
        this.getGraphicsContext2D().setFill(color);
        this.getGraphicsContext2D()
            .fillOval(topLeftX, 
                      topLeftY, 
                      cellLength, 
                      cellLength);
    }
    
    private void paintCellSelection(final int cellX, final int cellY) {
        final double topLeftX = cellLength * cellX;
        final double topLeftY = cellLength * cellY;
        final double innerWidth = cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA;
        
        this.getGraphicsContext2D().setFill(AIM_COLOR);
        this.getGraphicsContext2D()
                .fillOval(
                        topLeftX, 
                        topLeftY,
                        cellLength,
                        cellLength);
        
        this.getGraphicsContext2D().setFill(Color.WHITE);
        this.getGraphicsContext2D()
                .fillOval(topLeftX + RADIUS_SUBSTRACTION_DELTA, 
                          topLeftY + RADIUS_SUBSTRACTION_DELTA, 
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
    
    private Point convertMouseLocationToCellPoint(final MouseEvent mouseEvent) {
        final double x = mouseEvent.getSceneX();
        final double y = mouseEvent.getSceneY();
        
        final int cellX = (int)(x / cellLength);
        final int cellY = (int)(y / cellLength);
        
        final double cellCenterPointX = cellLength * cellX + cellLength / 2.0;
        final double cellCenterPointY = cellLength * cellY + cellLength / 2.0;
        
        final double maximumRadius = cellLength / 2.0 
                                   - RADIUS_SUBSTRACTION_DELTA;
        
        final double mouseCursorDistanceFromCellCenter = 
                getMouseCursorDistanceFromCellCenter(
                        cellCenterPointX, 
                        cellCenterPointY, 
                        x, 
                        y);
        
        if (mouseCursorDistanceFromCellCenter > maximumRadius) {
            return null;
        }
        
        return new Point(cellX, cellY);
    }
    
    private static double getMouseCursorDistanceFromCellCenter(
            final double cellCenterX,
            final double cellCenterY,
            final double mouseCursorX,
            final double mouseCursorY) {
        
        final double dx = cellCenterX - mouseCursorX;
        final double dy = cellCenterY - mouseCursorY;
        
        return Math.sqrt(dx * dx + dy * dy);
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
        
        paintAllCellsToWhite();
    }
    
    private void paintAllCellsToWhite() {
        this.getGraphicsContext2D().setFill(Color.WHITE);
        
        for (int y = 0; y < ConnectFourBoard.ROWS; y++) {
            for (int x = 0; x < ConnectFourBoard.COLUMNS; x++) {
                paintCellToWhite(x, y);
            }
        }
    }
    
    private void paintCellToWhite(final int x, final int y) {
        this.getGraphicsContext2D()
            .fillOval(x * cellLength + RADIUS_SUBSTRACTION_DELTA,
                      y * cellLength + RADIUS_SUBSTRACTION_DELTA,
                      cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA,
                      cellLength - 2.0 * RADIUS_SUBSTRACTION_DELTA);
    }
}
