package com.CA_Project2;
import com.CA_Project2.MainPApplet.pt;
import com.CA_Project2.MainPApplet.vec;
/*
 * Uses the polar equation of a log Spiral to generate one given two points on the spiral(p1,p2) and the center of the spiral(F).
 * 
 * Equation(Polar):
 * r = a * exp^(b * theta)
 * 
 * Recalculating a and b whenever user moves the points.
 */
public class SpiralGenerator {
	
	//Control points
	private pt p1;
	private pt p2;
	
	//Center
	private pt f;
	
	//Equation params
	private float a;
	private float b;
	
	//Cache PApplet Ref
	private MainPApplet pApp;
	
	//Cache points
	private pt[] preCompPts;
	
	private float increment = (float)Math.PI*2/5.0f;
	
	public boolean pattern = true;
	public boolean source = false;
	
	//Constructor
	public SpiralGenerator(pt p1, pt p2, pt f){
		this.p1 = p1;
		this.p2 = p2;
		this.f = f;
		
		//Get instance from global static reference
		pApp = MainPApplet.Instance;
		
		//Create the cache
		preCompPts = new pt[1000];
		for(int i = 0; i< 1000;i++){
			preCompPts[i] = pApp.P();
		}
		
		calcParams();
		
	}
	
	// ensure radii ratio is consistently less than 1
		private boolean constrainingError(pt pIn, int ident)
		{
			float r1, r2;
			vec v1, v2, vf;
			
			// inversion check
			if (ident == 0)
			{   // f
				r1 = pApp.d(pIn, p1);
				r2 = pApp.d(pIn, p2);
				if (r1 >= r2*0.99) return true;
			}
			else if (ident == 1)
			{   // p1
				r1 = pApp.d(f, pIn);
				r2 = pApp.d(f, p2);
				if (r1 >= r2*0.99) return true;
			}
			else if (ident == 2)
			{   // p2
				r1 = pApp.d(f, p1);
				r2 = pApp.d(f, pIn);
				if (r1 >= r2*0.99) return true;
			}
		
			// check if points are about to be colinear which is forbidden
			if (ident == 0)
			{   // f
				v1 = pApp.V(pIn, p1);
				v1.normalize();
				v2 = pApp.V(pIn, p2);
				v2.normalize();
				if (pApp.det(v1,  v2) <= 0.05) return true;
			}
			else if (ident == 1)
			{   // p1
				v1 = pApp.V(f, pIn);
				v1.normalize();
				v2 = pApp.V(f, p2);
				v2.normalize();
				if (pApp.det(v1,  v2) <= 0.05) return true;
			}
			else if (ident == 2)
			{   // p2
				v1 = pApp.V(f, p1);
				v1.normalize();
				v2 = pApp.V(f, pIn);
				v2.normalize();
				if (pApp.det(v1,  v2) <= 0.05) return true;
			}
			
			return false;
		}
	
	//Recalculates a and b
	private void calcParams(){
		//Get r1 and r2, distance from center to p1 and p2
		float r1 = pApp.d(f, p1);
		float r2 = pApp.d(f, p2);
		
		vec fp1 = pApp.V(f, p1);
		vec fp2 = pApp.V(f, p2);
		
		//sUBTENDING ANGLE
		float theta1 = pApp.angle(fp2, fp1);
		//Set params
		this.b = (float) ((float) Math.log(r1/r2)/theta1);
		this.a = (float)((float) r2/ Math.exp(this.b * 2 * Math.PI));
		
		
		int ptCtr = 0;
		//Recompute points in cache
		for(float th = (float) ( 2*Math.PI); ptCtr < preCompPts.length ; th-=0.1){
			//Get polar co-ordinate R
			
			
			pt result = computeVal(th);
			
			preCompPts[ptCtr].x = result.x;
			preCompPts[ptCtr].y = result.y;
			ptCtr++;
			
		}

	}
	
	public pt computeVal(float th){
		float R = (float) (this.a * Math.exp(this.b * th));
		
		vec fp12 = pApp.V(f, p2).normalize();
		fp12.rotateBy((float) (-(2*Math.PI - th))).scaleBy(R);
		
		pt result = pApp.P(f, fp12);
		
		return result;
	}
	
	//Call this in PApplets draw function to render the spiral
	public void draw(){

		//Draw control points
		pApp.pen(pApp.red, 20.0f);
		pApp.show(f);
		//Draw control points
		pApp.pen(pApp.black, 20.0f);
		pApp.show(p1);
		//Draw control points
		pApp.pen(pApp.green, 20.0f);
		pApp.show(p2);
		
		//Draw points from cache
		pApp.noFill();

		if(pattern)
		{
			pApp.pen(pApp.green, 2.0f);
			pApp.beginShape();
			
			for(float pI = (float) (2*Math.PI); pI> -50*Math.PI ; pI-=increment){
				pt p = computeVal(pI);
				pApp.vertex(p.x, p.y);
			}
			pApp.endShape();
		}
		
		if(source)
		{
			pApp.pen(pApp.sand, 1.0f);
			pApp.beginShape();
			for(int i=0;i<300;i++){
				pt p = preCompPts[i];
				pApp.vertex(p.x, p.y);
				pApp.show(p);
			}
			pApp.endShape();
		}
	}
	
	//Call in mouse dragged to interact with spiral
	public void interact(float mX, float mY){
		pt mouse = pApp.P(mX, mY);
		if(pApp.d(p1, mouse) < 10.0f) this.setP1(mouse);
		else if(pApp.d(p2, mouse) < 10.0f) this.setP2(mouse);
		else if(pApp.d(f, mouse) < 10.0f) this.setF(mouse);
	}
	
	
	public void makeDetailed(){
		if(increment >  Math.PI/4) increment-=0.005f;
	}
	
	public void makeCoarse(){
		increment+=0.005f;
	}
	
	public void setIncrement(int inc){
		if (inc % 2 == 0){
			increment = pApp.PI*1.0f / (inc/2.0f);
		}
		else{
			increment = pApp.PI*2.0f / inc;
		}
	}

	public pt getP1() {
		return p1;
	}

	public void setP1(pt p1) {
		if (constrainingError(p1,1)) return;
		this.p1 = p1;
		//Recalculate
		calcParams();
	}

	public pt getP2() {
		return p2;
	}

	public void setP2(pt p2) {
		if (constrainingError(p2,2)) return;
		this.p2 = p2;
		//Recalculate
		calcParams();
	}

	public pt getF() {
		return f;
	}

	public void setF(pt f) {
		if (constrainingError(f,0)) return;
		this.f = f;
		//Recalculate
		calcParams();
	}

}
