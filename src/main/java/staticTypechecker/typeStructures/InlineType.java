package staticTypechecker.typeStructures;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.ArrayList;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
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
	private boolean openRecord; // indicates whether this type is an open- or closed-record 
	private ParsingContext context;

	public InlineType(BasicTypeDefinition basicType, Range cardinality, ParsingContext context, boolean openRecord){
		this.basicType = basicType;
		this.children = new HashMap<>();
		this.cardinality = cardinality;
		this.context = context;
		this.openRecord = openRecord;
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

	public boolean isOpen(){
		return this.openRecord;
	}

	public boolean isClosed(){
		return !this.openRecord;
	}

	public void setBasicTypeUnsafe(BasicTypeDefinition basicType){
		this.basicType = basicType;
	}

	public void setChildrenUnsafe(HashMap<String, Type> children){
		this.children = children;
	}

	public void addChildrenUnsafe(HashMap<String, Type> children){
		this.children.putAll(children);
	}

	public void setCardinalityUnsafe(Range cardinality){
		this.cardinality = cardinality;
	}

	public void setContextUnsafe(ParsingContext context){
		this.context = context;
	}

	public void setOpenStatusUnsafe(boolean openRecord){
		this.openRecord = openRecord;
	}

	/**
	 * Adds the entry {name, child} to this node. Owerwrites existing entries. 
	 * @param name the name of the child (what key to associate it to)
	 * @param child the structure of the child
	 */
	public void addChildUnsafe(String name, Type child){
		this.children.put(name, child);
	}

	public void removeChildUnsafe(String name){
		this.children.remove(name);
	}


	public InlineType setBasicType(BasicTypeDefinition basicType){
		InlineType copy = this.copy();
		copy.setBasicTypeUnsafe(basicType);
		return copy;
	}

	public InlineType setChildren(HashMap<String, Type> children){
		InlineType copy = this.copy();
		copy.setChildrenUnsafe(children);
		return copy;
	}

	public InlineType addChildren(HashMap<String, Type> children){
		InlineType copy = this.copy();
		copy.addChildrenUnsafe(children);
		return copy;
	}

	public boolean contains(String name){
		return this.children.containsKey(name);
	}

	public Type getChild(String name){
		if(name == "?" && this.isOpen()){
			return Type.OPEN_RECORD;
		}

		return this.children.get(name);
	}

	public String getChildName(Type struct){
		for(Entry<String, Type> child : this.children.entrySet()){
			if(child.getValue() == struct){
				return child.getKey();
			}
		}
		return null;
	}

	public HashMap<String, Type> children(){
		return this.children;
	}

	/**
	 * Checks for equality
	 * @param other the other object
	 * @return true if the objects are structural equivalent and false otherwise
	 */
	public boolean equals(Object other){
		if(!other.getClass().equals(this.getClass())){ // of different classes, they cannot be equivalent
			return false;
		}

		return Bisimulator.equivalent(this, (InlineType)other);
	}

	public boolean isSubtypeOf(Type other){
		return Bisimulator.isSubtypeOf(this, other);
	}

	public InlineType copy(){
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
		InlineType struct = new InlineType(this.basicType, this.cardinality, this.context, this.openRecord);
		seenTypes.put(this, struct);

		this.children.entrySet().forEach(child -> {
			String childName = child.getKey();
			Type childStruct = child.getValue();

			// run through the already seen types and see if we already copied this object, if so just use this copy
			for(Entry<Type, Type> seenType : seenTypes.entrySet()){
				if(seenType.getKey() == childStruct){
					struct.addChildUnsafe(childName, seenType.getValue());
					return; // return acts as continue, since we are using a stream
				}
			}

			// otherwise we must make a new copy
			struct.addChildUnsafe(childName, childStruct.copy(finalize, seenTypes));
		});
		
		return struct;
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

	public String prettyString(int level){
		return this.prettyString(level, new ArrayList<>());
	}

	public String prettyString(int level, ArrayList<Type> recursive){
		String result = "";
		
		// print the basic type
		if(this.basicType != null){
			result += this.basicType.nativeType().id();
		}
		else{
			result += "no type";
		}

		// print the children if any
		if(this.children.size() != 0){
			result += " {"; // open a bracket

			if(this.openRecord){
				result += "\n" + "\t".repeat(level+1) + "?";
			}

			for(Entry<String, Type> ent : this.children.entrySet()){
				String childName = ent.getKey();
				Type child = ent.getValue();

				if(childName == "?"){
					continue;
				}

				if(this.containsChildExact(recursive, child)){ // child have been printed before, it is recursive
					result += "\n" + "\t".repeat(level+1) + childName + " (recursive structure)";
				}
				else{ // not recursive
					recursive.add(child);
					ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice
					result += "\n" + "\t".repeat(level+1) + childName + ": " + child.prettyString(level+1, rec);
				}
			}

			result += "\n" + "\t".repeat(level) + "}"; // close the bracket again
		}
		else if(this.openRecord){
			result += " { ? }";
		}
		
		return result;
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
