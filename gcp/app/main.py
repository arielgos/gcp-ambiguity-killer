from fastapi import FastAPI

app = FastAPI(title="GCP Ambiguity Killer", version="1.0.0", description="A service to help clarify GCP concepts and services.")


@app.get("/", tags=["root"])
def root() -> dict[str, str]:
    return {"message": "Service is running"}


@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}
