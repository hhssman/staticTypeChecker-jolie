package staticTypechecker.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Pair;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.utils.TreeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.Path;

/**
 * Synthesizer for a parsed Jolie abstract syntax T. Works as a visitor and will visit each node in the provided T.
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Synthesizer implements OLVisitor<InlineType, InlineType> {
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

	public InlineType synthesize(OLSyntaxNode node, InlineType T){
		return node.accept(this, T);
	}

	public InlineType visit(Program p, InlineType T){
		InlineType T1 = T;

		for(OLSyntaxNode n : p.children()){
			T1 = n.accept(this, T1);
		}

		return T1;
	}

	public InlineType visit(TypeInlineDefinition t, InlineType T){
		return null;
	}

	public InlineType visit( OneWayOperationDeclaration decl, InlineType T ){
		return null;
	};

	public InlineType visit( RequestResponseOperationDeclaration decl, InlineType T ){
		return null;
	};

	public InlineType visit( DefinitionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( ParallelStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( SequenceStatement n, InlineType T ){
		InlineType T1 = T;

		for(OLSyntaxNode child : n.children()){
			T1 = child.accept(this, T1);
		}

		return T1;
	};

	public InlineType visit( NDChoiceStatement n, InlineType T ){
		InlineType[] trees = new InlineType[n.children().size()]; // save all the possible outcomes

		for(int i = 0; i < n.children().size(); i++){
			System.out.println("process child: " + n.children().get(i).value());
			InlineType T1 = n.children().get(i).key().accept(this, T); // synthesize the label (in the [])
			InlineType T2 = n.children().get(i).value().accept(this, T1); // synthesize the behaviour (in the {})
			trees[i] = T2;
		}
		
		if(trees.length == 1){
			System.out.println("only one tree");
			System.out.println(trees[0]);
			return trees[0];
		}
		else{
			System.out.println(trees.length + " tree(s)");
			System.out.println(Type.union(trees));
			return (InlineType)Type.union(trees);
		}
	};

	public InlineType visit( OneWayOperationStatement n, InlineType T ){
		Operation op = (Operation)this.module.symbols().get(n.id());

		Type T_in = (Type)this.module.symbols().get(op.requestType()); // the data type which is EXPECTED by the oneway operation
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given as input to the operation

		// update the node at the path to the input type
		InlineType T1 = T.copy(false);
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

		return T1;
	};

	public InlineType visit( RequestResponseOperationStatement n, InlineType T ){
		Operation op = (Operation)this.module.symbols().get(n.id());
		
		Type T_in = (Type)this.module.symbols().get(op.requestType()); // the type of the data the operation EXPECTS as input
		Type T_out = (Type)this.module.symbols().get(op.responseType()); // the type of the data RETURNED from the operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node which is given AS INPUT to the operation
		Path p_out = new Path(n.outputExpression().toString()); // the path to the node in which the OUTPUT of the operation is stored

		// given that p_in is of type T_in find the type of the behaviour
		InlineType T_update = T.copy(false);
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T_update);
		InlineType T1 = n.process().accept(this, T_update);
		
		// check that p_out is a subtype of T_out 
		ArrayList<Type> possibleTypes = TreeUtils.findNodesExact(p_out, T1, true); // the possible types of the output of the behaviour
		
		Type p_out_type;
		if(possibleTypes.size() == 1){
			p_out_type = possibleTypes.get(0);
		}
		else{
			p_out_type = new ChoiceType(possibleTypes);
		}

		Checker.get(this.module).check(p_out_type, T_out);

		return T1;
	};

	public InlineType visit( NotificationOperationStatement n, InlineType T ){
		Operation op = (Operation)this.module.symbols().get(n.id());
		
		Type T_out = (Type)this.module.symbols().get(op.requestType()); // the type of the data which is EXPECTED of the oneway operation
		Type p_out = TreeUtils.getTypeOfExpression(n.outputExpression(), T); // the type which is GIVEN to the oneway operation

		Checker.get(this.module).check(p_out, T_out);

		return T;
	};

	public InlineType visit( SolicitResponseOperationStatement n, InlineType T ){
		Operation op = (Operation)this.module.symbols().get(n.id());

		Type T_in = (Type)this.module.symbols().get(op.responseType()); // the type of the data which is RETURNED by the reqres operation
		Type T_out = (Type)this.module.symbols().get(op.requestType()); // the type of the data which is EXPECTED of the reqres operation
		
		Path p_in = new Path(n.inputVarPath().path()); // the path to the node in which to store the returned data
		Type p_out = TreeUtils.getTypeOfExpression(n.outputExpression(), T); // the type which is GIVEN to the reqres operation
		
		// check that p_out is subtype of T_out
		Checker.get(this.module).check(p_out, T_out);

		// update type of p_in to T_in
		InlineType T1 = T.copy(false);
		TreeUtils.setTypeOfNodeByPath(p_in, T_in, T1);

		return T1;
	};

	public InlineType visit( LinkInStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( LinkOutStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( AssignStatement n, InlineType T ){
		// retrieve the type of the expression
		Path path = new Path(n.variablePath().path());
		OLSyntaxNode e = n.expression();
		Type T_e = TreeUtils.getTypeOfExpression(e, T);

		// update the type of the node
		InlineType T1 = T.copy(false);
		TreeUtils.setTypeOfNodeByPath(path, T_e, T1);

		return T1;
	};

	@Override
	public InlineType visit(AddAssignStatement n, InlineType T) {
		Path path = new Path(n.variablePath().path());

		InlineType T1 = T.copy(false);
		TreeUtils.handleOperationAssignment(path, OperandType.ADD, n.expression(), T1);

		return T1;
	}

	@Override
	public InlineType visit(SubtractAssignStatement n, InlineType T) {
		Path path = new Path(n.variablePath().path());

		InlineType T1 = T.copy(false);
		TreeUtils.handleOperationAssignment(path, OperandType.SUBTRACT, n.expression(), T1);
		
		return T1;
	}

	@Override
	public InlineType visit(MultiplyAssignStatement n, InlineType T) {
		Path path = new Path(n.variablePath().path());

		InlineType T1 = T.copy(false);
		TreeUtils.handleOperationAssignment(path, OperandType.MULTIPLY, n.expression(), T1);
		
		return T1;
	}

	@Override
	public InlineType visit(DivideAssignStatement n, InlineType T) {
		Path path = new Path(n.variablePath().path());

		InlineType T1 = T.copy(false);
		TreeUtils.handleOperationAssignment(path, OperandType.DIVIDE, n.expression(), T1);
		
		return T1;
	}

	public InlineType visit( IfStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( DefinitionCallStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( WhileStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( OrConditionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( AndConditionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( NotExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( CompareConditionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( ConstantIntegerExpression n, InlineType T ){
		return null;
	};

	public InlineType visit( ConstantDoubleExpression n, InlineType T ){
		return null;
	};

	public InlineType visit( ConstantBoolExpression n, InlineType T ){
		return null;
	};

	public InlineType visit( ConstantLongExpression n, InlineType T ){
		return null;
	};

	public InlineType visit( ConstantStringExpression n, InlineType T ){
		return null;
	};

	public InlineType visit( ProductExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( SumExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( VariableExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( NullProcessStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( Scope n, InlineType T ){
		return null;
	};

	public InlineType visit( InstallStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( CompensateStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ThrowStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ExitStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ExecutionInfo n, InlineType T ){
		return null;
	};

	public InlineType visit( CorrelationSetInfo n, InlineType T ){
		return null;
	};

	public InlineType visit( InputPortInfo n, InlineType T ){
		return null;
	};

	public InlineType visit( OutputPortInfo n, InlineType T ){
		return null;
	};

	public InlineType visit( PointerStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( DeepCopyStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( RunStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( UndefStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ValueVectorSizeExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( PreIncrementStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( PostIncrementStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( PreDecrementStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( PostDecrementStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ForStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ForEachSubNodeStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ForEachArrayItemStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( SpawnStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( IsTypeExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( InstanceOfExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( TypeCastExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( SynchronizedStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( CurrentHandlerStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( EmbeddedServiceNode n, InlineType T ){
		return null;
	};

	public InlineType visit( InstallFixedVariableExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( VariablePathNode n, InlineType T ){
		return null;
	};

	public InlineType visit( TypeDefinitionLink n, InlineType T ){
		return null;
	};

	public InlineType visit( InterfaceDefinition n, InlineType T ){
		return null;
	};

	public InlineType visit( DocumentationComment n, InlineType T ){
		return null;
	};

	public InlineType visit( FreshValueExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( CourierDefinitionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( CourierChoiceStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( NotificationForwardStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( SolicitResponseForwardStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( InterfaceExtenderDefinition n, InlineType T ){
		return null;
	};

	public InlineType visit( InlineTreeExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( VoidExpressionNode n, InlineType T ){
		return null;
	};

	public InlineType visit( ProvideUntilStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( TypeChoiceDefinition n, InlineType T ){
		return null;
	};

	public InlineType visit( ImportStatement n, InlineType T ){
		return null;
	};

	public InlineType visit( ServiceNode n, InlineType T ){
		return null;
	};

	public InlineType visit( EmbedServiceNode n, InlineType T ){
		return null;
	};
}
