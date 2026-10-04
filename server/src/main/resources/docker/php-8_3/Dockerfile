FROM execution/isolate:1.0

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        php-cli \
    && php --version \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /execution

CMD ["/bin/bash"]
