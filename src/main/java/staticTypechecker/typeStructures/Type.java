package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Range;
import staticTypechecker.entities.Symbol;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class Type implements Symbol {
	// basic types
	public final static InlineType BOOL = new InlineType(BasicTypeDefinition.of(NativeType.BOOL), new Range(1, 1), null);
	public final static InlineType INT = new InlineType(BasicTypeDefinition.of(NativeType.INT), new Range(1, 1), null);
	public final static InlineType LONG = new InlineType(BasicTypeDefinition.of(NativeType.LONG), new Range(1, 1), null);
	public final static InlineType DOUBLE = new InlineType(BasicTypeDefinition.of(NativeType.DOUBLE), new Range(1, 1), null);
	public final static InlineType STRING = new InlineType(BasicTypeDefinition.of(NativeType.STRING), new Range(1, 1), null);
	public final static InlineType VOID = new InlineType(BasicTypeDefinition.of(NativeType.VOID), new Range(1, 1), null);

	public abstract boolean isSubtypeOf(Type other);
	public abstract boolean equals(Object other);
	// public abstract Type merge(Type other);

	// public abstract void put(String childName, Type structure);
	// public abstract void removeDuplicates();
	public abstract Type copy();
	public abstract Type copy(boolean finalize);
	public abstract Type copy(boolean finalize, HashMap<Type, Type> rec);
	public abstract String prettyStringHashCode();
	public abstract String prettyStringHashCode(int level, ArrayList<Type> recursive);

	public abstract String prettyString();
	public abstract String prettyString(int level);
	public abstract String prettyString(int level, ArrayList<Type> recursive);

	/**
	 * Finds the type(s) of a single node
	 * @param node the node to find the type(s) of
	 * @return an ArrayList of BasicTypeDefinitions corresponding to the type(s) of the specified node
	 */
	public static ArrayList<BasicTypeDefinition> getBasicTypesOfNode(Type node){
		ArrayList<BasicTypeDefinition> types = new ArrayList<>();
		
		if(node instanceof InlineType){
			InlineType parsedTree = (InlineType)node;
			types.add(parsedTree.basicType());
		}
		else{
			ChoiceType parsedTree = (ChoiceType)node;
			parsedTree.choices().forEach(c -> {
				types.addAll( Type.getBasicTypesOfNode(c) );
			});
		}

		return types;
	}

	/**
	 * Returns the deep copied version of t2 onto t1, that is t1 << t2
	 * @param t1
	 * @param t2
	 * @return the result of t1 << t2
	 */
	public static Type deepCopy(Type t1, Type t2){
		if(t1 instanceof InlineType && t2 instanceof InlineType){ // both are inline types, create copy of t1 and overwrite basic type and children from t2
			InlineType p1 = (InlineType)t1;
			InlineType p2 = (InlineType)t2;
			InlineType result = p1.copy();

			result.setBasicTypeUnsafe(p2.basicType());
			for(Entry<String, Type> childOfP2 : p2.children().entrySet()){
				result.addChildUnsafe(childOfP2.getKey(), childOfP2.getValue());
			}			
			
			return result;
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){ // t1 is inline, t2 is choice type
			ChoiceType result = new ChoiceType();

			for(InlineType choice : ((ChoiceType)t2).choices()){
				result.addChoiceUnsafe( Type.deepCopy(t1, choice) );
			}

			return result;
		}
		else if(t2 instanceof InlineType){ // t1 is choice and t2 is inline type
			ChoiceType result = new ChoiceType();

			for(InlineType choice : ((ChoiceType)t1).choices()){
				result.addChoiceUnsafe( Type.deepCopy(choice, t2) );
			}

			return result;
		}
		else{ // both are choice types
			ChoiceType result = new ChoiceType();

			for(InlineType c1 : ((ChoiceType)t1).choices()){
				for(InlineType c2 : ((ChoiceType)t2).choices()){
					result.addChoiceUnsafe( Type.deepCopy(c1, c2) );
				}
			}

			return result;
		}
	}
}
