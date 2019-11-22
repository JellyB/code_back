#!/bin/bash
start_time=`date +%s`
########################parm############################
package_name="${module}-dist.zip"
package_dir="${project}/${module}/target"
git_branch=${branch}
env_package_cmd="mvn  clean package ${updateDependency} -Dmaven.test.skip=true -Dmaven.compile.fork=true -pl ${module} -am -Pproduct"
if [ ${environment} -a ${environment}!="" ]; then
    env_package_cmd="mvn  clean package ${updateDependency} -Dmaven.test.skip=true -Dmaven.compile.fork=true -pl ${module} -am -Pproduct "
fi
echo "${env_package_cmd}"
#base="/tmp"
base="/data/projects"
build=${base}/projects

link_path="${base}/dist-source/${module}-dist.zip"

########################################################

#online + develop + tag
if [ ${type} == "1" ] ; then
    tag_build_project=${build}/build_tag/${project}
    dist_tag_file=${base}/dist-backup/${package_name}.${tag}
    echo "build dir: ${tag_build_project}, tag:${tag}"
    if [ ! -d ${tag_build_project} ]; then
        echo "${tag_build_project} is not exist or not a dir"
        exit 1
    fi
	cd ${tag_build_project}
    #mark tag
    msg="${tag_remark}"
    git fetch origin
    git checkout ${branch}
    rm -rf ${tag_build_project}/*
    git pull origin ${branch}
    git reset --hard HEAD
	echo "git tag -m ${msg} ${tag}"
    git tag -m "${msg}" ${tag}

    #error output
    if [ $? != 0 ]; then
        echo "tag exist [${tag}]"
        exit 1
    fi
    
   # git push origin ${tag}
   # git push origin --tags

    #error output
  #  if [ $? != 0 ]; then
  #      echo 'tar fail'
   #     exit 1
  #  fi

    #mvn execute
    ${env_package_cmd}

    if [ -f ${dist_tag_file} ] ; then
        echo "Delete the exist package ${dist_tag_file}"
        rm -f ${dist_tag_file}
    fi

    echo "cp ${tag_build_project}/${module}/target/${module}-*-dist.zip ${dist_tag_file}"
    cp ${tag_build_project}/${module}/target/${module}-*-dist.zip ${dist_tag_file}

    #link
    echo "deploy to dist[${base}/${package_name}"
    if [ -f "${link_path}" ]; then
        unlink ${link_path}
    fi
    ln ${dist_tag_file} ${link_path}

else
    #develop env
	cd ${build}/${project}/
	git fetch origin
    git checkout ${branch}
    rm -rf *
    git pull origin ${branch}
    git reset --hard HEAD
    #mvn execute
    ${env_package_cmd}
    cd ${module}
	if [ ! -f target/${module}-*-dist.zip ];  then
		echo "error:no such file target/${module}-*-dist.zip package fail"
		exit 1
	fi
		
    cp target/${module}-*-dist.zip ${link_path}
fi

echo "package finish."
end_time=`date +%s`
echo "exec time $((end_time-start_time)) s"
exit 0

