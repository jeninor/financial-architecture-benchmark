FROM python:3.12-slim
RUN apt-get update -qq && apt-get install -y -qq git --no-install-recommends && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir graphifyy
RUN pip install --no-cache-dir "git+https://github.com/yasinyaman/graphify-mcp.git"
WORKDIR /project
ENTRYPOINT ["python", "-m", "graphify_mcp"]
