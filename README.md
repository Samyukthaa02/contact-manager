# Contact Manager (Java 8 sample)

A minimal console contact manager implemented using Java 8.

How to build:
- Requires JDK 8 and Maven.
- mvn clean package
- java -jar target/contact-manager-1.0.0-jar-with-dependencies.jar

Storage:
- Uses data/contacts.csv (created automatically).

Why this is Java 8 friendly but upgradeable:
- Contact is a POJO (would be a record in Java 21).
- Background thread uses Thread + Runnable (would be virtual threads / structured concurrency).
- Anonymous Comparator and Optional.isPresent/get usage (can be replaced by Comparator.comparing and pattern matching/Optional.ifPresentOrElse).
- CSV storage is simple; in a modern app you'd likely use more robust serialization or an embedded database.
