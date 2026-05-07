ARG NODE_VERSION="25.9.0"

FROM docker.io/library/node:${NODE_VERSION}-slim as build
ARG APP_PATH="/home/node/foundry"
ARG TIMED_URL
RUN apt-get update && apt-get install -y unzip curl
USER node
RUN curl -v -L $TIMED_URL -o /home/node/foundry.zip
RUN unzip /home/node/foundry.zip -d /home/node/foundry

FROM node:${NODE_VERSION}-alpine3.23
ARG APP_PATH
VOLUME /home/node/.local/share/FoundryVTT/
EXPOSE 30000
COPY --chown=node:node --from=build /home/node/foundry /home/node/foundry
USER node
WORKDIR $APP_PATH
CMD ["main.mjs","--port=30000","--headless","--noupdate"]
