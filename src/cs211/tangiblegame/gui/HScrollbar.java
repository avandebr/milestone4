package cs211.tangiblegame.gui;

import static processing.core.PApplet.*;
import processing.core.PApplet;

class HScrollbar {
	
	private final PApplet parent;
	float barWidth; //Bar's width in pixels 
	float barHeight; //Bar's height in pixels 
	float xPosition; //Bar's x position in pixels 
	float yPosition; //Bar's y position in pixels
	float sliderPosition, newSliderPosition; //Position of slider
	float sliderPositionMin, sliderPositionMax; //Max and min values of slider
	boolean mouseOver; //Is the mouse over the slider?
	boolean locked; //Is the mouse clicking and dragging the slider now?

	/**
	 * @brief Creates a new horizontal scrollbar
	 *
	 * @param x The x position of the top left corner of the bar in pixels
	 * @param y The y position of the top left corner of the bar in pixels
	 * @param w The width of the bar in pixels
	 * @param h The height of the bar in pixels
	 */
	HScrollbar (float x, float y, float w, float h, PApplet parent) { 
		this.parent = parent;
		xPosition = x;
		yPosition = y;
		barWidth = w;
		barHeight = h;
		sliderPosition = xPosition + barWidth/2 - barHeight/2;
		newSliderPosition = sliderPosition;
		sliderPositionMin = xPosition;
		sliderPositionMax = xPosition + barWidth - barHeight;
	}
	
	public void reset(){
		newSliderPosition = (sliderPositionMin+sliderPositionMax)/2;
		sliderPosition = newSliderPosition;
	}

	/**
	 * @brief Updates the state of the scrollbar according to the mouse movement
	 */
	public void update() {
		if (isMouseOver()) {
			mouseOver = true; }
		else {
			mouseOver = false;
		}
		if (parent.mousePressed && mouseOver) {
			locked = true; }
		if (!parent.mousePressed) { 
			locked = false;
		}
		if (locked) {
			newSliderPosition = constrain(parent.mouseX - barHeight/2, sliderPositionMin, sliderPositionMax);
		}
		if (abs(newSliderPosition - sliderPosition) > 1) {
			sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
		}
	}

	/**
	 * @brief Clamps the value into the interval
	 *
	 * @param val The value to be clamped
	 * @param minVal Smallest value possible
	 * @param maxVal Largest value possible
	 *
	 * @return val clamped into the interval [minVal, maxVal]
	 */
	public float constrain(float val, float minVal, float maxVal) { 
		return min(max(val, minVal), maxVal);
	}

	/**
	 * @brief Gets whether the mouse is hovering the scrollbar
	 *
	 * @return Whether the mouse is hovering the scrollbar
	 */
	public boolean isMouseOver() {
		return parent.mouseX > xPosition && parent.mouseX < xPosition+barWidth &&
				parent.mouseY > yPosition && parent.mouseY < yPosition+barHeight;
	}

	/**
	 * @brief Draws the scrollbar in its current state
	 */
	public void display() {
		//noStroke();
		parent.fill(204);
		parent.rect(xPosition, yPosition, barWidth, barHeight); 
		if (mouseOver || locked) {
			parent.fill(0, 0, 0);
		}
		else {
			parent.fill(102, 102, 102);
		}
		parent.rect(sliderPosition, yPosition, barHeight, barHeight);
		parent.fill(255);
	}

	/**
	 * @brief Gets the slider position
	 *
	 * @return The slider position in the interval [0,1]
	 * corresponding to [leftmost position, rightmost position]
	 */
	public float getPos() {
		return (sliderPosition - xPosition)/(barWidth - barHeight);
	} 
}
