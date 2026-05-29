from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="GCP Ambiguity Killer", version="1.0.0", description="A service to help clarify GCP concepts and services.")

class StringRequest(BaseModel):
    value: str

@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}

@app.post("/process", tags=["process"])
def process(payload: StringRequest) -> dict[str, str]:
    return {"result": payload.value}
