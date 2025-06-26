"""
基于FAISS的向量数据库服务，用于存储和检索文档向量
"""
import os
import json
import numpy as np
import faiss
from typing import List, Tuple, Dict

class FAISSDatabase:
    """FAISS向量数据库"""
    
    def __init__(self, dim: int = 1536):  # DeepSeek embedding维度为1536
        """初始化FAISS索引"""
        self.index = faiss.IndexFlatL2(dim)  # 使用L2距离
        self.contents: List[str] = []  # 存储文本内容
        self.sources: List[str] = []   # 存储来源信息
        
    def add_embeddings(self, embeddings: np.ndarray, contents: List[str], sources: List[str]):
        """
        添加向量到数据库
        """
        if len(embeddings) != len(contents) or len(embeddings) != len(sources):
            raise ValueError("embeddings, contents and sources must have the same length")
            
        self.index.add(embeddings)
        self.contents.extend(contents)
        self.sources.extend(sources)
        
    def search(self, query_embedding: np.ndarray, top_k: int = 5) -> List[Dict[str, str]]:
        """
        检索最相似的文档
        返回: [{'content': str, 'source': str}, ...]
        """
        # 确保query_embedding是2D数组
        if len(query_embedding.shape) == 1:
            query_embedding = query_embedding.reshape(1, -1)
            
        # 搜索最近的k个向量
        distances, indices = self.index.search(query_embedding, top_k)
        
        results = []
        for idx in indices[0]:  # 取第一个查询的结果
            if idx < len(self.contents):
                results.append({
                    'content': self.contents[idx],
                    'source': self.sources[idx]
                })
        return results
    
    def save(self, directory: str):
        """
        保存数据库到文件
        """
        os.makedirs(directory, exist_ok=True)
        
        # 保存FAISS索引
        faiss.write_index(self.index, os.path.join(directory, 'index.faiss'))
        
        # 保存文本内容和来源
        metadata = {
            'contents': self.contents,
            'sources': self.sources
        }
        with open(os.path.join(directory, 'metadata.json'), 'w', encoding='utf-8') as f:
            json.dump(metadata, f, ensure_ascii=False, indent=2)
            
    def load(self, directory: str):
        """
        从文件加载数据库
        """
        # 加载FAISS索引
        self.index = faiss.read_index(os.path.join(directory, 'index.faiss'))
        
        # 加载文本内容和来源
        with open(os.path.join(directory, 'metadata.json'), 'r', encoding='utf-8') as f:
            metadata = json.load(f)
            self.contents = metadata['contents']
            self.sources = metadata['sources'] 