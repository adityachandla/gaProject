mvn clean compile
## Change the last part to the directory where you have your instances
mvn exec:java -Dexec.mainClass=com.ga.Main -Dexec.args=`pwd`/instances