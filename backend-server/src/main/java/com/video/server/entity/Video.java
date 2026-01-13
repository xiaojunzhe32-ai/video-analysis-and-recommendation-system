package com.video.server.entity;

import com.video.server.constant.VideoStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Data
public class Video {
    
    /**
     * 主键ID（自增）
     */
    private Long id;
    
    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 作者ID
     */
    private Long authorId;
    
    /**
     * 封面地址
     */
    private String coverUrl;
    
    /**
     * 视频地址
     */
    private String videoUrl;
    
    /**
     * 播放量
     */
    private Long playCount;
    
    /**
     * 点赞数
     */
    private Long likeCount;
    
    /**
     * 审核状态
     */
    private VideoStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
