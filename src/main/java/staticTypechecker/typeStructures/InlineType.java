package staticTypechecker.typeStructures;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.ArrayList;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;
import staticTypechecker.utils.Bisimulator;

/**
 * Represents the structure of a type in Jolie. It is a tree with a root node, which has a BasicTypeDefinition and a Range, and then a HashMap of child nodes, each referenced by a name. New children can be added until the finalize function is called (open record vs closed record). 
 * 
 * @author Kasper Bergstedt
 */
public class InlineType extends Type {
	private BasicTypeDefinition basicType; // the type of the root node
	private HashMap<String, Type> children; // the children of the root node
	private Range cardinality; // the cardinality of the root node
	private boolean finalized; // indicates whether this type is open to new children or not. If true, the structure is done and we do not allow more children
	private ParsingContext context;

	public InlineType(BasicTypeDefinition basicType, Range cardinality, ParsingContext context){
		this.basicType = basicType;
		this.children = new HashMap<>();
		this.cardinality = cardinality;
		this.context = context;
		this.finalized = false;
	}

	public InlineType(){
		this.basicType = null;
		this.children = new HashMap<>();
		this.cardinality = null;
		this.context = null;
		this.finalized = false;
	}

	public void reset(){
		this.basicType = null;
		this.children = null;
		this.cardinality = null;
		this.context = null;
		this.finalized = false;
	}

	public BasicTypeDefinition basicType(){
		return this.basicType;
	}

	public Range cardinality(){
		return this.cardinality;
	}

	public ParsingContext context(){
		return this.context;
	}

	public void setBasicType(BasicTypeDefinition basicType){
		this.basicType = basicType;
	}

	public void setChildren(HashMap<String, Type> children){
		this.children = children;
	}

	public void addChildren(HashMap<String, Type> children){
		this.children.putAll(children);
	}

	public void setCardinality(Range cardinality){
		this.cardinality = cardinality;
	}

	public void setContext(ParsingContext context){
		this.context = context;
	}

	/**
	 * @param name the name of the child
	 * @param structure the structure of the child
	 * @return true if this node contains a child with the specified name, false otherwise
	 */
	public boolean contains(String name, Type structure){
		if(!this.children.containsKey(name)){ // key does not exist
			return false;
		}

		return this.children.get(name).equals(structure); // if they are equivalent, return true, and false otherwise
	}

	public boolean contains(String name){
		return this.children.containsKey(name);
	}

	public Type getChild(String name){
		return this.children.get(name);
	}

	public String getChildName(Type struct){
		for(Entry<String, Type> child : this.children.entrySet()){
			if(child.getValue().equals(struct)){
				return child.getKey();
			}
		}
		return "";
	}

	public HashMap<String, Type> children(){
		return this.children;
	}

	/**
	 * Adds the entry {name, child} to this node iff the type has not been finalized. Owerwrites existing entries. 
	 * @param name the name of the child (what key to associate it to)
	 * @param child the structure of the child
	 */
	public void put(String name, Type child){
		if(!this.finalized){
			this.children.put(name, child);
		}
	}

	public void removeChild(String name){
		if(!this.finalized){
			this.children.remove(name);
		}
	}

	public void removeChild(InlineType child){
		for(Entry<String, Type> ent : this.children.entrySet()){
			if(ent.getValue().equals(child)){
				this.children.remove(ent.getKey());
			}
		}
	}

	public void finalize(){
		this.finalized = true;
	}

	/**
	 * Checks equality for the two TypeInlineStructures
	 * @param other the other object
	 * @return true if the objects are structural equivalent and false otherwise
	 */
	public boolean equals(Type other){
		if(!(other instanceof InlineType)){
			return false;
		}

		return Bisimulator.isEquivalent(this, other);
	}

	/**
	 * TODO
	 */
	public boolean isSubtypeOf(Type other){
		return Bisimulator.isSubtypeOf(this, other);
	}

	/**
	 * TODO
	 */
	public Type merge(Type other){
		return this;
	}


	public static InlineType getBaseSymbol(){
		return new InlineType(null, null, null);
	}

	public static InlineType getBaseSymbol(TypeInlineDefinition typeDef){
		return (InlineType)TypeConverter.createBaseStructure(typeDef);
	}

	public static InlineType getBaseSymbol(TypeDefinitionLink typeDef){
		return (InlineType)TypeConverter.createBaseStructure(typeDef);
	}

	public static InlineType getBasicType(NativeType type){
		return new InlineType(BasicTypeDefinition.of(type), null, null);
	}

	public Type copy(){
		return this.copy(false);
	}

