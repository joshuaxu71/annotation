import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;

public class CustomToStringProcessorHelper {
	public static Expression generateToStringMethod(List<Element> fields) {
		List<BinaryExpr> binaryExprList = new ArrayList<>();

		boolean first = true;
		for (Element field : fields) {
			String stringFormat = ", %s: ";
			if (first) {
				stringFormat = "%s: ";
				first = false;
			}

			binaryExprList.add(new BinaryExpr(
				new StringLiteralExpr(String.format(stringFormat, field.getSimpleName().toString())),
				new NameExpr(field.getSimpleName().toString()),
				BinaryExpr.Operator.PLUS
			));
		}

		return mergeBinaryExpressions(binaryExprList);
	}

	private static Expression mergeBinaryExpressions(List<BinaryExpr> expressions) {
		if (expressions.isEmpty()) {
			throw new IllegalArgumentException("List of expressions is empty.");
		}

		Expression result = expressions.get(0);
		for (int i = 1; i < expressions.size(); i++) {
			BinaryExpr nextExpression = expressions.get(i);
			result = new BinaryExpr(
				result,
				new BinaryExpr(nextExpression.getLeft(), nextExpression.getRight(), nextExpression.getOperator()),
				nextExpression.getOperator());
		}

		return result;
	}
}
