package fr.diverse.team.SEALS.decision.model;

import fr.diverse.team.SEALS.decision.model.visitor.IGoalVisitor;

public class Task extends GoalModelingElement {

	public Task(String ID) {
		super(ID);
	}

	@Override
	public <T> T accept(IGoalVisitor<T> visitor) {
		return visitor.visitTask(this);
	}

}
