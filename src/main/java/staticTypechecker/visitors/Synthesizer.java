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
 * Synthesizer for a parsed Jolie abstract syntax tree. Synthesizes the type of each node.
 * 
 * @author Kasper Bergstedt (kasper.bergstedt@hotmail.com)
 */
public class Synthesizer implements OLVisitor<Type, Type> {
	private static HashMap<String, Synthesizer> synths = new HashMap<>(); 	// a static map, which maps module paths to synthesizers
	private Module module; 													// the module of this synthesizer
	private Stack<ArrayList<Path>> pathsAlteredInWhile = new Stack<>();		// the stack to keep track of the paths to the nodes changed during a while loop
	private Service service = null;											// the service object of the service the synthesizer currently is synthezing. This is needed to find operations
	
	/**
	 * Get a synthesizer for the given module
	 * @param module the module of the synthesizer
	 * @return the Synthesizer for the module
	 */
	public static Synthesizer get(Module module){
		Synthesizer ret = Synthesizer.synths.get(module.fullPath());

		if(ret == null){
			ret = new Synthesizer(module);
			Synthesizer.synths.put(module.fullPath(), ret);
		}
	
		return ret;
	}
	
	private Synthesizer(Module module){
		this.module = module;
	}

	/**
	 * Synthesizes the program of the module of this Synthesizer
	 * @return the final type tree for the module 
	 */
	public Type synthesize(){
		return this.module.program().accept(this, Type.VOID());
	}

	/**
	 * Synthesizes the type of the given node
	 * @param node the node to synthesize the type of
	 * @param t the initial type tree. This tree will not be changed
	 * @return the type tree after the node has been synthesized
	 */
	public Type synthesize(OLSyntaxNode node, Type t){
		return node.accept(this, t);
	}

	public Type visit(Program p, Type t){
		for(OLSyntaxNode n : p.children()){
			t = this.synthesize(n, t);
		}
		return t;
	}

	public Type visit(TypeInlineDefinition ti, Type t){
		return t;
	}

	public Type visit( OneWayOperationDeclaration decl, Type t ){
		return t;
	}

	public Type visit( RequestResponseOperationDeclaration decl, Type t ){
		return t;
	}

	public Type visit( DefinitionNode n, Type t ){
		return this.synthesize(n.body(), t);
	}

	public Type visit( ParallelStatement n, Type t ){
		return t;
	}

	public Type visit( SequenceStatement n, Type t ){
		Type t1 = null;
		for(OLSyntaxNode child : n.children()){
			t1 = this.synthesize(child, t);
		}
		return t1 != null ? t1 : t;
	}

	public Type visit( NDChoiceStatement n, Type t ){
		ArrayList<Type> trees = new ArrayList<>(n.children().size()); // save all the possible outcomes

		for(int i = 0; i < n.children().size(); i++){
			Type t1 = this.synthesize(n.children().get(i).key(), t); // synthesize the label (in the [])
			Type t2 = this.synthesize(n.children().get(i).value(), t1); // synthesize the behaviour (in the {})
			trees.add(t2);
		}
		
		if(trees.size() == 1){
			return trees.get(0);
		}
		else{
			return new ChoiceType(trees);		
		}
	}

	public Type visit( OneWayOperationStatement n, Type t ){
		Service service = this.service;
		Operation op = service.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in service '" + service.name() + "'. Maybe you forgot to give the service an inputPort with an interface which provides the operation?", n.context()), false);
			return t;
		}

		Type t_in = op.requestType(); // the data type which is EXPECTED by the oneway operation
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given as input to the operation

