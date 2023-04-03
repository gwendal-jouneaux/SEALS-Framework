package fr.diverse.team.SEALS.lang.adaptation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.diverse.team.SEALS.decision.model.Goal;
import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.decision.model.Variable;
import fr.diverse.team.SEALS.decision.model.visitor.ChocoTransformVisitor;
import fr.diverse.team.SEALS.decision.model.visitor.IGoalVisitor;
import fr.diverse.team.SEALS.decision.model.visitor.ImpactEvaluationVisitor;
import fr.diverse.team.SEALS.decision.model.visitor.VariableAssessmentVisitor;
import fr.diverse.team.SEALS.lang.SelfAdaptableLanguage;
import fr.diverse.team.SEALS.module.adaptation.SelfAdaptationModule;

public abstract class AdaptationContext<Lang extends SelfAdaptableLanguage<?>> {
	
	private ModuleRegistry moduleRegistry;
	private Map<String, Softgoal> propertiesOfInterest;
	private Map<String, Double> expectedTradeOff;
	private List<Resource> environmentModel;
	private Goal tradeOffModel;
	
	public AdaptationContext() {
		moduleRegistry = new ModuleRegistry();
		
		tradeOffModel = new Goal("GlobalTradeOff");
		
		environmentModel = new ArrayList<Resource>();
		for (Resource resource : environment()) {
			environmentModel.add(resource);
		}
		
		propertiesOfInterest = new HashMap<>();
		for (String softgoalID : propertiesOfInterest()) {
			Softgoal soft = new Softgoal(softgoalID);
			tradeOffModel.addContribution(soft, 1/propertiesOfInterest().length);
			propertiesOfInterest.put(softgoalID, soft);
		}
	}
	
	public final void registerModule(SelfAdaptationModule<?, ?, ?> module) {
		moduleRegistry.register(module);
		module.afterRegister(this);
	}
	
	public final void connectModuleSoftgoal(String id, Softgoal soft) {
		propertiesOfInterest.get(id).addContribution(soft, 1);
	}
	
	public final <T> T evaluateModelWith(IGoalVisitor<T> visitor) {
		for (String id : expectedTradeOff.keySet()) {
			tradeOffModel.updateLink(id, expectedTradeOff.get(id));
		}
		return tradeOffModel.accept(visitor);
	}
	
	public final ImpactEvaluationVisitor evaluateModel() {
		ImpactEvaluationVisitor vis = new ImpactEvaluationVisitor();
		evaluateModelWith(vis);
		return vis;
	}
	public final void assessModelVariables() {
		VariableAssessmentVisitor vis = new VariableAssessmentVisitor();
		evaluateModelWith(vis);
		Map<Variable, Double> pathes = vis.getGlobalPathes();
		for (Variable v : pathes.keySet()) {
			v.setMaximize(pathes.get(v)>0);
		}
	}
	public final ChocoTransformVisitor modelToChocoModel() {
		ChocoTransformVisitor vis = new ChocoTransformVisitor(null);
		evaluateModelWith(vis);
		return vis;
	}
	
	abstract public Map<String, Double> readExpectedTradeOff();
	abstract public String[] propertiesOfInterest();
	abstract protected Resource[] environment();
	
	
	public final Map<String, Double> getExpectedTradeOff(){
		return this.expectedTradeOff;
	}
	
	public final void setExpectedTradeOff(Map<String, Double> expected){
		this.expectedTradeOff = expected;
	}
	
	public Map<String, Softgoal> getPropertiesOfInterest() {
		return propertiesOfInterest;
	}

	public List<Resource> getEnvironmentModel() {
		return environmentModel;
	}
	
	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

}
