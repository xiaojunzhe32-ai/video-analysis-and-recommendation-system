package com.video.server.service;

import com.video.server.dto.VideoAuditRequest;

/**
 * 视频审核服务接口
 */
public interface VideoAuditService {
    
    /**
     * 审核视频
     * @param request 审核请求
     */
    void auditVideo(VideoAuditRequest request);
}
