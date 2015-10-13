 mvn install:install-file \
   -Dfile=$1 \
   -DgroupId=com.betinvest \
   -DartifactId=poi \
   -Dversion=$2 \
   -Dpackaging=jar