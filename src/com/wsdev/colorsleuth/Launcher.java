package com.wsdev.colorsleuth;
import java.lang.Math; 
import java.util.HashMap; 
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox; 
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane; 
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;  
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.application.Platform;

public class Launcher {
	public static void main(String[] args) {
  		Main.appRunning = true;
    	Application.launch(Main.class, args);
    	Main.appRunning = false; 
	}

	// Main class extending javafx Application class in order to create window
	public static class Main extends Application 
	{ 
		
   		public final HashMap<String, Scene> screens = new HashMap<String, Scene>();
   		public Stage primaryStage;
   		public static boolean appRunning;
		
		
  		@Override 
  		public void start(Stage pStage) {
  			primaryStage = pStage; 
  			
  			// create scenes/screens
  			final PlayScreen playScreen = new PlayScreen(this, 500,500);
  			final EndScreen  endScreen  = new EndScreen(this, 500, 500); 
  			
  			screens.put("playScreen", playScreen);
  			screens.put("endScreen", endScreen);
  			
  			// initialize screens ui
  			playScreen.initializeDocument();
  			endScreen.initializeDocument(); 
  			
  			
  	 		
   		// set title and default screen/scene
  			primaryStage.setTitle("Color Sleuth!");
    		primaryStage.setScene(playScreen);
    		primaryStage.show();
  		}
			
		} 
}

// interface (blueprints) for screen classes
interface GameScreen  {
    Launcher.Main app = null; 
	AnchorPane root = null;
    void initializeDocument();
}

// default screen
class PlayScreen extends Scene implements GameScreen {
  private final Launcher.Main app; 
  private final AnchorPane root;
  private final BorderPane bpane;
  
  private int diffCount; 
  private int red, green, blue;
  private StringProperty playerOnePointsValue, playerTwoPointsValue;
  
  public boolean currentPlayer; // player 1 = false, player 2 = true
  public Thread timerThread;
  public int playerOnePoints, playerTwoPoints; 
  
  // screen elements
  private Label timer;
  private Integer maxTime, currentTime;
  private Label playerOne, playerOneScore, playerTwo, playerTwoScore;
  private VBox playerOneContainer, playerTwoContainer; 
  private FlowPane buttonContainer;
  private GameButton buttonOne, buttonTwo, buttonThree, buttonFour;
  
  
  public PlayScreen(Launcher.Main _app, int width, int height) {
  	super(new AnchorPane(), 300, 300);
  	app 				= _app; 
  	root  				= (AnchorPane)getRoot(); 
  	currentPlayer   	= false; 
	playerOnePoints 	= 0;
	playerTwoPoints 	= 0; 
    diffCount      		 = 200; 
    maxTime              = 20;
    currentTime    		 = maxTime;
    playerOnePointsValue = new SimpleStringProperty();
    playerTwoPointsValue = new SimpleStringProperty();
    bpane                = new BorderPane();
    playerOnePointsValue.set("0");
    playerTwoPointsValue.set("0");
  }
  
  public void initializeDocument() {

  	root.setStyle("-fx-background-color: rgb(93, 183, 247)");
	timer = new Label("time left: 20s");
	timer.setStyle("-fx-font-size: 20;-fx-font-weight: bold; -fx-text-fill: rgb(247, 189, 93);");
	playerOneContainer = new VBox();
	playerTwoContainer = new VBox();
	buttonContainer    = new FlowPane(); 
	
	
	addPlayerBoxLabels();
	setPlayerBoxesStyling(); 
	setCurrentPlayer();
	
    buttonContainer.setHgap(10);
    buttonContainer.setVgap(10);
	buttonContainer.setMinWidth(100); 
	buttonContainer.setAlignment(Pos.CENTER);
    

    // initialize the game buttons, generate their colors, display them, and set their styles
	addButtons(); 
	generateRandomColors();
    displayButtons(); 
    setButtonsAndContainerStyling(); 
    
    addTimerThread();
		
	root.setLeftAnchor(bpane,0.00);
	root.setTopAnchor(bpane, 0.00);
	bpane.setCenter(buttonContainer);
	bpane.prefWidthProperty().bind(widthProperty());
	bpane.prefHeightProperty().bind(heightProperty());
	
	root.setTopAnchor(timer,0.00);
	root.setLeftAnchor(timer,0.00);
	root.setLeftAnchor(playerOneContainer, 5.00);
	root.setBottomAnchor(playerOneContainer, 5.00);
	root.setRightAnchor(playerTwoContainer, 5.00);
	root.setBottomAnchor(playerTwoContainer, 5.00);
	root.getChildren().addAll(playerOneContainer, playerTwoContainer, bpane, timer);

  
  } 
  
