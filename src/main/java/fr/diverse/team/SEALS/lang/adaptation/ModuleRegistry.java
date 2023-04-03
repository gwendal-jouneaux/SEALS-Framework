package fr.diverse.team.SEALS.lang.adaptation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.diverse.team.SEALS.lang.semantics.AdaptableNode;
import fr.diverse.team.SEALS.module.adaptation.SelfAdaptationModule;

public class ModuleRegistry {
	private Map<Class<?>, 
				List<SelfAdaptationModule<?, ?, ?>>> 	modulesByNodeType;
	private List<SelfAdaptationModule<?, ?, ?>> 		modules;
	
	public ModuleRegistry() {
		modulesByNodeType = new HashMap<>();
		modules = new ArrayList<>();
	}
	
	public final void register(SelfAdaptationModule<?, ?, ?> module) {
		Class<? extends AdaptableNode<?>> clazz = module.getTargetNodeClass();
		List<SelfAdaptationModule<?, ?, ?>> clazzModules = modulesByNodeType.get(clazz);
		if(clazzModules == null) {
			clazzModules = new ArrayList<SelfAdaptationModule<?, ?, ?>>();
			modulesByNodeType.put(clazz, clazzModules);
		}
		clazzModules.add(module);
		modules.add(module);
	}

	public final List<SelfAdaptationModule<?, ?, ?>> getModulesFor(Class<? extends AdaptableNode> class1) {
		List<SelfAdaptationModule<?, ?, ?>> out = new ArrayList<>();
		for (Class<?> c : modulesByNodeType.keySet()) {
			if(c.isAssignableFrom(class1)) {
				out.addAll(modulesByNodeType.get(c));
			}
		}
		return out;
	}
	
	public final List<SelfAdaptationModule<?, ?, ?>> getModules() {
		return modules;
	}
}
