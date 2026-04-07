# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY web-client/package.json web-client/package-lock.json ./
RUN npm ci --quiet
COPY web-client/src ./src
COPY web-client/angular.json web-client/tsconfig.json web-client/tsconfig.app.json ./
RUN npx ng build --configuration production

# Stage 2: Serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist/web-client/browser /usr/share/nginx/html
COPY docker/web-client/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 4200
