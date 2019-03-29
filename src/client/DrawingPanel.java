package client;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumSet;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Class that handles the drawing on the screen
 * */
public class DrawingPanel extends Canvas{
	
	private static final long serialVersionUID = 5752906870208375983L;

	/**
	 * The possible drawing tools that the user that use.
	 * */
	private enum Tool {CURVE, LINE, RECT, OVAL, FILLED_RECT, FILLED_OVAL, SELECT, ERASE, TEXT, EMPTY};
	// the type of tools that represent the shapes, because they are handling differently because
	// they are drawn on top of the current picture
	private final static EnumSet<Tool> SHAPE_TOOLS = EnumSet.range(Tool.LINE, Tool.SELECT);
	
	/**
	 * The currently selected drawing tool. It can be changed from the comboBox inside the GUI
	 * */
	private Tool currentTool = Tool.CURVE;
	private Color currentColor = Color.BLACK;
	private Color fillColor = Color.WHITE;
	
	/**
	 *  This is variable is set to true when the mouse is dragged by the user
	 */
	private boolean dragging;
	
	private PaintClient instance;
	private int startX, startY, currentX, currentY;
	private int strokeWidth = 1;
	private int captureCounter = 0;
	/**
	 * This is the backBuffer that holds the drawn canvas
	 * */
	private BufferedImage backBuffer, image, filteredImage, selectedImage;
	private Font font;
	private String text;
	private String lineType = "Straight";
	
	/**
	 * Constructor for the DrawingPanel class
	 * @param instance The instance of the PaintClient object that this canvas is making changes to
	 * */
	public DrawingPanel(PaintClient instance){
		this.instance = instance;
		setBackground(Color.WHITE);
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
	}//end constructor for DrawingPanel class
	
	//Setters for the attributes of the class
	public void setCurrentX(int currentX) {
		this.currentX = currentX;
	}
	public void setCurrentY(int currentY) {
		this.currentY = currentY;
	}
	public void setStartX(int oldX) {
		this.startX = oldX;
	}
	public void setStartY(int oldY) {
		this.startY = oldY;
	}
	public void setCurrentTool(String tool) {
		currentTool = Tool.valueOf(tool); 
	}
	public void setCurrentColor(Color color) {
		currentColor = color;
	}
	public void setFillColor(Color color) {
		fillColor = color;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public void setText(String message) {
		text = message;
	}
	public void setStrokeWidth(int width) {
		strokeWidth = width;
	}
	public int getDrawingWidth() {
		return this.getWidth();
	}
	public void setLineType(String lineType) {
		this.lineType = new String(lineType);
	}
	public void setImage(BufferedImage srcImage) {
		image = srcImage;
	}
	public void setBackBuffer(BufferedImage srcImage) {
		backBuffer = srcImage;
	}
	
	
	//overriding the paint method of the canvas
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if(backBuffer == null) {
			createBackBuffer();
		}
		
		/* Copy the off-screen canvas to the current drawing panel.
		 * Since the image is already completely available, the ImageObsever,
		 * parameter is not needed
		 * */
		g.drawImage(backBuffer, 0, 0, null);
		
		if(dragging && SHAPE_TOOLS.contains(currentTool)) {
			g.setColor(currentColor);
			putCurrentShape(g);
		}//end if
	}//end paint method
	
	
	/**
	 * This method creates the off-screen canvas and fills it with the current 
	 * fill color.
	 * */
	private void createBackBuffer() {
		backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics osg = backBuffer.getGraphics();
		osg.setColor(fillColor);
		osg.fillRect(0,  0, getWidth(), getHeight());
		osg.dispose();
	}//end createBackBuffer method
	
