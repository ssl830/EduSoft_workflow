package org.example.edusoft.controller.ai;

import org.example.edusoft.dto.ai.AiAskRequest;
import org.example.edusoft.dto.ai.AiAskResponse;
import org.example.edusoft.service.ai.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {
    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/ask")
    public AiAskResponse ask(@RequestBody AiAskRequest request) {
        return aiAssistantService.ask(request);
    }
} 