package staticTypechecker.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeConverter;
import staticTypechecker.typeStructures.TypeStructure;
import staticTypechecker.entities.Module;

public class BehaviorProcessor implements OLVisitor<TypeInlineStructure, Void> {
	public BehaviorProcessor(){}

	public void process(Module module, TypeInlineStructure tree){
		module.program().accept(this, tree);
	}

	@Override
	public Void visit(Program p, TypeInlineStructure tree) {
		for(OLSyntaxNode child : p.children()){
			child.accept(this, tree);
		}

		return null;
	}
 
	@Override
	public Void visit(ServiceNode n, TypeInlineStructure tree) {
		// System.out.println("Service " + n.name() + "'s children: " + n.program().children());
		
		// if the service has a configuration parameter, add it to the tree
		if(n.parameterConfiguration().isPresent()){
			String configParamPath = n.parameterConfiguration().get().variablePath();
			TypeStructure configParamStruct = TypeConverter.convertNoFinalize(n.parameterConfiguration().get().type());

			tree.put(configParamPath, configParamStruct);

			System.out.println("Adding config parameter for " + n.name());
			System.out.println("New tree: " + tree.prettyString());
		}

		// accept each child of the program of the service node
		for(OLSyntaxNode child : n.program().children()){
			child.accept(this, tree);
		}
		
		return null;
	}

	@Override
	public Void visit(DefinitionNode n, TypeInlineStructure tree) {
		n.body().accept(this, tree);
		return null;
	}

	@Override
	public Void visit(SequenceStatement n, TypeInlineStructure tree) {
		n.children().forEach(child -> {
			child.accept(this, tree);
		});

		return null;
	}

	@Override
	public Void visit(UndefStatement n, TypeInlineStructure tree) {
		System.out.println("\n--------------------------");
		String pathName =  n.variablePath()
							.path()
							.stream()
							.map(p -> p.key().toString())
							.collect(Collectors.joining("."));

		System.out.println("undef(" + pathName + ")");

		List<Pair<OLSyntaxNode, OLSyntaxNode>> path = n.variablePath().path();
		boolean exists = true;

		// find the parent struct of where the last name in the path is stored
		TypeInlineStructure parentStruct = tree;
		for(int i = 0; i < path.size() - 1; i++){
			String currName = path.get(i).key().toString();

			if(parentStruct.contains(currName)){
				TypeStructure newStruct = parentStruct.getChild(currName);
	
				if(newStruct instanceof TypeInlineStructure){
					parentStruct = (TypeInlineStructure)newStruct;
				}
				else{ // it is a TypeChoiceStructure
					// currStruct = (TypeChoiceStructure)newStruct; // TODO
				}
			}
			else{ // path doesn't exist, nothing to undefine
				exists = false;
				break;
			}
		}

		if(exists){
			// actually remove the child from the structure found
			parentStruct.removeChild(path.get(path.size() - 1).key().toString());
		}
		
		System.out.println("New tree: " + tree.prettyString());
		return null;
	}

	@Override
	public Void visit(AssignStatement n, TypeInlineStructure tree) {
		System.out.println("\n--------------------------");
		// create a single string for the path name, note only used for printing purposes, can be removed later
		String pathName =  n.variablePath()
							.path()
							.stream()
							.map(p -> p.key().toString())
							.collect(Collectors.joining("."));

		System.out.println(pathName + " = " + n.expression().getClass());

		// starting at the root, follow the path in the tree to find the node to change the type of
		// creating new entries if they does not exist
		List<Pair<OLSyntaxNode, OLSyntaxNode>> path = n.variablePath().path();
		TypeStructure currNode = tree;
		String currName = "ROOT";

		for(int i = 0; i < path.size(); i++){
			currName = path.get(i).key().toString();

			if(currNode instanceof TypeInlineStructure){
				TypeInlineStructure parsedNode = (TypeInlineStructure)currNode;

				if(!parsedNode.contains(currName)){ // current node does not have a child with the name provided by the path
					TypeInlineStructure newNode = TypeInlineStructure.getBasicType(NativeType.VOID);

					parsedNode.put(currName, newNode);
					currNode = newNode;
					System.out.println("add child " + currName + " of value " + currNode);
				}
	
				currNode = parsedNode.getChild(currName);
			}
			else{ // instanceof TypeChoiceStructure
				TypeStructure node = this.searchChoices((TypeChoiceStructure)currNode, currName);
				
				// TODO await answer from Marco and do what he says regarding the path of choice types
				// if(node == null){ // none of the choices had a 
				// 	node
				// }

				currNode = node;
			}
		}

		// update the type of the found node
		currNode = this.updateType(currNode, n.expression(), tree);
		
		System.out.println("New tree: " + tree.prettyString());
		
		return null;
	}

