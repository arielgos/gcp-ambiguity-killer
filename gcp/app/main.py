import os
import re

from fastapi import FastAPI, HTTPException
import google.generativeai as genai
from pydantic import BaseModel, field_validator

app = FastAPI(title="GCP Ambiguity Killer", version="1.0.0", description="A service to help clarify GCP concepts and services.")


_REMOTE_EXECUTION_PATTERNS = (
    r"`",
    r"\$\(",
    r"\$\{",
    r"&&",
    r"\|\|",
    r";",
    r"\brm\s+-rf\b",
    r"\bsudo\b",
    r"\bchmod\b",
    r"\bchown\b",
    r"\bpython\s+-c\b",
    r"\bnode\s+-e\b",
    r"\beval\b",
    r"\bexec\b",
    r"\bos\.system\b",
    r"\bsubprocess\b",
)


class ProcessRequest(BaseModel):
    user: str
    value: str

    @field_validator("value")
    @classmethod
    def validate_value(cls, value: str) -> str:
        normalized_value = value.strip()
        if not normalized_value:
            raise ValueError("value cannot be empty")

        if len(normalized_value) > 2000:
            raise ValueError("value is too long")

        lowered_value = normalized_value.lower()
        for pattern in _REMOTE_EXECUTION_PATTERNS:
            if re.search(pattern, lowered_value):
                raise ValueError("value contains disallowed command execution content")

        return normalized_value


@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/process", tags=["process"])
def process(payload: ProcessRequest) -> dict[str, str]:

    system_prompt = (
        "You are a helpful assistant that provides clear and concise explanations of Google Cloud Platform (GCP) concepts and services. "
        "When given a user query, you will analyze it and provide a detailed response that clarifies any ambiguity. "
        "Your responses should be informative, easy to understand, and directly address the user's question."
    )

    user_prompt = (
        "Please analyze the user's input and provide a clear explanation of the GCP concept or service they are asking about. "
        "If the user's query is ambiguous, identify the potential areas of confusion and clarify them in your response. "
        "Make sure to cover any relevant details that would help the user understand the topic better."
    )

    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")

    try:
        genai.configure(api_key=api_key)
        model = genai.GenerativeModel(
            model_name=os.getenv("GEMINI_MODEL", "gemini-1.5-flash"),
            system_instruction=system_prompt,
        )

        prompt = f"User: {payload.user}\nInput: {payload.value}\n\nInstruction: {user_prompt}\n\nResponse:"
        response = model.generate_content(prompt)
        result = response.text or ""
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"Gemini request failed: {exc}") from exc

    return {"user": payload.user, "result": result}
