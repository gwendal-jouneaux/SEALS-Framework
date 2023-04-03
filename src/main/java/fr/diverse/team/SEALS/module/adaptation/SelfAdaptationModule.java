package fr.diverse.team.SEALS.module.adaptation;

import java.util.ArrayList;
import java.util.List;

import fr.diverse.team.SEALS.decision.model.Resource;
import fr.diverse.team.SEALS.decision.model.Softgoal;
import fr.diverse.team.SEALS.lang.adaptation.AdaptationContext;
import fr.diverse.team.SEALS.lang.semantics.AdaptableNode;
import fr.diverse.team.SEALS.lang.semantics.SemanticsAdaptationInterface;

public abstract class SelfAdaptationModule <AdaptationCtx extends AdaptationContext<?>,
											AdaptNode extends AdaptableNode<Interface>,
											Interface extends SemanticsAdaptationInterface>{

	private String moduleID;
	private Class<? extends AdaptableNode<?>> targetNodeClass;
	private List<Softgoal> modulesPropertiesOfInterest;
	private boolean active;


	public SelfAdaptationModule(String moduleID, Class<? extends AdaptableNode<?>> clazz) {
		this.moduleID = moduleID;
		this.targetNodeClass = clazz;
		this.modulesPropertiesOfInterest = new ArrayList();
		this.active = false;
	}
	


	final public void afterRegister(AdaptationContext adaptationContext) {		
		init((AdaptationCtx) adaptationContext);
		
		List<Resource> resources = adaptationContext.getEnvironmentModel();
		for (Resource resource : resources) {
			connectResource(resource);
		}
		
		String[] propertiesOfInterest = adaptationContext.propertiesOfInterest();
		for (int i = 0; i < propertiesOfInterest.length; i++) {
			Softgoal soft = new Softgoal(moduleID + "-" + propertiesOfInterest[i]);
			this.modulesPropertiesOfInterest.add(soft);
			adaptationContext.connectModuleSoftgoal(propertiesOfInterest[i], soft);
			connectSoftGoal(soft);
		}
	}
	
	/*
	 * Design of the impact model
	 * init(AdaptationCtx) 			: Create the impact model
	 * connectSoftGoal(Softgoal)    : Connect impact model to per-module properties of interest
	 * connectResource(Resource)    : Connect resources monitoring as input to the impact model
	*/
	abstract public void init(AdaptationCtx adaptationContext);
	abstract public void connectSoftGoal(Softgoal softgoal);
	abstract public void connectResource(Resource resource);
	
	/*
	 * Define adaptations
	 * adapt(Interface) 			: Interface mutation function
	 * isTargetedNode(AdaptNode)	: Verify if the semantic should be adapted by this module
	*/
	abstract public Interface adapt(Interface configInterface);
	abstract public boolean isTargetedNode(AdaptableNode<Interface> adaptableNode);
	
	public boolean isActive() {
		return this.active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	final public Class<? extends AdaptableNode<?>> getTargetNodeClass() {
		return targetNodeClass;
	}
	
	final public List<Softgoal> getModulesPropertiesOfInterest() {
		return modulesPropertiesOfInterest;
	}
}
