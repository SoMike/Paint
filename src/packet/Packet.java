package packet;

/**
 * Class that holds the information that will be sent to the server and from
 * the server to the client 
 * */
public class Packet implements java.io.Serializable{

	private static final long serialVersionUID = 8078668766933884822L;
	
	private String userName = "";
	private int[][] image;
	private int imageWidth, imageHeight;
	private boolean imageState = false;
	
	/**
	 * Constructor without parameter for the Packet class
	 * */
	public Packet() {}
	
	/**
	 * Constructor with 1 parameter for the Packet class
	 * @param name The userName of the client
	 * */
	public Packet(String name) {
		this.userName = name;
	}//end Packet constructor
	
	/**
	 * Constructor with 2 parameters for the Packet class
	 * @param name The userName of the client
	 * @param image The array of pixels of the image taken from the canvas of the user
	 * */
	public Packet(String name, int[][] image) {
		this.userName = name;
		this.image = image;
	}//end Packet constructor
	
	//Setters and getters for the attributes of the Packet class
	public void setUserName(String name) {
		this.userName = name;
	}//end setUserName
	public String getUserName() {
		return userName;
	}//end getUserName
	public void setImage(int[][] image) {
		this.image = image;
	}//end setImage 
	public int[][] getImage() {
		return image;
	}//end getImage
	public void setImageHeight(int height) {
		this.imageHeight = height;
	}//end setImageHeight
	public int getImageHeight() {
		return imageHeight;
	}//end getImageHeight
	public void setImageWidth(int width) {
		this.imageWidth = width;
	}//end setImageWidth
	public int getImageWidth() {
		return imageWidth;
	}//end getImageWidth
	public void setImageState(boolean state) {
		this.imageState = state;
	}//end setImageState
	public boolean getImageState() {
		return imageState;
	}//end getImageState
}//end Packet class