package fr.diverse.team.SEALS.decision.model;

import fr.diverse.team.SEALS.decision.model.visitor.IGoalVisitor;

public final class Variable extends GoalModelingElement {
	
	private Bounds bound;
	private Boolean maximize;
	private Double value;
	
	

	public Variable(String ID) {
		super(ID);
		this.bound = new Bounds(-1, 1);
	}
	
	public Variable(String ID, double lowerBound) {
		super(ID);
		this.bound = new Bounds(lowerBound, 1);
	}
	
	public Variable(String ID, double lowerBound, double higherBound) {
		super(ID);
		this.bound = new Bounds(lowerBound, higherBound);
	}
	
	@Override
	public <T> T accept(IGoalVisitor<T> visitor) {
		return visitor.visitVariable(this);
	}
	
	@Override
	public void addContribution(GoalModelingElement elem, double impact){
		System.err.println("WARNING : Add contribution to a variable is unnecessary");
	}
	
	public double getValue(Bounds b) {
		if(value != null) {
			if (b.contains(this.value)) return value;
			if(b.getLow() > this.value) return b.getLow();
			return b.getHigh();
		}
		
		if(b == null) {
			return maximize ? getLowerBound() : getHigherBound();
		}
		Bounds instanceBounds = this.bound.intersection(b);
		return maximize ? instanceBounds.getLow() : instanceBounds.getHigh();
	}
	
	public void setValue(double value) {
		this.value = value;
		this.maximize = null;
	}

	public void setMaximize(boolean value) {
		this.maximize = value;
		this.value = null;
	}
	
	public double getHigherBound() {
		return bound.getHigh();
	}

	private void setHigherBound(double higherBound) {
		this.bound.setHigh(higherBound);
	}

	public double getLowerBound() {
		return bound.getLow();
	}

	private void setLowerBound(double lowerBound) {
		this.bound.setLow(lowerBound);
	}

}
