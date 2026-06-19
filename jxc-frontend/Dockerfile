FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
RUN apk add --no-cache bash
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx-simple.conf /etc/nginx/nginx.conf.template
ENV PORT=8080
EXPOSE 8080
CMD ["/bin/sh", "-c", "sed 's/__PORT__/${PORT}/g' /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && nginx -g 'daemon off;'"]
