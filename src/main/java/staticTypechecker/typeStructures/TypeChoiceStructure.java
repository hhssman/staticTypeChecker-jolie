package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
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
public class TypeChoiceStructure extends TypeStructure {
	private HashSet<TypeInlineStructure> choices;

	public TypeChoiceStructure(){
		this.choices = new HashSet<>();
	}

	public TypeChoiceStructure(ArrayList<TypeStructure> choices){
		this.choices = new HashSet<>();
		for(TypeStructure choice : choices){
			this.addChoice(choice);
		}
	}

	public TypeChoiceStructure(HashSet<TypeInlineStructure> choices){
		this.choices = choices;
	}

	public void addChoice(TypeInlineStructure choice){
		this.choices.add(choice);
	}

	public void addChoice(TypeChoiceStructure choice){
		for(TypeInlineStructure newChoice : choice.choices()){
			this.choices.add(newChoice);
		}
	}

	public void addChoice(TypeStructure choice){
		if(choice instanceof TypeInlineStructure){
			this.addChoice((TypeInlineStructure)choice);
		}
		else{
			this.addChoice((TypeChoiceStructure)choice);
		}
	}

	public ArrayList<TypeInlineStructure> choices(){
		return new ArrayList<>(this.choices);
	}

	public void setChoices(ArrayList<TypeInlineStructure> choices){
		this.choices = new HashSet<>(choices);
	}

	public static TypeChoiceStructure getBaseSymbol(TypeChoiceDefinition typeDef){
		return (TypeChoiceStructure)TypeConverter.createBaseStructure(typeDef);
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
	public ArrayList<TypeInlineStructure> choicesWithChild(String childName){
		ArrayList<TypeInlineStructure> ret = new ArrayList<>();

		for(TypeInlineStructure choice : this.choices){
			if(choice.contains(childName)){
				ret.add(choice);
			}
		}

		return ret;
	}

	/**
	 * TODO
	 */
	public boolean equals(TypeStructure other){
		if(!(other instanceof TypeChoiceStructure)){
			return false;
		}

		TypeChoiceStructure parsedOther = (TypeChoiceStructure)other;
		return this == parsedOther;
	}

	/**
	 * TODO
	 */
	public boolean isSubtypeOf(TypeStructure other){
		return Bisimulator.isSubtypeOf(this, other);
	}

	/**
	 * TODO
	 */
	public TypeStructure merge(TypeStructure other){
		return this;
	}

	public String toString(){
		String toString = this.choices.stream().map(c -> c.toString()).collect(Collectors.joining(" | "));
		return toString;
	}

	public TypeChoiceStructure copy(boolean finalize){
		TypeChoiceStructure copy = new TypeChoiceStructure();
		this.choices.forEach(c -> copy.addChoice(c.copy(finalize)));
		return copy;
	}

	public String prettyString(){
		ArrayList<TypeStructure> recursive = new ArrayList<>();
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyString(int level, ArrayList<TypeStructure> recursive){
		String toString = "\n" + "\t".repeat(level);
		toString += this.choices.stream().map(c -> {
			ArrayList<TypeStructure> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice
			return c.prettyString(level, rec);
		}).collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}
}
