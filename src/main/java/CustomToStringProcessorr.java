import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"CustomToString"})
public class CustomToStringProcessorr extends AbstractProcessor {

	private Filer filer;
	private Set<String> processedClasses = new HashSet<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		// Get the project root directory
		String projectRoot = System.getProperty("user.dir");

		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);

			for (Element element : annotatedElements) {
				// You can perform processing for each annotated element here
				// For example, generate code, validate, or perform other actions
				String className = element.getSimpleName().toString();

				// Get the package of the annotated class
				PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);

				// Get the binary name of the annotated class
				String binaryName = processingEnv.getElementUtils().getBinaryName((TypeElement) element).toString();

				// Build the file path using the package and binary name
				String filePath = packageElement.isUnnamed()
					? binaryName + ".java"
					: packageElement.getQualifiedName() + "." + binaryName + ".java";

				filePath = projectRoot + "\\src\\main\\java\\" + filePath;

				try {
					addToStringMethodToClass(className, filePath);
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
				}
			}
		}
		return true;
	}

	private static void addToStringMethodToClass(String className, String filePath) throws IOException {
		// Parse the existing Java file
		JavaParser javaParser = new JavaParser();
		FileInputStream in = new FileInputStream(filePath);
		CompilationUnit cu = javaParser.parse(in).getResult().get();
//		FileInputStream in2 = new FileInputStream(cu.getClassByName(className).toString() + "a");
		in.close();

		// Create the binary expression for the return statement
		Expression binaryExpression = new BinaryExpr(
			new NameExpr("fieldName1"),
			new StringLiteralExpr(" - "),
			BinaryExpr.Operator.PLUS
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
		cu.getClassByName(className)
			.ifPresent(clazz -> clazz.addMember(toStringMethod));

		// Write the modified content back to the file
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			out.write(cu.toString().getBytes());
		}
	}
}