	/**
	 * Creates a deep copy of this structure
	 * @param finalize whether or not the copy should be finalized
	 * @return the deep copy
	 */
	public InlineType copy(boolean finalize){
		return this.copy(finalize, new HashMap<>());
	}

	/**
	 * @param seenTypes is a HashMap which maps typestructures in the original type to their equivalent part in the copy. This is used when dealing with recursive types.
	 */
	public InlineType copy(boolean finalize, HashMap<Type, Type> seenTypes){
		InlineType struct = new InlineType(this.basicType, this.cardinality, this.context);
		seenTypes.put(this, struct);

		this.children.entrySet().forEach(child -> {
			String childName = child.getKey();
			Type childStruct = child.getValue();

			// run through the already seen types and see if we already copied this object, if so just use this copy
			for(Entry<Type, Type> seenType : seenTypes.entrySet()){
				if(seenType.getKey() == childStruct){
					struct.put(childName, seenType.getValue());
					return; // return acts as continue, since we are using a stream
				}
			}

			// otherwise we must make a new copy
			struct.put(childName, childStruct.copy(finalize, seenTypes));
		});

		if(finalize){
			struct.finalize();
		}
		
		return struct;
	}

	/**
	 * Dummy equals
	 * TODO make it correct
	 */
	public boolean equals(Object other){
		if(!(other instanceof InlineType)){
			return false;
		}

		return Bisimulator.isEquivalent(this, (InlineType)other);
	}

	public int hashCode(){
		int hashCode = 1;

		if(this.basicType != null){
			hashCode += this.basicType.hashCode();
		}

		if(this.children != null){
			hashCode += this.children.size() * 7823; // 7823 is just a large prime number
		}
		
		return hashCode;
	}

	/**
	 * Get a nice string representing this structure
	 */
	public String prettyString(){
		return this.prettyString(0, new ArrayList<>());
	}

	public String prettyString(int level, ArrayList<Type> recursive){
		// String prettyString = this.children.size() != 0 ? "\n" + "\t".repeat(level) : "";
		String prettyString = "";
		prettyString += this.basicType != null ? this.basicType.nativeType().id() + " " : "no type ";

		if(this.cardinality != null && (this.cardinality.min() != 1 || this.cardinality.max() != 1)){ // there is a range
			prettyString += "[" + this.cardinality.min() + "," + this.cardinality.max() + "]";
		}

		if(this.children.size() != 0){
			prettyString += "{";

			prettyString += this.children.entrySet().stream().map(child -> {
				if(this.containsChildExact(recursive, child.getValue())){
					return "\n" + "\t".repeat(level+1) + child.getKey() + " (recursive structure)";
				}
				else{
					recursive.add(child.getValue());
					ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice

					return "\n" + "\t".repeat(level+1) + child.getKey() + ": " + child.getValue().prettyString(level+2, rec);
				}
			})
			.collect(Collectors.joining("\n"));

			prettyString += "\n" + "\t".repeat(level) + "}";
		}

		return prettyString;
	}

	/**
	 * Utility function to check if the exact object given is present in the given arraylist. Used in order to handle recursive types
	 * @param list
	 * @param type
	 * @return
	 */
	private boolean containsChildExact(ArrayList<Type> list, Type type){
		for(int i = 0; i < list.size(); i++){
			if(list.get(i) == type){
				return true;
			}
		}

		return false;
	}

	/**
	 * Get a nice string representing this structure
	 */
	public String prettyStringHashCode(){
		return this.prettyStringHashCode(0, new ArrayList<>());
	}

	public String prettyStringHashCode(int level, ArrayList<Type> recursive){
		String prettyString = "(" + System.identityHashCode(this) + ")";
		prettyString += this.basicType != null ? this.basicType.nativeType().id() + " " : "no type ";

		if(this.cardinality != null && (this.cardinality.min() != 1 || this.cardinality.max() != 1)){ // there is a range
			prettyString += "[" + this.cardinality.min() + "," + this.cardinality.max() + "]";
		}

		if(this.children.size() != 0){
			prettyString += "{";

			prettyString += this.children.entrySet().stream().map(child -> {
				if(this.containsChildExact(recursive, child.getValue())){
					return "\n" + "\t".repeat(level+1) + "(" + System.identityHashCode(child.getValue()) + ")" + child.getKey() + " (recursive structure)";
				}
				else{
					recursive.add(child.getValue());
					ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice

					return "\n" + "\t".repeat(level+1) + "(" + System.identityHashCode(child.getValue()) + ")" + child.getKey() + ": " + child.getValue().prettyStringHashCode(level+2, rec);
				}
			})
			.collect(Collectors.joining("\n"));

			prettyString += "\n" + "\t".repeat(level) + "}";
		}

		return prettyString;
	}
}
