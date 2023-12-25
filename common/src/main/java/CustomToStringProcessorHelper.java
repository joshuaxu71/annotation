import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomToStringProcessorHelper {
	public static String getFilePath(TypeElement typeElement, ProcessingEnvironment processingEnv) {
		// Get the package of the annotated class
		PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);

		// Get the binary name of the annotated class
		String binaryName = processingEnv.getElementUtils().getBinaryName(typeElement).toString();

		// Build the file path using the package and binary name
		String filePath = packageElement.isUnnamed()
			? binaryName + ".java"
			: packageElement.getQualifiedName() + "." + binaryName + ".java";

		return System.getProperty("user.dir") + "\\src\\main\\java\\" + filePath;
	}

	public static CompilationUnit getCompilationUnit(String filePath) throws IOException {
		// Parse the existing Java file using JavaParser
		JavaParser javaParser = new JavaParser();
		FileInputStream in = new FileInputStream(filePath);
		CompilationUnit cu = javaParser.parse(in).getResult().get();
		in.close();
		return cu;
	}

	public static ClassOrInterfaceDeclaration addToStringMethod(TypeElement typeElement, CompilationUnit cu) {
		String className = typeElement.getSimpleName().toString();
		ClassOrInterfaceDeclaration declaration = cu.getClassByName(className).orElse(null);
		if (declaration == null) {
			return null;
		}

		// Remove existing toString() method
		declaration.setMembers(
			declaration.getMembers().stream()
				.filter(member -> {
					if (member.isMethodDeclaration()) {
						MethodDeclaration methodDeclaration = (MethodDeclaration) member;
						return !methodDeclaration.getNameAsString().equals("toString");
					}
					return true;
				})
				.collect(NodeList.toNodeList())
		);

		// Generate the toString() method and add it to the class
		MethodDeclaration toStringMethod = CustomToStringProcessorHelper.generateToStringMethod(
			typeElement.getEnclosedElements().stream()
				.filter(element -> ElementKind.FIELD.equals(element.getKind())).collect(Collectors.toList())
		);
		declaration.addMember(toStringMethod);

		return declaration;
	}
	private static MethodDeclaration generateToStringMethod(List<Element> fields) {
		List<BinaryExpr> binaryExprList = new ArrayList<>();

		boolean first = true;
		for (Element field : fields) {
			String stringFormat = ", %s: ";
			if (first) {
				stringFormat = "%s: ";
				first = false;
			}

			// Generates the expression fieldName: {field}, e.g. "name: " + name
			binaryExprList.add(new BinaryExpr(
				new StringLiteralExpr(String.format(stringFormat, field.getSimpleName().toString())),
				new NameExpr(field.getSimpleName().toString()),
				BinaryExpr.Operator.PLUS
			));
		}

		Expression binaryExpression = mergeBinaryExpressions(binaryExprList);

		return new MethodDeclaration()
			.setModifiers(Modifier.Keyword.PUBLIC)
			.addAnnotation(new MarkerAnnotationExpr(new Name("Override")))
			.setType(new ClassOrInterfaceType(null, "String"))
			.setName("toString")
			.setBody(new BlockStmt().addStatement(
				new ReturnStmt(binaryExpression)
			));
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