  // creats a thread to subtract current time by one every second
  // controls the countdown
  private void addTimerThread() {
    Label timeLabel = timer; 

    
  	timerThread = new Thread(()->{
  		
  		// while the game is running, count down
  		while (app.appRunning) {
    			
    			// runs on FX thread because some variables can only be changed on it
    			Platform.runLater(()->{
  						if (currentTime.intValue() > 0) {
  							currentTime-=1;
  						} else {
  							currentTime = maxTime; 
  						    addPoints(-1);
  						    resetColors();
  						    setCurrentPlayer();
  						}
  			   		 	timer.setText("time left: "+String.valueOf(currentTime.intValue()));
    			});
  			    
  				try {
  					Thread.sleep(1000);
  				} catch (Exception e) {
  					break;
  				}
  				
  		}
  	});
  	
  	// thread has a low priority
  	timerThread.setDaemon(true); 
  	timerThread.start();
  }
  
  // add labels to player containers (scores and player names)
  private void addPlayerBoxLabels() {
    playerOne = new Label("player one");
	playerTwo = new Label("player two");
	playerOneScore = new Label("score: 0");
	playerTwoScore = new Label("score: 0");
  	
  	playerOneContainer.getChildren().addAll(playerOne, playerOneScore);
	playerTwoContainer.getChildren().addAll(playerTwo, playerTwoScore);
	
    playerOneScore.textProperty().bind(
    	new SimpleStringProperty("score: ")
    	    .concat(playerOnePointsValue)
    );
    
    playerTwoScore.textProperty().bind(
    	new SimpleStringProperty("score: ")
    	    .concat(playerTwoPointsValue)
    );
    
    
    
  }
  
  // sets the sizing properties for the buttons and their container (FlowPane)
  private void setButtonsAndContainerStyling() {
		
		buttonContainer.minWidthProperty().bind(heightProperty().divide(2.2));
		buttonContainer.maxWidthProperty().bind(heightProperty().divide(2.2));

		
		buttonOne.prefWidthProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonOne.prefHeightProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonTwo.prefWidthProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonTwo.prefHeightProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonThree.prefWidthProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonThree.prefHeightProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonFour.prefWidthProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		buttonFour.prefHeightProperty().bind(buttonContainer.maxWidthProperty().divide(3));
		
	}
	
	// sets the default styling of the player boxes
	private void setPlayerBoxesStyling() {
		playerOneContainer.setMinWidth(100);
		playerOneContainer.setMinHeight(100);
		playerOneContainer.prefWidthProperty().bind(heightProperty().divide(4));
		playerOneContainer.prefHeightProperty().bind(heightProperty().divide(4));
		playerOneContainer.setStyle("-fx-border-color: black; -fx-border-width:6; -fx-border-style: solid;");
	
		playerTwoContainer.setMinWidth(100);
		playerTwoContainer.setMinHeight(100);
		playerTwoContainer.prefWidthProperty().bind(heightProperty().divide(4));
		playerTwoContainer.prefHeightProperty().bind(heightProperty().divide(4));
		playerTwoContainer.setStyle("-fx-border-color: black; -fx-border-width:6; -fx-border-style: solid;");
	
		// alignment of containers elements
		playerOneContainer.setAlignment(Pos.CENTER);
		playerTwoContainer.setAlignment(Pos.CENTER);
	
	}

