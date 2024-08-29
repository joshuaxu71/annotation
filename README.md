# Annotations

Medium Blog Post: [link](https://medium.com/@joshuaxu71/things-we-take-for-granted-1-annotations-e45c94a4fde0)

Hello :D

In this repository, we are exploring how annotations work in Java (Maven).
We are trying to create an annotation, register it, and use the result of the
processed annotation, which in this case, is a custom toString() method.

There are currently 3 different methods that we are exploring:
1. Replacing the .java file using the annotation processor (manual-replace) 
2. Generating a new file in generated-sources (manual-generate)
3. Using Google's Auto Service, a third-party library (auto-service)

You can see the results of each method in the respective modules.

To run the modules properly, please run `mvn install -f .\common\` first since
they are dependent on the common module.

Also, when compiling each submodule, please do it from the respective submodule,
because I'm using user.dir as part of the file path generation :3
