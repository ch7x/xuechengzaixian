package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }


    //实现如下
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0 ? false : true;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }

        // 如果任务执行失败
        if (status.equals("3")) {
            // 更新MediaProcess表的状态
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);

            /*// 高效的更新方式
            LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MediaProcess::getStatus,"3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.update(mediaProcess,queryWrapper);*/

            return;
        }

        // 如果任务执行成功
        // 文件表记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        // 更新media_file表中的url
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 更新MediaProcess表的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 将MediaProcess表的记录上传到MediaProcessHistory表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 从MediaProcess表删除当前记录
        mediaProcessMapper.deleteById(taskId);


    }

}