	/**
	 * Updates the type of the provided node with the type of the provided expression. 
	 * @param node the node to update the type of
	 * @param expression the expression to derive the type from
	 * @param tree the tree containing the node
	 */
	private TypeStructure updateType(TypeStructure node, OLSyntaxNode expression, TypeInlineStructure tree){
		ArrayList<BasicTypeDefinition> newTypes = new ArrayList<>();

		if(expression instanceof ConstantIntegerExpression){
			newTypes.add(BasicTypeDefinition.of(NativeType.INT));
		}
		if(expression instanceof ConstantStringExpression){
			newTypes.add(BasicTypeDefinition.of(NativeType.STRING));
		}
		if(expression instanceof SumExpressionNode){
			System.out.println(((SumExpressionNode)expression).operands().stream().map(pair -> pair.key().toString() + ": " + pair.value().toString()).collect(Collectors.joining()));
			// TODO derive type of the sum
		}
		if(expression instanceof ConstantDoubleExpression){
			newTypes.add(BasicTypeDefinition.of(NativeType.DOUBLE));
		}
		if(expression instanceof ConstantLongExpression){
			newTypes.add(BasicTypeDefinition.of(NativeType.LONG));
		}
		if(expression instanceof ConstantBoolExpression){
			newTypes.add(BasicTypeDefinition.of(NativeType.BOOL));
		}
		if(expression instanceof VariableExpressionNode){ // assignment on the form a = d
			List<Pair<OLSyntaxNode, OLSyntaxNode>> path = ((VariableExpressionNode)expression).variablePath().path();
			newTypes = this.getType(path, tree);
		}


		if(newTypes.size() == 0){
			System.out.println("no new type???");
			return node;
		}
		else if(newTypes.size() == 1){ // only one possibility of type, return a TypeInlineStructure
			System.out.println("only one possibility");
			if(node instanceof TypeInlineStructure){ // if node is already a TypeInlineStructure, update basic type and return it
				((TypeInlineStructure)node).setBasicType(newTypes.get(0));
				return node;
			}

			// node is a choice type, create a new TypeInlineStructure
			return new TypeInlineStructure(newTypes.get(0), null, null);
		}
		else{ // more possibilities for types, return a TypeChoiceStructure 
			System.out.println("multiple possibilities");
			if(node instanceof TypeInlineStructure){ // its not a choice type, create a new and for each possible basic type add a TypeInlineStructure with the children of node
				System.out.println("of type inline");
				TypeChoiceStructure ret = new TypeChoiceStructure();

				// stream over the BasicTypeDefinitions, mapping them to TypeInlineStructures with them as basic type, and join them to an arraylist, which can be set as the choices for the choice type  
				ret.setChoices( 
					newTypes.stream()
							.map(t -> {
								TypeInlineStructure tmp = new TypeInlineStructure(t, null, null);
								for(Entry<String, TypeStructure> entry : ((TypeInlineStructure)node).children().entrySet()){
									tmp.put(entry.getKey(), entry.getValue());
								}
								return tmp;
							})
							.collect(Collectors.toCollection(ArrayList::new))
				);

				System.out.println("choices: " + ret.choices());

				return ret;

			}
			else{ // node is already a choice, overwrite the choices
				System.out.println("of choice type");
				((TypeChoiceStructure)node).setChoices( 
					newTypes.stream()
							.map(t -> new TypeInlineStructure(t, null, null))
							.collect(Collectors.toCollection(ArrayList::new))
				);

				return node;
			}
		}
	}
	
