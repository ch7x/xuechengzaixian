package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 测试大文件上传方法
 */
public class BigFileTest {
    // 分块测试
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("f:\\1.mp4");
        // 分块存储路径
        String chunkFilePath = "f:\\chunk\\";
        // 分块文件大小
        int chunkSize = 1024 * 1024 * 5;
        // 分块文件个数
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流 从源文件中读数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        // 缓存区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    // 将分块合并
    @Test
    public void testMerge() throws IOException {
        // 块文件目录
        File chunkFolder = new File("f:\\chunk");
        // 源文件
        File sourceFile = new File("f:\\1.mp4");
        // 合并后的文件
        File mergeFile = new File("f:\\2.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        List<File> filesList = Arrays.asList(files);

        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        // 向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");

        // 缓存区
        byte[] bytes = new byte[1024];
        // 遍历分块文件，向合并的文件写
        for (File file : filesList) {
            // 读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();

        // 对合并后的流进行md5校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
        String md5_source = DigestUtils.md5Hex(fileInputStream_source);
        if (md5_merge.equals(md5_source)) {
            System.out.println("文件合并成功");
        }
    }




}
