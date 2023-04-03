package fr.diverse.team.SEALS.decision.model.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.diverse.team.SEALS.decision.model.TagConstraint;

public class FlattenedImpact {
	List<Double> weights;
	List<Set<TagConstraint>> pathConstraints;
	
	public FlattenedImpact() {
		weights = new ArrayList();
		pathConstraints = new ArrayList();
		
		weights.add(1.0);
		pathConstraints.add(new HashSet());
	}
	
	public void merge(FlattenedImpact fi) {
		this.weights.addAll(fi.weights);
		this.pathConstraints.addAll(fi.pathConstraints);
	}
	
	public void addConstraints(Set<TagConstraint> constraint) {
		for (Set<TagConstraint> set : pathConstraints) {
			set.addAll(constraint);
		}
	}

	public void addWeight(double weight) {
		for (int i = 0; i < weights.size(); i++) {
			weights.set(i, weights.get(i) * weight);
		}
	}
	
	public double impactForValue(double value) {
		double out = 0.0;
		for (int i = 0; i < weights.size(); i++) {
			Set<TagConstraint> constraints = pathConstraints.get(i);
			boolean valid = true;
			for (TagConstraint tagConstraint : constraints) {
				if(! tagConstraint.isValid()) {
					valid = false;
					break;
				}
			}
			if(valid) {
				out += value * weights.get(i);
			}
		}
		return out;
	}
}
