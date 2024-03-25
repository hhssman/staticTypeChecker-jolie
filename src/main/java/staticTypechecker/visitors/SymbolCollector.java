package staticTypechecker.visitors;

import java.util.List;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.AddAssignStatement;
import jolie.lang.parse.ast.AssignStatement;
import jolie.lang.parse.ast.CompareConditionNode;
import jolie.lang.parse.ast.CompensateStatement;
import jolie.lang.parse.ast.CorrelationSetInfo;
import jolie.lang.parse.ast.CurrentHandlerStatement;
import jolie.lang.parse.ast.DeepCopyStatement;
import jolie.lang.parse.ast.DefinitionCallStatement;
import jolie.lang.parse.ast.DefinitionNode;
import jolie.lang.parse.ast.DivideAssignStatement;
import jolie.lang.parse.ast.DocumentationComment;
import jolie.lang.parse.ast.EmbedServiceNode;
import jolie.lang.parse.ast.EmbeddedServiceNode;
import jolie.lang.parse.ast.ExecutionInfo;
import jolie.lang.parse.ast.ExitStatement;
import jolie.lang.parse.ast.ForEachArrayItemStatement;
import jolie.lang.parse.ast.ForEachSubNodeStatement;
import jolie.lang.parse.ast.ForStatement;
import jolie.lang.parse.ast.IfStatement;
import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.ImportSymbolTarget;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.InstallFixedVariableExpressionNode;
import jolie.lang.parse.ast.InstallStatement;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.InterfaceExtenderDefinition;
import jolie.lang.parse.ast.LinkInStatement;
import jolie.lang.parse.ast.LinkOutStatement;
import jolie.lang.parse.ast.MultiplyAssignStatement;
import jolie.lang.parse.ast.NDChoiceStatement;
import jolie.lang.parse.ast.NotificationOperationStatement;
import jolie.lang.parse.ast.NullProcessStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OneWayOperationStatement;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.ParallelStatement;
import jolie.lang.parse.ast.PointerStatement;
import jolie.lang.parse.ast.PostDecrementStatement;
import jolie.lang.parse.ast.PostIncrementStatement;
import jolie.lang.parse.ast.PreDecrementStatement;
import jolie.lang.parse.ast.PreIncrementStatement;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ProvideUntilStatement;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationStatement;
import jolie.lang.parse.ast.RunStatement;
import jolie.lang.parse.ast.Scope;
import jolie.lang.parse.ast.SequenceStatement;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.ast.SolicitResponseOperationStatement;
import jolie.lang.parse.ast.SpawnStatement;
import jolie.lang.parse.ast.SubtractAssignStatement;
import jolie.lang.parse.ast.SynchronizedStatement;
import jolie.lang.parse.ast.ThrowStatement;
import jolie.lang.parse.ast.TypeCastExpressionNode;
import jolie.lang.parse.ast.UndefStatement;
import jolie.lang.parse.ast.ValueVectorSizeExpressionNode;
import jolie.lang.parse.ast.VariablePathNode;
import jolie.lang.parse.ast.WhileStatement;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.AndConditionNode;
import jolie.lang.parse.ast.expression.ConstantBoolExpression;
import jolie.lang.parse.ast.expression.ConstantDoubleExpression;
import jolie.lang.parse.ast.expression.ConstantIntegerExpression;
import jolie.lang.parse.ast.expression.ConstantLongExpression;
import jolie.lang.parse.ast.expression.ConstantStringExpression;
import jolie.lang.parse.ast.expression.FreshValueExpressionNode;
import jolie.lang.parse.ast.expression.IfExpressionNode;
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode;
import jolie.lang.parse.ast.expression.InstanceOfExpressionNode;
import jolie.lang.parse.ast.expression.IsTypeExpressionNode;
import jolie.lang.parse.ast.expression.NotExpressionNode;
import jolie.lang.parse.ast.expression.OrConditionNode;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SolicitResponseExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.lang.parse.ast.expression.VariableExpressionNode;
import jolie.lang.parse.ast.expression.VoidExpressionNode;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.entities.Module;
import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.SymbolTable.Pair;
import staticTypechecker.entities.Type;

/**
 * This class collects all symbols in a given module. A symbol in this case means the name of any type, interface, service, port etc. used in this module. Note, that also symbols imported by this module will be in the symbol table for this module, even though they are not declared here.
 * 
 * @author Kasper Bergstedt (kasper.bergstedt@hotmail.com)
 */
public class SymbolCollector implements OLVisitor<SymbolTable, Void>, TypeCheckerVisitor {
	private Module module;

	public SymbolCollector(){}

