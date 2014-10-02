#!/bin/sh

prog=`basename $0`
script_dir=`dirname $0`
root_dir=`cd "$script_dir"/..; echo $PWD`
pom_file="$root_dir/pom.xml"
startdt=`date -d "-1 month -$(($(date +%e)-1)) days" +"%Y-%m-%d"`
enddt=`date -d "-$(date +%e) days" +"%Y-%m-%d"`
inifile=

usage() {
  echo "Usage: $prog [--start-date YYYY-MM-DD] [--end-date YYYY-MM-DD] ini_file" >&2
}

check_date() {
  dt="$1"
  if echo "$dt" | egrep -q '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'; then
    return 1
  else
    return 0
  fi
}

while [ $# -gt 0 ]; do
  case "$1" in
    -s|--start*) shift; startdt="$1" ;;
    -e|--end*) shift; enddt="$1" ;;
    -*) echo "$prog: $1: Invalid option" >&2; usage; exit 1 ;;
    *) inifile="$1" ;;
  esac
  shift
done

if [ ! "$inifile" ]; then
 echo "$prog: Configuration file is a required parameter" >&2
 exit 1
elif [ ! -r "$inifile" ]; then
  echo "$prog: $1: Ini file not found" >&2
  exit 1
elif check_date "$startdt"; then
  echo "$prog: $startdt: Invalid start date" >&2
  exit 1
elif check_date "$enddt"; then
  echo "$prog: $enddt: Invalid end date" >&2
  exit 1
fi

if [ ! -r "$pom_file" ]; then
  echo "$prog: $pom_file: Maven POM file not found" >&2
  exit 1
fi

app_ver=`php -r '$x=simplexml_load_file($argv[1]); echo $x->version;' $pom_file`

echo "Running analytics for $startdt to $enddt using $inifile (ver=$app_ver)"
java -jar $root_dir/target/analytics-$app_ver.jar --start-date $startdt --end-date $enddt --ini-file $inifile

exit $?
