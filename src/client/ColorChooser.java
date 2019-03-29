package client;

import java.awt.BorderLayout;
import java.awt.Color;


import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class opens a new Frame that lets the user choose a color for the drawing
 * It uses the JColorChooser component
 * */
public class ColorChooser extends JFrame{

	private Color color;
	private JColorChooser colorChooser;
	private PaintClient instance;
	private boolean fill = false;
	
	/**
	 * Constructor for the ColorChooser class
	 * */
	public ColorChooser(PaintClient instance){
		createView();
		//make a instance of the paint client so we can access the color of the drawingPanel
		this.instance = instance;
		
		setSize(600, 300);
		setResizable(false);
		setTitle("Color Picker");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}//end ColorChooser constructor
	
	/**
	 * Method that creates the view of the color chooser
	 * */
	private void createView() {
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		
		colorChooser = new JColorChooser();
		colorChooser.setPreviewPanel(new JPanel());
		
		//Adding the change listener for the JColorChooser to set the color to the one selected
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				color = colorChooser.getColor();
				instance.drawingP.setCurrentColor(color);
			}
		});
			
		panel.add(colorChooser, BorderLayout.CENTER);
	}//end createaView method
	
	
	public Color getColor() {
		return color;
	}//end getColor method
	
}//end ColorChooser class
