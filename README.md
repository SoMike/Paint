#Paint

## Concept
	The app is designed to allow the user to use a canvas freely for drawing 
	and even sharing it to other users that are online.

## Implementation
	The app was implemented using Java Swing library.

## Updates
1. version 1.01
	* added the option of modifing the image added on the canvas by adding different filters
	* added the option of clearing the entire canvas
2. version 1.02
	* added more options on the menu bar of the application
	* changed the logic of the client-server 
	* should add more options for the menu bar and more predefined shapes for drawing, and later the option to change the layout of the GUI

## Bugs
* after the 1.02 version, found a bug on the sharing side of the application, where the output stream of the server is locked to only the user that sent the share request to the server, blocking the other users; working on the fix now