package fr.diverse.team.SEALS.lang.semantics;

public abstract class AdaptiveOperation<AN extends AdaptableNode<Interface>, Interface extends SemanticsAdaptationInterface> extends Operation<AN> {

	@Override
	final public Object execute(SelfAdaptiveVisitor vis, AN node, Object execCtx) {
		return null;
	}
	
	abstract public Object execute(SelfAdaptiveVisitor vis, AN node, Object execCtx, Interface config);

}
