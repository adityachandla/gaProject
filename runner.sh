mvn compile
# First argument is the source directory and the second argument is the output directory
mvn exec:java -Dexec.mainClass=com.ga.Main -Dexec.args="`pwd`/instances-2IMA15 `pwd`/instances-2IMA15-output/"
