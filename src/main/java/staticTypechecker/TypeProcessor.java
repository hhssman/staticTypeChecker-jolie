package staticTypechecker;

import java.util.ArrayList;

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
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.typeStructures.TypeStructure;

public class TypeProcessor implements OLVisitor<SymbolTable_new, Void> {
	public TypeProcessor(){}

	public void process(Module module){
		module.program().accept(this, module.symbols());
	}

	@Override
	public Void visit(Program p, SymbolTable_new symbols) {
		// ArrayList<OLSyntaxNode> importNodes = new ArrayList<>(); // list of import nodes in the given program

		// accept all children of the program, EXCEPT for the import statements. These are stored and processed afterwards, since they may 
		for(OLSyntaxNode child : p.children()){
			// if(child instanceof ImportStatement){
				// importNodes.add(child);
			// }
			// else{
				child.accept(this, symbols);
			// }
		}

		// process the import nodes
		// for(OLSyntaxNode importNode : importNodes){
		// 	importNode.accept(this, symbols);
		// }

		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, SymbolTable_new symbols) {
		processTypeDef(n, symbols);		
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, SymbolTable_new symbols) {
		processTypeDef(n, symbols);	
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, SymbolTable_new symbols) {
		processTypeDef(n, symbols);			
		return null;
	}

	private void processTypeDef(TypeDefinition n, SymbolTable_new symbols){
		if(symbols.get(n.name()) == null){ // structure have not been initialized yet, create base structure 
			TypeStructure baseStruct = TypeConverter.createBaseStructure(n);
			symbols.put(n.name(), baseStruct);
		}
		else{ // structure have been initialized, finish it
			TypeConverter.finalizeBaseStructure(symbols.get(n.name()), n);
		}
	}

	@Override
	public Void visit(ImportStatement n, SymbolTable_new symbols) {
		String moduleName = "./src/test/files/" + n.importTarget().get(n.importTarget().size() - 1) + ".ol"; // TODO: figure out a way not to hardcode the path
		
		for(ImportSymbolTarget s : n.importSymbolTargets()){
			String alias = s.localSymbolName();
			String originalName = s.originalSymbolName();

			TypeStructure structure = ModuleHandler.get(moduleName).symbols().get(originalName);
			
			symbols.put(alias, structure);
		}

		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(ServiceNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(InputPortInfo n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, SymbolTable_new symbols) {
		return null;
	}

	@Override
	public Void visit(SequenceStatement n, SymbolTable_new symbols) {
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
	public Void visit(OutputPortInfo n, SymbolTable_new symbols) {
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
