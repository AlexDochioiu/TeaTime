/*
 * Copyright 2018 Alexandru Iustin Dochioiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.alexdochioiu.teatimeprocesor;

import com.squareup.javapoet.ParameterSpec;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Alexandru Iustin Dochioiu on 7/25/2018
 */
public class Utils {
/*    static class TypeMirrorUtils {

        static boolean isSameAnnotation(
                final ProcessingEnvironment processingEnvironment,
                final TypeMirror firstAnnotation,
                final TypeMirror secondAnnotation
        ) {
            return processingEnvironment.getTypeUtils().isSameType(firstAnnotation, secondAnnotation);
        }


        static boolean isSameAnnotation(
                final ProcessingEnvironment processingEnvironment,
                final TypeMirror firstAnnotation,
                final String annotationFullName
        ) {
            return isSameAnnotation(processingEnvironment, firstAnnotation, getAnnotationAsTypeMirror(processingEnvironment, annotationFullName));
        }

        static TypeMirror getAnnotationAsTypeMirror(
                final ProcessingEnvironment processingEnvironment,
                final String annotationFullName
        ) {
            return processingEnvironment.getElementUtils().getTypeElement(annotationFullName).asType();
        }
    }*/

    /**
     * Class providing utils for Executable Elements
     */
    static class ExecutableElementUtil {
        /**
         * Method checking if {@link ExecutableElement} is a constructor
         *
         * @param executableElement the {@link ExecutableElement} to be checked
         * @return true if it is a constructor; false otherwise
         */
        static boolean isConstructor(final ExecutableElement executableElement) {
            return executableElement.getSimpleName().toString().equals("<init>");
        }

        /**
         * Method checking if {@link ExecutableElement} is a public-non static method
         * <p>
         * <b>NOTE:</b> this assumes it is not a constructor
         *
         * @param executableElement a non-constructor {@link ExecutableElement}
         * @return true if the method is public non static; false otherwise
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        static boolean isPublicNonStatic(final ExecutableElement executableElement) {
            boolean foundPublicModifier = false;
            for (Modifier modifier : executableElement.getModifiers()) {
                if (modifier.equals(Modifier.STATIC)) {
                    return false;
                } else if (modifier.equals(Modifier.PUBLIC)) {
                    foundPublicModifier = true;
                }
            }
            return foundPublicModifier;
        }

        /**
         * Creates a list of {@link ParameterSpec} based on the parameters given in this method
         *
         * @param executableElement the method whose params we want to add to the list
         * @return the newly created {@link MethodSignatureModel.Parameters}
         */
        static MethodSignatureModel.Parameters getParameters(final ExecutableElement executableElement) {
            final List<ParameterSpec> parameterSpecs = new LinkedList<>();

            for (final VariableElement variableElement : executableElement.getParameters()) {
                final ParameterSpec parameterSpec = ParameterSpec.get(variableElement);

                // parameterSpec.annotations is immutable so the following code won't work
                /*
                final List<? extends AnnotationMirror> annotationMirrors = variableElement.getAnnotationMirrors();
                for (AnnotationMirror annotationMirror : annotationMirrors) {
                    parameterSpec.annotations.add(AnnotationSpec.get(annotationMirror));
                }
                */
                parameterSpecs.add(parameterSpec);
            }

            return new MethodSignatureModel.Parameters(parameterSpecs);
        }
    }

    static class ElementUtil {

        static LinkedHashSet<MethodSignatureModel> getMethodSignatureModelsHashSetFromInterface(
                final Element interfaceElement,
                final ProcessingEnvironment processingEnvironment
        ) {
            LinkedHashSet<MethodSignatureModel> methodSignatureModels = new LinkedHashSet<>();

            for (Element element : interfaceElement.getEnclosedElements()) {
                if (element instanceof ExecutableElement) {
                    final ExecutableElement executableElement = (ExecutableElement) element;

                    if (Utils.ExecutableElementUtil.isConstructor(executableElement)) {
                        MessagerWrapper.logWarning("Unexpected constructor(??) in interface %s", interfaceElement.getSimpleName());
                        // We ignore constructors
                        continue;
                    }

                    if (!Utils.ExecutableElementUtil.isPublicNonStatic(executableElement)) {
                        MessagerWrapper.logWarning(
                                "Unexpected non-public or static method %s in interface %s",
                                executableElement.getSimpleName(),
                                interfaceElement.getSimpleName()
                        );
                        // We ignore methods which are not public or statics
                        continue;
                    }

                    methodSignatureModels.add(new MethodSignatureModel(executableElement));
                }
            }

            if (interfaceElement.getKind() != ElementKind.INTERFACE) {
                MessagerWrapper.logError("Internal error: Expected %s to be interface.", interfaceElement.getSimpleName());
            }


            for(TypeMirror interfaceMirror : ((TypeElement) interfaceElement).getInterfaces()) {
                // recursive call
                methodSignatureModels.addAll(getMethodSignatureModelsHashSetFromInterface(processingEnvironment.getTypeUtils().asElement(interfaceMirror), processingEnvironment));
            }

            return methodSignatureModels;
        }
    }
}
