package server;

import shared.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Catalog extends UnicastRemoteObject implements CatalogRemote {

    // ----------------------------------------------
    // ANSI escape codes to style console output
    // ----------------------------------------------

        // green color
        final String ANSI_GREEN = "\u001B[32m";
        // red color
        final String ANSI_RED = "\u001B[31m";
        // reset color
        final String ANSI_RESET = "\u001B[0m";

    // ----------------------------------------------
    // END
    // ----------------------------------------------

    // ----------------------------------------------
    // Catalog Objects, will store all important data
    // from user interactions
    // ----------------------------------------------

        // Clients is a collection with all user information
        private ArrayList<User> clients = getClientsData();
        private Integer currentClientId = getCurrentClientId();

        // Categories is a collection with all category names and their respective ID's.
        private ArrayList<Category> categories = getCategoriesData();
        private Integer currentCategoryId = getCurrentCategoryId();

        // This collection was created just for a matter of efficiency
        // CategorySeller is a collection that links sellers to their respective categories and vice versa.
        private ArrayList<CategorySeller> categorySellers = getCategorySellersData();

        // List with all category requests from clients
        private ArrayList<CategoryRequest> categoryRequests = getCategoryRequestsData();
        // Used when category requested is available but client was offline and couldn't be notified
        private ArrayList<CategoryRequest> pendingCategoryRequests = new ArrayList<CategoryRequest>();

    // ----------------------------------------------
    // END
    // ----------------------------------------------

    // Constructor
    public Catalog() throws RemoteException {
        super();
    }

    /**
     * Verifies if a given user exists
     *
     * @param email
     * @return
     */
    public boolean userExists( String email ) {
        for ( User user : clients )
            if ( user.getEmail().equalsIgnoreCase(email))
                return true;

        return false;
    }


    /**
     * Updates Client RMI instance when he re-connects to the server
     *
     * @param subscription
     * @param email
     * @param ip
     * @return
     */
    public synchronized ResponseTypes updateSubscription( ClientInterface subscription , String email , String ip ) {

        for ( User user : clients )
            if ( user.getEmail().equalsIgnoreCase( email ) ) {
                user.updateClientInterface(subscription);
                user.updateIp(ip);
                verifyCategoryRequestsByUser( user );
                return ResponseTypes.SUBSCRIPTION_ACCEPTED;
            }

        return ResponseTypes.SUBSCRIPTION_REJECTED;
    }

    /**
     * This method generates a new unique id
     *
     * @param idType can be categoryId [1] or clientId [2]
     * @return
     *          int - generated id
     */
    private synchronized int generateUniqueId( int idType ) {

        if ( idType == 1) {
            currentCategoryId = new Integer(currentCategoryId.intValue() + 1);
            return currentCategoryId.intValue();
        }
        else if ( idType == 2 ) {
            currentClientId = new Integer(currentClientId.intValue() + 1);
            return currentClientId.intValue();
        }

        return -1;
    }

    /**
     * This method allows new clients to register on catalog
     *
     * @param email
     * @param ip
     * @param port
     * @param clientInterface   Proxy that will allow catalog to answer callbacks coming from client
     */
    public void createUser ( String email , String ip , int port , ClientInterface clientInterface ) {

        // Synchronized segment since we are generating unique ID's for clients
        synchronized ( currentClientId ) {
            int id =  generateUniqueId( 2 );

            synchronized ( clients ) {
                clients.add(
                        new User(
                                id,
                                email,
                                ip,
                                port,
                                clientInterface
                        )
                );
            }

            System.out.print(ANSI_GREEN + "\n\nNew Client: " + email + "\n\n" + ANSI_RESET);
            saveClients();
        }
    }

    /**
     * Reads Clients file and returns its content if file exists
     * @return
     *          ArrayList<User>
     */
    public ArrayList<User> getClientsData() {

        try (
            FileInputStream fis = new FileInputStream(new File("src/server/storage/clients.ser"));

            ObjectInputStream ois = new ObjectInputStream(fis)
        ) {

            return  ( ArrayList<User> ) ois.readObject();

        } catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "\nNo Data from Clients File, starting empty object!\n" + ANSI_RESET);
        }

        return new ArrayList<>();
    }

    /**
     * Gets the last client id given to a client
     * @return
     *          Integer
     */
    private Integer getCurrentClientId() {

        try (
            FileInputStream fis = new FileInputStream( new File("src/server/storage/currentClientId.ser"));

            ObjectInputStream ois = new ObjectInputStream( fis )
        ) {

            return ( Integer ) ois.readObject();

        } catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "No Data from currentClientId File, starting empty object!\n" + ANSI_RESET);
        }

        return new Integer(0);
    }

    /**
     * Reads Categories file and returns its content if file exists
     * @return
     *          ArrayList<Category>
     */
    public ArrayList<Category> getCategoriesData() {

        try (
            FileInputStream fis = new FileInputStream(new File("src/server/storage/categories.ser"));

            ObjectInputStream ois = new ObjectInputStream(fis)
        ) {

            return  ( ArrayList<Category> ) ois.readObject();

        } catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "No Data from Categories File, starting empty object!\n" + ANSI_RESET);
        }

        return new ArrayList<>();
    }

    /**
     * Gets the last category id given
     * @return
     *          Integer
     */
    private Integer getCurrentCategoryId() {

        try (
                FileInputStream fis = new FileInputStream( new File("src/server/storage/currentCategoryId.ser"));

                ObjectInputStream ois = new ObjectInputStream( fis )
        ) {

            return ( Integer ) ois.readObject();

        } catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "No Data from currentCategoryId File, starting empty object!\n" + ANSI_RESET);
        }

        return new Integer(0);
    }

    /**
     * Reads CategorySellers file and returns its content if file exists
     * @return
     *          ArrayList<CategorySeller>
     */
    public ArrayList<CategorySeller> getCategorySellersData() {

        try (
            FileInputStream fis = new FileInputStream(new File("src/server/storage/categorySellers.ser"));

            ObjectInputStream ois = new ObjectInputStream(fis)
        ) {

            return  ( ArrayList<CategorySeller> ) ois.readObject();

        }  catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "No Data from CategorySellers File, starting empty object!\n" + ANSI_RESET);
        }

        return new ArrayList<>();
    }

    /**
     * Reads CategoryRequests file and returns its content if file exists
     * @return
     *          ArrayList<CategoryRequest>
     */
    public ArrayList<CategoryRequest> getCategoryRequestsData() {

        try (
            FileInputStream fis = new FileInputStream(new File("src/server/storage/categoryRequests.ser"));

            ObjectInputStream ois = new ObjectInputStream(fis)
        ) {

            return  ( ArrayList<CategoryRequest> ) ois.readObject();

        } catch ( Exception e ) {
            System.out.print(ANSI_GREEN + "No Data from CategoryRequests File, starting empty object!\n\n" + ANSI_RESET);
        }

        return new ArrayList<>();
    }


    /**
     * Saves Clients info in two object files, clients.ser and currentClientId.ser.
     *
     * @return
     *          true on success
     *          false on failure
     */
    public synchronized boolean saveClients() {

        try (
            FileOutputStream fos =
                    new FileOutputStream( new File("src/server/storage/clients.ser"));
            FileOutputStream fosIds =
                    new FileOutputStream( new File("src/server/storage/currentClientId.ser"));

            ObjectOutputStream oos    = new ObjectOutputStream( fos    );
            ObjectOutputStream oosIds = new ObjectOutputStream( fosIds )
        ) {

            oos.writeObject     ( clients         );
            oosIds.writeObject  ( currentClientId );

        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves Categories info in two object files, categories.ser and currentCategoryId.ser.
     *
     * @return
     *          true on success
     *          false on failure
     */
    public synchronized boolean saveCategories() {

        try (
            FileOutputStream fos =
                    new FileOutputStream( new File("src/server/storage/categories.ser"));
            FileOutputStream fosIds =
                    new FileOutputStream( new File("src/server/storage/currentCategoryId.ser"));

            ObjectOutputStream oos    = new ObjectOutputStream( fos    );
            ObjectOutputStream oosIds = new ObjectOutputStream( fosIds )
        ) {

            oos.writeObject     ( categories         );
            oosIds.writeObject  ( currentCategoryId );

        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves CategorySellers info in two object files, categories.ser and currentCategoryId.ser.
     *
     * @return
     *          true on success
     *          false on failure
     */
    public synchronized boolean saveCategorySellers() {

        try (
            FileOutputStream fos =
                    new FileOutputStream( new File("src/server/storage/categorySellers.ser"));

            ObjectOutputStream oos = new ObjectOutputStream( fos )
        ) {

            oos.writeObject ( categorySellers );

        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves CategoryRequests info in two object files, categories.ser and currentCategoryId.ser.
     *
     * @return
     *          true on success
     *          false on failure
     */
    public synchronized boolean saveCategoryRequests() {

        try (
            FileOutputStream fos =
                    new FileOutputStream( new File("src/server/storage/categoryRequests.ser"));

            ObjectOutputStream oos = new ObjectOutputStream( fos )
        ) {

            oos.writeObject ( categoryRequests );

        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Adds a Category request to the pending requests for the purpose of notifying the client when that category has sellers.
     * @param email Email of the client that requested the category
     * @param categoryName Category name of the category requested
     * @return
     *          ResponseTypes.CATEGORY_REQUEST_REJECTED if category already has sellers,
     *          ResponseTypes.CATEGORY_REQUEST_ACCEPTED that means request was successful
     */
    public ResponseTypes addCategoryRequest( String email , String categoryName ) {

        Category category = null ;

        // Verifies the request has already been made, if yes then reject this new request
        for ( CategoryRequest cr : categoryRequests ) {
            if (cr.getCategoryName().equalsIgnoreCase(categoryName) && cr.getClientEmail().equalsIgnoreCase(email))
                return ResponseTypes.CATEGORY_REQUEST_REJECTED;
        }
        // Gets the Category object if there is a category with the same name as the category requested
        for ( Category c : categories )
            if ( c.getCategoryName().equalsIgnoreCase(categoryName) )
                category = c;

        // If category exists, verifies if category has already sellers
        if ( category != null )
            for ( CategorySeller cs : categorySellers )
                if ( cs.getCategoryId() == category.getId() )
                    return ResponseTypes.CATEGORY_REQUEST_REJECTED;

        // Add request since at this point we know that category or doesn't exist yet or has no sellers
        synchronized (categoryRequests) {
            categoryRequests.add(new CategoryRequest( categoryName , email ));
        }

        // Save new data into file
        saveCategoryRequests();

        return ResponseTypes.CATEGORY_REQUEST_ACCEPTED;
    }

    /**
     * Notifies the client that requested a category that is now available
     * @param email Email of the client that requested the category
     * @param categoryName Category name of the category requested
     * @return a String saying that category is now available.
     */
    private boolean notifyClient(  String email , String categoryName ) {

        try {

            ClientInterface ci = getClientInterface( email );

            if ( ci != null )
                ci.notifyClient( ANSI_GREEN + " Category " + categoryName + " is available! "+ ANSI_RESET );

            return true;

        } catch ( RemoteException e ) {
            System.err.println("Client not listening!");
            return false;
        }

    }

    public ClientInterface getClientInterface( String email ) {

        for ( User u : clients )
            if ( u.getEmail().equalsIgnoreCase(email) )
                return u.getClientInterface();

        return null;
    }

    /**
     * Verifies which categories requested are now available and notifies the user
     *
     * @param user
     */
    private void verifyCategoryRequestsByUser( User user ) {

        ArrayList<CategoryRequest> requestsToRemove = new ArrayList<>();

        for ( CategoryRequest cr : pendingCategoryRequests )
            if (cr.getClientEmail().equalsIgnoreCase(user.getEmail())) {
                if ( notifyClient( cr.getClientEmail() , cr.getCategoryName() ) ) {
                    requestsToRemove.add(cr);
                }
            }

        synchronized (pendingCategoryRequests) {
            pendingCategoryRequests.removeAll(requestsToRemove);
        }
    }

    /**
     * Verifies which users requested a category and notifies them
     *
     * @param categoryName
     */
    private void verifyCategoryRequestsByCategory( String categoryName ) {

        ArrayList<CategoryRequest> requestsToRemove = new ArrayList<>();

        for (CategoryRequest cr : categoryRequests)
            if (cr.getCategoryName().equalsIgnoreCase(categoryName)) {
                if ( !notifyClient( cr.getClientEmail() , categoryName ) ) {
                    pendingCategoryRequests.add(new CategoryRequest( cr.getCategoryName() , cr.getClientEmail() ));
                }
                requestsToRemove.add(cr);
            }

        synchronized (categoryRequests) {
            categoryRequests.removeAll(requestsToRemove);
        }
    }

    /**
     * Makes a relation between a seller and a category, if category doesn't exist then also adds a new category
     * @param email
     * @param categoryName
     */
    public synchronized void addCategorySeller( String email , String categoryName ) {

        // Get seller data
        User seller = getSeller( email );

        // Verifying if category already exists on the catalog, if doesn't exist then add it to the catalog
        Category category = getCategory( categoryName );
        if( category == null ) {
            // Category doesn't exits, so lets add it
            synchronized ( currentCategoryId ){

                int id = generateUniqueId(1);

                synchronized ( categories ) {
                    category = new Category(id, categoryName);
                    categories.add(category);
                }
            }

            // Checks if any user requested this category, and notify them if so
            verifyCategoryRequestsByCategory( categoryName );
        }

        // Verifying if seller is already selling this category
        CategorySeller categorySeller = getCategorySeller( seller.getId() , category.getId() );
        if( categorySeller == null )
            // Seller doesn't sell this category, so lets add it
            synchronized ( categorySellers ) {
                categorySellers.add(new CategorySeller(seller.getId(), category.getId()));
            }

        // Save new data into file
        saveCategories();
        saveCategorySellers();
    }


    // Category Stuff

    public ArrayList<Category> getCategories() {

        ArrayList<Category> categoryList = new ArrayList<>();

        for ( Category c : categories ) {
            categoryList.add(new Category(c));
        }

        return categoryList;
    }

    public Category getCategory( String categoryName ) {

        // Looks for a category with name === @categoryName
        for ( Category c : categories ) {
            if ( c.getCategoryName().equals(categoryName) )
                return c;
        }

        // return null if doesn't exist
        return null;
    }

    /**
     * Looks for a category with categoryId and returns its name
     * @param categoryId
     * @return
     *          String CategoryName
     *          NULL if category doesn't exist
     */
    public String getCategoryName( int categoryId ) {

        for ( Category c : categories )
            if ( c.getId() == categoryId )
                return c.getCategoryName();

        return null;
    }

    // Seller stuff

    public User getSeller( String email ) {

        for ( User user : clients )
            if ( user.getEmail().equalsIgnoreCase(email) )
                return user;

        // return null if doesn't exist
        return null;
    }

    public User getSellerById( int id ) {

        for ( User user : clients ) {
            if ( user.getId() == id )
                return user;
        }

        // return null if doesn't exist
        return null;
    }

    // CategorySeller stuff

    public ArrayList<User> getCategorySellers( int categoryId ) {

        ArrayList<User> sellersList = new ArrayList<>();

        for ( CategorySeller cs : categorySellers ) {
            if ( cs.getCategoryId() == categoryId ) {
                User user = getSellerById( cs.getSellerId() );
                if ( user != null )
                    sellersList.add(new User(user));
            }
        }

        return sellersList;
    }

    public CategorySeller getCategorySeller( int sellerId , int categoryId ) {

        for ( CategorySeller cs : categorySellers )
            if ( cs.getSellerId() == sellerId && cs.getCategoryId() == categoryId )
                return cs;

        // return null if doesn't exist
        return null;
    }

}
