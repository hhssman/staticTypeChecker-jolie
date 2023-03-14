// package staticTypechecker.typeStructures;

// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.Map.Entry;

// import jolie.lang.parse.ast.types.TypeChoiceDefinition;
// import jolie.lang.parse.ast.types.TypeDefinition;
// import jolie.lang.parse.ast.types.TypeDefinitionLink;
// import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
// import jolie.lang.parse.ast.types.TypeInlineDefinition;
// import staticTypechecker.entities.SymbolTable;

// /**
//  * A static converter for the existing Jolie types. Converts them to my custom type used in the static typechecking, namely InlineTypes and ChoiceTypes.
//  * 
//  * @author Kasper Bergstedt (kberg18@student.sdu.dk)
//  */
// public class TypeConverter_OG {

// 	/**
// 	 * Finalizes the given structure using the given type. 
// 	 * @param structure the structure object to finalize
// 	 * @param type the type definition to use when finalizing the struture
// 	 */
// 	public static void finalizeBaseStructure(Type struct, TypeDefinition type, SymbolTable symbols){
// 		// case where struct is a standard type definition
// 		if(struct instanceof InlineType){
// 			InlineType castedStruct = (InlineType)struct;

// 			if(type instanceof TypeInlineDefinition){ // structure and definition are compatible, finalize the object
// 				TypeInlineDefinition castedType = (TypeInlineDefinition)type;
// 				HashMap<String, Type> recursiveTable = new HashMap<>();
// 				recursiveTable.put(type.name(), castedStruct);

// 				castedStruct.setBasicTypeUnsafe(castedType.basicType());
// 				castedStruct.setCardinalityUnsafe(castedType.cardinality());
// 				castedStruct.setContextUnsafe(castedType.context());
// 				castedStruct.setOpenStatusUnsafe(castedType.untypedSubTypes());

// 				TypeConverter_OG.addChildrenToBase(castedStruct, castedType, symbols); 
// 			}
// 			else if(type instanceof TypeDefinitionLink){ // an alias, finalize the linked type definition
// 				TypeConverter_OG.finalizeBaseStructure(castedStruct, ((TypeDefinitionLink)type).linkedType(), symbols);
// 			}
// 			else{ // struct and type def are incompatible, maybe throw error here TODO
// 				System.out.println("Incompatible struct and types");
// 				return;
// 			}

// 			return;
// 		}

// 		// case where struct is a choice type definition
// 		if(struct instanceof ChoiceType){
// 			ChoiceType castedStruct = (ChoiceType)struct;
			
// 			if(type instanceof TypeChoiceDefinition){ // struct and type def match
// 				TypeChoiceDefinition castedType = (TypeChoiceDefinition)type;
// 				ChoiceType tmpStruct = (ChoiceType)TypeConverter_OG.convert(castedType, symbols);

// 				castedStruct.setChoicesUnsafe(tmpStruct.choices());
// 			}
// 			else if(type instanceof TypeDefinitionLink){ // an alias, finalize the linked type def
// 				TypeConverter_OG.finalizeBaseStructure(castedStruct, ((TypeDefinitionLink)type).linkedType(), symbols);
// 			}
// 			else{ // struct and type def are incompatible, maybe throw error here TODO
// 				System.out.println("Incompatible struct and types");
// 				return;
// 			}
// 		}
// 	}

// 	public static Type convertNoFinalize(TypeDefinition type, SymbolTable symbols){
// 		return TypeConverter_OG.convert(type, symbols);
// 	}

// 	/**
// 	 * Creates a structure instance representing the structure of the given type.
// 	 * @param type the type to create the structure from
// 	 * @return the structure instance representing the specified type
// 	 */
// 	private static Type convert(TypeDefinition type, SymbolTable symbols){
// 		if(type instanceof TypeInlineDefinition){
// 			TypeInlineDefinition parsedType = (TypeInlineDefinition)type;
// 			InlineType base = new InlineType(parsedType.basicType(), parsedType.cardinality(), parsedType.context(), parsedType.untypedSubTypes());
// 			TypeConverter_OG.addChildrenToBase(base, (TypeInlineDefinition)type, symbols);
// 			return base;
// 		}

// 		if(type instanceof TypeChoiceDefinition){
// 			return TypeConverter_OG.convert((TypeChoiceDefinition)type, symbols);
// 		}

// 		if(type instanceof TypeDefinitionLink){
// 			return TypeConverter_OG.convert((TypeDefinitionLink)type, symbols);
// 		}

// 		if(type instanceof TypeDefinitionUndefined){
// 			return TypeConverter_OG.convert((TypeDefinitionUndefined)type, symbols);
// 		}

// 		return null;
// 	}

// 	private static InlineType convert(TypeInlineDefinition type, SymbolTable symbols){
// 		InlineType base = new InlineType(type.basicType(), type.cardinality(), type.context(), type.untypedSubTypes());
// 		TypeConverter_OG.addChildrenToBase(base, type, symbols);
// 		return base;
// 	}

// 	private static void addChildrenToBase(InlineType base, TypeInlineDefinition type, SymbolTable symbols){
// 		if(type.subTypes() != null){ // type has children
// 			for(Entry<String, TypeDefinition> child : type.subTypes()){
// 				String childName = child.getKey();
// 				String typeName = "";
				
// 				if(child.getValue() instanceof TypeDefinitionLink){ // subtype is an alias for an existing type. In this case, we look for the linked type name instead of the alias
// 					TypeDefinitionLink subtype = (TypeDefinitionLink)child.getValue();
// 					typeName = subtype.linkedTypeName();
// 				}

// 				if(symbols.containsKey(typeName)){
// 					base.addChildUnsafe(childName, (Type)symbols.get(typeName));
// 				}
// 				else{
// 					Type subStructure = TypeConverter_OG.convert(child.getValue(), symbols);
// 					base.addChildUnsafe(childName, subStructure);
// 				}
// 			}
// 		}
// 	}

// 	private static Type convert(TypeChoiceDefinition type, SymbolTable symbols){
// 		HashSet<InlineType> choices = new HashSet<>();
// 		TypeConverter_OG.getChoices(type, choices, symbols);
// 		return new ChoiceType(choices);
// 	}

// 	private static void getChoices(TypeDefinition type, HashSet<InlineType> list, SymbolTable symbols){
// 		if(type instanceof TypeChoiceDefinition){
// 			TypeConverter_OG.getChoices(((TypeChoiceDefinition)type).left(), list, symbols);
// 			TypeConverter_OG.getChoices(((TypeChoiceDefinition)type).right(), list, symbols);
// 		}
// 		else if(type instanceof TypeInlineDefinition){
// 			list.add( TypeConverter_OG.convert((TypeInlineDefinition)type, symbols) );
// 		}
// 		else if(type instanceof TypeDefinitionLink){
// 			TypeConverter_OG.getChoices(((TypeDefinitionLink)type).linkedType(), list, symbols);
// 		}
// 		else{
// 			System.out.println("CONVERTION NOT SUPPORTED");
// 		}
// 	}

// 	private static Type convert(TypeDefinitionLink type, SymbolTable symbols){
// 		return TypeConverter_OG.convert(type.linkedType(), symbols);
// 	}

// 	private static Type convert(TypeDefinitionUndefined type, SymbolTable symbols){
// 		return null;
// 	}
// }
