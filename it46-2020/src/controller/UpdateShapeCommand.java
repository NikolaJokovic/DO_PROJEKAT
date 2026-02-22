package controller;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JComponent;

import draw.DrawingModel;
import draw.PnlDraw;
import geometrija.Circle;
import geometrija.Donut;
import geometrija.HexagonAdapter;
import geometrija.Point;
import geometrija.Rectangle;
import geometrija.Shape;

public class UpdateShapeCommand implements Command {

	private final DrawingModel model;
    private final int index;
    private final JComponent panel;

    private final Shape oldState;
    private final Shape newState; 

    public UpdateShapeCommand(DrawingModel model, int index, Shape oldState, Shape newState, JComponent panel) {
        this.model=model;
        this.index = index;
        this.oldState = oldState;
        this.newState = newState;
        this.panel = panel;
    }
    

    @Override
    public void execute() {
    	
            model.updateShape(index, newState);  
        
        if (panel != null) {
            panel.repaint();
        }
    }

    @Override
    public void unexecute() {
    	
            model.updateShape(index, oldState);  
        
        if (panel != null) {
            panel.repaint();
        }
    }
    @Override
	public String toLog() {
	    return "EDITED|" + shapeToLogString(oldState)+ "|TO|"+ shapeToLogString(newState);
	   
	}
	
	private String shapeToLogString(Shape s) {

	        if (s instanceof Point ) {
	        	Point p= (Point)s;
	            return "POINT|" + p.getX() + "|" + p.getY() + "|" + colorToString(p.getColor());
	        }
	        
	        if (s instanceof Donut) {
	        	Donut d=(Donut) s;
	        	
	            return "DONUT|" +
	                   d.getCenter().getX() + "|" +
	                   d.getCenter().getY() + "|" +
	                   d.getRadius() + "|" +
	                   d.getInnerRadius() + "|" +
	                   colorToString(d.getColor()) + "|" +
	                   colorToString(d.getInnerColor());
	        }

	        if (s instanceof Circle ) {
	            Circle c=(Circle) s;
	        	
	        	return "CIRCLE|" +
	                   c.getCenter().getX() + "|" +
	                   c.getCenter().getY() + "|" +
	                   c.getRadius() + "|" +
	                   colorToString(c.getColor()) + "|" +
	                   colorToString(c.getInnerColor());
	        }


	        if (s instanceof Rectangle) {
	        	Rectangle r= (Rectangle) s;
	        	
	            return "RECTANGLE|" +
	                   r.getUpperLeftPoint().getX() + "|" +
	                   r.getUpperLeftPoint().getY() + "|" +
	                   r.getWidth() + "|" +
	                   r.getHeight() + "|" +
	                   colorToString(r.getColor()) + "|" +
	                   colorToString(r.getInnerColor());
	        }

	        if (s instanceof HexagonAdapter) {
	        	HexagonAdapter h = (HexagonAdapter) s;
	        	
	            return "HEXAGON|" +
	                   h.getHexagon().getX() + "|" +
	                   h.getHexagon().getY() + "|" +
	                   h.getHexagon().getR() + "|" +
	                   colorToString(h.getColor()) + "|" +
	                   colorToString(h.getInnerColor());
	        }

	        throw new IllegalArgumentException("Unknown shape type");
	    }
	    private String colorToString(Color c) {
	        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	    }	 
}
