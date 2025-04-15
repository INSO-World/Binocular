ARG NODE_VERSION
ARG BUILDPLATFORM=${BUILDPLATFORM:-amd64}
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS install

# NPM ci first, as to NOT invalidate previous steps except for when package.json changes
WORKDIR /app/binocular

RUN --mount=type=bind,src=./package.json,target=./package.json,readonly \
    --mount=type=bind,src=./binocular-backend/package.json,target=./binocular-backend/package.json,readonly \
    --mount=type=bind,src=./binocular-frontend/package.json,target=./binocular-frontend/package.json,readonly \
    npm i && \
    npm cache clean --force
#RUN wget https://gobinaries.com/tj/node-prune --output-document - | /bin/sh && node-prune

###
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS builder
ENV NODE_ENV=production
ENV NODE_OPTIONS=--max_old_space_size=4096
ENV GENERATE_SOURCEMAP=false

WORKDIR /app/binocular

COPY --from=install /app/binocular/node_modules ./node_modules
COPY --from=install /app/binocular/binocular-frontend/node_modules ./binocular-frontend/node_modules
COPY --from=install /app/binocular/binocular-backend/node_modules ./binocular-backend/node_modules

RUN npm i -g tsx ts-node

#COPY ./LICENSE ./LICENSE
#COPY ./.git ./.git
#COPY ./ui ./ui
#COPY ./lib ./lib
#COPY ./services ./services
#COPY ./foxx ./foxx
#COPY ./package.json ./package.json
#COPY ./utils ./utils
#COPY ./binocular-backend ./binocular-backend
COPY ./ ./
RUN npm i mocha
RUN npm link
#RUN cd ./binocular-backend && \
#    npm link --ignore-scripts

RUN --mount=type=bind,src=./.binocularrc_ci,target=./.binocularrc,readonly \
    DEBUG=idx*,importer*,git*,db* \
    binocular \
    run \
    --no-frontend \
    --no-server \
    ./
RUN npm run build:offline
