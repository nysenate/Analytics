#!/bin/bash


root_dir=`dirname $0`/../
lib_dir=$root_dir/lib

function mvn_install {
    mvn install:install-file -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dfile=$4 -Dpackaging=jar -DgeneratePom=true
}

mvn_install com.google.gdata    gdata-analytics         2.0     $lib_dir/gdata-analytics-2.0.jar
mvn_install com.google.gdata    gdata-analytics-meta    2.0     $lib_dir/gdata-analytics-meta-2.0.jar
mvn_install com.google.gdata    gdata-core              1.0     $lib_dir/gdata-core-1.0.jar
mvn_install org.winterwell      jtwitter                1.0     $lib_dir/jtwitter.jar
mvn_install com.livestream.api  livestream-api-v2       1.0     $lib_dir/livestream-api-v2.jar
