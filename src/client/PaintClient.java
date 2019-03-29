package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;


import network.NetworkClient;
import packet.Packet;

/**
 * Project's main client class that holds all the graphical components
 * @author Somlea Mihai
 * @since 06.01.2019
 * */
public class PaintClient extends JFrame implements WindowListener{
	
	private static final long serialVersionUID = 1527669407967958046L;
	
	private static final String TITLE = "Paint Client";
	private static PaintClient instance;
	private static final String newline = "\n";
	private static ColorChooser colorChooser;
	
	static final int THICKNESS_MAX = 50;
	static final int THICKNESS_MIN = 1;
	static final int THICKNESS_INIT = 3;
	
	private int imageCounter = 0;
	
	//Network client object(network module)
	private NetworkClient client;
	
	private JMenuBar menuBar;
	private JMenu fileMenu, window, about, subMenu;
	private JMenuItem menuItem;
	private JFileChooser fileC;
	public DrawingPanel drawingP;
	private BufferedImage image, screenCopy;
	
	
	/**
	 * Constructor for the paint client class
	 * */
	public PaintClient() {
		createMenuView();
		createView();
		
		instance = this;
		
		client = new NetworkClient("127.0.0.1", 1501, this);
		client.connectToServer();
		
		setTitle(TITLE);
		setSize(1200, 650);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}// end paintClient constructor
	
