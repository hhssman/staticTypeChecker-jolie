package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.context.ParsingContext;
import staticTypechecker.utils.Bisimulator;
import staticTypechecker.utils.BisimulatorOld;

/**
 * A type structure representing a choice type. Choices are defined recursively as more TypeChoiceStructures
 * 
 * @author Kasper Bergstedt
 */
public class ChoiceType extends Type {
	private HashSet<InlineType> choices;
	private ParsingContext ctx = null;

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
		this.choices = choices;
	}

	public static ChoiceType fromBasicTypes(ArrayList<BasicTypeDefinition> typesOfChoices){
		HashSet<InlineType> choices = new HashSet<>();

		for(BasicTypeDefinition type : typesOfChoices){
			choices.add( new InlineType(type, null, null) );
		}

		return new ChoiceType(choices);
	}

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

	public void setChoicesUnsafe(ArrayList<InlineType> choices){
		this.choices = new HashSet<>(choices);
	}

	public void setContext(ParsingContext ctx){
		this.ctx = ctx;
	}

	public ParsingContext context(){
		return this.ctx;
	}

	public ChoiceType addChoice(Type choice){
		ChoiceType copy = (ChoiceType)this.copy();
		copy.addChoiceUnsafe(choice);
		return copy;
	}

	public ArrayList<InlineType> choices(){
		return new ArrayList<>(this.choices);
	}

	public ChoiceType put(String childName, Type structure){
		ChoiceType copy = (ChoiceType)this.copy();

		for(InlineType choice : copy.choices){
			choice.addChildUnsafe(childName, structure);
		}
		
		return copy;
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
	 * TODO
	 */
	public boolean equals(Object other){
		if(!other.getClass().equals(this.getClass())){ // of different classes, they cannot be equivalent
			return false;
		}

		return Bisimulator.equivalent(this, (ChoiceType)other);
	}

	/**
	 * TODO
	 */
	public boolean isSubtypeOf(Type other){
		return BisimulatorOld.isSubtypeOf(this, other);
	}

	public String toString(){
		String toString = this.choices.stream().map(c -> c.toString()).collect(Collectors.joining(" | "));
		return toString;
	}

	public ChoiceType copy(){
		return this.copy(false);
	}

	public ChoiceType copy(boolean finalize){
		ChoiceType copy = new ChoiceType();
		copy.setContext(this.ctx);
		
		for(InlineType choice : this.choices){
			copy.addChoiceUnsafe(choice.copy(finalize));
		}

		return copy;
	}

	public ChoiceType copy(boolean finalize, HashMap<Type, Type> seenTypes){
		ChoiceType copy = new ChoiceType();
		copy.setContext(this.ctx);

		for(InlineType choice : this.choices){
			copy.addChoiceUnsafe(choice.copy(finalize, seenTypes));
		}

		return copy;
	}

	public String prettyString(){
		ArrayList<Type> recursive = new ArrayList<>();
		String toString = this.choices.stream()
										.map(c -> c.prettyString(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyString(int level){
		return this.prettyString(level, new ArrayList<>());
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
										.map(c -> c.prettyStringHashCode(0, recursive))
										.collect(Collectors.joining("\n|\n"));

		return toString;
	}

	public String prettyStringHashCode(int level, ArrayList<Type> recursive){
		String toString = "\n" + "\t".repeat(level);
		toString += this.choices.stream().map(c -> {
			ArrayList<Type> rec = new ArrayList<>(recursive); // shallow copy to not pass the same to each choice
			return c.prettyStringHashCode(level, rec);
		}).collect(Collectors.joining("\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level)));

		return toString;
	}
}
