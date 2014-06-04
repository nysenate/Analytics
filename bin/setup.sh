#!/bin/sh

script_dir=`dirname $0`
root_dir=`cd $script_dir/..; echo $PWD`
lib_dir=$root_dir/lib

function mvn_install {
    mvn install:install-file -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dfile=$4 -Dpackaging=jar -DgeneratePom=true
}

mvn_install org.winterwell      jtwitter                2.8.2     $lib_dir/jtwitter.jar
mvn_install com.livestream.api  livestream-api-v2       1.0     $lib_dir/livestream-api-v2.jar
mvn_install oauth.signpost      oauth-signpost          1.2.1.2 $lib_dir/signpost-core-1.2.1.2.jar
