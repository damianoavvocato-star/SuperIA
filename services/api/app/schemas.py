from typing import Literal
from pydantic import BaseModel, Field
from uuid import UUID


class ChatMessage(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str = Field(min_length=1, max_length=32000)


class ChatRequest(BaseModel):
    request_id: UUID | None = None
    messages: list[ChatMessage] = Field(min_length=1, max_length=60)
    model: str | None = Field(default=None, min_length=1, max_length=120)