	/**
	 * A utility method to draw the shape that was selected
	 * This method isn't used when current tool is Tool.Curve
	 * */
	private void putCurrentShape(Graphics g) {
		switch(currentTool) {
		case LINE:
			Graphics2D g2d = (Graphics2D) g.create();
			if(lineType.equals("Straight")) {
				g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			}else if(lineType.equals("Dotted")) {
				g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {strokeWidth, 10*strokeWidth}, 0));
			}else {
				g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {strokeWidth}, 0));
			}
			g2d.drawLine(startX, startY, currentX, currentY);
			g2d.dispose();
			break;
		case OVAL:
			putOval(g,false,startX, startY, currentX, currentY);
	        break;
	    case RECT:
	        putRect(g,false,startX, startY, currentX, currentY);
	        break;
	    case FILLED_OVAL:
	        putOval(g,true,startX, startY, currentX, currentY);
	        break;
	    case FILLED_RECT:
	        putRect(g,true,startX, startY, currentX, currentY);
	        break;
	    case SELECT:
	    	getSelected(g, startX, startY, currentX, currentY);
	    	break;
		}
	}// end putCurrentShape method
	
	/**
	 * This method draws a filled or unfilled rectangle with corners at the points (x1, y1)
	 * and (x2, y2).
	 * @param g the graphics context where the rectangle is drawn
	 * @param filled tells if the rectangle is drawn filled or unfilled
	 * */
	private void putRect(Graphics g, boolean filled, int x1, int y1, int x2, int y2) {
		 if (x1 == x2 || y1 == y2)
	         return;
	      if (x2 < x1) {  // Swap x1,x2 if necessary to make x2 > x1.
	         int temp = x1;
	         x1 = x2;
	         x2 = temp;
	      }//end if
	      if (y2 < y1) {  // Swap y1,y2 if necessary to make y2 > y1.
	         int temp = y1;
	         y1 = y2;
	         y2 = temp;
	      }//end if
	      if (filled)
	         g.fillRect(x1,y1,x2-x1,y2-y1);
	      else
	         g.drawRect(x1,y1,x2-x1,y2-y1);		
	}// end putRect method
	
	 /**
	  * Draws a filled or unfilled oval in the rectangle with corners at the 
	  * points (x1,y1) and (x2,y2).
	  * @param g the graphics context where the oval is drawn
	  * @param filled tells whether if the oval should be drawn filled or unfilled
	  */
	 private void putOval(Graphics g, boolean filled, int x1, int y1, int x2, int y2) {
	    if (x1 == x2 || y1 == y2)
	       return;
	    if (x2 < x1) {  // Swap x1,x2 if necessary to make x2 > x1.
	       int temp = x1;
	       x1 = x2;
	       x2 = temp;
	    }//end if
	    if (y2 < y1) {  // Swap y1,y2 if necessary to make y2 > y1.
	       int temp = y1;
	       y1 = y2;
	       y2 = temp;
	    }//end if
	    if (filled)
	       g.fillOval(x1,y1,x2-x1,y2-y1);
	    else
	       g.drawOval(x1,y1,x2-x1,y2-y1);
	 }//end putOval method

	 
	 /**
	  * Calls the repaint() method of this panel for the rectangle with corners
	  * at the points (x1,y1) and (x2,y2).  An extra one-pixel border is added
	  * to the area that is repainted; this allows for the size of the "pen"
	  * that is used to draw lines and unfilled ovals and rectangles.
	  */
	 private void repaintRect(int x1, int y1, int x2, int y2) {
	    if (x2 < x1) {  // Swap x1,x2 if necessary to make x2 >= x1.
	       int temp = x1;
	       x1 = x2;
	       x2 = temp;
	    }//end if
	    if (y2 < y1) {  // Swap y1,y2 if necessary to make y2 >= y1.
	       int temp = y1;
	       y1 = y2;
	       y2 = temp;
	    }//end if
	    x1--;
	    x2++;
	    y1--;
	    y2++;
	    repaint(x1,y1,x2-x1,y2-y1);
	 }// end repaintRect method
	 
	 /**
	  * This method gets the selected content from the canvas and save it as a image
	  * @param g the graphics context from where the content is extracted
	  * @param x1, y1 start positions
	  * @param x2, y2 end positions
	  * */
	 private void getSelected(Graphics g, int x1, int y1, int x2, int y2) {
		 //draw the rectangle  that will show what u selected 
		 Graphics2D g2d = (Graphics2D) g.create();
		 g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] {20, 10}, 0));
		 g2d.setColor(Color.BLACK);
		 g2d.drawRect(x1, y1, x2 - x1, y2 - y1);
		 g2d.dispose();
	 }//end getSelectedMethod
	 
	
	@Override
	public void update(Graphics g) {
		paint(g);
	}//end update method

	/**
	 * Renders the image to the canvas
	 * @param srcImage The buffered image that will be displayed on the screen
	 * @param image Boolean that if the value is true will place the image with an offset
	 * of 100 pixels from the right and 50 pixels from the top of the canvas
	 * */
	public void renderImage(BufferedImage srcImage, boolean image) {
		Graphics g = backBuffer.getGraphics();
		if(image) {
			g.drawImage(srcImage, 100, 50, null);
		}else {
			g.drawImage(srcImage, 0, 0, null);
		}//end if else
		g.dispose();
		repaint();
	}//end renderImage
	
	/**
	 * Method that filters the inserted image
	 * @param type the type of filter that is applied to the image
	 * */
	public void imageFilter(String type) {
		int width = image.getWidth();
		int height = image.getHeight();
		int p, r, g, b, avg;
		filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = filteredImage.createGraphics();
		g2d.drawImage(image, 0, 0, width, height, null);
		g2d.dispose();
		//extracting the rgb of the pixels of the image
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				p = filteredImage.getRGB(x, y);
				r = (p >> 16) & 0xff;
				g = (p >> 8) & 0xff;
				b = (p >> 0) & 0xff;
				switch(type) {
				case "Gray Scale":
					avg = (r + g + b) / 3;
					p = ((avg << 16) | (avg << 8) | avg);
					filteredImage.setRGB(x, y, p);
					break;
				case "Negative":
					r = 255 - r;
					g = 255 - g;
					b = 255 - b;
					p = ((r << 16) | (g << 8) | b);
					filteredImage.setRGB(x, y, p);
					break;
				case "Red Filter":
					g = 0;
					b = 0;
					p = ((r << 16) | (g << 8) | b);
					filteredImage.setRGB(x, y, p);
					break;
				case "Green Filter":
					r = 0;
					b = 0;
					p = ((r << 16) | (g << 8) | b);
					filteredImage.setRGB(x, y, p);
					break;
				case "Blue Filter":
					r = 0;
					g = 0;
					p = ((r << 16) | (g << 8) | b);
					filteredImage.setRGB(x, y, p);
					break; 
				}//end switch
			}//end for
		}//end for
		//render the filtered image to the canvas
		renderImage(filteredImage, true);
	}//end imageFilter method
	
	/**
	 * Method that clears the canvas
	 * */
	public void clearScreen() {
		Graphics g = backBuffer.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.dispose();
		repaint();
	}//end clearScreen method
	
	
