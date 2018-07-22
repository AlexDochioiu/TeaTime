package com.github.alexdochioiu.interfaceinjector;

import com.github.alexdochioiu.boningknife.Interfaced;

/**
 * Created by Alexandru Iustin Dochioiu on 7/21/2018
 */
@Interfaced
public class SimpleClass extends SimpleBaseClass implements IISimpleClass, SimpleInterface {
    private static final String STRING_ME = "sda";

    public SimpleClass(String str, Integer integer) {

    }

    private SimpleClass(Boolean b) {

    }

    private int simpleIntVar = 2;
    public int publicIntVar = 10;

    public void methodA() {
    }

    /**
     * This method has docs
     *
     * @param param some docs for this {@link String}
     * @param test  some int docs
     * @return the first param
     */
    public String methodString(String param, int test) {
        return param;
    }

    private void privMethod() {
    }

    void internalMethod() {
    }

    public static int staticMethod() {
        return 2;
    }

    public void newMethod() {
    }

    @Override
    public String interfaceMethod(Integer value) {
        return null;
    }
}
