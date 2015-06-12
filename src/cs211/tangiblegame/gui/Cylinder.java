package cs211.tangiblegame.gui;

import static cs211.tangiblegame.gui.TangibleGame.SPHERE_RADIUS;
import static cs211.tangiblegame.gui.TangibleGame.HEIGHT;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.GROUP;
import static processing.core.PConstants.QUAD_STRIP;
import static processing.core.PConstants.TRIANGLES;
import static processing.core.PConstants.TWO_PI;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

class Cylinder {

	private final PApplet parent;
	
	private final float DIM_BOX_Y = HEIGHT/60;
	protected final static float CYLINDER_BASE_SIZE = 10; 
	protected final static float CYLINDER_HEIGHT = 40; 
	protected final static int CYLINDER_RESOLUTION = 40;

	protected boolean isTouched;
	protected PVector center;
	protected PShape cylinder;

	//constructor of the cylinder of center position (centerX, -SPHERE_RADIUS-DIM_BOX_Y/2, centerZ)
	public Cylinder(float centerX, float centerZ, PApplet parent) {
		this.isTouched = false;
		this.parent = parent;
		this.center = new PVector(centerX, -SPHERE_RADIUS-DIM_BOX_Y/2, centerZ);
		this.cylinder = new PShape();
	}

	// Method to create a cylinder of center position (centerX, centerY)
	// (Unused method since we now draw mushrooms)
	public Cylinder build() {

		PShape openCylinder = new PShape(); 
		PShape downFace = new PShape(); 
		PShape upFace = new PShape();

		float angle;
		float[] x = new float[CYLINDER_RESOLUTION + 1]; 
		float[] y = new float[CYLINDER_RESOLUTION + 1];
		//get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = (TWO_PI / CYLINDER_RESOLUTION) * i; 
			x[i] = sin(angle) * CYLINDER_BASE_SIZE + center.x; 
			y[i] = cos(angle) * CYLINDER_BASE_SIZE + center.z;
		}

		openCylinder = parent.createShape();
		openCylinder.beginShape(QUAD_STRIP);
		// create the border of the cylinder
		for (int i = 0; i < x.length; i++) { 
			openCylinder.vertex(x[i], y[i], DIM_BOX_Y/2); 
			openCylinder.vertex(x[i], y[i], DIM_BOX_Y/2 + CYLINDER_HEIGHT);
		}
		openCylinder.endShape();

		downFace = parent.createShape();
		downFace.beginShape(TRIANGLES);
		// create the down face of the cylinder
		for (int i = 0; i < x.length-1; i++) { 
			downFace.vertex(center.x, center.z, DIM_BOX_Y/2);
			downFace.vertex(x[i], y[i], DIM_BOX_Y/2); 
			downFace.vertex(x[i+1], y[i+1], DIM_BOX_Y/2);
		}
		downFace.endShape();

		upFace = parent.createShape();
		upFace.beginShape(TRIANGLES);
		// create the up face of the cylinder
		for (int i = 0; i < x.length-1; i++) { 
			upFace.vertex(center.x, center.z, DIM_BOX_Y/2 + CYLINDER_HEIGHT);
			upFace.vertex(x[i], y[i], DIM_BOX_Y/2 + CYLINDER_HEIGHT); 
			upFace.vertex(x[i+1], y[i+1], DIM_BOX_Y/2 + CYLINDER_HEIGHT);
		}
		upFace.endShape();

		// create the closed cylinder that we need 
		cylinder = parent.createShape(GROUP);
		cylinder.addChild(openCylinder);
		cylinder.addChild(downFace);
		cylinder.addChild(upFace);
		//return the closed cylinder which we just created
		return this;
	}
}