/**
 * Class that controls the mouse actions
 * */
private class MouseHandler implements MouseListener, MouseMotionListener {
	      
	  int prevX, prevY;  // Previous position of mouse during a drag.
	      
	  /**
	  * When the ERASE tool is used and the mouse jumps
	  * from (x1,y1) to (x2,y2), the tool has to be applied to a
	  * line of pixel positions between the two points in order to
	  * cover the entire line that the mouse moves along.  The change
	  * is made to the off-screen canvas, and repaint() is called to
	  * copy the changes to the screen.
	  */
	  void applyToolAlongLine(int x1, int y1, int x2, int y2) {
	     Graphics g = backBuffer.getGraphics();
	     g.setColor(fillColor);    // (for ERASE only)
	     int dist = Math.max(Math.abs(x2-x1),Math.abs(y2-y1));
	        // dist is the number of points along the line from
	        // (x1,y1) to (x2,y2) at which the tool will be applied.
	     double dx = (double)(x2-x1)/dist;
	     double dy = (double)(y2-y1)/dist;
	     for (int d = 1; d <= dist; d++) {
	            // Apply the tool at one of the points (x,y) along the
	            // line from (x1,y1) to (x2,y2).
	        int x = (int)Math.round(x1 + dx*d);
	        int y = (int)Math.round(y1 + dy*d);
	        if (currentTool == Tool.ERASE) {
	               // Erase a  block of pixels around (x,y) with the width of strokeWidth
	           g.fillRect(x-(strokeWidth)/2, y-(strokeWidth)/2, strokeWidth, strokeWidth);
	           repaint(x-(strokeWidth)/2, y-(strokeWidth)/2, strokeWidth, strokeWidth);
	        }//end if
	     }//end for
	    g.dispose();
	  }//end applyToolAlongLine method
	
	
	   /**
	   * Start a drag operation.
	   */
	   public void mousePressed(MouseEvent evt) {
	       startX = prevX = currentX = evt.getX();
	       startY = prevY = currentY = evt.getY();
	       dragging = true;
	       if (currentTool == Tool.ERASE) {
	             // Erase a 10-by-10 block around the starting mouse position.
	          Graphics g = backBuffer.getGraphics();
	          g.setColor(fillColor);
	          g.fillRect(startX-5,startY-5,10,10);
	          g.dispose();
	          repaint(startX-5,startY-5,10,10);
	       }//end if
	       if(currentTool == Tool.TEXT) {
	    	   if(text == null) {
	    		   return;
	    	   }//end if
	    	   Graphics g = backBuffer.getGraphics();
	    	   g.setColor(currentColor);
	    	   g.setFont(font);
	    	   FontMetrics fm = g.getFontMetrics();
	    	   StringBuffer wrapText = new StringBuffer();
	    	   int counter = 1;
	    	   if(startX + fm.stringWidth(text) >= instance.getWidth()) {
	    		   if(text.contains(" ")) {
	    			   StringTokenizer tokens = new StringTokenizer(text, " ");
	    			   while(tokens.hasMoreTokens()) {
	    				   String element = tokens.nextToken(); 
	    				   String test = new String(wrapText);
	    				   if(startX + fm.stringWidth(element) + fm.stringWidth(test) >= instance.getWidth()) {
	    					   wrapText.append("\n").append(element);
	    				   }else {
	    					   wrapText.append(element).append(" ");   
	    				   }//end if else
	    			   }//end while
	    			   tokens = new StringTokenizer(new String(wrapText), "\n");
	    			   g.drawString(tokens.nextToken(), startX, startY);
	    			   while(tokens.hasMoreElements()) {
	    				   g.drawString(tokens.nextToken(), startX, startY + counter*fm.getAscent() + counter*fm.getDescent());
	    				   counter++;
	    			   }//end while
	    		   }else {
	    			   for(int i = 0; i < text.length(); i++) {
	    				   char c = text.charAt(i);
	    				   wrapText.append(c);
	    				   String test = new String(wrapText);
	    				   if(startX + fm.stringWidth(test) >= instance.getWidth()) {
	    					  wrapText.append("\n"); 
	    				   }//end if
	    			   }//end for
	    			   StringTokenizer tokenz = new StringTokenizer(new String(wrapText), "\n");
	    			   while(tokenz.hasMoreElements()) {
	    				   g.drawString(tokenz.nextToken(), startX, startY + counter*fm.getAscent() + counter*fm.getDescent());
	    				   counter++;
	    			   }//end while
	    		   }//end if else
	    	   }else {
	    		   g.drawString(text, startX, startY);   
	    	   }//end if else
	    	   g.dispose();
	    	   repaint();
	       }//end if
	    }//end mousePressed method
	      
