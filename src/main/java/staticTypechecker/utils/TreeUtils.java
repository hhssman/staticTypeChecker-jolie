package staticTypechecker.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jolie.lang.Constants.OperandType;
import jolie.lang.NativeType;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.TypeCastExpressionNode;
import jolie.lang.parse.ast.expression.ConstantBoolExpression;
import jolie.lang.parse.ast.expression.ConstantDoubleExpression;
import jolie.lang.parse.ast.expression.ConstantIntegerExpression;
import jolie.lang.parse.ast.expression.ConstantLongExpression;
import jolie.lang.parse.ast.expression.ConstantStringExpression;
import jolie.lang.parse.ast.expression.InstanceOfExpressionNode;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.lang.parse.ast.expression.VariableExpressionNode;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Pair;
import staticTypechecker.entities.Path;
import staticTypechecker.faults.Warning;
import staticTypechecker.faults.WarningHandler;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

public class TreeUtils {
	/**
	 * Retrieves the basic types of an expression (such as int, string, void etc.). The reason it is an arraylist is because there can be multiple basic types, in case of a choice type
	 * @param expression the expression to find the basic types of
	 * @param tree the tree in which the expression node resides
	 * @return an arraylist of the basic types found for the given expression
	 */
	// public static ArrayList<BasicTypeDefinition> getBasicTypesOfExpression(OLSyntaxNode expression, Type tree){
	// 	ArrayList<BasicTypeDefinition> types = new ArrayList<>();

	// 	// find the new type(s)
	// 	if(expression instanceof VariableExpressionNode){ // assignment on the form a = d
	// 		Path path = new Path( ((VariableExpressionNode)expression).variablePath().path() );
	// 		types = TreeUtils.getBasicTypesByPath(path, tree);
	// 	}
	// 	else if(expression instanceof SumExpressionNode){
	// 		types.addAll(TreeUtils.deriveTypeOfSum((SumExpressionNode)expression, tree));
	// 	}
	// 	else if(expression instanceof ProductExpressionNode){
	// 		types.addAll(TreeUtils.deriveTypeOfProduct((ProductExpressionNode)expression, tree));
	// 	}
	// 	else if(expression instanceof TypeCastExpressionNode){
	// 		TypeCastExpressionNode parsed = (TypeCastExpressionNode)expression;
	// 		types.add(BasicTypeDefinition.of(parsed.type()));
	// 	}
	// 	else if(expression instanceof InstanceOfExpressionNode){
	// 		types.add(BasicTypeDefinition.of(NativeType.BOOL));
	// 	}
	// 	else{
	// 		types.add(TreeUtils.getBasicType(expression));
	// 	}

	// 	return types;
	// }

	/**
	 * Retrieves the type tree of an expression
	 * @param expression the expression to find the basic types of
	 * @param tree the tree in which the expression node resides
	 * @return the type tree found for the given expression
	 */
	// public static Type getTypeOfExpression(OLSyntaxNode expression, Type tree){
	// 	if(expression instanceof VariableExpressionNode){ // a variable expression, such as a.b.c
	// 		Path path = new Path( ((VariableExpressionNode)expression).variablePath().path() );
	// 		ArrayList<Type> foundNodes = TreeUtils.findNodesExact(path, tree, false);

	// 		if(foundNodes.isEmpty()){ // return void if no nodes was found
	// 			return new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null);
	// 		}
	// 		else if(foundNodes.size() == 1){
	// 			return foundNodes.get(0);
	// 		}
	// 		else{
	// 			ChoiceType ret = new ChoiceType();
	// 			for(Type node : foundNodes){
	// 				ret.addChoiceUnsafe(node);
	// 			}
	// 			return ret;
	// 		}
	// 	}
	// 	else{ // some constant, we create an inline type with the basic type of the expression
	// 		BasicTypeDefinition type = TreeUtils.getBasicType(expression);
	// 		return new InlineType(type, null, null);
	// 	}
	// }

