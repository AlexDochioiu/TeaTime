package com.github.alexdochioiu.boningknifeprocesor;

import com.squareup.javapoet.ParameterSpec;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * Created by Alexandru Iustin Dochioiu on 7/21/2018
 */
class ExecutableElementUtil {
    /**
     * Method checking if {@link ExecutableElement} is a constructor
     *
     * @param executableElement the {@link ExecutableElement} to be checked
     * @return true if it is a constructor; false otherwise
     */
    static boolean isConstructor(ExecutableElement executableElement) {
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
    public static boolean isPublicNonStatic(
            ExecutableElement executableElement
    ) {
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
     * @return the newly created list of {@link ParameterSpec}
     */
    public static List<ParameterSpec> getParameters(ExecutableElement executableElement) {
        final List<ParameterSpec> parameterSpecs = new LinkedList<>();

        for (VariableElement variableElement : executableElement.getParameters()) {
            parameterSpecs.add(ParameterSpec.get(variableElement));
        }

        return parameterSpecs;
    }
}
