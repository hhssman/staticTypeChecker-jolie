package staticTypechecker.entities;

import java.util.HashMap;

import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import staticTypechecker.visitors.TypeCheckerVisitor;

public class ModuleHandler {
	private static HashMap<String, Module> modules = new HashMap<>();

	public static void loadModules(String initialModuleName){
		Module initialModule = new Module(initialModuleName);
		ModuleHandler.modules.put(initialModuleName, initialModule);

		for(OLSyntaxNode node : initialModule.program().children()){
			if(node instanceof ImportStatement){
				ImportStatement n = (ImportStatement)node;
				String moduleName = ModuleHandler.getModuleName(n);
				ModuleHandler.modules.put(moduleName, new Module(moduleName));
			}
		}
	}

	public static String getModuleName(ImportStatement n){
		return "./src/test/files/" + n.importTarget().get(n.importTarget().size() - 1) + ".ol"; // TODO: figure out a way not to hardcode the path
	}

	/**
	 * Runs the given visitor on all loaded modules
	 * @param visitor
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
