package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> queryCourseTeachList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(qw);
    }

    @Override
    public void createCourseTeach(Long companyId, CourseTeacher courseTeacher) {
        courseTeacher.setCreateDate(LocalDateTime.now());
        if (courseTeacher.getId() == null) {
            if (courseTeacherMapper.insert(courseTeacher) <= 0) {
                XueChengPlusException.cast("新增失败");
            }
        } else {
            if (courseTeacherMapper.updateById(courseTeacher) <= 0) {
                XueChengPlusException.cast("修改失败");
            }
        }
    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long id) {
        LambdaQueryWrapper<CourseTeacher> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseTeacher::getCourseId, courseId).
                eq(CourseTeacher::getId, id);
        if (courseTeacherMapper.delete(qw) <= 0) {
            XueChengPlusException.cast("删除失败");
        }
    }


}
