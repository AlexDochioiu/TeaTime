package com.github.alexdochioiu.interfaceinjector;

/**
 * Created by Alexandru Iustin Dochioiu on 7/21/2018
 */
public class SimpleBaseClass implements BaseClassInterface {

    public String getSomething() {
        return "";
    }

    @Override
    public int onBaseInterfaceMethod(IISimpleClass simpleClass) {
        return 0;
    }
}
