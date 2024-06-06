package com.github.coderodde.game.connect4fx;

import com.github.coderodde.game.connect4.ConnectFourBoard;
import java.awt.Point;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

/**
 *
 * @author PotilasKone
 */
public final class ConnectFourFXCanvas extends Canvas {
    
    private static final Color BACKGROUND_COLOR = Color.valueOf("#295c9e");
    private static final double CELL_LENGTH_SUBSTRACT = 10.0;
    private static final double RADIUS_SUBSTRACTION_DELTA = 10.0;
    
    private final Point previousCellPoint = new Point(-1, -1);    
    private ConnectFourBoard board;
    private double cellLength;
    
    public ConnectFourFXCanvas() {
        setSize();
        paintBackground();
        
        this.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            processMouseMoved(e);
        });
        
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        
        });
        
//        final EventHandler<MouseEvent> mouseHandler = 
//                new ConnectFourFXCanvasMouseHandler();
        
//        this.setOnMouseClicked(mouseHandler);
//        this.setOnMouseMoved(mouseHandler);
    }
    
    public void hit(final int x) {
        
    }
    
    private void processMouseMoved(final MouseEvent mouseEvent) {
        final Point point = convertMouseLocationToCellPoint(mouseEvent);
        
        if (point == null) {
            
        }
        
        
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
