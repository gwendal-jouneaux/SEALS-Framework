package fr.diverse.team.SEALS.decision.model.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.diverse.team.SEALS.decision.model.Bounds;
import fr.diverse.team.SEALS.decision.model.Goal;
import fr.diverse.team.SEALS.decision.model.GoalModelingElement;
import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.decision.model.Task;
import fr.diverse.team.SEALS.decision.model.Variable;

public class ImpactEvaluationVisitor implements IGoalVisitor<Double>{
	private Map<GoalModelingElement, Double> values;
	Map<String, Bounds> bounds;
	
	public ImpactEvaluationVisitor() {
		values = new HashMap<>();
		this.bounds = new HashMap<>();
	}
	
	public ImpactEvaluationVisitor(Map<GoalModelingElement, Double> values, Map<String, Bounds> bounds) {
		this.values = values;
		this.bounds = bounds;
	}
	
	public ImpactEvaluationVisitor(Map<String, Bounds> bounds) {
		values = new HashMap<>();
		this.bounds = bounds;
	}

	@Override
	public Double visitGoal(Goal goal) {
		return visitGeneralCase(goal);
	}

	@Override
	public Double visitSoftGoal(Softgoal softgoal) {
		return visitGeneralCase(softgoal);
	}

	@Override
	public Double visitTask(Task task) {
		return visitGeneralCase(task);
	}

	@Override
	public Double visitVariable(Variable variable) {
		if(! variable.verifyTags()) return 0.0;
		if (values.containsKey(variable)) {
			return values.get(variable);
		}
		Bounds b = bounds.get(variable.ID);
		values.put(variable, variable.getValue(b));
		return variable.getValue(b);
	}

	@Override
	public Double visitResource(Resource resource) {
		if(! resource.verifyTags()) return 0.0;
		if (values.containsKey(resource)) {
			return values.get(resource);
		}
		values.put(resource, resource.monitor());
		return resource.monitor();
	}
	
	private Double visitGeneralCase(GoalModelingElement elem) {
		if(! elem.verifyTags()) return 0.0;
		if (values.containsKey(elem)) {
			return values.get(elem);
		}
		
		Double value = 0.0;
		
		
		if(elem.getFlattenedImpact() != null) {
			value = visitFlattened(elem);
		} else {
			Set<GoalModelingElement> children = elem.getInputLinks().keySet();
			for (GoalModelingElement element : children) {
				if(element == null) continue;
				Double val = element.accept(this);
				value += val * elem.getInputLinks().get(element);
			}
		}
		
		
		value = Math.min(1, Math.max(value, -1));
		values.put(elem, value);
		return value;
	}
	
	private Double visitFlattened(GoalModelingElement elem) {
		Map<GoalModelingElement, FlattenedImpact> flattenedImpacts = elem.getFlattenedImpact();
		Double out = 0.0;
		for (GoalModelingElement element : flattenedImpacts.keySet()) {
			FlattenedImpact fi = flattenedImpacts.get(element);
			double value = element.accept(this);
			double impact = fi.impactForValue(value);
			out += impact;
		}
		return out;
	}

	public Map<GoalModelingElement, Double> getValues() {
		return values;
	}

}