	/**
	 * Retrieves the type of the node at the provided path in the provided tree. Note if the path does not exist, return void type
	 * @param path the path to follow
	 * @param tree the tree to look in
	 * @return an arraylist with all the possible types of the node at the path in the tree
	 */
	// public static ArrayList<BasicTypeDefinition> getBasicTypesByPath(Path path, Type tree){
	// 	ArrayList<BasicTypeDefinition> types = new ArrayList<>();

	// 	if(path.isEmpty()){ // path is empty, TreeUtils means that tree is the correct node, thus return the type of it
	// 		if(tree == null){
	// 			types.add(BasicTypeDefinition.of(NativeType.VOID));
	// 		}
	// 		else{
	// 			types.addAll(Type.getBasicTypesOfNode(tree));
	// 		}
	// 		return types;
	// 	}
	// 	else{ // path is not empty, continue the search
	// 		String childToLookFor = path.get(0);
	
	// 		if(tree instanceof InlineType){ // easy case, check the children
	// 			InlineType parsedTree = (InlineType)tree;
	
	// 			Type child = parsedTree.getChild(childToLookFor);

	// 			if(child == null){ // node does not have child
	// 				types.add(BasicTypeDefinition.of(NativeType.VOID));
	// 				return types;
	// 			}

	// 			types.addAll( TreeUtils.getBasicTypesByPath(path.remainder(), child) );
	// 		}
	// 		else{ // choice case, check each choice
	// 			ChoiceType parsedTree = (ChoiceType)tree;
	// 			ArrayList<InlineType> possibleChoices = parsedTree.choicesWithChild(childToLookFor);

	// 			if(possibleChoices.isEmpty()){
	// 				types.add(BasicTypeDefinition.of(NativeType.VOID));
	// 				return types;
	// 			}
	
	// 			possibleChoices.forEach(c -> types.addAll(TreeUtils.getBasicTypesByPath(path.remainder(), c)));
	// 		}
	// 	}

	// 	return types;
	// }

	public static ArrayList<Pair<InlineType, String>> findParentAndName(Path path, Type root, boolean createPath){
		if(root instanceof InlineType){
			return TreeUtils.findParentAndNameRec(path, (InlineType)root, createPath);
		}
		else{
			ArrayList<Pair<InlineType, String>> result = new ArrayList<>();
			for(InlineType choice : ((ChoiceType)root).choices()){
				result.addAll(TreeUtils.findParentAndNameRec(path, choice, createPath));
			}

			return result;
		}
	}

	/**
	 * Return the node(s) at the specified path in the specified tree. NOTE: if a choice node is at the end, its choices are returned rather than the node itself
	 * @param path the path to follow
	 * @param root the root node of the tree to search
	 * @param createPath if true creates the path with void nodes if it does not exist
	 * @return an ArrayList of the nodes found at the path
	 */
	// public static ArrayList<InlineType> findNodes(Path path, Type root, boolean createPath){
	// 	ArrayList<InlineType> ret = new ArrayList<>();
	// 	ArrayList<Pair<InlineType, String>> parents = TreeUtils.findParentAndName(path, root, createPath);

	// 	for(Pair<InlineType, String> pair : parents){
	// 		InlineType parent = pair.key();
	// 		Type child = parent.getChild(pair.value());

	// 		if(child instanceof InlineType){
	// 			ret.add((InlineType)child);
	// 		}
	// 		else{
	// 			ret.addAll(((ChoiceType)child).choices());
	// 		}
	// 	}

	// 	return ret;
	// }

