"""Vercel Python Functions entrypoint for the existing FastAPI service.

Configure the Vercel project Root Directory as `services/api`.
The existing `requirements.txt` and `app/` package then remain self-contained.
"""

from app.main import app
