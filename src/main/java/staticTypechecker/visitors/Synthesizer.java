package staticTypechecker.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.utils.BasicTypeUtils;
import staticTypechecker.utils.TreeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.Path;
import staticTypechecker.faults.FaultHandler;
import staticTypechecker.faults.WarningHandler;

/**
 * Synthesizer for a parsed Jolie abstract syntax tree. Synthesizes the type of each node
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Synthesizer implements OLVisitor<Type, Type> {
	private static HashMap<String, Synthesizer> synths = new HashMap<>(); // maps module name to synthesizer
	
	public static Synthesizer get(Module module){
		Synthesizer ret = Synthesizer.synths.get(module.name());

		if(ret == null){
			ret = new Synthesizer(module);
			Synthesizer.synths.put(module.name(), ret);
		}
	
		return ret;
	}

	private Module module;
	
	private Synthesizer(Module module){
		this.module = module;
	}

	public Type synthesize(OLSyntaxNode node, Type T){
		return node.accept(this, T);
	}

	public Type visit(Program p, Type T){
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
		return T;
	};

	public Type visit( ParallelStatement n, Type T ){
		return T;
	};

	public Type visit( SequenceStatement n, Type T ){
		Type T1 = T.copy();

		for(OLSyntaxNode child : n.children()){
			T1 = child.accept(this, T1);
		}

		return T1;
	};

	public Type visit( NDChoiceStatement n, Type T ){
		ArrayList<Type> trees = new ArrayList<>(n.children().size()); // save all the possible outcomes

		for(int i = 0; i < n.children().size(); i++){
			Type T1 = n.children().get(i).key().accept(this, T); // synthesize the label (in the [])
			Type T2 = n.children().get(i).value().accept(this, T1); // synthesize the behaviour (in the {})
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
		Operation op = (Operation)this.module.symbols().get(n.id());

		Type T_in = (Type)this.module.symbols().get(op.requestType()); // the data type which is EXPECTED by the oneway operation
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given as input to the operation

		// update the node at the path to the input type
		Type T1 = T.copy();
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

		return T1;
	};

	public Type visit( RequestResponseOperationStatement n, Type T ){
		Operation op = (Operation)this.module.symbols().get(n.id());
		
		Type T_in = (Type)this.module.symbols().get(op.requestType()); // the type of the data the operation EXPECTS as input
		Type T_out = (Type)this.module.symbols().get(op.responseType()); // the type of the data RETURNED from the operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given AS INPUT to the operation
		Path p_out = new Path(n.outputExpression().toString()); // the path to the node in which the OUTPUT of the operation is stored

		// given that p_in is of type T_in find the type of the behaviour
		Type T_update = T.copy();
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T_update);
		Type T1 = n.process().accept(this, T_update);
		
		// check that p_out is a subtype of T_out 
		ArrayList<Type> possibleTypes = TreeUtils.findNodesExact(p_out, T1, true, false); // the possible types of p_out after the behaviour
		
		Type p_out_type;
		if(possibleTypes.size() == 1){
			p_out_type = possibleTypes.get(0);
		}
		else{
			p_out_type = new ChoiceType(possibleTypes);
		}

		this.check(p_out_type, T_out, n.context());

		return T1;
	};

	public Type visit( NotificationOperationStatement n, Type T ){
		Operation op = (Operation)this.module.symbols().get(n.id());
		
		Type T_out = (Type)this.module.symbols().get(op.requestType()); // the type of the data which is EXPECTED of the oneway operation
		Type p_out = n.outputExpression().accept(this, T); // the type which is GIVEN to the oneway operation

		this.check(p_out, T_out, n.context());

		return T;
	};

	public Type visit( SolicitResponseOperationStatement n, Type T ){
		Operation op = (Operation)this.module.symbols().get(n.id());

		Type T_in = (Type)this.module.symbols().get(op.responseType()); // the type of the data which is RETURNED by the reqres operation
		Type T_out = (Type)this.module.symbols().get(op.requestType()); // the type of the data which is EXPECTED of the reqres operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node in which to store the returned data
		Type p_out = n.outputExpression().accept(this, T); // the type which is GIVEN to the reqres operation
		
		// check that p_out is subtype of T_out
		this.check(p_out, T_out, n.context());

		// update type of p_in to T_in
		Type T1 = T.copy();
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

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
		Type T_e = e.accept(this, T);
		
		// update the type of the node
		Type T1 = T.copy();
		Path path = new Path(n.variablePath().path());
		ArrayList<BasicTypeDefinition> basicTypes = Type.getBasicTypesOfNode(T_e);
		TreeUtils.setBasicTypeOfNodeByPath(path, basicTypes, T1);

		return T1;
	};

	@Override
	public Type visit(AddAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.copy();
		
		Type typeOfRightSide = n.variablePath().accept(this, T);
		Type typeOfExpression = n.expression().accept(this, T);

		Type newType = BasicTypeUtils.deriveTypeOfOperation(OperandType.ADD, typeOfRightSide, typeOfExpression, n.context());
		TreeUtils.setTypeOfNodeByPath(path, newType, T1);

		return T1;
	}

	@Override
	public Type visit(SubtractAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.copy();

		Type typeOfRightSide = n.variablePath().accept(this, T);
		Type typeOfExpression = n.expression().accept(this, T);

		Type newType = BasicTypeUtils.deriveTypeOfOperation(OperandType.SUBTRACT, typeOfRightSide, typeOfExpression, n.context());
		TreeUtils.setTypeOfNodeByPath(path, newType, T1);
		
		return T1;
	}

	@Override
	public Type visit(MultiplyAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.copy();

		Type typeOfRightSide = n.variablePath().accept(this, T);
		Type typeOfExpression = n.expression().accept(this, T);

		Type newType = BasicTypeUtils.deriveTypeOfOperation(OperandType.MULTIPLY, typeOfRightSide, typeOfExpression, n.context());
		TreeUtils.setTypeOfNodeByPath(path, newType, T1);
		
		return T1;
	}

	@Override
	public Type visit(DivideAssignStatement n, Type T) {
		Path path = new Path(n.variablePath().path());

		Type T1 = T.copy();

		Type typeOfRightSide = n.variablePath().accept(this, T);
		Type typeOfExpression = n.expression().accept(this, T);

		Type newType = BasicTypeUtils.deriveTypeOfOperation(OperandType.DIVIDE, typeOfRightSide, typeOfExpression, n.context());
		TreeUtils.setTypeOfNodeByPath(path, newType, T1);
		
		return T1;
	}

	public Type visit( IfStatement n, Type T ){
		ChoiceType resultType = new ChoiceType();

		for(Pair<OLSyntaxNode, OLSyntaxNode> p : n.children()){
			OLSyntaxNode expression = p.key();
			OLSyntaxNode body = p.value();

			if(!(expression instanceof InstanceOfExpressionNode)){ // COND-1, e is an expression of anything else than instanceof
				this.check(T, expression, Type.BOOL()); // check that expression is of type bool
				Type T1 = body.accept(this, T);
				resultType.addChoiceUnsafe(T1);
			}
		}

		OLSyntaxNode elseProcess = n.elseProcess();
		if(elseProcess != null){ // there is an else clause
			resultType.addChoiceUnsafe(elseProcess.accept(this, T));
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

	/**
	 * TODO, not finished yet
	 */
	public Type visit( WhileStatement n, Type T ){
		OLSyntaxNode condition = n.condition();
		OLSyntaxNode body = n.body();
		Type originalState = T; // used in fallback

		Type state = T; // state is the final state of the while loop. For each iteration it is merged with the resulting state of that iteration, hopefully resulting in a merged state of all the iterations

		// in this loop, T is the state of the previous iteration, and R is the state resulting from the current iteration
		for(int i = 0; i < 3; i++){
			// System.out.println("-------------- ITERATION " + i + " -----------------" );
			// System.out.println("T:\n" + T.prettyString() + "\n");
			this.check(T, condition, Type.BOOL()); // check that the condition is of type bool
			Type R = body.accept(this, T); // synthesize the type of the body after an iteration
			// System.out.println("R:\n" + R.prettyString() + "\n");
			
			if(R.isSubtypeOf(state)){ // the new state is subtype of the merged state, return the merged state
				System.out.println("subtype!");
				return state;
			}

			state = Type.merge(state, R);
			// System.out.println("merged state:\n" + state.prettyString() + "\n");
			T = R;
		}

		// we did not find a merged state to cover all cases of the while loop. Here we do the fallback plan, which is to undefine all variables changed in the while loop
		// TODO throw warning
		WarningHandler.throwWarning("could not determine the resulting type of the while loop, affected types may be incorrect from here", n.context());

		// TODO fallback plan
		System.out.println("FALLBACK");
		return TreeUtils.undefine(originalState, state);
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
		Type typeOfSum = Type.VOID();  // set initial type to void to make sure it will be overwritten by any other type

		List<Pair<OperandType, OLSyntaxNode>> operands = n.operands();
		for(Pair<OperandType, OLSyntaxNode> pair : operands){
			OperandType currOp = pair.key();
			OLSyntaxNode currTerm = pair.value();
			Type typeOfCurrTerm = currTerm.accept(this, T);

			typeOfSum = BasicTypeUtils.deriveTypeOfOperation(currOp, typeOfSum, typeOfCurrTerm, n.context());
		}

		return typeOfSum;
	};

	public Type visit( SumExpressionNode n, Type T ){
		Type typeOfSum = Type.VOID();  // set initial type to void to make sure it will be overwritten by any other type

		List<Pair<OperandType, OLSyntaxNode>> operands = n.operands();
		for(Pair<OperandType, OLSyntaxNode> pair : operands){
			OperandType currOp = pair.key();
			OLSyntaxNode currTerm = pair.value();
			Type typeOfCurrTerm = currTerm.accept(this, T);

			typeOfSum = BasicTypeUtils.deriveTypeOfOperation(currOp, typeOfSum, typeOfCurrTerm, n.context());
		}

		return typeOfSum;
	};

	public Type visit( VariableExpressionNode n, Type T ){
		Path path = new Path(n.variablePath().path());
		ArrayList<Type> types = TreeUtils.findNodesExact(path, T, false, false);

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
		Type T1 = T.copy();

		Path leftPath = new Path(n.leftPath().path());
		OLSyntaxNode expression = n.rightExpression();
		Type typeOfExpression = expression.accept(this, T1);
		
		// find the nodes to update and their parents
		T1 = TreeUtils.unfold(leftPath, T1);
		ArrayList<Pair<InlineType, String>> leftSideNodes = TreeUtils.findParentAndName(leftPath, T1, true, false);

		// update the nodes with the deep copied versions
		for(Pair<InlineType, String> pair : leftSideNodes){
			InlineType parent = pair.key();
			String childName = pair.value();
			Type child = parent.getChild(childName);

			// check child for null, since we do not create it in the findParentAndName method here. If it is null, it means that it did not exist before, and we can use the void type
			if(child == null){
				child = Type.VOID();
			}

			TreeUtils.fold(child);
			Type resultOfDeepCopy = Type.deepCopy(child, typeOfExpression);

			parent.addChildUnsafe(childName, resultOfDeepCopy);
		}

		return T1;
	};

	public Type visit( RunStatement n, Type T ){
		return T;
	};

	public Type visit( UndefStatement n, Type T ){
		Path path = new Path(n.variablePath().path());
		Type T1 = T.copy();

		ArrayList<Pair<InlineType, String>> nodesToRemove = TreeUtils.findParentAndName(path, T1, false, false);

		for(Pair<InlineType, String> pair : nodesToRemove){
			pair.key().removeChildUnsafe(pair.value());
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
		return T;
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
		ArrayList<Type> types = TreeUtils.findNodesExact(path, T, false, false);

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
		return T;
	};

	public Type visit( EmbedServiceNode n, Type T ){
		return T;
	};

	/**
	 * Checks that the type of the node, n, is a subtype of S
	 * @param T the tree in which n resides
	 * @param n the node to check the type of
	 * @param S the type of which the type of n must be a subtype
	 */
	public void check(Type T, OLSyntaxNode n, Type S){
		Type typeOfN = n.accept(this, T);
		if(!typeOfN.isSubtypeOf(S)){
			FaultHandler.throwFault("The type:\n\t" + typeOfN.prettyString(1) + "\n\nis not a subtype of the type:\n\t" + S.prettyString(1), n.context());
		}
	}

	public void check(Type T, Type S, ParsingContext ctx){
		if(!T.isSubtypeOf(S)){
			FaultHandler.throwFault("The type:\n\t" + T.prettyString(1) + "\n\nis not a subtype of the type:\n\t" + S.prettyString(1), ctx);
		}
	}
}
