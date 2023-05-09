package staticTypechecker.visitors;

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
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.utils.ToString;
import staticTypechecker.utils.TypeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Path;

/**
 * Type checks the behaviours of the services, such as init and main.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class BehaviorProcessor implements OLVisitor<Type, Type> {
	private Module module;
	private Synthesizer synthesizer;
	private boolean print = false;

	public BehaviorProcessor(boolean print){
		this.print = print;
	}

	private void printTree(Type T){
		if(this.print){
			System.out.println(T.prettyString());
			System.out.println("\n--------------------------\n");
		}
	}

	private void printNode(OLSyntaxNode node){
		if(this.print){
			System.out.println(ToString.of(node));
		}
	}

	public Type process(Module module){
		this.module = module;
		this.synthesizer = Synthesizer.get(module);

		if(this.print){
			System.out.println("---- Processing behaviours for " + module.name() + "--------");
		}

		Type result = module.program().accept(this, Type.VOID());

		if(this.print){
			System.out.println("Final tree for module " + module.name() + ":");
			this.printTree(result);
		}
		
		return result;
	}
	
	@Override
	public Type visit(Program p, Type T) {
		for(OLSyntaxNode n : p.children()){
			T = n.accept(this, T);
		}
		return T;
	}
 
	@Override
	public Type visit(ServiceNode n, Type T) {
		Type result;
		Type T1 = T;

		if(n.parameterConfiguration().isPresent()){
			Path path = new Path(n.parameterConfiguration().get().variablePath());
			Type typeOfParam = (Type)this.module.symbols().get(n.parameterConfiguration().get().type().name(), SymbolType.TYPE);
			T1 = T.shallowCopyExcept(path);

			TypeUtils.setTypeOfNodeByPath(path, typeOfParam, T1);
			this.printTree(T1);
		}

		// accept the program of the service node
		result = n.program().accept(this, T1);

		return result;
	}

	@Override
	public Type visit(DefinitionNode n, Type T) {
		return n.body().accept(this, T);
	}

	@Override
	public Type visit(SequenceStatement n, Type T) {
		for(OLSyntaxNode child : n.children()){
			T = child.accept(this, T);
		}
		return T;
	}

	@Override
	public Type visit(UndefStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(AssignStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);		
		return T1;
	}

	/**
	 * In a case of a deep copy, a << b, a will be overwritten with everything in b, both the root and all children. However, if a has some children, which b does not have, a will keep them. Ex:
	 * 	a = 10 {
	 * 		x = "hey"
	 * 	}
	 * 	
	 * 	b = 20 {
	 * 		y = 30
	 * 	}
	 * 
	 * 	then
	 * 
	 * 	a << b ==>
	 * 
	 * 	a = 20 {
	 * 		x = "hey"
	 * 		y = 30
	 * 	}
	 */
	@Override
	public Type visit(DeepCopyStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(NullProcessStatement n, Type T) {
		this.printNode(n);
		this.printTree(T);
		return T;
	}

	@Override
	public Type visit(ConstantIntegerExpression n, Type T) {
		return T;
	}

	@Override
	public Type visit(AddAssignStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(SubtractAssignStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(MultiplyAssignStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(DivideAssignStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(OutputPortInfo n, Type T) {
		return T;
	}

	@Override
	public Type visit(EmbedServiceNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(InputPortInfo n, Type T) {
		return T;
	}

	@Override
	public Type visit(InterfaceDefinition n, Type T) {
		return T;
	}

	@Override
	public Type visit(TypeInlineDefinition n, Type T) {
		return T;
	}

	@Override
	public Type visit(TypeDefinitionLink n, Type T) {
		return T;
	}

	@Override
	public Type visit(TypeChoiceDefinition n, Type T) {
		return T;
	}

	@Override
	public Type visit(ImportStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(OneWayOperationDeclaration decl, Type T) {
		return T;
	}

	@Override
	public Type visit(RequestResponseOperationDeclaration decl, Type T) {
		return T;
	}

	@Override
	public Type visit(ParallelStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(NDChoiceStatement n, Type T) {
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(OneWayOperationStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(RequestResponseOperationStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(NotificationOperationStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(SolicitResponseOperationStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(LinkInStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(LinkOutStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(IfStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(DefinitionCallStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(WhileStatement n, Type T) {
		this.printNode(n);
		Type T1 = this.synthesizer.synthesize(n, T);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(OrConditionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(AndConditionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(NotExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(CompareConditionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(ConstantDoubleExpression n, Type T) {
		return T;
	}

	@Override
	public Type visit(ConstantBoolExpression n, Type T) {
		return T;
	}

	@Override
	public Type visit(ConstantLongExpression n, Type T) {
		return T;
	}

	@Override
	public Type visit(ConstantStringExpression n, Type T) {
		return T;
	}

	@Override
	public Type visit(ProductExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(SumExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(VariableExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(Scope n, Type T) {
		return T;
	}

	@Override
	public Type visit(InstallStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(CompensateStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ThrowStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ExitStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ExecutionInfo n, Type T) {
		return T;
	}

	@Override
	public Type visit(CorrelationSetInfo n, Type T) {
		return T;
	}

	@Override
	public Type visit(PointerStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(RunStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ValueVectorSizeExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(PreIncrementStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(PostIncrementStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(PreDecrementStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(PostDecrementStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ForStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ForEachSubNodeStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(ForEachArrayItemStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(SpawnStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(IsTypeExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(InstanceOfExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(TypeCastExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(SynchronizedStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(CurrentHandlerStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(EmbeddedServiceNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(InstallFixedVariableExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(VariablePathNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(DocumentationComment n, Type T) {
		return T;
	}

	@Override
	public Type visit(FreshValueExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(CourierDefinitionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(CourierChoiceStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(NotificationForwardStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(SolicitResponseForwardStatement n, Type T) {
		return T;
	}

	@Override
	public Type visit(InterfaceExtenderDefinition n, Type T) {
		return T;
	}

	@Override
	public Type visit(InlineTreeExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(VoidExpressionNode n, Type T) {
		return T;
	}

	@Override
	public Type visit(ProvideUntilStatement n, Type T) {
		return T;
	}
}