	    /**
	     * Continue a drag operation when the user drags the mouse.
	     * For the CURVE tool, a line is drawn from the previous mouse
	     * position to the current mouse position in the off-screen canvas,
	     * and the repaint() method is called for a rectangle that contains
	     * the line segment that was drawn.  For shape tools, the off-screen
	     * canvas is not changed, but the repaint() method is called so
	     * that the paintComponent() method can redraw the picture with
	     * the user's shape in the new position.  For the 
	     * ERASE tools, the tool is applied along a line from the previous
	     * mouse position to the current position;
	     */
	    public void mouseDragged(MouseEvent evt) {
	       currentX = evt.getX();
	       currentY = evt.getY();
	       if (currentTool == Tool.CURVE) {
	          Graphics g = backBuffer.getGraphics();
	          Graphics2D g2d = (Graphics2D) g.create();
	          g2d.setColor(currentColor);
	          g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	          g2d.drawLine(prevX,prevY,currentX,currentY);
	          g.dispose();
	          repaintRect(prevX,prevY,currentX,currentY);
	       }
	       else if (SHAPE_TOOLS.contains(currentTool)) {
	               // Repaint the rectangles occupied by the previous position of
	               // the shape and by its current position.
	    	   //if the tool is SELECT then don't repaint because it is not necessary
//	    	   if(currentTool == Tool.SELECT) {
//	    		   return;
//	    	   }//end if
	          repaintRect(startX,startY,prevX,prevY);
	          repaintRect(startX,startY,currentX,currentY);
	       }
	       else {
	             // Tool has to be ERASE 
	          applyToolAlongLine(prevX,prevY,currentX,currentY);
	       }
	       prevX = currentX;
	       prevY = currentY;
	    }

