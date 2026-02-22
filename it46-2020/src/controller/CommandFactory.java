package controller;

import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

import draw.DrawingModel;
import draw.PnlDraw;
import geometrija.Shape;
import geometrija.Point;
import geometrija.Line;
import geometrija.Circle;
import geometrija.Donut;
import geometrija.Rectangle;
import geometrija.HexagonAdapter;

public class CommandFactory {

    private CommandFactory() {}


    public static Command fromLog(String line, List<Shape> shapes,DrawingModel model, JComponent panel) {
        if (line == null) throw new IllegalArgumentException("Log line is null");
        line = line.trim();
        if (line.isEmpty()) throw new IllegalArgumentException("Log line is empty");

        String[] t = line.split("\\|");
        String kind = t[0].trim().toUpperCase();

        switch (kind) {
            case "ADD": {
                Shape s = parseShape(t, 1);
                return new AddCmd(shapes, s, panel);
            }

            case "REMOVED": {
                if (t.length < 3) throw new IllegalArgumentException("Bad REMOVED line: " + line);
                int index = parseInt(t[1], "REMOVED.index");
                Shape expected = parseShape(t, 2);
                return new RemoveCmd(shapes, index, expected, panel);
            }
            case "EDITED":{
            	if (t.length < 2) throw new IllegalArgumentException("Bad EDITED line: " + line);
            	Shape toEdit = parseShape(t,1);
            	Shape edited= parseShape(t,8);
            	
            	int index = parseInt(t[2], "EDITED.index");
            	return new EditCmd(index, toEdit, edited, panel,model);
            }

            case "MOVED": {
                if (t.length < 3) throw new IllegalArgumentException("Bad MOVED line: " + line);

                ZAxisAction action = parseAction(t[1]);

                if (t.length >= 5) {
                    int from = parseInt(t[2], "MOVED.from");
                    int to   = parseInt(t[3], "MOVED.to");
                    return new MoveCmd(shapes, from, to, panel);
                }

                int idx = parseInt(t[2], "MOVED.index");
                return new LegacyMoveCmd(shapes, idx, action, panel);
            }
            case "UNDO":{
            	if (t.length < 2) throw new IllegalArgumentException("Bad UNDO line: " + line);
                
                StringBuilder originalLine = new StringBuilder();
                for (int i = 1; i < t.length; i++) {
                    if (i > 1) originalLine.append("|");
                    originalLine.append(t[i]);
                }
                Command originalCmd = fromLog(originalLine.toString(), shapes,null, panel);
                return new UndoReplayCmd(originalCmd, panel);
            }
           
            case "REDO" :{
            	 if (t.length < 2) throw new IllegalArgumentException("Bad REDO line: " + line);
                 
                 StringBuilder originalLine = new StringBuilder();
                 for (int i = 1; i < t.length; i++) {
                     if (i > 1) originalLine.append("|");
                     originalLine.append(t[i]);
                 }
                 return fromLog(originalLine.toString(), shapes,null, panel);
            }
            default:
                throw new IllegalArgumentException("Unknown command type: " + kind + " in line: " + line);
        }
    }


    private static final class AddCmd implements Command {
        private final List<Shape> shapes;
        private final JComponent panel;
        private Shape shape;
        private int indexAdded = -1;

        private AddCmd(List<Shape> shapes, Shape shape, JComponent panel) {
            this.shapes = shapes;
            this.shape = shape;
            this.panel = panel;
        }

        @Override
        public void execute() {
            indexAdded = shapes.size();
            shapes.add(shape);
            if (panel != null) panel.repaint();
        }

        @Override
        public void unexecute() {
            if (indexAdded >= 0 && indexAdded < shapes.size()) {
                shapes.remove(indexAdded);
            } else {
                shapes.remove(shape);
            }
            if (panel != null) panel.repaint();
        }

        @Override
        public String toLog() {
            return "ADDED|" + shapeToLogString(shape);
        }
    }

    private static final class RemoveCmd implements Command {
        private final List<Shape> shapes;
        private final JComponent panel;

