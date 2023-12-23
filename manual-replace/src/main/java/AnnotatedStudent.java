@CustomToString
public class AnnotatedStudent {

    String name;

    String email;

    public AnnotatedStudent(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "name: " + name + ", email: " + email;
    }
}
