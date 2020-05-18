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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelSlicing {
	
	public static void main(String[] args) {
		
		
		String path = "C:\\Users\\Lola\\eclipse-workspace\\metamodels\\emasa.uml";
		Resource res = load(path);
		
//		System.out.println(res.getAllContents());
		
		
		Set<String> slice1 = createSliceTypeI(res);
		System.out.println(slice1);
		
		Set<Set<String>> slices2 = createSliceTypeII(res);
		System.out.println(slices2);
		
		Set<Set<String>> slices3 = createSliceTypeIII(res);
		System.out.println(slices3);
		
		
//		org.eclipse.uml2.uml.Class clazz = (org.eclipse.uml2.uml.Class) EcoreUtil.getObjectByType(res.getContents(),
//				UMLPackage.Literals.CLASS);
//		System.out.println(clazz);
		
		
		
	}

	private static Set<String> createSliceTypeI(Resource res) {
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

	private static Set<Set<String>> createSliceTypeII(Resource res) {
		Set<Set<String>> result = new HashSet<Set<String>>();
		for (EObject c : res.getContents()) {
			if (c != null) {
				for (EObject e : c.eContents()) {
					if (e instanceof ClassImpl) {
						ClassImpl clazz = (ClassImpl) e;
						Set<String> classAndFeatures = classAndFeatures(res, clazz);
						result.add(classAndFeatures);
					}
				}
			}
		}
		return result;
	}
	
	private static Set<Set<String>> createSliceTypeIII(Resource res) {
		
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
		
		Set<Set<String>> result = new HashSet<Set<String>>(); 
		
		for (ClassImpl c1 : classes) {
			for (ClassImpl c2 : classes) {
				Set<String> classAndFeatures = classAndFeatures(res, c1);
				classAndFeatures.addAll(classAndFeatures(res, c2));
				result.add(classAndFeatures);
			}
		}
		
		return result;
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

	
//	protected static org.eclipse.uml2.uml.Package load(URI uri) {
//		org.eclipse.uml2.uml.Package package_ = null;
//
//		try {
//			ResourceSetImpl RESOURCE_SET = new ResourceSetImpl();
//			Resource resource = RESOURCE_SET.getResource(uri, true);
//
//			package_ = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(),
//					UMLPackage.Literals.PACKAGE);
//			System.out.println(package_);
//		} catch (WrappedException we) {
//			we.printStackTrace();
//			System.exit(1);
//		}
//
//		return package_;
//	}
	


}
