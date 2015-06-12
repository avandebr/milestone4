package cs211.tangiblegame.gui;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.video.Capture;
import processing.video.Movie;
import cs211.tangiblegame.imageprocessing.ImageProcessing;
//import ddf.minim.AudioPlayer;
//import ddf.minim.Minim;

@SuppressWarnings("serial")
public class TangibleGame extends PApplet {

	protected final static int WIDTH = 800;
	protected final static int HEIGHT = WIDTH;
	private final float rWindow = (float)HEIGHT/(float)WIDTH;

	private final static float DIM_BOX_X = 300*WIDTH/800;
	private final static float DIM_BOX_Y = HEIGHT/60;
	private final static float DIM_BOX_Z = 300*HEIGHT/800;
	private final static float rBox = DIM_BOX_X/DIM_BOX_Z;

	private final float DIM_BORDER_X = 0;
	private final float DIM_BORDER_Y = 3*DIM_BOX_Y;
	private final float DIM_BORDER_Z = DIM_BOX_Z;

	private float angleX = 0;
	private float angleY = 0;
	private float angleZ = 0;

	private final float g = 0.0981f;
	private final float eps = 0.1f;

	final static float SPHERE_RADIUS = 10*DIM_BOX_Z/300;
	private PVector sphereLocation = new PVector(0, -SPHERE_RADIUS-DIM_BOX_Y/2, 0);
	private PVector sphereSpeed = new PVector(0, 0, 0);

	private final PVector cheatLeft = new PVector(eps * SPHERE_RADIUS, 0, 0);
	private final PVector cheatRight = new PVector(-eps * SPHERE_RADIUS, 0, 0);
	private final PVector cheatDown = new PVector(0, 0, eps * SPHERE_RADIUS);
	private final PVector cheatUp = new PVector(0, 0, -eps * SPHERE_RADIUS);

	private final float ANGLE_VARIATION = PI/60;
	private float coefWheel = 1;
	private final float REBOUND_COEFF = 0.95f;
	private final float mu = 0.995f; 

	private final float CYLINDER_BASE_SIZE = Cylinder.CYLINDER_BASE_SIZE;
	private ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();
	private ArrayList<PVector> calibrationCorners = new ArrayList<PVector>();
	private PShape mushroom;
	private PShape blueMushroom;
	private PShape redMushroom;

	private Random random = new Random();
	private int nbRound = 500;
	private int n = 0;
	private int TAILLE = 3*HEIGHT/4;
	private char nb = '3';
	private boolean webcam = true;
	private boolean play = false;
	private boolean willBegin = false;
	private boolean start = false;
	private boolean choseMode = false;
	private boolean changeMode = true;
	private boolean pause = false;
	private boolean calibration = false;
	private boolean commands = false;
	private boolean modeNormal = false;
	private boolean modeFlipper = false;

	private final float SCALE_X = WIDTH / (6 * DIM_BOX_X);
	private final float SCALE_Z = HEIGHT / (6 * DIM_BOX_Z);

	private Capture cam;
	private Movie movie;
	
	/*private Minim minim;
	private AudioPlayer sound;
	private AudioPlayer musicFlipper;
	private AudioPlayer musicMario;*/
	
	// ========================
	private Movie sound;
	private Movie musicFlipper;
	private Movie musicMario;
	// ========================
		
	private PImage img;
	private PImage fondMenu;
	private PImage vegas;
	private PImage champis;
	private PImage fondNormal;
	private PImage fondFlipper;
	private ImageProcessing ip;

	private PFont policeMenu;
	private PFont avenir;
	private PGraphics menu;
	private PGraphics menu2;
	private PGraphics endMessage;
	private PGraphics mySurface;
	private PGraphics topView;
	private PGraphics scoreBoard;
	private PGraphics barChart;
	private HScrollbar hs;

	private float lastScore = 0;
	private float score = 0;
	private float scoreMax = 100;
	private ArrayList<Float> scoreGraph = new ArrayList<Float>();
	private int counter = 0;
	private float nbRect = 0;
	private float rectScoreValue;
	private final float DIM_RECT = HEIGHT/160;
	private final float ECART = 1;
	private final int NB_RECT_MAX_X = (int)((WIDTH -4*WIDTH/60 - HEIGHT/6*(0.75f + rBox))/(DIM_RECT+ECART));
	private final int NB_RECT_MAX_Y = (int)((HEIGHT/8)/(DIM_RECT+ECART));

