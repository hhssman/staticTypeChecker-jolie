package staticTypechecker.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import jolie.lang.Constants.OperandType;
import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.AddAssignStatement;
import jolie.lang.parse.ast.AssignStatement;
import jolie.lang.parse.ast.CompareConditionNode;
import jolie.lang.parse.ast.CompensateStatement;
import jolie.lang.parse.ast.CorrelationSetInfo;
import jolie.lang.parse.ast.CurrentHandlerStatement;
import jolie.lang.parse.ast.DeepCopyStatement;
import jolie.lang.parse.ast.DefinitionCallStatement;
import jolie.lang.parse.ast.DefinitionNode;
import jolie.lang.parse.ast.DivideAssignStatement;
import jolie.lang.parse.ast.DocumentationComment;
import jolie.lang.parse.ast.EmbedServiceNode;
import jolie.lang.parse.ast.EmbeddedServiceNode;
import jolie.lang.parse.ast.ExecutionInfo;
import jolie.lang.parse.ast.ExitStatement;
import jolie.lang.parse.ast.ForEachArrayItemStatement;
import jolie.lang.parse.ast.ForEachSubNodeStatement;
import jolie.lang.parse.ast.ForStatement;
import jolie.lang.parse.ast.IfStatement;
import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.InstallFixedVariableExpressionNode;
import jolie.lang.parse.ast.InstallStatement;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.InterfaceExtenderDefinition;
import jolie.lang.parse.ast.LinkInStatement;
import jolie.lang.parse.ast.LinkOutStatement;
import jolie.lang.parse.ast.MultiplyAssignStatement;
import jolie.lang.parse.ast.NDChoiceStatement;
import jolie.lang.parse.ast.NotificationOperationStatement;
import jolie.lang.parse.ast.NullProcessStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OneWayOperationStatement;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.ParallelStatement;
import jolie.lang.parse.ast.PointerStatement;
import jolie.lang.parse.ast.PostDecrementStatement;
import jolie.lang.parse.ast.PostIncrementStatement;
import jolie.lang.parse.ast.PreDecrementStatement;
import jolie.lang.parse.ast.PreIncrementStatement;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ProvideUntilStatement;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationStatement;
import jolie.lang.parse.ast.RunStatement;
import jolie.lang.parse.ast.Scope;
import jolie.lang.parse.ast.SequenceStatement;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.ast.SolicitResponseOperationStatement;
import jolie.lang.parse.ast.SpawnStatement;
import jolie.lang.parse.ast.SubtractAssignStatement;
import jolie.lang.parse.ast.SynchronizedStatement;
import jolie.lang.parse.ast.ThrowStatement;
import jolie.lang.parse.ast.TypeCastExpressionNode;
import jolie.lang.parse.ast.UndefStatement;
import jolie.lang.parse.ast.ValueVectorSizeExpressionNode;
import jolie.lang.parse.ast.VariablePathNode;
import jolie.lang.parse.ast.WhileStatement;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.AndConditionNode;
import jolie.lang.parse.ast.expression.ConstantBoolExpression;
import jolie.lang.parse.ast.expression.ConstantDoubleExpression;
import jolie.lang.parse.ast.expression.ConstantIntegerExpression;
import jolie.lang.parse.ast.expression.ConstantLongExpression;
import jolie.lang.parse.ast.expression.ConstantStringExpression;
import jolie.lang.parse.ast.expression.FreshValueExpressionNode;
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode;
import jolie.lang.parse.ast.expression.InstanceOfExpressionNode;
import jolie.lang.parse.ast.expression.IsTypeExpressionNode;
import jolie.lang.parse.ast.expression.NotExpressionNode;
import jolie.lang.parse.ast.expression.OrConditionNode;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.lang.parse.ast.expression.VariableExpressionNode;
import jolie.lang.parse.ast.expression.VoidExpressionNode;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Pair;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.utils.BasicTypeUtils;
import staticTypechecker.utils.ToString;
import staticTypechecker.utils.TypeUtils;
import staticTypechecker.utils.TypeConverter;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.OutputPort;
import staticTypechecker.entities.Path;
import staticTypechecker.entities.Service;
import staticTypechecker.faults.FaultHandler;
import staticTypechecker.faults.MiscFault;
import staticTypechecker.faults.TypeFault;
import staticTypechecker.faults.UnknownFunctionFault;
import staticTypechecker.faults.WarningHandler;

