package staticTypechecker.entities;

import java.util.HashMap;

import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import staticTypechecker.visitors.TypeCheckerVisitor;

/**
 * Handles the Module instances of all the necessary Jolie modules
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class ModuleHandler {
	private static HashMap<String, Module> modules = new HashMap<>(); // maps module names to their Module instances

	/**
	 * Loads the Jolie module with the given name by parsing it and creating Module instances, which are saved in ModuleHandler. Also loads all dependency-modules (modules which are imported from this one). 
	 * @param moduleName the name of the module to load
	 */
	public static void loadModule(String moduleName){
		Module module = new Module(moduleName);
		ModuleHandler.modules.put(moduleName, module);

		for(OLSyntaxNode node : module.program().children()){
			if(node instanceof ImportStatement){
				ImportStatement n = (ImportStatement)node;
				String importedModuleName = ModuleHandler.getModuleName(n);

				ModuleHandler.loadModule(importedModuleName);
			}
		}
	}

	/**
	 * Returns the module name of the given import statement
	 * @param n the importstatement
	 * @return the module name
	 */
	public static String getModuleName(ImportStatement n){
		return "./src/test/files/" + n.importTarget().get(n.importTarget().size() - 1) + ".ol"; // TODO: figure out a way not to hardcode the path
	}

	/**
	 * Runs the given visitor on all loaded modules twice. Firstly, it runs the visitor with the "processImports" set to false, secondly with it set to true
	 * @param visitor the visitor to run
	 */
	public static void runVisitor(TypeCheckerVisitor visitor){
		for(Module m : ModuleHandler.modules.values()){
			visitor.process(m, false);
		}
	
		for(Module m : ModuleHandler.modules.values()){
			visitor.process(m, true);
		}
	}

	public static Module get(String name){
		return ModuleHandler.modules.get(name);
	}

	public static boolean contains(String name){
		return ModuleHandler.modules.containsKey(name);
	}

	public static HashMap<String, Module> modules(){
		return ModuleHandler.modules;
	}
}
