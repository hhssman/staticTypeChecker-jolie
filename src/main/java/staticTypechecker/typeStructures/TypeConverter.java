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

/**
 * A static converter for the existing Jolie types. Converts them to my custom type used in the static typechecking, namely InlineTypes and ChoiceTypes.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeConverter {
	/**
	 * Creates a structure instance representing the structure of the given type.
	 * @param type the type to create the structure from
	 * @return the structure instance representing the specified type
	 */
	public static Type convert(TypeDefinition type){
		return convert(type, new HashMap<>(), type.context());
	}

	private static Type convert(TypeDefinition type, HashMap<String, Type> rec, ParsingContext ctx){
		if(type instanceof TypeInlineDefinition){
			return TypeConverter.convert((TypeInlineDefinition)type, rec, ctx);
		}

		if(type instanceof TypeChoiceDefinition){
			return TypeConverter.convert((TypeChoiceDefinition)type, rec, ctx);
		}

		if(type instanceof TypeDefinitionLink){
			return TypeConverter.convert((TypeDefinitionLink)type, rec, ctx);
		}

		if(type instanceof TypeDefinitionUndefined){
			return TypeConverter.convert((TypeDefinitionUndefined)type, rec, ctx);
		}

		return null;
	}

	private static InlineType convert(TypeInlineDefinition type, HashMap<String, Type> rec, ParsingContext ctx){
		InlineType result = new InlineType(type.basicType(), type.cardinality(), ctx, type.untypedSubTypes());

		if(rec.containsKey(type.name())){
			return (InlineType)rec.get(type.name());
		}

		rec.put(type.name(), result);
		
		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String childName = child.getKey();
				Type subStructure = TypeConverter.convert(child.getValue(), rec, ctx);

				result.addChildUnsafe(childName, subStructure);
			}
		}

		return result;
	}

	private static Type convert(TypeChoiceDefinition type, HashMap<String, Type> rec, ParsingContext ctx){
		HashSet<InlineType> choices = new HashSet<>();
		TypeConverter.getChoices(type, choices, rec, ctx);
		return new ChoiceType(choices);
	}

	private static void getChoices(TypeDefinition type, HashSet<InlineType> list, HashMap<String, Type> rec, ParsingContext ctx){
		if(type instanceof TypeChoiceDefinition){
			TypeConverter.getChoices(((TypeChoiceDefinition)type).left(), list, new HashMap<>(rec), ctx);
			TypeConverter.getChoices(((TypeChoiceDefinition)type).right(), list, new HashMap<>(rec), ctx);
		}
		else if(type instanceof TypeInlineDefinition){
			list.add( TypeConverter.convert((TypeInlineDefinition)type, rec, ctx) );
		}
		else if(type instanceof TypeDefinitionLink){
			TypeConverter.getChoices(((TypeDefinitionLink)type).linkedType(), list, rec, ctx);
		}
		else{
			System.out.println("CONVERTION NOT SUPPORTED");
		}
	}

	private static Type convert(TypeDefinitionLink type, HashMap<String, Type> rec, ParsingContext ctx){
		return TypeConverter.convert(type.linkedType(), rec, ctx);
	}

	private static Type convert(TypeDefinitionUndefined type, HashMap<String, Type> rec, ParsingContext ctx){
		return null;
	}
}
