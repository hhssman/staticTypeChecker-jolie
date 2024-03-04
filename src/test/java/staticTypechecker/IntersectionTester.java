package staticTypechecker;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.EmptyType;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.utils.Intersection;
import staticTypechecker.utils.ModuleHandler;

public class IntersectionTester {

    public static void main(String[] args) {

        List<Module> modules = parseTypes(AppTest.BASE_PATH + "intersectionFiles/recursion.ol");

        Type t1, t2, tEquals;

        t1 = (Type)modules.get(0).symbols().get("Q", SymbolType.TYPE);
        t2 = (Type)modules.get(0).symbols().get("T", SymbolType.TYPE);

        boolean test = t1.equals(Intersection.intersection(t1, t2));

        assertTrue(t1.equals(Intersection.intersection(t1, t2)));
    }

    @Test
    public void basic() {

        List<Module> modules = parseTypes(AppTest.BASE_PATH + "intersectionFiles/basic.ol");

        Type t1, t2, tEquals;

        tEquals = Intersection.intersection(Type.ANY(), Type.STRING());
        assertTrue(tEquals.equals(Type.STRING()));

        t1 = Type.VOID().addChild("x", Type.STRING());
        assertTrue(Intersection.intersection(t1, t1).equals(t1));

        t2 = Type.VOID().addChild("x", Type.INT());
        assertTrue(Intersection.intersection(t1, t2).equals(new EmptyType()));

        t1 = new ChoiceType().addChoice(Type.INT()).addChoice(Type.STRING());
        tEquals = Intersection.intersection(t1, Type.INT());
        assertTrue(tEquals.equals(Type.INT()));

        t1 = (Type)modules.get(0).symbols().get("T1", SymbolType.TYPE);
        t2 = Type.VOID().addChild("z", Type.STRING());
        assertTrue(t2.equals(Intersection.intersection(t1, t2)));

        t2 = Type.VOID();
        assertTrue(t2.equals(Intersection.intersection(t1, t2)));

        t2 = Type.VOID().addChild("z", Type.INT());
        assertTrue(Type.EMPTY().equals(Intersection.intersection(t1, t2)));

        t2 = (Type)modules.get(0).symbols().get("T2", SymbolType.TYPE);
        assertTrue(Type.VOID().equals(Intersection.intersection(t1, t2)));

        t1 = Type.UNDEFINED();
        t2 = Type.STRING();
        assertTrue(t2.equals(Intersection.intersection(t1, t2)));

    }

    @Test
    public void recursion() {
        List<Module> modules = parseTypes(AppTest.BASE_PATH + "intersectionFiles/recursion.ol");

        Type t1, t2, tEquals;

        t1 = (Type)modules.get(0).symbols().get("R", SymbolType.TYPE);
        t2 = (Type)modules.get(0).symbols().get("T", SymbolType.TYPE);

        Type test = Intersection.intersection(t1, t2);

        assertTrue(t1.equals(Intersection.intersection(t1, t2)));
    }

    private static List<Module> parseTypes(String moduleName) {
        List<Module> modules = ModuleHandler.loadModule(moduleName);
        AppTest.runProcessors(modules, 2);
        return modules;
    }
}
