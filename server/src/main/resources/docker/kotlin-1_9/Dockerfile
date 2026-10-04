FROM execution/java:21

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        unzip \
    && curl -fsSL https://github.com/JetBrains/kotlin/releases/download/v1.9.24/kotlin-compiler-1.9.24.zip -o /tmp/kotlinc.zip \
    && unzip -q /tmp/kotlinc.zip -d /usr/local \
    && rm -f /tmp/kotlinc.zip \
    && ln -s /usr/local/kotlinc/bin/kotlinc /usr/local/bin/kotlinc \
    && ln -s /usr/local/kotlinc/bin/kotlin /usr/local/bin/kotlin \
    && kotlinc -version \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /execution

CMD ["/bin/bash"]
