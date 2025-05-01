ARG NODE_VERSION
ARG BUILDPLATFORM=${BUILDPLATFORM:-amd64}
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS install

# NPM ci first, as to NOT invalidate previous steps except for when package.json changes
WORKDIR /app/binocular/binocular-frontend

#RUN --mount=type=bind,src=./package-lock.json,target=./package-lock.json,readonly \
#    --mount=type=bind,src=./package.json,target=./package.json,readonly \
RUN --mount=type=bind,src=./package.json,target=./package.json,readonly \
    --mount=type=bind,src=./binocular-backend/package.json,target=./binocular-backend/package.json,readonly \
    --mount=type=bind,src=./binocular-frontend/package.json,target=./binocular-frontend/package.json,readonly \
#    npm ci --omit=optional --ignore-scripts && \
# Error: Cannot find module @rollup/rollup-linux-x64-musl.
    # npm has a bug related to optional dependencies (https://github.com/npm/cli/issues/4828).
    # Please try `npm i` again after removing both package-lock.json and node_modules directory.
    npm i && \
#    npm run install:frontend && \
    npm cache clean --force

###
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS builder
ARG NPM_BUILD_CMD="build:prod"
ENV BUILD_CMD=${NPM_BUILD_CMD} 
ENV NODE_ENV=production
ENV NODE_OPTIONS=--max_old_space_size=4096
ENV GENERATE_SOURCEMAP=false

WORKDIR /app/binocular/binocular-frontend

COPY --from=install --chown=node:node /app/binocular/binocular-frontend/node_modules ./node_modules
COPY --from=install --chown=node:node /app/binocular/binocular-frontend/binocular-frontend/node_modules ./binocular-frontend/node_modules
COPY --chown=node:node ./LICENSE ./LICENSE
COPY --chown=node:node ./binocular-frontend ./binocular-frontend
COPY --chown=node:node ./utils ./utils

RUN echo "{\"repo\":{\"name\":\"TODO: remove static context.json\"}}" > ./binocular-frontend/config/context.json

# This seems to be the most expensive step
RUN --mount=type=bind,src=./package.json,target=./package.json,readonly \
    npm run ${BUILD_CMD}

COPY ./binocular-frontend/package.json ./binocular-frontend/package.json

RUN chown node:node -R /app
RUN chown node:node $(npm root -g)
RUN chown node:node $(npm root)

# FROM alpine AS lean

# WORKDIR /app
# COPY --from=builder --chown=node:node /app/binocular/binocular-frontend /app
USER node

WORKDIR /app/binocular
COPY ./package.json ./package.json
# COPY ./package-lock.json ./package-lock.json

ENTRYPOINT [ "npm" ]
