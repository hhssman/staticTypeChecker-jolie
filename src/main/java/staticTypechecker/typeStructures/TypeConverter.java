package staticTypechecker.typeStructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Pair;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Symbol.SymbolType;

/**
 * A static converter for the existing Jolie types. Converts them to my custom types used in the static typechecking, namely InlineTypes and ChoiceTypes.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeConverter {
	/**
	 * Creates a structure instance representing the structure of the given type.
	 * @param type the type to create the structure from
	 * @return the structure instance representing the specified type
	 */
	public static Type convert(TypeDefinition type, SymbolTable symbols){
		return convert(type, new HashMap<>(), type.context(), symbols);
	}

	private static Type convert(TypeDefinition type, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		if(type instanceof TypeInlineDefinition){
			return TypeConverter.convert((TypeInlineDefinition)type, rec, ctx, symbols);
		}

		if(type instanceof TypeChoiceDefinition){
			return TypeConverter.convert((TypeChoiceDefinition)type, rec, ctx, symbols);
		}

		if(type instanceof TypeDefinitionLink){
			return TypeConverter.convert((TypeDefinitionLink)type, rec, ctx, symbols);
		}

		if(type instanceof TypeDefinitionUndefined){
			return TypeConverter.convert((TypeDefinitionUndefined)type, rec, ctx, symbols);
		}

		return null;
	}

	private static InlineType convert(TypeInlineDefinition type, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		if(rec.containsKey(type.name())){
			return (InlineType)rec.get(type.name());
		}

		InlineType result = new InlineType(type.basicType(), type.cardinality(), ctx, type.untypedSubTypes());
		
		if(symbols.containsKey(type.name())){ // it is a known type
			InlineType st = (InlineType)symbols.get(type.name());

			if(st == null){ // type has not been in initialized, we init it here
				symbols.put(type.name(), new Pair<SymbolType, Symbol>(SymbolType.TYPE, result));
			}
			else if(type.subTypes() != null && !st.children().isEmpty()){ // if the type has chilren and the children of the struct is not empty, then the type has been finalized, we use it directly
				return st;
			}
			else{ // otherwise the base type has been initialized but it has not been finalized, we finalize it here
				result = st;
				rec.put(type.name(), result);
			}
		}

		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String childName = child.getKey();
				Type subStructure = TypeConverter.convert(child.getValue(), rec, ctx, symbols);

				result.addChildUnsafe(childName, subStructure);
			}
		}

		return result;
	}

	private static Type convert(TypeChoiceDefinition type, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		HashSet<InlineType> choices = new HashSet<>();
		TypeConverter.getChoices(type, choices, rec, ctx, symbols);
		return new ChoiceType(choices);
	}

	private static Type convert(TypeDefinitionLink type, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		return TypeConverter.convert(type.linkedType(), rec, ctx, symbols);
	}

	private static Type convert(TypeDefinitionUndefined type, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		return null;
	}

	private static void getChoices(TypeDefinition type, HashSet<InlineType> list, HashMap<String, Type> rec, ParsingContext ctx, SymbolTable symbols){
		if(type instanceof TypeChoiceDefinition){
			TypeConverter.getChoices(((TypeChoiceDefinition)type).left(), list, new HashMap<>(rec), ctx, symbols);
			TypeConverter.getChoices(((TypeChoiceDefinition)type).right(), list, new HashMap<>(rec), ctx, symbols);
		}
		else if(type instanceof TypeInlineDefinition){
			TypeInlineDefinition parsed = (TypeInlineDefinition)type;
			TypeInlineDefinition copy = new TypeInlineDefinition(parsed.context(), "", parsed.basicType(), parsed.cardinality()); // copy to type without name, because the choices in TypeChoiceDefinition has the same name as the choice node itself.......
			list.add( TypeConverter.convert(copy, rec, ctx, symbols) );
		}
		else if(type instanceof TypeDefinitionLink){
			TypeConverter.getChoices(((TypeDefinitionLink)type).linkedType(), list, rec, ctx, symbols);
		}
		else{
			System.out.println("CONVERTION NOT SUPPORTED");
		}
	}
}
