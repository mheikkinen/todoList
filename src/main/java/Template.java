import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Data object representing a template for creating todo items
 */
@Entity
public class Template {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int id;
    public String name;
    public String description;
    public Priority priority;
    public long daysToAdd;
    public boolean isActive;

    /**
     * Constructor for creating templates for todo items inside the program
     * @param name Name of the template
     * @param description Description of the todo item provided by the template
     * @param priority Priority of the todo item provided by the template
     * @param daysToAdd Days to add to tomorrow in the todo item provided by the template
     */
    public Template(String name, String description, Priority priority, long daysToAdd){
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.daysToAdd = daysToAdd;
        this.isActive = true;
    }

    /**
     * Empty constructor for Hibernate/JPA
     */
    public Template(){}
}
