package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;

/**
 * A type structure representing a choice type. Choices are defined recursively as more TypeChoiceStructures
 * 
 * @author Kasper Bergstedt
 */
public class TypeChoiceStructure extends TypeStructure {
	private ArrayList<TypeStructure> choices;

	public TypeChoiceStructure(){
		this.choices = new ArrayList<>();
	}

	public TypeChoiceStructure(ArrayList<TypeStructure> choices){
		this.choices = choices;
	}

	public void addChoice(TypeStructure choice){
		this.choices.add(choice);
	}

	public ArrayList<TypeStructure> choices(){
		return this.choices;
	}

	public void setChoices(ArrayList<TypeStructure> choices){
		this.choices = choices;
	}

	public static TypeChoiceStructure getBaseSymbol(TypeChoiceDefinition typeDef){
		return (TypeChoiceStructure)TypeConverter.createBaseStructure(typeDef);
	}

	public String toString(){
		String toString = this.choices.stream().map(c -> c.toString()).collect(Collectors.joining(" | "));
		return toString;
	}

	public TypeChoiceStructure copy(boolean finalize){
		TypeChoiceStructure copy = new TypeChoiceStructure();
		this.choices.forEach(c -> copy.addChoice(c));
		return copy;
	}

	public String prettyString(){
		HashMap<String, Void> recursive = new HashMap<>();
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyString(int level, HashMap<String, Void> recursive){
		String toString = this.choices.stream()
										.map(c -> c.prettyString(level, recursive))
										.collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}
}
