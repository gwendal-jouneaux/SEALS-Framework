package fr.diverse.team.SEALS.lang.semantics;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes(value = { "fr.diverse.team.SEALS.lang.semantics.Operationalize" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class OperationalizationProcessor extends AbstractProcessor {
	
	private static boolean done = false;
	
	private Map<String, Map<Element, TypeElement>> visitorsOperations;
	
	public OperationalizationProcessor() {
		visitorsOperations = new HashMap<>();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(done) return true;
		done = true;
		
		for (TypeElement annotation : annotations) {
			
            Set<? extends Element> operations = roundEnv.getElementsAnnotatedWith(annotation);
            for(Element operation : operations) {
            	String name = "";
            	TypeElement target = null;
            	
            	try
                {
            		Operationalize operationalize = operation.getAnnotation(Operationalize.class);
            		name = operationalize.visitor();
            		operationalize.node();
                }
                catch( MirroredTypeException mte )
                {
                	DeclaredType t = (DeclaredType) mte.getTypeMirror();
                    target = ((TypeElement) t.asElement());
                }
            	
            	
            	Map<Element, TypeElement> visitorOperations = visitorsOperations.get(name);
            	if(visitorOperations == null) {
            		visitorOperations = new HashMap<>();
            		visitorsOperations.put(name, visitorOperations);
            	}
            	visitorOperations.put(operation, target);
            }


            //otherMethods.forEach(element -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@BuilderProperty must be applied to a setXxx method with a single argument", element));
        }
		
		for (String name : visitorsOperations.keySet()) {
			try {
	            generateVisitorClass(name, visitorsOperations.get(name));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
		return true;
	}

	private void generateVisitorClass(String visitorFQN, Map<Element, TypeElement> visitorOperations) throws IOException {
		StringBuilder classfile = new StringBuilder();
		Map<TypeElement, List<TypeElement>> inheritance = new HashMap<>();
		List<TypeElement> inheritanceRoots = new ArrayList<>();
		Map<TypeElement,String> interfaceForNode = new HashMap();
		
		Types tu = processingEnv.getTypeUtils();
		TypeElement nodeType = processingEnv.getElementUtils().getTypeElement("fr.diverse.team.SEALS.lang.semantics.Node");
		TypeElement adaptableNodeType = processingEnv.getElementUtils().getTypeElement("fr.diverse.team.SEALS.lang.semantics.AdaptableNode");
		
		String classname = visitorFQN.substring(visitorFQN.lastIndexOf(".")+1);
		String pkg = visitorFQN.substring(0, visitorFQN.lastIndexOf("."));
		
		classfile.append("package "+pkg+";\n\npublic class "+classname+" implements fr.diverse.team.SEALS.lang.semantics.SelfAdaptiveVisitor {\n");
		
		for (Element operation : visitorOperations.keySet()) {
			String operationClass = ((TypeElement) operation).getQualifiedName().toString();
			TypeElement nodeClass = visitorOperations.get(operation);
			String nodeFQN = nodeClass.getQualifiedName().toString();
			
			List<TypeElement> subclasses = new ArrayList();
			inheritanceRoots.add(nodeClass);
			for (TypeElement c : inheritance.keySet()) {
				if(nodeClass.getSuperclass().equals(c)) {
					inheritance.get(c).add(nodeClass);
					inheritanceRoots.remove(nodeClass);
				}
				if(c.getSuperclass().equals(nodeClass)) {
					subclasses.add(c);
					inheritanceRoots.remove(c);
				}
			}
			inheritance.put(nodeClass, subclasses);
			
			
			if(tu.isSubtype(tu.erasure(nodeClass.asType()), tu.erasure(processingEnv.getElementUtils().getTypeElement(AdaptableNode.class.getName()).asType()))) {
				String interfaceFQN = getInterfaceFQN(tu, nodeClass);
				interfaceForNode.put(nodeClass, interfaceFQN);
				classfile.append("\tpublic Object visit"+nodeClass.getSimpleName()+"("+nodeFQN+" node, Object execCtx, "+interfaceFQN+" config){ return (new "+operationClass+"()).execute(this,node,execCtx,config);}\n");
			} else {
				classfile.append("\tpublic Object visit"+nodeClass.getSimpleName()+"("+nodeFQN+" node, Object execCtx){ return (new "+operationClass+"()).execute(this,node,execCtx);}\n");
			}
		}
		
		inheritance.put(nodeType, inheritanceRoots);
		inheritance.put(adaptableNodeType, inheritanceRoots);
		
		classfile.append("\tpublic Object dispatch(fr.diverse.team.SEALS.lang.semantics.Node node, Object executionCtx){\n");
		classfile.append(generateDispatch(adaptableNodeType, inheritance).toString());
		classfile.append("\t\treturn null;\n\t}\n");
		classfile.append("\tpublic Object dispatch(fr.diverse.team.SEALS.lang.semantics.AdaptableNode node, Object executionCtx, fr.diverse.team.SEALS.lang.semantics.SemanticsAdaptationInterface config){\n");
		classfile.append(generateAdaptiveDispatch(nodeType, inheritance, interfaceForNode).toString());
		classfile.append("\t\treturn null;\n\t}\n");
		
		classfile.append("}");
		
		JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(visitorFQN);
		try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
			out.print(classfile.toString());
		}
	}
	
	private StringBuilder generateDispatch(TypeElement parent, Map<TypeElement, List<TypeElement>> inheritance) {
		StringBuilder out = new StringBuilder();
		
		for (TypeElement child : inheritance.get(parent)) {
			out.append(generateDispatch(child, inheritance).toString());
		}
		Types tu = processingEnv.getTypeUtils();
		if(! tu.isSubtype(tu.erasure(parent.asType()), tu.erasure(processingEnv.getElementUtils().getTypeElement(AdaptableNode.class.getName()).asType()))) {
			out.append("\t\tif(node instanceof "+parent.getQualifiedName().toString()+"){\n");
			out.append("\t\t\treturn visit"+parent.getSimpleName()+"(("+parent.getQualifiedName().toString()+") node, executionCtx);\n");
			out.append("\t\t}\n");
		}
		
		return out;
	}
	
	private StringBuilder generateAdaptiveDispatch(TypeElement parent, Map<TypeElement, List<TypeElement>> inheritance, Map<TypeElement,String> interfaceForNode) {
		StringBuilder out = new StringBuilder();
		
		for (TypeElement child : inheritance.get(parent)) {
			out.append(generateAdaptiveDispatch(child, inheritance, interfaceForNode).toString());
		}
		Types tu = processingEnv.getTypeUtils();
		if(tu.isSubtype(tu.erasure(parent.asType()), tu.erasure(processingEnv.getElementUtils().getTypeElement(AdaptableNode.class.getName()).asType()))) {
			String interfaceType = interfaceForNode.get(parent);
			out.append("\t\tif(node instanceof "+parent.getQualifiedName().toString()+"){\n");
			out.append("\t\t\treturn visit"+parent.getSimpleName().toString()+"(("+parent.getQualifiedName().toString()+") node, executionCtx,("+interfaceType+") config);\n");
			out.append("\t\t}\n");
		}
		
		return out;
	}
	
	private String getInterfaceFQN(Types tu, TypeElement nodeClass) {
		TypeElement type = nodeClass;
		for (TypeMirror typeMirror : type.getInterfaces()) {
            if (processingEnv.getTypeUtils().isSameType(tu.erasure(processingEnv.getElementUtils().getTypeElement(AdaptableNode.class.getName()).asType()), tu.erasure(typeMirror))) {
                DeclaredType dclt = (DeclaredType) typeMirror;
                for (TypeMirror argument : dclt.getTypeArguments()) {
                	if(tu.isSubtype(tu.erasure(argument), tu.erasure(processingEnv.getElementUtils().getTypeElement(SemanticsAdaptationInterface.class.getName()).asType()))) {
						return ((TypeElement) ((DeclaredType) argument).asElement()).getQualifiedName().toString();
					}
                }
            } else {
            	TypeElement parent = (TypeElement) ((DeclaredType) typeMirror).asElement();
            	String fqn = getInterfaceFQN(tu, parent);
            	if (fqn != null) {
					return fqn;
				}
            }
        }
		return null;
	}

}
