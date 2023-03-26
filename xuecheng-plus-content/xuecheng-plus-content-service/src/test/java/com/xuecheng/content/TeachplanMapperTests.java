package com.xuecheng.content;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 课程计划测试
 */
@SpringBootTest
public class TeachplanMapperTests {
    @Autowired
    TeachplanMapper teachplanMapper;


    @Test
    void testCourseCategoryMapper() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117);
        System.out.println(teachplanDtos);
    }
}
