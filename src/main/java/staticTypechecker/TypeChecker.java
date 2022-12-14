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
public class TypeChecker implements OLVisitor<Void, Void> {
	public Void visit(Program p, Void ctx){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public Void visit(TypeInlineDefinition t, Void ctx){
		System.out.println("Basic type: " + t.basicType().nativeType() + "[" + t.cardinality().min() + ", " + t.cardinality().max() + "]");

		if(t.subTypes() != null){ // has subtypes
			for(Map.Entry<String, TypeDefinition> key : t.subTypes()){
				key.getValue().accept(this, null);
			}
		}

		return null;
	}

	public Void visit(TypeNameDefinition t, Void ctx){
		return null;
	}

	public Void visit(TypeStructureDefinition t, Void ctx){
		return null;
	}

	public Void visit( OneWayOperationDeclaration decl, Void ctx ){
		return null;
	};

	public Void visit( RequestResponseOperationDeclaration decl, Void ctx ){
		return null;
	};

	public Void visit( DefinitionNode n, Void ctx ){
		return null;
	};

	public Void visit( ParallelStatement n, Void ctx ){
		return null;
	};

	public Void visit( SequenceStatement n, Void ctx ){
		return null;
	};

	public Void visit( NDChoiceStatement n, Void ctx ){
		return null;
	};

	public Void visit( OneWayOperationStatement n, Void ctx ){
		return null;
	};

	public Void visit( RequestResponseOperationStatement n, Void ctx ){
		return null;
	};

	public Void visit( NotificationOperationStatement n, Void ctx ){
		return null;
	};

	public Void visit( SolicitResponseOperationStatement n, Void ctx ){
		return null;
	};

	public Void visit( LinkInStatement n, Void ctx ){
		return null;
	};

	public Void visit( LinkOutStatement n, Void ctx ){
		return null;
	};

	public Void visit( AssignStatement n, Void ctx ){
		return null;
	};

	public Void visit( AddAssignStatement n, Void ctx ){
		return null;
	};

	public Void visit( SubtractAssignStatement n, Void ctx ){
		return null;
	};

	public Void visit( MultiplyAssignStatement n, Void ctx ){
		return null;
	};

	public Void visit( DivideAssignStatement n, Void ctx ){
		return null;
	};

	public Void visit( IfStatement n, Void ctx ){
		return null;
	};

	public Void visit( DefinitionCallStatement n, Void ctx ){
		return null;
	};

	public Void visit( WhileStatement n, Void ctx ){
		return null;
	};

	public Void visit( OrConditionNode n, Void ctx ){
		return null;
	};

	public Void visit( AndConditionNode n, Void ctx ){
		return null;
	};

	public Void visit( NotExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( CompareConditionNode n, Void ctx ){
		return null;
	};

	public Void visit( ConstantIntegerExpression n, Void ctx ){
		return null;
	};

	public Void visit( ConstantDoubleExpression n, Void ctx ){
		return null;
	};

	public Void visit( ConstantBoolExpression n, Void ctx ){
		return null;
	};

	public Void visit( ConstantLongExpression n, Void ctx ){
		return null;
	};

	public Void visit( ConstantStringExpression n, Void ctx ){
		return null;
	};

	public Void visit( ProductExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( SumExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( VariableExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( NullProcessStatement n, Void ctx ){
		return null;
	};

	public Void visit( Scope n, Void ctx ){
		return null;
	};

	public Void visit( InstallStatement n, Void ctx ){
		return null;
	};

	public Void visit( CompensateStatement n, Void ctx ){
		return null;
	};

	public Void visit( ThrowStatement n, Void ctx ){
		return null;
	};

	public Void visit( ExitStatement n, Void ctx ){
		return null;
	};

	public Void visit( ExecutionInfo n, Void ctx ){
		return null;
	};

	public Void visit( CorrelationSetInfo n, Void ctx ){
		return null;
	};

	public Void visit( InputPortInfo n, Void ctx ){
		return null;
	};

	public Void visit( OutputPortInfo n, Void ctx ){
		return null;
	};

	public Void visit( PointerStatement n, Void ctx ){
		return null;
	};

	public Void visit( DeepCopyStatement n, Void ctx ){
		return null;
	};

	public Void visit( RunStatement n, Void ctx ){
		return null;
	};

	public Void visit( UndefStatement n, Void ctx ){
		return null;
	};

	public Void visit( ValueVectorSizeExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( PreIncrementStatement n, Void ctx ){
		return null;
	};

	public Void visit( PostIncrementStatement n, Void ctx ){
		return null;
	};

	public Void visit( PreDecrementStatement n, Void ctx ){
		return null;
	};

	public Void visit( PostDecrementStatement n, Void ctx ){
		return null;
	};

	public Void visit( ForStatement n, Void ctx ){
		return null;
	};

	public Void visit( ForEachSubNodeStatement n, Void ctx ){
		return null;
	};

	public Void visit( ForEachArrayItemStatement n, Void ctx ){
		return null;
	};

	public Void visit( SpawnStatement n, Void ctx ){
		return null;
	};

	public Void visit( IsTypeExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( InstanceOfExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( TypeCastExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( SynchronizedStatement n, Void ctx ){
		return null;
	};

	public Void visit( CurrentHandlerStatement n, Void ctx ){
		return null;
	};

	public Void visit( EmbeddedServiceNode n, Void ctx ){
		return null;
	};

	public Void visit( InstallFixedVariableExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( VariablePathNode n, Void ctx ){
		return null;
	};

	public Void visit( TypeDefinitionLink n, Void ctx ){
		return null;
	};

	public Void visit( InterfaceDefinition n, Void ctx ){
		return null;
	};

	public Void visit( DocumentationComment n, Void ctx ){
		return null;
	};

	public Void visit( FreshValueExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( CourierDefinitionNode n, Void ctx ){
		return null;
	};

	public Void visit( CourierChoiceStatement n, Void ctx ){
		return null;
	};

	public Void visit( NotificationForwardStatement n, Void ctx ){
		return null;
	};

	public Void visit( SolicitResponseForwardStatement n, Void ctx ){
		return null;
	};

	public Void visit( InterfaceExtenderDefinition n, Void ctx ){
		return null;
	};

	public Void visit( InlineTreeExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( VoidExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( ProvideUntilStatement n, Void ctx ){
		return null;
	};

	public Void visit( TypeChoiceDefinition n, Void ctx ){
		return null;
	};

	public Void visit( ImportStatement n, Void ctx ){
		return null;
	};

	public Void visit( ServiceNode n, Void ctx ){
		return null;
	};

	public Void visit( EmbedServiceNode n, Void ctx ){
		return null;
	};

	public Void visit( SolicitResponseExpressionNode n, Void ctx ){
		return null;
	};

	public Void visit( IfExpressionNode n, Void Ctx ){
		return null;
	};
}
