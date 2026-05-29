from fastapi import FastAPI

app = FastAPI(title="GCP Ambiguity Killer", version="1.0.0", description="A service to help clarify GCP concepts and services.")

@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}
