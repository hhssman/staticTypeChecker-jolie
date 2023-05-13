package staticTypechecker.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.context.ParsingContext;
import staticTypechecker.utils.Simulator;

/**
 * Represents a choice type in Jolie, such as "type a: int | string", i.e. types which can have multiple values.
 * 
 * @author Kasper Bergstedt
 */
public class ChoiceType extends Type {
	private HashSet<InlineType> choices;	// the choices of this type
	private ParsingContext ctx = null;		// the parsing context of this type

	public ChoiceType(){
		this.choices = new HashSet<>();
	}

	public ChoiceType(ArrayList<Type> choices){
		this.choices = new HashSet<>();

		for(Type choice : choices){
			this.addChoiceUnsafe(choice);
		}
	}

	public ChoiceType(HashSet<InlineType> choices){
		this.choices = new HashSet<>(choices);
	}

	/**
	 * Creates a new choice type with childless inline types of the given basic types
	 * @param typesOfChoices the basic types to create the choices from
	 * @return the new choice type
	 */
	public static ChoiceType fromBasicTypes(List<BasicTypeDefinition> typesOfChoices){
		HashSet<InlineType> choices = new HashSet<>();

		for(BasicTypeDefinition type : typesOfChoices){
			choices.add( new InlineType(type, null, null, false) );
		}

		return new ChoiceType(choices);
	}

	/**
	 * Adds a new choice to this ChoiceType object. WARNING: alters the object
	 * @param choice the choice to add
	 */
	public void addChoiceUnsafe(Type choice){
		if(choice instanceof InlineType){
			this.choices.add((InlineType)choice);
		}
		else{
			for(InlineType newChoice : ((ChoiceType)choice).choices()){
				this.choices.add(newChoice);
			}
		}
	}

	/**
	 * Removes the given choice from this ChoiceType object if it is present, else does nothing. WARNING: may alter the object
	 * @param choice the choice to remove
	 */
	public void removeChoiceUnsafe(Type choice){
		if(choice instanceof InlineType){
			this.choices.remove(choice);
		}
		else{
			for(InlineType c : ((ChoiceType)choice).choices()){
				this.choices.remove(c);
			}
		}
	}

	/**
	 * Overrides the choices of this ChoiceType object with the choices given. WARNING: alters the object
	 * @param choices the choices to override with
	 */
	public void setChoicesUnsafe(Set<InlineType> choices){
		this.choices = new HashSet<>(choices);
	}

	public void setContext(ParsingContext ctx){
		this.ctx = ctx;
	}

	public ParsingContext context(){
		return this.ctx;
	}

	public ChoiceType addChoice(Type choice){
		ChoiceType copy = (ChoiceType)this.shallowCopy();
		copy.addChoiceUnsafe(choice);
		return copy;
	}

	public ArrayList<InlineType> choices(){
		return new ArrayList<>(this.choices);
	}

	public ChoiceType updateBasicTypeOfChoices(BasicTypeDefinition newType){
		ChoiceType copy = new ChoiceType();
		copy.setContext(this.ctx);

		for(InlineType choice : this.choices){
			copy.addChoiceUnsafe(choice.setBasicType(newType));
		}

		return copy;
	}

	/**
	 * Retrieves the choices of this structure which have a child with the provided name
	 * @param childName the name of the child to look for
	 * @return an ArrayList of TypeInlineStructures, all which have a child with the provided name
	 */
	public ArrayList<InlineType> choicesWithChild(String childName){
		ArrayList<InlineType> ret = new ArrayList<>();

		for(InlineType choice : this.choices){
			if(choice.contains(childName)){
				ret.add(choice);
			}
		}

		return ret;
	}

	/**
	 * Retrieves the children with the given name in all choices of this choice type
	 * @param childName the name to search for
	 * @return an arraylist of the children
	 */
	public ChoiceType getChild(String childName){
		ChoiceType ret = new ChoiceType();

		for(InlineType choice : this.choices){
			Type child = choice.getChild(childName);
			if(child != null){
				ret.addChoiceUnsafe(child);
			}
		}

		return ret;
	}

	/**
	 * @return the only possibility for this choice type, if it only contains one choice, else this object
	 */
	public Type convertIfPossible(){
		if(this.choices.size() == 1){
			return (InlineType)this.choices.toArray()[0];
		}
		
		return this;
	}

	public void clear(){
		this.choices.clear();
	}

	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		
		if(!other.getClass().equals(this.getClass())){ // of different classes, they cannot be equivalent
			return false;
		}

		return Simulator.equivalent(this, (ChoiceType)other);
	}

	public boolean isSubtypeOf(Type other){
		return Simulator.isSubtypeOf(this, other);
	}

	public ChoiceType shallowCopy(){
		return new ChoiceType(this.choices);
	}

	public Type shallowCopyExcept(Path p){
		if(p.isEmpty()){
			return this.shallowCopy();
		}

		ChoiceType result = new ChoiceType();
		for(InlineType choice : this.choices){
			result.addChoiceUnsafe(choice.shallowCopyExcept(p));
		}
		return result;
	}

	public ChoiceType copy(){
		ChoiceType copy = new ChoiceType();
		copy.setContext(this.ctx);

		IdentityHashMap<Type, Type> seenTypes = new IdentityHashMap<>();
		seenTypes.put(this, copy);
		
		for(InlineType choice : this.choices){
			copy.addChoiceUnsafe(choice.copy(seenTypes));
		}

		return copy;
	}

	public ChoiceType copy(IdentityHashMap<Type, Type> seenTypes){
		ChoiceType copy = new ChoiceType();
		copy.setContext(this.ctx);

		seenTypes.put(this, copy);

		for(InlineType choice : this.choices){
			copy.addChoiceUnsafe(choice.copy(seenTypes));
		}

		return copy;
	}

	public String prettyString(){
		IdentityHashMap<Type, Void> recursive = new IdentityHashMap<>();
		recursive.put(this, null);
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyString(int level){
		return this.prettyString(level, new IdentityHashMap<>());
	}

	public String prettyString(int level, IdentityHashMap<Type, Void> recursive){
		recursive.put(this, null);
		
		int newLevel = level + 1;
		
		// String toString = " " + System.identityHashCode(this) + "\n" + "\t".repeat(newLevel);
		String toString = "\n" + "\t".repeat(newLevel);
		toString += this.choices.stream()
			.map(c -> {
				if(recursive.containsKey(c)){
					// return "recursive edge to (" + System.identityHashCode(c) + ")";
					return "recursive edge";
				}
				IdentityHashMap<Type, Void> rec = new IdentityHashMap<>(recursive); // shallow copy to not pass the same to each choice
				return c.prettyString(newLevel, rec);
			})
			.collect(Collectors.joining("\n" + "\t".repeat(newLevel) + "|" + "\n" + "\t".repeat(newLevel)));

		return toString;
	}
}
