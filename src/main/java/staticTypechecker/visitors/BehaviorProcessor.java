package staticTypechecker.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jolie.lang.NativeType;
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
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeConverter;
import staticTypechecker.typeStructures.TypeStructure;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Path;
import staticTypechecker.faults.Warning;
import staticTypechecker.faults.WarningHandler;

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

		Path path = new Path(n.variablePath().path());
		System.out.println("undef(" + path + ")");

		ArrayList<Pair<TypeInlineStructure, String>> nodesToRemove = this.findParentAndName(path, tree, false);

		for(Pair<TypeInlineStructure, String> pair : nodesToRemove){
			pair.key().removeChild(pair.value());
		}
		
		System.out.println("New tree: " + tree.prettyString());
		return null;
	}

	@Override
	public Void visit(AssignStatement n, TypeInlineStructure tree) {
		System.out.println("\n--------------------------");
		
		Path path = new Path(n.variablePath().path());
		System.out.println(path + " = " + n.expression().getClass());
		
		ArrayList<Pair<TypeInlineStructure, String>> nodesToUpdate = this.findParentAndName(path, tree, true);
		for(Pair<TypeInlineStructure, String> pair : nodesToUpdate){
			TypeInlineStructure parent = pair.key();
			TypeStructure node = parent.getChild(pair.value());

			this.updateType(pair.key(), node, n.expression(), tree);
		}

		System.out.println("New tree: " + tree.prettyString());
		
		return null;
	}

	
	private ArrayList<Pair<TypeInlineStructure, String>> findParentAndName(Path path, TypeInlineStructure root, boolean createPath){
		return this.findNodesRec(path, root, createPath);
	}

	/**
	 * Return the node(s) at the specified path in the specified tree. NOTE: if a choice node is at the end, its choices are returned rather than the node itself
	 * @param path the path to follow
	 * @param root the root node of the tree to search
	 * @param createPath if true creates the path with void nodes if it does not exist
	 * @return an ArrayList of the nodes found at the path
	 */
	private ArrayList<TypeInlineStructure> findNodes(Path path, TypeInlineStructure root, boolean createPath){
		ArrayList<TypeInlineStructure> ret = new ArrayList<>();
		ArrayList<Pair<TypeInlineStructure, String>> parents = this.findParentAndName(path, root, createPath);

		for(Pair<TypeInlineStructure, String> pair : parents){
			TypeInlineStructure parent = pair.key();
			TypeStructure child = parent.getChild(pair.value());

			if(child instanceof TypeInlineStructure){
				ret.add((TypeInlineStructure)child);
			}
			else{
				ret.addAll(((TypeChoiceStructure)child).choices());
			}
		}

		return ret;
	}

	/**
	 * Finds the exact nodes at the specified path. That is, if it is a choice node at the end of the path, the choice node is returned, rather than the different choices as in the findNodes function.
	 * @param path the path to follow
	 * @param root the root of the tree to search
	 * @param createPath whether the path should be created with void nodes if it is not present
	 * @return an arraylist of the nodes found at the end of the path
	 */
	private ArrayList<TypeStructure> findNodesExact(Path path, TypeInlineStructure root, boolean createPath){
		ArrayList<TypeStructure> ret = new ArrayList<>();
		ArrayList<Pair<TypeInlineStructure, String>> parents = this.findParentAndName(path, root, createPath);

		for(Pair<TypeInlineStructure, String> pair : parents){
			TypeInlineStructure parent = pair.key();
			TypeStructure child = parent.getChild(pair.value());
			ret.add(child);
		}

		return ret;
	}

	private ArrayList<Pair<TypeInlineStructure, String>> findNodesRec(Path path, TypeInlineStructure root, boolean createPath){
		ArrayList<Pair<TypeInlineStructure, String>> ret = new ArrayList<>();
				
		if(path.isEmpty()){
			return ret;
		}

		String childToLookFor = path.get(0);
		TypeStructure childNode;

		if(root.contains(childToLookFor)){
			childNode = root.getChild(childToLookFor);
			
			if(childNode instanceof TypeInlineStructure){ 
				TypeInlineStructure parsedChild = (TypeInlineStructure)childNode;

				if(path.size() == 1){ // this was the child to look for
					ret.add(new Pair<TypeInlineStructure, String>(root, childToLookFor));
				}
				else{ // continue the search
					ret.addAll(this.findNodesRec(path.remainder(), parsedChild, createPath));
				}
			}
			else{ // child is choice node, continue search in all choices
				TypeChoiceStructure parsedChild = (TypeChoiceStructure)childNode;
				
				if(path.size() == 1){ // this was the child to look for, add all the choices to ret
					ret.add(new Pair<TypeInlineStructure,String>(root, childToLookFor));
				}
				else{ // continue the search in each choice
					parsedChild.choices().forEach(c -> ret.addAll(this.findNodesRec(path.remainder(), c, createPath)));
				}
			}
		}
		else if(createPath){
			TypeInlineStructure newChild = TypeInlineStructure.getBasicType(NativeType.VOID);
			root.put(childToLookFor, newChild);
			
			if(path.size() == 1){
				ret.add(new Pair<TypeInlineStructure,String>(root, childToLookFor));
			}
			else{
				ret.addAll(this.findNodesRec(path.remainder(), newChild, createPath));
			}
		}

		return ret;
	}

	private ArrayList<BasicTypeDefinition> getTypesOfExpression(OLSyntaxNode expression, TypeInlineStructure tree){
		ArrayList<BasicTypeDefinition> types = new ArrayList<>();

		// find the new type(s)
		if(expression instanceof VariableExpressionNode){ // assignment on the form a = d
			Path path = new Path( ((VariableExpressionNode)expression).variablePath().path() );
			types = this.getTypeByPath(path, tree);
		}
		else if(expression instanceof SumExpressionNode){
			types.addAll(this.deriveTypeOfSum((SumExpressionNode)expression, tree));
		}
		else if(expression instanceof ProductExpressionNode){
			types.addAll(this.deriveTypeOfProduct((ProductExpressionNode)expression, tree));
		}
		else if(expression instanceof TypeCastExpressionNode){
			TypeCastExpressionNode parsed = (TypeCastExpressionNode)expression;
			types.add(BasicTypeDefinition.of(parsed.type()));
		}
		else if(expression instanceof InstanceOfExpressionNode){
			types.add(BasicTypeDefinition.of(NativeType.BOOL));
		}
		else{
			types.add(this.getBasicType(expression));
		}

		return types;
	}

	/**
	 * Updates the provided node with the type given by expression.
	 * @param parentNode the parent of the node to update
	 * @param node the node to update
	 * @param expression the expression to derive the type from
	 * @param tree the tree containing the node
	 */
	private TypeStructure updateType(TypeInlineStructure parentNode, TypeStructure child, OLSyntaxNode expression, TypeInlineStructure tree){
		ArrayList<BasicTypeDefinition> newTypes = this.getTypesOfExpression(expression, tree);
		String childName = parentNode.getChildName(child);

		// update the type
		if(newTypes.size() == 0){
			System.out.println("no new type???");
			return child;
		}
		else if(newTypes.size() == 1){ // only one possibility of type, overwrite existing basic types
			BasicTypeDefinition newType = newTypes.get(0);

			if(child instanceof TypeInlineStructure){ // if node is a TypeInlineStructure, simply change the type
				((TypeInlineStructure)child).setBasicType(newType);
			}
			else{ // child is a TypeChoiceStructure, change the basic type of each choice
				((TypeChoiceStructure)child).updateBasicTypeOfChoices(newType);
			}
			
			return child;
		}
		else{ // more possibilities for types, node must be converted to a choice type
			if(child instanceof TypeInlineStructure){ // node is an inline type, add children of node to each possible type
				TypeChoiceStructure newNode = new TypeChoiceStructure();

				for(BasicTypeDefinition type : newTypes){
					TypeInlineStructure newChoice = (TypeInlineStructure)child.copy(false);
					newChoice.setBasicType(type);
					newNode.addChoice(newChoice);
				}

				parentNode.put(childName, newNode); // update the child
				return newNode;
			}
			else{ // node is a TypeChoiceStructure, for each old choice and for each new type: create a choice with the children of the old choice and the new type
				TypeChoiceStructure parsedNode = (TypeChoiceStructure)child;

				for(BasicTypeDefinition type : newTypes){ // loop through new types
					for(TypeInlineStructure oldChoice : parsedNode.choices()){ // loop through old choices
						TypeInlineStructure newChoice = oldChoice.copy(false);
						newChoice.setBasicType(type);
						parsedNode.addChoice(newChoice);
					}
				}

				return child;
			}
		}
	}

	private ArrayList<BasicTypeDefinition> deriveTypeOfSum(SumExpressionNode sum, TypeInlineStructure tree){
		return this.der(sum.operands(), tree);
	}

	private ArrayList<BasicTypeDefinition> deriveTypeOfProduct(ProductExpressionNode product, TypeInlineStructure tree){
		return this.der(product.operands(), tree);
	}

	private ArrayList<BasicTypeDefinition> der(List<Pair<OperandType, OLSyntaxNode>> operands, TypeInlineStructure tree){
		HashSet<BasicTypeDefinition> typesOfSum = new HashSet<>();
		typesOfSum.add(BasicTypeDefinition.of(NativeType.VOID)); // set initial type to void to make sure it will be overwritten by any other type

		for(int i = 0; i < operands.size(); i++){
			OperandType currOp = operands.get(i).key();
			OLSyntaxNode currTerm = operands.get(i).value();
			ArrayList<BasicTypeDefinition> possibleTypesOfTerm;

			if(currTerm instanceof VariableExpressionNode){ // a variable used in the sum, such as 10 + b
				possibleTypesOfTerm = this.getTypeByPath(new Path(((VariableExpressionNode)currTerm).variablePath().path()), tree);
			}
			else{
				BasicTypeDefinition typeOfCurrTerm = this.getBasicType(operands.get(i).value());
				possibleTypesOfTerm = new ArrayList<>();
				possibleTypesOfTerm.add(typeOfCurrTerm);
			}

			HashSet<BasicTypeDefinition> oldTypesOfSum = (HashSet<BasicTypeDefinition>)typesOfSum.clone();
			typesOfSum.clear();

			for(BasicTypeDefinition t1 : oldTypesOfSum){
				for(BasicTypeDefinition t2 : possibleTypesOfTerm){
					BasicTypeDefinition type = this.deriveTypeOfOperation(currOp, t1, t2);
					System.out.println("deriven type: " + type.nativeType().id());
					typesOfSum.add(type);
				}
			}

		}

		return new ArrayList<>(typesOfSum);
	}
	 
	private BasicTypeDefinition getBasicType(OLSyntaxNode node){
		if(node instanceof ConstantBoolExpression){
			return BasicTypeDefinition.of(NativeType.BOOL);
		}
		if(node instanceof ConstantIntegerExpression){
			return BasicTypeDefinition.of(NativeType.INT);
		}
		if(node instanceof ConstantLongExpression){
			return BasicTypeDefinition.of(NativeType.LONG);
		}
		if(node instanceof ConstantDoubleExpression){
			return BasicTypeDefinition.of(NativeType.DOUBLE);
		}
		if(node instanceof ConstantStringExpression){
			return BasicTypeDefinition.of(NativeType.STRING);
		}

		return BasicTypeDefinition.of(NativeType.VOID);
	}

	private BasicTypeDefinition deriveTypeOfOperation(OperandType operand, BasicTypeDefinition t1, BasicTypeDefinition t2){
		System.out.println("deriving type of " + operand.toString() + " " + t1.nativeType().id() + " " + t2.nativeType().id());

		NativeType type1 = t1.nativeType();
		NativeType type2 = t2.nativeType();

		// if one of the types is void, return the other
		if(type1 == NativeType.VOID){
			return BasicTypeDefinition.of(type2);
		}
		else if(type2 == NativeType.VOID){
			return BasicTypeDefinition.of(type1);
		}

		// check all combinations and return the appropriate type (NOTE the appropriate types have been deducted by doing all these sums and seeing what the runtime interpreter derives them to)
		if(type1 == NativeType.BOOL){
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){ // adding a bool with a string results in string
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				else{ // all other cases are bools
					return BasicTypeDefinition.of(NativeType.BOOL);
				}
			}
			else{
				return BasicTypeDefinition.of(type2);
			}
		}
		else if(type1 == NativeType.INT){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT){
				return BasicTypeDefinition.of(NativeType.INT);
			}
			else if(type2 == NativeType.LONG){
				return BasicTypeDefinition.of(NativeType.LONG);
			}
			else if(type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			else if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				if(operand == OperandType.SUBTRACT || operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.INT);
				}

				// TODO throw warning, division and modulo with a string only allowed if string can be parsed to int
				WarningHandler.addWarning(new Warning("the operations 'int / string' and 'int % string' are only allowed if the string can be parsed to a number"));
				return BasicTypeDefinition.of(NativeType.INT);
			}
		}
		else if(type1 == NativeType.LONG){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT){
				return BasicTypeDefinition.of(NativeType.LONG);
			}
			if(type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				else if(operand == OperandType.SUBTRACT || operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.LONG);
				}

				// TODO throw warning, division and modulo with a string only allowed if string can be parsed to int
				WarningHandler.addWarning(new Warning("the operations 'long / string' and 'long % string' are only allowed if the string can be parsed to a number"));
				return BasicTypeDefinition.of(NativeType.LONG);
			}

		}
		else if(type1 == NativeType.DOUBLE){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT || type2 == NativeType.LONG || type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.DOUBLE);
				}
				
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
		}
		else if(type1 == NativeType.STRING){
			if(type2 == NativeType.BOOL){
				if(operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.BOOL);
				}

				return BasicTypeDefinition.of(NativeType.STRING);
			}

			if(operand == OperandType.ADD){
				return BasicTypeDefinition.of(NativeType.STRING);
			}

			return BasicTypeDefinition.of(type2);
		}

		return BasicTypeDefinition.of(NativeType.VOID);
	}
	
	/**
	 * Retrieves the type of the node at the provided path in the provided tree. Note if the path does not exist, return void type
	 * @param path the path to follow
	 * @param tree the tree to look in
	 * @return an arraylist with all the possible types of the node at the path in the tree
	 */
	private ArrayList<BasicTypeDefinition> getTypeByPath(Path path, TypeStructure tree){
		ArrayList<BasicTypeDefinition> types = new ArrayList<>();

		if(path.isEmpty()){ // path is empty, this means that tree is the correct node, thus return the type of it
			if(tree == null){
				types.add(BasicTypeDefinition.of(NativeType.VOID));
			}
			else{
				types.addAll(this.getTypesOfNode(tree));
			}
			return types;
		}
		else{ // path is not empty, continue the search
			String childToLookFor = path.get(0);
	
			if(tree instanceof TypeInlineStructure){ // easy case, check the children
				TypeInlineStructure parsedTree = (TypeInlineStructure)tree;
	
				TypeStructure child = parsedTree.getChild(childToLookFor);

				if(child == null){ // node does not have child
					types.add(BasicTypeDefinition.of(NativeType.VOID));
					return types;
				}

				types.addAll( this.getTypeByPath(path.remainder(), child) );
			}
			else{ // choice case, check each choice
				TypeChoiceStructure parsedTree = (TypeChoiceStructure)tree;
				ArrayList<TypeInlineStructure> possibleChoices = parsedTree.choicesWithChild(childToLookFor);

				if(possibleChoices.isEmpty()){
					types.add(BasicTypeDefinition.of(NativeType.VOID));
					return types;
				}
	
				possibleChoices.forEach(c -> types.addAll(this.getTypeByPath(path.remainder(), c)));
			}
		}

		return types;
	}

	/**
	 * Finds the type(s) of a single node
	 * @param node the node to find the type(s) of
	 * @return an ArrayList of BasicTypeDefinitions corresponding to the type(s) of the specified node
	 */
	private ArrayList<BasicTypeDefinition> getTypesOfNode(TypeStructure node){
		ArrayList<BasicTypeDefinition> types = new ArrayList<>();
		
		if(node instanceof TypeInlineStructure){
			TypeInlineStructure parsedTree = (TypeInlineStructure)node;
			types.add(parsedTree.basicType());
		}
		else{
			TypeChoiceStructure parsedTree = (TypeChoiceStructure)node;
			parsedTree.choices().forEach(c -> {
				types.addAll( this.getTypesOfNode(c) );
			});
		}

		return types;
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
	public Void visit(DeepCopyStatement n, TypeInlineStructure tree) {
		System.out.println("\n--------------------------");
		Path updatePath = new Path(n.leftPath().path());
		System.out.println(updatePath.toString() + " << " + n.rightExpression());

		OLSyntaxNode expression = n.rightExpression();
		
		// find the nodes to update and their parents
		ArrayList<Pair<TypeInlineStructure, String>> nodesToUpdate = this.findParentAndName(updatePath, tree, true);
		
		// TODO: properly document this large as shit piece of code
		if(expression instanceof VariableExpressionNode){ // assignment on the form a = d, here we also must save the children
			Path expressionPath = new Path( ((VariableExpressionNode)expression).variablePath().path() );
			ArrayList<TypeInlineStructure> nodesToMerge = this.findNodes(expressionPath, tree, false); // the nodes at the expression path which we must merge with the node

			for(Pair<TypeInlineStructure, String> u : nodesToUpdate){
				TypeInlineStructure parent = u.key();
				TypeStructure nodeToUpdate = u.key().getChild(u.value());
	
				if(nodesToMerge.size() == 1 && nodeToUpdate instanceof TypeInlineStructure){ // only one new type AND node also only have one type, we can overwrite it
					TypeInlineStructure mergingNode = nodesToMerge.get(0);
	
					((TypeInlineStructure)nodeToUpdate).setBasicType(mergingNode.basicType());
					((TypeInlineStructure)nodeToUpdate).setChildren(mergingNode.children());
				}
				else{ // multiple types for the new node, create a choice node
					TypeChoiceStructure newNode = new TypeChoiceStructure();

					if(nodeToUpdate instanceof TypeInlineStructure){ // old node is inline, copy it for each new choice, and overwrite basictype and children
						for(TypeInlineStructure mergingNode : nodesToMerge){ // run through the nodes to merge
							TypeInlineStructure copy = (TypeInlineStructure)nodeToUpdate.copy(false);

							copy.setBasicType(mergingNode.basicType());
							copy.addChildren(mergingNode.children());
	
							newNode.addChoice(copy);
						}
					}
					else{ // old node is choice, create the dot product of old choices and new choices and add them as the new choices
						for(TypeInlineStructure oldChoice : ((TypeChoiceStructure)nodeToUpdate).choices()){
							for(TypeInlineStructure mergingChoice : nodesToMerge){
								TypeInlineStructure newChoice = new TypeInlineStructure(null, null, null);
								
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
			ArrayList<BasicTypeDefinition> newTypes = this.getTypesOfExpression(expression, tree);

			for(Pair<TypeInlineStructure, String> pair : nodesToUpdate){
				TypeStructure nodeToUpdate = pair.key().getChild(pair.value());

				if(newTypes.size() == 1 && nodeToUpdate instanceof TypeInlineStructure){
					((TypeInlineStructure)nodeToUpdate).setBasicType(newTypes.get(0));
				}
				else{
					TypeChoiceStructure newNode = new TypeChoiceStructure();

					for(BasicTypeDefinition type : newTypes){
						for(TypeInlineStructure oldChoice : ((TypeChoiceStructure)nodeToUpdate).choices()){
							TypeInlineStructure copy = (TypeInlineStructure)oldChoice.copy(false);
							copy.setBasicType(type);
							newNode.addChoice(newNode);
						}
					}
				}
			}
		}

		System.out.println("New tree: " + tree.prettyString());

		return null;
	}

	@Override
	public Void visit(NullProcessStatement n, TypeInlineStructure tree) {
		System.out.println("null process");
		System.out.println("New tree: " + tree.prettyString());
		return null;
	}

	/**
	 * Checks whether the node at the end of the given path is equivalent to the specified type.
	 * @param path the path to follow
	 * @param type the type to check for
	 * @param tree the tree to search for the node
	 * @return true if the node is equivalent to the given type, false otherwise
	 */
	private boolean check(Path path, TypeStructure type, TypeInlineStructure tree){
		// TODO: talk to marco about what to do in a case of a choice (e.g. if A is a choice and only one of the choices has child X, and we check for A.X, will we return true or false?)
		ArrayList<TypeStructure> nodesToCheck = this.findNodesExact(path, tree, false);
		
		for(TypeStructure node : nodesToCheck){
			if(node.equals(type)){
				return true;
			}
		}
		
		return false;
	}

	private TypeStructure synthesize(Path path, TypeInlineStructure tree){
		ArrayList<TypeInlineStructure> nodes = this.findNodes(path, tree, false);

		if(nodes.isEmpty()){
			return new TypeInlineStructure(BasicTypeDefinition.of(NativeType.VOID), null, null);
		}
		if(nodes.size() == 1){
			return new TypeInlineStructure(nodes.get(0).basicType(), null, null);
		}
		else{
			TypeChoiceStructure ret = new TypeChoiceStructure();
			for(TypeInlineStructure choice : nodes){
				ret.addChoice(choice);
			}
			return ret;
		}
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