/**
 * Synthesizer for a parsed Jolie abstract syntax tree. Synthesizes the type of each node
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Synthesizer implements OLVisitor<Type, Type> {
	private static HashMap<String, Synthesizer> synths = new HashMap<>(); // maps module name to synthesizer
	private Module module;
	private Stack<ArrayList<Path>> pathsAlteredInWhile = new Stack<>();
	private Stack<Service> serviceStack = new Stack<>();
	private boolean print;
	
	public static Synthesizer get(Module module){
		return Synthesizer.get(module, false);
	}

	public static Synthesizer get(Module module, boolean print){
		Synthesizer ret = Synthesizer.synths.get(module.name());

		if(ret == null){
			ret = new Synthesizer(module, print);
			Synthesizer.synths.put(module.name(), ret);
		}
	
		return ret;
	}
	
	private Synthesizer(Module module, boolean print){
		this.module = module;
		this.print = print;
	}

	private void printNode(OLSyntaxNode node){
		if(this.print){
			System.out.println(ToString.of(node));
		}
	}

	private void printTree(Type T){
		if(this.print){
			System.out.println(T.prettyString());
			System.out.println("\n--------------------------\n");
		}
	}

	public Type synthesize(){
		if(this.print){
			System.out.println("---- Processing behaviours for " + module.name() + "--------");
		}

		Type result = this.module.program().accept(this, Type.VOID());

		if(this.print){
			System.out.println("Final tree for module " + module.name() + ":");
			this.printTree(result);
		}
		
		return result;
	}

	public Type synthesize(OLSyntaxNode node, Type T){
		this.printNode(node);
		Type res = node.accept(this, T);
		this.printTree(res);
		return res;
	}

	public Type visit(Program p, Type T){
		for(OLSyntaxNode n : p.children()){
			T = this.synthesize(n, T);
		}
		return T;
	}

	public Type visit(TypeInlineDefinition t, Type T){
		return T;
	}

	public Type visit( OneWayOperationDeclaration decl, Type T ){
		return T;
	};

	public Type visit( RequestResponseOperationDeclaration decl, Type T ){
		return T;
	};

	public Type visit( DefinitionNode n, Type T ){
		return this.synthesize(n.body(), T);
	};

	public Type visit( ParallelStatement n, Type T ){
		return T;
	};

	public Type visit( SequenceStatement n, Type T ){
		for(OLSyntaxNode child : n.children()){
			T = this.synthesize(child, T);
		}
		return T;
	};

	public Type visit( NDChoiceStatement n, Type T ){
		ArrayList<Type> trees = new ArrayList<>(n.children().size()); // save all the possible outcomes

		for(int i = 0; i < n.children().size(); i++){
			Type T1 = this.synthesize(n.children().get(i).key(), T); // synthesize the label (in the [])
			Type T2 = this.synthesize(n.children().get(i).value(), T1); // synthesize the behaviour (in the {})
			trees.add(T2);
		}
		
		if(trees.size() == 1){
			return trees.get(0);
		}
		else{
			return new ChoiceType(trees);		
		}
	};

	public Type visit( OneWayOperationStatement n, Type T ){
		Service service = this.serviceStack.peek();
		Operation op = service.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in service '" + service.name() + "'. Maybe you forgot to give the service an inputPort with an interface which provides the operation?", n.context()), false);
			return T;
		}
		// Operation op = (Operation)this.module.symbols().get(n.id(), SymbolType.OPERATION);

		Type T_in = op.requestType(); // the data type which is EXPECTED by the oneway operation
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given as input to the operation

		// update the node at the path to the input type
		Type T1 = T.shallowCopyExcept(p_in);
		TypeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(p_in);
		}

		return T1;
	};

	public Type visit( RequestResponseOperationStatement n, Type T ){
		Service service = this.serviceStack.peek();
		Operation op = service.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in service '" + service.name() + "'. Maybe you forgot to give the service an inputPort with an interface which provides the operation?", n.context()), false);
			return T;
		}
		// Operation op = (Operation)this.module.symbols().get(n.id(), SymbolType.OPERATION);
		
		Type T_in = op.requestType(); // the type of the data the operation EXPECTS as input
		Type T_out = op.responseType(); // the type of the data RETURNED from the operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given AS INPUT to the operation
		Path p_out = new Path(n.outputExpression().toString()); // the path to the node in which the OUTPUT of the operation is stored

		// given that p_in is of type T_in find the type of the behaviour
		Type T_update = T.shallowCopyExcept(p_in);
		TypeUtils.setTypeOfNodeByPath(p_in, T_in, T_update);
		Type T1 = this.synthesize(n.process(), T_update);
		
		// check that p_out is a subtype of T_out 
		ArrayList<Type> possibleTypes = TypeUtils.findNodesExact(p_out, T1, true, false); // the possible types of p_out after the behaviour
		
		Type p_out_type;
		if(possibleTypes.size() == 1){
			p_out_type = possibleTypes.get(0);
		}
		else{
			p_out_type = new ChoiceType(possibleTypes);
		}

		this.check(p_out_type, T_out, n.context(), "operation \"" + op.name() + "\" does not have the expected return type.\nActual return type:\n" + p_out_type.prettyString() + "\n\nExpected return type:\n" + T_out.prettyString());

		return T1;
	};

	public Type visit( NotificationOperationStatement n, Type T ){
		OutputPort port = (OutputPort)this.module.symbols().get(n.outputPortId(), SymbolType.OUTPUT_PORT);
		Operation op = port.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in outputPort '" + port.name(), n.context()), false);
			return T;
		}

		String operationName = op.name();
		String outputPortName = n.outputPortId();
		
		// if the notify is an assertion, it is a typehint
		if(operationName.equals("assert") && outputPortName.equals(System.getProperty("typehint"))){
			if(!(n.outputExpression() instanceof InstanceOfExpressionNode)){
				FaultHandler.throwFault(new MiscFault("argument given to assert must be an instanceof expression", n.context()), true);
			}
			InstanceOfExpressionNode parsedNode = (InstanceOfExpressionNode)n.outputExpression();
			OLSyntaxNode expression = parsedNode.expression();

			if(!(expression instanceof VariableExpressionNode)){
				FaultHandler.throwFault(new MiscFault("first argument of instanceof must be a path to a variable", n.context()), true);
			}
			
			Type type = TypeConverter.convert(parsedNode.type(), this.module.symbols());
			Type typeOfEx = this.synthesize(expression, T);
			String nameOfExpression = ToString.of(expression);
			this.check(typeOfEx, type, n.context(), nameOfExpression + " does not have the same type as the typehint. Type of " + nameOfExpression + ":\n" + typeOfEx.prettyString() + "\n\nExpected type:\n" + type.prettyString());

			Path path = new Path(((VariableExpressionNode)expression).variablePath().path());
			Type T1 = T.shallowCopyExcept(path);
			TypeUtils.setTypeOfNodeByPath(path, type, T1);
			return T1;
		}
		
		// else it is just a normal oneway invocation
		Type T_out = op.requestType(); // the type of the data which is EXPECTED of the oneway operation
		Type p_out = n.outputExpression() != null ? this.synthesize(n.outputExpression(), T) : Type.VOID(); // the type which is GIVEN to the oneway operation

		this.check(p_out, T_out, n.context(), "Type given to \"" + op.name() + "\" is different from what is expected. Given type:\n" + p_out.prettyString() + "\n\nExpected type:\n" + T_out.prettyString());

		return T;
	};

	public Type visit( SolicitResponseOperationStatement n, Type T ){
		OutputPort port = (OutputPort)this.module.symbols().get(n.outputPortId(), SymbolType.OUTPUT_PORT);
		Operation op = port.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in outputPort '" + port.name(), n.context()), false);
			return T;
		}
		// Operation op = (Operation)this.module.symbols().get(n.id(), SymbolType.OPERATION);

		Type T_in = op.responseType(); // the type of the data which is RETURNED by the reqres operation
		Type T_out = op.requestType(); // the type of the data which is EXPECTED of the reqres operation
		
		Path p_in; // the path to the node in which to store the returned data

		if(n.inputVarPath() == null){ // no input path given
			p_in = new Path();
		}
		else{
			p_in = new Path(n.inputVarPath().path());
		}

		Type p_out = n.outputExpression() != null ? this.synthesize(n.outputExpression(), T) : Type.VOID(); // the type which is GIVEN to the reqres operation
		
		// check that p_out is subtype of T_out
		this.check(p_out, T_out, n.context(), "Type given to \"" + op.name() + "\" is different from what is expected. Given type:\n" + p_out.prettyString() + "\n\nExpected type:\n" + T_out.prettyString());

		// update type of p_in to T_in
		Type T1 = T.shallowCopyExcept(p_in);
		TypeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(p_in);
		}

		return T1;
	};

	public Type visit( LinkInStatement n, Type T ){
		return null;
	};

	public Type visit( LinkOutStatement n, Type T ){
		return null;
	};

	public Type visit( AssignStatement n, Type T ){
		// retrieve the type of the expression
		OLSyntaxNode e = n.expression();
		Type T_e = this.synthesize(e, T);
		
		// update the type of the node
		Path path = new Path(n.variablePath().path());
		Type T1 = T.shallowCopyExcept(path);
		ArrayList<BasicTypeDefinition> basicTypes = Type.getBasicTypesOfNode(T_e);
		TypeUtils.setBasicTypeOfNodeByPath(path, basicTypes, T1);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}

		return T1;
	};

	@Override
	public Type visit(AddAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.shallowCopyExcept(path);
		
		Type typeOfRightSide = this.synthesize(n.variablePath(), T);
		Type typeOfExpression = this.synthesize(n.expression(), T);

		this.deriveTypeAndUpdateNode(path, T1, OperandType.ADD, typeOfRightSide, typeOfExpression, n.context());

		return T1;
	}

	@Override
	public Type visit(SubtractAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.shallowCopyExcept(path);

		Type typeOfRightSide = this.synthesize(n.variablePath(), T);
		Type typeOfExpression = this.synthesize(n.expression(), T);

		this.deriveTypeAndUpdateNode(path, T1, OperandType.SUBTRACT, typeOfRightSide, typeOfExpression, n.context());

		return T1;
	}

	@Override
	public Type visit(MultiplyAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.shallowCopyExcept(path);

		Type typeOfRightSide = this.synthesize(n.variablePath(), T);
		Type typeOfExpression = this.synthesize(n.expression(), T);

		this.deriveTypeAndUpdateNode(path, T1, OperandType.MULTIPLY, typeOfRightSide, typeOfExpression, n.context());
		
		return T1;
	}

	@Override
	public Type visit(DivideAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.shallowCopyExcept(path);

		Type typeOfRightSide = this.synthesize(n.variablePath(), T);
		Type typeOfExpression = this.synthesize(n.expression(), T);

		this.deriveTypeAndUpdateNode(path, T1, OperandType.DIVIDE, typeOfRightSide, typeOfExpression, n.context());
		
		return T1;
	}

	private void deriveTypeAndUpdateNode(Path path, Type tree, OperandType opType, Type rightSide, Type expression, ParsingContext ctx){
		List<BasicTypeDefinition> newBasicTypes = BasicTypeUtils.deriveTypeOfOperation(opType, rightSide, expression, ctx);
		TypeUtils.setBasicTypeOfNodeByPath(path, newBasicTypes, tree);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}
	}

	public Type visit( IfStatement n, Type T ){
		ChoiceType resultType = new ChoiceType();

		for(Pair<OLSyntaxNode, OLSyntaxNode> p : n.children()){
			OLSyntaxNode expression = p.key();
			OLSyntaxNode body = p.value();

			Type typeOfEx = this.synthesize(expression, T);
			this.check(typeOfEx, Type.BOOL(), n.context(), "Guard of if-statement is not subtype of bool { ? }. Found type:\n" + typeOfEx.prettyString()); // check that expression is of type bool
			Type T1 = this.synthesize(body, T);
			resultType.addChoiceUnsafe(T1);
			// if(!(expression instanceof InstanceOfExpressionNode)){ // COND-1, e is an expression of anything else than instanceof
			// }
		}

		OLSyntaxNode elseProcess = n.elseProcess();
		if(elseProcess != null){ // there is an else clause
			resultType.addChoiceUnsafe(this.synthesize(elseProcess, T));
		}
		else{ // there is not else clause, here we add the initial state as choice as well, since we may not enter the if statement
			resultType.addChoiceUnsafe(T);
		}
		
		if(resultType.choices().size() == 1){
			return resultType.choices().get(0);
		}
			
		return resultType;
	};

	public Type visit( DefinitionCallStatement n, Type T ){
		return null;
	};

	public Type visit( WhileStatement n, Type T ){
		this.pathsAlteredInWhile.push(new ArrayList<>());

		OLSyntaxNode condition = n.condition();
		OLSyntaxNode body = n.body();

		Type typeOfCondition = this.synthesize(condition, T);
		this.check(typeOfCondition, Type.BOOL(), n.context(), "Guard of while loop is not of type bool. Found type:\n" + typeOfCondition.prettyString()); // check that the initial condition is of type bool

		Type originalState = T; // saved here, since it is used in the fallback plan

		// the return type is a conjunction between the original state and the one found through the iterations OR the fallback
		ChoiceType result = new ChoiceType();
		result.addChoiceUnsafe(originalState);

		ChoiceType mergedState = new ChoiceType();
		mergedState.addChoiceUnsafe(originalState);

		for(int i = 0; i < 10; i++){
			Type R = this.synthesize(body, T); // synthesize the type of the body after an iteration
			typeOfCondition = this.synthesize(condition, R);
			this.check(typeOfCondition, Type.BOOL(), n.context(), "Guard of while loop is not of type bool. Found type:\n" + typeOfCondition.prettyString()); // check that the initial condition is of type bool
			
			if(R.isSubtypeOf(mergedState)){ // the new state is a subtype of one of the previous states (we have a steady state)
				result.addChoiceUnsafe(mergedState);
				this.pathsAlteredInWhile.pop();
				return result;
			}

			mergedState.addChoiceUnsafe(R);
			T = R;
		}

		// we did not find a steady state in the while loop. Here we do the fallback plan, which is to undefine all variables changed in the while loop
		result.addChoiceUnsafe( TypeUtils.undefine(originalState, this.pathsAlteredInWhile.peek()) );

		WarningHandler.throwWarning("could not determine the resulting type of the while loop, affected types may be incorrect from here", n.context());
		
		this.pathsAlteredInWhile.pop();
		return result.convertIfPossible();
	};

	public Type visit( OrConditionNode n, Type T ){
		return Type.BOOL();
	};

	public Type visit( AndConditionNode n, Type T ){
		return Type.BOOL();
	};

	public Type visit( NotExpressionNode n, Type T ){
		return Type.BOOL();
	};

	public Type visit( CompareConditionNode n, Type T ){
		return Type.BOOL();
	};

	public Type visit( ConstantIntegerExpression n, Type T ){
		return Type.INT();
	};

	public Type visit( ConstantDoubleExpression n, Type T ){
		return Type.DOUBLE();
	};

	public Type visit( ConstantBoolExpression n, Type T ){
		return Type.BOOL();
	};

	public Type visit( ConstantLongExpression n, Type T ){
		return Type.LONG();
	};

	public Type visit( ConstantStringExpression n, Type T ){
		return Type.STRING();
	};

	public Type visit( ProductExpressionNode n, Type T ){
		return this.getTypeOfSumOrProduct(n, T);
	};

	public Type visit( SumExpressionNode n, Type T ){
		return this.getTypeOfSumOrProduct(n, T);
	};

	private Type getTypeOfSumOrProduct(OLSyntaxNode n, Type T){
		List<Pair<OperandType, OLSyntaxNode>> operands;

		if(n instanceof SumExpressionNode){
			operands = ((SumExpressionNode)n).operands();
		}
		else{
			operands = ((ProductExpressionNode)n).operands();
		}

		Type currType = Type.VOID(); // set initial type to void to make sure it will be overwritten by any other type

		for(Pair<OperandType, OLSyntaxNode> pair : operands){
			OperandType currOp = pair.key();
			Type nextType = this.synthesize(pair.value(), T);

			List<BasicTypeDefinition> basicTypes = BasicTypeUtils.deriveTypeOfOperation(currOp, currType, nextType, n.context());

			if(basicTypes.size() == 1){
				currType = new InlineType(basicTypes.get(0), null, null, false);
			}
			else{
				currType = ChoiceType.fromBasicTypes(basicTypes);
			}
		}

		return currType;
	}

	public Type visit( VariableExpressionNode n, Type T ){
		Path path = new Path(n.variablePath().path());
		ArrayList<Type> types = TypeUtils.findNodesExact(path, T, false, false);

		if(types.isEmpty()){ // return void type if no nodes was found
			return Type.VOID();
		}
		else if(types.size() == 1){
			return types.get(0);
		}
		else{
			return new ChoiceType(types);
		}
	};

	public Type visit( NullProcessStatement n, Type T ){
		return T;
	};

	public Type visit( Scope n, Type T ){
		return T;
	};

	public Type visit( InstallStatement n, Type T ){
		return T;
	};

	public Type visit( CompensateStatement n, Type T ){
		return T;
	};

	public Type visit( ThrowStatement n, Type T ){
		return T;
	};

	public Type visit( ExitStatement n, Type T ){
		return T;
	};

	public Type visit( ExecutionInfo n, Type T ){
		return T;
	};

	public Type visit( CorrelationSetInfo n, Type T ){
		return T;
	};

	public Type visit( InputPortInfo n, Type T ){
		return T;
	};

	public Type visit( OutputPortInfo n, Type T ){
		return T;
	};

	public Type visit( PointerStatement n, Type T ){
		return T;
	};

	public Type visit( DeepCopyStatement n, Type T ){
		Path leftPath = new Path(n.leftPath().path());
		
		Type T1 = T.shallowCopyExcept(leftPath);
		T1 = TypeUtils.unfold(leftPath, T1);

		ArrayList<InlineType> trees = new ArrayList<>();
	
		if(T1 instanceof InlineType){
			trees.add((InlineType)T1);
		}
		else{
			ChoiceType parsed = (ChoiceType)T1;
			trees = parsed.choices();
		}

		for(InlineType tree : trees){
			Type typeOfExpression = this.synthesize(n.rightExpression(), tree);
	
			// find the nodes to update and their parents
			ArrayList<Pair<InlineType, String>> leftSideNodes = TypeUtils.findParentAndName(leftPath, tree, true, false);
	
			// update the nodes with the deep copied versions
			for(Pair<InlineType, String> pair : leftSideNodes){
				InlineType parent = pair.key();
				String childName = pair.value();
				Type child = parent.getChild(childName);

				
				// TypeUtils.fold(child);
				Type resultOfDeepCopy = Type.deepCopy(child, typeOfExpression);
				parent.addChildUnsafe(childName, resultOfDeepCopy);
			}
		}

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(leftPath);
		}

		return T1;
	};

	public Type visit( RunStatement n, Type T ){
		return T;
	};

	public Type visit( UndefStatement n, Type T ){
		Path path = new Path(n.variablePath().path());
		Type T1 = T.shallowCopyExcept(path);

		ArrayList<Pair<InlineType, String>> nodesToRemove = TypeUtils.findParentAndName(path, T1, false, false);

		for(Pair<InlineType, String> pair : nodesToRemove){
			pair.key().removeChildUnsafe(pair.value());
		}

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}

		return T1;
	};

	public Type visit( ValueVectorSizeExpressionNode n, Type T ){
		return T;
	};

	public Type visit( PreIncrementStatement n, Type T ){
		return T;
	};

	public Type visit( PostIncrementStatement n, Type T ){
		return T;
	};

	public Type visit( PreDecrementStatement n, Type T ){
		return T;
	};

	public Type visit( PostDecrementStatement n, Type T ){
		return T;
	};

	public Type visit( ForStatement n, Type T ){
		return T;
	};

	public Type visit( ForEachSubNodeStatement n, Type T ){
		return T;
	};

	public Type visit( ForEachArrayItemStatement n, Type T ){
		return T;
	};

	public Type visit( SpawnStatement n, Type T ){
		return T;
	};

	public Type visit( IsTypeExpressionNode n, Type T ){
		return T;
	};

	public Type visit( InstanceOfExpressionNode n, Type T ){
		return Type.BOOL();
	};

	public Type visit( TypeCastExpressionNode n, Type T ){
		return new InlineType(BasicTypeDefinition.of(n.type()), null, null, false);
	};

	public Type visit( SynchronizedStatement n, Type T ){
		return T;
	};

	public Type visit( CurrentHandlerStatement n, Type T ){
		return T;
	};

	public Type visit( EmbeddedServiceNode n, Type T ){
		return T;
	};

	public Type visit( InstallFixedVariableExpressionNode n, Type T ){
		return T;
	};

	public Type visit( VariablePathNode n, Type T ){
		Path path = new Path(n.path());
		ArrayList<Type> types = TypeUtils.findNodesExact(path, T, false, false);

		if(types.size() == 1){
			return types.get(0);
		}
		else{
			return new ChoiceType(types);
		}
	};

	public Type visit( TypeDefinitionLink n, Type T ){
		return T;
	};

	public Type visit( InterfaceDefinition n, Type T ){
		return T;
	};

	public Type visit( DocumentationComment n, Type T ){
		return T;
	};

	public Type visit( FreshValueExpressionNode n, Type T ){
		return T;
	};

	public Type visit( CourierDefinitionNode n, Type T ){
		return T;
	};

	public Type visit( CourierChoiceStatement n, Type T ){
		return T;
	};

	public Type visit( NotificationForwardStatement n, Type T ){
		return T;
	};

	public Type visit( SolicitResponseForwardStatement n, Type T ){
		return T;
	};

	public Type visit( InterfaceExtenderDefinition n, Type T ){
		return T;
	};

	public Type visit( InlineTreeExpressionNode n, Type T ){
		return T;
	};

	public Type visit( VoidExpressionNode n, Type T ){
		return T;
	};

	public Type visit( ProvideUntilStatement n, Type T ){
		return T;
	};

	public Type visit( TypeChoiceDefinition n, Type T ){
		return T;
	};

	public Type visit( ImportStatement n, Type T ){
		return T;
	};

	public Type visit( ServiceNode n, Type T ){
		Type result;
		Type T1 = T;

		if(n.parameterConfiguration().isPresent()){
			Path path = new Path(n.parameterConfiguration().get().variablePath());
			Type typeOfParam = (Type)this.module.symbols().get(n.parameterConfiguration().get().type().name(), SymbolType.TYPE);
			T1 = T.shallowCopyExcept(path);

			TypeUtils.setTypeOfNodeByPath(path, typeOfParam, T1);
			this.printTree(T1);
		}

		// synthesize the program of the service node
		this.serviceStack.push((Service)this.module.symbols().get(n.name(), SymbolType.SERVICE));
		result = this.synthesize(n.program(), T1);
		this.serviceStack.pop();

		return result;
	};

	public Type visit( EmbedServiceNode n, Type T ){
		return T;
	};

	public void check(Type T, Type S, ParsingContext ctx, String faultMessage){
		if(!T.isSubtypeOf(S)){
			FaultHandler.throwFault(new TypeFault(faultMessage, ctx), false);
		}
	}
}
