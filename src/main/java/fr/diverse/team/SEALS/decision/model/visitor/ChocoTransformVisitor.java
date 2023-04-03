package fr.diverse.team.SEALS.decision.model.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.arithmetic.CArExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import fr.diverse.team.SEALS.decision.model.Bounds;
import fr.diverse.team.SEALS.decision.model.Goal;
import fr.diverse.team.SEALS.decision.model.GoalModelingElement;
import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.decision.model.TagConstraint;
import fr.diverse.team.SEALS.decision.model.TagConstraint.TAG;
import fr.diverse.team.SEALS.decision.model.Task;
import fr.diverse.team.SEALS.decision.model.Variable;

public class ChocoTransformVisitor implements IGoalVisitor<RealVar> {
	
	public static final double PRECISION = 0.0001;
	
	private Map<GoalModelingElement, RealVar> elementsVariables;
	private Map<String, Bounds> constraints;
	private Map<RealVar, Variable> variables;
	private Model model;

	public ChocoTransformVisitor(Map<String, Bounds> constraints) {
		this.elementsVariables = new HashMap<>();
		this.constraints = constraints;
		this.variables = new HashMap<>();
		this.model = new Model("Self-Adaptive Language Goal-Model");
	}
	
	public Model getModel() {
		return model;
	}

	public Map<RealVar, Variable> getVariables() {
		return variables;
	}

	private RealVar visitGeneralCase(GoalModelingElement elem) {
		if(elem.getFlattenedImpact() != null) {
			RealVar value = visitFlattened(elem);
			return value;
		}
		
		CArExpression weightedSum = model.realVar("0", 0);
		RealVar out = model.realVar(-1, 1, PRECISION);

		Set<GoalModelingElement> children = elem.getInputLinks().keySet();
		for (GoalModelingElement element : children) {
			if(element == null) continue;
			RealVar val = element.accept(this);
			if(weightedSum == null) {
				weightedSum = val.mul(elem.getInputLinks().get(element));
			} else {
				weightedSum = weightedSum.add(val.mul(elem.getInputLinks().get(element)));
			}
		}
		CArExpression bornedWeightedSum = weightedSum.min(1).max(-1);
		
		Set<TagConstraint> constraints = elem.getTags();
		for (TagConstraint tagConstraint : constraints) {
			RealVar tagElem = tagConstraint.getElem().accept(this);
			bornedWeightedSum = addTag(bornedWeightedSum, tagElem, tagConstraint.getTag(), tagConstraint.getValue());
		}
		model.post(out.eq(bornedWeightedSum).equation());
		return out;
	}
	
	private RealVar visitFlattened(GoalModelingElement elem) {
		Map<GoalModelingElement, FlattenedImpact> flattenedImpacts = elem.getFlattenedImpact();
		CArExpression weightedSum = null;
		RealVar out = model.realVar(-1, 1, PRECISION);
		
		// for each child element
		for (GoalModelingElement element : flattenedImpacts.keySet()) {
			RealVar value = element.accept(this);
			
			FlattenedImpact fi = flattenedImpacts.get(element);
			List<Double> weights = fi.weights;
			List<Set<TagConstraint>> constraints = fi.pathConstraints;
			
			// for each path to the child (create paths from child to parent)
			for (int i = 0; i < weights.size(); i++) {
				CArExpression path = value;
				Set<TagConstraint> pathConstraints = constraints.get(i);
				
				//for each tag of the path (add tag to the path)
				for (TagConstraint tagConstraint : pathConstraints) {
					RealVar tagElem = tagConstraint.getElem().accept(this);
					path = addTag(path, tagElem, tagConstraint.getTag(), tagConstraint.getValue());
				}
				
				// add path to the parent weighted sum
				if(weightedSum == null) {
					weightedSum = path.mul(weights.get(i));
				} else {
					weightedSum = weightedSum.add(path.mul(weights.get(i)));
				}
			}
		}
		// Ensure -1 <= wightedSum <= 1
		CArExpression bornedWeightedSum = weightedSum.min(1).max(-1);
		model.post(out.eq(bornedWeightedSum).equation()); // add the constraint parent value == weightedSum
		return out;
	}
	
	private CArExpression addTag(CArExpression taggedElement, RealVar testedElement, TAG op, double threshold) {
		IntVar tagFactor = model.intVar(0, 1);
		BoolVar condition = null;
		
		switch(op) {
			case INFERIOR:
				condition = testedElement.lt(threshold).reify();
				break;
			case SUPERIOR:
				condition = testedElement.gt(threshold).reify();
				break;
			case INFERIOR_EQ:
				condition = testedElement.le(threshold).reify();
				break;
			case SUPERIOR_EQ:
				condition = testedElement.ge(threshold).reify();
				break;
			case EQUAL:
				condition = testedElement.eq(threshold).reify();
				break;
			case NOTEQUAL:
				condition = testedElement.eq(threshold).reify().not();
				break;
		}
		
		model.ifThenElse(condition, model.arithm(tagFactor, "=", 1), model.arithm(tagFactor, "=", 0));
		RealVar realFactor = model.realVar(-1, 1, PRECISION); // model.realIntView(tagFactor, PRECISION);
		model.eq(realFactor, tagFactor).post();
		CArExpression out = taggedElement.mul(realFactor);
		return out;
		
	}

	@Override
	public RealVar visitGoal(Goal goal) {
		if(elementsVariables.containsKey(goal)) {
			return elementsVariables.get(goal);
		}
		
		RealVar out = visitGeneralCase(goal);
		elementsVariables.put(goal, out);
		return out;
	}

	@Override
	public RealVar visitSoftGoal(Softgoal softgoal) {
		if(elementsVariables.containsKey(softgoal)) {
			return elementsVariables.get(softgoal);
		}
		
		RealVar out = visitGeneralCase(softgoal);
		elementsVariables.put(softgoal, out);
		return out;
	}

	@Override
	public RealVar visitTask(Task task) {
		if(elementsVariables.containsKey(task)) {
			return elementsVariables.get(task);
		}
		
		RealVar out = visitGeneralCase(task);		
		elementsVariables.put(task, out);
		return out;
	}

	@Override
	public RealVar visitVariable(Variable variable) {
		if(elementsVariables.containsKey(variable)) {
			return elementsVariables.get(variable);
		}
		
		RealVar out = model.realVar(variable.ID, variable.getLowerBound(), variable.getHigherBound(), PRECISION);
		
		if(constraints.containsKey(variable.ID)) {
			Bounds bounds = constraints.get(variable.ID);
			model.post(out.ge(bounds.getLow() ).equation());
			model.post(out.le(bounds.getHigh()).equation());
		}
		
		elementsVariables.put(variable, out);
		variables.put(out, variable);
		return out;
	}

	@Override
	public RealVar visitResource(Resource resource) {
		if(elementsVariables.containsKey(resource)) {
			return elementsVariables.get(resource);
		}

		RealVar out = model.realVar(resource.ID, resource.monitor(), resource.monitor(), PRECISION);
		elementsVariables.put(resource, out);
		return out;
	}

}
