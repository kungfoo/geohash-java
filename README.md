java code for geohashes
=======================

An implementation of Geohashes in pure Java.
The produced hashes, when using character precision (multiples of 5 bits) are compatible
to the reference implementation geohash.org.

You can however also encode Geohashes down to the full available precision of a long i.e. 64 bits.

Build status
------------

<a href="https://travis-ci.org/kungfoo/geohash-java"><img src="https://travis-ci.org/kungfoo/geohash-java.svg?branch=release-1.0"/></a>

Getting the library
-------------------

As of today, new releases are made available via maven Central.
Thus you can use the artifacts in various build systems like maven, ivy, gradle, sbt and mayn more.

Find the current versions here:

    http://search.maven.org/#search|ga|1|g%3A%22ch.hsr%22%20AND%20a%3A%22geohash%22


Building/Testing the code
-------------------------

The geohash-java code can be built using Apache Ant or Apache Maven.
Ant targets that are available:

    - clean     # wash dishes.
    - compile   # compile the main source code.
    - test      # (default) run the unit tests.
    - jar       # pack a jar archive with the compiled class files.
    - zip       # pack a zip archive with the compiled binaries, source code and all other files.

Maven targets are the usual suspects.
    
    - compile
    - test
    - package   # pack a versioned jar containing the compiled class files

TODO
----

Current development involves getting the following things to run:

-	a method to find the n closest hashes to any given point.


Last important changes
----------------------

Version 1.0.14
----------------------

Fix the code of GeoHash.compareTo() to actually be jdk-6 compatible.

Version 1.0.13
-----------------------

Merge this fix https://github.com/0mok/geohash-java/commit/12550a392ea974cf75c81fce2bd21fe1535715a1
It causes compareTo() of geohashes to behave similarly as comaring their base32 encoded strings

Version 1.0.12
-----------------------

toBase32() now throws an exception if the hash precision is not a multiple of 5.

Version 1.0.11
-----------------------

-   Added a static method to obtain a geohash string in one call.
    Thanks to Andy Chosak.

Version 1.0.10
--------------

Finally realeased to maven central.

Version 1.0.9
-------------

-   Added method getCharacterPrecision() to GeoHash class

Version 1.0.8
-------------

-   Changed the code in VincentyGeodesy not to stumble over the fact that NaN == NaN will always return false

Version 1.0.6
-------------

-	Added a small fix that keeps VincentyGeodesy#moveInDirection() from returning wrong values when moving large distances. [submitted by André Kischkel]

Version 1.0.5
-------------

-   Added next(), next(step) and prev() methods to Geohash.
-   Added fromBase32() and toBase32() to TwoGeoHashBoundingBox.
-   Cleaned up the entire source tree using clearly defined settings.

Version 1.0.4
-------------

-   Added/fixed the methods toBinaryString() and Geohash.fromBinaryString() that can encode and decode a geohash into a simple String of 0s and 1s.
-   Also added test code for those methods.

Version 1.0.3
-------------

-   Classes containing data are now Serializable.

Version 1.0.2
-------------

-   Merged Kevins [ktcorby] code that yields all the geohashes in a bounding box, at a given resolution.
-   Merged Chris' [cchandler] code to encode and decode geohashes from a binary string.

Version 1.0.1
-------------

-   Fixed issue #2 from Github:
	- Neighbouring hashes can be calculated by using the getAdjacent() method, which yields a hashes 8 neighbors.
	- northern, eastern, western and southern neighbours can be determined using the respective methods.

-   Fixed issue #1 from Github:
	- A bug caused different hashes to be yielded sometimes.

	
License
-------

This code has been placed under the LGPL. See the LICENSE file for more information.
Please contribute improvements and bug fixes back via github.
