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

public class ModelFlatteningVisitor implements IGoalVisitor<Map<GoalModelingElement, FlattenedImpact>>{

	@Override
	public Map<GoalModelingElement, FlattenedImpact> visitGoal(Goal goal) {
		if(goal.getFlattenedImpact() != null) return goal.getFlattenedImpact();
		Map<GoalModelingElement, FlattenedImpact> out = visitGeneralCase(goal);
		goal.setFlattenedImpact(out);
		return out;
	}

	@Override
	public Map<GoalModelingElement, FlattenedImpact> visitSoftGoal(Softgoal softgoal) {
		if(softgoal.getFlattenedImpact() != null) return softgoal.getFlattenedImpact();
		Map<GoalModelingElement, FlattenedImpact> out = visitGeneralCase(softgoal);
		softgoal.setFlattenedImpact(out);
		return out;
	}

	@Override
	public Map<GoalModelingElement, FlattenedImpact> visitTask(Task task) {
		if(task.getFlattenedImpact() != null) return task.getFlattenedImpact();
		Map<GoalModelingElement, FlattenedImpact> out = visitGeneralCase(task);
		task.setFlattenedImpact(out);
		return out;
	}

	@Override
	public Map<GoalModelingElement, FlattenedImpact> visitVariable(Variable variable) {
		if(variable.getFlattenedImpact() != null) return variable.getFlattenedImpact();
		Map<GoalModelingElement, FlattenedImpact> out = new HashMap<GoalModelingElement, FlattenedImpact>();
		FlattenedImpact fi = new FlattenedImpact();
		fi.addConstraints(variable.getTags());
		out.put(variable, fi);
		variable.setFlattenedImpact(out);
		return out;
	}

	@Override
	public Map<GoalModelingElement, FlattenedImpact> visitResource(Resource resource) {
		if(resource.getFlattenedImpact() != null) return resource.getFlattenedImpact();
		Map<GoalModelingElement, FlattenedImpact> out = new HashMap<GoalModelingElement, FlattenedImpact>();
		FlattenedImpact fi = new FlattenedImpact();
		fi.addConstraints(resource.getTags());
		out.put(resource, fi);
		resource.setFlattenedImpact(out);
		return out;
	}
	
	private Map<GoalModelingElement, FlattenedImpact> visitGeneralCase(GoalModelingElement elem) {
		Map<GoalModelingElement, FlattenedImpact> out = new HashMap<GoalModelingElement, FlattenedImpact>();
		Set<GoalModelingElement> children = elem.getInputLinks().keySet();
		for (GoalModelingElement child : children) {
			if(child == null) continue;
			Map<GoalModelingElement, FlattenedImpact> val = child.accept(this);
			double weight = elem.getInputLinks().get(child);
			
			for (GoalModelingElement goalModelingElement : val.keySet()) {
				FlattenedImpact fi = val.get(goalModelingElement);
				fi.addWeight(weight);
				if(out.containsKey(goalModelingElement)) {
					out.get(goalModelingElement).merge(fi);
				} else {
					out.put(goalModelingElement, fi);
				}
			}
		}
		for (FlattenedImpact fi : out.values()) {
			fi.addConstraints(elem.getTags());
		}
		return out;
	}

}
