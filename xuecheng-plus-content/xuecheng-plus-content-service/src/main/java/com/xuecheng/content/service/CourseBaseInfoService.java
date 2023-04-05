package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     *
     * @param pageParams           分页参数模型
     * @param queryCourseParamsDto 查询条件
     * @param companyId 培训机构id
     * @return 查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 添加课程基本信息
     *
     * @param companyId    教学机构id
     * @param addCourseDto 课程基本信息
     * @return 课程详细信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     *
     * @param courseId 课程id
     * @return 课程详细信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     *
     * @param companyId 机构id
     * @param dto       课程信息
     * @return 课程详细信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    /**
     * 删除课程
     *
     * @param companyId 机构id
     * @param courseId  课程id
     */
    void deleteCourseBase(Long companyId, Long courseId);

}
