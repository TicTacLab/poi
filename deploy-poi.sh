 mvn install:install-file \
   -Dfile=$1 \
   -DgroupId=com.betengines \
   -DartifactId=$2 \
   -Dversion=$3 \
   -Dpackaging=jar
