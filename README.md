# IF
An inventory framework for managing GUIs

This framework is based on a pane principle. This means that the GUI is divided into different types of panes which all behave differently. A GUI consists of multiple panes which can interact with each other.

Next to those panes, GUIs can also be created from XML files by simple loading them in. This allows for easy GUI creation with little code.

## Maven dependency
To add this project as a dependency to your pom.xml, add the following to your pom.xml:
```XML
<dependency>
    <groupId>com.github.stefvanschie.inventoryframework</groupId>
    <artifactId>IF</artifactId>
    <version>0.5.12</version>
</dependency>
```
The project is in the Central Repository, so specifying a repository is not needed.

Now in order to shade the project into your project, add the following to your pom.xml:
```XML
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <dependencyReducedPomLocation>${project.build.directory}/dependency-reduced-pom.xml</dependencyReducedPomLocation>
        <relocations>
            <relocation>
                <pattern>com.github.stefvanschie.inventoryframework</pattern>
                <shadedPattern>[YOUR PACKAGE].inventoryframework</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
Replace [YOUR PACKAGE] with the top-level package of your project.

## Gradle dependency
To add this project as a dependency for your Gradle project, make sure your `dependencies` section of your build.gradle looks like the following:
```Groovy
dependencies {
    compile 'com.github.stefvanschie.inventoryframework:IF:0.5.12'
    // ...
}
```
The project is in Maven Central, so ensure your `repositories` section resembles the following:
```Groovy
repositories {
    mavenCentral()
    // ...
}
```
In order to include the project in your own project, you will need to use the `shadowJar` plugin. If you don't have it already, add the following to the top of your file:
```Groovy
apply plugin: 'com.github.johnrengelman.shadow'
```
To relocate the project's classes to your own namespace, add the following, with [YOUR PACKAGE] being the top-level package of your project:
```Groovy
shadowJar {
    relocate 'com.github.stefvanschie.inventoryframework', '[YOUR PACKAGE].inventoryframework'
}
```

## Building from source
If you want to build this project from source, run the following from Git Bash:

    git clone https://github.com/stefvanschie/IF.git
    cd IF
    mvn clean package

The build can then be found in /IF/target/.
