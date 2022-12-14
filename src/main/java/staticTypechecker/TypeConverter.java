package staticTypechecker;

import java.util.AbstractMap;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
import jolie.lang.parse.ast.types.TypeInlineDefinition;

/**
 * A static converter for the existing Jolie types. Converts them to my custom type used in the static typechecking, namely TypeNameDefinition and TypeStructureDefinition.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeConverter {
	// VERSION USING TYPENAMEDEFINITIONS INSTEAD OF STRINGS
	// public static Entry<TypeNameDefinition, TypeStructureDefinition> convert(TypeInlineDefinition type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructureDefinition> convert(TypeChoiceDefinition type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructureDefinition> convert(TypeDefinitionLink type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructureDefinition> convert(TypeDefinitionUndefined type){
	// 	return null;
	// }


	// VERSION USING STRINGS
	public static TypeStructureDefinition convert(TypeDefinition type){
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

	private static TypeStructureDefinition convert(TypeInlineDefinition type){
		TypeStructureDefinition structure = new TypeStructureDefinition(type.basicType(), type.cardinality(), type.context());

		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String name = child.getKey();
				TypeStructureDefinition subStructure = TypeConverter.convert(child.getValue());

				structure.addChild(name, subStructure);
			}
		}

		structure.finalize();

		return structure;
	}

	private static TypeStructureDefinition convert(TypeChoiceDefinition type){
		return null;
	}

	private static TypeStructureDefinition convert(TypeDefinitionLink type){
		return TypeConverter.convert(type.linkedType());
	}

	private static TypeStructureDefinition convert(TypeDefinitionUndefined type){
		return null;
	}
}
