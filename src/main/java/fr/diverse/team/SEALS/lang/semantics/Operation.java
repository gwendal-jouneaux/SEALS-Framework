package fr.diverse.team.SEALS.lang.semantics;

public abstract class Operation<N extends Node> {
	abstract public Object execute(SelfAdaptiveVisitor vis, N node, Object execCtx);
}
