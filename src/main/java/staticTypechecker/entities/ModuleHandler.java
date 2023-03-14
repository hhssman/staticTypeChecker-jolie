package staticTypechecker.entities;

import java.util.HashMap;

import staticTypechecker.typeStructures.Type;
import staticTypechecker.visitors.TypeCheckerVisitor;

public class ModuleHandler {
	private static HashMap<String, Module> modules = new HashMap<>();

	public static void loadModules(String[] moduleNames){
		for(int i = 0; i < moduleNames.length; i++){
			ModuleHandler.loadModule(moduleNames[i]);
		}
	}

	public static void loadModule(String moduleName){
		if(!ModuleHandler.modules.containsKey(moduleName)){
			ModuleHandler.modules.put(moduleName, new Module(moduleName));
		}
	}

	public static Type runVisitor(TypeCheckerVisitor visitor, String moduleName){
		if(!ModuleHandler.contains(moduleName)){
			ModuleHandler.loadModule(moduleName);
		}

		Module m = ModuleHandler.modules.get(moduleName);
		
		if(m != null){
			return visitor.process(m);
		}

		return null;
	}

	// public static ArrayList<Type> runVisitor(TypeCheckerVisitor visitor){
	// 	ArrayList<Type> results = new ArrayList<>();
		
	// 	for(Module m : ModuleHandler.modules.values()){
	// 		results.add(visitor.process(m));
	// 	}

	// 	return results;
	// }

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
