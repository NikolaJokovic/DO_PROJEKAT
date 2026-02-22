package draw;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;

import geometrija.Circle;
import geometrija.Donut;
import geometrija.HexagonAdapter;
import geometrija.Line;
import geometrija.Point;
import geometrija.Rectangle;
import geometrija.Shape;
import hexagon.Hexagon;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.border.LineBorder;

import controller.Command;
import controller.CommandFactory;
import controller.CommandManager;
import controller.LogObserver;
import controller.ModelObserver;
import controller.RemoveShape;
import controller.SaveDrawingStrategy;
import controller.SaveStrategy;
import controller.TextLog;
import controller.UpdateShapeCommand;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

public class FrmDraw extends JFrame {

	private JPanel contentPane;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JToggleButton tglBtnPoint;
	private JToggleButton tglBtnLine;
	private JToggleButton tglBtnCircle;
	private JToggleButton tglBtnDonut;
	private JToggleButton tglBtnRectangle;
	private JToggleButton tglBtnSelect;
	private JToggleButton tglBtnHexagon;
	private JButton btnDelete;
	private JButton btnEdit;
	private PnlDraw pnlCenter;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JMenuBar menuBar;
	private JMenu mnNewMenu;
	private JMenuItem mntmNewMenuItem;
	private JMenuItem mntmNewMenuItem_1;
	private JButton btnUndo;
	private JButton btnRedo;
	private Color activeBorderColor = Color.BLACK;
	private Color activeInnerColor  = Color.GRAY;
	private JButton btnActiveBorder;
	private JButton btnActiveInner;
	private JTextArea txtLog;
	private JScrollPane scrollLog;
	
	private DrawingModel model;
	
	public Color getActiveBorderColor() { return activeBorderColor; }
	public Color getActiveInnerColor()  { return activeInnerColor; }
	
	public void setActiveBorderColor(Color c) { activeBorderColor = c; }
	public void setActiveInnerColor(Color c)  { activeInnerColor = c; }
	
	private final CommandManager commandManager = new CommandManager();
	public CommandManager getCommandManager() { return commandManager; }

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FrmDraw frame = new FrmDraw();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FrmDraw() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("IT 46-2020 Nikola Jokovic");
		setBounds(100, 100, 580, 500);
		
		model=new DrawingModel();
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mnNewMenu = new JMenu("Menu");
		menuBar.add(mnNewMenu);
		
