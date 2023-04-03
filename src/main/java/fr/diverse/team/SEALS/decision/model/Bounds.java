package fr.diverse.team.SEALS.decision.model;

public class Bounds {
	
	private double low;
	private double high;

	public Bounds() {
		low  = 0;
		high = 0;
	}
	
	public Bounds(double value) {
		this.low  = value;
		this.high = value;
	}
	
	public Bounds(double low, double high) {
		this.low  = low;
		this.high = high;
	}
	
	public Bounds add(Bounds b) {
		low += b.low;
		high += b.high;
		return this;
	}
	
	public Bounds factor(double d) {
		 low *= d;
		high *= d;
		return this;
	}
	
	public Bounds intersection(Bounds b) {
		boolean intersect = (this.contains(b.high)) || (b.contains(this.high));
		if(! intersect) return null;
		
		return new Bounds(Math.max(low, b.low), Math.min(high, b.high));
	}
	
	public boolean contains(double value) {
		return value <= high && value >= low;
	}

	public double getLow() {
		return low;
	}

	public double getHigh() {
		return high;
	}
	
	public void setLow(double low) {
		this.low = low;
	}

	public void setHigh(double high) {
		this.high = high;
	}

}
