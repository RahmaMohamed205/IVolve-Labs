FROM node:22-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --omit=dev

RUN npm install -g npm@latest

COPY app.js ./

EXPOSE 3000

CMD ["npm", "start"]
