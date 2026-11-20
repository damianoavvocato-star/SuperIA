from typing import Literal
from pydantic import BaseModel, Field


class ChatMessage(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str = Field(min_length=1, max_length=12000)


class ChatRequest(BaseModel):
    messages: list[ChatMessage] = Field(min_length=1, max_length=60)
    model: str | None = Field(default=None, min_length=1, max_length=120)


class ChatResponse(BaseModel):
    content: str
    model: str
    provider: str = "free-only-router"
