import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for handling persistence of items
 */
public class ItemController {

    private EntityManagerFactory entityManagerFactory;

    /**
     * Constructor with dependency injection for JPA entityManagerFactory
     * @param entityManagerFactory Injectable entityManagerFactory
     */
    public ItemController(EntityManagerFactory entityManagerFactory){
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Creates a new todo item in the database
     * @param description Description of the item
     * @param priority Priority of the item
     * @param deadlineDate Deadline date of the item
     * @return True if item created successfully
     */
    public boolean createItem(String description, Priority priority, LocalDate deadlineDate) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(new Item(description, priority, deadlineDate));
        entityManager.getTransaction().commit();
        entityManager.close();
        return true;
    }

    /**
     * Gets an item by id
     * @param id Id of the item to get
     * @return Item if found, null if not found or deleted
     */
    public Item getItem(int id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Item item = entityManager.find(Item.class, id);
        entityManager.getTransaction().commit();
        entityManager.close();
        return (item != null && item.isActive) ? item : null;
    }

    /**
     * Gets all items
     * @param getExpiredItems If true, fetches items whose deadlines are in the past.
     *                        If false, fetches items whose deadlines are in the future.
     * @return List of items matching criteria
     */
    public List<Item> getItems(boolean getExpiredItems) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Item> items;
        if (getExpiredItems){
             items = entityManager.createQuery(
                    "SELECT i FROM Item i WHERE isActive = 'true' AND deadlineDate < :now ORDER BY deadlineDate desc", Item.class)
                    .setParameter("now", LocalDate.now())
                    .getResultList();
        } else {
            items = entityManager.createQuery(
                    "SELECT i FROM Item i WHERE isActive = 'true' AND deadlineDate >= :now ORDER BY deadlineDate", Item.class)
                    .setParameter("now", LocalDate.now())
                    .getResultList();
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return items;

    }

    /**
     * Updates the item with new values
     * @param id Id of the item to update
     * @param description New description of the item
     * @param priority New priority of the item
     * @param deadlineDate New deadline date of the item
     * @param isCompleted New completion status of the item
     * @return True if the item was updated. False if the item was not found or it was deleted.
     */
    public boolean updateItem(int id, String description, Priority priority, LocalDate deadlineDate, boolean isCompleted){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Item item = entityManager.find(Item.class, id);
        boolean existsAndIsActive = (item != null && item.isActive);
        if (existsAndIsActive){
            item.description = description;
            item.priority = priority;
            item.deadlineDate = deadlineDate;
            item.isCompleted = isCompleted;
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return existsAndIsActive;
    }

    /**
     * Toggles the completion status of the item
     * @param id Id of the item whose status to toggle
     * @return True if the status was toggled. False if the item was not found or was deleted.
     */
    public boolean toggleCompleted(int id){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Item item = entityManager.find(Item.class, id);
        boolean existsAndIsActive = (item != null && item.isActive);
        if (existsAndIsActive){
            item.isCompleted = !item.isCompleted;
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return existsAndIsActive;
    }

    /**
     * Soft deletes an item
     * @param id Id of the item to delete
     * @return True if the item was deleted. False if the item was not found or it was already deleted.
     */
    public boolean deleteItem(int id){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Item item = entityManager.find(Item.class, id);
        boolean existsAndIsActive = (item != null && item.isActive);
        if (existsAndIsActive){
            item.isActive = false;
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return existsAndIsActive;
    }

}
