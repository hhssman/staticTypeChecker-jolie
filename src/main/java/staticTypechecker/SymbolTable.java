package staticTypechecker;

import java.util.HashMap;
import java.util.Map.Entry;

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

public class SymbolTable implements OLVisitor<Void, HashMap<String, TypeStructureDefinition>> {
	private HashMap<String, TypeStructureDefinition> table;
	
	public SymbolTable(Program p){
		this.table = p.accept(this, null);
	}

	public HashMap<String, TypeStructureDefinition> table(){
		return this.table;
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(Program p, Void ctx) {
		HashMap<String, TypeStructureDefinition> tmp = new HashMap<>();

		p.children()
			.stream()
			.map(node -> node.accept(this, null))
			.forEach(map -> tmp.putAll(map));

		return tmp;
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(OneWayOperationDeclaration decl, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(RequestResponseOperationDeclaration decl, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(DefinitionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ParallelStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SequenceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(NDChoiceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(OneWayOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(RequestResponseOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(NotificationOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SolicitResponseOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(LinkInStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(LinkOutStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(AssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(AddAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SubtractAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(MultiplyAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(DivideAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(IfStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(DefinitionCallStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(WhileStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(OrConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(AndConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(NotExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CompareConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ConstantIntegerExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ConstantDoubleExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ConstantBoolExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ConstantLongExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ConstantStringExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ProductExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SumExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(VariableExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(NullProcessStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(Scope n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InstallStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CompensateStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ThrowStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ExitStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ExecutionInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CorrelationSetInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InputPortInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(OutputPortInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(PointerStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(DeepCopyStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(RunStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(UndefStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ValueVectorSizeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(PreIncrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(PostIncrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(PreDecrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(PostDecrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ForStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ForEachSubNodeStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ForEachArrayItemStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SpawnStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(IsTypeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InstanceOfExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(TypeCastExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SynchronizedStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CurrentHandlerStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(EmbeddedServiceNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InstallFixedVariableExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(VariablePathNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(TypeInlineDefinition n, Void ctx) {
		HashMap<String, TypeStructureDefinition> ret = new HashMap<>();
		TypeStructureDefinition structure = TypeConverter.convert(n);

		ret.put(n.name(), structure);

		return ret;
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(TypeDefinitionLink n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InterfaceDefinition n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(DocumentationComment n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(FreshValueExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CourierDefinitionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(CourierChoiceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(NotificationForwardStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(SolicitResponseForwardStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InterfaceExtenderDefinition n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(InlineTreeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(VoidExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ProvideUntilStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(TypeChoiceDefinition n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ImportStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(ServiceNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructureDefinition> visit(EmbedServiceNode n, Void ctx) {
		return new HashMap<>();
	}
	
}