		// update the node at the path to the input type
		Type t1 = setTypeOfNode(t, t_in, p_in);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(p_in);
		}

		return t1;
	}

	public Type visit( RequestResponseOperationStatement n, Type t ){
		Service service = this.service;
		Operation op = service.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in service '" + service.name() + "'. Maybe you forgot to give the service an inputPort with an interface which provides the operation?", n.context()), false);
			return t;
		}
		
		Type t_in = op.requestType(); // the type of the data the operation EXPECTS as input
		Type t_out = op.responseType(); // the type of the data RETURNED from the operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given AS INPUT to the operation
		Path p_out = new Path(n.outputExpression().toString()); // the path to the node in which the OUTPUT of the operation is stored

		// given that p_in is of type t_in find the type of the behaviour
		Type t_update = setTypeOfNode(t, t_in, p_in);
		Type t1 = this.synthesize(n.process(), t_update);
		
		// check that p_out is a subtype of t_out 
		ArrayList<Type> possibleTypes = TypeUtils.findNodesExact(p_out, t1, true, false); // the possible types of p_out after the behaviour
		
		Type p_out_type;
		if(possibleTypes.size() == 1){
			p_out_type = possibleTypes.get(0);
		}
		else{
			p_out_type = new ChoiceType(possibleTypes);
		}

		this.check(p_out_type, t_out, n.context(), "operation \"" + op.name() + "\" does not have the expected return type.\nActual return type:\n" + p_out_type.prettyString() + "\n\nExpected return type:\n" + t_out.prettyString());

		return t1;
	}

	public Type visit( NotificationOperationStatement n, Type t ){
		OutputPort port = (OutputPort)this.module.symbols().get(n.outputPortId(), SymbolType.OUTPUT_PORT);
		Operation op = port.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in outputPort '" + port.name(), n.context()), false);
			return t;
		}

		String operationName = op.name();
		String outputPortName = n.outputPortId();
		
		// if the notify is an assertion, it is a typehint
		if(operationName.equals("assert") && outputPortName.equals(System.getProperty("typehint"))){
			return assertOperation(n, t);
		}
		
		// else it is just a normal oneway invocation
		Type t_out = op.requestType(); // the type of the data which is EXPECTED of the oneway operation
		Type p_out = n.outputExpression() != null ? this.synthesize(n.outputExpression(), t) : Type.VOID(); // the type which is GIVEN to the oneway operation

		this.check(p_out, t_out, n.context(), "Type given to \"" + op.name() + "\" is different from what is expected. Given type:\n" + p_out.prettyString() + "\n\nExpected type:\n" + t_out.prettyString());

		return t;
	}

	public Type visit( SolicitResponseOperationStatement n, Type t ){
		OutputPort port = (OutputPort)this.module.symbols().get(n.outputPortId(), SymbolType.OUTPUT_PORT);
		Operation op = port.getOperation(n.id());
		if(op == null){
			FaultHandler.throwFault(new UnknownFunctionFault("The operation '" + n.id() + "' is unknown in outputPort '" + port.name(), n.context()), false);
			return t;
		}

		Type t_in = op.responseType(); // the type of the data which is RETURNED by the reqres operation
		Type t_out = op.requestType(); // the type of the data which is EXPECTED of the reqres operation
		
		Path p_in; // the path to the node in which to store the returned data

		if(n.inputVarPath() == null){ // no input path given
			p_in = new Path();
		}
		else{
			p_in = new Path(n.inputVarPath().path());
		}

		Type p_out = n.outputExpression() != null ? this.synthesize(n.outputExpression(), t) : Type.VOID(); // the type which is GIVEN to the reqres operation
		
		// check that p_out is subtype of t_out
		this.check(p_out, t_out, n.context(), "Type given to \"" + op.name() + "\" is different from what is expected. Given type:\n" + p_out.prettyString() + "\n\nExpected type:\n" + t_out.prettyString());

		// update type of p_in to t_in
		Type t1 = setTypeOfNode(t, t_in, p_in);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(p_in);
		}

		return t1;
	}

	public Type visit( LinkInStatement n, Type t ){
		return null;
	}

	public Type visit( LinkOutStatement n, Type t ){
		return null;
	}

	public Type visit( AssignStatement n, Type t ){
		// retrieve the type of the expression
		OLSyntaxNode e = n.expression();
		Type t_e = this.synthesize(e, t);
		
		// update the type of the node
		Path path = new Path(n.variablePath().path());
		Type t1 = setBasicTypeOfNode(t, t_e, path);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}

		return t1;
	}

	@Override
	public Type visit(AddAssignStatement n, Type t) {
		Path path = new Path(n.variablePath().path());

		Type t1 = t.shallowCopyExcept(path);
		
		Type typeOfLeftSide = this.synthesize(n.variablePath(), t);
		Type typeOfRightSide = this.synthesize(n.expression(), t);

		this.deriveTypeAndUpdateNode(path, t1, OperandType.ADD, typeOfLeftSide, typeOfRightSide, n.context());

		return t1;
	}

	@Override
	public Type visit(SubtractAssignStatement n, Type t) {
		Path path = new Path(n.variablePath().path());

		Type t1 = t.shallowCopyExcept(path);

		Type typeOfLeftSide = this.synthesize(n.variablePath(), t);
		Type typeOfRightSide = this.synthesize(n.expression(), t);

		this.deriveTypeAndUpdateNode(path, t1, OperandType.SUBTRACT, typeOfLeftSide, typeOfRightSide, n.context());

		return t1;
	}

	@Override
	public Type visit(MultiplyAssignStatement n, Type t) {
		Path path = new Path(n.variablePath().path());

		Type t1 = t.shallowCopyExcept(path);

		Type typeOfLeftSide = this.synthesize(n.variablePath(), t);
		Type typeOfRightSide = this.synthesize(n.expression(), t);

		this.deriveTypeAndUpdateNode(path, t1, OperandType.MULTIPLY, typeOfLeftSide, typeOfRightSide, n.context());
		
		return t1;
	}

	@Override
	public Type visit(DivideAssignStatement n, Type t) {
		Path path = new Path(n.variablePath().path());

		Type t1 = t.shallowCopyExcept(path);

		Type typeOfLeftSide = this.synthesize(n.variablePath(), t);
		Type typeOfRightSide = this.synthesize(n.expression(), t);

		this.deriveTypeAndUpdateNode(path, t1, OperandType.DIVIDE, typeOfLeftSide, typeOfRightSide, n.context());
		
		return t1;
	}

	/**
	 * Derives the resulting type between the calculation of "leftSide opType rightSide" and updates the basictype of the node at the end of the given path to this result
	 * @param path the path to the node to update
	 * @param tree the type tree in which the node resides
	 * @param opType the type of the operand
	 * @param leftSide the left side of the calculation
	 * @param rightSide the right side of the calculation
	 * @param ctx the parsing context of the calculation
	 */
	private void deriveTypeAndUpdateNode(Path path, Type tree, OperandType opType, Type leftSide, Type rightSide, ParsingContext ctx){
		List<BasicTypeDefinition> newBasicTypes = BasicTypeUtils.deriveTypeOfOperation(opType, leftSide, rightSide, ctx);
		TypeUtils.setBasicTypeOfNodeByPath(path, newBasicTypes, tree);

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}
	}

	/**
	 * If statement
	 */
	public Type visit( IfStatement n, Type t ){
		ChoiceType resultType = new ChoiceType();

		for(Pair<OLSyntaxNode, OLSyntaxNode> p : n.children()){
			OLSyntaxNode expression = p.key();
			OLSyntaxNode body = p.value();

			Type typeOfEx = this.synthesize(expression, t);
			this.check(typeOfEx, Type.BOOL(), n.context(), "Guard of if-statement is not subtype of bool { ? }. Found type:\n" + typeOfEx.prettyString()); // check that expression is of type bool
			Type t1 = this.synthesize(body, t);
			resultType.addChoiceUnsafe(t1);
		}

		OLSyntaxNode elseProcess = n.elseProcess();
		if(elseProcess != null){ // there is an else clause
			resultType.addChoiceUnsafe(this.synthesize(elseProcess, t));
		}
		else{ // there is not else clause, here we add the initial state as choice as well, since we may not enter the if statement
			resultType.addChoiceUnsafe(t);
		}
		
		if(resultType.choices().size() == 1){
			return resultType.choices().get(0);
		}
			
		return resultType;
	}

	public Type visit( DefinitionCallStatement n, Type t ){
		return t;
	}

	/**
	 * While statement
	 */
	public Type visit( WhileStatement n, Type t ){
		this.pathsAlteredInWhile.push(new ArrayList<>());

		OLSyntaxNode condition = n.condition();
		OLSyntaxNode body = n.body();

		Type typeOfCondition = this.synthesize(condition, t);
		this.check(typeOfCondition, Type.BOOL(), n.context(), "Guard of while loop is not of type bool. Found type:\n" + typeOfCondition.prettyString()); // check that the initial condition is of type bool

		Type originalState = t; // saved here, since it is used in the fallback plan

		// the return type is a conjunction between the original state and the one found through the iterations OR the fallback
		ChoiceType result = new ChoiceType();
		result.addChoiceUnsafe(originalState);

		ChoiceType mergedState = new ChoiceType();
		mergedState.addChoiceUnsafe(originalState);

		for(int i = 0; i < 10; i++){
			Type r = this.synthesize(body, t); // synthesize the type of the body after an iteration
			typeOfCondition = this.synthesize(condition, r);
			this.check(typeOfCondition, Type.BOOL(), n.context(), "Guard of while loop is not of type bool. Found type:\n" + typeOfCondition.prettyString()); // check that the initial condition is of type bool
			
			if(r.isSubtypeOf(mergedState)){ // the new state is a subtype of one of the previous states (we have a steady state)
				result.addChoiceUnsafe(mergedState);
				this.pathsAlteredInWhile.pop();
				return result;
			}

			mergedState.addChoiceUnsafe(r);
			t = r;
		}

		// we did not find a steady state in the while loop. Here we do the fallback plan, which is to undefine all variables changed in the while loop
		result.addChoiceUnsafe( TypeUtils.undefine(originalState, this.pathsAlteredInWhile.peek()) );

		WarningHandler.throwWarning("could not determine the resulting type of the while loop, affected types may be incorrect from here", n.context());
		
		this.pathsAlteredInWhile.pop();
		return result.convertIfPossible();
	}

	public Type visit( OrConditionNode n, Type t ){
		return Type.BOOL();
	}

	public Type visit( AndConditionNode n, Type t ){
		return Type.BOOL();
	}

	public Type visit( NotExpressionNode n, Type t ){
		return Type.BOOL();
	}

	public Type visit( CompareConditionNode n, Type t ){
		return Type.BOOL();
	}

	public Type visit( ConstantIntegerExpression n, Type t ){
		return Type.INT();
	}

	public Type visit( ConstantDoubleExpression n, Type t ){
		return Type.DOUBLE();
	}

	public Type visit( ConstantBoolExpression n, Type t ){
		return Type.BOOL();
	}

	public Type visit( ConstantLongExpression n, Type t ){
		return Type.LONG();
	}

	public Type visit( ConstantStringExpression n, Type t ){
		return Type.STRING();
	}

	public Type visit( ProductExpressionNode n, Type t ){
		return this.getTypeOfSumOrProduct(n, t);
	}

	public Type visit( SumExpressionNode n, Type t ){
		return this.getTypeOfSumOrProduct(n, t);
	}

	private Type getTypeOfSumOrProduct(OLSyntaxNode n, Type t){
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
			Type nextType = this.synthesize(pair.value(), t);

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

	/**
	 * A variable path, such as x.y.z. Here we find the exact node at the end of the path and return it.
	 */
	public Type visit( VariableExpressionNode n, Type t ){
		Path path = new Path(n.variablePath().path());
		ArrayList<Type> types = TypeUtils.findNodesExact(path, t, false, false);

		if(types.isEmpty()){ // return void type if no nodes was found
			return Type.VOID();
		}
		else if(types.size() == 1){ // if only one node was found, return it
			return types.get(0);
		}
		else{ // if more nodes were found, return the disjunction between them
			return new ChoiceType(types);
		}
	}

	public Type visit( NullProcessStatement n, Type t ){
		return t;
	}

	public Type visit( Scope n, Type t ){
		return t;
	}

	public Type visit( InstallStatement n, Type t ){
		return t;
	}

	public Type visit( CompensateStatement n, Type t ){
		return t;
	}

	public Type visit( ThrowStatement n, Type t ){
		return t;
	}

	public Type visit( ExitStatement n, Type t ){
		return t;
	}

	public Type visit( ExecutionInfo n, Type t ){
		return t;
	}

	public Type visit( CorrelationSetInfo n, Type t ){
		return t;
	}

	public Type visit( InputPortInfo n, Type t ){
		return t;
	}

	public Type visit( OutputPortInfo n, Type t ){
		return t;
	}

	public Type visit( PointerStatement n, Type t ){
		return t;
	}

	/**
	 * Deep copy
	 */
	public Type visit( DeepCopyStatement n, Type t ){
		Path leftPath = new Path(n.leftPath().path());
		
		Type t1 = t.shallowCopyExcept(leftPath); // the node at the end of leftPath will be changed, so the entire path must be deep copied
		t1 = TypeUtils.unfold(leftPath, t1);

		ArrayList<InlineType> trees = new ArrayList<>(); // the trees of the initial state	
		if(t1 instanceof InlineType){ // only one tree
			trees.add((InlineType)t1);
		}
		else{ // case of disjunction, add all the choices as initial trees
			ChoiceType parsed = (ChoiceType)t1;
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

				Type resultOfDeepCopy = Type.deepCopy(child, typeOfExpression);
				parent.addChildUnsafe(childName, resultOfDeepCopy);
			}
		}

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(leftPath);
		}

		return t1;
	}

	public Type visit( RunStatement n, Type t ){
		return t;
	}

	/**
	 * Undef statement. Here we find the parent of the path and remove the child
	 */
	public Type visit( UndefStatement n, Type t ){
		Path path = new Path(n.variablePath().path());
		Type t1 = t.shallowCopyExcept(path);

		ArrayList<Pair<InlineType, String>> nodesToRemove = TypeUtils.findParentAndName(path, t1, false, false);

		for(Pair<InlineType, String> pair : nodesToRemove){
			pair.key().removeChildUnsafe(pair.value());
		}

		for(ArrayList<Path> a : this.pathsAlteredInWhile){
			a.add(path);
		}

		return t1;
	}

	public Type visit( ValueVectorSizeExpressionNode n, Type t ){
		return t;
	}

	public Type visit( PreIncrementStatement n, Type t ){
		return t;
	}

	public Type visit( PostIncrementStatement n, Type t ){
		return t;
	}

	public Type visit( PreDecrementStatement n, Type t ){
		return t;
	}

	public Type visit( PostDecrementStatement n, Type t ){
		return t;
	}

	public Type visit( ForStatement n, Type t ){
		return t;
	}

	public Type visit( ForEachSubNodeStatement n, Type t ){
		return t;
	}

	public Type visit( ForEachArrayItemStatement n, Type t ){
		return t;
	}

	public Type visit( SpawnStatement n, Type t ){
		return t;
	}

	public Type visit( IsTypeExpressionNode n, Type t ){
		return t;
	}

	public Type visit( InstanceOfExpressionNode n, Type t ){
		return Type.BOOL();
	}

	public Type visit( TypeCastExpressionNode n, Type t ){
		return new InlineType(BasicTypeDefinition.of(n.type()), null, null, false);
	}

	public Type visit( SynchronizedStatement n, Type t ){
		return t;
	}

	public Type visit( CurrentHandlerStatement n, Type t ){
		return t;
	}

	public Type visit( EmbeddedServiceNode n, Type t ){
		return t;
	}

	public Type visit( InstallFixedVariableExpressionNode n, Type t ){
		return t;
	}

	public Type visit( VariablePathNode n, Type t ){
		Path path = new Path(n.path());
		ArrayList<Type> types = TypeUtils.findNodesExact(path, t, false, false);

		if(types.size() == 1){
			return types.get(0);
		}
		else{
			return new ChoiceType(types);
		}
	}

	public Type visit( TypeDefinitionLink n, Type t ){
		return t;
	}

	public Type visit( InterfaceDefinition n, Type t ){
		return t;
	}

	public Type visit( DocumentationComment n, Type t ){
		return t;
	}

	public Type visit( FreshValueExpressionNode n, Type t ){
		return t;
	}

	public Type visit( CourierDefinitionNode n, Type t ){
		return t;
	}

	public Type visit( CourierChoiceStatement n, Type t ){
		return t;
	}

	public Type visit( NotificationForwardStatement n, Type t ){
		return t;
	}

	public Type visit( SolicitResponseForwardStatement n, Type t ){
		return t;
	}

	public Type visit( InterfaceExtenderDefinition n, Type t ){
		return t;
	}

	public Type visit( InlineTreeExpressionNode n, Type t ){
		return t;
	}

	public Type visit( VoidExpressionNode n, Type t ){
		return t;
	}

	public Type visit( ProvideUntilStatement n, Type t ){
		return t;
	}

	public Type visit( TypeChoiceDefinition n, Type t ){
		return t;
	}

	public Type visit( ImportStatement n, Type t ){
		return t;
	}

	public Type visit( ServiceNode n, Type t ){
		Type result;
		Type t1 = t;

		if(n.parameterConfiguration().isPresent()){
			Path path = new Path(n.parameterConfiguration().get().variablePath());
			Type typeOfParam = (Type)this.module.symbols().get(n.parameterConfiguration().get().type().name(), SymbolType.TYPE);
			t1 = t.shallowCopyExcept(path);

			TypeUtils.setTypeOfNodeByPath(path, typeOfParam, t1);
		}

		// synthesize the program of the service node
		this.service = (Service)this.module.symbols().get(n.name(), SymbolType.SERVICE);
		result = this.synthesize(n.program(), t1);
		this.service = null;

		return result;
	}

	public Type visit( EmbedServiceNode n, Type t ){
		return t;
	}

	/**
	 * Checks that the given t is a subtype of the given S. Throws a fault to FaultHandler if not.
	 * @param t the possible subtype
	 * @param s the possible supertype
	 * @param ctx the parsingcontext of the node where the subtype check happens
	 */
	public void check(Type t, Type s, ParsingContext ctx, String faultMessage){
		if(!t.isSubtypeOf(s)){
			FaultHandler.throwFault(new TypeFault(faultMessage, ctx), false);
		}
	}

	/**
	 * Checks that the given t is a subtype of the given S. Throws a fault to FaultHandler if not.
	 * @param t the possible subtype
	 * @param s the possible supertype
	 * @param ctx the parsingcontext of the node where the subtype check happens
	 * @param faultMessage the message to give the programmer in case of a fault
	 * @param terminate if true the execution of the type checker will stop, otherwise nothing happens
	 */
	public void check(Type t, Type s, ParsingContext ctx, String faultMessage, boolean terminate){
		if(!t.isSubtypeOf(s)){
			FaultHandler.throwFault(new TypeFault(faultMessage, ctx), terminate);
		}
	}

	/**
	 * Set the basic type of a node given a path and a t_e (type expression).
	 * @param t The current type tree for the program
	 * @param t_e A type from an expression
	 * @param path A path to a given node
	 * @return The new type tree for the program
	 */
	private Type setBasicTypeOfNode(Type t, Type t_e, Path path) {
		Type t1 = t.shallowCopyExcept(path);
		ArrayList<BasicTypeDefinition> basicTypes = Type.getBasicTypesOfNode(t_e);
		TypeUtils.setBasicTypeOfNodeByPath(path, basicTypes, t1);
		return t1;
	}

	/**
	 * Set the type of a node given a path and a new Type to insert.
	 * @param t The current type tree for the program
	 * @param t_in The new type to insert
	 * @param p_in The path to a givin node
	 * @return The new type tree for the program
	 */
	private Type setTypeOfNode(Type t, Type t_in, Path p_in) {
		Type t1 = t.shallowCopyExcept(p_in);
		TypeUtils.setTypeOfNodeByPath(p_in, t_in, t1);
		return t1;
	}

	/**
	 * This is a special case for notification operation statement where we are given a typehint
	 */
	private Type assertOperation(NotificationOperationStatement n, Type t) {
		if(!(n.outputExpression() instanceof InstanceOfExpressionNode)){
			FaultHandler.throwFault(new MiscFault("argument given to assert must be an instanceof expression", n.context()), true);
		}
		InstanceOfExpressionNode parsedNode = (InstanceOfExpressionNode)n.outputExpression();
		OLSyntaxNode expression = parsedNode.expression();

		if(!(expression instanceof VariableExpressionNode)){
			FaultHandler.throwFault(new MiscFault("first argument of instanceof must be a path to a variable", n.context()), true);
		}
		
		Type type = TypeConverter.convert(parsedNode.type(), this.module.symbols());
		Type typeOfEx = this.synthesize(expression, t);
		String nameOfExpression = ToString.of(expression);
		this.check(typeOfEx, type, n.context(), "the type of '" + nameOfExpression + "' is not a subtype of the typehint. Type of " + nameOfExpression + ":\n" + typeOfEx.prettyString() + "\n\nTypehint given:\n" + type.prettyString(), true);

		Path path = new Path(((VariableExpressionNode)expression).variablePath().path());
		Type t1 = t.shallowCopyExcept(path);
		TypeUtils.setTypeOfNodeByPath(path, type, t1);
		return t1;
	}
}
