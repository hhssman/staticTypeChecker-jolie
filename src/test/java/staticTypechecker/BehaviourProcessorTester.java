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

		return result.equals(target);
	}

	public static boolean testWhile(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testWhile.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		ChoiceType target = new ChoiceType();

		// first option, not entering the while loop
		InlineType option1 = Type.VOID().addChild("a", Type.INT()).addChild("i", Type.INT());
		target.addChoiceUnsafe(option1);

		// second option, 1st iteration of the while loop, not entering the if statement
		InlineType option2 = Type.VOID().addChild("a", Type.INT()).addChild("b", Type.STRING()).addChild("i", Type.INT());
		target.addChoiceUnsafe(option2);

		// third option, 1st iteration of the while loop, entering the if statement
		InlineType option3 = Type.VOID().addChild("a", Type.BOOL()).addChild("b", Type.STRING()).addChild("c", Type.BOOL()).addChild("i", Type.INT());
		target.addChoiceUnsafe(option3);

		// fourth option, 2nd iteration of the while loop, not entering the if statement
		InlineType option4 = Type.VOID().addChild("a", Type.INT()).addChild("b", Type.STRING()).addChild("c", Type.BOOL()).addChild("i", Type.INT());
		target.addChoiceUnsafe(option4);

		return result.equals(target);
	}

	public static boolean testWhileFallback1(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testWhileFallback1.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		ChoiceType target = new ChoiceType();

		// first option, never entered the while loop
		InlineType option1 = Type.VOID().addChild("a", Type.INT()).addChild("i", Type.INT());
		target.addChoiceUnsafe(option1);

		// second option, enter the while loop, but fail to find a steady state, so fallback plan
		InlineType option2 = Type.VOID().addChild("a", Type.INT().addChild("b", Type.OPEN_RECORD())).addChild("i", Type.INT());
		target.addChoiceUnsafe(option2);

		return result.equals(target);
	}

	public static boolean testWhileFallback2(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testWhileFallback2.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		ChoiceType target = new ChoiceType();

		// first option, not in if statement, and not entered the while loop
		InlineType option1 = Type.VOID().addChild("a", Type.INT());
		target.addChoiceUnsafe(option1);

		// second option, in if statement, and not entered the while loop
		InlineType option2 = Type.VOID().addChild("a", Type.STRING());
		target.addChoiceUnsafe(option2);

		// enter the while loop, but fail to find a steady state, so fallback plan
		// first choice of fallback
		InlineType fallback1 = Type.VOID().addChild("a", Type.INT().addChild("b", Type.OPEN_RECORD()).addChild("c", Type.OPEN_RECORD()));
		target.addChoiceUnsafe(fallback1);

		// second choice of fallback
		InlineType fallback2 = Type.VOID().addChild("a", Type.STRING().addChild("b", Type.OPEN_RECORD()).addChild("c", Type.OPEN_RECORD()));

		target.addChoiceUnsafe(fallback2);

		// System.out.println("result:\n" + result.prettyString());
		// System.out.println("target:\n" + target.prettyString());

		return result.equals(target);
	}

	

	public static boolean testWhileTypeHint(){
		String moduleName = AppTest.BASE_PATH + "testFilesForBehaviours/testWhileTypeHint.ol";
		List<Module> modules = BehaviourProcessorTester.readyModules(moduleName);

		Type result = new BehaviorProcessor(false).process(modules.get(0));

		InlineType target = Type.VOID();

		return result.equals(target);
	}

	private static List<Module> readyModules(String moduleName){
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 4);
		return modules;
	}
}
