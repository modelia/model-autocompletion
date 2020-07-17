package org.recommender.engine;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.impl.AssociationImpl;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;
import org.eclipse.uml2.uml.internal.impl.PropertyImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Engine {
	
	public static void main(String[] args) {
		
		run();
	
	}


	private static void run() {
		
		/***********************************************************/
		/**    Model Slicing  */
		
		String partialModelPath = "domain_models/emasa.uml";
		Resource res = load(partialModelPath);
		
		double time0 = System.currentTimeMillis();
		
		Set<String> slice1 = createSliceType1(res);
		
		Map<String, Set<String>> slices2 = createSliceType2(res);
		
		Map<Set<String>, Set<String>> slices3 = createSliceType3(res);
		
		System.out.println("Slicing time: "+(System.currentTimeMillis()-time0) + " ms.");
		
		System.out.println("*** SLICES ***");
		
		System.out.println(slice1);
		System.out.println(slices2.values());
		System.out.println(slices3.values());
		
		
		/***********************************************************/
		/**   Querying the NLP models */
		
		// This task is not automated yet. For it, it is needed to run the python file as a service.
		// When the service starts, it will load the two sources of knowledge (contextual and general) and wait for query requests. 
		
//		String sliceI = "[Order, Notice]";
//		String slicesII = "[[Notice,user],[Order,change]]";
		String slicesIII = "[[Notice,user],[Order,Notice,change,user],[Order,change]]";
		
//		runScript("python_scripts/NLP_Components.py", slicesIII);
		
		/***********************************************************/
		/**    Building suggestions   */
		
		
		System.out.println("\n*** SUGGESTIONS ***");
		
		// Assuming this is a list of suggestions provided by the NLP query:
		List<String> words = stringToList("['notice', 'duplicate', 'history', 'assign']");

		double time1 = System.currentTimeMillis();
		
		buildSuggestionsType1(words);
		
		String sourceClass = "Supervisor";
		buildSuggestionsType2(sourceClass, words);

		Set<String> sourceClasses = new HashSet<String>(); sourceClasses.add("Supervisor"); sourceClasses.add("Supervisor"); sourceClasses.add("Order");
		buildSuggestionsType3(sourceClasses, words);
		
		System.out.println("Time building suggestions: "+(System.currentTimeMillis()-time1)+" ms.");
	}
	
	

	/************************************
	 * Methods model slicing            *
	 ************************************/
	
	private static Set<String> createSliceType1(Resource res) {
		Set<String> modelElemsNames = new HashSet<String>();
		for (EObject c : res.getContents()) {
			if (c != null) {
				for (EObject e : c.eContents()) {
					if (e instanceof ClassImpl) {
						modelElemsNames.add(((ClassImpl) e).getName());
					}
				}
			}
		}
		return modelElemsNames;
	}

	private static Map<String, Set<String>> createSliceType2(Resource res) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (EObject c : res.getContents()) {
			if (c != null) {
				for (EObject e : c.eContents()) {
					if (e instanceof ClassImpl) {
						ClassImpl clazz = (ClassImpl) e;
						Set<String> classAsSet = new HashSet<String>(); classAsSet.add(clazz.getName());
//						result.put(clazz.getName()+"_1", classAsSet);
						Set<String> classAndFeatures = classAndFeatures(res, clazz);
						result.put(clazz.getName()+"_2", classAndFeatures);
					}
				}
			}
		}
		return result;
	}
	
	private static Map<Set<String>, Set<String>> createSliceType3(Resource res) {
		
		Set<ClassImpl> classes = getClasses(res);
		
		Map<Set<String>, Set<String>> result = new HashMap<Set<String>, Set<String>>(); 
		
		for (ClassImpl c1 : classes) {
			for (ClassImpl c2 : classes) {
				if (c1.getName().compareTo(c2.getName())<0) {
					Set<String> keys1 = new HashSet<String>();
					keys1.add(c1.getName()+"_1");
					keys1.add(c2.getName()+"_1");
					Set<String> classesAsSet = new HashSet<String>(); classesAsSet.add(c1.getName()); classesAsSet.add(c2.getName());
//					result.put(keys1, classesAsSet);
					// ----
					Set<String> keys2 = new HashSet<String>();
					keys2.add(c1.getName()+"_2");
					keys2.add(c2.getName()+"_2");
					Set<String> classAndFeatures = classAndFeatures(res, c1);
					classAndFeatures.addAll(classAndFeatures(res, c2));
					result.put(keys2, classAndFeatures);
				}
			}
		}
		
		return result;
	}

	private static Set<ClassImpl> getClasses(Resource res) {
		Set<ClassImpl> classes = new HashSet<ClassImpl>();
		for (EObject c : res.getContents()) {
			if (c != null) {
				for (EObject e : c.eContents()) {
					if (e instanceof ClassImpl) {
						classes.add((ClassImpl) e);
					}
				}
			}
		}
		return classes;
	}
	
	private static Set<String> classAndFeatures(Resource res, ClassImpl clazz) {
		
		Set<String> result = new HashSet<String>();
		
		String className = clazz.getName();
		result.add(className);
		
		/** Properties of class 'clazz' */
		for (EObject att : clazz.eContents()) {
			if (att instanceof PropertyImpl) {
				if (((PropertyImpl) att).getName() != null) {
//					System.out.print( ((PropertyImpl) att).getName() + " ");
//					System.out.println();
					result.add(((PropertyImpl) att).getName());
				}
			}
		}
		
		/**  Association names and outgoing role names for class 'clazz' */
		for (EObject c : res.getContents()) {
				if (c != null) {
					for (EObject as : c.eContents()) {
						if (as instanceof AssociationImpl) {
							AssociationImpl assoc = ((AssociationImpl) as);
							for (Property end : assoc.getMemberEnds()) {
								if (end.getType().equals(clazz)) {
//									System.out.println("\t"+assoc.getName());
//									System.out.println("\t\t" + end.getOtherEnd().getName() + " : " + end.getOtherEnd().getType().getName());
									if (assoc.getName() != null) { result.add(assoc.getName()); }
									if (end.getOtherEnd().getName()!=null) { result.add(end.getOtherEnd().getName()); }
								}
//								System.out.println("\t\t"+end.getName() + " - " + end.getType().getName());
							}
							
						}
					}
				}
		}
		return result;
		
	}

	private static Resource load(String path) {
		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		set.getResourceFactoryRegistry().getExtensionToFactoryMap()
		   .put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
		   .put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);

		Resource res = set.getResource(URI.createFileURI(path), true);
		return res;
	}

	/*************************************
	 * Method for querying python script *
	 *************************************/
	
	public static void runScript(String pyFile, String args) {
		Process process;
		try {
			process = Runtime.getRuntime().exec( "py " + pyFile + " " + args ); 
			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					System.out.println("stdout: " + line);
				}
			} catch (IOException e) {
				System.out.println("Exception in reading output" + e.toString());
			}
			
		} catch (Exception e) {
			System.out.println("Exception Raised" + e.toString());
		}
	}
	

	/************************************
	 * Methods for building suggestions *
	 ************************************/
	
	private static void buildSuggestionsType2(String sourceClass, List<String> words) {
		// TODO Auto-generated method stub
		
		for (String w : words) {
			System.out.println("Add new attribute to class "+ sourceClass + " named " + w);
			System.out.println("Add new class "+ w + " to the model and link "+ sourceClass + " with " + w);
			System.out.println("[If a class named "+ w +" exists in the model] Add new association between "+ sourceClass + " and " + w);
		}
		
	}

	private static void buildSuggestionsType3(Set<String> sourceClasses, List<String> words) {
		for (String w : words) {
			System.out.print("Add new association or role name between classes (");
			for (String c : sourceClasses) {
				System.out.print(c+" ");
			}
			System.out.println(") named " + w);
		}
		
	}

	private static void buildSuggestionsType1(List<String> words) {
		for (String w : words) {
			String className = w.substring(0, 1).toUpperCase() + w.substring(1);
			System.out.println("Add new class " + className);
		}
	}

	private static List<String> stringToList(String string) {
		List<String> result = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(string, "[]', ");
		while (st.hasMoreTokens()) {
			String word = st.nextToken(); 
			result.add(word);
		}
		return result;
	}
	

}
