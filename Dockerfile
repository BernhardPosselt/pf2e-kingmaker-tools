FROM node:25.9.0-slim as build

# click on Timed URL inside your purchased licenses
ARG TIMED_URL

RUN apt-get update && apt-get install -y unzip curl
USER node
RUN curl -v -L $TIMED_URL -o /home/node/foundry.zip
RUN unzip /home/node/foundry.zip -d /home/node/foundry

FROM node:25.9.0-alpine3.23
# next 2 lines just for documentation
VOLUME /home/node/.local/share/FoundryVTT/
EXPOSE 30000
COPY --chown=node:node --from=build /home/node/foundry /home/node/foundry
USER node
CMD ["/home/node/foundry/main.mjs", "--port=30000", "--headless", "--noupdate"]