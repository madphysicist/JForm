#!/bin/bash

##
## install.sh
##
## JForm Project
##
## The MIT License (MIT)
##
## Copyright (c) 2013 by Joseph Fox-Rabinovitz
##
## Permission is hereby granted, free of charge, to any person obtaining a copy
## of this software and associated documentation files (the "Software"), to deal
## in the Software without restriction, including without limitation the rights
## to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
## copies of the Software, and to permit persons to whom the Software is
## furnished to do so, subject to the following conditions:
##
## The above copyright notice and this permission notice shall be included in
## all copies or substantial portions of the Software.
##
## THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
## IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
## FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
## AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
## LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
## OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
## THE SOFTWARE.
##

##
## This script is a Linux Mint/Ubuntu pseudo-install script. In its current
## incarnation it creates symlinks to the key parts of JForm. To copy instead of
## symlinking, change "ln -s" to "cp".
##
## Author:   Joseph Fox-Rabinovitz
## Version:  1.0.0, 03 Feb 2014: Joseph Fox-Rabinovitz: Created
##

PROG="ln -s"

ISRC=/usr/local/src
IDOC=/usr/local/share/doc
IJAR=/usr/local/share/java

BASE=madphysicist-JForm
JEXT=.jar

# Do not export!
function inst() {
    INSTALL_DIR="$1"
    FILE_SUFFIX="$2"
    FILE_NAME="${BASE}${FILE_SUFFIX}${JEXT}"
    TO_FILE="${INSTALL_DIR}/${FILE_NAME}"
    if [ -f "${TO_FILE}" ]
    then
        echo "Removing previous version from \"${TO_FILE}\""
        rm "${TO_FILE}"
    fi
    FROM_FILE="$(pwd)/dist/${FILE_NAME}"
    echo "Installing \"${FROM_FILE}\" into \"${INSTALL_DIR}\""
    ${PROG} "${FROM_FILE}" "${INSTALL_DIR}"
}

function uinst() {
    INSTALL_DIR="$1"
    FILE_SUFFIX="$2"
    FILE_NAME="${BASE}${FILE_SUFFIX}${JEXT}"
    TO_FILE="${INSTALL_DIR}/${FILE_NAME}"
    if [ -f "${TO_FILE}" ]
    then
        echo "Uninstalling from \"${TO_FILE}\""
        rm "${TO_FILE}"
    else
        echo "\"${FILE_NAME}\" not installed"
    fi
}

if [ ${#} -eq 1 -a "${1}" == "-u" ]
then    
    uinst "${ISRC}" "-sources"
    uinst "${IDOC}" "-javadoc"
    uinst "${IJAR}" ""
elif [ ${#} -ne 0 ]
then
    echo "Usage: ${0} [-u]"
    echo "    no argument installs JForm"
    echo "    -u uninstalls JForm"
    exit 1
else
    inst "${ISRC}" "-sources"
    inst "${IDOC}" "-javadoc"
    inst "${IJAR}" ""
fi