        private final int index;
        private final Shape expected; 
        private Shape removed;        
        private int removedIndex = -1;

        private RemoveCmd(List<Shape> shapes, int index, Shape expected, JComponent panel) {
            this.shapes = shapes;
            this.index = index;
            this.expected = expected;
            this.panel = panel;
        }

        @Override
        public void execute() {
            removed = null;
            removedIndex = -1;

            if (index >= 0 && index < shapes.size()) {
                Shape candidate = shapes.get(index);
                if (sameByLog(candidate, expected)) {
                    removed = shapes.remove(index);
                    removedIndex = index;
                    if (panel != null) panel.repaint();
                    return;
                }
            }
            for (int i = shapes.size() - 1; i >= 0; i--) {
                if (sameByLog(shapes.get(i), expected)) {
                    removed = shapes.remove(i);
                    removedIndex = i;
                    if (panel != null) panel.repaint();
                    return;
                }
            }

            if (panel != null) panel.repaint();
        }

        @Override
        public void unexecute() {
            if (removed == null) return;

            int pos = removedIndex;
            if (pos < 0) pos = Math.min(index, shapes.size());
            if (pos < 0) pos = 0;
            if (pos > shapes.size()) pos = shapes.size();

            shapes.add(pos, removed);
            if (panel != null) panel.repaint();
        }

        @Override
        public String toLog() {
            return "REMOVED|" + index + "|" + shapeToLogString(expected);
        }
    }
  private static final class EditCmd implements Command {
    	
      private final JComponent panel;
      private DrawingModel model;
      private final int index;
      private final Shape toEdit; 
      private Shape edited;        
      
      
      private final CommandManager c = new CommandManager();
      
      private EditCmd( int index, Shape toEdit,Shape edited, JComponent panel,DrawingModel model) {
          this.index = index;
          this.toEdit = toEdit;
          this.edited=edited;
          this.panel = panel;
          this.model=model;
      }
      
      @Override
      public void execute() {
    	 model.updateShape(index, edited);
    	  if (panel != null) panel.repaint();
      }
      
      @Override
      public void unexecute() {
    	  model.updateShape(index, toEdit);
          if (panel != null) panel.repaint();
      }
      
      @Override
      public String toLog() {
    	  return "EDITED|"+ shapeToLogString(toEdit) + "|TO|" + shapeToLogString(edited);
      }
    }
    
    private static final class UndoReplayCmd implements Command {
        private final Command wrappedCommand;
        private final JComponent panel;
        
        private UndoReplayCmd(Command wrapped, JComponent panel) {
            this.wrappedCommand = wrapped;
            this.panel = panel;
        }
        
        @Override
        public void execute() {
            wrappedCommand.unexecute();
            if (panel != null) panel.repaint();
        }
        
        @Override
        public void unexecute() {
            wrappedCommand.execute();
            if (panel != null) panel.repaint();
        }
        
        @Override
        public String toLog() {
            return "UNDO|" + wrappedCommand.toLog();
        }
    }
    // MOVED|ACTION|from|to
    private static final class MoveCmd implements Command {
        private final List<Shape> shapes;
        private final JComponent panel;
        private int from;
        private int to;

        private MoveCmd(List<Shape> shapes, int from, int to, JComponent panel) {
            this.shapes = shapes;
            this.from = from;
            this.to = to;
            this.panel = panel;
        }

        @Override
        public void execute() {
            if (!validIndex(from, shapes) || !validIndex(to, shapes)) return;
            Shape s = shapes.remove(from);
            shapes.add(to, s);
            if (panel != null) panel.repaint();
        }

        @Override
        public void unexecute() {
            if (!validIndex(to, shapes) || from < 0 || from > shapes.size()) return;
            Shape s = shapes.remove(to);
            shapes.add(from, s);
            if (panel != null) panel.repaint();
        }

        @Override
        public String toLog() {
            return "MOVED|MOVE|" + from + "|" + to;
        }
    }


