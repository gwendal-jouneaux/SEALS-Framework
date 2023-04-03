package fr.diverse.team.SEALS.decision.model.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.diverse.team.SEALS.decision.model.Goal;
import fr.diverse.team.SEALS.decision.model.GoalModelingElement;
import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.decision.model.Task;
import fr.diverse.team.SEALS.decision.model.Variable;

public class VariableAssessmentVisitor implements IGoalVisitor<Void>{
	private Map<Variable, Double> globalPathes;
	double currentPath;
	
	public VariableAssessmentVisitor() {
		globalPathes = new HashMap<>();
		currentPath = 1;
	}

	@Override
	public Void visitGoal(Goal goal) {
		visitGeneralCase(goal);
		return null;
	}

	@Override
	public Void visitSoftGoal(Softgoal softgoal) {
		visitGeneralCase(softgoal);
		return null;
	}

	@Override
	public Void visitTask(Task task) {
		visitGeneralCase(task);
		return null;
	}

	@Override
	public Void visitVariable(Variable variable) {
		if(! variable.verifyTags()) return null;
		
		Double currentValue = globalPathes.get(variable);
		if(currentValue == null) {
			currentValue = 0.0;
		}
		currentValue = currentValue + currentPath;
		globalPathes.put(variable, currentValue);
		return null;
	}

	@Override
	public Void visitResource(Resource resource) {
		return null;
	}
	
	private Void visitGeneralCase(GoalModelingElement elem) {
		if(! elem.verifyTags()) return null;
		
		Double path = currentPath;
		
		if(elem.getFlattenedImpact() != null) {
			visitFlattened(elem);
		} else {
			Set<GoalModelingElement> children = elem.getInputLinks().keySet();
			for (GoalModelingElement element : children) {
				if(element == null) continue;
				currentPath = path * elem.getInputLinks().get(element);
				element.accept(this);
			}
		}
		currentPath = path;
		return null;
	}
	
	private Void visitFlattened(GoalModelingElement elem) {
		double path = currentPath;
		Map<GoalModelingElement, FlattenedImpact> flattenedImpacts = elem.getFlattenedImpact();
		for (GoalModelingElement element : flattenedImpacts.keySet()) {
			if(element instanceof Variable) {
				FlattenedImpact fi = flattenedImpacts.get(element);
				double impact = fi.impactForValue(1);
				currentPath = path * impact;
				element.accept(this);
			}
		}
		return null;
	}
	
	
	public Map<Variable, Double> getGlobalPathes() {
		return globalPathes;
	}

}
