import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"CustomToString"})
public class CustomToStringProcessor extends AbstractProcessor {

	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(annotation);

			for (Element element : annotatedElements) {
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
		String filePath = CustomToStringProcessorHelper.getFilePath(typeElement, processingEnv);
		CompilationUnit cu = CustomToStringProcessorHelper.getCompilationUnit(filePath);
		cu.setPackageDeclaration("generated");

		ClassOrInterfaceDeclaration declaration = CustomToStringProcessorHelper.addToStringMethod(typeElement, cu);
		if (declaration == null) {
			return;
		}

		// Remove the annotation since we've already processed it
		declaration.getAnnotations().removeIf(annotationExpr ->
			annotationExpr.getNameAsString().equals("CustomToString"));

		// Get the JavaFileObject for the class and open a writer
		JavaFileObject sourceFile = filer.createSourceFile(typeElement.getQualifiedName().toString());
		try (Writer writer = sourceFile.openWriter()) {
			writer.write(cu.toString());
		}
	}
}
