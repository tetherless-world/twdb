# Tetherless World Knowledge Store (TWKS)

TWKS is a [provenance-aware](https://www.w3.org/TR/prov-o/) [RDF](https://www.w3.org/RDF/) store.

The store is implemented in Java, and exposes several interfaces:
* a REST API for creating, reading, updating, and deleting [nanopublications](http://nanopub.org)
* a [SPARQL 1.1](https://www.w3.org/TR/sparql11-protocol/) endpoint
* a Java library for programmatic use

The primary API is defined by [`Twks.java`](java/lib/src/main/java/edu/rpi/tw/twks/lib/Twks.java).

# Using the server

See the [Docker documentation](docker/README.md) for server setup.

# Programmatic use

See the language-specific documentation:
* [Java](java/README.md)
