package fr.diverse.team.SEALS.decision.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.diverse.team.SEALS.decision.model.TagConstraint.TAG;
import fr.diverse.team.SEALS.decision.model.visitor.FlattenedImpact;
import fr.diverse.team.SEALS.decision.model.visitor.IGoalVisitor;

public abstract class GoalModelingElement {
	
	final public String ID;
	
	private Map<GoalModelingElement, Double> inputLinks;
	private Set<TagConstraint> tags;
	private Map<GoalModelingElement, FlattenedImpact> flattenedImpact;
	
	public abstract <T> T accept(IGoalVisitor<T> visitor);

	public GoalModelingElement(String ID) {
		inputLinks = new HashMap<GoalModelingElement, Double>();
		tags = new HashSet<>();
		flattenedImpact = null;
		this.ID = ID;
	}
	
	public void addContribution(GoalModelingElement elem, double impact) {
		getInputLinks().put(elem, impact);
	}
	
	public void addTag(GoalModelingElement elem, TAG tag, double value) {
		tags.add(new TagConstraint(elem, tag, value));
	}
		
	public boolean verifyTags() {
		for (TagConstraint tc : tags) {
			if(! tc.isValid()) {
				return false;
			}
		}
		return true;
	}
	
	public Map<GoalModelingElement, Double> getInputLinks() {
		return inputLinks;
	}

	public Set<TagConstraint> getTags() {
		return tags;
	}
	
	public void setFlattenedImpact(Map<GoalModelingElement, FlattenedImpact> flattenedImpact) {
		this.flattenedImpact = flattenedImpact;
	}
	
	public Map<GoalModelingElement, FlattenedImpact> getFlattenedImpact() {
		return flattenedImpact;
	}

}
