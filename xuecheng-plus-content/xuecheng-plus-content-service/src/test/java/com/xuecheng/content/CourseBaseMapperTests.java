package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper courseBaseMapper;


    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);

        //查询条件模型
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");

        //分页参数模型
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(3L);

        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),
                CourseBase::getName,
                courseParamsDto.getCourseName());

        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                courseParamsDto.getAuditStatus());

        //todo:按课程发布状态查询

        //page对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();

        //返回结果
        PageResult<CourseBase> result = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

        System.err.println(result);
    }
}
