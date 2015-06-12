package cs211.tangiblegame.imageprocessing;

import static cs211.tangiblegame.imageprocessing.QuadGraph.getArea;
import static cs211.tangiblegame.imageprocessing.QuadGraph.isConvex;
import static cs211.tangiblegame.imageprocessing.QuadGraph.nonFlatQuad;
import static cs211.tangiblegame.imageprocessing.QuadGraph.validArea;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static processing.core.PApplet.min;
import static processing.core.PApplet.pow;
import static processing.core.PApplet.round;
import static processing.core.PApplet.sqrt;
import static processing.core.PConstants.ALPHA;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.RGB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import processing.core.PImage;
import processing.core.PVector;
import cs211.tangiblegame.gui.TangibleGame;

public class ImageProcessing {

	private TangibleGame game;
	public PImage houghImg;	

	double minHue = 100;
	double maxHue = 135;

	double minBrightness = 30;
	double maxBrightness = 180;

	double minSaturation = 0;
	double maxSaturation = 100;

	float discretizationStepsPhi; 
	float discretizationStepsR;
	int phiDim;

	float[] tabSin;
	float[] tabCos;
	
	public ImageProcessing(TangibleGame game){
		this.game = game;		
		discretizationStepsPhi = 0.06f; 
		discretizationStepsR = 2.5f;
		phiDim = round(PI / discretizationStepsPhi);
		tabSin = new float[phiDim];
		tabCos = new float[phiDim];
		float ang = 0;
		for (int accPhi = 0; accPhi < phiDim; ang += discretizationStepsPhi, accPhi++) {
			tabSin[accPhi] = (float)sin(ang);
			tabCos[accPhi] = (float)cos(ang);
		}
	}

	public ArrayList<PVector> pipeline(PImage img){
		return hough(sobel(intenistyThresholding(convolute(deleteNoise(img)))), 4);
	}

	public float mySin(float phi){
		int accPhi = round(phi/discretizationStepsPhi);
		return tabSin[(accPhi < phiDim) ? accPhi : phiDim-1];
	}
	public float myCos(float phi){
		int accPhi = round(phi/discretizationStepsPhi);
		return tabCos[(accPhi < phiDim) ? accPhi : phiDim-1];
	}
	
	public void setThresholds(PVector[] newThresholds){
		if(newThresholds != null){
			/*this.minHue = newThresholds[0].x;
			this.maxHue = newThresholds[0].y;
			this.minBrightness = newThresholds[1].x;
			this.maxBrightness = newThresholds[1].y;
			this.minSaturation = newThresholds[2].x;
			this.maxSaturation = newThresholds[2].y;*/
		}
	}

	public PImage deleteNoise(PImage img){

		PImage result = game.createImage(img.width, img.height, RGB); 
		for(int i = 0; i < img.width * img.height; i++) {
			if((game.hue(img.pixels[i]) < maxHue && game.hue(img.pixels[i]) > minHue)
					&& (game.brightness(img.pixels[i]) < maxBrightness && game.brightness(img.pixels[i]) > minBrightness)
					&& !(game.saturation(img.pixels[i]) < maxSaturation && game.saturation(img.pixels[i]) > minSaturation)){
				result.pixels[i] = game.color(255);
			}
			else result.pixels[i] = game.color(0);
		}
		return result;
	}

	public PImage convolute(PImage img) {

		float[][] kernel = {{9, 12, 9},
				{12, 15, 12},
				{9, 12, 9}};	

		float weight = 99.f;
		PImage result = game.createImage(img.width, img.height, ALPHA);
		final int N = kernel.length;
		int sum;

		for (int x = N/2; x < img.width - N/2; x++) {
			for (int y = N/2; y < img.height - N/2; y++) {
				sum = 0;
				for (int i = 0; i < N; i++){
					for (int j = 0; j < N; j++){
						sum += game.brightness(img.pixels[x - N/2 + i + (y - N/2 + j) * img.width]) * kernel[i][j];
					}
				}
				result.pixels[x + y * img.width] = game.color(sum / weight);
			}
		}
		return result;
	}

	public PImage intenistyThresholding(PImage img){

		PImage result = game.createImage(img.width, img.height, RGB);
		for(int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = game.color(game.brightness(img.pixels[i]) > 100 ? 255 : 0);
		}
		return result;
	}