	public void setup() {

		size(WIDTH, HEIGHT, P3D);

		ip = new ImageProcessing(this);
		
		String[] cameras = Capture.list();
		cam = new Capture(this, cameras[0]);
		cam.start();
		
		movie = new Movie(this, "testvideo.mp4");
		movie.loop();
		img = movie.get();
		
		/*minim = new Minim(this);
		sound = minim.loadFile("pinballSound.mp3");
		musicFlipper = minim.loadFile("machineSous.mp3");
		musicMario = minim.loadFile("MarioBros.mp3");*/
		
		// ==============================================
		sound = new Movie(this, "pinballSound.mp4");
		musicFlipper = new Movie(this, "machineSous.mp4");
		musicMario = new Movie(this, "MarioBros.mp4");
		// ==============================================

		mySurface = createGraphics(WIDTH, HEIGHT/5, P2D);
		topView = createGraphics((int)(rBox * HEIGHT/6), HEIGHT/6, P2D);
		scoreBoard = createGraphics((3*HEIGHT/4)/6, HEIGHT/6, P2D);
		barChart = createGraphics((int)(WIDTH -4*WIDTH/60 - HEIGHT/6*(0.75f + rBox)), HEIGHT/8, P2D);
		menu = createGraphics(WIDTH, HEIGHT);
		menu2 = createGraphics(WIDTH, HEIGHT);
		endMessage = createGraphics(WIDTH/2, HEIGHT/5);

		final float longueur = WIDTH -4*WIDTH/60 - HEIGHT/6*(0.75f + rBox);
		final float epaisseur = HEIGHT/60;
		final float debutX = 3*WIDTH/60 + (HEIGHT/6)*(0.75f+rBox);
		final float debutZ = HEIGHT - (HEIGHT/60 + epaisseur + 5);
		hs = new HScrollbar(debutX, debutZ, longueur, epaisseur, this); 

		mushroom = loadShape("mushroom.obj");
		mushroom.scale(10*DIM_BOX_Z/300);
		blueMushroom = loadShape("blueMushroom.obj");
		redMushroom = loadShape("redMushroom.obj");
		blueMushroom.scale(15*DIM_BOX_Z/300);
		redMushroom.scale(15*DIM_BOX_Z/300);

		policeMenu = createFont("Harrington.ttf", HEIGHT/10);
		avenir = createFont("Avenir.ttc", HEIGHT/50);
		fondMenu = loadImage("Champignons.jpg");
		fondMenu.resize(WIDTH, HEIGHT);

		fondNormal = loadImage("mushrooms.jpg");
		fondNormal.resize((int)(WIDTH*1.22), (int)(HEIGHT*1.22));
		fondFlipper = loadImage("pinball.jpg");
		fondFlipper.resize((int)(fondFlipper.width*1.1), (int)(fondFlipper.height*1.1));

		vegas = loadImage("lasVegas.jpg");
		vegas.resize(WIDTH, HEIGHT/2);
		champis = loadImage("champiMario.jpg");
		champis.resize(WIDTH, HEIGHT/2);
	}

	/* ========================================================================================================== 
	Methods to draw elements of the game and its interface
 	=========================================================================================================== */
		
	public void draw() {

		if(play){

			// ======= FOR WEBCAM ========
			/*if (webcam && cam.available()) {
				cam.read();
				img = cam.get();
			}*/
			// =========================== 

			// ======= FOR MOVIE =========
			if(webcam && !pause && !willBegin) {
				movie.read();
				img = movie.get();
			}
			// ===========================

			if(!calibration) {
				if(willBegin) {
					drawInterface();
					countDown();
				}
				else drawGame();
			}
			else calibrate();
		}
		else if(choseMode) drawMenu2();
		else drawMenu();
		if(changeMode && !play){
			drawChangeMessage();
		}
	}

