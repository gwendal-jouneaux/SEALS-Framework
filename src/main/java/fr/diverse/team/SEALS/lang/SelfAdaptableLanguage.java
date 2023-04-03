package fr.diverse.team.SEALS.lang;

import fr.diverse.team.SEALS.lang.adaptation.AdaptationContext;
import fr.diverse.team.SEALS.lang.adaptation.FeedbackLoop;
import fr.diverse.team.SEALS.lang.semantics.SelfAdaptiveVisitor;
import fr.diverse.team.SEALS.module.adaptation.SelfAdaptationModule;

public abstract class SelfAdaptableLanguage <AdaptationCtx extends AdaptationContext<?>>{
	
	private static SelfAdaptableLanguage<?> INSTANCE;

	private AdaptationCtx       				  adaptationContext;
	private FeedbackLoop<?, AdaptationCtx>        feedbackLoop;
	private SelfAdaptiveVisitor 				  visitor;
	
	protected SelfAdaptableLanguage() {
		this.adaptationContext = createAdaptationContext();
		this.feedbackLoop = createFeedbackLoop(this.adaptationContext);
		this.visitor = createVisitor();
		if(INSTANCE == null) INSTANCE = this;
	}
	
	public void registerModule(SelfAdaptationModule<AdaptationCtx, ?, ?> module) {
		adaptationContext.registerModule(module);
	}
	
	public AdaptationCtx getAdaptationContext() {
		return this.adaptationContext;
	}
	
	public FeedbackLoop<?, AdaptationCtx> getFeedbackLoop() {
		return this.feedbackLoop;
	}
	
	public SelfAdaptiveVisitor getVisitor() {
		return this.visitor;
	}
	
	abstract protected AdaptationCtx       				   createAdaptationContext();
	abstract protected FeedbackLoop<?, AdaptationCtx>  	   createFeedbackLoop(AdaptationCtx ctx);
	abstract protected SelfAdaptiveVisitor 				   createVisitor();
	
	public static SelfAdaptableLanguage<?> INSTANCE() {
		return INSTANCE;
	}
}
