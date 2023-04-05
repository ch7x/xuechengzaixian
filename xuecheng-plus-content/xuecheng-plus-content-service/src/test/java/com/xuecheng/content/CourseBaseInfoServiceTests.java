package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseInfoServiceTests {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @Test
    void testCourseBaseInfoService() {

        //查询条件模型
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");
        courseParamsDto.setAuditStatus("202004");

        //分页参数模型
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(3L);

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(null, pageParams, courseParamsDto);

        System.err.println(courseBasePageResult);
    }
}
