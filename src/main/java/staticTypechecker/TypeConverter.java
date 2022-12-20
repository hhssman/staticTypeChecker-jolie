package staticTypechecker;

import java.util.AbstractMap;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;

/**
 * A static converter for the existing Jolie types. Converts them to my custom type used in the static typechecking, namely TypeNameDefinition and TypeStructure.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeConverter {
	// VERSION USING TYPENAMEDEFINITIONS INSTEAD OF STRINGS
	// public static Entry<TypeNameDefinition, TypeStructure> convert(TypeInlineDefinition type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructure> convert(TypeChoiceDefinition type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructure> convert(TypeDefinitionLink type){
	// 	return null;
	// }

	// public static Entry<TypeNameDefinition, TypeStructure> convert(TypeDefinitionUndefined type){
	// 	return null;
	// }


	// VERSION USING STRINGS
	public static TypeStructure convert(TypeDefinition type, SymbolTable symbols){
		if(type instanceof TypeInlineDefinition){
			return TypeConverter.convert((TypeInlineDefinition)type, symbols);
		}

		if(type instanceof TypeChoiceDefinition){
			return TypeConverter.convert((TypeChoiceDefinition)type, symbols);
		}

		if(type instanceof TypeDefinitionLink){
			return TypeConverter.convert((TypeDefinitionLink)type, symbols);
		}

		if(type instanceof TypeDefinitionUndefined){
			return TypeConverter.convert((TypeDefinitionUndefined)type, symbols);
		}

		return null;
	}

	private static TypeInlineStructure convert(TypeInlineDefinition type, SymbolTable symbols){
		TypeInlineStructure structure = new TypeInlineStructure(type.basicType(), type.cardinality(), type.context());

		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String name = child.getKey();
				TypeStructure subStructure = TypeConverter.convert(child.getValue(), symbols);

				structure.addChild(name, subStructure);
			}
		}

		structure.finalize();

		return structure;
	}

	private static TypeStructure convert(TypeChoiceDefinition type, SymbolTable symbols){
		TypeStructure left = TypeConverter.convert(type.left(), symbols);
		TypeStructure right = TypeConverter.convert(type.right(), symbols);

		return new TypeChoiceStructure(left, right);
	}

	private static TypeStructure convert(TypeDefinitionLink type, SymbolTable symbols){
		return TypeConverter.convert(type.linkedType(), symbols);
	}

	private static TypeStructure convert(TypeDefinitionUndefined type, SymbolTable symbols){
		return null;
	}
}