	/**
	 * Retrieves the type of the node at the provided path in the provided tree. Note if the path does not exist, return void type
	 * @param path the path to follow
	 * @param tree the tree to look in
	 * @return the type of the node at the path in the tree
	 */
	private ArrayList<BasicTypeDefinition> getType(List<Pair<OLSyntaxNode, OLSyntaxNode>> path, TypeStructure tree){
		ArrayList<BasicTypeDefinition> ret = new ArrayList<>();
		TypeStructure currNode = tree;

		for(int i = 0; i < path.size(); i++){
			String currName = path.get(i).key().toString();

			if(currNode instanceof TypeInlineStructure){
				TypeInlineStructure parsedNode = (TypeInlineStructure)currNode;

				if(!parsedNode.contains(currName)){ // current node does not have a child with the name provided by the path
					ret.add( BasicTypeDefinition.of(NativeType.VOID) );
					return ret;
				}
	
				currNode = parsedNode.getChild(currName);
			}
			else{ // instanceof TypeChoiceStructure
				TypeStructure node = this.searchChoices((TypeChoiceStructure)currNode, currName);
				currNode = node;
			}
		}


		if(currNode instanceof TypeInlineStructure){ // there is only one basic type possible
			ret.add( ((TypeInlineStructure)currNode).basicType() );
		}
		else{ // there can be multiple, find them and add them to the list
			((TypeChoiceStructure)currNode).choices()
											.stream()
											.map(c -> {
												if(c instanceof TypeInlineStructure){
													return ((TypeInlineStructure)c).basicType();
												}
												return null;
											})
											.filter(c -> c != null)
											.forEach(c -> ret.add(c));
		}

		return ret;
	}

	private TypeStructure searchChoices(TypeChoiceStructure node, String name){
		for(int i = 0; i < node.choices().size(); i++){
			TypeStructure choice = node.choices().get(i);
			
			if(choice instanceof TypeInlineStructure && ((TypeInlineStructure)choice).contains(name)){
				return ((TypeInlineStructure)choice).getChild(name);
			}
		}

		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, TypeInlineStructure tree) {
		System.out.println("null process");
		System.out.println("New tree: " + tree.prettyString());
		return null;
	}

	@Override
	public Void visit(ConstantIntegerExpression n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(AddAssignStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SubtractAssignStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(MultiplyAssignStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(DivideAssignStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(OutputPortInfo n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(EmbedServiceNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InputPortInfo n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InterfaceDefinition n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(TypeInlineDefinition n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(TypeDefinitionLink n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(TypeChoiceDefinition n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ImportStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationDeclaration decl, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationDeclaration decl, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ParallelStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(NDChoiceStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(OneWayOperationStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(RequestResponseOperationStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(NotificationOperationStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseOperationStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(LinkInStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(LinkOutStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(IfStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(DefinitionCallStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(WhileStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(OrConditionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(AndConditionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(NotExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CompareConditionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ConstantDoubleExpression n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ConstantBoolExpression n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ConstantLongExpression n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ConstantStringExpression n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ProductExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SumExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(VariableExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(Scope n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InstallStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CompensateStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ThrowStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ExitStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ExecutionInfo n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CorrelationSetInfo n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(PointerStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(DeepCopyStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(RunStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ValueVectorSizeExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(PreIncrementStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(PostIncrementStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(PreDecrementStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(PostDecrementStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ForStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ForEachSubNodeStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ForEachArrayItemStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SpawnStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(IsTypeExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InstanceOfExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(TypeCastExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SynchronizedStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CurrentHandlerStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(EmbeddedServiceNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InstallFixedVariableExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(VariablePathNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(DocumentationComment n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(FreshValueExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CourierDefinitionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(CourierChoiceStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(NotificationForwardStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(SolicitResponseForwardStatement n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InterfaceExtenderDefinition n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(InlineTreeExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(VoidExpressionNode n, TypeInlineStructure tree) {
		return null;
	}

	@Override
	public Void visit(ProvideUntilStatement n, TypeInlineStructure tree) {
		return null;
	}
}
