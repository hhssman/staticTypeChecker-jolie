package staticTypechecker.visitors;

import java.util.HashMap;
import java.util.Map;

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
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.utils.Bisimulator;
import staticTypechecker.entities.Module;;

/**
 * Typechecker for a parsed Jolie abstract syntax tree. Works as a visitor and will visit each node in the provided tree.
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Checker implements OLVisitor<Type, Void> {
	private static HashMap<String, Checker> checkers = new HashMap<>(); // maps module name to checker
	
	public static Checker get(Module module){
		Checker ret = Checker.checkers.get(module.name());
		
		if(ret == null){
			ret = new Checker(module);
			Checker.checkers.put(module.name(), ret);
		}
	
		return ret;
	}
	
	private Module module;
	
	private Checker(Module module){
		this.module = module;
	}

	/**
	 * Check that the type of the given OLSyntaxNode is a subtype of the given type. Throws an error if not
	 * @param T
	 * @param node
	 * @param type
	 */
	public void check(Type T, OLSyntaxNode node, Type type){
		node.accept(this, null);
	} 

	/**
	 * Check thtat the given type is a subtype of the other given type. Throws an error if not
	 * @param T
	 * @param node
	 * @param type
	 */
	public void check(Type type1, Type type2){
		Bisimulator.isSubtypeOf(type1, type2);
	} 

	public Void visit(Program p, Type T){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public Void visit(TypeInlineDefinition t, Type T){
		System.out.println("Basic type: " + t.basicType().nativeType() + "[" + t.cardinality().min() + ", " + t.cardinality().max() + "]");

		if(t.subTypes() != null){ // has subtypes
			for(Map.Entry<String, TypeDefinition> key : t.subTypes()){
				key.getValue().accept(this, null);
			}
		}

		return null;
	}

	public Void visit(Type t, Type T){
		return null;
	}

	public Void visit( OneWayOperationDeclaration decl, Type T ){
		return null;
	};

	public Void visit( RequestResponseOperationDeclaration decl, Type T ){
		return null;
	};

	public Void visit( DefinitionNode n, Type T ){
		return null;
	};

	public Void visit( ParallelStatement n, Type T ){
		return null;
	};

	public Void visit( SequenceStatement n, Type T ){
		return null;
	};

	public Void visit( NDChoiceStatement n, Type T ){
		return null;
	};

	public Void visit( OneWayOperationStatement n, Type T ){
		return null;
	};

	public Void visit( RequestResponseOperationStatement n, Type T ){
		return null;
	};

	public Void visit( NotificationOperationStatement n, Type T ){
		return null;
	};

	public Void visit( SolicitResponseOperationStatement n, Type T ){
		return null;
	};

	public Void visit( LinkInStatement n, Type T ){
		return null;
	};

	public Void visit( LinkOutStatement n, Type T ){
		return null;
	};

	public Void visit( AssignStatement n, Type T ){
		return null;
	};

	public Void visit( AddAssignStatement n, Type T ){
		return null;
	};

	public Void visit( SubtractAssignStatement n, Type T ){
		return null;
	};

	public Void visit( MultiplyAssignStatement n, Type T ){
		return null;
	};

	public Void visit( DivideAssignStatement n, Type T ){
		return null;
	};

	public Void visit( IfStatement n, Type T ){
		return null;
	};

	public Void visit( DefinitionCallStatement n, Type T ){
		return null;
	};

	public Void visit( WhileStatement n, Type T ){
		return null;
	};

	public Void visit( OrConditionNode n, Type T ){
		return null;
	};

	public Void visit( AndConditionNode n, Type T ){
		return null;
	};

	public Void visit( NotExpressionNode n, Type T ){
		return null;
	};

	public Void visit( CompareConditionNode n, Type T ){
		return null;
	};

	public Void visit( ConstantIntegerExpression n, Type T ){
		return null;
	};

	public Void visit( ConstantDoubleExpression n, Type T ){
		return null;
	};

	public Void visit( ConstantBoolExpression n, Type T ){
		return null;
	};

	public Void visit( ConstantLongExpression n, Type T ){
		return null;
	};

	public Void visit( ConstantStringExpression n, Type T ){
		return null;
	};

	public Void visit( ProductExpressionNode n, Type T ){
		return null;
	};

	public Void visit( SumExpressionNode n, Type T ){
		return null;
	};

	public Void visit( VariableExpressionNode n, Type T ){
		return null;
	};

	public Void visit( NullProcessStatement n, Type T ){
		return null;
	};

	public Void visit( Scope n, Type T ){
		return null;
	};

	public Void visit( InstallStatement n, Type T ){
		return null;
	};

	public Void visit( CompensateStatement n, Type T ){
		return null;
	};

	public Void visit( ThrowStatement n, Type T ){
		return null;
	};

	public Void visit( ExitStatement n, Type T ){
		return null;
	};

	public Void visit( ExecutionInfo n, Type T ){
		return null;
	};

	public Void visit( CorrelationSetInfo n, Type T ){
		return null;
	};

	public Void visit( InputPortInfo n, Type T ){
		return null;
	};

	public Void visit( OutputPortInfo n, Type T ){
		return null;
	};

	public Void visit( PointerStatement n, Type T ){
		return null;
	};

	public Void visit( DeepCopyStatement n, Type T ){
		return null;
	};

	public Void visit( RunStatement n, Type T ){
		return null;
	};

	public Void visit( UndefStatement n, Type T ){
		return null;
	};

	public Void visit( ValueVectorSizeExpressionNode n, Type T ){
		return null;
	};

	public Void visit( PreIncrementStatement n, Type T ){
		return null;
	};

	public Void visit( PostIncrementStatement n, Type T ){
		return null;
	};

	public Void visit( PreDecrementStatement n, Type T ){
		return null;
	};

	public Void visit( PostDecrementStatement n, Type T ){
		return null;
	};

	public Void visit( ForStatement n, Type T ){
		return null;
	};

	public Void visit( ForEachSubNodeStatement n, Type T ){
		return null;
	};

	public Void visit( ForEachArrayItemStatement n, Type T ){
		return null;
	};

	public Void visit( SpawnStatement n, Type T ){
		return null;
	};

	public Void visit( IsTypeExpressionNode n, Type T ){
		return null;
	};

	public Void visit( InstanceOfExpressionNode n, Type T ){
		return null;
	};

	public Void visit( TypeCastExpressionNode n, Type T ){
		return null;
	};

	public Void visit( SynchronizedStatement n, Type T ){
		return null;
	};

	public Void visit( CurrentHandlerStatement n, Type T ){
		return null;
	};

	public Void visit( EmbeddedServiceNode n, Type T ){
		return null;
	};

	public Void visit( InstallFixedVariableExpressionNode n, Type T ){
		return null;
	};

	public Void visit( VariablePathNode n, Type T ){
		return null;
	};

	public Void visit( TypeDefinitionLink n, Type T ){
		return null;
	};

	public Void visit( InterfaceDefinition n, Type T ){
		return null;
	};

	public Void visit( DocumentationComment n, Type T ){
		return null;
	};

	public Void visit( FreshValueExpressionNode n, Type T ){
		return null;
	};

	public Void visit( CourierDefinitionNode n, Type T ){
		return null;
	};

	public Void visit( CourierChoiceStatement n, Type T ){
		return null;
	};

	public Void visit( NotificationForwardStatement n, Type T ){
		return null;
	};

	public Void visit( SolicitResponseForwardStatement n, Type T ){
		return null;
	};

	public Void visit( InterfaceExtenderDefinition n, Type T ){
		return null;
	};

	public Void visit( InlineTreeExpressionNode n, Type T ){
		return null;
	};

	public Void visit( VoidExpressionNode n, Type T ){
		return null;
	};

	public Void visit( ProvideUntilStatement n, Type T ){
		return null;
	};

	public Void visit( TypeChoiceDefinition n, Type T ){
		return null;
	};

	public Void visit( ImportStatement n, Type T ){
		return null;
	};

	public Void visit( ServiceNode n, Type T ){
		return null;
	};

	public Void visit( EmbedServiceNode n, Type T ){
		return null;
	};
}
