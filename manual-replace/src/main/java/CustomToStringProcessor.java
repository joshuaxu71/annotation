import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"CustomToString"})
public class CustomToStringProcessor extends AbstractProcessor {

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

		ClassOrInterfaceDeclaration declaration = CustomToStringProcessorHelper.addToStringMethod(typeElement, cu);
		if (declaration == null) {
			return;
		}

		// Write the modified content back to the file
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			out.write(cu.toString().getBytes());
		}
	}
}
