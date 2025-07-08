"""
文件存储服务
"""
import os
import shutil
from datetime import datetime
from typing import Optional
from utils.logger import api_logger as logger

class StorageService:
    """文件存储服务（支持动态工作空间根目录）。

    目录结构示例：
    ── <root>/
       ├── data/vector_db
       ├── storage/documents
       ├── storage/temp
       └── logs
    """
    
    def __init__(self, root_path: str = "."):
        """初始化存储服务

        Args:
            root_path: 用户工作空间根目录（可在运行时切换）。
        """
        self.root_path = os.path.abspath(root_path)
        self._ensure_storage_structure()
    
    def _ensure_storage_structure(self):
        """创建所需目录结构 (若不存在)。"""
        directories = [
            os.path.join(self.root_path, "data", "vector_db"),
            os.path.join(self.root_path, "storage", "documents"),
            os.path.join(self.root_path, "storage", "temp"),
            os.path.join(self.root_path, "logs"),
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
                target_dir = os.path.join(self.root_path, "storage", "documents", course_id, date_prefix)
            else:
                target_dir = os.path.join(self.root_path, "storage", "documents", date_prefix)
            
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
        """返回向量数据库目录路径 (<root>/data/vector_db)"""
        return os.path.join(self.root_path, "data", "vector_db")
    
    def get_temp_dir(self) -> str:
        """返回临时文件目录 (<root>/storage/temp)"""
        return os.path.join(self.root_path, "storage", "temp")
    
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

    def set_base_path(self, base_path: str):
        """动态修改存储根路径（允许用户切换不同的知识库目录）。

        如果路径变化，将重新创建所需的子目录结构。调用方应在更新后
        重新加载或保存向量数据库。
        """
        # 若传入相同路径直接返回
        if os.path.abspath(base_path) == os.path.abspath(self.root_path):
            return

        self.root_path = os.path.abspath(base_path)
        self._ensure_storage_structure()
        logger.info(f"Storage root path set to: {self.root_path}")

    def document_exists(self, filename: str, course_id: Optional[str] = None) -> bool:
        """检查同名文档是否已存在，避免重复上传。

        上传逻辑中会在原文件名后附加时间戳保存；因此，我们认为同名
        （不含时间戳部分）的文件即视为重复。例如 `example.pdf` 上传后
        保存为 `example_101010.pdf`，再次上传 `example.pdf` 应视为重复。
        """
        base_name, ext = os.path.splitext(filename)

        # 构造待检查目录
        if course_id:
            search_dir = os.path.join(self.root_path, "storage", "documents", course_id)
        else:
            search_dir = os.path.join(self.root_path, "storage", "documents")

        if not os.path.exists(search_dir):
            return False

        for root, _dirs, files in os.walk(search_dir):
            for f in files:
                # 文件名形如 base_YYYYMMDDHHMMSS.pdf
                if not f.endswith(ext):
                    continue
                if f.startswith(f"{base_name}_"):
                    logger.debug(f"Duplicate file detected: {f} vs {filename}")
                    return True
        return False 