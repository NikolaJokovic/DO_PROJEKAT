package controller;
import java.util.*;


public class CommandManager {
	
	private final Stack<Command> undoStack = new Stack<>();
	private final Stack<Command> redoStack = new Stack<>();
	private final List<String> log = new ArrayList<>();
	
	private final List<LogObserver> logObservers = new ArrayList<>();
	

	 public void addLogObserver(LogObserver observer) {
	        logObservers.add(observer);
	    }
	    
	 private void notifyLogObservers(String logLine) {
		 for (LogObserver observer : logObservers) {
			 observer.onLogAdded(logLine);
		 }
	 }
	 
	 public void executeCommandFromReplay(Command c) {
	        c.execute();
	        notifyLogObservers(c.toLog());
	        undoStack.push(c);
	    }
	    
	    
	public void executeCommand (Command c) {
		c.execute();
		undoStack.push(c);
		redoStack.clear();
		
		String line = c.toLog();
	    if (line != null && !line.isBlank()) {
	        log.add(line);
	        notifyLogObservers(line);
	    }
	}
	
	public void undo() {
		if(undoStack.isEmpty())return;
		
		Command c = undoStack.pop();
		c.unexecute();
		redoStack.push(c);
		
		String line = c.toLog();
	    if (line != null && !line.isBlank()) {
	        log.add("UNDO|" + line);
	        notifyLogObservers("UNDO: " + c.toLog());
	    }
	}	
	
	public void redo() {
		if(redoStack.isEmpty())return;
		
		Command c = redoStack.pop();
		c.execute();
		undoStack.push(c);
		
		String line = c.toLog();
	    if (line != null && !line.isBlank()) {
	    	log.add("REDO|" + line);
	        notifyLogObservers("REDO|" + c.toLog());
	    }
	}
	
	public void clearAll() {
	    undoStack.clear();
	    redoStack.clear();
	    log.clear();
	}
	
	public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    public List<String> getLog() { return log; }
    
    public Stack<Command> getUndoStack(){return undoStack;}
    public Stack<Command> getRedoStack(){return redoStack;}


	
}
