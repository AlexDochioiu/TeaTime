package com.github.alexdochioiu.boningknifeprocesor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

@SupportedAnnotationTypes("com.github.alexdochioiu.boningknife.Interfaced")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BoningKnifeProcessor extends AbstractProcessor {

    private int round = -1;

    private Filer filer;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        round++;

        if (round == 0) {
            EnvironmentUtil.init(processingEnv);
        }

        if (!processInterfacedClasses(roundEnvironment)) {
            return false;
        }

        EnvironmentUtil.logWarning("Finished successfully");
        return true;

    }

    private boolean processInterfacedClasses(RoundEnvironment roundEnvironment) {
        TypeElement interfacedType = processingEnv
                .getElementUtils()
                .getTypeElement("com.github.alexdochioiu.boningknife.Interfaced");

        final Set<? extends Element> interfaced = roundEnvironment.getElementsAnnotatedWith(interfacedType);

        if (interfaced == null || interfaced.isEmpty()) {
            return true;
        } else {
            // there are some to classes process
            for (Element element : interfaced) {
                if (element.getKind() != ElementKind.CLASS) {
                    EnvironmentUtil.logError("Interfaced can only be used for classes!");
                    return false;
                }

                if (!generateInterface((TypeElement) element)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean generateInterface(TypeElement element) {
        final ClassName elementClassName = ClassName.get(element);
        final List<MethodSpec> methodSpecs = getMethodSpecsRecursively(element);

        TypeSpec generatedInterface = TypeSpec.interfaceBuilder(String.format("II%s", elementClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodSpecs)
                .build();

        try {
            JavaFile.builder(elementClassName.packageName(), generatedInterface)
                    .addFileComment("Generated by BoningKnife")
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            EnvironmentUtil.logError(String.format("Could not generate interface for '%s'", elementClassName.simpleName()));
            return false;
        }

        return true;
    }

    private List<MethodSpec> getMethodSpecsRecursively(final TypeElement typeElement) {
        EnvironmentUtil.logWarning(String.format("getMethodsRec: %s", typeElement.getSimpleName()));
        LinkedList<MethodSpec> methodSpecs = new LinkedList<>();

        for (Element elementEnclosed : typeElement.getEnclosedElements()) {
            if (elementEnclosed instanceof ExecutableElement) {
                final ExecutableElement executableElement = (ExecutableElement) elementEnclosed;

                if (ExecutableElementUtil.isConstructor(executableElement)) {
                    // We ignore constructors
                    continue;
                }

                if (!ExecutableElementUtil.isPublicNonStatic(executableElement)) {
                    // We ignore methods which are not public or statics
                    continue;
                }
                // TODO: extend present interfaces
                // TODO: remove duplicates due to overriding from base class
                // TODO: check if it is @DontInterface (care if it is overridden to not interface it from base class)

                List<ParameterSpec> parameterSpecs = ExecutableElementUtil.getParameters(executableElement);

                MethodSpec methodSpec = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                        .returns(TypeName.get(executableElement.getReturnType()))
                        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                        .addParameters(parameterSpecs)
                        .build();

                methodSpecs.add(methodSpec);
            }
        }

        // Check if the base class for this exists and is not Object. In such case, go deeper and get those methods as well
        final TypeMirror superTypeMirror = typeElement.getSuperclass();
        if (superTypeMirror != null) {
            EnvironmentUtil.logWarning("Got superclass");
            final Element superElement = typeUtils.asElement(superTypeMirror);
            if (superElement != null) {
                EnvironmentUtil.logWarning("Got superelement");
                final TypeElement superTypeElement = (TypeElement) superElement;
                //noinspection ConstantConditions
                if (superTypeElement != null && !superTypeElement.getSimpleName().toString().equals("Object")) {
                    EnvironmentUtil.logWarning("Got supertypelemenet");
                    //our superclass exists and is indeed a class (not sure if it can be something else but still worth making sure)
                    methodSpecs.addAll(getMethodSpecsRecursively(superTypeElement));
                }
            }
        }

        return methodSpecs;
    }
}
