#!/bin/sh

CLOC="/home/exp/down/devel/loc/cloc-1.02.pl"
CLOCNB="/home/exp/down/devel/loc/cloc-nobraket.pl"
WS="/home/exp/data/devel/workspace-ecg"

$CLOC --no3 --exclude-dir=.svn,parsers,diffmatchpatch,codereplay $WS/CPC*

$CLOCNB --no3 --exclude-dir=.svn,parsers,diffmatchpatch,codereplay $WS/CPC*
