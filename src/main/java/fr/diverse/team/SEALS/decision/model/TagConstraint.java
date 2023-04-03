package fr.diverse.team.SEALS.decision.model;

import fr.diverse.team.SEALS.decision.model.visitor.ImpactEvaluationVisitor;

public class TagConstraint {
	
	public enum TAG {SUPERIOR, INFERIOR, SUPERIOR_EQ, INFERIOR_EQ, EQUAL, NOTEQUAL}

	private GoalModelingElement elem;
	private TAG tag;
	private Double value;

	public TagConstraint(GoalModelingElement elem, TAG tag, Double value) {
		this.elem = elem;
		this.tag = tag;
		this.value = value;
	}
	
	public boolean isValid() {
		double elementValue = elem.accept(new ImpactEvaluationVisitor());
		switch(tag) {
		case SUPERIOR:
			return elementValue > value;
		case INFERIOR:
			return elementValue < value;
		case SUPERIOR_EQ:
			return elementValue >= value;
		case INFERIOR_EQ:
			return elementValue <= value;
		case NOTEQUAL:
			return elementValue != value;
		case EQUAL:
			return elementValue == value;
		}
		return false;
	}
	
	public GoalModelingElement getElem() {
		return elem;
	}

	public TAG getTag() {
		return tag;
	}

	public Double getValue() {
		return value;
	}

}
