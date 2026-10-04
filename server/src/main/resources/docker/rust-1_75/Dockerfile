FROM execution/isolate:1.0

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        rustc \
        gcc \
        libc6-dev \
    && rustc --version \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /execution

CMD ["/bin/bash"]