    private static final class LegacyMoveCmd implements Command {
        private final List<Shape> shapes;
        private final JComponent panel;
        private final ZAxisAction action;

        private int oldIndex;
        private int newIndex;

        private LegacyMoveCmd(List<Shape> shapes, int index, ZAxisAction action, JComponent panel) {
            this.shapes = shapes;
            this.oldIndex = index;
            this.action = action;
            this.panel = panel;
        }

        @Override
        public void execute() {
            if (!validIndex(oldIndex, shapes)) return;

            int last = shapes.size() - 1;
            newIndex = oldIndex;

            switch (action) {
                case TO_FRONT:
                    if (oldIndex == last) return;
                    newIndex = oldIndex + 1;
                    break;
                case TO_BACK:
                    if (oldIndex == 0) return;
                    newIndex = oldIndex - 1;
                    break;
                case BRING_TO_FRONT:
                    if (oldIndex == last) return;
                    newIndex = last;
                    break;
                case BRING_TO_BACK:
                    if (oldIndex == 0) return;
                    newIndex = 0;
                    break;
            }

            Shape s = shapes.remove(oldIndex);
            shapes.add(newIndex, s);
            if (panel != null) panel.repaint();
        }

        @Override
        public void unexecute() {
            if (!validIndex(newIndex, shapes)) return;
            Shape s = shapes.remove(newIndex);
            shapes.add(oldIndex, s);
            if (panel != null) panel.repaint();
        }

        @Override
        public String toLog() {
            return "MOVED|" + action.name() + "|" + oldIndex;
        }
    }

  

    private static Shape parseShape(String[] t, int start) {
        if (t.length <= start) throw new IllegalArgumentException("Missing shape in log");

        String type = t[start].trim().toUpperCase();

        switch (type) {
            case "POINT": {
                int x = parseInt(t[start + 1], "POINT.x");
                int y = parseInt(t[start + 2], "POINT.y");
                Color border = parseColor(t[start + 3], "POINT.color");
                Point p = new Point(x, y);
                p.setColor(border);
                p.setSelected(false);
                return p;
            }

            case "LINE": {
                int x1 = parseInt(t[start + 1], "LINE.x1");
                int y1 = parseInt(t[start + 2], "LINE.y1");
                int x2 = parseInt(t[start + 3], "LINE.x2");
                int y2 = parseInt(t[start + 4], "LINE.y2");
                Color border = parseColor(t[start + 5], "LINE.color");
                Line l = new Line(new Point(x1, y1), new Point(x2, y2));
                l.setColor(border);
                l.setSelected(false);
                return l;
            }

            case "CIRCLE": {
                int cx = parseInt(t[start + 1], "CIRCLE.cx");
                int cy = parseInt(t[start + 2], "CIRCLE.cy");
                int r  = parseInt(t[start + 3], "CIRCLE.r");
                Color border = parseColor(t[start + 4], "CIRCLE.border");
                Color inner  = parseColor(t[start + 5], "CIRCLE.inner");

                Circle c = new Circle();
                c.setCenter(new Point(cx, cy));
                c.setRadius(r);
                c.setColor(border);
                c.setInnerColor(inner);
                c.setSelected(false);
                return c;
            }

            case "RECTANGLE": {
                int x = parseInt(t[start + 1], "RECTANGLE.x");
                int y = parseInt(t[start + 2], "RECTANGLE.y");
                int w = parseInt(t[start + 3], "RECTANGLE.w");
                int h = parseInt(t[start + 4], "RECTANGLE.h");
                Color border = parseColor(t[start + 5], "RECTANGLE.border");
                Color inner  = parseColor(t[start + 6], "RECTANGLE.inner");

                Rectangle rct = new Rectangle();
                rct.setUpperLeftPoint(new Point(x, y));
                rct.setWidth(w);
                rct.setHeight(h);
                rct.setColor(border);
                rct.setInnerColor(inner);
                rct.setSelected(false);
                return rct;
            }

            case "HEXAGON": {
                int x = parseInt(t[start + 1], "HEXAGON.x");
                int y = parseInt(t[start + 2], "HEXAGON.y");
                int r = parseInt(t[start + 3], "HEXAGON.r");
                Color border = parseColor(t[start + 4], "HEXAGON.border");
                Color inner  = parseColor(t[start + 5], "HEXAGON.inner");

                HexagonAdapter hx = new HexagonAdapter(x, y, r, border, inner, false);
                hx.setSelected(false);
                return hx;
            }

            default:
                throw new IllegalArgumentException("Unknown shape type: " + type);
        }
    }

