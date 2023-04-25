package staticTypechecker;

import java.util.List;

import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.visitors.BehaviorProcessor;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Type;

public class BehaviourProcessorTester {
	public static boolean testNil(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testNil.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));
		Type target = Type.VOID();

		return result.equals(target);
	}

	public static boolean testSeq(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testSeq.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();
		InlineType a = Type.INT();
		InlineType b = Type.STRING();

		target.addChildUnsafe("a", a);
		target.addChildUnsafe("b", b);

		return result.equals(target);
	}

	public static boolean testNotify(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testNotify.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		target.addChildUnsafe("inputType", inputType);

		return result.equals(target);
	}

	public static boolean testOneWay(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testOneWay.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		InlineType p = Type.INT();

		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		p.addChildUnsafe("x", Type.STRING());
		p.addChildUnsafe("y", Type.INT());

		target.addChildUnsafe("inputType", inputType);
		target.addChildUnsafe("p", p);

		return result.equals(target);
	}

	private static List<Module> readyModules(String moduleName){
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 4);
		return modules;
	}
}
