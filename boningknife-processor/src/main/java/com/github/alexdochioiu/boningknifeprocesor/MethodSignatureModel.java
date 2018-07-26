package com.github.alexdochioiu.boningknifeprocesor;

import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Alexandru Iustin Dochioiu on 7/23/2018
 */
class MethodSignatureModel {
    private final String simpleName;
    private final TypeMirror returnType;
    private final Parameters parameters;

    /**
     * Constructor
     *
     * @param executableElement used for constructing the method model
     */
    MethodSignatureModel(ExecutableElement executableElement) {
        this.simpleName = executableElement.getSimpleName().toString();
        this.returnType = executableElement.getReturnType();
        this.parameters = Utils.ExecutableElementUtil.getParameters(executableElement);

    }

    String getSimpleName() {
        return simpleName;
    }

    TypeMirror getReturnType() {
        return returnType;
    }

    Parameters getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + simpleName.hashCode();
        hash = hash * 13 + returnType.hashCode();
        hash = hash * 13 + parameters.hashCode();
        return hash;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        MethodSignatureModel other = (MethodSignatureModel) o;
        if (!getSimpleName().equals(other.getSimpleName())) return false;
        if (!getReturnType().equals(other.getReturnType())) return false;
        if (!getParameters().equals(other.getParameters())) return false;
        return true;
    }

    static class Parameters {
        final List<ParameterSpec> orderedParameterSpecs;

        /**
         * Constructor
         *
         * @param orderedParameterSpecs the <b>ORDERED</b> list of parameters taken by method
         */
        Parameters(List<ParameterSpec> orderedParameterSpecs) {
            this.orderedParameterSpecs = orderedParameterSpecs;
        }

        /**
         * @return a copy of the parameters list (so we don't operate on it)
         */
        public List<ParameterSpec> getOrderedParameterSpecs() {
            return new ArrayList<>(orderedParameterSpecs);
        }

        @Override
        public int hashCode() {
            int hash = 1;
            if (orderedParameterSpecs != null) {
                hash = hash * 13 + orderedParameterSpecs.size();
                for (ParameterSpec parameterSpec : orderedParameterSpecs) {
                    hash = hash * 17 + parameterSpec.getClass().hashCode();
                }
            }
            return hash;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (getClass() != o.getClass()) return false;

            List<ParameterSpec> thisParameterSpecs = getOrderedParameterSpecs();
            List<ParameterSpec> otherParameterSpecs = ((Parameters) o).getOrderedParameterSpecs();

            if (thisParameterSpecs.size() != otherParameterSpecs.size()) return false;
            for (int i = 0; i < thisParameterSpecs.size(); ++i) {
                if (thisParameterSpecs.get(i).getClass() != otherParameterSpecs.get(i).getClass()) {
                    return false;
                }
            }
            return true;
        }
    }
}
