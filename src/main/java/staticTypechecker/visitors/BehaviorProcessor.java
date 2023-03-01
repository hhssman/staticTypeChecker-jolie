package staticTypechecker.visitors;

import java.util.ArrayList;

import jolie.lang.Constants.OperandType;
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
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Pair;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.TypeConverter;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.utils.ToString;
import staticTypechecker.utils.TreeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.ModuleHandler;
import staticTypechecker.entities.Path;

public class BehaviorProcessor implements OLVisitor<Type, Type> {
	private Module module;
	private Synthesizer synthesizer;
	private Checker checker;

	public BehaviorProcessor(){}

	private void printTree(Type tree){
		System.out.println("New tree: " + tree.prettyString());
		System.out.println("\n--------------------------\n");
	}

	public Type process(Module module, Type tree){
		this.module = module;
		this.synthesizer = Synthesizer.get(module);
		this.checker = Checker.get(module);

		return module.program().accept(this, tree);
	}
	
	@Override
	public Type visit(Program p, Type tree) {
		Type T_tmp = tree;
		
		for(OLSyntaxNode child : p.children()){
			T_tmp = child.accept(this, T_tmp);
		}

		return T_tmp;
	}
 
	@Override
	public Type visit(ServiceNode n, Type tree) {
		Type T = tree;

		// if the service has a configuration parameter, add it to T
		// if(n.parameterConfiguration().isPresent()){
		// 	String configParamPath = n.parameterConfiguration().get().variablePath();
		// 	Type configParamStruct = TypeConverter.convertNoFinalize(n.parameterConfiguration().get().type()); // we do not finalize this type structure, since we can change it later in the behaviours

		// 	T.put(configParamPath, configParamStruct);

		// 	System.out.println("Adding config parameter for " + n.name());
		// 	this.printTree(T);
		// }

		// accept the program of the service node
		n.program().accept(this, T);
		
		return T;
	}

	@Override
	public Type visit(DefinitionNode n, Type tree) {
		return n.body().accept(this, tree);
	}

	@Override
	public Type visit(SequenceStatement n, Type tree) {
		Type T_tmp = tree;

		for(OLSyntaxNode child : n.children()){
			T_tmp = child.accept(this, T_tmp);
		}

		return T_tmp;
	}

