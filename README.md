***

# 🚀 Event-Driven GraphQL Microservice

A high-performance, asynchronous microservice built with **Kotlin Coroutines**, **Spring GraphQL**, **AWS DynamoDB**, and **Apache Kafka**.

This project serves as a comprehensive demonstration of modern server-side architecture. It showcases how to build a strict, strongly-typed GraphQL API, resolve the classic GraphQL N+1 performance bottleneck, persist data non-blockingly to a NoSQL database, and publish/consume domain events using a resilient Kafka KRaft architecture.

## 🛠️ Tech Stack & Concepts
* **Language:** Kotlin (leveraging Coroutines for fully non-blocking I/O)
* **Framework:** Spring Boot 3, Spring for GraphQL, Spring Kafka
* **Database:** AWS DynamoDB Local (via AWS SDK v2 Enhanced Async Client)
* **Event Streaming:** Apache Kafka (KRaft Mode - No Zookeeper)
* **Testing:** JUnit 5, MockK, `kotlinx-coroutines-test`
* **Infrastructure:** Docker & Docker Compose

## 🧠 Architectural Highlights
1. **Optimized GraphQL Data Fetching:** Implemented lazy-loaded `@SchemaMapping` resolvers. The application only queries DynamoDB for nested relationships (like a Person's Courses) if the client explicitly requests them, entirely avoiding over-fetching and the N+1 problem.
2. **Reactive Data Layer:** Replaced traditional blocking database calls with `DynamoDbEnhancedAsyncClient` and Kotlin `suspend` functions, ensuring high throughput under load.
3. **Resilient Kafka Processing:** Designed an event-driven workflow with `@RetryableTopic`. Consumers feature exponential backoff for transient failures and a dedicated Dead Letter Queue (DLQ) `@DltHandler` to isolate poison pills without blocking partitions.

---

## 💻 Local Environment Setup

### 1. Prerequisites
* Docker Desktop running
* Java 17+ installed

### 2. Spin up the Infrastructure
The project uses Docker Compose to run a local DynamoDB instance and a modern Zookeeper-less Kafka broker (KRaft).
```bash
docker compose up -d
```
*Verify containers are running: `docker ps` (You should see `cp-kafka` and `dynamodb-local`).*

### 3. Start the Spring Boot Application
```bash
./gradlew bootRun
```
*The application will automatically connect to DynamoDB (port 8000) and Kafka (port 9092).*

---

## 🧪 Testing the Features

### 1. Testing the GraphQL API (GraphiQL)
Navigate to **`http://localhost:8080/graphiql`** in your browser.

**A. Create a Person (Mutation)**
Paste this into the GraphiQL editor to create a new record. Notice how the shape of the requested return data dictates exactly what the server sends back.

```graphql
mutation CreateMyPerson {
  createPerson(request: {
    name: "Luke Skywalker",
    age: 22,
    email: "luke@example.com",
    address: {
      city: "Tatooine",
      state: "Outer Rim",
      pinCode: 1138
    }
  }) {
    personId
    name
    email
  }
}
```

**B. Fetch Nested Data on Demand (Query)**
*(Make sure to replace the ID with the one generated from the previous step).* Because we request `enrolledCourses`, Spring GraphQL will automatically trigger the `@SchemaMapping` to resolve the nested list. If you remove the `enrolledCourses` block, the database is never queried for courses!

```graphql
query FetchPerson {
  getPerson(id: "YOUR-GENERATED-UUID-HERE") {
    name
    address {
      city
    }
    enrolledCourses {
      title
      price
    }
  }
}
```

### 2. Testing Kafka (Happy Path & Dead Letter Queue)
When a Person is created via the GraphQL mutation, the service automatically publishes a `PersonCreatedEvent` to Kafka.

* **The Happy Path:** Run the `CreateMyPerson` mutation with a normal email. Look at your Spring Boot console. You will see the Producer publish the event, and the Consumer immediately process it:
  > `Successfully published event for [UUID] at offset 0`
  > `Received Kafka event! Sending welcome email to: luke@example.com`

* **Testing the Dead Letter Queue (Poison Pill):** Run the mutation again, but change the email to `"vader@error.com"`. Our consumer is programmed to fail on emails containing "error".
  Watch the console: You will see Spring Kafka automatically retry the message 3 times with an exponential backoff (2s, then 4s). After all retries fail, it routes the message to the DLQ:
  > `🚨 DLQ ALERT 🚨`
  > `Message for [UUID] failed all retries.`

### 3. Running Unit Tests (MockK & Coroutines)
The service layer is fully unit-tested using **MockK** to mock dependencies and `runTest` to handle Kotlin coroutine scopes.

To run the test suite:
```bash
./gradlew test
```
**Testing Strategy:**
* **`coEvery` / `coVerify`:** Used extensively to mock and verify asynchronous `suspend` functions interacting with the DynamoDB repository and Kafka Producer.
* **Isolation:** Ensures business logic (like verifying a course exists before enrolling a person) works perfectly without needing a real database or broker connection.

***