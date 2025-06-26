"""
文件存储服务
"""
import os
import shutil
from datetime import datetime
from typing import Optional
from utils.logger import api_logger as logger

class StorageService:
    """文件存储服务"""
    
    def __init__(self, base_path: str = "storage"):
        """
        初始化存储服务
        :param base_path: 基础存储路径
        """
        self.base_path = base_path
        self._ensure_storage_structure()
    
    def _ensure_storage_structure(self):
        """确保存储目录结构存在"""
        directories = [
            self.base_path,
            os.path.join(self.base_path, "documents"),  # 原始文档
            os.path.join(self.base_path, "temp"),       # 临时文件
            os.path.join(self.base_path, "vector_db"),  # 向量数据库
            os.path.join(self.base_path, "logs")        # 日志文件
        ]
        
        for directory in directories:
            os.makedirs(directory, exist_ok=True)
            logger.info(f"Ensured directory exists: {directory}")
    
    def save_document(self, file_path: str, course_id: Optional[str] = None) -> str:
        """
        保存上传的文档
        :param file_path: 临时文件路径
        :param course_id: 课程ID（可选）
        :return: 保存后的文件路径
        """
        try:
            # 生成目标路径
            filename = os.path.basename(file_path)
            date_prefix = datetime.now().strftime("%Y%m%d")
            
            if course_id:
                target_dir = os.path.join(self.base_path, "documents", course_id, date_prefix)
            else:
                target_dir = os.path.join(self.base_path, "documents", date_prefix)
            
            os.makedirs(target_dir, exist_ok=True)
            
            # 生成唯一文件名
            base_name, ext = os.path.splitext(filename)
            timestamp = datetime.now().strftime("%H%M%S")
            unique_filename = f"{base_name}_{timestamp}{ext}"
            target_path = os.path.join(target_dir, unique_filename)
            
            # 移动文件
            shutil.move(file_path, target_path)
            logger.info(f"Saved document to {target_path}")
            
            return target_path
            
        except Exception as e:
            logger.error(f"Error saving document {file_path}: {str(e)}")
            raise
    
    def get_vector_db_path(self) -> str:
        """获取向量数据库路径"""
        return os.path.join(self.base_path, "vector_db")
    
    def get_temp_dir(self) -> str:
        """获取临时文件目录"""
        return os.path.join(self.base_path, "temp")
    
    def cleanup_temp_files(self):
        """清理临时文件"""
        temp_dir = self.get_temp_dir()
        try:
            for filename in os.listdir(temp_dir):
                file_path = os.path.join(temp_dir, filename)
                if os.path.isfile(file_path):
                    os.remove(file_path)
            logger.info("Cleaned up temporary files")
        except Exception as e:
            logger.error(f"Error cleaning up temp files: {str(e)}")
            raise 