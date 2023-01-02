chmod +x mvnw
curl https://papermc.io/api/v2/projects/paper/versions/1.14.4/builds/243/downloads/paper-1.14.4-243.jar -o paper-1.14.4.jar
java -jar paper-1.14.4.jar
mvnw install:install-file -Dfile=cache/patched_1.14.4.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.14.4-R0.1-SNAPSHOT" -Dpackaging="jar"
curl https://papermc.io/api/v2/projects/paper/versions/1.15.2/builds/391/downloads/paper-1.15.2-391.jar -o paper-1.15.2.jar
java -jar paper-1.15.2.jar
mvnw install:install-file -Dfile=cache/patched_1.15.2.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.15.2-R0.1-SNAPSHOT" -Dpackaging="jar"
curl https://papermc.io/api/v2/projects/paper/versions/1.16.1/builds/138/downloads/paper-1.16.1-138.jar -o paper-1.16.1.jar
java -jar paper-1.16.1.jar
mvnw install:install-file -Dfile=cache/patched_1.16.1.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.1-R0.1-SNAPSHOT" -Dpackaging="jar"
curl https://papermc.io/api/v2/projects/paper/versions/1.16.3/builds/253/downloads/paper-1.16.3-253.jar -o paper-1.16.3.jar
java -jar paper-1.16.3.jar
mvnw install:install-file -Dfile=cache/patched_1.16.3.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.3-R0.1-SNAPSHOT" -Dpackaging="jar"
curl https://papermc.io/api/v2/projects/paper/versions/1.16.4/builds/416/downloads/paper-1.16.4-416.jar -o paper-1.16.4.jar
java -jar paper-1.16.4.jar
mvnw install:install-file -Dfile=cache/patched_1.16.4.jar -DgroupId="io.papermc" -DartifactId="paper" -Dversion="1.16.4-R0.1-SNAPSHOT" -Dpackaging="jar"
mvnw paper-nms:init -pl nms/1_17_0
mvnw paper-nms:init -pl nms/1_17_1
mvnw paper-nms:init -pl nms/1_18_0
mvnw paper-nms:init -pl nms/1_18_1
mvnw paper-nms:init -pl nms/1_18_2
mvnw paper-nms:init -pl nms/1_19_0
mvnw paper-nms:init -pl nms/1_19_1
mvnw paper-nms:init -pl nms/1_19_2
mvnw paper-nms:init -pl nms/1_19_3