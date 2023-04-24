package staticTypechecker.utils;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeDefinitionUndefined;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Pair;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Type;
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
		return convert(type, new IdentityHashMap<>(), type.context(), symbols);
	}

	private static Type convert(TypeDefinition type, IdentityHashMap<TypeDefinition, Type> rec, ParsingContext ctx, SymbolTable symbols){
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

	private static InlineType convert(TypeInlineDefinition type, IdentityHashMap<TypeDefinition, Type> rec, ParsingContext ctx, SymbolTable symbols){
		if(rec.containsKey(type)){
			return (InlineType)rec.get(type);
		}

		InlineType result = new InlineType(type.basicType(), type.cardinality(), ctx, type.untypedSubTypes());

		if(symbols.containsKey(type.name())){ // it is a known type
			if(symbols.get(type.name()) == null){ // type has not been in initialized, we init it here
				symbols.put(type.name(), new Pair<SymbolType, Symbol>(SymbolType.TYPE, result));
			}
			else if(symbols.get(type.name()) instanceof InlineType){ // check that the symbol is of the right type, it may be a choice type, if this node's parent is a choice, this node will have the same name as the parent, and thus the symbol table will hold the parent under this name
				InlineType existingType = (InlineType)symbols.get(type.name());

				if(type.subTypes() != null && !existingType.children().isEmpty()){ // if the type has chilren and the children of the struct is not empty, then the type has been finalized, we use it directly
					return existingType;
				}
				else{ // otherwise the base type has been initialized but it has not been finalized, we finalize it in this method call then
					result = existingType;
				}
			}
		}
		rec.put(type, result);

		if(type.subTypes() != null){ // type has children
			for(Entry<String, TypeDefinition> ent : type.subTypes()){
				String childName = ent.getKey();
				Type child = TypeConverter.convert(ent.getValue(), rec, ctx, symbols);

				result.addChildUnsafe(childName, child);
			}
		}

		return result;
	}

	private static ChoiceType convert(TypeChoiceDefinition type, IdentityHashMap<TypeDefinition, Type> rec, ParsingContext ctx, SymbolTable symbols){
		// System.out.println("choice in rec? " + rec.containsKey(type));
		if(rec.containsKey(type)){
			return (ChoiceType)rec.get(type);
		}

		ChoiceType result = new ChoiceType();
		rec.put(type, result);

		if(symbols.containsKey(type.name())){ // it is a known type
			ChoiceType st = (ChoiceType)symbols.get(type.name());
			
			if(st == null){ // type has not been in initialized, we init it here
				symbols.put(type.name(), new Pair<SymbolType, Symbol>(SymbolType.TYPE, result));
			}
			else{ // otherwise the base type has been initialized but it has not been finalized, we finalize it in this method call then
				return st;
			}
		}

		// TODO talk to Marco and figure out if it is allowed to have types on the form: type A: A | int. In such a case, we need to make choice types able to hold other choice types right? 
		HashSet<TypeInlineDefinition> choices = TypeConverter.getChoices(type);
		for(TypeInlineDefinition def : choices){
			InlineType t = TypeConverter.convert(def, new IdentityHashMap<>(rec), ctx, symbols);
			result.addChoiceUnsafe( t );
		}

		return result;
	}

	private static Type convert(TypeDefinitionLink type, IdentityHashMap<TypeDefinition, Type> rec, ParsingContext ctx, SymbolTable symbols){
		return TypeConverter.convert(type.linkedType(), rec, ctx, symbols);
	}

	private static Type convert(TypeDefinitionUndefined type, IdentityHashMap<TypeDefinition, Type> rec, ParsingContext ctx, SymbolTable symbols){
		return null;
	}

	private static HashSet<TypeInlineDefinition> getChoices(TypeDefinition type){
		HashSet<TypeInlineDefinition> choices = new HashSet<>();
		TypeConverter.getChoicesRec(type, choices);
		return choices;
	}
	
	private static void getChoicesRec(TypeDefinition type, HashSet<TypeInlineDefinition> list){
		if(type instanceof TypeChoiceDefinition){
			TypeConverter.getChoicesRec(((TypeChoiceDefinition)type).left(), list);
			TypeConverter.getChoicesRec(((TypeChoiceDefinition)type).right(), list);
		}
		else if(type instanceof TypeInlineDefinition){
			TypeInlineDefinition parsed = (TypeInlineDefinition)type;
			
			list.add(parsed);

			// // copy to type without name, because the choices in TypeChoiceDefinition has the same name as the choice node itself.......
			// TypeInlineDefinition copy = new TypeInlineDefinition(parsed.context(), "", parsed.basicType(), parsed.cardinality()); 
			
			// Set<Entry<String, TypeDefinition>> children = parsed.subTypes();

			// if(children != null){
			// 	for(Entry<String, TypeDefinition> ent : children){
			// 		copy.putSubType(ent.getValue());
			// 	}
			// }

			// copy.setUntypedSubTypes(parsed.untypedSubTypes());

			// list.add( copy );
		}
		else if(type instanceof TypeDefinitionLink){
			TypeConverter.getChoicesRec(((TypeDefinitionLink)type).linkedType(), list);
		}
		else{
			System.out.println("CONVERTION NOT SUPPORTED");
		}
	}
}
