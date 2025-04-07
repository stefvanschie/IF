#!/bin/zsh
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O BuildTools.jar

git clone https://hub.spigotmc.org/stash/scm/spigot/bukkit.git Bukkit
cd Bukkit
git checkout 304e83eb384c338546aa96eea51388e0e8407e26
cd ..

git clone https://hub.spigotmc.org/stash/scm/spigot/craftbukkit.git CraftBukkit
cd CraftBukkit
git checkout 91b1fc3f1cf89e2591367dca1fa7362fe376f289
cd ..

git clone https://hub.spigotmc.org/stash/scm/spigot/spigot.git Spigot
cd Spigot
git checkout b698b49caf14f97a717afd67e13fd7ac59f51089
cd ..

git clone https://hub.spigotmc.org/stash/scm/spigot/builddata.git BuildData
cd BuildData
git checkout a7f7c2118b877fde4cf0f32f1f730ffcdee8e9ee
cd ..

java -jar BuildTools.jar --remapped --disable-java-check --dont-update
java -jar BuildTools.jar --rev 1.20.6 --remapped --disable-java-check

cd Bukkit
git checkout 2ec53f498e32b3af989cb24672fc54dfab087154
cd ..

cd CraftBukkit
git checkout 8ee6fd1b8db9896590aa321d0199453de1fc35db
cd ..

cd Spigot
git checkout fb8fb722a327a2f9f097f2ded700ac5de8157408
cd ..

cd BuildData
git checkout ae1e7b1e31cd3a3892bb05a6ccdcecc48c73c455
cd ..

java -jar BuildTools.jar --remapped --disable-java-check --dont-update
java -jar BuildTools.jar --rev 1.21.1 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.3 --remapped --disable-java-check
java -jar BuildTools.jar --rev 1.21.4 --remapped --disable-java-check