public class Main {
	public static void main(String[] args) {
		Student student = new Student("Joshua", "joshuaxu71@gmail.com");
		AnnotatedStudent annotatedStudent = new AnnotatedStudent("Joshua", "joshuaxu71@gmail.com");

		System.out.println(student);
		System.out.println(annotatedStudent);
	}
}