package staticTypechecker.visitors;

import java.util.HashMap;

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
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode;
import jolie.lang.parse.ast.expression.InstanceOfExpressionNode;
import jolie.lang.parse.ast.expression.IsTypeExpressionNode;
import jolie.lang.parse.ast.expression.NotExpressionNode;
import jolie.lang.parse.ast.expression.OrConditionNode;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.lang.parse.ast.expression.VariableExpressionNode;
import jolie.lang.parse.ast.expression.VoidExpressionNode;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.SymbolTable_new;

/**
 * This class collects all symbols in a given module with the method "collect". A symbol in this case means the name of any type, interface, service, port etc. used in this module. Note, that also symbols imported by this module can be found in the symbol table for this module, even though it is not declared here.
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class SymbolCollector implements OLVisitor<SymbolTable_new, Void> {
	public SymbolCollector(){}

	public void collect(Module module){
		SymbolTable_new ret = new SymbolTable_new();
		module.program().accept(this, ret);
		module.setSymbols(ret);
	}

	@Override
	public Void visit(Program p, SymbolTable_new symbols) {
		// accept all children of the program
		for(OLSyntaxNode child : p.children()){
			child.accept(this, symbols);
		}

		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.name())){ // symbol already exists, possibly an error here? TODO
			return null;
		}
		else{ // new symbol
			symbols.put(n.name(), null);
		}
		
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, SymbolTable_new symbols) {
		System.out.println("In TypeDefLink, simple name: " + n.simpleName() + ", linked name: " + n.linkedTypeName() + ", just name: " + n.name());
		symbols.put(n.name(), null);
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.name())){ // symbol already exists, possibly an error here? TODO
			return null;
		}
		else{ // new symbol
			symbols.put(n.name(), null);
		}
		
		return null;
	}

	@Override
	public Void visit(ImportStatement n, SymbolTable_new symbols) {
		for(ImportSymbolTarget s : n.importSymbolTargets()){
			symbols.put(s.localSymbolName(), null);
		}

		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.name())){ // symbol exists, possibly error here? TODO
			return null;
		}
		else{ // new symbol, save the interface name and the names of each operation
			symbols.put(n.name(), null);
			n.operationsMap().keySet().stream().forEach(opName -> symbols.put(opName, null));
		}

		return null;
	}

	@Override
	public Void visit(ServiceNode n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.name())){ // symbol exists, possibly error here? TODO
			return null;
		}
		else{ // new symbol, save the service name and accept its program to find all symbols there
			symbols.put(n.name(), null);
			n.program().accept(this, symbols);
		}

		return null;
	}

	@Override
	public Void visit(InputPortInfo n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.id())){ // symbol exists, TODO throw error?
			return null;
		}
		else{ // new symbol
			symbols.put(n.id(), null);
		}
		return null;
	}

	@Override
	public Void visit(OutputPortInfo n, SymbolTable_new symbols) {
		if(symbols.containsKey(n.id())){ // symbol exists, TODO throw error?
			return null;
		}
		else{ // new symbol
			symbols.put(n.id(), null);
		}
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, SymbolTable_new symbols) {
		// System.out.println("Definition node: " + n.id() + ", body: " + n.body());

		// n.body().accept(this, symbols);

		return null;
	}

	@Override
	public Void visit(SequenceStatement n, SymbolTable_new symbols) {
		// System.out.println("In sequence, children: " + n.children());

		// for(OLSyntaxNode child : n.children()){
		// 	child.accept(this, symbols);
		// }

		return null;
	}

	@Override
	public Void visit(OneWayOperationDeclaration decl, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationDeclaration decl, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ParallelStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(NDChoiceStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(NotificationOperationStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseOperationStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(LinkInStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(LinkOutStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(AssignStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(AddAssignStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SubtractAssignStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(MultiplyAssignStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(DivideAssignStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(IfStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(DefinitionCallStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(WhileStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(OrConditionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(AndConditionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(NotExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CompareConditionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantIntegerExpression n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantDoubleExpression n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantBoolExpression n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantLongExpression n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ConstantStringExpression n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ProductExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SumExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(VariableExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(Scope n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InstallStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CompensateStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ThrowStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ExitStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ExecutionInfo n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CorrelationSetInfo n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(PointerStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(DeepCopyStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(RunStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(UndefStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ValueVectorSizeExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(PreIncrementStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(PostIncrementStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(PreDecrementStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(PostDecrementStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ForStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ForEachSubNodeStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ForEachArrayItemStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SpawnStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(IsTypeExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InstanceOfExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(TypeCastExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SynchronizedStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CurrentHandlerStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(EmbeddedServiceNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InstallFixedVariableExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(VariablePathNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(DocumentationComment n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(FreshValueExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CourierDefinitionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(CourierChoiceStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(NotificationForwardStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseForwardStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InterfaceExtenderDefinition n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InlineTreeExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(VoidExpressionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ProvideUntilStatement n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(EmbedServiceNode n, SymbolTable_new symbols) {
		return null;
	}
}