	public PImage sobel(PImage img){

		float[][] hKernel = {{0, 1, 0},
				{0, 0, 0},
				{0, -1, 0}};

		float[][] vKernel = {{0, 0, 0},
				{1, 0, -1},
				{0, 0, 0}};

		PImage result = game.createImage(img.width, img.height, ALPHA);

		for(int i = 0; i < img.width*img.height; i++){
			result.pixels[i] = game.color(0);
		}

		float max = 0;
		float[] buffer = new float[img.width * img.height];

		int sum_h = 0, sum_v = 0, sum = 0;
		final int N = hKernel.length;

		for (int x = N/2; x < img.width - N/2; x++) {
			for (int y = N/2; y < img.height - N/2; y++) {
				sum_h = 0;
				sum_v = 0;
				sum = 0;
				for (int i = 0; i < N; i++){
					for (int j = 0; j < N; j++){
						sum_h += img.pixels[x - N/2 + i + (y - N/2 + j) * img.width] * hKernel[i][j];
						sum_v += img.pixels[x - N/2 + i + (y - N/2 + j) * img.width] * vKernel[i][j];
					}
				}
				sum = round(sqrt(pow(sum_h, 2) + pow(sum_v, 2)));
				if(sum > max){
					max = sum;
				}
				buffer[x + y*img.width] = sum;
			}
		}

		for(int y = 2; y < img.height; y++){
			for(int x = 2; x < img.width; x++){
				if(buffer[y*img.width + x] > round(max*0.3f)){
					result.pixels[y*img.width + x] = game.color(255);
				}
				else {
					result.pixels[y*img.width + x] = game.color(0);
				}
			}
		}
		return result;
	}

