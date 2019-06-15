import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * Data object representing a todo item
 */
@Entity
public class Item {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int id;
    public String description;
    public Priority priority;
    public LocalDate deadlineDate;
    public boolean isCompleted;
    public boolean isActive;

    /**
     * Constructor for creating todo items inside the program
     * @param description Description of the todo item
     * @param priority Priority of the todo item
     * @param deadlineDate Deadline date of the todo item
     */
    public Item (String description, Priority priority, LocalDate deadlineDate){
        this.description = description;
        this.priority = priority;
        this.deadlineDate = deadlineDate;
        this.isActive = true;
    }

    /**
     * Empty constructor for Hibernate/JPA
     */
    public Item(){}
}
