# Automated Registration with auto-service

This module shows how you can make use of Google's auto-service library ([link](https://github.com/google/auto/tree/main/service))
to register the annotation processor.

This is just a duplicate of the manual-replace module, but with slight changes. Those changes are:
1. We no longer need to register the annotation processors by creating a META-INF\services\javax.annotation.processing.Processor
2. We no longer need to have separated compilation executions in the pom.xml
3. We need to separate the implementation's module from the annotation processor module

The reason we need to separate the annotation processor module is because when the @AutoService annotation
is processed, it generates the META-INF file in the output's classes directory (e.g. target\classes\META-INF).
However, this file is not accessed in the same compilation, so we need to move the actual implementation of the 
annotation to another module ([auto-service-impl](https://github.com/joshuaxu71/annotation/tree/auto-service/auto-service-impl)).