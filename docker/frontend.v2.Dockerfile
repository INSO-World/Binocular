ARG NODE_VERSION=${NODE_VERSION:-18.18.2}
ARG BUILDPLATFORM=${BUILDPLATFORM:-amd64}

# FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS install_root

# # NPM ci first, as to NOT invalidate previous steps except for when package.json changes
# WORKDIR /app/binocular

# RUN --mount=type=bind,src=./docker/frontend-mem-nag.sh,target=/frontend-mem-nag.sh \
#     /frontend-mem-nag.sh

# RUN --mount=type=bind,src=./package-lock.json,target=./package-lock.json,readonly \
#    --mount=type=bind,src=./package.json,target=./package.json,readonly \
#     npm ci --ignore-scripts && \
#     npm cache clean --force

FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-alpine AS install_fe
WORKDIR /app/binocular/frontend

RUN --mount=type=bind,src=./docker/frontend-mem-nag.sh,target=/frontend-mem-nag.sh \
    /frontend-mem-nag.sh

RUN --mount=type=bind,src=./binocular-frontend-new/package-lock.json,target=./package-lock.json,readonly \
   --mount=type=bind,src=./binocular-frontend-new/package.json,target=./package.json,readonly \
    npm ci --ignore-scripts && \
    npm cache clean --force

FROM --platform=${BUILDPLATFORM} node:${NODE_VERSION}-slim AS builder
WORKDIR /app/binocular

# COPY --from=install_root --chown=node:node /app/binocular/node_modules ./node_modules
COPY --from=install_fe --chown=node:node /app/binocular/frontend/node_modules ./frontend/node_modules

WORKDIR /app/binocular/frontend

COPY ./binocular-frontend-new ./
COPY --chown=node:node ./utils /app/binocular/utils

# ARG CACHEBUST
RUN npm run build

########## final lean image #########
FROM nginxinc/nginx-unprivileged:alpine3.20 AS lean

# copying compiled code from dist to nginx folder for serving
COPY  --from=builder --chown=node:node /app/binocular/frontend/dist /usr/share/nginx/html

# copying nginx config from local to image
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

# exposing internal port
EXPOSE 80
