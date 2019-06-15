import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for handling persistence of templates
 */
public class TemplateController {

    private EntityManagerFactory entityManagerFactory;

    /**
     * Constructor with entityManagerFactory injection
     * @param entityManagerFactory Injectable entityManagerFactory
     */
    public TemplateController(EntityManagerFactory entityManagerFactory){
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Creates a new template for todo items
     * @param name Name of the template
     * @param description Default description of the todo item provided by the template
     * @param priority Default priority of the todo item provided by the template
     * @param daysToAdd Default number of days until the deadline of the todo item provided by the template
     * @return True if template created successfully
     */
    public boolean createTemplate(String name, String description, Priority priority, long daysToAdd) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(new Template(name, description, priority, daysToAdd));
        entityManager.getTransaction().commit();
        entityManager.close();
        return true;
    }

    /**
     * Gets all available templates
     * @return List of templates in the database
     */
    public List<Template> getTemplates() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Template> templates = entityManager.createQuery(
                    "SELECT t FROM Template t WHERE isActive = 'true'", Template.class)
                    .getResultList();
        entityManager.getTransaction().commit();
        entityManager.close();
        return templates;
    }

    /**
     * Gets a template by its Id
     * @param id Id of the template to get
     * @return Template if found. Null if no template with that id or if it has been deleted.
     */
    public Template getTemplate(int id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Template template = entityManager.find(Template.class, id);
        entityManager.getTransaction().commit();
        entityManager.close();
        return (template != null && template.isActive) ? template : null;
    }

    /**
     * Updates template with new values
     * @param id Id of the template to update
     * @param name Updated name of the template
     * @param description Updated default description of the todo item provided by the template
     * @param priority Updated default priority of the todo item provided by the template
     * @param daysToAdd Updated default number of days until the deadline of the todo item provided by the template
     * @return True if successfully updated. False if template with given id doesn't exist or has been deleted.
     */
    public boolean updateTemplate(int id, String name, String description, Priority priority, long daysToAdd){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Template template = entityManager.find(Template.class, id);
        boolean existsAndIsActive = (template != null && template.isActive);
        if (existsAndIsActive){
            template.name = name;
            template.description = description;
            template.priority = priority;
            template.daysToAdd = daysToAdd;
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return existsAndIsActive;
    }

    /**
     * Deletes a template
     * @param id Id of the template to delete
     * @return True if successfully deleted. False if template with given id doesn't exist or has been deleted.
     */
    public boolean deleteTemplate(int id){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Template template = entityManager.find(Template.class, id);
        boolean existsAndIsActive = (template != null && template.isActive);
        if (existsAndIsActive){
            template.isActive = false;
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return existsAndIsActive;
    }
}
