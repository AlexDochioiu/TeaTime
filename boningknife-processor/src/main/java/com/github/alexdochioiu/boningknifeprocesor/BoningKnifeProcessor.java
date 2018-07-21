package com.github.alexdochioiu.boningknifeprocesor;

import com.github.alexdochioiu.boningknife.Interfaced;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.github.alexdochioiu.boningknife.Interfaced")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BoningKnifeProcessor extends AbstractProcessor {

    private int round = -1;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        round++;

        if (round == 0) {
            EnvironmentUtil.init(processingEnv);
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Interfaced.class)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Found %s", element.getSimpleName()));
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.");
                return false;
            }

        }


        return true;

    }
}
