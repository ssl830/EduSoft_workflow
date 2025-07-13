package org.example.edusoft.service.file;

import org.example.edusoft.common.domain.Result;
import org.example.edusoft.entity.file.FileInfo;
import org.example.edusoft.entity.file.FileType;
import org.springframework.web.multipart.MultipartFile;
import org.example.edusoft.service.file.FolderService;

public interface FileUpload {
    /**
     * 上传文件
     */
    public Result<?> uploadFile(MultipartFile file, String title, Long courseId, Long sectionId, String visibility, Long uploaderId, String type);
    
    /**
     * 上传文件，并指定是否上传到知识库
     */
    public Result<?> uploadFile(MultipartFile file, String title, Long courseId, Long sectionId, String visibility, Long uploaderId, String type, boolean uploadToKnowledgeBase);
    
    /**
     * 上传文件
     */
    Result<?> upload(MultipartFile file, String title, Long parentId, Long courseId, Long classId, String visibility, Long sectionId, FileType type, Long uploaderId);
}
