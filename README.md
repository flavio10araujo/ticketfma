# Ticket FMA Service

Simple API to search and reserve seats for events.

## Technologies

In this project we, are using the following technologies:

- Java 21
- Spring Boot 3.3.4
- Swagger 2.6.0
- Opencsv 5.7.1

## Explanation

Run the class located at /src/main/java/com/ticketfma/Application.java to start the application.

The application will start on port 8080.

The application has a redirect from http://localhost:8080 to the swagger page, where you can see the endpoints and test them.

## Notes

About searching events:

1. As it was requested to search events by name but the CSV file does not have a column with the name of the event, we decided to name the events as: "Event " + eventId.
2. We are considering that all the events in the CSV are available events, even if there are no more available seats in the event.
3. The search by event is sorted "ascending" by name or by date. No "descending" option is available in this version. Example: `localhost:8080/api/v1/events?sort=name` or `localhost:8080/api/v1/events?sort=date`.

About searching and reserving seats:

1. We are considering the column "sellRank" ascending to identify the better seats. Example: sellRank 1 is better than sellRank 2.
2. The reserve seat endpoint updates the seat status from OPEN to HOLD. We did not implement the feature to confirm the reservation and update the status to SOLD.
3. We are considering that if a single seat in the list is not available, the reservation will not be made.

## Possible Improvements

### Performance

#### Cache

This exercise uses a single instance with in-memory storage.
However, if this application evolves to use a real database and multiple instances, we should consider using a distributed cache to improve performance.

#### Read-replicas

This application has use cases with both read and write operations.
If the application grows, we could consider using read-replicas to avoid blocking write operations.

#### Indexes

As we search events by name and date, we could consider creating indexes in the database to improve search performance.

### Security

#### API-KEY

We could consider adding an API-KEY to the endpoints to improve the security of the application.

#### Rate Limit

We could consider adding a rate limit to the endpoints to avoid abuse of the application. This could be done by adding a Gateway in front of the application.

### Observability

#### Logs, metrics and traces

We could consider adding logs, metrics, and traces to the application to monitor the application and identify possible bottlenecks.

### Others

#### Avoiding race conditions

As this application was developed as an exercise with a single instance and in-memory storage, we used ConcurrentHashMap and ReentrantLock to control access to the data and prevent two clients from reserving the same seat at the same time.

The chosen approach, instead of using synchronized methods, was to avoid blocking the entire object, which would have prevented reservations for seats in different events from being processed concurrently.

However, if this application evolves to use a real database and multiple instances, we should consider using a distributed cache with a lock or lease mechanism to achieve a similar outcome.

Another option would be an asynchronous approach, where reservations are placed in a queue and processed by a worker. In this case, instead of returning the reservation immediately, we could return a reservationId, allowing the client to check the reservation status later or receive an email confirmation.
