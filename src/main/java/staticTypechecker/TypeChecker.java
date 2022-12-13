package staticTypechecker;

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

/**
 * Typechecker for a parsed Jolie abstract syntax tree. Works as a visitor and will visit each node in the provided tree.
 * 
 * @param <C> The type of the context carried along the visit
 * @param <R> The return type of the visit
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class TypeChecker<C, R> implements OLVisitor<C, R> {
	public R visit(Program p, C ctx){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public R visit(TypeInlineDefinition t, C ctx ){
		System.out.println("Basic type: " + t.basicType().nativeType() + "[" + t.cardinality().min() + ", " + t.cardinality().max() + "]");

		if(t.subTypes() != null){ // has subtypes
			for(Map.Entry<String, TypeDefinition> key : t.subTypes()){
				key.getValue().accept(this, null);
			}
		}

		return null;
	}

	public R visit(TypeNameDefinition t, C ctx){
		return null;
	}

	public R visit(TypeStructureDefinition t, C ctx){
		return null;
	}

	public R visit( OneWayOperationDeclaration decl, C ctx ){
		return null;
	};

	public R visit( RequestResponseOperationDeclaration decl, C ctx ){
		return null;
	};

	public R visit( DefinitionNode n, C ctx ){
		return null;
	};

	public R visit( ParallelStatement n, C ctx ){
		return null;
	};

	public R visit( SequenceStatement n, C ctx ){
		return null;
	};

	public R visit( NDChoiceStatement n, C ctx ){
		return null;
	};

	public R visit( OneWayOperationStatement n, C ctx ){
		return null;
	};

	public R visit( RequestResponseOperationStatement n, C ctx ){
		return null;
	};

	public R visit( NotificationOperationStatement n, C ctx ){
		return null;
	};

	public R visit( SolicitResponseOperationStatement n, C ctx ){
		return null;
	};

	public R visit( LinkInStatement n, C ctx ){
		return null;
	};

	public R visit( LinkOutStatement n, C ctx ){
		return null;
	};

	public R visit( AssignStatement n, C ctx ){
		return null;
	};

	public R visit( AddAssignStatement n, C ctx ){
		return null;
	};

	public R visit( SubtractAssignStatement n, C ctx ){
		return null;
	};

	public R visit( MultiplyAssignStatement n, C ctx ){
		return null;
	};

	public R visit( DivideAssignStatement n, C ctx ){
		return null;
	};

	public R visit( IfStatement n, C ctx ){
		return null;
	};

	public R visit( DefinitionCallStatement n, C ctx ){
		return null;
	};

	public R visit( WhileStatement n, C ctx ){
		return null;
	};

	public R visit( OrConditionNode n, C ctx ){
		return null;
	};

	public R visit( AndConditionNode n, C ctx ){
		return null;
	};

	public R visit( NotExpressionNode n, C ctx ){
		return null;
	};

	public R visit( CompareConditionNode n, C ctx ){
		return null;
	};

	public R visit( ConstantIntegerExpression n, C ctx ){
		return null;
	};

	public R visit( ConstantDoubleExpression n, C ctx ){
		return null;
	};

	public R visit( ConstantBoolExpression n, C ctx ){
		return null;
	};

	public R visit( ConstantLongExpression n, C ctx ){
		return null;
	};

	public R visit( ConstantStringExpression n, C ctx ){
		return null;
	};

	public R visit( ProductExpressionNode n, C ctx ){
		return null;
	};

	public R visit( SumExpressionNode n, C ctx ){
		return null;
	};

	public R visit( VariableExpressionNode n, C ctx ){
		return null;
	};

	public R visit( NullProcessStatement n, C ctx ){
		return null;
	};

	public R visit( Scope n, C ctx ){
		return null;
	};

	public R visit( InstallStatement n, C ctx ){
		return null;
	};

	public R visit( CompensateStatement n, C ctx ){
		return null;
	};

	public R visit( ThrowStatement n, C ctx ){
		return null;
	};

	public R visit( ExitStatement n, C ctx ){
		return null;
	};

	public R visit( ExecutionInfo n, C ctx ){
		return null;
	};

	public R visit( CorrelationSetInfo n, C ctx ){
		return null;
	};

	public R visit( InputPortInfo n, C ctx ){
		return null;
	};

	public R visit( OutputPortInfo n, C ctx ){
		return null;
	};

	public R visit( PointerStatement n, C ctx ){
		return null;
	};

	public R visit( DeepCopyStatement n, C ctx ){
		return null;
	};

	public R visit( RunStatement n, C ctx ){
		return null;
	};

	public R visit( UndefStatement n, C ctx ){
		return null;
	};

	public R visit( ValueVectorSizeExpressionNode n, C ctx ){
		return null;
	};

	public R visit( PreIncrementStatement n, C ctx ){
		return null;
	};

	public R visit( PostIncrementStatement n, C ctx ){
		return null;
	};

	public R visit( PreDecrementStatement n, C ctx ){
		return null;
	};

	public R visit( PostDecrementStatement n, C ctx ){
		return null;
	};

	public R visit( ForStatement n, C ctx ){
		return null;
	};

	public R visit( ForEachSubNodeStatement n, C ctx ){
		return null;
	};

	public R visit( ForEachArrayItemStatement n, C ctx ){
		return null;
	};

	public R visit( SpawnStatement n, C ctx ){
		return null;
	};

	public R visit( IsTypeExpressionNode n, C ctx ){
		return null;
	};

	public R visit( InstanceOfExpressionNode n, C ctx ){
		return null;
	};

	public R visit( TypeCastExpressionNode n, C ctx ){
		return null;
	};

	public R visit( SynchronizedStatement n, C ctx ){
		return null;
	};

	public R visit( CurrentHandlerStatement n, C ctx ){
		return null;
	};

	public R visit( EmbeddedServiceNode n, C ctx ){
		return null;
	};

	public R visit( InstallFixedVariableExpressionNode n, C ctx ){
		return null;
	};

	public R visit( VariablePathNode n, C ctx ){
		return null;
	};

	public R visit( TypeDefinitionLink n, C ctx ){
		return null;
	};

	public R visit( InterfaceDefinition n, C ctx ){
		return null;
	};

	public R visit( DocumentationComment n, C ctx ){
		return null;
	};

	public R visit( FreshValueExpressionNode n, C ctx ){
		return null;
	};

	public R visit( CourierDefinitionNode n, C ctx ){
		return null;
	};

	public R visit( CourierChoiceStatement n, C ctx ){
		return null;
	};

	public R visit( NotificationForwardStatement n, C ctx ){
		return null;
	};

	public R visit( SolicitResponseForwardStatement n, C ctx ){
		return null;
	};

	public R visit( InterfaceExtenderDefinition n, C ctx ){
		return null;
	};

	public R visit( InlineTreeExpressionNode n, C ctx ){
		return null;
	};

	public R visit( VoidExpressionNode n, C ctx ){
		return null;
	};

	public R visit( ProvideUntilStatement n, C ctx ){
		return null;
	};

	public R visit( TypeChoiceDefinition n, C ctx ){
		return null;
	};

	public R visit( ImportStatement n, C ctx ){
		return null;
	};

	public R visit( ServiceNode n, C ctx ){
		return null;
	};

	public R visit( EmbedServiceNode n, C ctx ){
		return null;
	};

	public R visit( SolicitResponseExpressionNode n, C ctx ){
		return null;
	};

	public R visit( IfExpressionNode n, C Ctx ){
		return null;
	};
}
