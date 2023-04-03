package fr.diverse.team.SEALS.lang.adaptation;

import fr.diverse.team.SEALS.lang.SelfAdaptableLanguage;
import fr.diverse.team.SEALS.lang.semantics.Node;

public abstract class FeedbackLoop <Lang extends SelfAdaptableLanguage<AdaptationCtx>,
									AdaptationCtx extends AdaptationContext<?>>{

	private AdaptationCtx adaptationContext;
	
	protected FeedbackLoop(AdaptationCtx adaptationCtx) {
		this.adaptationContext = adaptationCtx;
	}
	
	public final void loop() {
		monitor();
		boolean needNewPlan = analyze();
		if(needNewPlan) {
			plan();
			execute();
		}
	}
	
	abstract protected void    monitor();
	abstract protected boolean analyze();
	abstract protected void    plan   ();
	abstract protected void    execute();
	
	abstract public boolean isTriggered(Node n);

	protected final AdaptationCtx getAdaptationContext() {
		return adaptationContext;
	}
}
