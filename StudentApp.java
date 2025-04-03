import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import javax.persistence.*;
import java.util.List;

// Hibernate Entity Class
@Entity
@Table(name = "students")
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column
    private int age;

    public Student() {}

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', age=" + age + "}";
    }
}

// Hibernate Utility Class (DAO)
class StudentDAO {
    private static SessionFactory factory;

    static {
        try {
            factory = new Configuration()
                    .configure() // Loads hibernate.cfg.xml
                    .addAnnotatedClass(Student.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addStudent(Student student) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(student);
        tx.commit();
        session.close();
    }

    public Student getStudent(int id) {
        Session session = factory.openSession();
        Student student = session.get(Student.class, id);
        session.close();
        return student;
    }

    public void updateStudent(int id, String newName, int newAge) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) {
            student.setName(newName);
            student.setAge(newAge);
            session.update(student);
        }
        tx.commit();
        session.close();
    }

    public void deleteStudent(int id) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) session.delete(student);
        tx.commit();
        session.close();
    }

    public List<Student> getAllStudents() {
        Session session = factory.openSession();
        List<Student> students = session.createQuery("from Student", Student.class).list();
        session.close();
        return students;
    }
}

// Main Application
public class StudentApp {
    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        // Create
        studentDAO.addStudent(new Student("Alice", 22));
        studentDAO.addStudent(new Student("Bob", 25));

        // Read
        System.out.println("Retrieved: " + studentDAO.getStudent(1));

        // Update
        studentDAO.updateStudent(1, "Alice Johnson", 23);
        System.out.println("Updated: " + studentDAO.getStudent(1));

        // List All
        System.out.println("All Students: " + studentDAO.getAllStudents());

        // Delete
        studentDAO.deleteStudent(2);
        System.out.println("After Deletion: " + studentDAO.getAllStudents());
    }
}
