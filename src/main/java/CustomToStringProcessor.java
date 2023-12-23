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
public class CustomToStringProcessor extends AbstractProcessor {

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
					addToStringMethodToClass((TypeElement) element, filePath);
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
				}
			}
		}
		return true;
	}

	private void addToStringMethodToClass(TypeElement typeElement, String filePath) throws IOException {
		if (hasBeenProcessed(typeElement.getQualifiedName().toString())) {
			return;
		}

		markAsProcessed(typeElement.getQualifiedName().toString());

		// Get the qualified name of the class
		String qualifiedClassName = typeElement.getQualifiedName().toString();

		// Parse the existing Java file using JavaParser
		JavaParser javaParser = new JavaParser();
		FileInputStream in = new FileInputStream(filePath);
		CompilationUnit cu = javaParser.parse(in).getResult().get();

		// Create the binary expression for the return statement
		Expression binaryExpression = new BinaryExpr(
			new NameExpr("fieldName1"),
			new StringLiteralExpr(" - "),
			BinaryExpr.Operator.PLUS
		);

//		throw new FileNotFoundException("asd");

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
		cu.findFirst(ClassOrInterfaceDeclaration.class)
			.ifPresent(clazz -> clazz.addMember(toStringMethod));

		cu.setPackageDeclaration(qualifiedClassName + ".generated");

		// Get the JavaFileObject for the class and open a writer
		JavaFileObject sourceFile = filer.createSourceFile(qualifiedClassName);
		try (Writer writer = sourceFile.openWriter()) {
			writer.write(cu.toString());
		}

	}

	// Helper methods to track processed classes
	private boolean hasBeenProcessed(String qualifiedClassName) {
		// Implement logic to check whether the class has been processed
		// For example, you can use a set to keep track of processed classes
		// and check if the set contains the qualified class name.
		return processedClasses.contains(qualifiedClassName);
	}

	private void markAsProcessed(String qualifiedClassName) {
		// Implement logic to mark the class as processed
		// For example, you can add the qualified class name to a set.
		processedClasses.add(qualifiedClassName);
	}
}
