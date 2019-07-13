package edu.kit.mima.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jannis Weis
 * @since 2019R
 */
@SupportedAnnotationTypes("edu.kit.mima.annotations.Context")
@SupportedSourceVersion(SourceVersion.RELEASE_12)
public class ContextAnnotationProcessor extends AbstractProcessor {

    @Nullable
    private static AnnotationMirror getAnnotationMirror(
            @NotNull final Element typeElement, @NotNull final Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    @Nullable
    private static AnnotationValue getAnnotationValue(@NotNull final AnnotationMirror annotationMirror,
                                                      final String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, @NotNull final RoundEnvironment roundEnv) {
        Collection<? extends Element> annotatedElements =
                roundEnv.getElementsAnnotatedWith(Context.class);

        StringBuilder builder = new StringBuilder()
                        .append("\nimport edu.kit.mima.annotations.ContextManager;\n")
                        .append("public class ContextBuilder {\n")
                        .append("static {\n");

        for (var element : annotatedElements) {
            for (var clazz : getMyValue2(element)) {
                builder.append("ContextManager.registerProvider(")
                        .append(element.toString())
                        .append(".class")
                        .append(',')
                        .append(clazz.toString())
                        .append(");\n");
            }
        }

        builder.append("}\n}");

        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("ContextBuilder");
            Writer writer = builderFile.openWriter();
            writer.write(builder.toString());
            writer.close();
        } catch (FilerException ignore) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private List<Object> getMyValue2(final Element foo) {
        AnnotationMirror am = getAnnotationMirror(foo, Context.class);
        if (am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, "provides");
        if (av == null) {
            return null;
        } else {
            //noinspection unchecked
            return (List<Object>) av.getValue();
        }
    }

    private TypeElement asTypeElement(final TypeMirror typeMirror) {
        Types utils = this.processingEnv.getTypeUtils();
        return (TypeElement) utils.asElement(typeMirror);
    }
}
