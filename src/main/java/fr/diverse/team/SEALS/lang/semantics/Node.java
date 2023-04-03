package fr.diverse.team.SEALS.lang.semantics;

import fr.diverse.team.SEALS.lang.SelfAdaptableLanguage;
import fr.diverse.team.SEALS.lang.adaptation.FeedbackLoop;

public interface Node {
	default public Object accept(SelfAdaptiveVisitor vis, Object executionContext) {
		FeedbackLoop<?,?> feedbackLoop = SelfAdaptableLanguage.INSTANCE().getFeedbackLoop();
		
		if(feedbackLoop.isTriggered(this)) {
			feedbackLoop.loop();
		}
		return vis.dispatch(this, executionContext);
	}
}
