package staticTypechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
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
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Range;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;

public class SymbolTable implements OLVisitor<Void, HashMap<String, TypeStructure>> {
	private HashMap<String, TypeStructure> table;
	
	public SymbolTable(Program p){
		this.table = new HashMap<>();
		this.addBaseTypes();
		p.accept(this, null);
	}

	private void addBaseTypes(){
		NativeType[] baseTypes = NativeType.values();
		for(NativeType t : baseTypes){
			TypeInlineStructure typeStruct = new TypeInlineStructure(BasicTypeDefinition.of(t), new Range(1, 1), null);

			this.table.put(t.id(), typeStruct);
		}
	}

	public HashMap<String, TypeStructure> table(){
		return this.table;
	}

	public boolean contains(String key){
		return this.table.containsKey(key);
	}

	public TypeStructure getStructure(String key){
		return this.table.get(key);
	}

	public ArrayList<String> getNames(TypeStructure structure){
		ArrayList<String> names = new ArrayList<>();

		for(Entry<String, TypeStructure> entry : this.table.entrySet()){
			if(structure.isEquivalent(entry.getValue())){
				names.add(entry.getKey());
			}
		}

		return names;
	}

	public void addType(String name, TypeStructure structure){
		this.table.put(name, structure); // TODO: decide if overwrite or not
	}

	@Override
	public HashMap<String, TypeStructure> visit(Program p, Void ctx) {
		// accept all children of the program and store them in a hashmap
		p.children()
			.stream()
			.map(node -> node.accept(this, null))
			.forEach(map -> this.table.putAll(map));

		return null;
	}

	@Override
	public HashMap<String, TypeStructure> visit(TypeInlineDefinition n, Void ctx) {
		HashMap<String, TypeStructure> ret = new HashMap<>();
		TypeStructure structure = TypeConverter.convert(n, new HashMap<>());

		ret.put(n.name(), structure);

		return ret;
	}

	@Override
	public HashMap<String, TypeStructure> visit(TypeDefinitionLink n, Void ctx) {
		HashMap<String, TypeStructure> ret = new HashMap<>();
		TypeStructure structure = TypeConverter.convert(n, new HashMap<>());

		ret.put(n.name(), structure);

		return ret;
	}

	@Override
	public HashMap<String, TypeStructure> visit(TypeChoiceDefinition n, Void ctx) {
		HashMap<String, TypeStructure> ret = new HashMap<>();
		TypeStructure structure = TypeConverter.convert(n, new HashMap<>());

		ret.put(n.name(), structure);

		return ret;
	}

	@Override
	public HashMap<String, TypeStructure> visit(ImportStatement n, Void ctx) {
		HashMap<String, TypeStructure> ret = new HashMap<>();

		for(ImportSymbolTarget s : n.importSymbolTargets()){
			ret.put(s.localSymbolName(), null); // TODO: figure out how to get the type structure
		}

		return ret;
	}

	public String toString(){
		return table.toString();
	}

	@Override
	public HashMap<String, TypeStructure> visit(OneWayOperationDeclaration decl, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(RequestResponseOperationDeclaration decl, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(DefinitionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ParallelStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SequenceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(NDChoiceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(OneWayOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(RequestResponseOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(NotificationOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SolicitResponseOperationStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(LinkInStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(LinkOutStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(AssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(AddAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SubtractAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(MultiplyAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(DivideAssignStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(IfStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(DefinitionCallStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(WhileStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(OrConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(AndConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(NotExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CompareConditionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ConstantIntegerExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ConstantDoubleExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ConstantBoolExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ConstantLongExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ConstantStringExpression n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ProductExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SumExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(VariableExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(NullProcessStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(Scope n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InstallStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CompensateStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ThrowStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ExitStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ExecutionInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CorrelationSetInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InputPortInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(OutputPortInfo n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(PointerStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(DeepCopyStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(RunStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(UndefStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ValueVectorSizeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(PreIncrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(PostIncrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(PreDecrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(PostDecrementStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ForStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ForEachSubNodeStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ForEachArrayItemStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SpawnStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(IsTypeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InstanceOfExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(TypeCastExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SynchronizedStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CurrentHandlerStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(EmbeddedServiceNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InstallFixedVariableExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(VariablePathNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InterfaceDefinition n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(DocumentationComment n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(FreshValueExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CourierDefinitionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(CourierChoiceStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(NotificationForwardStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(SolicitResponseForwardStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InterfaceExtenderDefinition n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(InlineTreeExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(VoidExpressionNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ProvideUntilStatement n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(ServiceNode n, Void ctx) {
		return new HashMap<>();
	}

	@Override
	public HashMap<String, TypeStructure> visit(EmbedServiceNode n, Void ctx) {
		return new HashMap<>();
	}
	
}
