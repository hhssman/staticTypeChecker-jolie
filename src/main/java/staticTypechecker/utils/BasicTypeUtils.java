package staticTypechecker.utils;

import jolie.lang.Constants.OperandType;

import java.util.ArrayList;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import staticTypechecker.faults.Warning;
import staticTypechecker.faults.WarningHandler;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

public class BasicTypeUtils {
	public static Type deriveTypeOfOperation(OperandType operand, Type t1, Type t2){
		if(t1 instanceof InlineType && t2 instanceof InlineType){
			return BasicTypeUtils.deriveTypeOfOperation(operand, (InlineType)t1, (InlineType)t2);
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){
			return BasicTypeUtils.deriveTypeOfOperation(operand, (InlineType)t1, (ChoiceType)t2);
		}
		else if(t1 instanceof ChoiceType && t2 instanceof InlineType){
			return BasicTypeUtils.deriveTypeOfOperation(operand, (ChoiceType)t1, (InlineType)t2);
		}
		else{ // both are choice types
			return BasicTypeUtils.deriveTypeOfOperation(operand, (ChoiceType)t1, (ChoiceType)t2);
		}
	}

	public static Type deriveTypeOfOperation(OperandType operand, InlineType t1, InlineType t2){
		BasicTypeDefinition basicType = BasicTypeUtils.deriveTypeOfOperation(operand, t1.basicType(), t2.basicType());
		return new InlineType(basicType, null, null, false);
	}

	public static Type deriveTypeOfOperation(OperandType operand, InlineType t1, ChoiceType t2){
		ArrayList<BasicTypeDefinition> basicTypes = new ArrayList<>();

		for(InlineType choice : t2.choices()){
			basicTypes.add(BasicTypeUtils.deriveTypeOfOperation(operand, t1.basicType(), choice.basicType()));
		}

		return ChoiceType.fromBasicTypes(basicTypes);
	}

	public static Type deriveTypeOfOperation(OperandType operand, ChoiceType t1, InlineType t2){
		return BasicTypeUtils.deriveTypeOfOperation(operand, t2, t1);
	}

	public static Type deriveTypeOfOperation(OperandType operand, ChoiceType t1, ChoiceType t2){
		ArrayList<BasicTypeDefinition> basicTypes = new ArrayList<>();

		for(InlineType choice1 : t1.choices()){
			for(InlineType choice2 : t2.choices()){
				basicTypes.add(BasicTypeUtils.deriveTypeOfOperation(operand, choice1.basicType(), choice2.basicType()));
			}
		}

		return ChoiceType.fromBasicTypes(basicTypes);
	}


	public static BasicTypeDefinition deriveTypeOfOperation(OperandType operand, BasicTypeDefinition t1, BasicTypeDefinition t2){
		// System.out.println("deriving type of " + operand.toString() + " " + t1.nativeType().id() + " " + t2.nativeType().id());

		NativeType type1 = t1.nativeType();
		NativeType type2 = t2.nativeType();

		// if one of the types is void, return the other
		if(type1 == NativeType.VOID){
			return BasicTypeDefinition.of(type2);
		}
		else if(type2 == NativeType.VOID){
			return BasicTypeDefinition.of(type1);
		}

		// check all combinations and return the appropriate type (NOTE the appropriate types have been deducted by doing all these sums and seeing what the runtime interpreter derives them to)
		if(type1 == NativeType.BOOL){
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){ // adding a bool with a string results in string
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				else{ // all other cases are bools
					return BasicTypeDefinition.of(NativeType.BOOL);
				}
			}
			else{
				return BasicTypeDefinition.of(type2);
			}
		}
		else if(type1 == NativeType.INT){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT){
				return BasicTypeDefinition.of(NativeType.INT);
			}
			else if(type2 == NativeType.LONG){
				return BasicTypeDefinition.of(NativeType.LONG);
			}
			else if(type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			else if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				if(operand == OperandType.SUBTRACT || operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.INT);
				}

				// TODO throw warning, division and modulo with a string only allowed if string can be parsed to int
				WarningHandler.addWarning(new Warning("the operations 'int / string' and 'int % string' are only allowed if the string can be parsed to a number"));
				return BasicTypeDefinition.of(NativeType.INT);
			}
		}
		else if(type1 == NativeType.LONG){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT){
				return BasicTypeDefinition.of(NativeType.LONG);
			}
			if(type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.STRING);
				}
				else if(operand == OperandType.SUBTRACT || operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.LONG);
				}

				// TODO throw warning, division and modulo with a string only allowed if string can be parsed to int
				WarningHandler.addWarning(new Warning("the operations 'long / string' and 'long % string' are only allowed if the string can be parsed to a number"));
				return BasicTypeDefinition.of(NativeType.LONG);
			}

		}
		else if(type1 == NativeType.DOUBLE){
			if(type2 == NativeType.BOOL || type2 == NativeType.INT || type2 == NativeType.LONG || type2 == NativeType.DOUBLE){
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
			if(type2 == NativeType.STRING){
				if(operand == OperandType.ADD){
					return BasicTypeDefinition.of(NativeType.DOUBLE);
				}
				
				return BasicTypeDefinition.of(NativeType.DOUBLE);
			}
		}
		else if(type1 == NativeType.STRING){
			if(type2 == NativeType.BOOL){
				if(operand == OperandType.MULTIPLY){
					return BasicTypeDefinition.of(NativeType.BOOL);
				}

				return BasicTypeDefinition.of(NativeType.STRING);
			}

			if(operand == OperandType.ADD){
				return BasicTypeDefinition.of(NativeType.STRING);
			}

			return BasicTypeDefinition.of(type2);
		}

		return BasicTypeDefinition.of(NativeType.VOID);
	}
}
