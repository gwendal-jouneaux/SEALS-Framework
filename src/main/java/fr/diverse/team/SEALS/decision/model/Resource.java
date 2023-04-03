package fr.diverse.team.SEALS.decision.model;

import fr.diverse.team.SEALS.decision.model.visitor.IGoalVisitor;

public final class Resource extends GoalModelingElement {
	
	Double value;

	public Resource(String ID) {
		super(ID);
	}
	
	public void setMonitoredValue(double val) {
		value = val;
	}
	
	public double monitor() {
		return value;
	}
	
	@Override
	public void addContribution(GoalModelingElement elem, double impact){
		System.err.println("WARNING : Add contribution to a resource is unnecessary");
	}

	@Override
	public <T> T accept(IGoalVisitor<T> visitor) {
		return visitor.visitResource(this);
	}

}
