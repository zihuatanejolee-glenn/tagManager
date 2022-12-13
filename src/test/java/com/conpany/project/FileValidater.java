package com.conpany.project;

import com.jun.li.util.HashUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class FileValidater {

    /**
     * 判断文件有没有内容相同的
     *
     * @throws Exception
     */
    @Test
    public void checkSum() throws Exception {
        Map<String, String> md5info = new HashMap<>();
        // 校验文件md5是否有相同的
        File file = new File("D:\\validatefiles\\");
        File[] files = file.listFiles();
//        File[] allfiles = ArrayUtils.addAll(files);
        for (File file1 : files) {
            if (!file1.isFile() || !file1.getName().endsWith(".txt")) {
                continue;
            }
            String md51 = HashUtils.encrypt(file1, "MD5");
            if (md5info.get(md51) == null) {
                md5info.put(md51, file1.getName());
            } else {
                System.out.println("MD5重复了 已添加进去的文件是：" + md5info.get(md51) + " 待添加进去的文件是：" + file1.getName());
            }
        }
    }


    /**
     * 判断有没有文件名一样的
     *
     * @throws Exception
     */
    @Test
    public void checkFileName() throws Exception {
        Map<String, String> fileInfo = new HashMap<>();
        // 校验文件md5是否有相同的
        File file = new File("D:\\validatefiles\\");
        File[] files = file.listFiles();
//        File[] allfiles = ArrayUtils.addAll(files);
        for (File one : files) {
            if (!one.isFile() || !one.getName().endsWith(".txt")) {
                continue;
            }
            if (fileInfo.get(one.getName()) != null) {
                // 有值说明有重复
                System.out.println("发现文件名有重复的 已有的文件路径为:" + fileInfo.get(one.getName())
                        + ", 待加入的文件路径为：" + one.getCanonicalPath());
            } else {
                fileInfo.put(one.getName(), one.getCanonicalPath());
            }

        }
    }

    /**
     * 判断记录有没有重复的
     *
     * @throws IOException
     */
    @Test
    public void filter() throws IOException {

        File file = new File("D:\\validatefiles\\");
        File[] files = file.listFiles();
        Map<String, String> map = new HashMap<>();
        for (File file1 : files) {
            if (!file1.isFile() || !file1.getName().endsWith(".txt")) {
                continue;
            }
            List<String> metaLines = IOUtils
                    .readLines(new InputStreamReader(Files.newInputStream(file1.toPath()), StandardCharsets.UTF_8));

            for (int i = 0; i < metaLines.size(); i++) {
                String value = map.get(metaLines.get(i));
                if (value == null) {
                    map.put(metaLines.get(i), file1.getName() + ":" + (i + 1));
                } else {
                    System.out.println("文件内容记录重复了 已添加进去的记录是：" + metaLines.get(i) + ", 文件名:行数是 " + value + ", 待添加进去的文件名:行数是 " + file1.getName() + ":" + (i + 1));
                }
            }
        }
    }

    /**
     * 给文件加序列号
     *
     * @throws IOException
     */
    @Test
    public void addSerialNumberToFile() throws IOException {
        int lastIndex = 4;
        String checkedFilePath = "D:\\checkedfiles\\";
        File checkedFile = new File(checkedFilePath);
        File[] listFiles = checkedFile.listFiles();
        for (File source : listFiles) {
            if (!source.isFile() || !source.getName().endsWith(".txt")) {
                continue;
            }

            lastIndex++;
            if (lastIndex > 5) {
                lastIndex = 0;
            }
            String destPath = checkedFilePath + "serialnumberfiles\\" + lastIndex + "_" + source.getName();
            File dest = new File(destPath);
            FileUtils.copyFile(source, dest);
        }
    }

    @Test
    public void generate() throws IOException {
        List<String> newList = new ArrayList<>();
        List<String> metaLines = IOUtils
                .readLines(new InputStreamReader(Files.newInputStream(new File("D:\\uploadfiles\\0_test01.txt").toPath()), StandardCharsets.UTF_8));
        for (String metaLine : metaLines) {
            newList.add("F" + metaLine);
        }
        IOUtils.writeLines(newList, null, new FileOutputStream("D:\\uploadfiles\\_test03.txt", false), StandardCharsets.UTF_8);
    }
}
