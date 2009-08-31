#!/bin/zsh

if [ "$1" = "" ]
then
	echo "$0: need a l10n file as first argument.";
	exit 1
fi

echo -n > .temp

for x in ../**/*.java
do
	cat $x | grep "getBaseL10n().getString(\"" | sed -s "s/^.*getBaseL10n()\.getString(\"\(.\+\)\").*$/\1/" | sed -s "s/\".*$//" | uniq | sort >> .temp
done

echo -n > .temp2

for x in `cat .temp`
do
	CONTAINS=`cat $1 | sed -s "s/=.*//" | grep $x`
	if [ "$CONTAINS" = "`echo`" ]
	then
		echo $x >> .temp2
	fi	
done

cat .temp2 | sort | uniq 

rm -f .temp .temp2
