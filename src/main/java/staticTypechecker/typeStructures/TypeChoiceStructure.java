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
	private ArrayList<TypeInlineStructure> choices;

	public TypeChoiceStructure(){
		this.choices = new ArrayList<>();
	}

	public TypeChoiceStructure(ArrayList<TypeInlineStructure> choices){
		this.choices = choices;
	}

	public void addChoice(TypeInlineStructure choice){
		this.choices.add(choice);
	}

	public ArrayList<TypeInlineStructure> choices(){
		return this.choices;
	}

	public void setChoices(ArrayList<TypeInlineStructure> choices){
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
		this.choices.forEach(c -> copy.addChoice(c.copy(finalize)));
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
		String toString = "";

		for(int i = 0; i < this.choices.size()-1; i++){
			HashMap<String, Void> rec = new HashMap<>(recursive); // shallow copy to not pass the same to each choice
			toString += this.choices.get(i).prettyString(level, rec);
			toString += "\n" + "\t".repeat(level) + "|";
		}

		HashMap<String, Void> rec = new HashMap<>(recursive); // shallow copy to not pass the same to each choice
		toString += this.choices.get(this.choices.size()-1).prettyString(level, rec);

		return toString;
	}
}
