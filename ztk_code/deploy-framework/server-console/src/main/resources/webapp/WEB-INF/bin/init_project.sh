#!/usr/bin/env bash
base="/data/projects"
build=${base}/projects
project_home=${build}/${project}
echo ${project_home}
#------------#---------- init git ------------#---------
##########develop test env########
if [ ! -d "${project_home}" ] ; then
    echo "init project: ${project_home}"
    mkdir -p ${project_home}
    cd ${build}
    echo "git clone ${project_home}"
    git clone ${git_home} ${project_home}
else
	echo "${project_home} is exist, init fail"
	exit 1;
fi


####online env############
tag_build_project=${build}/build_tag/${project}
if [ ! -d ${tag_build_project} ]; then
	echo "mkdir -p ${tag_build_project}"
    mkdir -p ${tag_build_project}
    cd ${build}/build_tag
    echo "git clone ${git_home} ${tag_build_project}"
    git clone ${git_home} ${tag_build_project}
else
	echo "${tag_build_project} is exist, init fail"
	exit 1;
fi