	// generates the starting random colors
	private void generateRandomColors() {
		red       = (int)Math.round(Math.random()*255);
		green     = (int)Math.round(Math.random()*255); 
		blue      = (int)Math.round(Math.random()*255);
	};
	
	// initialize basic buttons
	// could be shortened with a loop but meh
	private void addButtons() {
		
		  
		  buttonOne =  new GameButton("A");
		  buttonTwo =  new GameButton("B");
		  buttonThree =  new GameButton("C");
		  buttonFour =  new GameButton("D");
		 
		  buttonOne.addListeners(this); 
		  buttonTwo.addListeners(this); 
		  buttonThree.addListeners(this); 
		  buttonFour.addListeners(this); 
	}

	// display buttons on the screen and initialize the shades
	private void displayButtons() {

		resetColors();
		
	    buttonContainer.getChildren().add(buttonOne);
		buttonContainer.getChildren().add(buttonTwo);
		buttonContainer.getChildren().add(buttonThree);
		buttonContainer.getChildren().add(buttonFour);
		
	}

	// resets all buttons colors
	public void resetColors() {
		generateRandomColors();
		int   outBtn     = (int)(Math.random()*3)+1; 
		
		// more of 1 equation in the list, more likely it is chosen
		// difficulty curve is highhh
		int[]    diffRGB    = {
			0-diffCount+10,
			0-diffCount+10,
			0-diffCount+10,
		    0-diffCount+10, 
		    0-diffCount+10,
		    0-diffCount+10,
		    diffCount+10, 
		    (int)Math.random()*240+10, 
		    (diffCount+(int)Math.random()*240)%255+10, 
		    Math.abs(diffCount-(int)Math.random()*240)%255+10
		 }; 
		 
		int  diffR, diffG, diffB;
		
		// generates new colors
		diffR = (int)Math.round(Math.random()*9);
		diffG = (int)Math.round(Math.random()*9);
		diffB = (int)Math.round(Math.random()*9);
		
	     // sets each buttons color
	     buttonOne.changeColor((red)%255, (green)%255, (blue)%255, false);
	     buttonTwo.changeColor((red)%255, (green)%255, (blue)%255, false);
	     buttonThree.changeColor((red)%255, (green)%255, (blue)%255, false);
	     buttonFour.changeColor((red)%255, (green)%255, (blue)%255, false);
	   
	    // code could be shortened, but meh, good enough
	    // sets the correct buttons color
	    switch (outBtn) {
	    	 case 1: 
	   		 	buttonOne
	   		 		.changeColor((red+(diffRGB[diffR]))%255, (green+diffRGB[diffG])%255, (blue+diffRGB[diffB])%255, true);
	         		break;
	         case 2: 
	            buttonTwo
	   		 		.changeColor((red+(diffRGB[diffR]))%255, (green+diffRGB[diffG])%255, (blue+diffRGB[diffB])%255, true);
	   		 	break;
	   		 case 3: 
	   		 	buttonThree
	   		 		.changeColor((red+(diffRGB[diffR]))%255, (green+diffRGB[diffG])%255, (blue+diffRGB[diffB])%255, true);
	   			break;
	   		 case 4: 
	   			buttonFour
	   		 		.changeColor((red+(diffRGB[diffR]))%255, (green+diffRGB[diffG])%255, (blue+diffRGB[diffB])%255, true);
	   		 	break; 
	   		 default: 
	   		    break;
	     }
	  
	}
	
	// returns diffCount
	public int getDiff() 
	{
		return diffCount; 
	}
	
	// subtracts from diffcount
	public void subtractDiff() {
		diffCount-=5;
	}
	
	
	// adds a point to the current players score
	public void addPoints() {
		if (currentPlayer) {
			playerOnePoints++; 
		} else {
			playerTwoPoints++; 
		}
		updatePoints();
		if (playerOnePoints < playerTwoPoints-3 || playerTwoPoints < playerOnePoints-3 || playerOnePoints < -3 || playerTwoPoints < -3) {
			gameOver();
   	 
		}
	}
	
