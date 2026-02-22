package controller;

import java.awt.Color;
import java.util.List;

import javax.swing.JComponent;

import draw.DrawingModel;
import geometrija.Circle;
import geometrija.Donut;
import geometrija.HexagonAdapter;
import geometrija.Line;
import geometrija.Point;
import geometrija.Rectangle;
import geometrija.Shape;

public class AddShape implements Command{
	
	private final DrawingModel model;
	private final Shape shape;
	private final JComponent panel;
	private int addedIndex=-1;
	
	public AddShape(DrawingModel model, Shape shape, JComponent panel) {
		this.model=model;
		this.panel= panel;
		this.shape=shape;
	} 
	
	@Override
	public void execute() {
		addedIndex = model.size();  
        model.addShape(shape);      
        if (panel != null) {
            panel.repaint();
        }
	}
	
	@Override
	public void unexecute() {
		   if (addedIndex >= 0 && addedIndex < model.size()) {
	            model.removeShape(addedIndex);  
	        }
	        if (panel != null) {
	            panel.repaint();
	        }
	}
	
	@Override
	public String toLog() {
	    return "ADD|" + shapeToLogString(shape);
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
	        if (s instanceof Line) {
	            Line l = (Line) s;
	            return "LINE|" +
	                   l.getStartPoint().getX() + "|" +
	                   l.getStartPoint().getY() + "|" +
	                   l.getEndPoint().getX() + "|" +
	                   l.getEndPoint().getY() + "|" +
	                   colorToString(l.getColor());
	        }

	        throw new IllegalArgumentException("Unknown shape type");
	    }
	    private String colorToString(Color c) {
	        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	    }	  
	
}