    private static int parseInt(String s, String field) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad int for " + field + ": " + s);
        }
    }

    private static Color parseColor(String s, String field) {
        try {
            String[] rgb = s.trim().split(",");
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return new Color(r, g, b);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad color for " + field + ": " + s);
        }
    }

    private static boolean validIndex(int idx, List<?> list) {
        return idx >= 0 && idx < list.size();
    }

    public enum ZAxisAction {
        TO_FRONT, TO_BACK, BRING_TO_FRONT, BRING_TO_BACK
    }

    private static ZAxisAction parseAction(String s) {
        String a = s.trim().toUpperCase();
        switch (a) {
            case "TO_FRONT": return ZAxisAction.TO_FRONT;
            case "TO_BACK": return ZAxisAction.TO_BACK;
            case "BRING_TO_FRONT": return ZAxisAction.BRING_TO_FRONT;
            case "BRING_TO_BACK": return ZAxisAction.BRING_TO_BACK;
            default:
                return ZAxisAction.TO_FRONT; 
        }
    }

    private static boolean sameByLog(Shape a, Shape b) {
        if (a == null || b == null) return false;
        return shapeToLogString(a).equals(shapeToLogString(b));
    }

    public static String shapeToLogString(Shape s) {
        if (s instanceof Point) {
            Point p = (Point) s;
            return "POINT|" + p.getX() + "|" + p.getY() + "|" + colorToString(p.getColor());
        }

        if (s instanceof Line) {
            Line l = (Line) s;
            return "LINE|"
                    + l.getStartPoint().getX() + "|"
                    + l.getStartPoint().getY() + "|"
                    + l.getEndPoint().getX() + "|"
                    + l.getEndPoint().getY() + "|"
                    + colorToString(l.getColor());
        }
        
        if (s instanceof Donut) {
            Donut d = (Donut) s;
            return "DONUT|"
                    + d.getCenter().getX() + "|"
                    + d.getCenter().getY() + "|"
                    + d.getRadius() + "|"
                    + d.getInnerRadius() + "|"
                    + colorToString(d.getColor()) + "|"
                    + colorToString(d.getInnerColor());
        }

        if (s instanceof Circle) {
            Circle c = (Circle) s;
            return "CIRCLE|"
                    + c.getCenter().getX() + "|"
                    + c.getCenter().getY() + "|"
                    + c.getRadius() + "|"
                    + colorToString(c.getColor()) + "|"
                    + colorToString(c.getInnerColor());
        }

        if (s instanceof Rectangle) {
            Rectangle r = (Rectangle) s;
            return "RECTANGLE|"
                    + r.getUpperLeftPoint().getX() + "|"
                    + r.getUpperLeftPoint().getY() + "|"
                    + r.getWidth() + "|"
                    + r.getHeight() + "|"
                    + colorToString(r.getColor()) + "|"
                    + colorToString(r.getInnerColor());
        }

        if (s instanceof HexagonAdapter) {
            HexagonAdapter h = (HexagonAdapter) s;
            return "HEXAGON|"
                    + h.getHexagon().getX() + "|"
                    + h.getHexagon().getY() + "|"
                    + h.getHexagon().getR() + "|"
                    + colorToString(h.getColor()) + "|"
                    + colorToString(h.getInnerColor());
        }

        throw new IllegalArgumentException("Unknown shape type: " + s.getClass().getName());
    }

    private static String colorToString(Color c) {
        if (c == null) c = Color.BLACK;
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }
}
