# Manual Replacement

This module shows how you can make use of an annotation processor
to replace an existing .java file.

If you check the AnnotatedStudent.java file, you can see that it does
not have a toString() method, but the class is annotated with @CustomToString.
This is a custom annotation  that will generate a  toString() method and replace
it if an existing one is already declared.

## Testing

You can test the method generation by compiling the module. Once you compile it, the class
should have a toString() method like so
```
@Override
public String toString() {
    return "name: " + name + ", email: " + email;
}
```
This is automatically generated from the field names of the class.

Therefore, when you run Main.java, you can also see that the printed result for
Student and AnnotatedStudent is different
```
Student@74a14482
name: Joshua, email: joshuaxu71@gmail.com
```

Now, let's test the scenario where the user adjusted the toString() method manually.
```
@Override
public String toString() {
    return "name: " + name + ", email: " + email + "this is now edited";
}
```

Now if we compile the code again, the toString() method is replaced properly.
```
@Override
public String toString() {
    return "name: " + name + ", email: " + email;
}
```

And if we run Main.java, it'll return the same output
```
Student@74a14482
name: Joshua, email: joshuaxu71@gmail.com
```