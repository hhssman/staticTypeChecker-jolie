package staticTypechecker.typeStructures;

import java.util.HashSet;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
import jolie.lang.parse.ast.types.TypeInlineDefinition;

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
		if(type instanceof TypeInlineDefinition){
			return TypeConverter.convert((TypeInlineDefinition)type);
		}

		if(type instanceof TypeChoiceDefinition){
			return TypeConverter.convert((TypeChoiceDefinition)type);
		}

		if(type instanceof TypeDefinitionLink){
			return TypeConverter.convert((TypeDefinitionLink)type);
		}

		if(type instanceof TypeDefinitionUndefined){
			return TypeConverter.convert((TypeDefinitionUndefined)type);
		}

		return null;
	}

	private static InlineType convert(TypeInlineDefinition type){
		InlineType result = new InlineType(type.basicType(), type.cardinality(), type.context(), type.untypedSubTypes());
		
		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String childName = child.getKey();
				Type subStructure = TypeConverter.convert(child.getValue());

				result.addChildUnsafe(childName, subStructure);
			}
		}

		// TODO, maybe not needed anyway, if I just pass an ANY node when asking for ? child
		// if(type.untypedSubTypes()){ // it is an open record, add the node for this
		// 	result.addChildUnsafe("?", Type.OPEN_RECORD);
		// }

		return result;
	}

	private static Type convert(TypeChoiceDefinition type){
		HashSet<InlineType> choices = new HashSet<>();
		TypeConverter.getChoices(type, choices);
		return new ChoiceType(choices);
	}

	private static void getChoices(TypeDefinition type, HashSet<InlineType> list){
		if(type instanceof TypeChoiceDefinition){
			TypeConverter.getChoices(((TypeChoiceDefinition)type).left(), list);
			TypeConverter.getChoices(((TypeChoiceDefinition)type).right(), list);
		}
		else if(type instanceof TypeInlineDefinition){
			list.add( TypeConverter.convert((TypeInlineDefinition)type) );
		}
		else if(type instanceof TypeDefinitionLink){
			TypeConverter.getChoices(((TypeDefinitionLink)type).linkedType(), list);
		}
		else{
			System.out.println("CONVERTION NOT SUPPORTED");
		}
	}

	private static Type convert(TypeDefinitionLink type){
		return TypeConverter.convert(type.linkedType());
	}

	private static Type convert(TypeDefinitionUndefined type){
		return null;
	}
}
