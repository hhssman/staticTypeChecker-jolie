package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;
import staticTypechecker.entities.Symbol;

/**
 * Represents a type in Jolie
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class Type implements Symbol {
	// basic types
	private final static InlineType BOOL = new InlineType(BasicTypeDefinition.of(NativeType.BOOL), new Range(1, 1), null, false);
	private final static InlineType INT = new InlineType(BasicTypeDefinition.of(NativeType.INT), new Range(1, 1), null, false);
	private final static InlineType LONG = new InlineType(BasicTypeDefinition.of(NativeType.LONG), new Range(1, 1), null, false);
	private final static InlineType DOUBLE = new InlineType(BasicTypeDefinition.of(NativeType.DOUBLE), new Range(1, 1), null, false);
	private final static InlineType STRING = new InlineType(BasicTypeDefinition.of(NativeType.STRING), new Range(1, 1), null, false);
	private final static InlineType VOID = new InlineType(BasicTypeDefinition.of(NativeType.VOID), new Range(1, 1), null, false);
	private final static InlineType ANY = new InlineType(BasicTypeDefinition.of(NativeType.ANY), new Range(1, 1), null, false);
	private final static InlineType OPEN_RECORD = new InlineType(BasicTypeDefinition.of(NativeType.ANY), null, null, true);

	public static InlineType BOOL(){ return Type.BOOL.copy(); };
	public static InlineType INT(){ return Type.INT.copy(); };
	public static InlineType LONG(){ return Type.LONG.copy(); };
	public static InlineType DOUBLE(){ return Type.DOUBLE.copy(); };
	public static InlineType STRING(){ return Type.STRING.copy(); };
	public static InlineType VOID(){ return Type.VOID.copy(); };
	public static InlineType ANY(){ return Type.ANY.copy(); };
	public static InlineType OPEN_RECORD(){ return Type.OPEN_RECORD.copy(); };

	// add the recursive child to the open record node
	// static {
	// 	OPEN_RECORD.addChildUnsafe("?", OPEN_RECORD);
	// }

	public abstract boolean isSubtypeOf(Type other);
	public abstract boolean equals(Object other);

	public abstract ParsingContext context();

	public abstract Type copy();
	public abstract Type copy(IdentityHashMap<Type, Type> rec);

	public abstract String prettyString();
	public abstract String prettyString(int level);
	public abstract String prettyString(int level, IdentityHashMap<Type, Void> recursive);

	/**
	 * Finds the type(s) of the given node
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
		return Type.deepCopyRec(t1.copy(), t2.copy());
	}

	public static Type deepCopyRec(Type t1, Type t2){
		if(t1 instanceof InlineType && t2 instanceof InlineType){ // both are inline types, take t2 and add the children of t1 if they are not already in t2
			InlineType p1 = (InlineType)t1;
			InlineType p2 = (InlineType)t2;
			InlineType result = p2;

			for(Entry<String, Type> ent : p1.children().entrySet()){
				result.addChildIfAbsentUnsafe(ent.getKey(), ent.getValue());
			}

			return result;
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){ // t1 is inline, t2 is choice type
			ChoiceType result = new ChoiceType();

			for(InlineType choice : ((ChoiceType)t2).choices()){
				result.addChoiceUnsafe( Type.deepCopyRec(t1, choice) );
			}

			return result;
		}
		else if(t2 instanceof InlineType){ // t1 is choice and t2 is inline type
			ChoiceType result = new ChoiceType();

			for(InlineType choice : ((ChoiceType)t1).choices()){
				result.addChoiceUnsafe( Type.deepCopyRec(choice, t2) );
			}

			return result;
		}
		else{ // both are choice types
			ChoiceType result = new ChoiceType();

			for(InlineType c1 : ((ChoiceType)t1).choices()){
				for(InlineType c2 : ((ChoiceType)t2).choices()){
					result.addChoiceUnsafe( Type.deepCopyRec(c1, c2) );
				}
			}

			return result;
		}
	}

	/**
	 * @param t1
	 * @param t2
	 * @return the result of merging t1 with t2
	 */
	public static Type merge(Type t1, Type t2){
		if(t1 == null){
			return t2;
		}
		if(t2 == null){
			return t1;
		}

		// generally a good idea, but essential in dealing with recursive types
		if(t1.isSubtypeOf(t2)){
			return t2;
		}
		if(t2.isSubtypeOf(t1)){
			return t1;
		}

		if(t1 instanceof InlineType && t2 instanceof InlineType){
			InlineType x = (InlineType)t1;
			InlineType y = (InlineType)t2;

			HashSet<String> childNames = new HashSet<String>();
			childNames.addAll(x.children().keySet());
			childNames.addAll(y.children().keySet());

			if(x.basicType().equals(y.basicType())){
				InlineType ret = new InlineType(x.basicType(), null, null, false);

				for(String childName : childNames){
					Type xChild = x.getChild(childName);
					Type yChild = y.getChild(childName);

					ret.addChildUnsafe(childName, Type.merge(xChild, yChild));
				}

				return ret;
			}

			// base types does not match, create choice with each base type but with the same children
			ChoiceType ret = new ChoiceType();
			InlineType c1 = new InlineType(x.basicType(), null, null, false);
			InlineType c2 = new InlineType(y.basicType(), null, null, false);

			ret.addChoiceUnsafe(c1);
			ret.addChoiceUnsafe(c2);

			for(String childName : childNames){
				Type xChild = x.getChild(childName);
				Type yChild = y.getChild(childName);

				Type merged = Type.merge(xChild, yChild);
				c1.addChildUnsafe(childName, merged);
				c2.addChildUnsafe(childName, merged);
			}

			return ret;
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){
			InlineType x = (InlineType)t1;
			ChoiceType y = (ChoiceType)t2;

			// merge the children of x and each choice of y
			HashMap<String, Type> children = new HashMap<>();
			children.putAll(x.children());
			for(InlineType choice : y.choices()){ // for each choice in y
				for(Entry<String, Type> ent : choice.children().entrySet()){ // for each child in the current choice
					String childName = ent.getKey();
					Type child = ent.getValue();

					if(children.containsKey(childName)){ // already there, merge
						children.put(childName, Type.merge(children.get(childName), child));
					}
					else{ // not there, simply put the child
						children.put(childName, child);
					}
				}
			}

			boolean multipleBasicTypes = !y.choices().stream().map(c -> c.basicType().equals(x.basicType())).reduce(true, (c1, c2) -> c1 && c2); // finds out if there is more than one basic type amongst x and the chocies of y

			if(multipleBasicTypes){ // more than one basic type, return a choice type
				ChoiceType ret = new ChoiceType();

				ret.addChoiceUnsafe(x.setChildren(children));
				for(InlineType choice : y.choices()){
					ret.addChoiceUnsafe(choice.setChildren(children));
				}

				return ret;
			}
			else{ // only one basic type, return x with the updated children. Note the basictype must always be the one of x in this case
				return x.setChildren(children);
			}
		}
		else if(t1 instanceof ChoiceType && t2 instanceof InlineType){
			return Type.merge(t2, t1); // its the same as the other order
		}
		else{ // both are choice types
			ChoiceType x = (ChoiceType)t1;
			ChoiceType y = (ChoiceType)t2;

			Type ret = null;

			for(InlineType c1 : x.choices()){
				ret = Type.merge(ret, c1);
			}

			for(InlineType c2 : y.choices()){
				ret = Type.merge(ret, c2);
			}

			return ret;
		}
	}
}
