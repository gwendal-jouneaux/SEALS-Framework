package fr.diverse.team.SEALS.lang.semantics;

import java.lang.annotation.*;

import javax.annotation.processing.Processor;

import com.google.auto.service.AutoService;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Operationalize {
	/**
	   * The node operationalized
	   */
	  public Class node();

	  /**
	   * The class name for the visitor
	   */
	  public String visitor();
}
