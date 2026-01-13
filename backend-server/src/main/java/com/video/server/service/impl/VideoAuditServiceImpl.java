package com.video.server.service.impl;

import com.video.server.constant.VideoStatus;
import com.video.server.dto.VideoAuditRequest;
import com.video.server.service.VideoAuditService;
import com.video.server.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 视频审核服务实现类
 */
@Service
@RequiredArgsConstructor
public class VideoAuditServiceImpl implements VideoAuditService {
    
    private final VideoService videoService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String HOT_VIDEO_LIST_KEY = "hot:video:list";
    
    @Override
    public void auditVideo(VideoAuditRequest request) {
        String status;
        if ("pass".equalsIgnoreCase(request.getAction())) {
            status = VideoStatus.PASSED.name();
            // 删除热门视频缓存
            redisTemplate.delete(HOT_VIDEO_LIST_KEY);
        } else if ("reject".equalsIgnoreCase(request.getAction())) {
            status = VideoStatus.REJECTED.name();
        } else {
            throw new IllegalArgumentException("无效的审核操作: " + request.getAction());
        }
        
        // 更新视频状态
        videoService.updateStatus(request.getVideoId(), status);
    }
}
