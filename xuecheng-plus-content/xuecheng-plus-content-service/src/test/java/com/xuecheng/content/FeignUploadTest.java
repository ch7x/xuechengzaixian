package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 测试远程调用媒资服务
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test() throws IOException {

        // 将File转成MultipartFile
        File file = new File("F:\\xuechengwenjian\\wangye\\122.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        String upload = mediaServiceClient.upload(multipartFile, "course/122.html");
        if (upload == null){
            System.out.println("走了降级逻辑");
        }

    }
}
