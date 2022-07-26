yum install java-1.8.0-openjdk* wget -y -q
wget -q https://archive.apache.org/dist/maven/maven-3/3.8.5/binaries/apache-maven-3.8.5-bin.tar.gz
tar -xvf apache-maven-3.8.5-bin.tar.gz
./apache-maven-3.8.5/bin/mvn -DskipTests=true package
