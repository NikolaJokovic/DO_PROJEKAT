package draw;

import java.util.ArrayList;
import java.util.List;
import controller.ModelObserver;
import geometrija.Shape;

public class DrawingModel {

	 private ArrayList<Shape> shapes = new ArrayList<>();
	 private List<ModelObserver> observers = new ArrayList<>();
	    
	 public void addObserver(ModelObserver observer) {
	        if (!observers.contains(observer)) {
	            observers.add(observer);
	        }
	 	}
	 
	 public void removeObserver(ModelObserver observer) {
	        observers.remove(observer);
	    }
	 
	 public void notifyObservers() {
	        for (ModelObserver observer : observers) {
	            observer.update();
	        }
	    }   
	 
	 public void addShape(Shape shape) {
	        shapes.add(shape);
	        notifyObservers();
	    }
	 public void removeShape(int index) {
	        if (index >= 0 && index < shapes.size()) {
	            shapes.remove(index);
	            notifyObservers();
	        }
	    }
	 public void updateShape(int index, Shape newShape) {
	        if (index >= 0 && index < shapes.size()) {
	            shapes.set(index, newShape);
	            notifyObservers();
	        }
	    }
	 public ArrayList<Shape> getShapes() {
	        return shapes;
	    }
	    
	    public void setShapes(ArrayList<Shape> shapes) {
	        this.shapes = shapes;
	        notifyObservers();
	    }
	    
	    public Shape getShape(int index) {
	        if (index >= 0 && index < shapes.size()) {
	            return shapes.get(index);
	        }
	        return null;
	    }
	    
	    public int size() {
	        return shapes.size();
	    }
	    

	    public int getSelectedCount() {
	        int count = 0;
	        for (Shape s : shapes) {
	            if (s.isSelected()) {
	                count++;
	            }
	        }
	        return count;
	    }
	    
	    public int getFirstSelectedIndex() {
	        for (int i = 0; i < shapes.size(); i++) {
	            if (shapes.get(i).isSelected()) {
	                return i;
	            }
	        }
	        return -1;
	    }
	    
	    public int getLastSelectedIndex() {
	        for (int i = shapes.size() - 1; i >= 0; i--) {
	            if (shapes.get(i).isSelected()) {
	                return i;
	            }
	        }
	        return -1;
	    }
	    
	    public void deselectAll() {
	        for (Shape s : shapes) {
	            s.setSelected(false);
	        }
	        notifyObservers();
	    }
	    
	    public ArrayList<Integer> getAllSelectedIndices() {
	        ArrayList<Integer> indices = new ArrayList<>();
	        for (int i = 0; i < shapes.size(); i++) {
	            if (shapes.get(i).isSelected()) {
	                indices.add(i);
	            }
	        }
	        return indices;
	    }
}
