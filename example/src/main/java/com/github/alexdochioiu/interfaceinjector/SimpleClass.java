package com.github.alexdochioiu.interfaceinjector;

import android.support.annotation.NonNull;

import com.github.alexdochioiu.teatime.Interfaced;

/**
 * Created by Alexandru Iustin Dochioiu on 7/21/2018
 */
@Interfaced
public class SimpleClass extends SimpleBaseClass implements SimpleInterface, IISimpleClass {
    private static final String STRING_ME = "sda";
    private int simpleIntVar = 2;
    public int publicIntVar = 10;

    public SimpleClass(@NonNull String str, Integer integer) {

    }

    private SimpleClass(Boolean b) {

    }


    public void methodA() {
    }

    /**
     * This method has docs
     *
     * @param param some docs for this {@link String}
     * @param test  some int docs
     * @return the first param
     */
    public String methodString(@NonNull String param, int test) {
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

    public String interfaceMethod(Integer value) {
        return null;
    }

    @Override
    public int onBaseInterfaceMethod(IISimpleClass simpleClass) {
        return super.onBaseInterfaceMethod(simpleClass);
    }

    @Override
    public String getSomething() {
        return super.getSomething();
    }
}