	/**
	 * Method that creates the view of the menu of the frame
	 * */
	private void createMenuView() {
		//making the menu bar
		menuBar = new JMenuBar();
		//adding the first menu that controls the file
		fileMenu = new JMenu("File");
		//creating the menus for the file menu
		JMenuItem menuItemFile = new JMenuItem("New File");
		menuItemFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		menuItemFile.getAccessibleContext().setAccessibleDescription("Makes a new file where you can draw!");
		
		//adding the action listener for the new file menu item
		menuItemFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				instance.drawingP.clearScreen();
			}
		});//end action listener for the new file menu item
		
		fileMenu.add(menuItemFile);
		JMenuItem menuItemSave = new JMenuItem("Save");
		//adding the action listener for the save menu item
		menuItemSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage img = getScreenShot();
				try{
					ImageIO.write(img, "png", new File("screenshot" + imageCounter + ".png"));
					imageCounter++;
				}catch(Exception ex) {
					ex.printStackTrace();
				}//end try catch
			}//end actionPerformed method
		});//end menu item save action listener
		fileMenu.add(menuItemSave);
		
		JMenuItem menuItemSaveAs = new JMenuItem("Save as..");
		//adding the action listener for the save as button
		menuItemSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage img = getScreenShot();
				String extension = JOptionPane.showInputDialog("Enter the extension for the capture:");
				try {
					ImageIO.write(img, extension, new File("screenshot" + imageCounter + "." + extension));
					imageCounter++;
				}catch(Exception ex) {
					ex.printStackTrace();
				}//end try catch
			}//end actionPerformed method
		});//end menu item save as action listener
		fileMenu.add(menuItemSaveAs);
		
		JMenuItem menuItemDelete = new JMenuItem("Delete");
		//TODO add the behavior for the delete menu item
		fileMenu.add(menuItemDelete);
		JMenuItem menuItemClose = new JMenuItem("Close");
		//adding the action listener for the close menu item
		menuItemClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});//end menu item close action listener
		fileMenu.add(menuItemClose);
		
		//creating the window menu that hold the preferences
		//TODO add other menus and items for the window menu
		window = new JMenu("Window");
		subMenu = new JMenu("Preferences");
		//TODO add the menu items for the preferences sub menu
		window.add(subMenu);
		//creating the about menu
		about = new JMenu("About");
		
		//adding the menus to the menu bar
		menuBar.add(fileMenu);
		menuBar.add(window);
		menuBar.add(about);
		//adding the menu bar to the frame
		this.setJMenuBar(menuBar);
	}//end createMenuView method
	
	/**
	 * Method that creates the GUI of the paint client
	 * */
	private void createView() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(182, 189, 198));
		
		//making the layout for the top side
		//TOP
		JPanel topP = new JPanel(new BorderLayout());
		topP.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		topP.setPreferredSize(new Dimension(800, 100));
		topP.setBackground(new Color(237, 241, 247));
		panel.add(topP, BorderLayout.NORTH);
		
		fileC = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "gif");
		fileC.setFileFilter(filter);
		
		//TOP LEFT
		JPanel topLeftP = new JPanel(new BorderLayout());
		
		//TOP LEFT LEFT
		JPanel topLeftLeftP = new JPanel(new BorderLayout());
		topLeftLeftP.setBorder(BorderFactory.createTitledBorder("Clipboard"));
		JButton copy = new JButton();
		copy.setText("Copy");
		copy.setToolTipText("Copy (Alt + C)" + newline + " Copy the canvas");
		copy.setPreferredSize(new Dimension(70, 40));
		copy.setMnemonic(KeyEvent.VK_C);
		copy.setHorizontalAlignment(JButton.CENTER);
		
		//adding the action listener for the copy button
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				screenCopy = getScreenShot();
				System.out.println("The canvas has been copied to clipboard!!");
			}
		});//end action listener for the copy button
		
		topLeftLeftP.add(copy, BorderLayout.EAST);
		//adding the left panel to the top left side
		topLeftP.add(topLeftLeftP, BorderLayout.WEST);
		
		//TOP LEFT CENTER
		JPanel topCenterLeftP = new JPanel(new BorderLayout());
		topCenterLeftP.setBorder(BorderFactory.createTitledBorder("Tools"));
		//TOP LEFT CENTER LEFT
		//button that clears the canvas
		JButton  clearB = new JButton();
		clearB.setText("Clear");
		clearB.setPreferredSize(new Dimension(80, 40));
		
		//adding the action listener for the clear button
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.drawingP.clearScreen();
			}//end actionPerformed method
		});//end action listener for the clear button
		
		topCenterLeftP.add(clearB, BorderLayout.WEST);
		//TOP LEFT CENTER RIGHT
		JPanel topLeftCenterRightP = new JPanel(new BorderLayout());
		JCheckBox eraserC = new JCheckBox();
		eraserC.setText("Eraser");
		eraserC.setSelected(false);
		eraserC.setPreferredSize(new Dimension(90, 30));
		topLeftCenterRightP.add(eraserC, BorderLayout.NORTH);
		JButton randomB = new JButton();
		randomB.setEnabled(false);
		randomB.setPreferredSize(new Dimension(90, 10));
		topLeftCenterRightP.add(randomB, BorderLayout.CENTER);
		JCheckBox selectC = new JCheckBox();
		selectC.setText("Select");
		selectC.setPreferredSize(new Dimension(90, 30));
		selectC.setSelected(false);
		topLeftCenterRightP.add(selectC, BorderLayout.SOUTH);

		
		topCenterLeftP.add(topLeftCenterRightP, BorderLayout.EAST);
		topLeftP.add(topCenterLeftP, BorderLayout.CENTER);
		
		//TOP LEFT RIGHT
		JPanel topLeftRightP = new JPanel(new BorderLayout());
		topLeftRightP.setBorder(BorderFactory.createTitledBorder("Paint"));
		//left panel of the topLeftRightP
		JPanel left = new JPanel(new BorderLayout());
		JPanel leftLeft = new JPanel(new BorderLayout());
		JCheckBox pencilDraw = new JCheckBox();
		pencilDraw.setText("Free draw");
		pencilDraw.setSelected(true);
		pencilDraw.setPreferredSize(new Dimension(90, 30));
		leftLeft.add(pencilDraw, BorderLayout.NORTH);
		JCheckBox shape = new JCheckBox();
		shape.setText("Shapes");
		shape.setPreferredSize(new Dimension(70, 30));
		shape.setSelected(false);
		leftLeft.add(shape, BorderLayout.CENTER);
		JCheckBox text = new JCheckBox();
		text.setText("Text");
		text.setPreferredSize(new Dimension(50, 30));
		text.setSelected(false);
		leftLeft.add(text, BorderLayout.SOUTH);
		ButtonGroup paintGroup = new ButtonGroup();
		paintGroup.add(pencilDraw);
		paintGroup.add(shape);
		paintGroup.add(text);
		paintGroup.add(eraserC);
		paintGroup.add(selectC);
		JPanel leftRight = new JPanel(new BorderLayout());
		
		JLabel thicknessL = new JLabel();
		thicknessL.setText("Line thickness:");
		thicknessL.setPreferredSize(new Dimension(90, 25));
		thicknessL.setHorizontalAlignment(JLabel.CENTER);
		
		JSlider thickness = new JSlider(JSlider.HORIZONTAL, THICKNESS_MIN, THICKNESS_MAX, THICKNESS_INIT);
		thickness.setPreferredSize(new Dimension(100, 40));
		thickness.setAlignmentY(CENTER_ALIGNMENT);
		thickness.setMajorTickSpacing(1);
		thickness.setPaintLabels(false);
		
		//Adding the change listener to the slider
		thickness.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if(!source.getValueIsAdjusting()) {
					instance.drawingP.setStrokeWidth(source.getValue());
				}//end if
			}
		});//end change listener for the slider
		
		JButton colorB = new JButton();
		colorB.setText("Color");
		colorB.setPreferredSize(new Dimension(70, 30));
		
		//adding the action listener for the button that controls the color
		colorB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				colorChooser = new ColorChooser(instance);
			}
		});//end action listener for the color button
		
		leftRight.add(thicknessL, BorderLayout.NORTH);
		leftRight.add(thickness, BorderLayout.CENTER);
		leftRight.add(colorB, BorderLayout.SOUTH);
		left.add(leftLeft, BorderLayout.WEST);
		left.add(leftRight, BorderLayout.EAST);
		
		//adding the center of the top left panel
		JPanel center = new JPanel(new BorderLayout());
		JPanel centerTop = new JPanel(new BorderLayout());
		JLabel shapeL = new JLabel();
		shapeL.setText("Shapes:");
		shapeL.setPreferredSize(new Dimension(50, 20));
		
		JComboBox<String> shapes = new JComboBox<>();
		shapes.addItem(new String("OVAL"));
		shapes.addItem(new String("RECT"));
		shapes.addItem(new String("LINE"));
		shapes.setSelectedItem(null);
		shapes.setPreferredSize(new Dimension(70, 30));
		shapes.setEnabled(false);
		
		//adding the item inside the custom top right left panel
		centerTop.add(shapeL, BorderLayout.WEST);
		centerTop.add(shapes, BorderLayout.EAST);
		
		JPanel centerSouth = new JPanel(new BorderLayout());
		JCheckBox fill = new JCheckBox();
		fill.setText("Fill");
		fill.setSelected(false);
		fill.setEnabled(false);
		
		JComboBox<String> lines = new JComboBox<>();
		lines.addItem(new String("Straight"));
		lines.addItem(new String("Dotted"));
		lines.addItem(new String("Interupted"));
		lines.setSelectedItem(new String("Straight"));
		lines.setPreferredSize(new Dimension(70, 30));
		lines.setEnabled(false);
		
		//adding the item listener for the type of lines
		lines.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				instance.drawingP.setLineType((String)lines.getSelectedItem());
			}
		});//end item listener for the line combo box
		
		centerSouth.add(fill, BorderLayout.WEST);
		centerSouth.add(lines, BorderLayout.EAST);
		
		//adding the item listener for the shapes comboBox
		shapes.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				drawingP.setCurrentTool((String)shapes.getSelectedItem());
				//check if the selected shape is a line
				if(shapes.getSelectedItem().equals("LINE")) {
					fill.setEnabled(false);
					thickness.setEnabled(true);
					lines.setEnabled(true);
				}else {
					fill.setEnabled(true);
				}//end if else
				if(fill.isSelected()) {
					if(shapes.getSelectedItem().equals("LINE")) {
						return;
					}else {
						if(shapes.getSelectedItem().equals("OVAL")) {
							drawingP.setCurrentTool("FILLED_OVAL");
						}else {
							drawingP.setCurrentTool("FILLED_RECT");
						}//end if else
					}//end if else
				}//end if
			}//end ItemListener object
		});
		
		center.add(centerTop, BorderLayout.NORTH);
		center.add(centerSouth, BorderLayout.SOUTH);
	
		JPanel right = new JPanel(new BorderLayout());
		JButton fontB = new JButton();
		fontB.setText("Font");
		fontB.setPreferredSize(new Dimension(70, 32));
		fontB.setEnabled(false);
		//adding the action listener for the font button
		fontB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFontChooser fontChooser = new JFontChooser();
				int result = fontChooser.showDialog(instance);
				   if (result == JFontChooser.OK_OPTION){
				       instance.drawingP.setFont(fontChooser.getSelectedFont()); 
				       System.out.println("Selected Font : " + fontChooser.getSelectedFont()); 
				   }//end if
			}
		});//end action listener for the font button
		
		JButton textB = new JButton();
		textB.setText("Text");
		textB.setPreferredSize(new Dimension(70, 32));
		textB.setEnabled(false);
		//adding the action listener for the text button
		textB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Enter the text:");
				instance.drawingP.setText(input);
				instance.drawingP.setCurrentTool("TEXT");
			}
		});//end action listener for the text button
		
		//button that was placed only to add more spacing between components
		JButton rButton = new JButton();
		rButton.setPreferredSize(new Dimension(70, 10));
		rButton.setEnabled(false);
		rButton.setText("");
		
		right.add(fontB, BorderLayout.NORTH);
		right.add(rButton, BorderLayout.CENTER);
		right.add(textB, BorderLayout.SOUTH);
		
		
		//adding the action listener for the check boxes
		pencilDraw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(pencilDraw.isSelected()) {
					thickness.setEnabled(true);
					fill.setEnabled(false);
					shapes.setEnabled(false);
					fontB.setEnabled(false);
					textB.setEnabled(false);
					lines.setEnabled(false);
					eraserC.setSelected(false);
					selectC.setSelected(false);
					instance.drawingP.setCurrentTool("CURVE");
				}//end if
			}
		});//end action listener for pencilDraw
		
		
		//adding the action listener for the shape check box
		shape.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(shape.isSelected()) {
					shapes.setEnabled(true);
					fill.setEnabled(true);
					thickness.setEnabled(false);
					fontB.setEnabled(false);
					textB.setEnabled(false);
					lines.setEnabled(false);
					eraserC.setSelected(false);
					selectC.setSelected(false);
					instance.drawingP.setCurrentTool("EMPTY");
				}//end if
			}
		});//end action listener for the shape
		
		//adding the action listener for the text check box
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(text.isSelected()) {
					fontB.setEnabled(true);
					textB.setEnabled(true);
					thickness.setEnabled(false);
					shapes.setEnabled(false);
					fill.setEnabled(false);
					lines.setEnabled(false);
					eraserC.setSelected(false);
					selectC.setSelected(false);
					instance.drawingP.setCurrentTool("TEXT");
				}//end if
			}
		});//end action listener for the text
		
		//adding the action listener for the eraser check box
		eraserC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(eraserC.isSelected()) {
					fontB.setEnabled(false);
					textB.setEnabled(false);
					thickness.setEnabled(true);
					shapes.setEnabled(false);
					fill.setEnabled(false);
					lines.setEnabled(false);
					pencilDraw.setSelected(false);
					shape.setSelected(false);
					text.setSelected(false);
					instance.drawingP.setCurrentTool("ERASE");
				}//end if
			}
		});//end action listener for the eraser check box
		
		//adding the action listener for the select combo box
		selectC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectC.isSelected()) {
					fontB.setEnabled(false);
					textB.setEnabled(false);
					thickness.setEnabled(false);
					shapes.setEnabled(false);
					fill.setEnabled(false);
					lines.setEnabled(false);
					pencilDraw.setSelected(false);
					shape.setSelected(false);
					text.setSelected(false);
					instance.drawingP.setCurrentTool("SELECT");
				}//end if
			}
		});//end action listener for the select combo box
		
		
		topLeftRightP.add(left, BorderLayout.WEST);
		topLeftRightP.add(center, BorderLayout.CENTER);
		topLeftRightP.add(right, BorderLayout.EAST);
		topLeftP.add(topLeftRightP, BorderLayout.EAST);
		
		
		//TOP CENTER
		JPanel centerP =  new JPanel(new BorderLayout());
		centerP.setBorder(BorderFactory.createTitledBorder("Image"));
		centerP.setPreferredSize(new Dimension(100, 40));
		JPanel topCenter = new JPanel(new BorderLayout());

		//button that controls the image that is uploaded
		JButton imageUP = new JButton();
		imageUP.setText("Upload Image");
		imageUP.setPreferredSize(new Dimension(100, 30));
		

		
		//button that was placed only to add more spacing between components
		JButton rButt = new JButton();
		rButt.setPreferredSize(new Dimension(100, 10));
		rButt.setEnabled(false);
		
		JComboBox<String> imageOptions = new JComboBox<>();
		imageOptions.addItem(new String("Normal"));
		imageOptions.addItem(new String("Gray Scale"));
		imageOptions.addItem(new String("Negative"));
		imageOptions.addItem(new String("Red Filter"));
		imageOptions.addItem(new String("Blue Filter"));
		imageOptions.addItem(new String("Green Filter"));
		imageOptions.setSelectedItem("Normal");
		imageOptions.setEnabled(false);
		imageOptions.setPreferredSize(new Dimension(100, 30));
		
		//adding the item listener for the image options combo box
		imageOptions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				switch((String) imageOptions.getSelectedItem()) {
				case "Normal":
					insertImage(image, true);
					break;
				case "Gray Scale":
					instance.drawingP.imageFilter("Gray Scale");
					break;
				case "Negative":
					instance.drawingP.imageFilter("Negative");
					break;
				case "Red Filter":
					instance.drawingP.imageFilter("Red Filter");
					break;
				case "Green Filter":
					instance.drawingP.imageFilter("Green Filter");
					break;
				case "Blue Filter":
					instance.drawingP.imageFilter("Blue Filter");
					break;
				}//end switch statement
			}//end itemStateChanged method
		});//end item listener for the image options combo box
		
		
		//adding the action listener for the upload image button
		imageUP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fileVal = fileC.showOpenDialog(instance);
				
				if(fileVal == JFileChooser.APPROVE_OPTION) {
					imageOptions.setEnabled(true);
					File file = fileC.getSelectedFile();
					//resizing the selected image to 500x300 pixels
					image = getScaledImage(new ImageIcon(file.getAbsolutePath()).getImage(), 1000, 400);
					insertImage(image, true);
				}//end if
			}//end actionPerformed method
		});//end action listener for the item upload button
		
		topCenter.add(imageUP, BorderLayout.NORTH);
		topCenter.add(rButt, BorderLayout.CENTER);
		topCenter.add(imageOptions, BorderLayout.SOUTH);
		centerP.add(topCenter, BorderLayout.NORTH);
		
		
		//TOP Right
		JPanel topRightP = new JPanel(new BorderLayout());
		JButton randomButton = new JButton();
		randomButton.setPreferredSize(new Dimension(300, 100));
		randomButton.setEnabled(false);
		randomButton.setText("");
		JPanel topRightLeftP = new JPanel(new BorderLayout());
		topRightLeftP.setBorder(BorderFactory.createTitledBorder("Server"));
		JButton shareB = new JButton();
		shareB.setText("Share");
		shareB.setPreferredSize(new Dimension(70, 50));
		//adding the action listener for the share button
		shareB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Packet packet = new Packet();
				screenCopy = getScreenShot();
				int width = screenCopy.getWidth();
				int height = screenCopy.getHeight();
				packet.setImageWidth(width);
				packet.setImageHeight(height);
				packet.setImageState(true);
				int[][] pixels = new int[width][height];
				for(int x = 0; x < width; x++) {
					for(int y = 0; y < height; y++) {
						pixels[x][y] = screenCopy.getRGB(x, y);
					}//end for
				}//end for
				packet.setImage(pixels);
				try {
					client.sendPacket(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//end try catch statement
			}
		});//end action listener for the share button
		topRightLeftP.add(shareB, BorderLayout.CENTER);
		
		topRightP.add(topRightLeftP, BorderLayout.WEST);
		topRightP.add(randomButton, BorderLayout.CENTER);
		
		//adding the panels to the top panel
		topP.add(topLeftP, BorderLayout.WEST);
		topP.add(centerP, BorderLayout.CENTER);
		topP.add(topRightP, BorderLayout.EAST);
		
		//CENTER
		drawingP = new DrawingPanel(this);
		panel.add(drawingP, BorderLayout.CENTER);
		
		//adding the main panel to the frame
		getContentPane().add(panel);
		
	}//end createView method
	
	
	/**
	 * This method returns the instance of the PaintClient class
	 * */
	public static PaintClient getInstance() {
		return instance;
	}//end getInstance method

	/**
	 * Method that captures the drawing canvas of the client and returns it
	 * */
	private BufferedImage getScreenShot() {
		int width = instance.drawingP.getWidth();
		int height = instance.drawingP.getHeight();
		//int[][] pixels = new int[width][height];
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		Graphics2D g2d = image.createGraphics();
//		g2d.drawImage(instance.drawingP.getBackBuffer(), 0, 0, width, height, null);
//		g2d.dispose();
//		for(int x = 0; x < width; x++) {
//			for(int y = 0; y < height; y++) {
//				pixels[x][y] = image.getRGB(x, y);
//			}//end for
//		}//end for
		instance.drawingP.paint(image.getGraphics());
		return image;
	}//end getScreenShot
	
	/**
	 * Method that scales the image to a different dimension and returns it
	 * @param srcImage the source image that will be changed
	 * @param w the new width of the image
	 * @param h the new height of the image 
	 * */
	private BufferedImage getScaledImage(Image srcImage, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImg.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(srcImage, 0, 0, w, h, null);
		g2d.dispose();
		return resizedImg;
	}//end getScaledImage method
	
	/**
	 * Sends the image to the canvas and renders it to the screen
	 * @param srcImage The image that will be rendered to the screen
	 * @param offset A boolean that if true will enter a offset to the image
	 * */
	public void insertImage(BufferedImage srcImage,boolean offset) {
		instance.drawingP.setImage(srcImage);
		instance.drawingP.renderImage(srcImage, offset);
	}//end insertImage method
	
	
	/**
	 * Entry onto the application
	 * */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				instance = new PaintClient();
				instance.setVisible(true);
			}
		});
	}//end main
	
	//Window listener interface methods
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		client.closeConnection();
		System.exit(0);
	}//end windowClosing method

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

}//end PaintClient class