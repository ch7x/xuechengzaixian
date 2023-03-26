package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false)
                                       QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody
                                              //参数校验
                                              @Validated(ValidationGroups.Inster.class)
                                              AddCourseDto addCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }


    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody
                                              @Validated(ValidationGroups.Update.class)
                                              EditCourseDto editCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程基础")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseBase(@PathVariable Long courseId) {
        Long companyId = 1232141425L;

        courseBaseInfoService.deleteCourseBase(companyId,courseId);
    }


}