		mntmNewMenuItem = new JMenuItem("Help");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "To modify the shapes you need to use the select button");
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
		
		mntmNewMenuItem_1 = new JMenuItem("Back to menu");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrmMenu menu = new FrmMenu();
				menu.setVisible(true);
				dispose();
			}
		});
		
		JMenuItem miSaveLog = new JMenuItem("Save log");
		miSaveLog.addActionListener(e -> {
		    JFileChooser fc = new JFileChooser();
		    if (fc.showSaveDialog(FrmDraw.this) == JFileChooser.APPROVE_OPTION) {
		        File f = fc.getSelectedFile();
		        try {
		            new TextLog().save(commandManager.getLog(), f);
		            JOptionPane.showMessageDialog(FrmDraw.this, "Log saved.");
		        } catch (Exception ex) {
		            JOptionPane.showMessageDialog(FrmDraw.this, "Save failed: " + ex.getMessage());
		        }
		    }
		});
		mnNewMenu.add(miSaveLog);
		
		JMenuItem miLoadLog = new JMenuItem("Load log");
		miLoadLog.addActionListener(e -> {
		    JFileChooser fc = new JFileChooser();
		    if (fc.showOpenDialog(FrmDraw.this) == JFileChooser.APPROVE_OPTION) {
		        File f = fc.getSelectedFile();
		        try {
		            List<String> lines = new TextLog().load(f);
		            replayLog(lines);
		        } catch (Exception ex) {
		            JOptionPane.showMessageDialog(FrmDraw.this, "Load failed: " + ex.getMessage());
		        }
		    }
		});
		mnNewMenu.add(miLoadLog);
		
		JMenuItem miSaveDrawing = new JMenuItem("Save drawing");
		miSaveDrawing.addActionListener(e -> {
		    JFileChooser fc = new JFileChooser();
		    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		        try {
		            SaveStrategy strategy = new SaveDrawingStrategy();
		            strategy.save( fc.getSelectedFile(),model.getShapes());
		            JOptionPane.showMessageDialog(this, "Drawing saved.");
		        } catch (Exception ex) {
		            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
		        }
		    }
		});
		mnNewMenu.add(miSaveDrawing);

		JMenuItem miLoadDrawing = new JMenuItem("Load drawing");
		miLoadDrawing.addActionListener(e -> {
		    JFileChooser fc = new JFileChooser();
		    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		        try {
		            SaveStrategy strategy = new SaveDrawingStrategy();
		            List<Shape> loaded = strategy.load(fc.getSelectedFile());
		            model.setShapes(new ArrayList<>(loaded));
		            model.notifyObservers();
		            pnlCenter.repaint();
		            JOptionPane.showMessageDialog(this, "Drawing loaded.");
		        } catch (Exception ex) {
		            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
		        }
		    }
		});
		mnNewMenu.add(miLoadDrawing);
		
		btnUndo = new JButton("↶");
		btnRedo = new JButton("↷");
		btnUndo.setOpaque(true);
		btnRedo.setOpaque(true);
		btnUndo.setBorderPainted(false);
		btnRedo.setBorderPainted(false);
		btnUndo.setContentAreaFilled(false);
		btnRedo.setContentAreaFilled(false);
		btnUndo.setBackground(menuBar.getBackground());
		btnRedo.setBackground(menuBar.getBackground());
		menuBar.add(btnUndo);
		menuBar.add(btnRedo);
		btnUndo.addActionListener(e -> {
		    commandManager.undo();
		    pnlCenter.setIndexOfSelectedElement(-1); 
		    pnlCenter.repaint();
		    updateUndoRedoButtons();
		});
		
		btnRedo.addActionListener(e -> {
		    commandManager.redo();
		    pnlCenter.setIndexOfSelectedElement(-1);
		    pnlCenter.repaint();
		    updateUndoRedoButtons();
		});
		
		
		
		
		
		mnNewMenu.add(mntmNewMenuItem_1);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		pnlCenter = new PnlDraw(this,model);
		contentPane.add(pnlCenter, BorderLayout.CENTER);
		pnlCenter.setBackground(Color.white);
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(255, 0, 255), 2, true));
		panel.setBackground(new Color(0, 255, 255));
		contentPane.add(panel, BorderLayout.WEST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{61, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		lblNewLabel = new JLabel("DRAW");
		lblNewLabel.setFont(new Font("Wide Latin", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		tglBtnPoint = new JToggleButton("Point");
		tglBtnPoint.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnPoint = new GridBagConstraints();
		gbc_tglBtnPoint.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnPoint.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnPoint.gridx = 0;
		gbc_tglBtnPoint.gridy = 1;
		panel.add(tglBtnPoint, gbc_tglBtnPoint);
		tglBtnPoint.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnPoint);
		
		tglBtnLine = new JToggleButton("Line");
		tglBtnLine.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnLine = new GridBagConstraints();
		gbc_tglBtnLine.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnLine.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnLine.gridx = 0;
		gbc_tglBtnLine.gridy = 2;
		panel.add(tglBtnLine, gbc_tglBtnLine);
		tglBtnLine.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnLine);
		
		tglBtnCircle = new JToggleButton("Circle");
		tglBtnCircle.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnCircle = new GridBagConstraints();
		gbc_tglBtnCircle.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnCircle.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnCircle.gridx = 0;
		gbc_tglBtnCircle.gridy = 3;
		panel.add(tglBtnCircle, gbc_tglBtnCircle);
		tglBtnCircle.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnCircle);
		
		tglBtnDonut = new JToggleButton("Donut");
		tglBtnDonut.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnDonut = new GridBagConstraints();
		gbc_tglBtnDonut.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnDonut.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnDonut.gridx = 0;
		gbc_tglBtnDonut.gridy = 4;
		panel.add(tglBtnDonut, gbc_tglBtnDonut);
		tglBtnDonut.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnDonut);
		
		tglBtnRectangle = new JToggleButton("Rectangle");
		tglBtnRectangle.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnRectangle = new GridBagConstraints();
		gbc_tglBtnRectangle.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnRectangle.gridx = 0;
		gbc_tglBtnRectangle.gridy = 5;
		panel.add(tglBtnRectangle, gbc_tglBtnRectangle);
		tglBtnRectangle.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnRectangle);
		
		tglBtnHexagon = new JToggleButton("Hexagon");
		tglBtnHexagon.setForeground(new Color(255, 0, 255));
		GridBagConstraints gbc_tglBtnHexagon = new GridBagConstraints();
		gbc_tglBtnHexagon.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnHexagon.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnHexagon.gridx=0;
		gbc_tglBtnHexagon.gridy=6;
		panel.add(tglBtnHexagon,gbc_tglBtnHexagon);
		tglBtnHexagon.setFont(new Font("Wide Latin", Font.ITALIC, 11));
		buttonGroup.add(tglBtnHexagon);
		
		btnActiveBorder = new JButton();
		btnActiveInner  = new JButton();
		setupColorSwatch(btnActiveBorder, activeBorderColor);
		setupColorSwatch(btnActiveInner, activeInnerColor);
		
		GridBagConstraints gbc_border = new GridBagConstraints();
		gbc_border.insets = new Insets(0, -25, 5, 0);
		gbc_border.gridx = 0;
		gbc_border.gridy = 11;         
		gbc_border.fill = GridBagConstraints.NONE;
		panel.add(btnActiveBorder, gbc_border);
		
		GridBagConstraints gbc_inner = new GridBagConstraints();
		gbc_inner.insets = new Insets(0, 30, 5, 0);
		gbc_inner.gridx = 0;
		gbc_inner.gridy = 11;
		gbc_inner.fill = GridBagConstraints.NONE;
		panel.add(btnActiveInner, gbc_inner);

		btnActiveBorder.addActionListener(e -> {
		    Color c = JColorChooser.showDialog(this, "Border color", activeBorderColor);
		    if (c != null) {
		        setActiveBorderColor(c);
		        btnActiveBorder.setBackground(c);
		    }
		});
		btnActiveInner.addActionListener(e -> {
		    Color c = JColorChooser.showDialog(this, "Inner color", activeInnerColor);
		    if (c != null) {
		        setActiveInnerColor(c);
		        btnActiveInner.setBackground(c);
		    }
		});
		
		lblNewLabel_1 = new JLabel("MODIFY");
		lblNewLabel_1.setFont(new Font("Wide Latin", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 7;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		tglBtnSelect = new JToggleButton("Select");
		tglBtnSelect.setFont(new Font("Wide Latin", Font.PLAIN, 11));
		GridBagConstraints gbc_tglBtnSelect = new GridBagConstraints();
		gbc_tglBtnSelect.fill = GridBagConstraints.HORIZONTAL;
		gbc_tglBtnSelect.insets = new Insets(0, 0, 5, 0);
		gbc_tglBtnSelect.anchor = GridBagConstraints.NORTH;
		gbc_tglBtnSelect.gridx = 0;
		gbc_tglBtnSelect.gridy = 8;
		panel.add(tglBtnSelect, gbc_tglBtnSelect);
		buttonGroup.add(tglBtnSelect);
		
		btnEdit = new JButton("Edit");
		btnEdit.setEnabled(false);
		btnEdit.setFont(new Font("Wide Latin", Font.PLAIN, 11));
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnEdit.insets = new Insets(0, 0, 5, 0);
		gbc_btnEdit.gridx = 0;
		gbc_btnEdit.gridy = 9;
		panel.add(btnEdit, gbc_btnEdit);
		
		btnDelete = new JButton("Delete");
		btnDelete.setEnabled(false);
		btnDelete.setFont(new Font("Wide Latin", Font.PLAIN, 11));
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 10;
		panel.add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = model.getFirstSelectedIndex();
		        if (selectedIndex == -1) {
		            JOptionPane.showMessageDialog(null, "Please select shape you want to edit", "Error", JOptionPane.ERROR_MESSAGE);
		        } else {
		            Shape selectedShape = model.getShape(selectedIndex);
					int a=JOptionPane.showConfirmDialog(null,"Are you sure?");  
					if(a==JOptionPane.YES_OPTION){  
						ArrayList<Shape> shapes = model.getShapes();

						for (int i = shapes.size() - 1; i >= 0; i--) {
						    if (shapes.get(i).isSelected()) {
						        commandManager.executeCommand(new RemoveShape(model, i, pnlCenter));
						    }
						}
						pnlCenter.setIndexOfSelectedElement(-1);
						selectionChanged(0);       
						pnlCenter.repaint();
						updateUndoRedoButtons();
					}
				}
			}
		});
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = model.getFirstSelectedIndex();
		        if (selectedIndex == -1) {
		            JOptionPane.showMessageDialog(null, "Please select shape you want to edit", "Error", JOptionPane.ERROR_MESSAGE);
		        } else {
		            Shape selectedShape = model.getShape(selectedIndex);
					
					if(selectedShape instanceof Point) {	
						Point oldPoint= (Point) selectedShape;
						Shape oldState = oldPoint.clone();
						
						DlgPoint editPoint = new DlgPoint();
						 
						editPoint.getTextX().setText(((Point) selectedShape).getX()+"");
						editPoint.getTextY().setText(((Point) selectedShape).getY()+"");
						editPoint.getBtnBorderColor().setBackground(((Point) selectedShape).getColor());
						editPoint.setVisible(true);
						
							if(editPoint.isOk) {
								Point pEdited = new Point(Integer.parseInt(editPoint.getTextX().getText()), Integer.parseInt(editPoint.getTextY().getText()));
								pEdited.setColor(editPoint.getBtnBorderColor().getBackground());
								
								Shape newState = pEdited.clone();
								
								commandManager.executeCommand(
						                new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter)
						            );
								updateUndoRedoButtons();
					            pnlCenter.setIndexOfSelectedElement(-1);
					            selectionChanged(0);
							}
					}
					else if(selectedShape instanceof Line) {
						
						Line oldLine = (Line) selectedShape;
						Shape oldState= oldLine.clone();
						
						DlgLine editLine = new DlgLine();
						
						editLine.getTextStartPointX().setText(((Line) selectedShape).getStartPoint().getX()+"");
						editLine.getTextStartPointY().setText(((Line) selectedShape).getStartPoint().getY()+"");
						editLine.getTextEndPointX().setText(((Line) selectedShape).getEndPoint().getX()+"");
						editLine.getTextEndPointY().setText(((Line) selectedShape).getEndPoint().getY()+"");
						editLine.getBtnBorderColor().setBackground(((Line) selectedShape).getColor());
						editLine.setVisible(true);
						
						
						
						if(editLine.isOk) {
							Point p1 = new Point(Integer.parseInt(editLine.getTextStartPointX().getText()), Integer.parseInt(editLine.getTextStartPointY().getText()));
							Point p2 = new Point(Integer.parseInt(editLine.getTextEndPointX().getText()), Integer.parseInt(editLine.getTextEndPointY().getText()));
							Line lEdited = new Line(p1, p2);
							lEdited.setColor(editLine.getBtnBorderColor().getBackground());
						
							Shape newState = lEdited.clone();
							
							commandManager.executeCommand(
					                new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter)
					            );
							updateUndoRedoButtons();
				            pnlCenter.setIndexOfSelectedElement(-1);
				            selectionChanged(0);
						}
					}
					else if(selectedShape instanceof Donut) {
						
						Donut oldDonut = (Donut) selectedShape;
						Shape oldState = oldDonut.clone();
						
						DlgDonut editDonut = new DlgDonut();
						
						editDonut.getTextX().setText(((Donut) selectedShape).getCenter().getX()+"");
						editDonut.getTextY().setText(((Donut) selectedShape).getCenter().getY()+"");
						editDonut.getTextRadius().setText(((Donut) selectedShape).getRadius()+"");
						editDonut.getTextInnerRadius().setText(((Donut) selectedShape).getInnerRadius()+"");
						editDonut.getBtnBorderColor().setBackground(((Donut) selectedShape).getColor());
						editDonut.getBtnInnerColor().setBackground(((Donut) selectedShape).getInnerColor());
						editDonut.getOkButton().setText("Edit");
						editDonut.setVisible(true);
						
						if(editDonut.isOk) {
							Donut dEdited = new Donut();
							
							Point center = new Point(Integer.parseInt(editDonut.getTextX().getText()), Integer.parseInt(editDonut.getTextY().getText()));
							
							try {
								dEdited.setCenter(center);
								dEdited.setRadius(Integer.parseInt(editDonut.getTextRadius().getText()));
								dEdited.setInnerRadius(Integer.parseInt(editDonut.getTextInnerRadius().getText()));
								dEdited.setColor(editDonut.getBtnBorderColor().getBackground());
								dEdited.setInnerColor(editDonut.getBtnInnerColor().getBackground());
								
								Shape newState = dEdited.clone();
								
								commandManager.executeCommand(
						                new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter)
						            );
								updateUndoRedoButtons();
					            pnlCenter.setIndexOfSelectedElement(-1);
					            selectionChanged(0);

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							}
						}
	
					}
					else if(selectedShape instanceof Circle) {
						Circle oldCircle = (Circle) selectedShape;
					    Shape oldState = oldCircle.clone();
						
						
						
						DlgCircle editCircle = new DlgCircle();
						
						editCircle.getTextX().setText(((Circle) selectedShape).getCenter().getX()+"");
						editCircle.getTextY().setText(((Circle) selectedShape).getCenter().getY()+"");
						editCircle.getTextRadius().setText(((Circle) selectedShape).getRadius()+"");
						editCircle.getBtnBorderColor().setBackground(((Circle) selectedShape).getColor());
						editCircle.getBtnInnerColor().setBackground(((Circle) selectedShape).getInnerColor());
						editCircle.getOkButton().setText("Edit");
						editCircle.setVisible(true);
						
						if(editCircle.isOk) {
							
							
							try {
								Point center = new Point(Integer.parseInt(editCircle.getTextX().getText()), Integer.parseInt(editCircle.getTextY().getText()));
								
								Circle cEdited = new Circle();
								
								cEdited.setCenter(center);
								cEdited.setRadius(Integer.parseInt(editCircle.getTextRadius().getText()));
								cEdited.setColor(editCircle.getBtnBorderColor().getBackground());
								cEdited.setInnerColor(editCircle.getBtnInnerColor().getBackground());
								
								Shape newState= cEdited.clone();
								commandManager.executeCommand(new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter));
								updateUndoRedoButtons();
						        pnlCenter.setIndexOfSelectedElement(-1);
						        selectionChanged(0);

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							}
						}
					
						
					}
					else if(selectedShape instanceof Rectangle) {
						
						Rectangle oldRectangle = (Rectangle) selectedShape;
						Shape oldState= oldRectangle.clone();
						
						DlgRectangle editRectangle = new DlgRectangle();
						
						editRectangle.getTextX().setText(((Rectangle) selectedShape).getUpperLeftPoint().getX()+"");
						editRectangle.getTextY().setText(((Rectangle) selectedShape).getUpperLeftPoint().getY()+"");
						editRectangle.getTextHeight().setText(((Rectangle) selectedShape).getHeight()+"");
						editRectangle.getTextWidth().setText(((Rectangle) selectedShape).getWidth()+"");
						editRectangle.getBtnBorderColor().setBackground(((Rectangle) selectedShape).getColor());
						editRectangle.getBtnInnerColor().setBackground(((Rectangle) selectedShape).getInnerColor());
						editRectangle.getOkButton().setText("Edit");
						editRectangle.setVisible(true);
						if(editRectangle.isOk) {
							Rectangle rEdited = new Rectangle();
							
							try {
								Point uppLeft = new Point(Integer.parseInt(editRectangle.getTextX().getText()), Integer.parseInt(editRectangle.getTextY().getText()));
								
								rEdited.setUpperLeftPoint(uppLeft);
								rEdited.setHeight(Integer.parseInt(editRectangle.getTextHeight().getText()));
								rEdited.setWidth(Integer.parseInt(editRectangle.getTextWidth().getText()));
								rEdited.setColor(editRectangle.getBtnBorderColor().getBackground());
								rEdited.setInnerColor(editRectangle.getBtnInnerColor().getBackground());
								
								Shape newState= rEdited.clone();
								commandManager.executeCommand(
						                new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter)
						            );
								
								updateUndoRedoButtons();
					            pnlCenter.setIndexOfSelectedElement(-1);
					            selectionChanged(0);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
							}	
						}
					}
					else if(selectedShape instanceof HexagonAdapter) {
						
						HexagonAdapter oldHexagon=(HexagonAdapter) selectedShape;
						Shape oldState = oldHexagon.clone();
						
						DlgHexagon editHexagon = new DlgHexagon();
						
						editHexagon.getTextX().setText(((HexagonAdapter)selectedShape).getHexagon().getX()+"");
						editHexagon.getTextY().setText(((HexagonAdapter)selectedShape).getHexagon().getY()+"");
						editHexagon.getTextR().setText(((HexagonAdapter)selectedShape).getHexagon().getR()+"");
						editHexagon.getBtnBorderColor().setBackground(((HexagonAdapter) selectedShape).getColor());
						editHexagon.getBtnInnerColor().setBackground(((HexagonAdapter) selectedShape).getInnerColor());
						editHexagon.getOkButton().setText("Edit");
						editHexagon.setVisible(true);
						if(editHexagon.isOk) {			
							try {
						        int x = Integer.parseInt(editHexagon.getTextX().getText());
						        int y = Integer.parseInt(editHexagon.getTextY().getText());
						        int r = Integer.parseInt(editHexagon.getTextR().getText());
						        Color border = editHexagon.getBtnBorderColor().getBackground();
						        Color inner = editHexagon.getBtnInnerColor().getBackground();
						        
						        HexagonAdapter hEdited = new HexagonAdapter(x, y, r, border, inner, false);
						        Shape newState = hEdited.clone();
						        
						        commandManager.executeCommand(new UpdateShapeCommand(model, model.getLastSelectedIndex(), oldState, newState, pnlCenter));
						        
						        updateUndoRedoButtons();
						        pnlCenter.setIndexOfSelectedElement(-1);
						        selectionChanged(0);
						        
							} catch (Exception e2) {
								// TODO: handle exception
							}
							
						}
					}
					pnlCenter.setIndexOfSelectedElement(-1);
					pnlCenter.repaint();
				}
			}
			
		});
		
	JPanel logPanel = new JPanel();
	logPanel.setLayout(new BorderLayout());
	logPanel.setBorder(new LineBorder(Color.MAGENTA, 2));
	logPanel.setPreferredSize(new Dimension(0, 80));
	contentPane.add(logPanel,BorderLayout.SOUTH);
		    
	JLabel lblLog = new JLabel("Command Log:");
	lblLog.setFont(new Font("Wide Latin", Font.BOLD, 10));
	lblLog.setForeground(new Color(255, 0, 255));
	logPanel.add(lblLog, BorderLayout.NORTH);
	
	txtLog = new JTextArea(8, 8); 
	txtLog.setEditable(false);
	txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
	txtLog.setBackground(Color.WHITE);
	txtLog.setForeground(Color.BLACK);
	scrollLog = new JScrollPane(txtLog);
	scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	logPanel.add(scrollLog, BorderLayout.CENTER);
		    
		    
	commandManager.addLogObserver(new LogObserver() {
		@Override
		public void onLogAdded(String logLine) {
			txtLog.append(logLine +"\n");
		}
	});
	
	model.addObserver(new ModelObserver() {
		@Override
		public void update() {
			int count= model.getSelectedCount();
			btnDelete.setEnabled(count>0);
			btnEdit.setEnabled(count==1);
		}
	});
	
	}
	
	
	
	public DrawingModel getModel() {
        return model;
    }
	public void updateUndoRedoButtons() {
	    boolean canUndo = commandManager.canUndo();
	    boolean canRedo = commandManager.canRedo();

	    btnUndo.setEnabled(canUndo);
	    btnRedo.setEnabled(canRedo);

	    btnUndo.setForeground(canUndo ? Color.BLACK : Color.GRAY);
	    btnRedo.setForeground(canRedo ? Color.BLACK : Color.GRAY);
	}

	public JToggleButton getTglBtnPoint() {
		return tglBtnPoint;
	}
	public JToggleButton getTglBtnLine() {
		return tglBtnLine;
	}
	public JToggleButton getTglBtnCircle() {
		return tglBtnCircle;
	}
	public JToggleButton getTglBtnDonut() {
		return tglBtnDonut;
	}
	public JToggleButton getTglBtnRectangle() {
		return tglBtnRectangle;
	}
	public JToggleButton getTglBtnHexagon() {
		return tglBtnHexagon;
	}
	public JToggleButton getTglBtnSelect() {
		return tglBtnSelect;
	}
	
	public JButton getBtnDelete() {
		return btnDelete;
	}
	public JButton getBtnEdit() {
		return btnEdit;
	}
	public void selectionChanged(int selectedCount) {
	    btnDelete.setEnabled(selectedCount > 0);
	    btnEdit.setEnabled(selectedCount == 1);
	}
	private void setupColorSwatch(JButton b, Color c) {
	    b.setPreferredSize(new Dimension(24, 24));
	    b.setOpaque(true);
	    b.setContentAreaFilled(true);
	    b.setBorderPainted(true);
	    b.setFocusPainted(false);
	    b.setBackground(c);
	}
	
	private void replayLog(List<String> lines) {
	    for (String line : lines) {
	    	 Command cmd = CommandFactory.fromLog(line, model.getShapes(),model, pnlCenter);

	    	    int a = JOptionPane.showConfirmDialog(this, "Execute next?\n" + line, "Replay",
	    	            JOptionPane.YES_NO_OPTION);

	    	    if (a != JOptionPane.YES_OPTION) break;

	    	    commandManager.executeCommandFromReplay(cmd);
	    	    updateUndoRedoButtons();
	    }
	}
}
