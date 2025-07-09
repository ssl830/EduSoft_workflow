"""
文件存储服务
"""
import os
import shutil
from datetime import datetime
from typing import Optional
from utils.logger import api_logger as logger

class StorageService:
    """文件存储服务（支持多个工作空间根目录，用于联合知识库）。

    目录结构示例（每个根路径相同）：
    ── <root>/
       ├── data/vector_db
       ├── storage/documents
       ├── storage/temp
       └── logs

    通过 `set_base_paths([...])` 可一次性切换/激活多个知识库；上传时将文件同步保存到所有路径，
    检索时合并多个向量数据库结果。
    """

    def __init__(self, root_path: str | list[str] = "."):
        """初始化存储服务

        Args:
            root_path: 单一路径或路径列表。"""

        # 定义默认（服务器）根路径
        self.default_root = os.path.abspath(root_path if not isinstance(root_path, list) else root_path[0])

        if isinstance(root_path, list):
            self.root_paths = [os.path.abspath(p) for p in root_path]
        else:
            self.root_paths = [self.default_root]

        # 确保最少包含 default_root
        if not self.root_paths:
            self.root_paths = [self.default_root]

        for p in self.root_paths:
            self._ensure_storage_structure(p)
    
    def _ensure_storage_structure(self, base_path: str):
        """确保一个根路径下的目录结构。"""
        directories = [
            os.path.join(base_path, "data", "vector_db"),
            os.path.join(base_path, "storage", "documents"),
            os.path.join(base_path, "storage", "temp"),
            os.path.join(base_path, "logs"),
        ]

        for directory in directories:
            os.makedirs(directory, exist_ok=True)
            logger.info(f"Ensured directory exists: {directory}")
    
    def get_base_paths(self) -> list[str]:
        """返回激活根路径列表，服务器默认路径以空字符串表示，便于前端识别。"""
        result: list[str] = []
        for p in self.root_paths:
            if os.path.abspath(p) == self.default_root:
                result.append("")  # sentinel for server KB
            else:
                result.append(p)
        return result

    def set_base_paths(self, paths: list[str]):
        """一次性更新激活的根路径列表。前端使用空字符串代表服务器公共知识库。"""

        if not paths:
            paths = [""]

        processed: list[str] = []
        for p in paths:
            if p == "":
                processed.append(self.default_root)
            else:
                processed.append(os.path.abspath(p))

        # 去重并保持原顺序
        seen = set()
        abs_paths: list[str] = []
        for p in processed:
            if p not in seen:
                seen.add(p)
                abs_paths.append(p)

        if abs_paths == self.root_paths:
            return

        self.root_paths = abs_paths
        for p in self.root_paths:
            self._ensure_storage_structure(p)
        logger.info(f"Active storage root paths updated: {self.root_paths}")

    # 保留向后兼容接口（单路径）
    def set_base_path(self, base_path: str):
        """兼容旧接口：仅激活单一知识库路径。"""
        self.set_base_paths([base_path])
    
    def save_document(self, file_path: str, course_id: Optional[str] = None) -> str:
        """保存上传的文档到所有激活知识库路径，返回主路径文件位置（第一个）。"""
        try:
            filename = os.path.basename(file_path)
            date_prefix = datetime.now().strftime("%Y%m%d")

            saved_path_primary = ""

            for idx, root in enumerate(self.root_paths):
                if course_id:
                    target_dir = os.path.join(root, "storage", "documents", course_id, date_prefix)
                else:
                    target_dir = os.path.join(root, "storage", "documents", date_prefix)

                os.makedirs(target_dir, exist_ok=True)

                base_name, ext = os.path.splitext(filename)
                timestamp = datetime.now().strftime("%H%M%S")
                unique_filename = f"{base_name}_{timestamp}{ext}"
                target_path = os.path.join(target_dir, unique_filename)

                # 对于第一条路径移动文件，其余复制，避免源文件缺失
                if idx == 0:
                    shutil.move(file_path, target_path)
                    saved_path_primary = target_path
                else:
                    shutil.copy(saved_path_primary, target_path)

                logger.info(f"Saved document to {target_path}")

            return saved_path_primary

        except Exception as e:
            logger.error(f"Error saving document {file_path}: {str(e)}")
            raise
    
    def get_vector_db_paths(self) -> list[str]:
        """返回所有激活知识库的向量数据库目录路径 (每个 <root>/data/vector_db)。"""
        return [os.path.join(p, "data", "vector_db") for p in self.root_paths]

    # 兼容旧接口：返回第一条路径
    def get_vector_db_path(self) -> str:
        return self.get_vector_db_paths()[0]
    
    def get_temp_dir(self) -> str:
        """返回第一条路径的临时文件目录 (<root>/storage/temp)"""
        return os.path.join(self.root_paths[0], "storage", "temp")
    
    def cleanup_temp_files(self):
        """清理临时文件（仅主路径）。"""
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

    def document_exists(self, filename: str, course_id: Optional[str] = None) -> bool:
        """检查所有知识库路径下是否存在同名文件。"""
        base_name, ext = os.path.splitext(filename)
        for root_path in self.root_paths:
            # 构造待检查目录
            if course_id:
                search_dir = os.path.join(root_path, "storage", "documents", course_id)
            else:
                search_dir = os.path.join(root_path, "storage", "documents")

            if not os.path.exists(search_dir):
                continue

            for root, _dirs, files in os.walk(search_dir):
                for f in files:
                    if not f.endswith(ext):
                        continue
                    if f.startswith(f"{base_name}_"):
                        logger.debug(f"Duplicate file detected in {root_path}: {f} vs {filename}")
                        return True
        return False 