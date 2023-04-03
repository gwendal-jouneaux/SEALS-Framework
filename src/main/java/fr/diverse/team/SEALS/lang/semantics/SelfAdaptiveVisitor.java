package fr.diverse.team.SEALS.lang.semantics;

public interface SelfAdaptiveVisitor {
	public Object dispatch(Node node, Object executionCtx);
	public Object dispatch(AdaptableNode node, Object executionCtxm, SemanticsAdaptationInterface config);
}
