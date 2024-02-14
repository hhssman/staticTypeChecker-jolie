package staticTypechecker;

import static org.junit.Assert.assertTrue;

import java.util.List;

import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.utils.Intersection;
import staticTypechecker.utils.ModuleHandler;

public class IntersectionTester {

    public static void main(String[] args) {

        List<Module> modules = parseTypes(AppTest.BASE_PATH + "test.ol");
        Type t1 = (Type)modules.get(0).symbols().get("T1", SymbolType.TYPE);
        Type t2 = (Type)modules.get(0).symbols().get("T2", SymbolType.TYPE);

        Type t3 = Intersection.intersection(t1, t2);
        boolean result = t3.equals((Type)modules.get(0).symbols().get("T2", SymbolType.TYPE));
        assertTrue(result);
    }

    private static List<Module> parseTypes(String moduleName) {
        List<Module> modules = ModuleHandler.loadModule(moduleName);
        AppTest.runProcessors(modules, 2);
        return modules;
    }
}
