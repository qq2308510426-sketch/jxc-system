FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx-simple.conf /etc/nginx/conf.d/default.conf
RUN apk add --no-cache bash
CMD ["/bin/sh", "-c", "sed -i 's/BACKEND_HOST/'$BACKEND_HOST'/g' /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]