	@Override
	public Type visit(UndefStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();

		ArrayList<Pair<InlineType, String>> nodesToRemove = TreeUtils.findParentAndName(path, T1, false);

		for(Pair<InlineType, String> pair : nodesToRemove){
			pair.key().removeChild(pair.value());
		}
		
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(AssignStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();
		
		ArrayList<Pair<InlineType, String>> nodesToUpdate = TreeUtils.findParentAndName(path, T1, true);
		for(Pair<InlineType, String> pair : nodesToUpdate){
			InlineType parent = pair.key();
			Type node = parent.getChild(pair.value());

			TreeUtils.updateType(pair.key(), node, n.expression(), T1);
		}

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
	public Type visit(DeepCopyStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path updatePath = new Path(n.leftPath().path());
		InlineType T1 = (InlineType)tree.copy();

		OLSyntaxNode expression = n.rightExpression();
		
		// find the nodes to update and their parents
		ArrayList<Pair<InlineType, String>> nodesToUpdate = TreeUtils.findParentAndName(updatePath, T1, true);
		
		if(expression instanceof VariableExpressionNode){ // assignment on the form a = d, here we also must save the children
			Path expressionPath = new Path( ((VariableExpressionNode)expression).variablePath().path() );
			ArrayList<InlineType> nodesToMerge = TreeUtils.findNodes(expressionPath, T1, false); // the nodes at the expression path which we must merge with the node

			for(Pair<InlineType, String> u : nodesToUpdate){
				InlineType parent = u.key();
				Type nodeToUpdate = u.key().getChild(u.value());
	
				if(nodesToMerge.size() == 1 && nodeToUpdate instanceof InlineType){ // only one new type AND node also only have one type, we can overwrite it
					InlineType mergingNode = nodesToMerge.get(0);
	
					((InlineType)nodeToUpdate).setBasicType(mergingNode.basicType());
					((InlineType)nodeToUpdate).setChildren(mergingNode.children());
				}
				else{ // multiple types for the new node, create a choice node
					ChoiceType newNode = new ChoiceType();

					if(nodeToUpdate instanceof InlineType){ // old node is inline, copy it for each new choice, and overwrite basictype and children
						for(InlineType mergingNode : nodesToMerge){ // run through the nodes to merge
							InlineType copy = (InlineType)nodeToUpdate.copy(false);

							copy.setBasicType(mergingNode.basicType());
							copy.addChildren(mergingNode.children());
	
							newNode.addChoice(copy);
						}
					}
					else{ // old node is choice, create the dot product of old choices and new choices and add them as the new choices
						for(InlineType oldChoice : ((ChoiceType)nodeToUpdate).choices()){
							for(InlineType mergingChoice : nodesToMerge){
								InlineType newChoice = new InlineType(null, null, null);
								
								// use basic type of mergingChoice and also assign mergingChoice's children last such that they overwrite any intersecting children of oldChoice
								newChoice.setBasicType(mergingChoice.basicType());
								newChoice.addChildren(oldChoice.children());
								newChoice.addChildren(mergingChoice.children());

								newNode.addChoice(newChoice);
							}
						}
					}

					parent.put(u.value(), newNode);
				}
			}	
		}
		else{ // deep copy of something else, such as a constant, sum etc., no children to save here
			ArrayList<BasicTypeDefinition> newTypes = TreeUtils.getBasicTypesOfExpression(expression, T1);

			for(Pair<InlineType, String> pair : nodesToUpdate){
				Type nodeToUpdate = pair.key().getChild(pair.value());

				if(newTypes.size() == 1 && nodeToUpdate instanceof InlineType){
					((InlineType)nodeToUpdate).setBasicType(newTypes.get(0));
				}
				else{
					ChoiceType newNode = new ChoiceType();

					for(BasicTypeDefinition type : newTypes){
						for(InlineType oldChoice : ((ChoiceType)nodeToUpdate).choices()){
							InlineType copy = (InlineType)oldChoice.copy(false);
							copy.setBasicType(type);
							newNode.addChoice(newNode);
						}
					}
				}
			}
		}

		this.printTree(T1);

		return T1;
	}

	@Override
	public Type visit(NullProcessStatement n, Type tree) {
		System.out.println(ToString.of(n));
		this.printTree(tree);
		return tree;
	}

	@Override
	public Type visit(ConstantIntegerExpression n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(AddAssignStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();
		
		TreeUtils.handleOperationAssignment(path, OperandType.ADD, n.expression(), T1);		

		this.printTree(T1);

		return T1;
	}

	@Override
	public Type visit(SubtractAssignStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();
		
		TreeUtils.handleOperationAssignment(path, OperandType.SUBTRACT, n.expression(), T1);		

		this.printTree(T1);

		return T1;
	}

	@Override
	public Type visit(MultiplyAssignStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();
		
		TreeUtils.handleOperationAssignment(path, OperandType.MULTIPLY, n.expression(), T1);		

		this.printTree(T1);

		return T1;
	}

	@Override
	public Type visit(DivideAssignStatement n, Type tree) {
		System.out.println(ToString.of(n));

		Path path = new Path(n.variablePath().path());
		InlineType T1 = (InlineType)tree.copy();
		
		TreeUtils.handleOperationAssignment(path, OperandType.DIVIDE, n.expression(), T1);		

		this.printTree(T1);

		return T1;
	}

	@Override
	public Type visit(OutputPortInfo n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(EmbedServiceNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InputPortInfo n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InterfaceDefinition n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(TypeInlineDefinition n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(TypeDefinitionLink n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(TypeChoiceDefinition n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ImportStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(OneWayOperationDeclaration decl, Type tree) {
		return tree;
	}

	@Override
	public Type visit(RequestResponseOperationDeclaration decl, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ParallelStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(NDChoiceStatement n, Type tree) {
		System.out.println("Choice statement\n");
		Type T1 = this.synthesizer.synthesize(n, tree);

		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(OneWayOperationStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(RequestResponseOperationStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(NotificationOperationStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(SolicitResponseOperationStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(LinkInStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(LinkOutStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(IfStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(DefinitionCallStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(WhileStatement n, Type tree) {
		System.out.println(ToString.of(n));
		Type T1 = this.synthesizer.synthesize(n, tree);
		T1.removeDuplicates();
		this.printTree(T1);
		return T1;
	}

	@Override
	public Type visit(OrConditionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(AndConditionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(NotExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CompareConditionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ConstantDoubleExpression n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ConstantBoolExpression n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ConstantLongExpression n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ConstantStringExpression n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ProductExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(SumExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(VariableExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(Scope n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InstallStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CompensateStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ThrowStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ExitStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ExecutionInfo n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CorrelationSetInfo n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(PointerStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(RunStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ValueVectorSizeExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(PreIncrementStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(PostIncrementStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(PreDecrementStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(PostDecrementStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ForStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ForEachSubNodeStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ForEachArrayItemStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(SpawnStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(IsTypeExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InstanceOfExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(TypeCastExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(SynchronizedStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CurrentHandlerStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(EmbeddedServiceNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InstallFixedVariableExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(VariablePathNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(DocumentationComment n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(FreshValueExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CourierDefinitionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(CourierChoiceStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(NotificationForwardStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(SolicitResponseForwardStatement n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InterfaceExtenderDefinition n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(InlineTreeExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(VoidExpressionNode n, Type tree) {
		return tree;
	}

	@Override
	public Type visit(ProvideUntilStatement n, Type tree) {
		return tree;
	}
}
