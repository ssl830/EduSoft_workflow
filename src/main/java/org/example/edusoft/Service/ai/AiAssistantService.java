package org.example.edusoft.service.ai;

import org.example.edusoft.dto.ai.AiAskRequest;
import org.example.edusoft.dto.ai.AiAskResponse;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {
    public AiAskResponse ask(AiAskRequest request) {
        // TODO: 集成本地知识库检索与大模型API，RAG流程
        // 1. 检索知识库相关内容
        // 2. 调用大模型生成答案
        // 3. 返回答案和知识点出处
        AiAskResponse response = new AiAskResponse();
        response.setAnswer("[AI答案占位符] 这里将返回基于知识库和大模型的智能解答。");
        response.setSources(new String[]{"知识库文档1.pdf 第3页"});
        return response;
    }
} 