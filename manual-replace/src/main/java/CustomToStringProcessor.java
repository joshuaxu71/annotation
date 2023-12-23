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

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"CustomToString"})
public class CustomToStringProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);

			for (Element element : annotatedElements) {
				// You can perform processing for each annotated element here
				// For example, generate code, validate, or perform other actions
				try {
					addToStringMethodToClass((TypeElement) element, processingEnv);
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
				}
			}
		}
		return true;
	}

	private void addToStringMethodToClass(
		TypeElement typeElement,
		ProcessingEnvironment processingEnv
	) throws IOException {
		String className = typeElement.getSimpleName().toString();

		// Get the package of the annotated class
		PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);

		// Get the binary name of the annotated class
		String binaryName = processingEnv.getElementUtils().getBinaryName(typeElement).toString();

		// Build the file path using the package and binary name
		String filePath = packageElement.isUnnamed()
			? binaryName + ".java"
			: packageElement.getQualifiedName() + "." + binaryName + ".java";

		filePath = System.getProperty("user.dir") + "\\src\\main\\java\\" + filePath;

		// Parse the existing Java file
		JavaParser javaParser = new JavaParser();
		FileInputStream in = new FileInputStream(filePath);
		CompilationUnit cu = javaParser.parse(in).getResult().get();
		in.close();

		// Add the new method to the existing class
		ClassOrInterfaceDeclaration declaration = cu.getClassByName(className).orElse(null);
		if (declaration == null) {
			return;
		}

		// Replace existing toString() method
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

		Expression binaryExpression = generateToStringMethod(
			typeElement.getEnclosedElements().stream()
				.filter(element -> ElementKind.FIELD.equals(element.getKind())).collect(Collectors.toList())
		);

		// Create a new method
		MethodDeclaration toStringMethod = new MethodDeclaration()
			.setModifiers(Modifier.Keyword.PUBLIC)
			.addAnnotation(new MarkerAnnotationExpr(new Name("Override")))
			.setType(new ClassOrInterfaceType(null, "String"))
			.setName("toString")
			.setBody(new BlockStmt().addStatement(
				new ReturnStmt(binaryExpression)
			));

		// Add the new method to the existing class
		declaration.addMember(toStringMethod);

		// Write the modified content back to the file
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			out.write(cu.toString().getBytes());
		}
	}

	private Expression generateToStringMethod(List<Element> fields) {
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

	private Expression mergeBinaryExpressions(List<BinaryExpr> expressions) {
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
