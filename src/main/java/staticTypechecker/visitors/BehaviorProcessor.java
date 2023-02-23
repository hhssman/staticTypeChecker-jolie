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
import staticTypechecker.utils.TreeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Path;

public class BehaviorProcessor implements OLVisitor<InlineType, Void> {
	private Module module;
	private Synthesizer synthesizer;
	private Checker checker;

	public BehaviorProcessor(){}

	private void printTree(InlineType tree){
		System.out.println("New tree: " + tree.prettyString());
		System.out.println("\n--------------------------\n");
	}

	public void process(Module module, InlineType tree){
		this.module = module;
		this.synthesizer = Synthesizer.get(module);
		this.checker = Checker.get(module);

		module.program().accept(this, tree);
	}
	
	@Override
	public Void visit(Program p, InlineType tree) {
		for(OLSyntaxNode child : p.children()){
			child.accept(this, tree);
		}

		return null;
	}
 
	@Override
	public Void visit(ServiceNode n, InlineType tree) {
		// if the service has a configuration parameter, add it to the tree
		if(n.parameterConfiguration().isPresent()){
			String configParamPath = n.parameterConfiguration().get().variablePath();
			Type configParamStruct = TypeConverter.convertNoFinalize(n.parameterConfiguration().get().type()); // we do not finalize this type structure, since we can change it later in the behaviours

			tree.put(configParamPath, configParamStruct);

			System.out.println("Adding config parameter for " + n.name());
			this.printTree(tree);
		}

		// accept each child of the program of the service node
		for(OLSyntaxNode child : n.program().children()){
			child.accept(this, tree);
		}
		
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, InlineType tree) {
		n.body().accept(this, tree);
		return null;
	}

	@Override
	public Void visit(SequenceStatement n, InlineType tree) {
		n.children().forEach(child -> {
			child.accept(this, tree);
		});

		return null;
	}

	@Override
	public Void visit(UndefStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		System.out.println("undef(" + path + ")");

		ArrayList<Pair<InlineType, String>> nodesToRemove = TreeUtils.findParentAndName(path, tree, false);

		for(Pair<InlineType, String> pair : nodesToRemove){
			pair.key().removeChild(pair.value());
		}
		
		this.printTree(tree);
		return null;
	}

	@Override
	public Void visit(AssignStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		System.out.println(path + " = " + n.expression().getClass());
		
		ArrayList<Pair<InlineType, String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true);
		for(Pair<InlineType, String> pair : nodesToUpdate){
			InlineType parent = pair.key();
			Type node = parent.getChild(pair.value());

			TreeUtils.updateType(pair.key(), node, n.expression(), tree);
		}

		this.printTree(tree);
		
		return null;
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
	public Void visit(DeepCopyStatement n, InlineType tree) {
		Path updatePath = new Path(n.leftPath().path());
		System.out.println(updatePath.toString() + " << " + n.rightExpression());

		OLSyntaxNode expression = n.rightExpression();
		
		// find the nodes to update and their parents
		ArrayList<Pair<InlineType, String>> nodesToUpdate = TreeUtils.findParentAndName(updatePath, tree, true);
		
		if(expression instanceof VariableExpressionNode){ // assignment on the form a = d, here we also must save the children
			Path expressionPath = new Path( ((VariableExpressionNode)expression).variablePath().path() );
			ArrayList<InlineType> nodesToMerge = TreeUtils.findNodes(expressionPath, tree, false); // the nodes at the expression path which we must merge with the node

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
			ArrayList<BasicTypeDefinition> newTypes = TreeUtils.getBasicTypesOfExpression(expression, tree);

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

		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, InlineType tree) {
		System.out.println("null process");
		this.printTree(tree);
		return null;
	}

	@Override
	public Void visit(ConstantIntegerExpression n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(AddAssignStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		
		System.out.println(path + " += " + n);
		
		TreeUtils.handleOperationAssignment(path, OperandType.ADD, n.expression(), tree);		

		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(SubtractAssignStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		
		System.out.println(path + " -= " + n);
		
		TreeUtils.handleOperationAssignment(path, OperandType.SUBTRACT, n.expression(), tree);		

		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(MultiplyAssignStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		
		System.out.println(path + " *= " + n);
		
		TreeUtils.handleOperationAssignment(path, OperandType.MULTIPLY, n.expression(), tree);		

		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(DivideAssignStatement n, InlineType tree) {
		Path path = new Path(n.variablePath().path());
		
		System.out.println(path + " /= " + n);
		
		TreeUtils.handleOperationAssignment(path, OperandType.DIVIDE, n.expression(), tree);		

		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(OutputPortInfo n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(EmbedServiceNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InputPortInfo n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ImportStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationDeclaration decl, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationDeclaration decl, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ParallelStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(NDChoiceStatement n, InlineType tree) {
		System.out.println("Choice statement\n");

		for(Pair<OLSyntaxNode, OLSyntaxNode> pair : n.children()){
			OLSyntaxNode label = pair.key();
			OLSyntaxNode behaviour = pair.value();

			label.accept(this, tree);
			behaviour.accept(this, tree);
		}

		return null;
	}

	@Override
	public Void visit(OneWayOperationStatement n, InlineType tree) {
		System.out.println("Oneway operation statement");
		this.synthesizer.synthesize(n, tree);
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationStatement n, InlineType tree) {
		System.out.println("Request response operation statement");
		this.synthesizer.synthesize(n, tree);
		return null;
	}

	@Override
	public Void visit(NotificationOperationStatement n, InlineType tree) {
		System.out.println("Notification: " + n.id() + "(" + n.outputExpression() + ")");
		this.synthesizer.synthesize(n, tree);
		this.printTree(tree);

		return null;
	}

	@Override
	public Void visit(SolicitResponseOperationStatement n, InlineType tree) {
		System.out.println("Solicit: " + n.id() + "(" + n.outputExpression() + ")" + "(" + new Path(n.inputVarPath().path()) + ")");
		this.synthesizer.synthesize(n, tree);
		this.printTree(tree);
		return null;
	}

	@Override
	public Void visit(LinkInStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(LinkOutStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(IfStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(DefinitionCallStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(WhileStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(OrConditionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(AndConditionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(NotExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CompareConditionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ConstantDoubleExpression n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ConstantBoolExpression n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ConstantLongExpression n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ConstantStringExpression n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ProductExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(SumExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(VariableExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(Scope n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InstallStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CompensateStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ThrowStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ExitStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ExecutionInfo n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CorrelationSetInfo n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(PointerStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(RunStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ValueVectorSizeExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(PreIncrementStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(PostIncrementStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(PreDecrementStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(PostDecrementStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ForStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ForEachSubNodeStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ForEachArrayItemStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(SpawnStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(IsTypeExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InstanceOfExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(TypeCastExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(SynchronizedStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CurrentHandlerStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(EmbeddedServiceNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InstallFixedVariableExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(VariablePathNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(DocumentationComment n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(FreshValueExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CourierDefinitionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(CourierChoiceStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(NotificationForwardStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseForwardStatement n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InterfaceExtenderDefinition n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(InlineTreeExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(VoidExpressionNode n, InlineType tree) {
		return null;
	}

	@Override
	public Void visit(ProvideUntilStatement n, InlineType tree) {
		return null;
	}
}