	    /**
	     * Finish a mouse drag operation.  Nothing is done unless the current tool
	     * is a shape tool.  For shape tools, the user's shape is drawn to the
	     * off-screen canvas, making it a permanent part of the picture, and
	     * then the repaint() method is called to show the modified picture
	     * on the screen.
	     */
	    public void mouseReleased(MouseEvent evt) {
	       dragging = false;
	       if (SHAPE_TOOLS.contains(currentTool)) {
	          Graphics g = backBuffer.getGraphics();
	          g.setColor(currentColor);
	          putCurrentShape(g);
	          //if the current tool is the SELECT one, then don't repaint and save that file with the capture
	          if(currentTool == Tool.SELECT) {
	        	  int width = currentX - startX - 4;
	        	  int height = currentY - startY - 1;
	        	  selectedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	     		 Graphics2D g2dIM = selectedImage.createGraphics();
	     		 g2dIM.setColor(Color.BLACK);
	     		 g2dIM.fillRect(0, 0, width, height);
	     		 g2dIM.dispose();
	     		 //extract all the pixel in the selected area from the back buffer and form the image
	     		 for(int x = startX + 4; x < currentX; x++) {
	     			 for(int y = startY + 1; y < currentY; y++) {
	     				 selectedImage.setRGB(x - startX - 4, y - startY - 1, backBuffer.getRGB(x, y));
	     			 }//end for
	     		 }//end for
	        	  String name = JOptionPane.showInputDialog("Enter the name of the capture:");
					try {
						ImageIO.write(selectedImage, "png", new File(name + captureCounter + "." + "png"));
						captureCounter++;
					}catch(Exception ex) {
						ex.printStackTrace();
					}//end try catch
	          }//end if
	          g.dispose();
	          repaint();
	       }//end if 
	    }//end mouseReleased
	      
	    //MouseListner, MouseMotionListener interfaces methods
	      public void mouseMoved(MouseEvent evt) { }
	      public void mouseClicked(MouseEvent evt) { }
	      public void mouseEntered(MouseEvent evt) { }
	      public void mouseExited(MouseEvent evt) { }
	}//end MouseHandler class
}
