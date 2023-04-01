package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 测试freemarker页面静态化方法
 */
@SpringBootTest
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;

    // 测试页面静态化
    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.getVersion());

        // 拿到classpath路径
        String classpath = this.getClass().getResource("/").getPath();
        // 指定模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        // 指定编码
        configuration.setDefaultEncoding("utf-8");
        // 得到模板
        Template template = configuration.getTemplate("course_template.ftl");
        //准备数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(122L);
        HashMap<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        // 输入流
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        // 输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:\\xuechengwenjian\\wangye\\122.html"));
        // 使用流将html写入文件
        IOUtils.copy(inputStream, fileOutputStream);

    }
}
