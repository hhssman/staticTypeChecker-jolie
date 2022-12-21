package staticTypechecker;

import java.util.AbstractMap;
import java.util.HashMap;
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
	public static TypeStructure convert(TypeDefinition type, HashMap<String, TypeStructure> recursiveTable){
		if(type instanceof TypeInlineDefinition){
			return TypeConverter.convert((TypeInlineDefinition)type, recursiveTable);
		}

		if(type instanceof TypeChoiceDefinition){
			return TypeConverter.convert((TypeChoiceDefinition)type, recursiveTable);
		}

		if(type instanceof TypeDefinitionLink){
			return TypeConverter.convert((TypeDefinitionLink)type, recursiveTable);
		}

		if(type instanceof TypeDefinitionUndefined){
			return TypeConverter.convert((TypeDefinitionUndefined)type, recursiveTable);
		}

		return null;
	}

	private static TypeInlineStructure convert(TypeInlineDefinition type, HashMap<String, TypeStructure> recursiveTable){
		TypeInlineStructure structure = new TypeInlineStructure(type.basicType(), type.cardinality(), type.context());

		recursiveTable.put(type.name(), structure);

		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> child : type.subTypes()){
				String childName = child.getKey();
				String typeName = "";

				if(child.getValue() instanceof TypeDefinitionLink){
					TypeDefinitionLink subtype = (TypeDefinitionLink)child.getValue();
					typeName = subtype.linkedTypeName();
				}

				if(recursiveTable.containsKey(typeName)){
					structure.addChild(childName, recursiveTable.get(typeName));
				}
				else{
					TypeStructure subStructure = TypeConverter.convert(child.getValue(), recursiveTable);
					structure.addChild(childName, subStructure);
				}
			}
		}

		structure.finalize();

		return structure;
	}

	private static TypeStructure convert(TypeChoiceDefinition type, HashMap<String, TypeStructure> recursiveTable){
		TypeStructure left = TypeConverter.convert(type.left(), recursiveTable);
		TypeStructure right = TypeConverter.convert(type.right(), recursiveTable);

		return new TypeChoiceStructure(left, right);
	}

	private static TypeStructure convert(TypeDefinitionLink type, HashMap<String, TypeStructure> recursiveTable){
		return TypeConverter.convert(type.linkedType(), recursiveTable);
	}

	private static TypeStructure convert(TypeDefinitionUndefined type, HashMap<String, TypeStructure> recursiveTable){
		return null;
	}
}
