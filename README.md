# IF <a href="https://discord.gg/RXmy4HdR4x"><img align="right" src="https://img.shields.io/discord/780514939293925407" alt="Discord guild"></a>

*This framework works for Minecraft versions 1.14-1.19*

An inventory framework for managing GUIs

This framework is based on a pane principle. This means that the GUI is divided into different types of panes which all behave differently. A GUI consists of multiple panes which can interact with each other.

Next to those panes, GUIs can also be created from XML files by simple loading them in. This allows for easy GUI creation with little code.

## Maven dependency
To add this project as a dependency to your pom.xml, add the following to your pom.xml:
```XML
<dependency>
    <groupId>com.github.stefvanschie.inventoryframework</groupId>
    <artifactId>IF</artifactId>
    <version>0.10.7</version>
</dependency>
```
The project is in the Central Repository, so specifying a repository is not needed.

Now in order to shade the project into your project, add the following to your pom.xml:
```XML
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
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
    implementation 'com.github.stefvanschie.inventoryframework:IF:0.10.7'
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
plugins {
    // ...
    id "com.github.johnrengelman.shadow" version "7.1.2"
}
```
To relocate the project's classes to your own namespace, add the following, with [YOUR PACKAGE] being the top-level package of your project:
```Groovy
shadowJar {
    relocate 'com.github.stefvanschie.inventoryframework', '[YOUR PACKAGE].inventoryframework'
}
```

## Dependency via plugin.yml
IF does **not** support declaring the dependency via the libraries section in the plugin.yml. Please make use of a build tool as described above to use IF as a dependency.

## Building from source
If you want to build this project from source, run the following:

    git clone https://github.com/stefvanschie/IF.git

This will clone this repository to your device. This project relies on NMS, for which the dependencies are not available online. Because of this, you'll need to follow additional steps to obtain all these dependencies locally.

### Installing Paper manually
For versions 1.14-1.16, we have to manually install Paper. Run the following scripts for each version to install the dependencies locally. Running these commands generate additional files in the folder where you execute them. To ensure that you don't accidentallly overwrite other files, execute this in an empty folder. The files that get created can be deleted afterwards (either after installing a single version or after installing all of them), since they're no longer necessary.

#### 1.14.4
```
wget https://papermc.io/api/v2/projects/paper/versions/1.14.4/builds/243/downloads/paper-1.14.4-243.jar -O paperclip/paper-1.14.4.jar
java -jar paper-1.14.4.jar
mvn install:install-file -Dfile=cache/patched_1.14.4.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.14.4-R0.1-SNAPSHOT" -Dpackaging="jar"
```

#### 1.15.2
```
wget https://papermc.io/api/v2/projects/paper/versions/1.15.2/builds/391/downloads/paper-1.15.2-391.jar -O paperclip/paper-1.15.2.jar
java -jar paper-1.15.2.jar
mvn install:install-file -Dfile=cache/patched_1.15.2.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.15.2-R0.1-SNAPSHOT" -Dpackaging="jar"
```

#### 1.16.1
```
wget https://papermc.io/api/v2/projects/paper/versions/1.16.1/builds/138/downloads/paper-1.16.1-138.jar -O paperclip/paper-1.16.1.jar
java -jar paper-1.16.1.jar
mvn install:install-file -Dfile=cache/patched_1.16.1.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.1-R0.1-SNAPSHOT" -Dpackaging="jar"
```

#### 1.16.3
```
wget https://papermc.io/api/v2/projects/paper/versions/1.16.3/builds/253/downloads/paper-1.16.3-253.jar -O paperclip/paper-1.16.3.jar
java -jar paper-1.16.3.jar
mvn install:install-file -Dfile=cache/patched_1.16.3.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.3-R0.1-SNAPSHOT" -Dpackaging="jar"
```

#### 1.16.4
```
wget https://papermc.io/api/v2/projects/paper/versions/1.16.4/builds/416/downloads/paper-1.16.4-416.jar -O paperclip/paper-1.16.4.jar
java -jar paper-1.16.4.jar
mvn install:install-file -Dfile=cache/patched_1.16.4.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.4-R0.1-SNAPSHOT" -Dpackaging="jar"
```

### Installing Paper via the maven plugin
For versions 1.17-1.18, we use Paper via the [paper-nms-maven-plugin](https://github.com/Alvinn8/paper-nms-maven-plugin). To install these versions locally, we must run a few maven commands. These commands should be ran in the root directory of the project.
```
mvn paper-nms:init -pl nms/1_17_0
mvn paper-nms:init -pl nms/1_17_1
mvn paper-nms:init -pl nms/1_18_0
mvn paper-nms:init -pl nms/1_18_1
mvn paper-nms:init -pl nms/1_18_2
mvn paper-nms:init -pl nms/1_19_0
mvn paper-nms:init -pl nms/1_19_1
mvn paper-nms:init -pl nms/1_19_2
```

Your environment is now set up correctly. To create a build, run the following inside the root folder of the project.
```
mvn clean package
```
Your build is now available in the /IF/target folder.

## Adventure support

IF supports [Adventure](https://github.com/KyoriPowered/adventure), but does not shade it in itself.
The use of Adventure `Component`s instead of legacy `String`s is completely optional.
If you do not wish to use Adventure you can safely ignore all `TextHolder` related methods.

### What is Adventure?

Adventure is a library that adds proper modern text support to Minecraft.
Modern text is represented using bungee-chat and `BaseComponent` instances in Spigot.
Adventure is an alternative to bungee-chat and offers more features.

### Using Adventure on 1.16.5+ Paper

You don't need to import/shade anything for Adventure support in this case!

*Note: Paper only supports Adventure on build 473 and above. If you aren't running months old builds, then you are fine.*

### Using Adventure on Spigot and older Paper

On Spigot Adventure isn't included in the server, therefore you have to shade and relocate it yourself.
The following dependencies need to be imported and shaded:
- adventure-api
- adventure-platform-bukkit

Please consult the [Adventure documentation](https://docs.adventure.kyori.net/) for more information.

### How to use Adventure `Component`s

Example of migration from legacy `String` to Adventure `Component`:
 - legacy: `namedGui.setTitle("My Title!");`
 - Adventure: `namedGui.setTitle(ComponentHolder.of(Component.text("My Title!")));`

We apologize for the boilerplate (the `ComponentHolder.of(...)` call), but that was the only way to not make IF hard-depend on Adventure.

Full Adventure support is only achieved when your server natively supports Adventure (it is running Paper) and your plugin depends on Paper (instead of Spigot).
In other words, you won't benefit from Adventure as much if you use Spigot instead of Paper.
This is because when Adventure is relocated we have to convert everything back to legacy `String`s before passing them to the Bukkit API.
