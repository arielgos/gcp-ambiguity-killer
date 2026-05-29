from fastapi import FastAPI

app = FastAPI(title="GCP Ambiguity Killer API")


@app.get("/", tags=["root"])
def root() -> dict[str, str]:
    return {"message": "Service is running"}


@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}
