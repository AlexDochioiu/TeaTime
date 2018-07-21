package com.github.alexdochioiu.boningknifeprocesor;

import com.github.alexdochioiu.boningknife.Interfaced;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.github.alexdochioiu.boningknife.Interfaced")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BoningKnifeProcessor extends AbstractProcessor {

    private int round = -1;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnvironment.getFiler();
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

/*
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Interfaced.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.");
                return false;
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format("Found %s in %s", element.getSimpleName(), ClassName.get((TypeElement) element).packageName()));

            }
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ERROR");
*/
        EnvironmentUtil.logWarning("Finished successfully");
        return true;

    }

    private boolean processInterfacedClasses(RoundEnvironment roundEnvironment) {
        final Set<? extends Element> interfaced = roundEnvironment.getElementsAnnotatedWith(Interfaced.class);

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

        TypeSpec generatedInterface = TypeSpec.interfaceBuilder(String.format("II%s", elementClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .build();

        //File file = new File("").getAbsoluteFile();

        try {
            //EnvironmentUtil.logWarning(Boolean.toString(file.isDirectory()));

            JavaFile.builder(elementClassName.packageName(), generatedInterface).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            EnvironmentUtil.logError(String.format("Could not generate interface for '%s'", elementClassName.simpleName()));
            return false;
        }


        return true;
    }
}
