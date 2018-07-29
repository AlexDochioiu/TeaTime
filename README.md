# TeaTime

[![bintray](https://api.bintray.com/packages/jeefo12/TeaTime/teatime/images/download.svg) ](https://bintray.com/jeefo12/JLogger/jeefologger/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
Coverage: 92%

**TeaTime is a Java(and Android) library which generates an interface for any class annotated with `@Interfaced`. This is intended to save the time and effort put into creating single-use interfaces (implemented by a single class) which are required only for mocking, faking or stubbing the concrete class in tests.**

*In this repository you can find an example project showing its usage as well as the library projects.*

## Why would you want TeaTime?
* To maintain the projects smaller and improve their readability (less files)
* To easily keep the interfaces up-to-date (everytime you build, the interface picks up any changes made in the class and mirrors them)
* To save enough time for preparing a cup of tea with every single interface you generate with TeaTime

## Features
* **Everything happens during the build stage so any problems will be signaled at that stage. It cannot possibly cause any runtime problems!**
* The generated interface will maintain **all** the methods accessible by the class it interfaces (except the ones part of the `Object` class)
	* **Note: The generated interface will have the same name as the class prefixed by II** *(from InjectedInterface)*. Example: MyClass would generate **IIMyClass** interface.
	* Note: It extends all the interfaces which are already implemented by the class and all its base classes
	* Note: It gives access to the methods defined in base classes as well
* Circular dependency works perfectly fine
	* The interfaced class can have methods which return instances of the generated interface
* As one would expect, only the public non-static methods are interfaced

## Installation with Android Gradle
```groovy
// Add TeaTime dependencies
dependencies {
    implementation 'com.github.alexdochioiu:teatime:1.0.2'
    annotationProcessor 'com.github.alexdochioiu:teatime-processor:1.0.2'
}
```

## Sample Usage Example

### Step 1: Assume we have a provider class which triggers network calls using a repository class

```groovy
public class MyRepository {

    public String returnMyString(String myString) {
        return myString;
    }
    
    public boolean makeNetworkCall(Object object) {
        return makeNetworkCallInternal(object);
    }

    private boolean makeNetworkCallInternal(Object object) {
        return true;
    }
}

public class MyProvider {
    private final MyRepository repository;
    public MyProvider(MyRepository repository) {
        this.repository = repository;
    }

    public void makeRepositoryDoSomething() {
        repository.makeNetworkCall(new Object());
    }
}
```

To unit test `MyProvider`, **we need to mock `MyRepository` class in order to avoid making actual network calls.**

### Step 2: Interface the repository and build

In order to mock `MyRepository`, we need to create an interface exposing its public methods. Using TeaTime, the `@Interfaced` annotation is added above the class and then we build the project:

```groovy
@Interfaced
public class MyRepository {
	...
}
```

**After building the project, you can find that in the same package as the class *(not same directory though so use the search function)* there is the interface `IIMyRepository`**:

```groovy
public interface IIMyRepository {
  boolean makeNetworkCall(Object object);

  String returnMyString(String myString);
}
```

As you can see, both the public, non-static methods appear in the generated interface while the private method was ignored.

### Step 3: MyRepository should now implement the generated interface and MyProvider should use it via its interface

```groovy
public class MyRepository implements IIMyRepository {

    public String returnMyString(String myString) {
        return myString;
    }
    
    public boolean makeNetworkCall(Object object) {
        return makeNetworkCallInternal(object);
    }

    private boolean makeNetworkCallInternal(Object object) {
        return true;
    }
}

public class MyProvider {
    private final IIMyRepository repository; // using IIMyRepository interface
    public MyProvider(IIMyRepository repository) { // // using IIMyRepository interface
        this.repository = repository;
    }

    public void makeRepositoryDoSomething() {
        repository.makeNetworkCall(new Object());
    }
}
```

### Step 4: Unit Test MyProvider

At this point you can easily create a mock repository using the generated interface and then unit test your provider with it.

**Note:** Now, after making any change in `MyRepository`, the interface will pick them up during build time and the interface will be updated accordingly.

**Note:** Changing the already interfaced methods will show an IDE error until you build the project again (because your class is not properly implementing the previously generated interface anymore. This goes away once you build the project and the generated interface gets updated!)
