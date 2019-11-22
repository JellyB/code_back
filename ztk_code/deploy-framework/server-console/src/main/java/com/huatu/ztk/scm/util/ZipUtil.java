package com.huatu.ztk.scm.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by shaojieyue on 8/2/14.
 */
public class ZipUtil {
    public static final int BUFFER_SIZE = 2048;

    private static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    //This function converts the zip file into uncompressed files which are placed in the
    //destination directory
    //destination directory should be created first

    public static boolean unzipFiles(String zipFile, String destDirectory) {
        try {
            //first make sure that all the arguments are valid and not null
            if (zipFile == null || zipFile.equals("")) {
                logger.error("zip file is null");
                return false;
            }
            if (destDirectory == null || destDirectory.equals("")) {
                logger.error("destDirectory is null");
                return false;
            }
            //now make sure that these directories exist
            File sourceFile = new File(zipFile);
            File destinationDirectory = new File(destDirectory);
            if (!sourceFile.exists()) {
                logger.error("source file is not exist");
                return false;
            }
            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs();
            }

            //now start with unzip process
            BufferedOutputStream dest = null;

            FileInputStream fis = new FileInputStream(sourceFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry entry = null;

            while ((entry = zis.getNextEntry()) != null) {
                String outputFilename = destDirectory + File.separator + entry.getName();
                createDirIfNeeded(destDirectory, entry);
                if(entry.isDirectory()) continue;
                int count;
                byte data[] = new byte[BUFFER_SIZE];
                //write the file to the disk
                FileOutputStream fos = new FileOutputStream(outputFilename);
                dest = new BufferedOutputStream(fos, BUFFER_SIZE);

                while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                    dest.write(data, 0, count);
                }

                //close the output streams
                dest.flush();
                dest.close();
            }

            //we are done with all the files
            //close the zip file
            zis.close();

        } catch (Exception e) {
            logger.error("unzip "+zipFile+" error",e);
            return false;
        }

        return true;
    }

    private static void createDirIfNeeded(String destDirectory, ZipEntry entry) {
        String name = entry.getName();

        if (name.contains("/")) {
            int index = name.lastIndexOf("/");
            String dirSequence = name.substring(0, index);
            File newDirs = new File(destDirectory + File.separator + dirSequence);
            //create the directory
            newDirs.mkdirs();
        }
    }

    public static void main(String[] args) throws IOException {
        //unzipFiles("/home/shaojieyue/smc-task-server-online.zip","/tmp");
        //File file = File.createTempFile("/aa","bb");
        //System.out.println(file.getAbsolutePath());
        File file = new File("/tmp/smc-task-server/online");
        String ss = "/home/shaojieyue/smc-task-server-online.zip";
        unzipFiles(ss,"/tmp/smc-task-server/online");
        FileUtils.forceDelete(file);
    }

}
