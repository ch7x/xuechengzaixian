package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程基本信息管理业务接口
 */
public interface TeachplanService {

    /**
     * 查询课程计划树型结构
     *
     * @param courseId 课程id
     * @return List<TeachplanDto>
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增/修改/保持课程计划
     *
     * @param teachplanDto 课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto teachplanDto);


    /**
     * 删除课程计划
     *
     * @param teachplanId 课程id
     */
    void deleteTeachplan(Long teachplanId);

    /**
     * 上移课程
     *
     * @param teachplanId 课程id
     */
    void moveUp(Long teachplanId);

    /**
     * 下移课程
     *
     * @param teachplanId 课程id
     */
    void moveDown(Long teachplanId);
}

