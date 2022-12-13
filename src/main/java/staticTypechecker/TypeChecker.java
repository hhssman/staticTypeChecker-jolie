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
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class TypeChecker implements OLVisitor<String, Void> {
	public Void visit(Program p, String ctx){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public Void visit(TypeInlineDefinition t, String ctx ){
		System.out.println("Basic type: " + t.basicType().nativeType());
		System.out.println("Cardinality: " + t.cardinality().min() + "-" + t.cardinality().max());

		if(t.subTypes() != null){ // has subtypes
			for(Map.Entry<String, TypeDefinition> key : t.subTypes()){
				key.getValue().accept(this, null);
			}
		}

		return null;
	}

	public Void visit( OneWayOperationDeclaration decl, String ctx ){
		return null;
	};

	public Void visit( RequestResponseOperationDeclaration decl, String ctx ){
		return null;
	};

	public Void visit( DefinitionNode n, String ctx ){
		return null;
	};

	public Void visit( ParallelStatement n, String ctx ){
		return null;
	};

	public Void visit( SequenceStatement n, String ctx ){
		return null;
	};

	public Void visit( NDChoiceStatement n, String ctx ){
		return null;
	};

	public Void visit( OneWayOperationStatement n, String ctx ){
		return null;
	};

	public Void visit( RequestResponseOperationStatement n, String ctx ){
		return null;
	};

	public Void visit( NotificationOperationStatement n, String ctx ){
		return null;
	};

	public Void visit( SolicitResponseOperationStatement n, String ctx ){
		return null;
	};

	public Void visit( LinkInStatement n, String ctx ){
		return null;
	};

	public Void visit( LinkOutStatement n, String ctx ){
		return null;
	};

	public Void visit( AssignStatement n, String ctx ){
		return null;
	};

	public Void visit( AddAssignStatement n, String ctx ){
		return null;
	};

	public Void visit( SubtractAssignStatement n, String ctx ){
		return null;
	};

	public Void visit( MultiplyAssignStatement n, String ctx ){
		return null;
	};

	public Void visit( DivideAssignStatement n, String ctx ){
		return null;
	};

	public Void visit( IfStatement n, String ctx ){
		return null;
	};

	public Void visit( DefinitionCallStatement n, String ctx ){
		return null;
	};

	public Void visit( WhileStatement n, String ctx ){
		return null;
	};

	public Void visit( OrConditionNode n, String ctx ){
		return null;
	};

	public Void visit( AndConditionNode n, String ctx ){
		return null;
	};

	public Void visit( NotExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( CompareConditionNode n, String ctx ){
		return null;
	};

	public Void visit( ConstantIntegerExpression n, String ctx ){
		return null;
	};

	public Void visit( ConstantDoubleExpression n, String ctx ){
		return null;
	};

	public Void visit( ConstantBoolExpression n, String ctx ){
		return null;
	};

	public Void visit( ConstantLongExpression n, String ctx ){
		return null;
	};

	public Void visit( ConstantStringExpression n, String ctx ){
		return null;
	};

	public Void visit( ProductExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( SumExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( VariableExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( NullProcessStatement n, String ctx ){
		return null;
	};

	public Void visit( Scope n, String ctx ){
		return null;
	};

	public Void visit( InstallStatement n, String ctx ){
		return null;
	};

	public Void visit( CompensateStatement n, String ctx ){
		return null;
	};

	public Void visit( ThrowStatement n, String ctx ){
		return null;
	};

	public Void visit( ExitStatement n, String ctx ){
		return null;
	};

	public Void visit( ExecutionInfo n, String ctx ){
		return null;
	};

	public Void visit( CorrelationSetInfo n, String ctx ){
		return null;
	};

	public Void visit( InputPortInfo n, String ctx ){
		return null;
	};

	public Void visit( OutputPortInfo n, String ctx ){
		return null;
	};

	public Void visit( PointerStatement n, String ctx ){
		return null;
	};

	public Void visit( DeepCopyStatement n, String ctx ){
		return null;
	};

	public Void visit( RunStatement n, String ctx ){
		return null;
	};

	public Void visit( UndefStatement n, String ctx ){
		return null;
	};

	public Void visit( ValueVectorSizeExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( PreIncrementStatement n, String ctx ){
		return null;
	};

	public Void visit( PostIncrementStatement n, String ctx ){
		return null;
	};

	public Void visit( PreDecrementStatement n, String ctx ){
		return null;
	};

	public Void visit( PostDecrementStatement n, String ctx ){
		return null;
	};

	public Void visit( ForStatement n, String ctx ){
		return null;
	};

	public Void visit( ForEachSubNodeStatement n, String ctx ){
		return null;
	};

	public Void visit( ForEachArrayItemStatement n, String ctx ){
		return null;
	};

	public Void visit( SpawnStatement n, String ctx ){
		return null;
	};

	public Void visit( IsTypeExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( InstanceOfExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( TypeCastExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( SynchronizedStatement n, String ctx ){
		return null;
	};

	public Void visit( CurrentHandlerStatement n, String ctx ){
		return null;
	};

	public Void visit( EmbeddedServiceNode n, String ctx ){
		return null;
	};

	public Void visit( InstallFixedVariableExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( VariablePathNode n, String ctx ){
		return null;
	};

	public Void visit( TypeDefinitionLink n, String ctx ){
		return null;
	};

	public Void visit( InterfaceDefinition n, String ctx ){
		return null;
	};

	public Void visit( DocumentationComment n, String ctx ){
		return null;
	};

	public Void visit( FreshValueExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( CourierDefinitionNode n, String ctx ){
		return null;
	};

	public Void visit( CourierChoiceStatement n, String ctx ){
		return null;
	};

	public Void visit( NotificationForwardStatement n, String ctx ){
		return null;
	};

	public Void visit( SolicitResponseForwardStatement n, String ctx ){
		return null;
	};

	public Void visit( InterfaceExtenderDefinition n, String ctx ){
		return null;
	};

	public Void visit( InlineTreeExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( VoidExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( ProvideUntilStatement n, String ctx ){
		return null;
	};

	public Void visit( TypeChoiceDefinition n, String ctx ){
		return null;
	};

	public Void visit( ImportStatement n, String ctx ){
		return null;
	};

	public Void visit( ServiceNode n, String ctx ){
		return null;
	};

	public Void visit( EmbedServiceNode n, String ctx ){
		return null;
	};

	public Void visit( SolicitResponseExpressionNode n, String ctx ){
		return null;
	};

	public Void visit( IfExpressionNode n, String Ctx ){
		return null;
	};
}
