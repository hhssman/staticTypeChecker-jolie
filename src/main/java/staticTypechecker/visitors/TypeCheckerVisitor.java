package staticTypechecker.visitors;

import staticTypechecker.typeStructures.Type;
import staticTypechecker.entities.Module;

public interface TypeCheckerVisitor {
	public Type process(Module m, boolean processImports);
}
