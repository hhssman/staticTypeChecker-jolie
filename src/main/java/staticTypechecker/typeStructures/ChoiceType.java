package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import staticTypechecker.utils.Bisimulator;

/**
 * A type structure representing a choice type. Choices are defined recursively as more TypeChoiceStructures
 * 
 * @author Kasper Bergstedt
 */
public class ChoiceType extends Type {
	private HashSet<InlineType> choices;

	public ChoiceType(){
		this.choices = new HashSet<>();
	}

	public ChoiceType(ArrayList<Type> choices){
		this.choices = new HashSet<>();
		for(Type choice : choices){
			this.addChoice(choice);
		}
	}

	public ChoiceType(HashSet<InlineType> choices){
		this.choices = choices;
	}

	public void addChoice(InlineType choice){
		this.choices.add(choice);
	}

	public void addChoice(ChoiceType choice){
		for(InlineType newChoice : choice.choices()){
			this.choices.add(newChoice);
		}
	}

	public void addChoice(Type choice){
		if(choice instanceof InlineType){
			this.addChoice((InlineType)choice);
		}
		else{
			this.addChoice((ChoiceType)choice);
		}
	}

	public ArrayList<InlineType> choices(){
		return new ArrayList<>(this.choices);
	}

	public void setChoices(ArrayList<InlineType> choices){
		this.choices = new HashSet<>(choices);
	}

	public static ChoiceType getBaseSymbol(TypeChoiceDefinition typeDef){
		return (ChoiceType)TypeConverter.createBaseStructure(typeDef);
	}

	public static ChoiceType getBaseSymbol(){
		return new ChoiceType();
	}

	public void updateBasicTypeOfChoices(BasicTypeDefinition newType){
		this.choices = this.choices.stream()
		.map(c -> {
			c.setBasicType(newType);
			return c;
		})
		.distinct()
		.collect(Collectors.toCollection(HashSet::new));
	}

	public void removeDuplicates(){
		System.out.println("choice before: " + this.choices);
		this.choices = this.choices.stream().distinct().collect(Collectors.toCollection(HashSet::new));
		System.out.println("choice after: " + this.choices);
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
	 * TODO
	 */
	public boolean equals(Object other){
		if(!(other instanceof ChoiceType)){
			return false;
		}

		return Bisimulator.isEquivalent(this, (ChoiceType)other);
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

	public String toString(){
		String toString = this.choices.stream().map(c -> c.toString()).collect(Collectors.joining(" | "));
		return toString;
	}

	public ChoiceType copy(boolean finalize){
		ChoiceType copy = new ChoiceType();
		this.choices.forEach(c -> copy.addChoice(c.copy(finalize)));
		return copy;
	}

	public ChoiceType copy(boolean finalize, ArrayList<Type> seenTypes){
		ChoiceType copy = new ChoiceType();
		this.choices.forEach(c -> copy.addChoice(c.copy(finalize, seenTypes)));
		return copy;
	}

	public String prettyString(){
		ArrayList<Type> recursive = new ArrayList<>();
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyString(int level, ArrayList<Type> recursive){
		String toString = "\n" + "\t".repeat(level);
		toString += this.choices.stream().map(c -> {
			ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice
			return c.prettyString(level, rec);
		}).collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}


	public String prettyStringHashCode(){
		ArrayList<Type> recursive = new ArrayList<>();
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyStringHashCode(int level, ArrayList<Type> recursive){
		String toString = "\n" + "\t".repeat(level);
		toString += this.choices.stream().map(c -> {
			ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice
			return c.prettyString(level, rec);
		}).collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}
}
