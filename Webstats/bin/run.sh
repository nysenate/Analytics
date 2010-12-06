#!/bin/sh
#

basedir=`dirname $0`/..
libdir=$basedir/lib
classdir=$basedir/classes
today=`date +'%Y%m%d'`
outdir="webstats_$today"

mkdir -p $outdir

CLASSPATH="$classdir:$libdir/gdata-core-1.0.jar:$libdir/gdata-analytics-2.0.jar:$libdir/google-collect-1.0-rc1.jar:$libdir/jtwitter.jar:$libdir/json.jar"
#CLASSPATH="$CLASSPATH:$libdir/gdata-analytics-meta-2.0.jar:$libdir/jsr305.jar"
[ "$OSTYPE" = "cygwin" ] && CLASSPATH=`cygpath -p -w "$CLASSPATH"`
echo "Using CLASSPATH=$CLASSPATH"
export CLASSPATH

java gov.nysenate.webstats.WebstatsMain $@

mv *.csv $outdir && zip -r $outdir.zip $outdir

exit $?