	// adds x points to the current players score (overloaded method)
	public void addPoints(int toAdd) {

		
		if (currentPlayer) {
			playerOnePoints+=toAdd; 
		} else {
			playerTwoPoints+=toAdd; 
		}
		updatePoints();
		if (playerOnePoints < playerTwoPoints-3 || playerTwoPoints < playerOnePoints-3 || playerOnePoints < -3 || playerTwoPoints < -3) {
			
			gameOver();
		}
	}
	
    // returns points for the current player
	public int getPoints() {
		if (currentPlayer) {
			return playerOnePoints;
		} else {
			return playerTwoPoints;
		}
	}
	
	// switches to game over screen
	public void gameOver() {
		    
		    // sets app to endscreen
			app.primaryStage.setScene(app.screens.get("endScreen"));
			app.primaryStage.setWidth(getWidth());
			app.primaryStage.setHeight(getHeight());
			app.appRunning = false; 
			
			// resets the final message
			((EndScreen)app.screens.get("endScreen")).finalText =  playerOnePoints>playerTwoPoints ? "Game Over! Player one wins with a score of "+String.valueOf(playerOnePoints)+"!" : "Game Over! Player two wins with score of "+String.valueOf(playerTwoPoints)+"!";
   		    ((EndScreen)app.screens.get("endScreen")).finalText += playerOnePoints<playerTwoPoints-3 ? " Player one fell more than 3 points from the winners score!" : "";
   			((EndScreen)app.screens.get("endScreen")).finalText += playerTwoPoints<playerOnePoints-3 ? " Player two fell more than 3 points from the winners score!" : "";
   			((EndScreen)app.screens.get("endScreen")).finalText += playerOnePoints < -3 ? " Player one lost because they fell below -3 points!" : "";
   			((EndScreen)app.screens.get("endScreen")).finalText += playerTwoPoints < -3 ? " Player two lost because they fell below -3 points!" : "";
						
   			((EndScreen)app.screens.get("endScreen")).gameOverText.setText(((EndScreen)app.screens.get("endScreen")).finalText);
   			
	}
	
	// adds to the max time
	public void addMaxTime(int time) {
		maxTime+=time;
	}
	
	// resets current time to max time
	public void resetTime() {
		currentTime=maxTime;
	}
	
	// returns the max time
	public int getMaxTime() {
		return maxTime;
	}
	
    // updates point label values
	public void updatePoints() {
		playerOnePointsValue.set(
			String.valueOf(playerOnePoints)
		);
		playerTwoPointsValue.set(
			String.valueOf(playerTwoPoints)
		);
	}
	
	// changes the current player to the next player
	// also changes the styling of player containers to indicate whos turn it is
	public void setCurrentPlayer() {
		currentPlayer = !currentPlayer; 
		if (currentPlayer) {
			playerOne.setStyle("-fx-text-fill: rgb(247, 189, 93); -fx-font-weight: bolder;");
			playerTwo.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
			playerOneContainer.setStyle("-fx-border-color: rgb(247, 189, 93); -fx-border-width:6; -fx-border-style: solid;");
			playerTwoContainer.setStyle("-fx-border-color: black; -fx-border-width:6; -fx-border-style: solid;");
			root.setLeftAnchor(playerOneContainer, 15.00);
	        root.setBottomAnchor(playerOneContainer, 15.00);
	        root.setRightAnchor(playerTwoContainer, 5.00);
	        root.setBottomAnchor(playerTwoContainer, 5.00);
		} else {
			playerOne.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
			playerTwo.setStyle("-fx-text-fill: rgb(247, 189, 93); -fx-font-weight: bolder;");
			playerOneContainer.setStyle("-fx-border-color: black; -fx-border-width:6; -fx-border-style: solid;");
			playerTwoContainer.setStyle("-fx-border-color: rgb(247, 189, 93); -fx-border-width:6; -fx-border-style: solid;");
			root.setLeftAnchor(playerOneContainer, 5.00);
			root.setBottomAnchor(playerOneContainer, 5.00);
			root.setRightAnchor(playerTwoContainer, 15.00);
			root.setBottomAnchor(playerTwoContainer, 15.00);
		}
	}
	
