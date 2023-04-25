package staticTypechecker;

import java.util.List;

import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.visitors.BehaviorProcessor;
import staticTypechecker.entities.ChoiceType;
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

	public static boolean testSolicit(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testSolicit.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();

		// arg
		InlineType arg = Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT());
		target.addChildUnsafe("arg", arg);

		// out
		ChoiceType child = new ChoiceType();
		child.addChoiceUnsafe(Type.STRING());
		child.addChoiceUnsafe(Type.INT());

		InlineType out = Type.STRING().addChild("x", child);
		target.addChildUnsafe("out", out);

		return result.equals(target);
	}

	public static boolean testRequest(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testRequest.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();

		// arg
		InlineType arg = Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT());
		target.addChildUnsafe("arg", arg);

		// f
		InlineType f = Type.INT();
		target.addChildUnsafe("f", f);

		// out
		InlineType out = Type.STRING().addChild("x", Type.INT());
		target.addChildUnsafe("out", out);

		return result.equals(target);
	}

	public static boolean testChoice(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testChoice.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		ChoiceType target = new ChoiceType();
		
		// first choice, tree1
		InlineType tree1 = Type.VOID();
		tree1.addChildUnsafe("arg", Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT()));
		tree1.addChildUnsafe("out", Type.STRING().addChild("x", Type.STRING()));

		// second choice, tree2
		InlineType tree2 = Type.VOID();
		tree2.addChildUnsafe("input", Type.INT());
		tree2.addChildUnsafe("out", Type.INT());

		// third choice, tree3
		InlineType tree3 = Type.VOID();
		tree3.addChildUnsafe("input", Type.STRING());
		tree3.addChildUnsafe("out", Type.BOOL());
		tree3.addChildUnsafe("random", Type.INT());

		target.addChoiceUnsafe(tree1);
		target.addChoiceUnsafe(tree2);
		target.addChoiceUnsafe(tree3);

		// System.out.println("target:\n" + target.prettyString());
		// System.out.println("result:\n" + result.prettyString());

		return result.equals(target);
	}

	private static List<Module> readyModules(String moduleName){
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 4);
		return modules;
	}
}
