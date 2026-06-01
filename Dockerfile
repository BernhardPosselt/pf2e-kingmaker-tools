ARG NODE_VERSION="25.9.0"

FROM docker.io/library/node:${NODE_VERSION}-slim as build
ARG TIMED_URL
RUN apt-get update && apt-get install -y unzip curl
USER node
COPY --chown=node:node foundry.zip* /home/node/
RUN if [ -f /home/node/foundry.zip ]; then \
      echo "Using local foundry.zip"; \
    else \
      echo "Downloading from timed URL..."; \
      curl -L "$TIMED_URL" -o /home/node/foundry.zip; \
    fi
RUN unzip /home/node/foundry.zip -d /home/node/foundry && \
    if [ -d /home/node/foundry/resources/app ]; then \
      mv /home/node/foundry/resources/app/* /home/node/foundry/ && \
      rm -rf /home/node/foundry/resources; \
    fi

FROM node:${NODE_VERSION}-alpine3.23
ARG APP_PATH="/home/node/foundry"
VOLUME /home/node/.local/share/FoundryVTT/
EXPOSE 30000
COPY --chown=node:node --from=build /home/node/foundry /home/node/foundry
USER node
WORKDIR $APP_PATH
CMD ["main.mjs","--port=30000","--headless","--noupdate"]