	// resets the game
	public void reset() {
	   currentPlayer = true; 
	   playerOnePoints = 0;
	   playerTwoPoints = 0; 
       diffCount = 200; 
       playerOnePointsValue.set("0");
       playerTwoPointsValue.set("0");
       maxTime = 20;
       currentTime = maxTime;
       app.appRunning = true; 
       addTimerThread();
       resetColors();
	}
	

} 

class EndScreen extends Scene implements GameScreen {
   private final Launcher.Main app; 
   private final AnchorPane root;
   private final BorderPane bpane;
   
   public String finalText;
   
   private Button playAgainButton;
   public  Label gameOverText;
   
   public EndScreen(Launcher.Main _app, int width, int height) {
   	super(new AnchorPane(), width, height);
   	root  = (AnchorPane)getRoot();
   	bpane = new BorderPane();
   	app   = _app; 
   }
   
   public void initializeDocument() {
   	 root.setStyle("-fx-background-color: rgb(93, 183, 247)");
   	 
	 playAgainButton = new Button("PLAY AGAIN!");
   	 gameOverText    = new Label(finalText);
   	 
   	 addEventHandlers();
   	 
   	 root.setLeftAnchor(bpane, 0.00);
   	 root.setTopAnchor(bpane, 0.00);
   	 bpane.setCenter(gameOverText);
   	 bpane.setBottom(playAgainButton);
   	 bpane.setAlignment(playAgainButton, Pos.BOTTOM_CENTER);
   	 bpane.setMargin(playAgainButton, new Insets(0,0,5,0));
   	 bpane.prefWidthProperty().bind(widthProperty());
   	 bpane.prefHeightProperty().bind(heightProperty());
   	 
   	 root.getChildren().addAll(bpane);
   }
   
   private void addEventHandlers() {
   	
   	playAgainButton.setOnAction(
   		new EventHandler<ActionEvent>() {
			 @Override
			 public void handle(ActionEvent ev) {
			 	((PlayScreen)app.screens.get("playScreen")).reset(); 
			 	app.primaryStage.setScene(app.screens.get("playScreen"));
			 }
   		}		 	
    );
    
   }
   
}

// simple Button subclass that allows easier change of the color
class GameButton extends Button {
	public boolean   outBtn; 
	
	GameButton(String label) {
		super(label); 
		outBtn = false; 
		
	}
    
    // changes the color of the button
	public void changeColor(int _red, int _green, int _blue, boolean out) {
 		outBtn = out;
 		
 		// math.abs because color cannot be negative
 		_red = Math.abs(_red);
 		_green = Math.abs(_green);
 		_blue = Math.abs(_blue);
		setStyle("-fx-border-radius: 0px; -fx-background-radius: 0px; -fx-background-color: rgb(" + _red + "," + _green + "," + _blue + ");"); 
	}

	// adds Onclick listeners
	public void addListeners(PlayScreen scope) {
	   setOnAction(new EventHandler<ActionEvent>() {
			 @Override
			 public void handle(ActionEvent ev) {
				 // adds a point if it is the correct button
				 if (outBtn==true) {
				 	if (scope.getDiff()>15) scope.subtractDiff();
				 	if (scope.getPoints()%5==0 && scope.getPoints()!=0 && scope.getMaxTime()>5) scope.addMaxTime(-1); 
				 	scope.addPoints();
				 	scope.resetTime();
				 } else {
				 	scope.addPoints(-1);
				 }
				 scope.resetColors(); 
				 scope.setCurrentPlayer();
				 
			 }
		 }); 
	}
	
}


