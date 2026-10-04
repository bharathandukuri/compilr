FROM execution/isolate:1.0

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        unzip \
    && curl -fsSL https://storage.googleapis.com/dart-archive/channels/stable/release/3.4.4/sdk/dartsdk-linux-x64-release.zip -o /tmp/dartsdk.zip \
    && unzip -q /tmp/dartsdk.zip -d /usr/local \
    && rm -f /tmp/dartsdk.zip \
    && ln -s /usr/local/dart-sdk/bin/dart /usr/local/bin/dart \
    && dart --version \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /execution

CMD ["/bin/bash"]
