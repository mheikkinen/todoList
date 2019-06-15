import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemControllerTest {

    private static EntityManagerFactory entityManagerFactory;
    private static ItemController itemController;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory( "test.inmemory.database" );
        itemController = new ItemController(entityManagerFactory);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }

    @Test
    public void createItemAndGetItems_shouldCreateAndGetItems(){
        // Arrange
        List<Item> items = itemController.getItems(false);
        assertEquals(0, items.size());
        itemController.createItem("First item", Priority.HIGH, LocalDate.now());
        // Act
        items = itemController.getItems(false);
        // Assert
        assertEquals(1, items.size());
        Item item = items.get(0);
        assertAll(
                () -> assertEquals(1, item.id),
                () -> assertEquals(LocalDate.now(), item.deadlineDate),
                () -> assertEquals("First item", item.description),
                () -> assertEquals(Priority.HIGH, item.priority),
                () -> assertFalse(item.isCompleted),
                () -> assertTrue(item.isActive)
        );
    }

    @Test
    public void getItem_itemExists_shouldReturnItem(){
        // Arrange
        itemController.createItem("First item", Priority.HIGH, LocalDate.now());
        // Act
        Item item = itemController.getItem(1);
        // Assert
        assertNotNull(item);
        assertAll(
                () -> assertEquals(1, item.id),
                () -> assertEquals(LocalDate.now(), item.deadlineDate),
                () -> assertEquals("First item", item.description),
                () -> assertEquals(Priority.HIGH, item.priority),
                () -> assertFalse(item.isCompleted),
                () -> assertTrue(item.isActive)
        );
    }

    @Test
    public void getItem_invalidItem_shouldReturnNull(){
        // Arrange
        itemController.createItem("First item", Priority.HIGH, LocalDate.now());
        // Act
        Item item = itemController.getItem(2);
        // Assert
        assertNull(item);
    }

    @Test
    public void getItem_deletedItem_shouldReturnNull(){
        // Arrange
        itemController.createItem("First item", Priority.HIGH, LocalDate.now());
        itemController.deleteItem(1);
        // Act
        Item item = itemController.getItem(1);
        // Assert
        assertNull(item);
    }

    @Test
    public void getItems_shouldNotGetDeletedItems(){
        // Arrange
        itemController.createItem("GetItem", Priority.MEDIUM, LocalDate.now());
        itemController.deleteItem(1);
        // Act
        List<Item> items = itemController.getItems(false);
        // Assert
        assertEquals(0, items.size());
    }

    @Test
    public void getItems_shouldBeArrangedByDate(){
        // Arrange
        itemController.createItem("Tomorrow's item", Priority.MEDIUM, LocalDate.now().plusDays(1));
        itemController.createItem("Today's item", Priority.MEDIUM, LocalDate.now());
        itemController.createItem("Item in two days", Priority.MEDIUM, LocalDate.now().plusDays(2));
        // Act
        List<Item> items = itemController.getItems(false);
        // Assert
        assertEquals(3, items.size());
        assertAll(
                () -> assertEquals("Today's item", items.get(0).description),
                () -> assertEquals("Tomorrow's item", items.get(1).description),
                () -> assertEquals("Item in two days", items.get(2).description)
        );
    }
    @Test

    public void getItems_expiredItems_shouldBeArrangedByDate(){
        // Arrange
        itemController.createItem("Item from three days ago", Priority.MEDIUM, LocalDate.now().minusDays(3));
        itemController.createItem("Yesterday's item", Priority.MEDIUM, LocalDate.now().minusDays(1));
        itemController.createItem("Item from two days ago", Priority.MEDIUM, LocalDate.now().minusDays(2));
        // Act
        List<Item> items = itemController.getItems(true);
        // Assert
        assertEquals(3, items.size());
        assertAll(
                () -> assertEquals("Yesterday's item", items.get(0).description),
                () -> assertEquals("Item from two days ago", items.get(1).description),
                () -> assertEquals("Item from three days ago", items.get(2).description)
        );
    }

    @Test
    public void getItems_getExpiredItemsFalse_shouldGetOnlyFutureItems(){
        // Arrange
        itemController.createItem("FutureItem", Priority.MEDIUM, LocalDate.now());
        itemController.createItem("PastItem", Priority.MEDIUM, LocalDate.now().minusDays(1));
        itemController.createItem("PastItem2", Priority.MEDIUM, LocalDate.now().minusDays(1));
        // Act
        List<Item> items = itemController.getItems(false);
        // Assert
        assertEquals(1, items.size());
        assertEquals("FutureItem", items.get(0).description);
    }

    @Test
    public void getItems_getExpiredItemsTrue_shouldGetOnlyExpiredItems(){
        // Arrange
        itemController.createItem("FutureItem", Priority.MEDIUM, LocalDate.now());
        itemController.createItem("PastItem", Priority.MEDIUM, LocalDate.now().minusDays(1));
        itemController.createItem("PastItem2", Priority.MEDIUM, LocalDate.now().minusDays(1));
        // Act
        List<Item> items = itemController.getItems(true);
        // Assert
        assertEquals(2, items.size());
        assertEquals("PastItem", items.get(0).description);
        assertEquals("PastItem2", items.get(1).description);
    }

    @Test
    public void updateItem_validId_shouldUpdateValues(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        // Act
        boolean updated = itemController.updateItem(1, "NewDescription",Priority.MEDIUM, LocalDate.now().plusDays(1), true);
        Item item = itemController.getItem(1);
        // Assert
        assertTrue(updated);
        assertNotNull(item);
        assertAll(
                () -> assertEquals(1, item.id),
                () -> assertEquals(LocalDate.now().plusDays(1), item.deadlineDate),
                () -> assertEquals("NewDescription", item.description),
                () -> assertEquals(Priority.MEDIUM, item.priority),
                () -> assertTrue(item.isCompleted),
                () -> assertTrue(item.isActive)
        );
    }

    @Test
    public void updateItem_invalidId_shouldReturnFalse(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        // Act
        boolean updated =  itemController.updateItem(2, "NewDescription",Priority.MEDIUM, LocalDate.now().plusDays(1), true);
        // Assert
        assertFalse(updated);
    }

    @Test
    public void updateItem_deletedId_shouldReturnFalse(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        itemController.deleteItem(1);
        // Act
        boolean updated =  itemController.updateItem(1, "NewDescription",Priority.MEDIUM, LocalDate.now().plusDays(1), true);
        // Assert
        assertFalse(updated);
    }

    @Test
    public void toggleCompleted_validId_ShouldReturnTrueAndToggle(){
        // Arrange
        itemController.createItem("Description",Priority.LOW, LocalDate.now());
        // Act
        Item beforeToggle = itemController.getItem(1);
        boolean result = itemController.toggleCompleted(1);
        Item afterToggle = itemController.getItem(1);
        boolean result2 = itemController.toggleCompleted(1);
        Item afterToggle2 = itemController.getItem(1);
        // Assert
        assertTrue(result);
        assertTrue(result2);
        assertFalse(beforeToggle.isCompleted);
        assertTrue(afterToggle.isCompleted);
        assertFalse(afterToggle2.isCompleted);
    }

    @Test
    public void toggleCompleted_invalidId_ShouldReturnFalse(){
        // Arrange
        itemController.createItem("Description",Priority.LOW, LocalDate.now());
        // Act
        boolean result = itemController.toggleCompleted(2);
        // Assert
        assertFalse(result);
    }

    @Test
    public void toggleCompleted_deletedId_ShouldReturnFalseAndNotToggle(){
        // Arrange
        itemController.createItem("Description",Priority.LOW, LocalDate.now());
        itemController.deleteItem(1);
        // Act
        Item beforeToggle = itemController.getItem(1);
        boolean result = itemController.toggleCompleted(1);
        Item afterToggle = itemController.getItem(1);
        // Assert
        assertNull(beforeToggle);
        assertNull(afterToggle);
        assertFalse(result);
    }

    @Test
    public void deleteItem_validId_shouldReturnTrueAndSoftDelete(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        // Act
        boolean result = itemController.deleteItem(1);
        List<Item> items = itemController.getItems(false);
        // Assert
        assertTrue(result);
        assertEquals(0, items.size());
    }

    @Test
    public void deleteItem_deletedId_shouldReturnFalse(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        // Act
        itemController.deleteItem(1);
        boolean result = itemController.deleteItem(1);
        // Assert
        assertFalse(result);
    }

    @Test
    public void deleteItem_invalidId_shouldReturnFalse(){
        // Arrange
        itemController.createItem("OldDescription",Priority.LOW, LocalDate.now());
        // Act
        boolean result = itemController.deleteItem(2);
        // Assert
        assertFalse(result);
    }

}