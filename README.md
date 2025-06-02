
### Docker commands

````
docker build -t money-backend --platform linux/arm64 .

docker run --name money-app -p 8080:8080 money-backend

docker run -p 8080:8080 --name money-app \
-e DB_URL=jdbc:postgresql://host.docker.internal:5432/money \
-e DB_USERNAME=postgres \
-e DB_PASSWORD=root \
money-backend

docker stop money-app

docker rm money-app

docker rmi money-backend