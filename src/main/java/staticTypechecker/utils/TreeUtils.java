package staticTypechecker.utils;

import java.util.ArrayList;

import jolie.lang.Constants.OperandType;
import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Pair;
import staticTypechecker.entities.Path;
import staticTypechecker.faults.Warning;
import staticTypechecker.faults.WarningHandler;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

public class TreeUtils {
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
			InlineType newChild = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, true);
			root.addChildUnsafe(childToLookFor, newChild);
			
			if(path.size() == 1){
				ret.add(new Pair<InlineType,String>(root, childToLookFor));
			}
			else{
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), newChild, createPath));
			}
		}

		return ret;
	}

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

	/**
	 * Sets the type of the nodes at the end of the specified path by the given type.
	 * @param path the path to follow
	 * @param type the type to update all nodes at the end of the path to
	 * @param tree the tree in which the nodes reside
	 */
	public static void setTypeOfNodeByPath(Path path, Type type, Type tree){
		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true);
		
		for(Pair<InlineType,String> pair : nodesToUpdate){
			pair.key().addChildUnsafe(pair.value(), type);
		}
	}

}
