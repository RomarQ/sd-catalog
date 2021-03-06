package client;

import shared.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static shared.helper.getIp;


public class Client {

    // RMI
    static Registry         registry;
    static CatalogRemote    catalog;

    public static ClientInfo clientInfo;

    /**
     * Lists all categories registered on Catalog
     */
    public static void listCategories() {

        try {

            BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

            ArrayList<Category> categories = catalog.getCategories();

            while ( true ) {

                if ( categories.size() == 0 )
                    System.out.print("\n\nNo Categories available :(\n\n");
                else {
                    System.out.print(
                            " _________________  \n" +
                            "|  Categories       \n" +
                            "|-----------------  \n" +
                            "| Id   | Category   \n" +
                            "|-----------------  \n"
                    );

                    for (Category c : categories ) {
                        System.out.print("| " + c.getId() + " | " + c.getCategoryName() + "\n");
                    }

                    System.out.print("|_________________\n");
                }

                System.out.print(
                        " ________                  \n" +
                        "|  MENU                    \n" +
                        "|--------                  \n" +
                        "| 1. Select a Category     \n" +
                        "| 2. Request a category    \n" +
                        "| 0. Exit Menu             \n" +
                        "|______                    \n"
                );

                int option = Integer.parseInt(input.readLine());

                switch (option) {
                    case 1: selectCategory();   break;
                    case 2: requestCategory();  break;
                    case 0: return;
                    default: break;
                }

            }

        } catch ( RemoteException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts a dialog to request a category that doesn't currently exists, also registers a callback
     * to notify user when category becomes available
     */
    public static void requestCategory () {

        BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

        System.out.print("Category Name? ");

        try {

            String categoryName = input.readLine();

            ResponseTypes response =
                    catalog.addCategoryRequest( clientInfo.getEmail() , categoryName );

            if ( response == ResponseTypes.CATEGORY_REQUEST_ACCEPTED )
                System.out.print("\n\nCategory request was registered, you will be notified once it is available!\n\n");
            else
                System.out.print("\n\nCategory request was rejected, this usually means that category is already available " +
                                 "or you already made this request.\n\n");

        } catch ( SocketException e ) {
            System.out.print("Couldn't get your IP");
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    /**
     * Asks user to select a category Id and then a sellerId to start a connection
     * via socket with the seller [Client -> Seller](Both Clients)
     */
    public static void selectCategory() {

        // refresh connection - Needed if server reconnected during client session
        connectToCatalog();

        try {

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Insert category ID\n");
            int categoryId = Integer.parseInt(input.readLine());

            ArrayList<User> sellers = catalog.getCategorySellers(categoryId);

            // sellerId is used to Identify a seller on the menu below
            int sellerId = 0;

            if (sellers.size() == 0)
                System.out.println("This Category doesn't have Sellers for now!");
            else if (sellers.size() == 1 && sellers.get(0).getEmail().equalsIgnoreCase(clientInfo.getEmail()))
                System.out.println("This Category only has a seller and is you!");
            else {
                System.out.print(
                        " _______________________________________\n" +
                        "|  Sellers                              \n" +
                        "|---------------------------------------\n" +
                        "| Id   | Ip                | Port       \n" +
                        "|---------------------------------------\n"
                );

                for (User seller : sellers) {
                    if ( !seller.getEmail().equalsIgnoreCase(clientInfo.getEmail()))
                        System.out.print("| " + ++sellerId + "    | " + seller.getIp() + "    | " + seller.getPort() + "\n");
                }

                System.out.print("|_______________________________________\n");

                System.out.print("Insert Seller ID to verify his products\n");
                sellerId = Integer.parseInt(input.readLine());

                sellerId--;
                if (sellerId >= 0 && sellerId < sellers.size())
                    connectToSeller( sellers.get(sellerId) , categoryId );
                else
                    System.out.print("\nSeller ID invalid, Try again :(\n");

            }
        } catch (EOFException e) {
            System.out.print("Input invalid, Try again :(\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to seller via socket to get his products list by category and also to ask for a contact
     * @param seller
     */
    public static void connectToSeller( User seller , int categoryId ) {

        BufferedReader input = new BufferedReader ( new InputStreamReader( System.in ) );

        try (
                Socket socket = new Socket(seller.getIp(), seller.getPort());
                ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
                ObjectInputStream ois = new ObjectInputStream( socket.getInputStream() )
        ) {
            String categoryName = catalog.getCategoryName(categoryId);

            // Waiting for CONNECTION_ESTABLISHED
            ois.readObject();

            oos.writeObject(ResponseTypes.PRODUCTS_REQUEST);
            oos.flush();

            oos.writeObject(categoryName);
            oos.flush();

            ArrayList<Product> products = (ArrayList<Product>) ois.readObject();

            // shows a menu list with all products and their prices
            listProducts( products , false );

            System.out.print("\nWant the seller contact? [Y/N]");

            if ( input.readLine().equalsIgnoreCase("Y") ) {
                oos.writeObject(ResponseTypes.CONTACT_REQUEST);
                oos.flush();

                System.out.print("\n"+ois.readObject()+"\n\n");
            }


        } catch ( ConnectException e ) {
            System.out.print("\n\nSeller is offline, Try again later. \n\n");
        } catch ( IOException e ) {
            System.out.print("\n\nSeller went offline, Try again later. \n\n");
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }


    }

    /**
     * Shows a menu list with all products and their respective prices
     * @param products ArrayList<Product>
     * @param showCategory boolean that tells to show category or not
     */
    public static void listProducts( ArrayList<Product> products , boolean showCategory ) {

        if ( products.size() == 0 )
            System.out.print("\nNo Products to display!\n");
        else {
            if ( showCategory ) {
                System.out.print(
                    " ____________________________________________________________\n" +
                    "|  Products                                                  \n" +
                    "|------------------------------------------------------------\n" +
                    "| Id      Name                 Category           Price      \n" +
                    "|------------------------------------------------------------\n"
                );
                int i = 1;
                for (Product p : products) {
                    System.out.print("| " + p.getId() + "  " + p.getName()  + "  " + p.getCategory() + "  " + p.getPrice() + "€\n");
                }

                System.out.print("|_________________________________________________________\n");

                return;
            }

            System.out.print(
                    " _______________________________________\n" +
                    "|  Products                             \n" +
                    "|---------------------------------------\n" +
                    "| Name                      Price       \n" +
                    "|---------------------------------------\n"
            );

            for (Product p : products) {
                System.out.print("| " + p.getName()  + " | " + p.getPrice() + "€\n");
            }

            System.out.print("|_______________________________________\n");

        }

    }

    /**
     * Prompts a dialog to insert a new product
     */
    public static void newProduct() {

        // refresh connection - Needed if server reconnected during client session
        connectToCatalog();

        BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

        while ( true ) {

            try {

                System.out.print("Insert product name\n");
                String productName = input.readLine();

                if ( productName.length() < 2 ) {
                    System.out.print("\nName is too small, Try again! :(\n\n");
                    continue;
                }

                System.out.print("Now insert the category of this product\n");
                String category = input.readLine();

                if ( category.length() < 2 ) {
                    System.out.print("\nCategory is too small, Try again! :(\n\n");
                    continue;
                }

                System.out.print("To finish, insert the price of this product\n");
                Double price = Double.parseDouble(input.readLine());

                if ( price <= 0 ) {
                    System.out.print("\nProducts need to have a price, Try again! :(\n\n");
                    continue;
                }


                if ( !clientInfo.productExist( productName , category ) ) {
                    // Adds this client as seller of the given category, if category doesn't exist then also adds it
                    catalog.addCategorySeller(clientInfo.getEmail(), category);

                    clientInfo.addProduct( new Product( clientInfo.generateProductId() , productName, category, price ) );
                }
                else {
                    System.out.print("\n\n You already have a product with this name, do you want to update its price? [Y/N]\n");
                    if ( input.readLine().equalsIgnoreCase("Y") )
                        clientInfo.updateProductPrice(productName, category, price);
                }

                saveClientFile();
                break;

            } catch ( NumberFormatException e ) {
                System.out.print("\nThe given price is invalid, Try again! :(\n\n");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Starts a dialog to remove a product from this user store
     */
    private static void removeProduct() {

        BufferedReader input = new BufferedReader( new InputStreamReader( System.in ));

        ArrayList<Product> products = clientInfo.getAllProducts();

        // Shows a menu list with all products, their category and prices
        listProducts( products , true );

        if ( products.size() > 0 ) {

            try {

                System.out.print("Insert product Id\n");
                int productId = Integer.parseInt(input.readLine());

                if ( productId > 0 && productId < clientInfo.getCurrentProductId())
                    clientInfo.removeProductById( productId );

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Lookup the Remote object Catalog
     */
    private static void connectToCatalog() {

        try {

            registry = LocateRegistry.getRegistry(config.serverAddress, config.RMIServerPort);
            catalog = (CatalogRemote) registry.lookup("Catalog");

        } catch ( Exception e) {
            System.out.print("\n\nServer is not responding or RMI service is offline...\n");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the port that user inserted is available to use.
     *
     * @param port
     * @return
     *          true if available
     */
    private static boolean isPortAvailable( int port ) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    /**
     * Lists all client objects stored on this computer, those objects can
     * be selected by the user and then starts a client instance by reading a
     * stored client object that user selected.
     *
     * @return
     *          true if successfully connected
     *          false if something went wrong
     */
    private static boolean connect() {

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            //-------------------------------------------------------------------
            // List all client object files on this computer
            File directory = new File("src/client/storage");

            File[] files = directory.listFiles();

            // Go back if there isn't any client object
            if ( files.length < 1 ) {
                System.out.print("\n\nNo Clients were found! :( \n\n");
                return false;
            }

            System.out.print("\n\nWhich one?\n");
            int i = 1;
            for (File f : files)
                if (f.isFile())
                    System.out.print("\nId: " + i++ + " - Client: " + f.getName());

            System.out.print("\n\nId: ");
            i = Integer.parseInt(input.readLine());

            // Go back if user inserted a invalid index
            if (i < 1 || i > files.length)
                return false;

            FileInputStream fis = new FileInputStream(files[i - 1]);
            ObjectInputStream ois = new ObjectInputStream(fis);

            clientInfo = (ClientInfo) ois.readObject();


            ois.close();
            fis.close();

            return subscribeToCatalog() == ResponseTypes.SUBSCRIPTION_ACCEPTED ? true : false;

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * New user registration
     */
    private static boolean signup() {

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {

            final Pattern mailFormat = Pattern.compile("^(([^<>()\\[\\]\\.,;:\\s@\\\"]+(\\.[^<>()\\[\\]\\.,;:\\s@\\\"]+)*)|(\\\".+\\\"))@(([^<>()\\.,;\\s@\\\"]+\\.{0,1})+[^<>()\\.,;:\\s@\\\"]{2,})$");

            //-------------------------------------------------------------------
            // Ask Email

            System.out.print("Insert your email\n");
            String email = input.readLine();

            while ( !mailFormat.matcher(email).matches() || catalog.userExists(email) ) {
                System.out.print("\nEmail is invalid or already exists, Try again! :(\n\n");
                System.out.print("Insert your email\n");
                email = input.readLine();
            }

            //-------------------------------------------------------------------
            // Ask phone number

            System.out.print("Now insert your phone number\n");
            long phone = Long.parseLong(input.readLine());

            while ( Long.toString(phone).length() < 9 ) {
                System.out.print("\nPhone number is invalid, Try again! :(\n\n");
                System.out.print("Now insert your phone number\n");
                phone = Long.parseLong(input.readLine());
            }

            //-------------------------------------------------------------------
            // Ask for a port to listen

            System.out.print("To finish, insert a port to allow other clients to contact you\n");
            int port = Integer.parseInt(input.readLine());

            while ( !isPortAvailable(port) ) {
                System.out.print("\nPort is already being used, Try again! :(\n\n");
                System.out.print("To finish, insert a port to allow other clients to contact you\n");
                port = Integer.parseInt(input.readLine());
            }

            clientInfo = new ClientInfo( email , Long.toString(phone) , port );

            saveClientFile();

            catalog.createUser(
                    clientInfo.getEmail(),
                    clientInfo.getClientIp(),
                    clientInfo.getPort(),
                    clientInfo
            );

            System.out.print("\n\n" + email + " you are now registered!\n\n");

            return subscribeToCatalog() == ResponseTypes.SUBSCRIPTION_ACCEPTED ? true : false;

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Subscribes to catalog sending a client interface, the email and current IP of the client.
     * If already subscribed, then just updates the proxy of the client interface.
     *
     * @return
     *          ResponseTypes.SUBSCRIPTION_ACCEPTED
     *          ResponseTypes.SUBSCRIPTION_REJECTED
     */
    private static ResponseTypes subscribeToCatalog() {

        try {
            return catalog.updateSubscription( clientInfo , clientInfo.getEmail() , getIp() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return ResponseTypes.SUBSCRIPTION_REJECTED;
    }

    /**
     * Saves ClientInfo(client object) in a file, so next time client can start session by selecting his
     * object file with all his information.
     */
    private static void saveClientFile() {

        try (
            FileOutputStream fos = new FileOutputStream(new File("src/client/storage/"+clientInfo.getEmail()+".ser"));
            ObjectOutputStream oos = new ObjectOutputStream( fos );
        ) {
            oos.writeObject(clientInfo);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public static void main( String [] args ) {

        System.setSecurityManager(new SecurityManager());

        // Connects to RMI Server "Catalog"
        connectToCatalog();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while ( true ) {

            try {

                System.out.print("\n\nDo you already have an Account? [Y/N] \n");

                if ( input.readLine().equalsIgnoreCase("Y") ) {
                    if (connect())
                        break;
                    else
                        continue;
                }

                if ( !signup() ) {
                    System.out.print("\n\nSomething went wrong, Try again!\n");
                    continue;
                }

                break;

            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }

        // Starts Client
        ClientThread clientThread = new ClientThread( clientInfo.getPort() );
        clientThread.startClientServer();

        while ( true ) {

            try {

                System.out.print(
                    " ________           \n" +
                    "|  MENU             \n" +
                    "|--------           \n" +
                    "| 1. Add Product    \n" +
                    "| 2. Remove Product \n" +
                    "| 3. Buy Product    \n" +
                    "| 0. Exit           \n" +
                    "|______             \n"
                );

                int option = Integer.parseInt(input.readLine());

                switch (option) {
                    case 1: newProduct();       break;
                    case 2: removeProduct();     break;
                    case 3: listCategories();   break;
                    case 0: clientThread.stopServer();
                            System.exit(0);
                }

            } catch ( NumberFormatException e ) {
                System.err.println("Invalid Input, Try again!\n");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }

    }
}