	public Type process(Module module, boolean processImports){
		this.module = module;

		if(module.symbols() == null){
			module.setSymbols(new SymbolTable());
		}

		Program p = module.program();

		if(!processImports){
			// accept all children which are NOT import statements
			for(OLSyntaxNode child : p.children()){
				if(!(child instanceof ImportStatement)){
					child.accept(this, module.symbols());
				}
			}
		}
		else{
			// accept all import statements
			for(OLSyntaxNode child : p.children()){
				if(child instanceof ImportStatement){
					child.accept(this, module.symbols());
				}
			}
		}

		return null;
	}

	@Override
	public Void visit(Program p, SymbolTable symbols) {
		for(OLSyntaxNode child : p.children()){
			child.accept(this, symbols);
		}
		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, SymbolTable symbols) {
		symbols.put(SymbolTable.newPair(n.name(), SymbolType.TYPE), null);
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, SymbolTable symbols) {
		symbols.put(SymbolTable.newPair(n.name(), SymbolType.TYPE), null);
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, SymbolTable symbols) {
		symbols.put(SymbolTable.newPair(n.name(), SymbolType.TYPE), null);
		return null;
	}

	@Override
	public Void visit(ImportStatement n, SymbolTable symbols) {
		String moduleName = ModuleHandler.findFullPath(n, this.module);
		
		for(ImportSymbolTarget s : n.importSymbolTargets()){
			String originalName = s.originalSymbolName();
			String alias = s.localSymbolName();

			List<Pair<Symbol, SymbolType>> importedSymbols = ModuleHandler.get(moduleName).symbols().getAllSymbols(originalName);
			for(Pair<Symbol, SymbolType> p : importedSymbols){
				symbols.put(SymbolTable.newPair(alias, p.value()), p.key());
			}
		}

		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, SymbolTable symbols) {
		symbols.put(SymbolTable.newPair(n.name(), SymbolType.INTERFACE), null);

		return null;
	}

	@Override
	public Void visit(ServiceNode n, SymbolTable symbols) {
		// if the service has a configuration parameter, add it
		if(n.parameterConfiguration().isPresent()){
			String configParamPath = n.parameterConfiguration().get().variablePath();
			symbols.put(SymbolTable.newPair(configParamPath, SymbolType.TYPE), null);
		}
	
		// add the service name and accept its program to find all symbols there
		symbols.put(SymbolTable.newPair(n.name(), SymbolType.SERVICE), null);
		n.program().accept(this, symbols);

		return null;
	}

	@Override
	public Void visit(InputPortInfo n, SymbolTable symbols) {
		String portName = n.id();
		symbols.put(SymbolTable.newPair(portName, SymbolType.INPUT_PORT), null);
		
		return null;
	}

	@Override
	public Void visit(OutputPortInfo n, SymbolTable symbols) {
		String portName = n.id();
		symbols.put(SymbolTable.newPair(portName, SymbolType.OUTPUT_PORT), null);
		
		return null;
	}

	@Override
	public Void visit(EmbedServiceNode n, SymbolTable symbols) {
		if(n.isNewPort()){
			symbols.put(SymbolTable.newPair(n.bindingPort().id(), SymbolType.OUTPUT_PORT), null);
		}
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SequenceStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationDeclaration decl, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationDeclaration decl, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ParallelStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(NDChoiceStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(NotificationOperationStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseOperationStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(LinkInStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(LinkOutStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(AssignStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(AddAssignStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SubtractAssignStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(MultiplyAssignStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(DivideAssignStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(IfStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(DefinitionCallStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(WhileStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(OrConditionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(AndConditionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(NotExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CompareConditionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantIntegerExpression n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantDoubleExpression n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantBoolExpression n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantLongExpression n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantStringExpression n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ProductExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SumExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(VariableExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(Scope n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(InstallStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CompensateStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ThrowStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ExitStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ExecutionInfo n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CorrelationSetInfo n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(PointerStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(DeepCopyStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(RunStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(UndefStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ValueVectorSizeExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(PreIncrementStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(PostIncrementStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(PreDecrementStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(PostDecrementStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ForStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ForEachSubNodeStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ForEachArrayItemStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SpawnStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(IsTypeExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(InstanceOfExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(TypeCastExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SynchronizedStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CurrentHandlerStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(EmbeddedServiceNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(InstallFixedVariableExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(VariablePathNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(DocumentationComment n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(FreshValueExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CourierDefinitionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(CourierChoiceStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(NotificationForwardStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseForwardStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(InterfaceExtenderDefinition n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(InlineTreeExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(VoidExpressionNode n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(ProvideUntilStatement n, SymbolTable symbols) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseExpressionNode n, SymbolTable ctx) {
		return null;
	}

	@Override
	public Void visit(IfExpressionNode n, SymbolTable Ctx) {
		return null;
	}
}
