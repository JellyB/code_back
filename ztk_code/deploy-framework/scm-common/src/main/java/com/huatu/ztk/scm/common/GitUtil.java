package com.huatu.ztk.scm.common;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用java代码调用git命令的工具包
 *
 * @author shaojieyue
 * @date 2013-07-30 16:29:04
 */
public class GitUtil {
    /**
     * 前缀分隔符
     */
    private static final String PREFX_SPLIT_CHAR = "_";
    private static final String VERSION_SPLIT_CHAR = ".";
    private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    static{
        CredentialsProvider user = new UsernamePasswordCredentialsProvider("deploy", "123456");
        CredentialsProvider.setDefault(user);
    }

    /**
     * 列出project的所有分支
     *
     * @param projectHome project 的目录
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static String[] listBranch(String projectHome) throws IOException, GitAPIException {
        File root = new File(projectHome);
        Git git = Git.open(root);
        List<Ref> list = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        if (list == null) {
            return new String[0];
        }
        String[] branchs = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            branchs[i] = list.get(i).getName();
        }
        return branchs;
    }

    /**
     * 列出一个工程的所有tag号
     *
     * @param projectHome 工程的根目录
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static List<Tag> listTag(String projectHome) throws IOException, GitAPIException {
        File root = new File(projectHome);
        Git git = Git.open(root);
        git.pull().setCredentialsProvider(CredentialsProvider.getDefault()).call();//先进行pull,不然tag号取不全
        List<Ref> list = git.tagList().call();
        RevWalk walk = new RevWalk(git.getRepository());
        if (list == null) {
            return new ArrayList<Tag>();
        }
        List<Tag> tags = new ArrayList<Tag>(list.size());
        for (int i = list.size() - 1; i > 0; i--) {//降序
            RevTag tag = walk.parseTag(list.get(i).getObjectId());
            Tag tagInfo = parseTag(tag);
            tags.add(tagInfo);
        }

        Collections.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o2.getCreateTime().compareTo(o1.getCreateTime());
            }

        });
        //取前50个tag
        return tags;
    }


    private static Tag parseTag(RevTag tag){
        Tag tagInfo = new Tag();
        Date when = tag.getTaggerIdent().getWhen();
        String fullMsg = tag.getFullMessage();
        //module user remark
        String[] arr = fullMsg.split("##", 4);
        tagInfo.setCreateTime(formatter.format(when));
        tagInfo.setTagName(tag.getTagName());
        if (arr.length == 4) {
            tagInfo.setModule(arr[0]);
            tagInfo.setCreateBy(arr[1]);
            tagInfo.setRemark(arr[2]);
            tagInfo.setBranch(arr[3]);
        } else if(arr.length==3) {
            tagInfo.setModule(arr[0]);
            tagInfo.setCreateBy(arr[1]);
            tagInfo.setRemark(arr[2]);
        }
        return tagInfo;
    }

    public static Tag getTag(String projectHome,String currentTag) throws IOException, GitAPIException {
        File root = new File(projectHome);
        Git git = Git.open(root);
        git.pull().setCredentialsProvider(CredentialsProvider.getDefault()).call();//先进行pull,不然tag号取不全
        List<Ref> list = git.tagList().call();
        RevWalk walk = new RevWalk(git.getRepository());
        if (list == null) {
            return null;
        }
        for (int i = list.size() - 1; i > 0; i--) {
            RevTag tag = walk.parseTag(list.get(i).getObjectId());
            if(tag.getTagName().equals(currentTag)){
                return parseTag(tag);
            }
        }
        return null;
    }

    /**
     * 判断一个tag是否已经存在
     *
     * @param projectHome
     * @param tag
     * @return true:存在 false:不存在
     * @throws IOException
     * @throws GitAPIException
     */
    public static boolean isExistTag(String projectHome, String tag) throws IOException, GitAPIException {
        List<Tag> tags = listTag(projectHome);
        boolean isExist = false;
        if (tags == null) {
            return false;
        }

        for (Tag t : tags) {
            if (t.getTagName().equals(tag)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    /**
     * 根据当前tag号,生成新的tag号,注意:tag号的格式为%_数字.数字.数字...
     *
     * @param projectHome 项目的目录,新生成的tag会和该project做对比,指导生成不存在的tag号
     * @param currentTag
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static String autoGenerateTag(String projectHome, String currentTag) throws IOException, GitAPIException {
        List<Tag> tags = listTag(projectHome);
        String newTag = autoGenerateTag(currentTag);
        while (true) {
            boolean exist = false;
            for (int i = 0; i < tags.size(); i++) {
                if (tags.get(i).getTagName().equals(newTag)) {//存在已生成的tag
                    exist = true;
                    logger.info("tag " + newTag + " is exist");
                    break;
                }
            }
            if (!exist) {
                break;
            } else {
                newTag = autoGenerateTag(newTag);
            }
        }

        return newTag;
    }

    /**
     * 根据当前tag号,生成新的tag号,注意:tag号的格式为%_数字.数字.数字...
     *
     * @param currentTag
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public static String autoGenerateTag(String currentTag) throws IOException, GitAPIException {
        String[] arr = currentTag.split(PREFX_SPLIT_CHAR);
        if (arr.length < 2) {
            throw new IllegalGitTagPatternException("illegal git tag " + currentTag);
        }

        String mathVersion = arr[arr.length - 1];
        String[] arr1 = mathVersion.split("\\" + VERSION_SPLIT_CHAR);
        int[] lens = new int[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            //分隔符间版本位数
            lens[i] = arr1[i].length();
        }
        //将数字版本号间的分隔符去掉,例如1.1.1取出后为111
        String oldVerStr = mathVersion.replaceAll("\\" + VERSION_SPLIT_CHAR, "");
        long verMath = 0;
        try {
            //版本号累加1
            verMath = Long.valueOf(oldVerStr) + 1;
            if (verMath % 10 == 0) {//末位为0,则继续累加1
                verMath++;
            }
        } catch (Exception e) {
            throw new IllegalGitTagPatternException("illegal git tag " + currentTag);
        }

        if (oldVerStr.length() != (verMath + "").length()) {//超过最大限定抛出异常如:t_9.9,累加后为t_10.1
            throw new GitTagOutOfMaxValueException("old tag math :" + oldVerStr + " new tag math:" + verMath);
        }

        StringBuilder newTag = new StringBuilder();

        for (int i = 0; i < arr.length - 1; i++) {//字符前缀组装
            newTag.append(arr[i]);
            newTag.append(PREFX_SPLIT_CHAR);
        }

        String newVerStr = verMath + "";
        String tmp = null;
        int beginIndex = 0;
        for (int i = 0; i < lens.length; i++) {//数字版本组装
            tmp = newVerStr.substring(beginIndex, beginIndex + lens[i]);
            beginIndex = beginIndex + lens[i];
            newTag.append(tmp);
            newTag.append(VERSION_SPLIT_CHAR);
        }

        return newTag.substring(0, newTag.length() - 1);


    }

    public static void main(String[] args) throws IOException, GitAPIException {
        String path="/home/shaojieyue/tools/workspace/smc-api";
        String ss = autoGenerateTag(path,"r_3.9.05");
        System.out.println(ss);
    }
}

class IllegalGitTagPatternException extends RuntimeException {

    public IllegalGitTagPatternException(String msg) {
        super(msg);
    }

}

class GitTagOutOfMaxValueException extends RuntimeException {

    public GitTagOutOfMaxValueException(String msg) {
        super(msg);
    }

}
