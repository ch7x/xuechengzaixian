package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    CoursePublishService coursePublishService;

    @ApiOperation("课程分页查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") // 指定权限标识符,拥有此权限才能访问此方法
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false)
                                       QueryCourseParamsDto queryCourseParamsDto) {
        // 当前用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        // 用户所属机构id
        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseInfoService.queryCourseBaseList(companyId,pageParams, queryCourseParamsDto);
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
        // 获取当前用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
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

        courseBaseInfoService.deleteCourseBase(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        // 查询课程发布信息
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }

}
