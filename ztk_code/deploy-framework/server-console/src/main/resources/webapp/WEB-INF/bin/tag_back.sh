#!/bin/bash
echo "###########parms#############"
echo "module=${module}"
echo "tag=${tag}"
echo "###########parms#############"
base="/data/deploy"
package_name="${module}-dist.zip"
environment="online"
dist_tag_file=${base}/dist-backup/${package_name}.${tag}
link_path="${base}/dist-source/${module}.zip"
if [ ! -f "${dist_tag_file}" ]; then
	echo "${dist_tag_file} not exist,tag back fail"
	exit 1
fi

if [ -f "${link_path}" ]; then
    unlink ${link_path}
fi

echo "ln ${dist_tag_file} ${link_path}"
ln ${dist_tag_file} ${link_path}

if [ ! $? == 0 ]; then 
	echo "ln file fail."
	exit 1
fi

echo "tag back success"
exit 0