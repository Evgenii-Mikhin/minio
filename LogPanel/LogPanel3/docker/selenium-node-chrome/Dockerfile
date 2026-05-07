FROM selenium/node-chrome:4.31.0

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends xdotool \
    && rm -rf /var/lib/apt/lists/*

USER seluser
