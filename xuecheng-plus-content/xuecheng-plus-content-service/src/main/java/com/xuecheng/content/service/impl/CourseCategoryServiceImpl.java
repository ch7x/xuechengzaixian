package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //先将list转map,key是结点id,valve就CourseCategoryTreeDto.对象，目的是为了方便从map获取节点
        Map<String, CourseCategoryTreeDto> mapTemp = categoryTreeDtos.stream().
                // 排除根结点
                        filter(item -> !id.equals(item.getId())).
                collect(Collectors.toMap(CourseCategory::getId,
                        value -> value,
                        (key1, key2) -> key2));

        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();
        //从头遍历List<CourseCategoryTreeDto>,-边遍历一边找子节点放在父节点childrenTreeNodes
        categoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            if (item.getParentid().equals(id)) {
                courseCategoryTreeDtos.add(item);
            }
            //找到当前节点的父节点
            CourseCategoryTreeDto courseCategoryParent = mapTemp.get(item.getParentid());

            if (courseCategoryParent != null) {
                //如果父节点的ChildrenTreeNodes为空则new一个集合
                if (courseCategoryParent.getChildrenTreeNodes() == null) {
                    courseCategoryParent.setChildrenTreeNodes(new ArrayList<>());

                }

                courseCategoryParent.getChildrenTreeNodes().add(item);
            }

        });

        return courseCategoryTreeDtos;
    }
}
