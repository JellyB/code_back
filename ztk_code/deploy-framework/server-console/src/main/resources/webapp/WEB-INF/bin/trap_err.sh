#!/bin/bash
# author:dongyongwang@sohu-inc.com
# shell error handler
function ERRTRAP()
{
  echo "Fatal [FILE:$1 LINE:$2] exit status $?"
  exit 1
}

trap 'ERRTRAP $0 $LINENO' ERR 
