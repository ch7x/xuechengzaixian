package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * 课程发布相关接口实现
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        // 课程基本信息,营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程找不到");
        }

        // 审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        // 如果课程的审核状态为已提交则不允许提交
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交");
        }
        // 课程的图片,计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请上传课程图片");
        }
        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("请编写课程计划");
        }
        // 查询到课程基本信息,营销信息,计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 设置机构id
        coursePublishPre.setCompanyId(companyId);
        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        // 课程计划信息
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        // 状态
        coursePublishPre.setStatus("202003");
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        // 查询预发布表,如果不存在则插入,如果存在则更新
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            // 插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本信息的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {

        // 查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录,无法发布");
        }
        // 课程如果没有审核通过不允许发布
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")) {
            XueChengPlusException.cast("课程没有审核通过不允许发布");
        }
        // 向课程发布表写数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        // 先查询课程发布表,如果没有则插入,有则更新
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        // 将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        Configuration configuration = new Configuration(Configuration.getVersion());

        // 最终返回的静态文件
        File htmlFile = null;
        try {
            // 拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            // 指定模板的目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 指定编码
            configuration.setDefaultEncoding("utf-8");
            // 得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            // 输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            // 输出文件
            htmlFile = File.createTempFile("coursepublish", ".html");
            FileOutputStream fileOutputStream = new FileOutputStream(htmlFile);
            // 使用流将html写入文件
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            log.error("页面静态化出现问题,课程id:{}", courseId, e);
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            // 将File转成MultipartFile
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.debug("远程调用走了降级逻辑得到上传结果为null,课程id:{}",courseId);
                XueChengPlusException.cast("上传静态文件过程中存在异常");
            }
        }catch (Exception e){
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件过程中存在异常");
        }

    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 保存消息表记录，稍后实现
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    /**
     * 根据课程id查询课程发布信息
     * @param courseId
     * @return
     */
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }


}

