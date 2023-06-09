package com.xuecheng.content.service.jobhandler;


import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 课程发布的任务类
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();  // 执行器的总数

        // 调用抽象类方法执行任务
        process(shardIndex,shardTotal,"course_publish",30,60);

    }


    // 执行课程发布任务的逻辑,如果此方法抛出异常说明任务执行失败
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 从mqMessage拿到课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        // 课程静态化上传到minio
        generateCourseHtml(mqMessage, courseId);
        // 向elasticsearch写索引数据
        saveCourseIndex(mqMessage, courseId);
        // 向redis写缓存


        return true;
    }

    //生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        // 消息id
        Long taskId = mqMessage.getId();

        MqMessageService mqMessageService = this.getMqMessageService();

        // 做任务幂等性处理
        // 查询数据库取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程静态化任务完成,无需处理");
            return;
        }

        // 开始进行课程静态化    生成html文件
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }
        // 将html文件上传到minio
        coursePublishService.uploadCourseHtml(courseId,file);

        // 任务处理完成修改任务状态完成
        mqMessageService.completedStageOne(taskId);

    }

    //保存课程索引信息  第二个阶段任务
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        // 消息id
        Long taskId = mqMessage.getId();

        MqMessageService mqMessageService = this.getMqMessageService();
        // 取出第二个阶段状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        // 做任务幂等性处理
        if (stageTwo > 0) {
            log.debug("课程索引信息已写入,无需处理");
            return;
        }

        // 查询课程信息,调用搜索服务添加索引
        // 从课程发布表查询课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        // 远程调用
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add){
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }


        // 任务处理完成修改任务状态完成
        mqMessageService.completedStageTwo(taskId);
    }


}
