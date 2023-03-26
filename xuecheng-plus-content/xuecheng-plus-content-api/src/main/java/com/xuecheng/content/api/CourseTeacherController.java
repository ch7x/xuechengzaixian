package com.xuecheng.content.api;


import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 师资管理接口
 */
@Api(value = "师资管理接口", tags = "师资管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;


    @ApiOperation("师资查询接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> queryCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.queryCourseTeachList(courseId);
    }

    @ApiOperation("新增师资接口")
    @PostMapping("/courseTeacher")
    public void addTeacher(@RequestBody CourseTeacher courseTeacher) {
        Long companyId = 1232141425L;
        courseTeacherService.createCourseTeach(companyId, courseTeacher);
    }

    @ApiOperation("删除师资接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteTeach(@PathVariable Long courseId,@PathVariable Long id) {
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId,courseId, id);
    }


}
