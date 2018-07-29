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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by Alexandru Iustin Dochioiu on 7/25/2018
 */
@SuppressWarnings("UnusedReturnValue")
final class MethodSignaturesAndInterfacesHashPair {
    private final Set<MethodSignatureModel> methodSignatureModels = new LinkedHashSet<>();
    private final Set<Element> interfaceElements = new LinkedHashSet<>();

    /**
     * Method used for joining the method signatures and interfaces from a second {@link MethodSignaturesAndInterfacesHashPair}
     * into the current one
     *
     * @param other the other {@link MethodSignaturesAndInterfacesHashPair} which has the method
     *              signatures and interfaces copied
     */
    void innerJoin(MethodSignaturesAndInterfacesHashPair other) {
        this.methodSignatureModels.addAll(other.methodSignatureModels);
        this.interfaceElements.addAll(other.interfaceElements);
    }

    LinkedHashSet<Element> getInterfaceElementsCopy() {
        return new LinkedHashSet<>(interfaceElements);
    }

    LinkedHashSet<MethodSignatureModel> getMethodSignaturesCopy() {
        return new LinkedHashSet<>(methodSignatureModels);
    }

    boolean addMethodSignature(MethodSignatureModel methodSignatureModel) {
        return methodSignatureModels.add(methodSignatureModel);
    }

    boolean addInterface(Element element) {
        return interfaceElements.add(element);
    }
}