	/**
	 * Finds the exact nodes at the specified path. That is, if it is a choice node at the end of the path, the choice node is returned, rather than the different choices as in the findNodes function.
	 * @param path the path to follow
	 * @param root the root of the tree to search
	 * @param createPath whether the path should be created with void nodes if it is not present
	 * @return an arraylist of the nodes found at the end of the path
	 */
	public static ArrayList<Type> findNodesExact(Path path, Type root, boolean createPath){
		ArrayList<Type> ret = new ArrayList<>();
		ArrayList<Pair<InlineType, String>> parents = TreeUtils.findParentAndName(path, root, createPath);

		for(Pair<InlineType, String> pair : parents){
			InlineType parent = pair.key();
			Type child = parent.getChild(pair.value());
			ret.add(child);
		}

		return ret;
	}

	private static ArrayList<Pair<InlineType, String>> findParentAndNameRec(Path path, InlineType root, boolean createPath){
		ArrayList<Pair<InlineType, String>> ret = new ArrayList<>();
				
		if(path.isEmpty()){
			return ret;
		}

		String childToLookFor = path.get(0);
		Type childNode;

		if(root.contains(childToLookFor)){
			childNode = root.getChild(childToLookFor);
			
			if(childNode instanceof InlineType){ 
				InlineType parsedChild = (InlineType)childNode;

				if(path.size() == 1){ // TreeUtils was the child to look for
					ret.add(new Pair<InlineType, String>(root, childToLookFor));
				}
				else{ // continue the search
					ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), parsedChild, createPath));
				}
			}
			else{ // child is choice node, continue search in all choices
				ChoiceType parsedChild = (ChoiceType)childNode;
				
				if(path.size() == 1){ // TreeUtils was the child to look for, add all the choices to ret
					ret.add(new Pair<InlineType,String>(root, childToLookFor));
				}
				else{ // continue the search in each choice
					parsedChild.choices().forEach(c -> ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), c, createPath)));
				}
			}
		}
		else if(createPath){
			InlineType newChild = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null);
			root.addChild(childToLookFor, newChild);
			
			if(path.size() == 1){
				ret.add(new Pair<InlineType,String>(root, childToLookFor));
			}
			else{
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), newChild, createPath));
			}
		}

		return ret;
	}

	/**
	 * Returns an UPDATED version of the given node. The new copy has the basic type deriven from the given expression. 
	 * @param parentNode the parent of the node to update
	 * @param node the node to update
	 * @param expression the expression to derive the type from
	 * @param tree the tree containing the node
	 */
	// public static Type updateType(Type child, OLSyntaxNode expression, Type tree){
	// 	ArrayList<BasicTypeDefinition> newTypes = TreeUtils.getBasicTypesOfExpression(expression, tree);
		
	// 	// update the type
	// 	if(newTypes.size() == 0){
	// 		System.out.println("no new type???");
	// 		return child.copy();
	// 	}
	// 	else if(newTypes.size() == 1){ // only one possibility of type, overwrite existing basic types
	// 		BasicTypeDefinition newType = newTypes.get(0);
			
	// 		if(child instanceof InlineType){ // if node is a InlineType, simply change the type
	// 			return ((InlineType)child).setBasicType(newType);
	// 		}
	// 		else{ // child is a ChoiceType, change the basic type of each choice
	// 			return ((ChoiceType)child).updateBasicTypeOfChoices(newType);
	// 		}
	// 	}
	// 	else{ // more possibilities for types, node must be converted to a choice type
	// 		ChoiceType newNode = new ChoiceType();
			
	// 		if(child instanceof InlineType){ // node is an inline type, add children of node to each possible type
	// 			for(BasicTypeDefinition type : newTypes){
	// 				InlineType newChoice = (InlineType)child.copy(false);
	// 				newChoice.setBasicTypeUnsafe(type);
	// 				newNode.addChoiceUnsafe(newChoice);
	// 			}
	// 		}
	// 		else{ // node is a ChoiceType, for each old choice and for each new type: create a choice with the children of the old choice and the new type
	// 			ChoiceType parsedNode = (ChoiceType)child;
	// 			ArrayList<InlineType> newChoices = new ArrayList<>();

	// 			for(BasicTypeDefinition type : newTypes){ // loop through new types
	// 				for(InlineType oldChoice : parsedNode.choices()){ // loop through old choices
	// 					InlineType newChoice = oldChoice.copy(false);
	// 					newChoice.setBasicTypeUnsafe(type);
	// 					newChoices.add(newChoice);
	// 				}
	// 			}

	// 			newNode.setChoicesUnsafe(newChoices);
	// 		}

	// 		return newNode;
	// 	}
	// }

	// public static ArrayList<BasicTypeDefinition> deriveTypeOfSum(SumExpressionNode sum, Type tree){
	// 	return TreeUtils.der(sum.operands(), tree);
	// }

	// public static ArrayList<BasicTypeDefinition> deriveTypeOfProduct(ProductExpressionNode product, Type tree){
	// 	return TreeUtils.der(product.operands(), tree);
	// }

	// private static ArrayList<BasicTypeDefinition> der(List<Pair<OperandType, OLSyntaxNode>> operands, Type tree){
	// 	HashSet<BasicTypeDefinition> typesOfSum = new HashSet<>();
	// 	typesOfSum.add(BasicTypeDefinition.of(NativeType.VOID)); // set initial type to void to make sure it will be overwritten by any other type

	// 	for(int i = 0; i < operands.size(); i++){
	// 		OperandType currOp = operands.get(i).key();
	// 		OLSyntaxNode currTerm = operands.get(i).value();
	// 		ArrayList<BasicTypeDefinition> possibleTypesOfTerm = new ArrayList<>();

	// 		if(currTerm instanceof VariableExpressionNode){ // a variable used in the sum, such as 10 + b
	// 			Path path = new Path(((VariableExpressionNode)currTerm).variablePath().path());
	// 			ArrayList<Type> tmp = TreeUtils.findNodesExact(path, tree, false);

	// 			for(Type t : tmp){
	// 				if(t instanceof InlineType){
	// 					possibleTypesOfTerm.add(((InlineType)t).basicType());
	// 				}
	// 				else{
	// 					possibleTypesOfTerm.addAll(((ChoiceType)t).choices().stream().map(c -> c.basicType()).collect(Collectors.toList()));
	// 				}
	// 			}
	// 		}
	// 		else{
	// 			BasicTypeDefinition typeOfCurrTerm = TreeUtils.getBasicType(currTerm);
	// 			possibleTypesOfTerm = new ArrayList<>();
	// 			possibleTypesOfTerm.add(typeOfCurrTerm);
	// 		}

	// 		HashSet<BasicTypeDefinition> oldTypesOfSum = new HashSet<>(typesOfSum);
	// 		typesOfSum.clear();

	// 		for(BasicTypeDefinition t1 : oldTypesOfSum){
	// 			for(BasicTypeDefinition t2 : possibleTypesOfTerm){
	// 				BasicTypeDefinition type = TreeUtils.deriveTypeOfOperation(currOp, t1, t2);
	// 				System.out.println("deriven type: " + type.nativeType().id());
	// 				typesOfSum.add(type);
	// 			}
	// 		}

	// 	}

	// 	return new ArrayList<>(typesOfSum);
	// }
	 
	// public static BasicTypeDefinition getBasicType(OLSyntaxNode node){
	// 	if(node instanceof ConstantBoolExpression){
	// 		return BasicTypeDefinition.of(NativeType.BOOL);
	// 	}
	// 	if(node instanceof ConstantIntegerExpression){
	// 		return BasicTypeDefinition.of(NativeType.INT);
	// 	}
	// 	if(node instanceof ConstantLongExpression){
	// 		return BasicTypeDefinition.of(NativeType.LONG);
	// 	}
	// 	if(node instanceof ConstantDoubleExpression){
	// 		return BasicTypeDefinition.of(NativeType.DOUBLE);
	// 	}
	// 	if(node instanceof ConstantStringExpression){
	// 		return BasicTypeDefinition.of(NativeType.STRING);
	// 	}

	// 	return BasicTypeDefinition.of(NativeType.VOID);
	// }

	public static BasicTypeDefinition deriveTypeOfOperation(OperandType operand, BasicTypeDefinition t1, BasicTypeDefinition t2){
		// System.out.println("deriving type of " + operand.toString() + " " + t1.nativeType().id() + " " + t2.nativeType().id());

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

	// public static void handleOperationAssignment(Path path, OperandType operand, OLSyntaxNode expression, Type tree){
	// 	ArrayList<Pair<InlineType, String>> parents = TreeUtils.findParentAndName(path, tree, true);
	// 	ArrayList<BasicTypeDefinition> typesOfExpression = TreeUtils.getBasicTypesOfExpression(expression, tree);
		
	// 	if(typesOfExpression.size() == 1){ // only one type for the expression, no inlines will be converted to choices, thus we just derive the new type
	// 		for(Pair<InlineType, String> pair : parents){
	// 			InlineType parent = pair.key();
	// 			String childName = pair.value();
	// 			Type child = parent.getChild(childName);

	// 			if(child instanceof InlineType){ // child is inline, we derive the new type, create a copy of the old child, and overwrite the child in the parent
	// 				InlineType parsedChild = (InlineType)child;
	// 				BasicTypeDefinition derivenType = TreeUtils.deriveTypeOfOperation(operand, parsedChild.basicType(), typesOfExpression.get(0));
	// 				InlineType newChild = parsedChild.setBasicType(derivenType);
	// 				parent.addChild(childName, newChild);
	// 			}
	// 			else{ // child is choice, create copy and update the type of each choice in the copy
	// 				ChoiceType parsedChild = (ChoiceType)child;
	// 				ChoiceType newChild = parsedChild.copy();
					
	// 				for(InlineType choice : parsedChild.choices()){
	// 					BasicTypeDefinition derivenType = TreeUtils.deriveTypeOfOperation(operand, choice.basicType(), typesOfExpression.get(0));
	// 					InlineType newChoice = choice.setBasicType(derivenType);
	// 					newChild.addChoiceUnsafe(newChoice);
	// 				}

	// 				parent.addChild(childName, newChild);
	// 			}
	// 		}
	// 	}
	// 	else{ // expression have multiple types, thus all nodes of the path must be converted to choice if not already
	// 		for(Pair<InlineType, String> pair : parents){
	// 			InlineType parent = pair.key();
	// 			Type child = parent.getChild(pair.value());

	// 			ArrayList<InlineType> previousChoices = new ArrayList<>();
	
	// 			if(child instanceof InlineType){
	// 				previousChoices.add((InlineType)child);
	// 			}
	// 			else{
	// 				previousChoices.addAll(((ChoiceType)child).choices());
	// 			}

	// 			// create the new node and add the new choices
	// 			ChoiceType newNode = new ChoiceType();

	// 			for(InlineType prevChoice : previousChoices){ // run through the previous choices
	// 				BasicTypeDefinition typeOfNode = prevChoice.basicType();
		
	// 				for(BasicTypeDefinition typeOfExpression : typesOfExpression){ // run through the expression types
	// 					// create the merge of the two and add it as a choice
	// 					BasicTypeDefinition newType = TreeUtils.deriveTypeOfOperation(operand, typeOfNode, typeOfExpression);
	
	// 					newNode.addChoiceUnsafe(new InlineType(newType, null, null));
	// 				}
	// 			}

	// 			parent.addChild(pair.value(), newNode); // pair.value() is the child name
	// 		}
	// 	}
	// }

	/**
	 * Sets the type of the nodes at the end of the specified path by the given type.
	 * @param path the path to follow
	 * @param type the type to update all nodes at the end of the path to
	 * @param tree the tree in which the nodes reside
	 */
	public static void setTypeOfNodeByPath(Path path, Type type, Type tree){
		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true);
		
		for(Pair<InlineType,String> pair : nodesToUpdate){
			pair.key().addChild(pair.value(), type);
		}
	}

}
