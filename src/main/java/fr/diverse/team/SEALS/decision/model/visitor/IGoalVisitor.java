package fr.diverse.team.SEALS.decision.model.visitor;

import fr.diverse.team.SEALS.decision.model.Goal;
import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.decision.model.Task;
import fr.diverse.team.SEALS.decision.model.Variable;

public interface IGoalVisitor<T> {
	
	public T visitGoal(Goal goal);
	public T visitSoftGoal(Softgoal softgoal);
	public T visitTask(Task task);
	public T visitVariable(Variable variable);
	public T visitResource(Resource resource);
}
