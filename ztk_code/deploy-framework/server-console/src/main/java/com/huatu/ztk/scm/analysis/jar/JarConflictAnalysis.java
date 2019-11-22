package com.huatu.ztk.scm.analysis.jar;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 该类用于分析jar包冲突
 * Created by shaojieyue on 7/24/14.
 */
public class JarConflictAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(JarConflictAnalysis.class);

    /**
     * 解析path下jar包冲突
     * @param path jar包所在目录
     * @return 冲突jar包列表
     */
    public static String[] analysis(String path) {
        logger.info("analye jars,path: "+path);
        File file = new File(path);
        if (!file.isDirectory()) {
            return new String[0];
        }
        //列出目录下所有jar包
        File[] jars = getJars(file);
        Multimap<String, String> map = ArrayListMultimap.create();
        //遍历jar
        for (File jar : jars) {
            //获取jar下所有文件名
            List<String> classes = listClasses(jar.getAbsolutePath());
            for (String clazzName : classes) {
                //利用multi map进行重复判断
                map.put(clazzName, jar.getName());
            }
        }

        Iterator<String> keys = map.keySet().iterator();
        String key = null;
        Set<String> repeat = new HashSet<String>();
        while (keys.hasNext()) {
            key = keys.next();
            Collection<String> files = map.get(key);
            //size 大于1，说明多个jar包含同一文件，则jar冲突
            if (files.size() > 1) {
                if(key.endsWith("class")){
                    repeat.add(collection2Str(files));
                }else if(key.endsWith("xml")||key.endsWith("properties")||key.endsWith("yml")){
                    //资源文件冲突，则把资源文件名打印出来
                    repeat.add(key+"\t"+collection2Str(files));
                }
            }
        }
        String[] array = new String[repeat.size()];
        repeat.toArray(array);
        return array;
    }

    private static String[] listZipFiles(String zip){
        try {
            ZipFile zipFile = new ZipFile(zip);
            Enumeration emu = zipFile.entries();
            while(emu.hasMoreElements()){
                ZipEntry entry = (ZipEntry)emu.nextElement();

                ZipInputStream stream = new ZipInputStream(zipFile.getInputStream(entry));
                JarInputStream jarInputStream = new JarInputStream(zipFile.getInputStream(entry));
                //会把目录作为一个file读出一次，所以只建立目录就可以，之下的文件还会被迭代到。
                System.out.println(entry);
                if (entry.isDirectory()) {
                }
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 集合转为字符串 集合里的字符串用空格隔开
     * @param coll
     * @return
     */
    private static String collection2Str(Collection<String> coll){
        Iterator<String> strs = coll.iterator();
        StringBuilder sb = new StringBuilder();
        while (strs.hasNext()){
            sb.append(strs.next());
            sb.append(" ");
        }
        return sb.toString();
    }


    /**
     * 获取dir目录下所有的jar包
     *
     * @param dir
     * @return
     */
    private static File[] getJars(File dir) {
        File[] jars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("jar");
            }
        });
        return jars;
    }

    /**
     * 列出jar包里所有的class
     *
     * @param jarPath
     * @return
     */
    private static List<String> listClasses(String jarPath) {
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            logger.error("jar ex,path:" + jarPath, e);
            return new ArrayList<String>();
        }
        Enumeration allEntries = jarFile.entries();
        List<String> classes = new ArrayList<String>();
        while (allEntries.hasMoreElements()) {
            JarEntry entry = (JarEntry) allEntries.nextElement();
            String className = null;
            if (!entry.isDirectory()) {//不处理目录
                className = entry.getName();
                if (className.startsWith("META-INF")) {//META信息不处理
                    continue;
                }
                classes.add(entry.getName());
            }
        }
        return classes;
    }

    public static void main(String[] args) {

        String[] aa = analysis("/home/shaojieyue/lib");
        for(String s:aa){
            System.out.println(s);
        }
    }
}
