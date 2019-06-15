import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateControllerTest {

    private static EntityManagerFactory entityManagerFactory;
    private static TemplateController templateController;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory( "test.inmemory.database" );
        templateController = new TemplateController(entityManagerFactory);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }

    @Test
    public void createTemplateAndGetTemplates_shouldCreateAndGetTemplates(){
        // Arrange
        List<Template> templates = templateController.getTemplates();
        assertEquals(0, templates.size());
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        templates = templateController.getTemplates();
        // Assert
        assertEquals(1, templates.size());
        Template template = templates.get(0);
        assertAll(
                () -> assertEquals(1, template.id),
                () -> assertEquals("name", template.name),
                () -> assertEquals("description", template.description),
                () -> assertEquals(Priority.MEDIUM, template.priority),
                () -> assertEquals(0L, template.daysToAdd),
                () -> assertTrue(template.isActive)
        );
    }

    @Test
    public void getTemplate_templateExists_ShouldGetTemplate(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        Template template = templateController.getTemplate(1);
        // Assert
        assertNotNull(template);
        assertAll(
                () -> assertEquals(1, template.id),
                () -> assertEquals("name", template.name),
                () -> assertEquals("description", template.description),
                () -> assertEquals(Priority.MEDIUM, template.priority),
                () -> assertEquals(0L, template.daysToAdd),
                () -> assertTrue(template.isActive)
        );
    }

    @Test
    public void getTemplate_invalidId_ShouldReturnNull(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        Template template = templateController.getTemplate(2);
        // Assert
        assertNull(template);
    }

    @Test
    public void getTemplate_deletedId_ShouldReturnNull(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        templateController.deleteTemplate(1);
        // Act
        Template template = templateController.getTemplate(1);
        // Assert
        assertNull(template);
    }

    @Test
    public void getTemplates_shouldNotGetDeletedItems(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        templateController.deleteTemplate(1);
        // Act
        List<Template> templates = templateController.getTemplates();
        // Assert
        assertEquals(0, templates.size());
    }

    @Test
    public void updateTemplate_validId_shouldUpdateValues(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        boolean result = templateController.updateTemplate(1, "newName", "newDescription", Priority.HIGH, 1L);
        Template template = templateController.getTemplate(1);
        // Assert
        assertNotNull(template);
        assertAll(
                () -> assertEquals(1, template.id),
                () -> assertEquals("newName", template.name),
                () -> assertEquals("newDescription", template.description),
                () -> assertEquals(Priority.HIGH, template.priority),
                () -> assertEquals(1L, template.daysToAdd),
                () -> assertTrue(template.isActive)
        );
    }

    @Test
    public void updateTemplate_invalidId_shouldReturnNull(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        boolean result  = templateController.updateTemplate(2, "newName", "newDescription", Priority.HIGH, 1L);
        // Assert
        assertFalse(result);
    }

    @Test
    public void updateTemplate_deletedId_shouldReturnNull(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        templateController.deleteTemplate(1);
        // Act
        boolean result  = templateController.updateTemplate(1, "newName", "newDescription", Priority.HIGH, 1L);
        // Assert
        assertFalse(result);
    }

    @Test
    public void deleteTemplate_validId_shouldReturnTrueAndSoftDelete(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        // Act
        boolean result = templateController.deleteTemplate(1);
        List<Template> templates = templateController.getTemplates();
        // Assert
        assertTrue(result);
        assertEquals(0, templates.size());
    }

    @Test
    public void deleteItem_invalidId_shouldReturnFalse(){
        // Arrange
        templateController.createTemplate("name", "description", Priority.MEDIUM, 0L);
        templateController.deleteTemplate(1);
        // Act
        boolean result = templateController.deleteTemplate(1);
        // Assert
        assertFalse(result);
    }
}