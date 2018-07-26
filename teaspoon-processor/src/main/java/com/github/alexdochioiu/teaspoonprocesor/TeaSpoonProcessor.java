package com.github.alexdochioiu.teaspoonprocesor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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

@SupportedAnnotationTypes("com.github.alexdochioiu.teaspoon.Interfaced")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TeaSpoonProcessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        this.processingEnvironment = processingEnvironment;
        MessagerWrapper.initInstance(processingEnvironment.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!processInterfacedClasses(roundEnvironment)) {
            return false;
        }

        MessagerWrapper.logWarning("Finished successfully");
        return true;
    }

    private boolean processInterfacedClasses(RoundEnvironment roundEnvironment) {
        TypeElement interfacedType = processingEnv
                .getElementUtils()
                .getTypeElement("com.github.alexdochioiu.teaspoon.Interfaced");

        final Set<? extends Element> interfaced = roundEnvironment.getElementsAnnotatedWith(interfacedType);

        if (interfaced == null || interfaced.isEmpty()) {
            return true;
        } else {
            // there are some to classes to process
            for (Element element : interfaced) {
                if (element.getKind() != ElementKind.CLASS) {
                    MessagerWrapper.logError("Interfaced can only be used for classes!");
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
        final MethodSignaturesAndInterfacesHashPair methodSignaturesAndInterfacesHashPair =
                getMethodsAndInterfacesTrail(element);

        final HashSet<Element> interfaceElements = methodSignaturesAndInterfacesHashPair.getInterfaceElementsCopy();

        // This set will contain all the methods from all the interfaces (the class, the base classes, and the base interfaces)
        final HashSet<MethodSignatureModel> currentlyInterfacedMethods = new HashSet<>();

        for (Element interfaceElement : interfaceElements) {
            currentlyInterfacedMethods.addAll(Utils.ElementUtil.getMethodSignatureModelsHashSetFromInterface(interfaceElement, processingEnvironment));
        }

        final HashSet<MethodSignatureModel> methodsToBeAddedInGeneratedInterface =
                new HashSet<>(methodSignaturesAndInterfacesHashPair.getMethodSignaturesCopy());
        for (MethodSignatureModel methodSignatureModel : currentlyInterfacedMethods) {
            if (methodsToBeAddedInGeneratedInterface.remove(methodSignatureModel)) {
                MessagerWrapper.logWarning("Removing method %s from generated interface", methodSignatureModel.getSimpleName());
            }
        }

        TypeSpec.Builder generatedInterfaceBuilder = TypeSpec.interfaceBuilder(String.format("II%s", elementClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC);

        for (MethodSignatureModel methodSignatureModel : methodsToBeAddedInGeneratedInterface) {
            generatedInterfaceBuilder.addMethod(
                    MethodSpec.methodBuilder(methodSignatureModel.getSimpleName())
                            .returns(TypeName.get(methodSignatureModel.getReturnType()))
                            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                            .addParameters(methodSignatureModel.getParameters().getOrderedParameterSpecs())
                            .build()
            );
        }

        for (Element interfaceElement : methodSignaturesAndInterfacesHashPair.getInterfaceElementsCopy()) {

            if (processingEnvironment.getElementUtils().getPackageOf(interfaceElement).getQualifiedName().toString().equals("")) {
                MessagerWrapper.logWarning("Could not determine package for %s", interfaceElement.getSimpleName());
            } else {
                MessagerWrapper.logWarning("Found type interface to be added as super: %s.%s",
                        processingEnvironment.getElementUtils().getPackageOf(interfaceElement).getQualifiedName().toString(),
                        interfaceElement.getSimpleName());

                TypeName interfaceTypeName = TypeName.get(interfaceElement.asType());

                generatedInterfaceBuilder.addSuperinterface(interfaceTypeName);
            }
        }

        try {
            JavaFile.builder(elementClassName.packageName(), generatedInterfaceBuilder.build())
                    .addFileComment("Generated by BoningKnife")
                    .build()
                    .writeTo(processingEnvironment.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
            MessagerWrapper.logError(String.format("Could not generate interface for '%s'", elementClassName.simpleName()));
            return false;
        }

        return true;
    }

    /**
     * Used to obtained all the methods publicly accessible for an instance of a given class and all
     * the interfaces extended by that class and its base classes
     * <p>
     * <b>NOTE:</b> this method uses recursion to check the base classes
     * <b>NOTE2:</b> the methods defined for {@link Object} type are ignored
     *
     * @param typeElement the {@link TypeElement} for a class <b>(it is assumed that this type element
     *                    is ElementKind.CLASS)</b>
     * @return
     */
    private MethodSignaturesAndInterfacesHashPair getMethodsAndInterfacesTrail(final TypeElement typeElement) {
        MessagerWrapper.logWarning(String.format("getMethodsRec: %s", typeElement.getSimpleName()));
        MethodSignaturesAndInterfacesHashPair pair = new MethodSignaturesAndInterfacesHashPair();

        // start by getting the signature for all the methods
        for (Element elementEnclosed : typeElement.getEnclosedElements()) {
            if (elementEnclosed instanceof ExecutableElement) {
                final ExecutableElement executableElement = (ExecutableElement) elementEnclosed;

                if (Utils.ExecutableElementUtil.isConstructor(executableElement)) {
                    // We ignore constructors
                    continue;
                }

                if (!Utils.ExecutableElementUtil.isPublicNonStatic(executableElement)) {
                    // We ignore methods which are not public or statics
                    continue;
                }
                // TODO: extend present interfaces

                MethodSignatureModel methodSignatureModel = new MethodSignatureModel(executableElement);
                pair.addMethodSignature(methodSignatureModel);
            }
        }

        // then get all the interfaces it extends
        for (TypeMirror interfaceMirror : typeElement.getInterfaces()) {
            pair.addInterface(processingEnvironment.getTypeUtils().asElement(interfaceMirror));
        }

        // Check if the base class for this exists and is not Object. In such case, go deeper and get those methods as well
        final TypeMirror superTypeMirror = typeElement.getSuperclass();
        if (superTypeMirror != null) {
            MessagerWrapper.logWarning("Got superclass");
            final Element superElement = processingEnvironment.getTypeUtils().asElement(superTypeMirror);
            if (superElement != null) {
                MessagerWrapper.logWarning("Got superelement");
                final TypeElement superTypeElement = (TypeElement) superElement;
                //noinspection ConstantConditions
                if (superTypeElement != null && !superTypeElement.getSimpleName().toString().equals("Object")) {
                    MessagerWrapper.logWarning("Got supertypelemenet");
                    //our superclass exists and is indeed a class (not sure if it can be something else but still worth making sure)
                    pair.innerJoin(getMethodsAndInterfacesTrail(superTypeElement));
                }
            }
        }

        return pair;
    }
}
