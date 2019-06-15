import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main class for the TODO-list project
 */
public class Main {

    private static EntityManagerFactory entityManagerFactory;
    private static ItemController itemController;
    private static TemplateController templateController;
    private static Scanner scanner;

    static {
        // If you want to test using a in-memory database, uncomment this line and comment the following one
        entityManagerFactory = Persistence.createEntityManagerFactory( "test.inmemory.database" );
        // Comment this line if using in-memory database
        //entityManagerFactory = Persistence.createEntityManagerFactory( "local.h2" );
        itemController = new ItemController(entityManagerFactory);
        templateController = new TemplateController(entityManagerFactory);
        scanner = new Scanner(System.in);
        // Comment this is you don't want to generate demo data
        createDemoData(20);
    }

    /**
     * Contains the main loop of the program
     * @param args Not used
     */
    public static void main(String[] args) {
        System.out.println("Hello!");
        printHelp();
        boolean quit = false;
        while (!quit){
            String i = scanner.nextLine();
            if (i.matches("^1"))
                printFutureItems();
            else if (i.matches("^2"))
                printPastItems();
            else if (i.matches("^3"))
                printTemplates();
            else if (i.matches("^q") ||i.matches("^x"))
                quit = true;
            else if (i.matches("^c"))
                createItem();
            else if (i.matches("^u\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                updateItem(id);
            }
            else if (i.matches("^s\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                toggleStatusOfItem(id);
            }
            else if (i.matches("^d\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                deleteItem(id);
            }
            else if (i.matches("^ct\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                createItemFromTemplate(id);
            }
            else if (i.matches("^ct"))
                createTemplate();
            else if (i.matches("^ut\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                updateTemplate(id);
            }
            else if (i.matches("^dt\\s\\d+$")){
                int id = Integer.valueOf(i.split("\\s")[1]);
                deleteTemplate(id);
            }
            else if (i.matches("^h"))
                printHistogram();
            else
                printHelp();
        }
        System.out.println("Bye!");
    }

    // region Printing methods

    /**
     * Prints instructions on how to use the app for the user
     */
    private static void printHelp() {
        System.out.println("(1) to read future items\n" +
                "(2) to read past items\n" +
                "(3) to read templates\n" +
                "(c)reate or (u)pdate, (d)elete or (s)tatus followed by id for items\n" +
                "(ct)reate or (ut)pdate or (dt)elete followed by id for templates\n" +
                "(ct)reate followed by id for creating an item from a template\n"+
                "(h)istogram for histogram of upcoming tasks\n"+
                "(q)uit or e(x)it to exit");
    }

    /**
     * Prints all items that have deadlines starting from today
     */
    private static void printFutureItems() {
        List<Item> futureItems = itemController.getItems(false);
        System.out.println("Future items:");
        for (Item item : futureItems){
            printItemInfo(item);
        }
    }

    /**
     * Prints all items that have deadlines in the past
     */
    private static void printPastItems() {
        List<Item> pastItems = itemController.getItems(true);
        System.out.println("Past items:");
        for (Item item : pastItems){
            printItemInfo(item);
        }
    }

    /**
     * Prints information about a given item
     * @param item Item whose information to print
     */
    private static void printItemInfo(Item item) {
        System.out.println(item.deadlineDate +
                " - Id :" + item.id +
                " - Description: " + item.description +
                " - Priority: " + item.priority.name() +
                " - Completed: " + item.isCompleted
        );
    }

    /**
     * Prints all templates
     */
    private static void printTemplates() {
        List<Template> templates = templateController.getTemplates();
        System.out.println("Templates:");
        for (Template template : templates){
            printTemplateInfo(template);
        }
    }

    /**
     * Prints information about a given template
     * @param template Template whose information to print
     */
    private static void printTemplateInfo(Template template) {
        System.out.println("Id: "+template.id+
                " - Name: "+template.name+
                " - Days from today: "+template.daysToAdd+
                " - Description: "+ template.description+
                " - Priority: "+ template.priority.name()
        );
    }

    /**
     * Prints a histogram about items coming in the future
     */
    private static void printHistogram() {
        List<Item> items = itemController.getItems(false);
        int[][] data = new int[8][3];
        for (Item item : items){
            // Eight buckets: 0,1,2,3,4,5,6,7+ days from now
            int bucket = (int) LocalDate.now().until(item.deadlineDate, ChronoUnit.DAYS);
            bucket = bucket > 7 ? 7 : bucket;
            int priority = item.priority.ordinal();
            data[bucket][priority]++;
        }
        for (int i = 0; i < 8; i++){
            if (i < 7)
                System.out.print(LocalDate.now().plusDays(i).toString()+ "  ");
            else
                System.out.print(LocalDate.now().plusDays(i).toString()+ "+ ");
            System.out.print("H".repeat(data[i][0]));
            System.out.print("M".repeat(data[i][1]));
            System.out.print("L".repeat(data[i][2])+"\n");
        }
    }
    // endregion

    // region Create, update and delete for items

    /**
     * UI and controller call for creating a new item
     */
    private static void createItem() {
        System.out.println("Creating new item...");
        Item item = generateItem("",2,0);
        boolean result = itemController.createItem(item.description, item.priority, item.deadlineDate);
        System.out.println("Item successfully created: "+result);
        printHelp();
    }

    /**
     * UI and controller call for creating an item from a template
     * @param id Id of the template to use for creating the item
     */
    private static void createItemFromTemplate(int id) {
        System.out.println("Creating new item from a template...");
        Template template = templateController.getTemplate(id);
        if (template == null){
            System.out.println("Template with id "+id+" was not found.");
        } else {
            Item item = generateItem(template.description, template.priority.ordinal()+1, template.daysToAdd);
            boolean result = itemController.createItem(item.description, item.priority, item.deadlineDate);
            System.out.println("Item successfully created: "+result);
        }
        printHelp();
    }

    /**
     * UI and controller call for updating an existing item
     * @param id Id of the item to update
     */
    private static void updateItem(int id) {
        System.out.println("Updating item...");
        Item item = itemController.getItem(id);
        if (item == null){
            System.out.println("Item with id "+id+" was not found.");
        } else {
            printItemInfo(item);
            Item newItem = generateItem(item.description, item.priority.ordinal()+1,
                    LocalDate.now().until(item.deadlineDate,ChronoUnit.DAYS));
            String isCompletedString = scanString("Is completed, (t)rue or (f)alse","");
            boolean isCompleted = false;
            if (isCompletedString.matches("^t"))
                isCompleted = true;
            else if (isCompletedString.matches("^f"))
                isCompleted = false;
            else
                System.out.println("Could not parse completion status, defaulted to false");
            boolean result = itemController.updateItem(id, newItem.description,
                    newItem.priority,newItem.deadlineDate, isCompleted);
            System.out.println("Item successfully updated: "+result);
        }
        printHelp();
    }

    /**
     * Toggles the completion status of the item between true and false
     * @param id Id of the item whose status to toggle
     */
    private static void toggleStatusOfItem(int id) {
        System.out.println("Toggling status of item...");
        Item item = itemController.getItem(id);
        if (item == null){
            System.out.println("Item with id "+id+" was not found.");
        } else {
            boolean result = itemController.toggleCompleted(id);
            System.out.println("Item status successfully toggled: "+result);
            Item updatedItem = itemController.getItem(id);
            printItemInfo(updatedItem);
        }
        printHelp();
    }

    /**
     * Controller call for deleting an existing item
     * @param id Id of the item to delete
     */
    private static void deleteItem(int id) {
        System.out.println("Deleting item...");
        Item item = itemController.getItem(id);
        if (item == null){
            System.out.println("Item with id "+id+" was not found.");
        } else {
            boolean result = itemController.deleteItem(id);
            System.out.println("Item was successfully deleted: "+result);
        }
        printHelp();
    }
    // endregion

    // region Create, update and delete for templates

    /**
     * UI and controller call for creating a new template
     */
    private static void createTemplate() {
        System.out.println("Creating new template...");
        Template template = generateTemplate(true, "", "", 2, 0);
        boolean result = templateController.createTemplate(template.name, template.description, template.priority,
                template.daysToAdd);
        System.out.println("Template successfully created: "+result);
        printHelp();
    }

    /**
     * UI and controller call for updating an existing template
     * @param id Id of the template to update
     */
    private static void updateTemplate(int id) {
        System.out.println("Updating template...");
        Template template = templateController.getTemplate(id);
        if (template == null){
            System.out.println("Template with id "+id+" was not found.");
        } else {
            printTemplateInfo(template);
            Template newTemplate = generateTemplate(true, template.name, template.description,
                    template.priority.ordinal()+1, template.daysToAdd);
            boolean result = templateController.updateTemplate(id, newTemplate.name, newTemplate.description,
                    newTemplate.priority, newTemplate.daysToAdd);
            System.out.println("Template successfully updated: "+result);
        }
        printHelp();
    }

    /**
     * Controller call for deleting an existing template
     * @param id Id of the template to delete
     */
    private static void deleteTemplate(int id){
        System.out.println("Deleting template...");
        Template template = templateController.getTemplate(id);
        if (template == null){
            System.out.println("Template with id "+id+" was not found.");
        } else {
            boolean result = templateController.deleteTemplate(id);
            System.out.println("Template was successfully deleted: "+result);
        }
        printHelp();
    }
    // endregion

    //region Helper methods

    /**
     * Helper method for UI for creating a new template or an item
     * @param askForName True when generating a template, False when generating an item
     * @param defaultName Value to which the name of the template will default to if none given by user
     * @param defaultDescription Value to which the priority of the item will default to if none given by user
     * @param defaultPriority Value to which the priority of the item will default to if none given by user
     * @param defaultDaysToAdd Value of days until the deadline of the item will default to if none given by the user
     * @return Template generated from the values given by user
     */
    private static Template generateTemplate(boolean askForName, String defaultName, String defaultDescription,
                                             int defaultPriority, long defaultDaysToAdd) {
        String name = null;
        if (askForName)
            name = scanString("Name", defaultName);
        String description = scanString("Description", defaultDescription);
        String priorityString = scanString("Priority, 1 = HIGH, 2 = MEDIUM, 3 = LOW",
                String.valueOf(defaultPriority));
        Priority priority = parsePriority(priorityString);
        int daysToAdd = (int) defaultDaysToAdd;
        String daysToAddString = scanString("Days from today as numbers, 0 = today, 1 = tomorrow etc",
                String.valueOf(defaultDaysToAdd));
        if (daysToAddString.matches("^\\d+$"))
            daysToAdd = Integer.valueOf(daysToAddString);
        else
            System.out.println("Could not parse days to add, defaulted to "+daysToAdd);
        return new Template (name, description, priority, daysToAdd);
    }

    /**
     * Helper method for UI for creating a new an item
     * @param defaultDescription Value to which the priority of the item will default to if none given by user
     * @param defaultPriority Value to which the priority of the item will default to if none given by user
     * @param defaultDaysToAdd Value of days until the deadline of the item will default to if none given by the user
     * @return Item generated from the values given by user
     */
    private static Item generateItem(String defaultDescription, int defaultPriority, long defaultDaysToAdd){
        Template template = generateTemplate(false, "", defaultDescription, defaultPriority,
                defaultDaysToAdd);
        return new Item(template.description, template.priority, LocalDate.now().plusDays(template.daysToAdd));
    }

    /**
     * Helper method for prompting string values from the user
     * @param userInstruction What the user will be shown before reading user input
     * @param defaultValue What the prompted value will default to, if any
     * @return The user input
     */
    private static String scanString(String userInstruction, String defaultValue){
        if (StringUtils.isEmpty(defaultValue))
            System.out.println(userInstruction+":");
        else
            System.out.println(userInstruction+", will default to "+defaultValue+":");
        String scanned = scanner.nextLine();
        return StringUtils.isEmpty(scanned) ?
                defaultValue :
                scanned;
    }

    /**
     * Parsing an enum value for Priority from a string
     * @param priorityString String given by the user
     * @return Parsed priority. If the input was invalid, returns Priority.LOW as default
     */
    private static Priority parsePriority(String priorityString){

        if (priorityString.matches("^1"))
            return Priority.HIGH;
        else if (priorityString.matches("^2"))
            return Priority.MEDIUM;
        else if (priorityString.matches("^3"))
            return Priority.LOW;

        System.out.println("Could not parse priority, defaulted to LOW");
        return Priority.LOW;
    }
    //endregion

    // region Creating demo data

    /**
     * Creates demo data in the form of semi-randomized items and predefined templates.
     * Useful when using an in-memory database or for seeding an empty database for the first time.
     * @param numberOfItems How many semi-randomized items to create.
     */
    private static void createDemoData(int numberOfItems) {

        String[] verbs = {
                "Review",
                "Design",
                "Code",
                "Test",
                "Develop",
                "Refactor",
                "Update",
                "Build",
                "Publish",
                "Investigate"
        };
        String[] objects = {
                "minimum viable product",
                "work from last week",
                "thing your boss just mentioned",
                "database scripts",
                "important functionality",
                "first sprint item",
                "demoable product"
        };

        Random random = new Random();
        List<Priority> priorities = Arrays.asList(Priority.values());
        for (int i = 0; i < numberOfItems; i++) {
            String description = verbs[random.nextInt(verbs.length)] + " the " + objects[random.nextInt(objects.length)];
            Priority priority = priorities.get(random.nextInt(priorities.size()));
            int daysToAdd = random.nextInt(10);
            LocalDate date;
            if (daysToAdd < 8)
                date = LocalDate.now().plusDays(daysToAdd);
            else
                date = LocalDate.now().minusDays(10 - daysToAdd);
            itemController.createItem(description, priority, date);
            if (random.nextInt(100) > 85)
                itemController.updateItem(i-1, description, priority,date,  true);
        }
        templateController.createTemplate("Today/High","DEADLINE TODAY!! ", Priority.HIGH, 0);
        templateController.createTemplate("Tomorrow/Medium", "Demo tomorrow", Priority.MEDIUM, 1);
        templateController.createTemplate("Upcoming", "Vacation",Priority.LOW, 5);
    }
    // endregion

}
