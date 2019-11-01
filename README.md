# Black Rook Redis

Copyright (c) 2019 Black Rook Software. All rights reserved.  
[https://github.com/BlackRookSoftware/Redis](https://github.com/BlackRookSoftware/Redis)

[Latest Release](https://github.com/BlackRookSoftware/Redis/releases/latest)

### Required Libraries

NONE

### Required Java Modules

[java.base](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html)  

### Introduction

This library contains classes for Redis Server functions, for both client and administrative use.

Currently compatible with Redis 2.6+

### Why?

Some other Redis-interfacing libraries do the bare minimum for supporting commands, and they don't
separate them categorically into meaningful ways suitable for DAOs or other drivers, nor do they contain
ways to create/store object data, reflection-wise. 

This one does.  

### Library

Contained in this release is a series of classes that are used for Redis server functions, plus
object conversion utilities. Supports pipelining series of commands and raw writes/reads from
a Redis connection, as well as Redis cursors.

The javadocs contain basic outlines of each package's contents.

### Compiling with Ant

To compile this library with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this library (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To compile, JAR, test, and Zip up everything:

	ant release

To clean up everything:

	ant clean
	
### Javadocs

Online Javadocs can be found at: [https://blackrooksoftware.github.io/Redis/javadoc](https://blackrooksoftware.github.io/Redis/javadoc)

### Other

This program and the accompanying materials are made available under the 
terms of the LGPL v2.1 License which accompanies this distribution.

A copy of the LGPL v2.1 License should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 

This contains code copied from Black Rook Base, under the terms of the MIT License (docs/LICENSE-BlackRookBase.txt).
