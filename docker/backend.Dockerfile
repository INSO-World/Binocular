ARG NODE_VERSION
ARG BUILDPLATFORM=${BUILDPLATFORM:-amd64}
# ARG APP_PATH_ARG=/app/binocular

######################################################################
# INSTALL image...
######################################################################
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine3.20 AS install
# ENV APP_PATH=${APP_PATH_ARG}

# NPM ci first, as to NOT invalidate previous steps except for when package.json changes

RUN --mount=type=bind,src=./docker/frontend-mem-nag.sh,target=/frontend-mem-nag.sh \
  /frontend-mem-nag.sh

WORKDIR /app/binocular

RUN mkdir -p /app/binocular/binocular-frontend
RUN mkdir -p /app/binocular/binocular-backend

RUN ls -l .

RUN --mount=type=bind,src=./package.json,target=./package.json,readonly \
    --mount=type=bind,src=./package-lock.json,target=./package-lock.json,readonly \
    --mount=type=bind,src=./binocular-backend/package.json,target=./binocular-backend/package.json,readonly \
    --mount=type=bind,src=./binocular-backend/package-lock.json,target=./binocular-backend/package-lock.json,readonly \
    # --mount=type=bind,src=./binocular-frontend/package.json,target=./binocular-frontend/package.json,readonly \
    # --mount=type=bind,src=./binocular-frontend/package-lock.json,target=./binocular-frontend/package-lock.json,readonly \
    npm ci --ignore-scripts && \
    cd binocular-backend && npm ci
    # && cd .. && \
    # cd binocular-frontend && npm i && cd .. 
    #&& \
    #npm link --ignore-scripts

# RUN npm install -g typescript
# COPY ./binocular-backend ./binocular-backend
# RUN cd ./binocular-backend && npm run build

######################################################################
# Final lean image...
######################################################################
FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine3.20 AS builder

ENV APP_PATH=${APP_PATH_ARG}
ENV NODE_ENV=production
ENV NODE_OPTIONS=--max_old_space_size=4096
ENV GENERATE_SOURCEMAP=false

RUN apk add git

WORKDIR /app/binocular
RUN chown node:node -R /app/binocular

COPY --from=install --chown=node:node /app/binocular/node_modules ./node_modules
COPY --from=install --chown=node:node /app/binocular/binocular-backend/node_modules ./binocular-backend/node_modules
# COPY --from=install --chown=node:node /app/binocular/binocular-frontend/node_modules ./binocular-frontend/node_modules

# folders
COPY --chown=node:node ./foxx ./foxx
COPY --chown=node:node ./utils ./utils
COPY --chown=node:node ./binocular-backend ./binocular-backend
COPY --chown=node:node ./services ./services
# COPY --chown=node:node ./binocular-frontend ./binocular-frontend

# files
COPY --chown=node:node ./package.json ./package.json
COPY --chown=node:node ./package-lock.json ./package-lock.json
COPY --chown=node:node ./LICENSE ./LICENSE

# RUN npm install -g .
RUN npm install -g tsx mocha ts-node
RUN npm link --ignore-scripts

RUN mkdir -p /app/binocular/binocular-frontend/db_export
RUN mkdir -p /app/binocular/binocular-frontend/config

# RUN chown node:node -R /app/binocular/binocular-frontend

RUN mkdir -p /app/binocular/repo
RUN chown node:node /app/binocular/repo
RUN chown node:node $(npm root -g)

RUN git config --global --add safe.directory /app/binocular/repo/*

RUN chown node:node -R /app/binocular
USER node

# ENTRYPOINT ["/usr/bin/dumb-init", "--", "/entrypoint"]
# CMD []
