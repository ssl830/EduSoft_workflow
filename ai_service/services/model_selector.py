from __future__ import annotations

"""Model selection & fallback utility for ai_service.

This centralises all LLM access so we can dynamically choose between
multiple providers (ChatGLM, Yi-6B, DeepSeek) without touching calling
code.

Priority order is determined by evaluation results:
    1. glm-4  – best overall quality / latency
    2. yi-34b-chat    – best source-recall, longer context
    3. DeepSeek-V3   – rich code explanations, existing default

If the primary model is unavailable or raises an exception, the
selector will transparently fall back to the next one.

Environment variables expected (all optional – missing creds will skip
that backend):
    # ChatGLM3
    CHATGLM_API_KEY
    CHATGLM_BASE_URL          (default https://open.bigmodel.cn/api/paas/v4)
    CHATGLM_MODEL_NAME        (default glm-4)

    # yi-34b-chat (01.ai)
    YI_API_KEY
    YI_BASE_URL               (default https://openai.01.ai/v1)
    YI_MODEL_NAME             (default yi-34b-chat)

    # DeepSeek-V3 (existing)
    DEEPSEEK_API_KEY
    DEEPSEEK_BASE_URL         (default https://api.deepseek.com/v1)
    DEEPSEEK_MODEL_NAME       (default deepseek-chat)

Optionally override default priority by setting PRIMARY_LLM_MODEL to
"chatglm" | "yi" | "deepseek".
"""

import os
import logging
from typing import Dict, List, Optional

from openai import OpenAI

logger = logging.getLogger("model_selector")


class ModelSelector:
    """Simple round-robin / fallback model chooser for chat completion."""

    def __init__(self) -> None:
        self._clients: Dict[str, dict] = {}
        self._init_clients()

        # Determine default priority order (skip models without creds)
        default_order = [
            os.getenv("PRIMARY_LLM_MODEL", "chatglm"),  # user-preferred first
            "chatglm",
            "yi",
            "deepseek",
        ]
        # Remove duplicates while preserving order and keeping only available
        seen = set()
        self.priority: List[str] = []
        for key in default_order:
            if key in self._clients and key not in seen:
                self.priority.append(key)
                seen.add(key)

        if not self.priority:
            raise RuntimeError("No valid LLM credentials found – cannot initialise ModelSelector")

        logger.info(f"ModelSelector initialised with priority {self.priority}")

    # ------------------------------------------------------------------
    # public helpers
    # ------------------------------------------------------------------
    def chat_completion(self, *, messages: List[dict], temperature: float = 0.7, top_p: float = 0.95, purpose: Optional[str] = None, **extra) -> any:  # noqa: ANN401
        """Try providers in order until one succeeds. Returns OpenAI response."""

        for key in self.priority:
            meta = self._clients[key]
            client: OpenAI = meta["client"]
            model_name: str = meta["model_name"]

            try:
                response = client.chat.completions.create(
                    model=model_name,
                    messages=messages,
                    temperature=temperature,
                    top_p=top_p,
                    **extra,
                )
                # Success – log & return
                logger.info(
                    "LLM success via %s (model=%s, tokens=%s)",
                    key,
                    model_name,
                    response.usage if hasattr(response, "usage") else "?",
                )
                return response
            except Exception as exc:  # noqa: BLE001
                logger.warning("LLM backend %s failed: %s – trying fallback", key, exc)
                continue

        # If we reach here, all backends failed
        raise RuntimeError("All LLM backends failed to generate a response")

    # ------------------------------------------------------------------
    # internal helpers
    # ------------------------------------------------------------------
    def _init_clients(self) -> None:
        """Create OpenAI-compatible clients for each provider with creds."""
        # ChatGLM ---------------------------------------------------------
        chatglm_key = os.getenv("CHATGLM_API_KEY")
        if chatglm_key:
            base_url = os.getenv("CHATGLM_BASE_URL", "https://open.bigmodel.cn/api/paas/v4")
            model_name = os.getenv("CHATGLM_MODEL_NAME", "glm-4")
            client = OpenAI(api_key=chatglm_key, base_url=base_url)
            self._clients["chatglm"] = {
                "client": client,
                "model_name": model_name,
            }
            logger.info("ChatGLM backend enabled (%s)", model_name)
        else:
            logger.info("ChatGLM backend disabled – no CHATGLM_API_KEY found")

        # Yi-6B -----------------------------------------------------------
        yi_key = os.getenv("YI_API_KEY")
        if yi_key:
            base_url = os.getenv("YI_BASE_URL", "https://openai.01.ai/v1")
            model_name = os.getenv("YI_MODEL_NAME", "yi-34b-chat")
            client = OpenAI(api_key=yi_key, base_url=base_url)
            self._clients["yi"] = {
                "client": client,
                "model_name": model_name,
            }
            logger.info("Yi backend enabled (%s)", model_name)
        else:
            logger.info("Yi backend disabled – no YI_API_KEY found")

        # DeepSeek --------------------------------------------------------
        deepseek_key = os.getenv("DEEPSEEK_API_KEY")
        if deepseek_key:
            base_url = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1")
            model_name = os.getenv("DEEPSEEK_MODEL_NAME", "deepseek-chat")
            client = OpenAI(api_key=deepseek_key, base_url=base_url)
            self._clients["deepseek"] = {
                "client": client,
                "model_name": model_name,
            }
            logger.info("DeepSeek backend enabled (%s)", model_name)
        else:
            logger.info("DeepSeek backend disabled – no DEEPSEEK_API_KEY found") 