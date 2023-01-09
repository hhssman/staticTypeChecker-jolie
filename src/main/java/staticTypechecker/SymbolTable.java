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
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Range;
import staticTypechecker.entities.ModuleHandler;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeConverter;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;

public class SymbolTable implements OLVisitor<Void, Void> {
	private HashMap<String, TypeStructure> table;
	
	public SymbolTable(){
		this.table = new HashMap<>();
	}

	public void initialize(Program p){
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

	/**
	 * @return the symbol table this instance maintains
	 */
	public HashMap<String, TypeStructure> table(){
		return this.table;
	}

	/**
	 * Checks whether the specified key in present in this SymbolTable
	 * @param key the key to look for
	 * @return true if the key exists, false otherwise
	 */
	public boolean contains(String key){
		return this.table.containsKey(key);
	}

	public TypeStructure getStructure(String key){
		return this.table.get(key);
	}

	/**
	 * Returns the names of all types equivalent to the one specified currently in this SymbolTable
	 * @param structure the structure to check for equivalence to
	 * @return a list of the names of the types which were equivalent to the given structure
	 */
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
	public Void visit(Program p, Void ctx) {
		/**
		 * Firstly, go through the types declared in this module and create the base objects for them.
		 * When each node is visited it will be finalized (have its children added).
		 * This is necessary to have a base reference to the structure object when importing from other modules even before it have been finalized
		 */
		for(OLSyntaxNode child : p.children()){
			if(child instanceof TypeDefinition){
				TypeStructure structure = TypeConverter.createBaseStructure((TypeDefinition)child);
				this.table.put( ((TypeDefinition)child).name(), structure );
			}
		}

		// accept all children of the program
		for(OLSyntaxNode child : p.children()){
			child.accept(this, null);
		}

		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, Void ctx) {
		if(this.table.containsKey(n.name())){ // base instance have been made for this type, we finalize it
			TypeConverter.finalizeBaseStructure((TypeInlineStructure)this.table.get(n.name()), n);
		}
		else{ // new type, create the structure from stratch
			TypeStructure structure = TypeConverter.convert(n);
			this.table.put(n.name(), structure);
		}
		
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, Void ctx) {
		TypeStructure structure = TypeConverter.convert(n);

		this.table.put(n.name(), structure);
		
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, Void ctx) {
		if(this.table.containsKey(n.name())){ // base instance have been made for this type, we finalize it
			TypeConverter.finalizeBaseStructure((TypeChoiceStructure)this.table.get(n.name()), n);
		}
		else{ // new type, create the structure from stratch
			TypeStructure structure = TypeConverter.convert(n);
			this.table.put(n.name(), structure);
		}

		return null;
	}

	@Override
	public Void visit(ImportStatement n, Void ctx) {
		String moduleName = "./src/test/files/" + n.importTarget().get(n.importTarget().size() - 1) + ".ol"; // TODO: figure out a way not to hardcode the path

		for(ImportSymbolTarget s : n.importSymbolTargets()){
			String typeNameAlias = s.localSymbolName();
			String originalTypeName = s.originalSymbolName();

			if(!ModuleHandler.contains(moduleName)){
				ModuleHandler.loadModule(moduleName);
			}

			TypeStructure structure = ModuleHandler.get(moduleName).symbols().getStructure(originalTypeName);
			this.table.put(typeNameAlias, structure);
		}

		return null;
	}

	public String toString(){
		return table.toString();
	}

	@Override
	public Void visit(OneWayOperationDeclaration decl, Void ctx) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationDeclaration decl, Void ctx) {
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ParallelStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SequenceStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(NDChoiceStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(NotificationOperationStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseOperationStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(LinkInStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(LinkOutStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(AssignStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(AddAssignStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SubtractAssignStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(MultiplyAssignStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(DivideAssignStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(IfStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(DefinitionCallStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(WhileStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(OrConditionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(AndConditionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(NotExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CompareConditionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ConstantIntegerExpression n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ConstantDoubleExpression n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ConstantBoolExpression n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ConstantLongExpression n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ConstantStringExpression n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ProductExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SumExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(VariableExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(Scope n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InstallStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CompensateStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ThrowStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ExitStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ExecutionInfo n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CorrelationSetInfo n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InputPortInfo n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(OutputPortInfo n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(PointerStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(DeepCopyStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(RunStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(UndefStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ValueVectorSizeExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(PreIncrementStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(PostIncrementStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(PreDecrementStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(PostDecrementStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ForStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ForEachSubNodeStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ForEachArrayItemStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SpawnStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(IsTypeExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InstanceOfExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(TypeCastExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SynchronizedStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CurrentHandlerStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(EmbeddedServiceNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InstallFixedVariableExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(VariablePathNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(DocumentationComment n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(FreshValueExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CourierDefinitionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(CourierChoiceStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(NotificationForwardStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseForwardStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InterfaceExtenderDefinition n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(InlineTreeExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(VoidExpressionNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ProvideUntilStatement n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(ServiceNode n, Void ctx) {
		return null;
	}

	@Override
	public Void visit(EmbedServiceNode n, Void ctx) {
		return null;
	}
	
}
