FROM python:3.12-slim
ARG LIZARD_VERSION=1.24.0
RUN pip install --no-cache-dir "lizard==${LIZARD_VERSION}"
COPY quality_analyzer.py /opt/quality_analyzer.py
ENTRYPOINT ["python", "/opt/quality_analyzer.py"]
