package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试minio的sdk
 */
public class MinioTest {
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    @Test
    public void test_upload() throws Exception {

        // 通过扩展名得到媒体资源类型
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("F:\\1.jpg")
//                .object("1.jpg")
                .object("test/01/1.jpg")
                .contentType(mimeType)
                .build();

        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test
    public void test_delete() throws Exception {

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("1.jpg").build();
        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    public void test_getFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test/01/1.jpg").build();

        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);

        // 指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("F:\\1a.jpg"));
        IOUtils.copy(inputStream, outputStream);

        // 校验文件的完整性对文件内容进行md5
        FileInputStream fileInputStream = new FileInputStream(new File("F:\\1.jpg"));
        String source_md5 = DigestUtils.md5Hex(fileInputStream);
        FileInputStream fileInputStream1 = new FileInputStream(new File("F:\\1a.jpg"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream1);
        if (source_md5.equals(local_md5)) {
            System.out.println("下载成功");
        }
    }

    // 将分块文件上传到minio
    @Test
    public void uploadChunk() throws Exception {
        for (int i = 0; i < 19; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("F:\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();

            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功");
        }
    }

    // 调用minio接口合并分块
    @Test
    public void testMerge() throws Exception {
//        List<ComposeSource> sources = new ArrayList<>();
//        for (int i = 0; i < 58; i++) {
//            // 指定分块文件的信息
//            ComposeSource composeSource = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
//            sources.add(composeSource);
//        }

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(19)
                .map(i -> ComposeSource
                        .builder()
                        .bucket("testbucket")
                        .object("chunk/" + i).build()).collect(Collectors.toList());


        // 指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sources)
                .build();

        minioClient.composeObject(composeObjectArgs);
    }
    // 批量清理分块文件
}