	// draw the main menu 
	public void drawMenu(){

		menu.beginDraw();
		menu.image(fondMenu, 0, 0);
		menu.fill(0);
		menu.textFont(policeMenu);
		menu.text("Welcome in\nWonderland", WIDTH/4, HEIGHT/8, WIDTH/2+WIDTH/8, HEIGHT/2);
		if(commands && (mouseX < WIDTH/5 || mouseX > 4*WIDTH/5 || mouseY < HEIGHT/2 || mouseY > 9*HEIGHT/10+15)){
			menu.fill(200, 100);
			menu.rect(0, 0, WIDTH, HEIGHT);
		}
		if(!commands){
			menu.fill(255);
			if(mouseX > WIDTH/3 && mouseX < 2*WIDTH/3 && mouseY > HEIGHT/2 && mouseY < HEIGHT/2+WIDTH/20+20){
				menu.fill(200);
				menu.rect(WIDTH/3-2.5f, HEIGHT/2-2.5f, WIDTH/3+5, HEIGHT/20+25);
			}
			else {
				menu.fill(255);
				menu.rect(WIDTH/3, HEIGHT/2, WIDTH/3, HEIGHT/20+20);
			}
			menu.fill(0);
			menu.textSize(WIDTH/20);
			menu.text("P L A Y", WIDTH/2-WIDTH/8+20, HEIGHT/2+5, WIDTH/2, HEIGHT/8);
			if(mouseX > WIDTH/3 && mouseX < 2*WIDTH/3 && mouseY > 2*HEIGHT/3 && mouseY < 2*HEIGHT/3+HEIGHT/20+20){
				menu.fill(200);
				menu.rect(WIDTH/3-2.5f, 2*HEIGHT/3-2.5f, WIDTH/3+5, HEIGHT/20+25);
			}
			else {
				menu.fill(255);
				menu.rect(WIDTH/3, 2*HEIGHT/3, WIDTH/3, HEIGHT/20+20);
			}
			menu.fill(0);
			menu.textSize(WIDTH/20);
			menu.text("Commands", WIDTH/2-WIDTH/8, 2*HEIGHT/3+5, WIDTH/4, HEIGHT/8);
		}
		else {
			menu.fill(255);
			menu.rect(WIDTH/5, HEIGHT/2, 3*WIDTH/5, 2*HEIGHT/5+15);
			
			menu.fill(0);
			menu.textSize(HEIGHT/20);
			menu.text("Commands", WIDTH/2-WIDTH/8, HEIGHT/2+5, WIDTH/4, HEIGHT/8);
			
			menu.textFont(avenir, HEIGHT/50);
			int i = 0;
			float spaceY = menu.textSize+HEIGHT/100;
			menu.text("At any time :", WIDTH/5+10, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press CTRL to switch between movie and mouse", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press BACKSPACE to go back to menu", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press ESC to quit the game", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("While playing :", WIDTH/5+10, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Use the mouse dragging to incline the board", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Keep pressing SHIFT to put the game in pause", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("(and then click on the board to add mushrooms)", WIDTH/5+60, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press TAB to calibrate", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press ENTER to reset", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
			menu.text("- Press SPACE to switch between modes", WIDTH/5+50, 12*HEIGHT/20+(i++*spaceY));
		}
		menu.endDraw();
		image(menu, 0, 0);
	}

	// draw the menu to select the gaming mode
	public void drawMenu2(){

		menu2.beginDraw();
		menu2.image(champis, 0, 0);
		menu2.image(vegas, 0, HEIGHT/2);

		if(mouseX > WIDTH/2-WIDTH/5 && mouseX < WIDTH/2+WIDTH/5 && mouseY > HEIGHT/10 && mouseY < HEIGHT/10+HEIGHT/12){
			menu2.fill(200);
			menu2.rect(WIDTH/2-WIDTH/5-2.5f, HEIGHT/10-2.5f, 2*WIDTH/5+5, HEIGHT/12+5);
		}
		else{
			menu2.fill(255);
			menu2.rect(WIDTH/2-WIDTH/5, HEIGHT/10, 2*WIDTH/5, HEIGHT/12);
		}
		menu2.fill(0);
		menu2.textSize(WIDTH/20);
		menu2.text("MODE NORMAL", WIDTH/2-WIDTH/6-20, HEIGHT/9, WIDTH/2+WIDTH/6, HEIGHT/9);

		if(pmouseX > WIDTH/2-WIDTH/5 && pmouseX < WIDTH/2+WIDTH/5 && pmouseY > 11*HEIGHT/20 && pmouseY < 11*HEIGHT/20+HEIGHT/12){
			menu2.fill(200);
			menu2.rect(WIDTH/2-WIDTH/5-2.5f, 11*HEIGHT/20-2.5f, 2*WIDTH/5+5, HEIGHT/12+5);
		}
		else {
			menu2.fill(255);
			menu2.rect(WIDTH/2-WIDTH/5, 11*HEIGHT/20, 2*WIDTH/5, HEIGHT/12);
		}
		menu2.fill(0);
		menu2.textSize(WIDTH/20);
		menu2.text("MODE PINBALL", WIDTH/2-WIDTH/6-10, 9*HEIGHT/16, WIDTH/2+WIDTH/6, HEIGHT/16);
		menu2.endDraw();

		image(menu2, 0, 0);
	}

	// draw all the interface of the game
	public void drawGame(){

		lights();  
		background(255);
		drawInterface();
		translate(WIDTH/2, HEIGHT/2);

		if(pause) {
			if(webcam) {
				movie.pause();
			}
			sound.pause();
			translate(sphereLocation.x, sphereLocation.z, -sphereLocation.y);  
			sphere(SPHERE_RADIUS);
			translate(-sphereLocation.x, -sphereLocation.z, sphereLocation.y);
			drawMushrooms();
		} 
		else {
			if(webcam) {
				movie.play();
				PVector angles = ip.getAngles(img);
				if(angles != null){
					angleX = min(max(angles.x, -PI/3), PI/3);
					angleY = angles.y;
					angleZ = min(max(angles.z, -PI/3), PI/3);
				}
			}
			while(angleY < -PI/4 || angleY > PI/4){
				angleY += (angleY < -PI/4) ? PI/2 : -PI/2;
			}
			rotateX(angleX);
			//rotateY(angleY);
			rotateZ(angleZ);
		}

		if(angleX > 0 || pause){
			fill(255, 100);
		}
		box(DIM_BOX_X, DIM_BOX_Y, DIM_BOX_Z);
		
		checkEndOfTheGame();
		
		letTheBassKick();
		
		if(!pause) {
			translate(sphereLocation.x, sphereLocation.y, sphereLocation.z); 
			sphere(SPHERE_RADIUS);
			translate(-sphereLocation.x, -sphereLocation.y, -sphereLocation.z);
			sphereRoll();
			rotateX(PI/2);
			drawMushrooms();
		}
	}

	// draw the borders of the box in mode pinball
	public void drawBorders(){
		translate((DIM_BOX_X+DIM_BORDER_X)/2, (DIM_BOX_Y-DIM_BORDER_Y)/2, 0);
		box(DIM_BORDER_X, DIM_BORDER_Y, DIM_BORDER_Z);
		translate(-DIM_BOX_X-DIM_BORDER_X, 0, 0);
		box(DIM_BORDER_X, DIM_BORDER_Y, DIM_BORDER_Z);
		translate((DIM_BOX_X+DIM_BORDER_X)/2, 0, -(DIM_BOX_X+DIM_BORDER_X)/2);
		box(DIM_BORDER_Z+2*DIM_BORDER_X, DIM_BORDER_Y, DIM_BORDER_X);
		translate(0, -DIM_BOX_Y/3, DIM_BOX_X+DIM_BORDER_X);
		box(DIM_BORDER_Z, 0, 0);
		translate(0, DIM_BOX_Y/3-DIM_BORDER_Y/2, 0);
		box(DIM_BORDER_Z, 0, 0);
		translate(0, DIM_BORDER_Y-DIM_BOX_Y/2, -(DIM_BOX_X+DIM_BORDER_X)/2);
	}

	// draw the message at the end of a round 
	public void drawEndMessage(){
		
		endMessage.beginDraw();
		endMessage.background(255);

		endMessage.fill(255);
		endMessage.stroke(0);
		endMessage.strokeWeight(4);
		endMessage.rect(0, 0, WIDTH/2, HEIGHT/5);

		endMessage.fill(0);
		endMessage.textSize(HEIGHT/20);
		endMessage.text("END OF THE GAME", 20, endMessage.textSize+10);

		endMessage.textSize(HEIGHT/60);
		int i = 1;
		endMessage.text("Press ENTER for a new round", WIDTH/10, HEIGHT/20+i*endMessage.textSize+10*++i);
		endMessage.text("Press SPACE for a new round in a new world", WIDTH/10, HEIGHT/20+i*endMessage.textSize+10*++i);
		endMessage.text("Press BACKSPACE to go back to menu", WIDTH/10, HEIGHT/20+i*endMessage.textSize+10*++i);
		endMessage.text("Press ESC to quit the game", WIDTH/10, HEIGHT/20+i*endMessage.textSize+10*++i);

		endMessage.endDraw();
		image(endMessage, WIDTH/4, HEIGHT/40);
	}
	
	public void drawChangeMessage(){

		if(n++ < 200){
			fill(255, 255-n);
			rect(WIDTH/3, HEIGHT/40, WIDTH/3, HEIGHT/20);
			fill(0);
			textSize(HEIGHT/40);
			textAlign(WIDTH/2);
			text(webcam ? "    Video mode activated" : "  Video mode desactivated", WIDTH/3, HEIGHT/20+5);
		}
		else {
			changeMode = false;
		}
	}

	// draw all the interface in the down part of the window and the image of the camera
	public void drawInterface(){

		translate(0, 0, -DIM_BOX_Z);
		if(modeNormal){
			image(fondNormal, 0, -HEIGHT/5);
		}
		else if(modeFlipper){
			image(fondFlipper, -WIDTH*1.1f/5, -WIDTH*1.1f/5);
		}
		translate(0, 0, DIM_BOX_Z);

		if(webcam){
			fill(0);
			line(3*WIDTH/4, 0, 3*WIDTH/4, HEIGHT/5);
			line(3*WIDTH/4, HEIGHT/5, WIDTH, HEIGHT/5);
			fill(255);
			image(img, 3*WIDTH/4, 0, WIDTH/4, HEIGHT/5);
		}
		drawMySurface(); 
		drawTopView();
		drawScoreBoard();
		drawBarChart();
		hs.update();
		hs.display(); 
	}

	// draw the surface that contains the topView, scoreBoard, barChart, and scrollBar
	public void drawMySurface() { 
		mySurface.beginDraw(); 
		mySurface.fill(200); 
		mySurface.rect(0, 0, WIDTH, HEIGHT/5);
		mySurface.stroke(0);
		mySurface.strokeWeight(4);
		mySurface.line(0, 0, WIDTH, 0);
		mySurface.noStroke();
		mySurface.endDraw();
		image(mySurface, 0, 4*HEIGHT/5);
	}

	// draw the topView of the box with the mushrooms and ball locations on it
	public void drawTopView() {

		topView.beginDraw(); 
		topView.rect(0, 0, HEIGHT/6 * rBox, HEIGHT/6);

		// draw the cylinders on the topView
		topView.noStroke();
		//topView.fill(0);
		for (Cylinder c : cylinders) {
			topView.fill(c.isTouched ? 200 : 0, 0, 0);
			topView.ellipse((c.center.x*SCALE_X*rWindow + HEIGHT/12)*rBox, c.center.z*SCALE_Z + HEIGHT/12, 
					2*CYLINDER_BASE_SIZE * SCALE_X*rBox*rWindow, 2*CYLINDER_BASE_SIZE * SCALE_Z);
			c.isTouched = false;
		}
		// draw the ball on the topView
		topView.stroke(0);
		topView.strokeWeight(0.5f);
		topView.fill(255);
		topView.ellipse((sphereLocation.x*SCALE_X*rWindow + HEIGHT/12)*rBox, sphereLocation.z*SCALE_Z + HEIGHT/12, 
				2*SPHERE_RADIUS * SCALE_X*rBox*rWindow, 2*SPHERE_RADIUS * SCALE_Z);

		topView.stroke(0);
		topView.strokeWeight(2); 
		topView.fill(0, 120, 190); 

		topView.endDraw();
		image(topView, WIDTH/60, HEIGHT/60 + 4*HEIGHT/5);
	}

	// draw the score board with total score, actual velocity, and last score
	public void drawScoreBoard() {

		scoreBoard.beginDraw(); 
		scoreBoard.stroke(0);
		scoreBoard.strokeWeight(2);
		scoreBoard.rect(0, 0, HEIGHT/8, HEIGHT/6);  

		scoreBoard.fill(0);
		scoreBoard.textSize(HEIGHT/80);

		scoreBoard.text("TOTAL SCORE :", HEIGHT/80, HEIGHT/80, HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);
		scoreBoard.text(Float.toString(score), HEIGHT/80, 2*HEIGHT/80, 
				HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);

		scoreBoard.text("VELOCITY :", HEIGHT/80, 5*HEIGHT/80, HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);
		scoreBoard.text(Float.toString(sphereSpeed.mag()), HEIGHT/80, 6*HEIGHT/80, 
				HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);

		scoreBoard.text("LAST SCORE :", HEIGHT/80, 9*HEIGHT/80, 
				HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);
		scoreBoard.text(Float.toString(lastScore), HEIGHT/80, 10*HEIGHT/80, 
				HEIGHT/8 - HEIGHT/80, HEIGHT/6 - HEIGHT/80);

		scoreBoard.fill(250);
		scoreBoard.endDraw();
		image(scoreBoard, 2*WIDTH/60 + HEIGHT/6 * rBox, HEIGHT/60 + 4*HEIGHT/5);  
	}

	// draw the barChart to show the evolution of the score 
	public void drawBarChart() {

		barChart.beginDraw();
		barChart.fill(250);
		barChart.stroke(0);
		barChart.strokeWeight(2);
		barChart.rect(0, 0, WIDTH-4*WIDTH/60 - HEIGHT/6*(0.75f + rBox), HEIGHT/8);

		if (!pause && !willBegin) {
			if (counter++ == 10) {
				scoreGraph.add(max(score, 0));
				if (nbRect >= NB_RECT_MAX_Y) {
					scoreMax *= 1.5f;
				}
				counter = 0;
			}
		}
		rectScoreValue = scoreMax/NB_RECT_MAX_Y;
		boolean valeurMaxTropFaible = true;
		boolean valeurMaxTropHaute = false;
		float x = 0;
		int borneMinX = max(0, (int)(scoreGraph.size()-NB_RECT_MAX_X/(2*hs.getPos())));
		for (int i = borneMinX; i < scoreGraph.size()-1; i++) {
			x += DIM_RECT+ECART;
			float s = scoreGraph.get(i);
			if (s > scoreMax / 2) valeurMaxTropFaible = false;
			else if(s > scoreMax) valeurMaxTropHaute = true;
			nbRect = (int)(s/rectScoreValue);
			for (int y=0; y<nbRect; y++) {
				barChart.noStroke();
				barChart.fill(0, 120, 190);
				barChart.rect(x*2*hs.getPos(), HEIGHT/8-(y+1)*(DIM_RECT+ECART)-2*ECART, DIM_RECT*2*hs.getPos(), DIM_RECT);
			}
		}
		if(valeurMaxTropFaible && scoreMax > 100) scoreMax /= 1.5f;
		if(valeurMaxTropHaute) scoreMax *= 1.5f;

		barChart.endDraw();
		image(barChart, 3*WIDTH/60 + (HEIGHT/6)*(0.75f+rBox), HEIGHT/60 + 4*HEIGHT/5);
	}

	// method to draw the mushrooms on the board
	public void drawMushrooms() {
		translate(0, DIM_BOX_Y/2);
		rotateX(PI/2);
		for (int i = 0; i < cylinders.size(); i++) {
			Cylinder c = cylinders.get(i);
			translate(c.center.x, 2*DIM_BOX_Y, -c.center.z);
			if(modeNormal){
				shape(mushroom);				
			}
			else if(modeFlipper){
				translate(0, c.isTouched ? -DIM_BOX_Y : -DIM_BOX_Y/4, 0);
				shape((i%2 == 0) ? redMushroom : blueMushroom);
				translate(0, c.isTouched ? DIM_BOX_Y : DIM_BOX_Y/4, 0);
			}
			translate(-c.center.x, -2*DIM_BOX_Y, c.center.z);
		}
		rotateX(pause ? -PI : -PI/2);
	}

	/* check if the round is ended, and if it is the case, 
	 shows the message of ended game */
	public void checkEndOfTheGame(){
		if((start && cylinders.size() == 0) || nbRound == 0){
			pause = true;
			translate(-WIDTH/2, 0, -HEIGHT/2);
			rotateX(PI/2);
			drawEndMessage();
			rotateX(-PI/2);
			translate(WIDTH/2, 0, HEIGHT/2);
		}
		else if(modeFlipper && !pause) nbRound--;
	}

	// Method to play the music in function of the mode we are playing
	public void letTheBassKick(){
		if(modeFlipper){
			musicMario.pause();
			musicFlipper.play();
			drawBorders();
		}
		else if(modeNormal){
			musicMario.play();
			musicFlipper.pause();
		}
	}

	public void calibrate(){
		movie.pause();
		img.resize(WIDTH, (img.height/img.width)*WIDTH);
		fill(0);
		rect(0, img.height, WIDTH, 4*HEIGHT/5-img.height);
		fill(255);
		textSize(HEIGHT/60);
		textAlign(WIDTH/2);
		text("Please select at least 3 points on the board, and then press ENTER. When finished press TAB",
				50, img.height+25*HEIGHT/800);
		image(img, 0, 0);
	}

	/* ========================================================================================================== 
	Methods to create the objects of the game and simulate their actions
 	=========================================================================================================== */

	// method to add new cylinders to our list of cylinders that we draw
	public boolean addMushroom(float centerX, float centerZ) {
		// we ensure that the cylinder that we want to create is not outside of the box
		if (centerX + CYLINDER_BASE_SIZE/2 < DIM_BOX_X/2-10 && centerX - CYLINDER_BASE_SIZE/2 > -DIM_BOX_X/2+10
				&& centerZ + CYLINDER_BASE_SIZE/2 < DIM_BOX_Z/2-10 && centerZ - CYLINDER_BASE_SIZE/2 > -DIM_BOX_Z/2+10) {
			Cylinder newCylinder = new Cylinder(centerX, centerZ, this);
			/* we check if the cylinder that we want to create
     		will not overlap with another cylinder which has been already created... */
			boolean isDistinct = true;
			for (Cylinder thatCylinder : cylinders) {
				float distance = sqrt(pow(thatCylinder.center.x - centerX, 2) + pow(thatCylinder.center.z - centerZ, 2));
				if (distance < 2*CYLINDER_BASE_SIZE) {
					isDistinct = false;
				}
			}
			// ...if not and also if it's not on the ball, we add it to our list of cylinders
			float distanceWithBall = newCylinder.center.dist(sphereLocation);
			if (isDistinct && distanceWithBall > SPHERE_RADIUS + CYLINDER_BASE_SIZE) {
				return cylinders.add(newCylinder);
			}
		}
		return false;
	}

	// method to simulate the rolling of the ball
	public void sphereRoll() {

		checkEdges();
		checkCylindersBorder();

		PVector gravity = new PVector(sin(angleZ)*g, 0, -sin(angleX)*g);
		sphereSpeed.add(gravity);
		sphereSpeed.mult(mu);
		if(modeFlipper){
			sphereSpeed.mult(mu);
			while(sphereSpeed.mag() > 15){
				sphereSpeed.div(1.5f);
			}
		}
		sphereLocation.add(sphereSpeed);
	}

	// method to simulate the collisions with the border of the box
	public void checkEdges() {

		boolean collision = abs(sphereLocation.x) + SPHERE_RADIUS >= (DIM_BOX_X/2)
							|| abs(sphereLocation.z) + SPHERE_RADIUS >= (DIM_BOX_Z/2); 
		if (collision) {
			
			lastScore = -sphereSpeed.mag();
			score += lastScore;

			if (sphereLocation.x + SPHERE_RADIUS > (DIM_BOX_X/2)) {
				sphereLocation.add(cheatRight);
				sphereSpeed.x *= -REBOUND_COEFF;
			}
			else if (sphereLocation.x - SPHERE_RADIUS < -(DIM_BOX_X/2)) {
				sphereLocation.add(cheatLeft);
				sphereSpeed.x *= -REBOUND_COEFF;
			} 

			if (sphereLocation.z + SPHERE_RADIUS > (DIM_BOX_Z/2)) {
				sphereLocation.add(cheatUp);
				sphereSpeed.z *= -REBOUND_COEFF;
			}
			else if (sphereLocation.z - SPHERE_RADIUS < -(DIM_BOX_Z/2)) { 
				sphereLocation.add(cheatDown);
				sphereSpeed.z *= -REBOUND_COEFF;
			}
		}
	}
	
	// method to simulate the collisions with the mushrooms
	public void checkCylindersBorder() {
		// for all cylinder that we have built...
		for (int i=0; i<cylinders.size(); i++) {
			Cylinder thatCylinder = cylinders.get(i);
			//...we compute the distance between the sphere and that cylinder
			float distance = sphereLocation.dist(thatCylinder.center);
			// and we check if there is a collision or not 
			if (distance <= SPHERE_RADIUS + CYLINDER_BASE_SIZE) {	
				lastScore = sphereSpeed.mag();
				score += lastScore;
				
				// simulation of the bound of the ball on the cylinder
				PVector n = PVector.sub(sphereLocation, thatCylinder.center);
				n.normalize();
				n.mult(-2*n.dot(sphereSpeed));
				sphereSpeed.add(n);

				// as for the edges of the box, we add a "cheat" to avoid bugs
				PVector cheat = PVector.mult(PVector.sub(sphereLocation, thatCylinder.center), eps);
				sphereLocation.add(cheat);
				
				if(modeNormal){
					sphereSpeed.mult(REBOUND_COEFF);
					cylinders.remove(i);
				}
				else if(modeFlipper){
					thatCylinder.isTouched = true;
					//sound.stop();
					//sound.play();
					//sound.loop(); // ici ou au dessus ??
					sphereSpeed.mult(2);
				}
			}
		}
	}

	/* Method to reset the game 
	(to begin a new round for instance or when we go back to menu) */
	public void resetGame(){
		angleX = 0;
		angleY = 0;
		angleZ = 0;
		sphereSpeed.mult(0);
		sphereLocation.mult(0);
		sphereLocation.add(0, -SPHERE_RADIUS-DIM_BOX_Y/2, 0);
		lastScore = 0;
		score = 0;
		coefWheel = 1;
		nbRound = webcam ? 500 : 2000;
		TAILLE = 3*HEIGHT/4;
		scoreGraph.clear();
		cylinders.clear();
		if(modeFlipper){
			musicFlipper.stop();
			musicMario.stop();
			musicFlipper.play();
			//musicFlipper.loop();
			reinitFlipper();
		}
		else if(modeNormal){
			musicFlipper.stop();
			musicMario.stop();
			musicMario.play();
			/*sound.pause();
			musicMario.loop();*/
		}
		else {
			sound.stop();
			musicFlipper.stop();
			musicMario.stop();
			/*musicFlipper.loop();
			musicMario.loop();
			sound.pause();
			musicFlipper.pause();
			musicMario.pause();*/
		}
		movie.stop();
		hs.reset();
		start = false;
		changeMode = false;
		calibration = false;
		pause = false;
	}

	/* method to reinitialize the board when we are in mode pinball 
	 by adding 10 random mushrooms and giving an initial random direction to the ball */
	public void reinitFlipper(){
		while(cylinders.size() < 10){
			addMushroom(random.nextInt((int)DIM_BOX_X)-(int)DIM_BOX_X/2, random.nextInt((int)DIM_BOX_Z)-(int)DIM_BOX_Z/2);
		}	
		willBegin = true; 
		nb = '3';
		float axe = random.nextFloat();
		float dirX = (float)Math.signum(random.nextDouble()-0.5)*axe;
		float dirZ = (float)Math.signum(random.nextDouble()-0.5)*(1-axe);
		sphereSpeed.add(10*dirX, 0, 10*dirZ);
	}
	
	// method to draw the count down before mode pinball 
	public void countDown(){
		if(nb > '0'){
			musicMario.pause();
			sound.pause();
			musicFlipper.pause();
			textAlign(CENTER);
			translate(0, -HEIGHT/4, 0);
			fill(250, 70, 50, 200);
			textSize(TAILLE);
			text(nb, width/2, height);
			TAILLE-=5;
			if (TAILLE < HEIGHT/4){
				nb--;
				TAILLE = 3*HEIGHT/4; 
			}
			translate(0, HEIGHT/4, 0);
			fill(255);
		} 
		else {
			willBegin = false;
		}
	}

	/* ========================================================================================================== 
	 Methods for keyboard and mouse actions 
 	=========================================================================================================== */

	// Rotation around X and Z by using the mouse drag
	public void mouseDragged() {

		if(play && !pause && !willBegin && (pmouseX > 0 && pmouseX < WIDTH && pmouseY > 0 && pmouseY < 4*HEIGHT/5)){

			if (mouseX > pmouseX) {
				angleZ = min(angleZ + coefWheel * ANGLE_VARIATION, PI/3);
			} 
			else if (mouseX < pmouseX) {
				angleZ = max(angleZ - coefWheel * ANGLE_VARIATION, -PI/3);
			}

			if (mouseY > pmouseY) {
				angleX = max(angleX - coefWheel * ANGLE_VARIATION, -PI/3);
			} 
			else if (mouseY < pmouseY) {
				angleX = min(angleX + coefWheel * ANGLE_VARIATION, PI/3);
			}
		}
	}

	// speed up or slow down rotation by using the mouse wheel 
	public void mouseWheel(MouseEvent event) {
		coefWheel = min(max(coefWheel + (event.getCount() < 0 ? -0.1f : 0.1f), 0.2f), 2);
	}

	public void keyPressed() {
		if (key == CODED) {
			// put the game in pause to add mushrooms
			if (keyCode == SHIFT) {
				if(play) {
					pause = true;
				}
			}
			// change in the game mode 
			if(keyCode == CONTROL){
				if(!calibration){
					if(webcam){
						movie.pause();
						nbRound *= 4;
					}
					else nbRound /= 4;
					n=0; changeMode = true;
					webcam = !webcam;
				}
			}
			// Rotation around y axis with left and right arrows of the keyboard
			if (keyCode == RIGHT) {
				angleY += coefWheel * ANGLE_VARIATION;
			} 
			if (keyCode == LEFT) {
				angleY -= coefWheel * ANGLE_VARIATION;
			}
		}
		// put the game in pause to calibrate
		if(key == TAB){
			if(play && webcam && !willBegin){
				pause = !pause;
				if(calibration){
					calibrationCorners.clear();
				}
				calibration = !calibration;
			}
		}
		/* if calibration, set the thresholds for the detection of the board in function of the selected corners
		 otherwise reset the game*/
		if(key == ENTER){
			if(calibration){
				ip.drawPolygone(ip.sortCorners(calibrationCorners));
				ip.setThresholds(ip.calibrate(img, ip.sortCorners(calibrationCorners)));
				calibrationCorners.clear();
			}
			else resetGame();
		}
		// return to menu
		if(key == BACKSPACE){
			modeNormal = false;
			modeFlipper = false;
			resetGame();
			willBegin = false;
			choseMode = false;
			play = false;
			commands = false;
		}
		// change the game "style"
		if(key == ' '){
			if(play && !calibration && !willBegin){
				modeNormal = !modeNormal;
				modeFlipper = !modeFlipper;
				resetGame();
			}
		}
	}

	/* put back the game at its previous configuration before we press the SHIFT key
 	but more with the added cylinders, when we release this key */
	public void keyReleased() {
		if (keyCode == SHIFT) {
			pause = false;
		}
	}

	public void mousePressed() {
		// select modes
		if(!play){
			if(choseMode){
				if(pmouseX > WIDTH/2-WIDTH/5 && pmouseX < WIDTH/2+WIDTH/5 && pmouseY > HEIGHT/10 && pmouseY < HEIGHT/10+HEIGHT/12){
					play = true;
					modeNormal = true;
				}
				else if(pmouseX > WIDTH/2-WIDTH/5 && pmouseX < WIDTH/2+WIDTH/5 && pmouseY > 11*HEIGHT/20 && pmouseY < 11*HEIGHT/20+HEIGHT/12){
					play = true;
					modeFlipper = true;
					reinitFlipper();
				}
			}
			else {
				if(!commands && pmouseX > WIDTH/3 && pmouseX < 2*WIDTH/3 && pmouseY > HEIGHT/2 && pmouseY < HEIGHT/2+WIDTH/20+20){
					choseMode = true;
				}
				else if(!commands && mouseX > WIDTH/3 && mouseX < 2*WIDTH/3 && mouseY > 2*HEIGHT/3 && mouseY < 2*HEIGHT/3+HEIGHT/20+20){
					commands = true;
				}
				else if(commands && (mouseX < WIDTH/5 || mouseX > 4*WIDTH/5 || mouseY < HEIGHT/2 || mouseY > 9*HEIGHT/10)){
					commands = false;
				}
			}
		}
		// draw a cylinder with center position the position of the mouse when if we are in pause mode
		else if (pause && !calibration) {
			if(addMushroom(pmouseX - WIDTH/2, pmouseY - HEIGHT/2) && !start){;
				start = true;
			}
		}
		// select corners for calibrate
		else if(calibration){
			calibrationCorners.add(new PVector(pmouseX, pmouseY));
			fill(255, 128, 0);
			for(PVector p : calibrationCorners){
				ellipse(p.x, p.y, 10, 10);
			}
		}
	}
}