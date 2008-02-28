#!/bin/sh

#usage: $0 <uuid> <file>
#prints to onlycloneuuid.out

echo "Searching for clone UUID $1 in file $2..."
cat "$2" | grep "$1" | grep --invert-match "originUuid: $1" > onlycloneuuid.out

