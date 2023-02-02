package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;

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

	public TypeChoiceStructure(HashSet<TypeInlineStructure> choices){
		this.choices = choices;
	}

	public void addChoice(TypeInlineStructure choice){
		this.choices.add(choice);
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
		System.out.println("UPDATING BASIC TYPE OF ALL CHOICES");
		System.out.print("CHOICES BEFORE: ");
		for(TypeInlineStructure choice : this.choices){
			System.out.print(choice.basicType().nativeType().id() + ", ");
		}
		System.out.println();

		this.choices = this.choices.stream()
		.map(c -> {
			c.setBasicType(newType);
			return c;
		})
		.distinct()
		.collect(Collectors.toCollection(HashSet::new));

		System.out.print("CHOICES AFTER: ");
		for(TypeInlineStructure choice : this.choices){
			System.out.print(choice.basicType().nativeType().id() + ", ");
		}
		System.out.println();
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
		toString += this.choices.stream().map(c -> {
			HashMap<String, Void> rec = new HashMap<>(recursive); // shallow copy to not pass the same to each choice
			return c.prettyString(level, rec);
		}).collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}
}
