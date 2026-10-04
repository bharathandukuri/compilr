FROM execution/isolate:1.0

ENV DEBIAN_FRONTEND=noninteractive \
    DOTNET_ROOT=/usr/lib/dotnet \
    DOTNET_CLI_TELEMETRY_OPTOUT=1 \
    DOTNET_NOLOGO=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        dotnet-sdk-8.0 \
    && dotnet --version \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /execution

CMD ["/bin/bash"]
