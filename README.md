# spring-boot-nginx-websocket-rabbitmq-kafka

This project shows how to implement an interactive news broadcasting app. A user can post news using a REST API, and it gets sent out to users instantly through a live `WebSocket` connection. Users can react by liking or disliking the news, and the app keeps track of all those reactions to show the total likes and dislikes.

## Proof-of-Concepts & Articles

On [ivangfr.github.io](https://ivangfr.github.io), I have compiled my Proof-of-Concepts (PoCs) and articles. You can easily search for the technology you are interested in by using the filter. Who knows, perhaps I have already implemented a PoC or written an article about what you are looking for.

## Applications

- ### news-app

  [`Spring Boot`](https://docs.spring.io/spring-boot/index.html) Java web app that provides a REST endpoint for publishing and broadcasting news. It also supports real-time broadcasting and user reactions through full-duplex [`WebSocket`](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) channels. The app stores data in [`PostgreSQL`](https://www.postgresql.org/) and runs behind a [`Nginx`](https://nginx.org/index.html) load balancer. For broadcasting, it connects to [`RabbitMQ`](https://www.rabbitmq.com/), and user reactions are handled sequentially and partitioned by news ID using an [`Apache Kafka`](https://kafka.apache.org/) topic partition.

## Project Diagram

![project-diagram](documentation/project-diagram.png)

## Prerequisites

- [`Java 21`](https://www.oracle.com/java/technologies/downloads/#java21) or higher;
- A containerization tool (e.g., [`Docker`](https://www.docker.com), [`Podman`](https://podman.io), etc.)

## Build News App Docker Image

Open a terminal and, inside the `spring-boot-nginx-websocket-rabbitmq-kafka` root folder, run the following script:
```bash
./build-docker-images.sh
```

## Configure /etc/hosts

Add the following line to `/etc/hosts`
```text
127.0.0.1 news-app.lb
```

## Start Docker Compose services

In a terminal and inside the `spring-boot-nginx-websocket-rabbitmq-kafka` root folder run:
```bash
podman compose up -d
```

## Simulation

- Open one or more browsers and access:
  ```
  http://news-app.lb
  ```

- In a terminal, publish a news:

  - Informing the news description:
    ```bash
    curl -X POST http://news-app.lb/api/news \
      -H "Content-Type: application/json" \
      -d '{"description": "This is the content of the breaking news."}'
    ```
  
  - Not informing the news description. In this case, a random description will be generated:
    ```bash
    curl -X POST http://news-app.lb/api/news
    ```

- You should see the news being displayed in the browsers opened before.

- You can react to the news by clicking on the "Like" or "Dislike" buttons.

- You can check the news statistic by executing the following command in a terminal:
  ```bash
  curl http://news-app.lb/api/news/{id}
  ```
  > **Note**: Replace `{id}` with the actual news id returned when publishing the news.

## Demo

![demo](documentation/demo.gif)

## Useful Commands

- **Nginx**

  If you wish to modify the `Nginx` configuration file without restarting its Docker container, follow these steps:

    - Apply the changes in the `nginx/nginx.conf` file;
    - Execute the following command to access the `nginx` Docker container:
      ```bash
      docker exec -it nginx bash
      ```
    - In the `nginx` Docker container terminal, run:
      ```bash
      nginx -s reload
      ```
    - To exit, just run the command `exit`.

- **PostgreSQL**

  - Execute the following command to access the `psql` terminal:
    ```bash
    docker exec -it postgres psql -U postgres -d newsdb
    ```
  - In the terminal, we can select all news records by running:
    ```sql
    select * from news;
    ```
  - To exit, just run the command `\q`.

- **Kafdrop**

  `Kafdrop` can be accessed at http://localhost:9000

- **RabbitMQ UI**

  `RabbitMQ UI` can be accessed at http://localhost:15672 (`guest` for _username_ and _password_)

## Shutdown

To stop and remove Docker Compose containers, network, and volumes, go to a terminal and, inside the `spring-boot-nginx-websocket-rabbitmq-kafka` root folder, run the following command:
```bash
podman compose down -v
```

## Cleanup

- To remove the Docker images created by this project, go to a terminal and, inside the `spring-boot-nginx-websocket-rabbitmq-kafka` root folder, run the script below:
  ```bash
  ./remove-docker-images.sh
  ```

- Remove the line below from `/etc/hosts`:
  ```text
  127.0.0.1 news-app.lb
  ```