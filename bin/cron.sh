#!/bin/sh

prog=`basename $0`
script_dir=`dirname $0`
root_dir=`cd "$script_dir"/..; echo $PWD`
pom_file="$root_dir/pom.xml"
startdt=`date -d "-1 month -$(($(date +%e)-1)) days" +"%Y-%m-%d"`
enddt=`date -d "-$(date +%e) days" +"%Y-%m-%d"`

if [ ! "$1" ]; then
 echo "$prog: Ini file required" >&2
 exit 1
elif [ ! -r "$1" ]; then
  echo "$prog: $1: Ini file not found" >&2
  exit 1
else
  inifile="$1"
fi

if [ ! -r "$pom_file" ]; then
  echo "$prog: $pom_file: Maven POM file not found" >&2
  exit 1
fi

app_ver=`php -r '$x=simplexml_load_file($argv[1]); echo $x->version;' $pom_file`

echo "Running analytics for $startdt to $enddt using $inifile (ver=$app_ver)"
java -jar $root_dir/target/analytics-$app_ver.jar --start-date $startdt --end-date $enddt --ini-file $inifile

exit $?
