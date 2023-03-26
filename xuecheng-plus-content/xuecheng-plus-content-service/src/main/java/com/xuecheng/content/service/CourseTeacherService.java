package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    /**
     * 师资分页查询
     *
     * @param courseId 课程id
     * @return 查询结果
     */
    List<CourseTeacher> queryCourseTeachList(Long courseId);

    /**
     * 添加师资基本信息
     *
     * @param companyId     教学机构id
     * @param courseTeacher 老师基本信息
     */
    void createCourseTeach(Long companyId, CourseTeacher courseTeacher);

    /**
     * 删除师资基本信息
     *
     * @param companyId 教学机构id
     * @param courseId  课程id
     * @param id        老师
     */
    void deleteCourseTeacher(Long companyId, Long courseId, Long id);
}