	public ArrayList<PVector> hough(PImage edgeImg, int nLines) {

		int rDim = round(((edgeImg.width + edgeImg.height) * 2 + 1) / discretizationStepsR);
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		float r = 0;
		int accPhi = 0;
		int accR = 0;
		int idx = 0;

		// our accumulator (with a 1 pix margin around)
		// Fill the accumulator: on edge points (ie, white pixels of the edge image), 
		//store all possible (r, phi) pairs describing lines going through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if (game.brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (float phi = 0; phi < PI; phi += discretizationStepsPhi) {
						r = x * myCos(phi) + y * mySin(phi);
						accPhi = round(phi / discretizationStepsPhi);
						accR = round(r / discretizationStepsR + (rDim - 1) * 0.5f);
						idx = (accPhi + 1) * (rDim + 2) + accR + 1;
						accumulator[idx]++;
					}
				} 
			}
		}

		houghImg = game.createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = game.color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		houghImg.resize(game.width/3, game.height);

		ArrayList<Integer> bestCandidates = new ArrayList<>();
		ArrayList<PVector> bestVectors = new ArrayList<>();
		// size of the region we search for a local maximum
		int neighbourhood = 10;
		int minVotes = 100;
		// only search around lines with more that this amount of votes // (to be adapted to your image)
		for (accR = 0; accR < rDim; accR++) {
			for (accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				idx = (accPhi + 1) * (rDim + 2) + accR + 1; 
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for(int dPhi=-neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) { 
						// check we are not outside the image
						if(accPhi+dPhi < 0 || accPhi+dPhi >= phiDim) continue; 
						for(int dR=-neighbourhood/2; dR < neighbourhood/2 +1; dR++) {
							// check we are not outside the image
							if(accR+dR < 0 || accR+dR >= rDim) continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
							if(accumulator[idx] < accumulator[neighbourIdx]) { 
								// the current idx is not a local maximum! 
								bestCandidate=false;
								break;
							} 
						}
						if(!bestCandidate) break; 
					}
					if(bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				} 
			}
		}

		Collections.sort(bestCandidates, new HoughComparator(accumulator));
		for(int i = 0; i < nLines && i < bestCandidates.size(); i++){

			idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:

			accPhi = round(idx / (rDim + 2)) - 1;
			accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR; 
			float phi = accPhi * discretizationStepsPhi;
			bestVectors.add(new PVector(r, phi));
			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of the image
			/*int x0 = 0;
			int y0 = round(r / mySin(phi));
			int x1 = round(r / myCos(phi));
			int y1 = 0;
			int x2 = edgeImg.width;
			int y2 = round(-myCos(phi) / mySin(phi) * x2 + r / mySin(phi)); 
			int y3 = edgeImg.width;
			int x3 = round(-(y3 - r / mySin(phi)) * (mySin(phi) / myCos(phi)));
			// Finally, plot the lines
			game.stroke(204,102,0); 
			if (y0 > 0) {
				if (x1 > 0)
					game.line(x0, y0, x1, y1);
				else if (y2 > 0)
					game.line(x0, y0, x2, y2);
				else
					game.line(x0, y0, x3, y3);
			}
			else {
				if (x1 > 0) {
					if (y2 > 0)
						game.line(x1, y1, x2, y2); 
					else
						game.line(x1, y1, x3, y3);
				}
				else
					game.line(x2, y2, x3, y3);
			}*/
		}
		return bestVectors;
	}
	
	public PVector[] calibrate(PImage img, List<PVector> corners){
		
		float minHue = 255, minBrightness = 255, minSaturation = 255;
		float maxHue = 0, maxBrightness = 0, maxSaturation = 0;
		for(int i = 0; i < img.width*img.height; i++){
			if(isInside(new PVector(i/img.width, i%img.width), corners)){
				if(game.hue(img.pixels[i]) < minHue){
					minHue = game.hue(img.pixels[i]);
				}
				else if(game.hue(img.pixels[i]) > maxHue){
					maxHue = game.hue(img.pixels[i]);
				}
				if(game.brightness(img.pixels[i]) < minBrightness){
					minBrightness = game.brightness(img.pixels[i]);
				}
				else if(game.brightness(img.pixels[i]) > maxBrightness){
					maxBrightness = game.brightness(img.pixels[i]);
				}
				if(game.saturation(img.pixels[i]) < minSaturation){
					minSaturation = game.saturation(img.pixels[i]);
				}
				else if(game.saturation(img.pixels[i]) > maxSaturation){
					maxSaturation = game.saturation(img.pixels[i]);
				}
			}
		}
		PVector[] toReturn = { 
				new PVector(minHue, maxHue), 
				new PVector(minBrightness, maxBrightness), 
				new PVector(minSaturation, maxSaturation)};
		for(int i = 0; i < 3; i++){
			System.out.println(toReturn[i]);
		}
		return toReturn;
	}
	
	public boolean isInside(PVector p, List<PVector> polygone){
		int idx = 0;
		for(int i = 0; i < polygone.size(); i++){
			PVector p1 = polygone.get(i);
			PVector p2 = polygone.get((i+1)%polygone.size());
			if(p1.y <= p.y){
				if(p2.y > p.y && isLeft(p, p1, p2)){
					idx++;
				}
			}
			else {
				if(p2.y <= p.y && isLeft(p, p2, p1)){
					idx--;
				}
			}
		}
		return idx != 0;
	}
	
	public boolean isLeft(PVector p, PVector p1, PVector p2){
		return (p1.x - p.x)*(p2.y - p.y) > (p2.x - p.x)*(p1.y - p.y);
	}
	
	public void drawPolygone(List<PVector> polygone){
		for(int i = 0; i< polygone.size(); i++){
			game.fill(255, 128, 0);
			game.line(polygone.get(i).x, polygone.get(i).y, 
					polygone.get((i+1)%polygone.size()).x, polygone.get((i+1)%polygone.size()).y);
		}
	}

	public PVector getAngles(PImage img){
		
		TwoDThreeeD tDtD = new TwoDThreeeD(img.width, img.height);
		PVector rot = null;
		final float imgArea = img.width*img.height;
		ArrayList<PVector> lines = pipeline(img);
		QuadGraph quads = new QuadGraph();
		quads.build(lines, img.width, img.height);
		double maxArea = 0;
		for (int[] quad : quads.findCycles()) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			
			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);
			
			double thatArea = getArea(c12, c23, c34, c41);
			if(isConvex(c12, c23, c34, c41) && nonFlatQuad(c12, c23, c34, c41) 
					&& validArea(c12, c23, c34, c41, imgArea, 0) && thatArea > maxArea){
				rot = tDtD.get3DRotations(sortCorners(Arrays.asList(c12, c23, c34, c41)));
				maxArea = thatArea;
			}
		}
		/*if(rot != null) rot.mult(180/PI);
		System.out.println(rot);
		if(rot != null) rot.mult(PI/180);*/
		return rot;
	}
	
	public void drawQuads(PVector c12, PVector c23, PVector c34, PVector c41){
		Random random = new Random();
		game.fill(255, 128, 0);
		game.ellipse(c12.x, c12.y, 5, 5);
		game.ellipse(c23.x, c23.y, 5, 5);
		game.ellipse(c34.x, c34.y, 5, 5);
		game.ellipse(c41.x, c41.y, 5, 5);
		game.fill(game.color(min(255, random.nextInt(300)),
				min(255, random.nextInt(300)),
				min(255, random.nextInt(300)), 50));
		game.quad(c12.x,c12.y,c23.x,c23.y,c34.x,c34.y,c41.x,c41.y);
	}

	public PVector intersection(PVector line1, PVector line2) { 

		float r1 = line1.x, r2 = line2.x;
		float phi1 = line1.y, phi2 = line2.y;
		float d = myCos(phi2)*mySin(phi1) - myCos(phi1)*mySin(phi2);
		float x = (r2*mySin(phi1) - r1*mySin(phi2)) / d;
		float y = (-r2*myCos(phi1) + r1*myCos(phi2)) / d;
		
		return new PVector(x, y); 
	}

	public ArrayList<PVector> getIntersections(List<PVector> lines) { 
		ArrayList<PVector> intersections = new ArrayList<PVector>(); 
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				float r1 = line1.x, r2 = line2.x;
				float phi1 = line1.y, phi2 = line2.y;
				float d = myCos(phi2)*mySin(phi1) - myCos(phi1)*mySin(phi2);
				float x = (r2*mySin(phi1) - r1*mySin(phi2)) / d;
				float y = (-r2*myCos(phi1) + r1*myCos(phi2)) / d;
				intersections.add(new PVector(x, y));
			}
		}
		return intersections; 
	}
	
	// calcul des diagonales de la plaque d'equation y = ax+b
	public PVector getDiagonal(PVector p1, PVector p2){
		float a = (p1.y - p2.y)/(p1.x - p2.x);
		float b = p1.y - a*p1.x;
		return new PVector(a, b);
	}
	
	// calcule le point d'intersection (x, y) des diagonales de la plaque 
	public PVector getCenter(PVector d1, PVector d2){
		float x = (d2.y - d1.y)/(d1.x - d2.x);
		float y = d2.x * x + d2.y;
		return new PVector(x, y);
	}

	public List<PVector> sortCorners(List<PVector> quad){
		// Sort corners so that they are ordered clockwise
		if(!(quad.size() < 4)){
			PVector center = getCenter(getDiagonal(quad.get(0), quad.get(2)), getDiagonal(quad.get(1), quad.get(3)));
			Collections.sort(quad, new CWComparator(center));
	
			// Re-order the corners so that the first one is the closest to the
			// origin (0,0) of the image.
			int idx = 0;
			PVector origin = new PVector(0, 0);
			float smallestDistance = quad.get(0).dist(origin);
			for(int i=1; i<quad.size(); i++){
				PVector c = quad.get(i);	
				float dist = c.dist(origin);
				if(dist < smallestDistance){
					smallestDistance = dist;
					idx = i;
				}
			}
			Collections.rotate(quad, idx);
			/*game.fill(255, 128, 0);
			game.line(quad.get(0).x, quad.get(0).y, quad.get(2).x, quad.get(2).y);
			game.line(quad.get(1).x, quad.get(1).y, quad.get(3).x, quad.get(3).y);
			game.ellipse(center.x, center.y, 10, 10);*/
		}
		return quad;
	}
	

	class HoughComparator implements Comparator<Integer> { 
		
		int[] accumulator;
		public HoughComparator(int[] accumulator) {
			this.accumulator = accumulator; 
		}
		@Override
		public int compare(Integer l1, Integer l2) { 
			return (accumulator[l1] > accumulator[l2] 
					|| (accumulator[l1] == accumulator[l2] && l1 < l2)) ?
							-1 : 1;
		} 
	}
	
	static class CWComparator implements Comparator<PVector> { 

		PVector center;
		public CWComparator(PVector center) {
			this.center = center; 
		}
		@Override
		public int compare(PVector b, PVector d) { 
			if(Math.atan2(b.y-center.y,b.x-center.x)<Math.atan2(d.y-center.y,d.x-center.x))
				return -1; 
			else return 1;
		} 
	}
}
