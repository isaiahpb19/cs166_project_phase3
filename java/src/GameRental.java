/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Date;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class GameRental {

   // reference to physical database connection.
   private Connection _connection = null;
   String loginUser = "";

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of GameRental store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public GameRental(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done\n");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end GameRental

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            GameRental.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      GameRental esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the GameRental object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new GameRental (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
               esql.loginUser = authorisedUser;
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Catalog");
                System.out.println("4. Place Rental Order");
                System.out.println("5. View Full Rental Order History");
                System.out.println("6. View Past 5 Rental Orders");
                System.out.println("7. View Rental Order Information");
                System.out.println("8. View Tracking Information");

                //the following functionalities basically used by employees & managers
                System.out.println("9. Update Tracking Information");

                //the following functionalities basically used by managers
                System.out.println("10. Update Catalog");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewCatalog(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewTrackingInfo(esql); break;
                   case 9: updateTrackingInfo(esql); break;
                   case 10: updateCatalog(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "*                                                     *\n" +
         "*                       RE:Game      	              *\n" + 
         "*          By: Sunny Atalig & Isaiah Bernardino       *\n" +
         "*                                                     *\n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            System.out.println("");
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(GameRental esql){
      Scanner reader = new Scanner(System.in);  
      String input;
      String find_name;
      String login;
      String phone;
      String pword;
      // public int executeQuery (String query) throws SQLException

      //get user name
      do{
         System.out.print("Please create a user_name (use only characters or numbers):\n");
         input = reader.next();
         find_name = "SELECT * FROM USERS WHERE login = '" +input+"';";

         try {
            if(!input.matches("[0-9a-zA-Z]+")) {
               System.out.print("Use only characters or numbers!\n");
            }
            else if(esql.executeQuery(find_name)>0) {
               System.out.print("This username is already used.\n");
            }
            else {
               break;
            }
         } catch (Exception e) {
            System.out.print("Something went wrong.\n");
         }
      } while(true);
      login = input;

      //get phone number
      do {
         System.out.print("Please add a phone number. Use the format +<country code>-xxx-xxx-xxxx.\n");
         input = reader.next();
         if(!input.matches("\\+[0-9]{1,5}\\-[0-9]{3}\\-[0-9]{3}\\-[0-9]{4}")) {
            System.out.print("Input does not match format!\n");
         }
         else{
            break;
         }
      }while(true);
      phone = input;

      //get pass word
      String user_password;
      do {
         do {
            System.out.print("Please enter a password. It must have the following:\n");
            System.out.print("1. Password must be at least 15 and at most 30 characters.\n");
            System.out.print("2. Password must contain only letters and numbers.\n");
            System.out.print("3. Password must contain at least one lower case letter, one upper case letter, and a digit.\n");
            System.out.print("Enter password: ");
            input = reader.next();
         }while(!check_password(input));
         user_password = new String(input);
         System.out.print("Please re-enter password: ");
         input = reader.next();
         if(!input.equals(user_password)) {
            System.out.print("Passwords do not match!\n");
         }
         else {
            break;
         }

      } while(true);
      pword = input;

      try {
         String update = "INSERT INTO Users VALUES('"+login+"','"+pword+"','customer','','"+phone+"',0);";
         esql.executeUpdate(update);
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
         return;
      }

      System.out.print("Account created. Login using your username and password.\n");
      // reader.close();

   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(GameRental esql){
      Scanner reader = new Scanner(System.in);  
      String username;
      String password;
      System.out.print("Enter username: ");
      username = reader.next();
      System.out.print("Enter password: ");
      password = reader.next();
      
      String sql = "SELECT * FROM USERS WHERE login = '" +username+"' AND password = '"+password+"';";
      try {
         if ( esql.executeQuery(sql) > 0) {
            System.out.print("Log In Success.\n\n");
            return username;
         }
         else {
            System.out.print("Incorrect login or password\n");
            return null;
         }
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
         return null;
      }
      


      // return null;
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(GameRental esql) {
      String sql = "SELECT phoneNum, numOverDueGames, favGames FROM Users WHERE login = '"+ esql.loginUser+"';";
      List<List<String>> items;
      try {
         items = esql.executeQueryAndReturnResult(sql);
         if (items.size() > 1) throw new Exception("uh oh");
      } catch (Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }
      for(int i = 0; i < items.get(0).get(2).length()+13; i++) System.out.print("-");
      System.out.println("\n          Profile\n");
      System.out.println(new String("* Phone Number: "+items.get(0).get(0)));
      System.out.println(new String("* Number of Overdue Games: "+items.get(0).get(1)));
      System.out.println(new String("* Favourite Games: "+items.get(0).get(2)));
      for(int i = 0; i < items.get(0).get(2).length()+13; i++) System.out.print("-");
      System.out.println("\n");
   }
   public static void updateProfile(GameRental esql) {
      System.out.println("------------------------------------------\n");
      System.out.println("Choose which entry to update:");
      System.out.println("1. Phone number");
      System.out.println("2. Favourite Games");
      System.out.println("3. Password");
      int input = readChoice();

      switch(input) {
         case 1:
            change_phone(esql);
            System.out.println("------------------------------------------\n");
            break;
         case 2:
            change_fav_games(esql);
            System.out.println("------------------------------------------\n");
            break;
         case 3:
            change_password(esql);
            System.out.println("------------------------------------------\n");
            break;
         default:
            System.out.println("Something went wrong.\n");
      }
   }
   
   public static void viewCatalog(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String genre;
      String min_price;
      String max_price;
      Boolean sort;

      String sql_genre;
      String sql_min_price;
      String sql_max_price;
      String sql_sort;

      System.out.println("-------------------------------------------------------------\n");
      System.out.println("Filter game by genre (press enter for no filter):");
      genre = reader.nextLine();
      System.out.println("Filter game by minimum price (press enter for no filter):");
      min_price = reader.nextLine();
      System.out.println("Filter game by maximum price (press enter for no filter):");
      max_price = reader.nextLine();
      System.out.println("Sort by:");
      System.out.println("0. Increasing Price");
      System.out.println("1. Decreasing Price");
      sort = (readChoice() != 0);

      sql_genre = genre.isEmpty() ? "TRUE" : "genre = '"+genre+"'";
      sql_min_price = min_price.isEmpty() ? "TRUE" : "price >= '"+min_price+"'";
      sql_max_price = max_price.isEmpty() ? "TRUE" : "price <= '"+max_price+"'";
      sql_sort = sort ? "ORDER BY price DESC" : "ORDER BY price ASC";

      String sql = "SELECT * FROM Catalog WHERE " + sql_genre + " AND " + sql_min_price + " AND " +sql_max_price + " " + sql_sort + ";";

      try {
         esql.executeQueryAndPrintResult(sql);
         System.out.println("-------------------------------------------------------------\n");
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
      }
      
   }

   public static void placeOrder(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String input;
      List<String> game_id = new ArrayList<String>();
      List<Integer> amount = new ArrayList<Integer>();
      int acc = 0;
      float total = 0;
      do {
         System.out.println("--------------------------------------------------------------\n");
         System.out.println("Please add a game id to order (press enter to continue):\n");
         input = reader.nextLine();
         if (input.equals("")) break;
         String sql = "SELECT price FROM Catalog WHERE gameID = '"+input+"';";
         List<List<String>> result;
         try {
            result = esql.executeQueryAndReturnResult(sql);
         } catch (Exception e) {
            System.out.println("Something went wrong. Code A\n");
            continue;
         }

         if(game_id.contains(input)) {
            System.out.println("Game already added.\n");
         }
         else if(result.size() <= 0) {
            System.out.println("Game not in Catalog\n");
         }
         else {
            game_id.add(input);
            System.out.println("Add number of copies to order.\n");
            int item = readChoice();
            amount.add(item);
            acc += item;
            total += item*Float.valueOf(result.get(0).get(0));
         }
      } while(true);

      // String update = "INSERT INTO Users VALUES('"+login+"','"+pword+"','customer',NULL,'"+phone+"',0);";

      String order_sql = "INSERT INTO RentalOrder VALUES(null, '"+esql.loginUser+"',"+Integer.toString(acc)+","+String.format("%.2f", total)+",null,null);";
      try {
         esql.executeUpdate(order_sql);
      } catch(Exception e) {
         System.out.println("Something went wrong. Code B.");
         System.out.println(e.getMessage());
         return;
      }
      String order_id = get_order_id(esql);
      String track_sql = "INSERT INTO TrackingInfo VALUES(null,'"+order_id+"','OrderReceived','Phoenix,AZ','USPS',null,'');";
      try {
         esql.executeUpdate(track_sql);
      } catch(Exception e) {
         System.out.println(e.getMessage());
         System.out.println("Something went wrong. Code C.");
         return;
      }

      for(int i = 0; i < game_id.size(); i++) {
         String cur_id = game_id.get(i);
         int cur_am = amount.get(i);
         String game_sql = "INSERT INTO GamesInOrder VALUES('"+order_id+"','"+cur_id+"',"+Integer.toString(cur_am)+");";
         try {
            esql.executeUpdate(game_sql);
         } catch(Exception e) {
            System.out.println("Something went wrong. Code D.");
            return;
         }
      }

      String track_id = get_track_id(esql);
      System.out.println("Order complete!");
      System.out.print("Order ID: ");
      System.out.println(order_id);
      System.out.print("Tracking ID: ");
      System.out.println(track_id);
      System.out.print("Total units ordered: ");
      System.out.println(Integer.toString(acc));
      System.out.print("Total price: ");
      System.out.println(String.format("%.2f", total));
      System.out.println("--------------------------------------------------------------\n");
   }
   public static void viewAllOrders(GameRental esql) {
      List<List<String>> result;
      try{
         System.out.println("\n-------------------------------");
         System.out.println("Order history\n");
         String query = "SELECT rentalOrderID FROM RentalOrder WHERE login='"+esql.loginUser+"';";
               // String check = "SELECT * FROM Users WHERE login='"+esql.loginUser+"' AND role='manager';";

         result = esql.executeQueryAndReturnResult(query);
         if (result.size() <= 0) {    
            System.out.println("No orders made"); 
         } 
         else {
            for (int i=0; i<=result.size()-1; ++i) System.out.println("* " + result.get(i).get(0));
            System.out.println("-------------------------------\n");
         }
      } catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void viewRecentOrders(GameRental esql) {
      List<List<String>> result;
      try{
         System.out.println("\n-------------------------------");
         System.out.println("Recent orders\n");
         String query = "SELECT rentalOrderID FROM RentalOrder WHERE login='"+esql.loginUser+"' ORDER BY orderTimestamp DESC;";
               // String check = "SELECT * FROM Users WHERE login='"+esql.loginUser+"' AND role='manager';";

         result = esql.executeQueryAndReturnResult(query);
         if (result.size() <= 0) {    
            System.out.println("No rental orders made"); 
         } 
         else {
            for (int i=0; i<=4; ++i) System.out.println(i+1 + ") " + result.get(i).get(0));
            System.out.println("-------------------------------\n");
         }
      } catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void viewOrderInfo(GameRental esql) {
      Scanner reader = new Scanner(System.in);  
      List<List<String>> result;
      List<String> gameList = new ArrayList<String>();
      String query;
      try{
         System.out.println("-------------------------------\n");
         do {
            System.out.print("Please enter rental order ID (format as \"gamerentalorder<num>\" e.g. gamerentalorder1004, enter 'b' to go back):\n > ");
            String roID = reader.next();
            if(roID.matches("gamerentalorder[0123456789]+")) {
               query = "SELECT r.orderTimestamp, r.dueDate, r.totalPrice, t.trackingID, c.gameName FROM RentalOrder r, GamesInOrder g, TrackingInfo t, Catalog c WHERE r.login= '"+esql.loginUser+"' AND t.rentalOrderID=r.rentalOrderID AND r.rentalOrderID= '"+roID+"' AND r.rentalOrderID=g.rentalOrderID AND g.gameID=c.gameID;";
               result = esql.executeQueryAndReturnResult(query);
               if(result.size() <= 0) {
                  System.out.println("Could not find " + roID);
               }
               else {
                  for(int i = 0; i <= result.size()-1; ++i) gameList.add(result.get(i).get(4));
                  System.out.println("\nTracking ID: " + result.get(0).get(3));
                  System.out.println("Order Timestamp: " + result.get(0).get(0));
                  System.out.print("Games: ");
                  for(int i = 0; i <= gameList.size()-2; ++i) System.out.print(gameList.get(i) + ", ");
                  System.out.println(gameList.get(gameList.size()-1));
                  System.out.println("Due Date: " + result.get(0).get(1));
                  System.out.println("Total Price: " + result.get(0).get(2) + "\n");
               }
            }
            else if(roID.matches("b")){
               System.out.println("-------------------------------\n");
               break;
            }
            else{
               System.out.println("Input doesn't match format specified");
            }
            
         } while(true);
      } catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void viewTrackingInfo(GameRental esql) {
      Scanner reader = new Scanner(System.in);  
      List<List<String>> result;
      String query;
      try{
         System.out.println("-------------------------------\n");
         do {
            System.out.print("Please enter tracking ID (format as \"trackingid<num>\" e.g. trackingid1004, enter 'b' to go back):\n > ");
            String tID = reader.next();
            if(tID.matches("trackingid[0123456789]+")) {
               query = "SELECT t.rentalOrderID, t.courierName, t.currentLocation, t.status, t.lastUpdateDate, t.additionalComments FROM TrackingInfo t, RentalOrder r WHERE t.rentalOrderID=r.rentalOrderID AND r.login= '"+esql.loginUser+"' AND t.trackingID= '"+tID+"' ;";
               result = esql.executeQueryAndReturnResult(query);
               if(result.size() <= 0) {
                  System.out.println("Could not find " + tID);
               }
               else {
                  System.out.println("\nRental order ID: " + result.get(0).get(0));
                  System.out.println("Courier name: " + result.get(0).get(1));
                  System.out.println("Current location: " + result.get(0).get(2));
                  System.out.println("Status: " + result.get(0).get(3));
                  System.out.println("Last updated: " + result.get(0).get(4));
                  System.out.println("Comments: " + result.get(0).get(5));
               }
            }
            else if(tID.matches("b")){
               System.out.println("-------------------------------\n");
               break;
            }
            else{
               System.out.println("Input doesn't match format specified");
            }
            
         } while(true);
      } catch(Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void updateTrackingInfo(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String check = "SELECT * FROM Users WHERE login='"+esql.loginUser+"' AND role!='customer';";
      try{
         if(esql.executeQuery(check) <=0) {
            System.out.println("Unauthorized user.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }

      System.out.print("Enter user tracking id you wish to update (format as \"trackingid<num>\" e.g. trackingid1004):\n > ");
      String tID = reader.nextLine();
      String sql = "SELECT * FROM TrackingInfo WHERE trackingID='"+tID+"';";
      List<List<String>> tracking_info = null;
      try{
         tracking_info = esql.executeQueryAndReturnResult(sql);

         if(tracking_info.size() <= 0) {
            System.out.println("No tracking info matches id.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }
      System.out.println(new String("\nTracking ID: " + tracking_info.get(0).get(0)));
      System.out.println(new String("Rental order ID: " + tracking_info.get(0).get(1)));
      System.out.println(new String("Status: "+ tracking_info.get(0).get(2)));
      System.out.println(new String("Current location: " + tracking_info.get(0).get(3)));
      System.out.println(new String("Courier name: " + tracking_info.get(0).get(4)));
      System.out.println(new String("Last updated: " + tracking_info.get(0).get(5)));
      System.out.println(new String("Comments: " + tracking_info.get(0).get(6)));

      int choice = 0;
      do {
         System.out.println("Choose which item to update");
         System.out.println("1. status");
         System.out.println("2. current location");
         System.out.println("3. courier name");
         System.out.println("4. comments");
         System.out.println("0. exit");
         choice = readChoice();

         String update;
         String timeStamp;

         switch(choice) {
            case 1:
               System.out.print("Enter new status:\n > ");
               String newStat = reader.nextLine();
               timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

               update = "UPDATE TrackingInfo SET lastUpdateDate='"+timeStamp+"', status='"+newStat+"' WHERE trackingID = '"+tID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("status updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;
            
            case 2:
               System.out.print("Enter new current location:\n > ");
               String newCurLoc = reader.nextLine();
               timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

               update = "UPDATE TrackingInfo SET lastUpdateDate='"+timeStamp+"', currentLocation='"+newCurLoc+"' WHERE trackingID = '"+tID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("current location updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;        

            case 3:
               System.out.print("Enter new courier name:\n > ");
               String newCourName = reader.nextLine();
               timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

               update = "UPDATE TrackingInfo SET lastUpdateDate='"+timeStamp+"', courierName='"+newCourName+"' WHERE trackingID = '"+tID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("courier name updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;  

            case 4:
               System.out.print("Enter new comments:\n > ");
               String newComm = reader.nextLine();
               timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

               update = "UPDATE TrackingInfo SET lastUpdateDate='"+timeStamp+"', additionalComments='"+newComm+"' WHERE trackingID = '"+tID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("comments updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;         

            case 0:
            System.out.println("Exiting....\n");
            break;

            default:
            System.out.println("Invalid choice.\n");
            break;
         }
      } while(choice != 0);
   }
   public static void updateCatalog(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String check = "SELECT * FROM Users WHERE login='"+esql.loginUser+"' AND role='manager';";
      try{
         if(esql.executeQuery(check) <=0) {
            System.out.println("Unauthorized user.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }

      System.out.print("Enter user game id you wish to update (format as \"game<num>\" e.g. game0350):\n > ");
      String gID = reader.nextLine();
      String sql = "SELECT * FROM Catalog WHERE gameID='"+gID+"';";
      List<List<String>> game_info = null;
      try{
         game_info = esql.executeQueryAndReturnResult(sql);

         if(game_info.size() <= 0) {
            System.out.println("No game info matches id.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }

      System.out.println(new String("\nGame ID: " + game_info.get(0).get(0)));
      System.out.println(new String("Name: " + game_info.get(0).get(1)));
      System.out.println(new String("Genre: "+ game_info.get(0).get(2)));
      System.out.println(new String("Price: " + game_info.get(0).get(3)));
      System.out.println(new String("Description: " + game_info.get(0).get(4)));
      System.out.println(new String("imageURL: " + game_info.get(0).get(5)));

      int choice = 0;
      do {
         System.out.println("Choose which item to update");
         System.out.println("1. name");
         System.out.println("2. genre");
         System.out.println("3. price");
         System.out.println("4. description");
         System.out.println("5. imageURL");
         System.out.println("0. exit");
         choice = readChoice();

         String update;

         switch(choice) {
            case 1:
               System.out.print("Enter new game name:\n > ");
               String newName = reader.nextLine();

               update = "UPDATE Catalog SET gameName='"+newName+"' WHERE  gameID='"+gID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("game name updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;
            
            case 2:
               System.out.print("Enter new genre:\n > ");
               String newGenre = reader.nextLine();

               update = "UPDATE Catalog SET genre='"+newGenre+"' WHERE gameID = '"+gID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("genre updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;        

            case 3:
               System.out.print("Enter new price:\n > ");
               String newPrice = reader.nextLine();

               update = "UPDATE Catalog SET price='"+newPrice+"' WHERE gameID = '"+gID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("price updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;  

            case 4:
               System.out.print("Enter new description:\n > ");
               String newDesc = reader.nextLine();

               update = "UPDATE Catalog SET description='"+newDesc+"' WHERE gameID = '"+gID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("description updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;       

            case 5:
               System.out.print("Enter new image URL:\n > ");
               String newImgURL = reader.nextLine();

               update = "UPDATE Catalog SET imageURL='"+newImgURL+"' WHERE gameID = '"+gID+"';";
               try {
                  esql.executeUpdate(update);
                  System.out.println("image url updated.");
               } catch(Exception e) {
                  System.out.println("Something went wrong.");
               }
            break;  

            case 0:
            System.out.println("Exiting....\n");
            break;

            default:
            System.out.println("Invalid choice.\n");
            break;
         }
      } while(choice != 0);
   }
   public static void updateUser(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String check = "SELECT * FROM Users WHERE login='"+esql.loginUser+"' AND role='manager';";
      try{
         if(esql.executeQuery(check) <=0) {
            System.out.println("Unauthorized user.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }
      System.out.println("Choose option:");
      System.out.println("1. View all User IDs");
      System.out.println("2. Update user");
      int input = readChoice();

      switch(input) {
         case 1:
         viewAllUsers(esql);
         break;
            
         case 2: 
         userUpdateInfo(esql);
         break;
         default:
         System.out.println("Invalid option.");
      }
      
   }


   public static boolean check_password(String s) {
      if(s.length() < 15) {
         System.out.println("Password is too short.\n");
         return false;
      }
      if(s.length() > 30) {
         System.out.println("Password is too long.\n");
         return false;
      }
      if(!s.matches("[a-zA-Z0-9]*")) {
         System.out.println("Unexpected symbol.\n");
         return false;
      }
      
      boolean lower=false;
      boolean upper=false;
      boolean digit=false;
      for(int i = 0; i < s.length();i++) {
         char c = s.charAt(i);
         lower = lower || (0x60 < c && c <= 0x7A);
         upper = upper || (0x40 < c && c <= 0x5A);
         digit = digit || (0x30 <= c && c <= 0x39);
      }

      if(!lower) {
         System.out.println("Missing lower case letter.\n");
      }
      if(!upper) {
         System.out.println("Missing upper case letter.\n");
      }
      if(!digit) {
         System.out.println("Missing digit.\n");
      }
      return lower && upper && digit;
   }

   public static void change_phone(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String user_input;
      do {
         System.out.println("Please add a phone number. Use the format +<country code>-xxx-xxx-xxxx.");
         user_input = reader.next();
         if(!user_input.matches("\\+[0-9]{1,5}\\-[0-9]{3}\\-[0-9]{3}\\-[0-9]{4}")) {
            System.out.println("Input does not match format!\n");
         }
         else{
            break;
         }
      }while(true);

      String sql = "UPDATE Users SET phoneNum = '"+user_input+"' WHERE login = '"+esql.loginUser+"';";
      try {
         esql.executeUpdate(sql);
         System.out.println("Phone number updated.\n");
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
      }
   }

   public static void change_fav_games(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String user_input;
      System.out.print("Please input favourite games here: ");
      user_input = reader.next();

      String sql = "UPDATE Users SET favGames = '"+user_input+"' WHERE login = '"+esql.loginUser+"';";
      try {
         esql.executeUpdate(sql);
         System.out.println("Favourite games updated.\n");
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
      }
   }

   public static void change_password(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      String user_password;
      String new_password;
      do {
         System.out.print("Enter old password: ");
         user_password = reader.next();
         String sql = "SELECT * FROM USERS WHERE login = '" +esql.loginUser+"' AND password = '"+user_password+"';";
         try {
            if ( esql.executeQuery(sql) > 0) {
               break;
            }
            else {
               System.out.print("Incorrect password\n");
            }
         } catch(Exception e) {
            System.out.print("Something went wrong.\n");
         }
      } while(true);

      do {
         do {
            System.out.print("Please enter a new password. It must have the following:\n");
            System.out.print("1. Password must be at least 15 and at most 30 characters.\n");
            System.out.print("2. Password must contain only letters and numbers.\n");
            System.out.print("3. Password must contain at least one lower case letter, one upper case letter, and a digit.\n");
            System.out.print("Enter password: ");
            new_password = reader.next();
         }while(!check_password(new_password));
         user_password = new String(new_password);
         System.out.print("Please re-enter password: ");
         new_password = reader.next();
         if(!new_password.equals(user_password)) {
            System.out.print("Passwords do not match!\n");
         }
         else {
            break;
         }

      } while(true);

      String sql = "UPDATE Users SET password = '"+new_password+"' WHERE login = '"+esql.loginUser+"';";
      try {
         esql.executeUpdate(sql);
         System.out.println("Password updated.\n");
      } catch(Exception e) {
         System.out.print("Something went wrong.\n");
      }
   }

   public static String get_order_id(GameRental esql) {
      List<List<String>> temp;
      try {
         temp = esql.executeQueryAndReturnResult("SELECT last_value FROM order_seq;");
      } catch(Exception e) {
         return "";
      }
      return "gamerentalorder"+temp.get(0).get(0);
   }

   public static String get_track_id(GameRental esql) {
      List<List<String>>  temp;
      try {
         temp = esql.executeQueryAndReturnResult("SELECT last_value FROM track_seq;");
      } catch(Exception e) {
         return "";
      }
      return "trackingid"+temp.get(0).get(0);
   }
   public static void viewAllUsers(GameRental esql) {
      String sql = "SELECT login FROM Users";
      try{
         esql.executeQueryAndPrintResult(sql);
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
      }
      System.out.println("");
   }
   public static void userUpdateInfo(GameRental esql) {
      Scanner reader = new Scanner(System.in);
      System.out.println("Enter user id you wish to update:");
      String id = reader.nextLine();
      String sql = "SELECT * FROM Users WHERE login='"+id+"';";
      List<List<String>> user_info = null;
      try{
         user_info = esql.executeQueryAndReturnResult(sql);

         if(user_info.size() <= 0) {
            System.out.println("No user matches this id.\n");
            return;
         }
      } catch(Exception e) {
         System.out.println("Something went wrong.\n");
         return;
      }
      System.out.println(new String ("Login: " + user_info.get(0).get(0)));
      System.out.println(new String("password: " + user_info.get(0).get(1)));
      System.out.println(new String("role: "+ user_info.get(0).get(2)));
      System.out.println(new String("favGames: " + user_info.get(0).get(3)));
      System.out.println(new String("phoneNum: " + user_info.get(0).get(4)));
      System.out.println(new String("numOverDueGames: " + user_info.get(0).get(5)));
      // login varchar(50) NOT NULL,
      // password varchar(30) NOT NULL,
      // role char(20) NOT NULL,
      // favGames text,
      // phoneNum varchar(20) NOT NULL,
      // numOverDueGames integer DEFAULT 0

      int choice = 0;
      do {
         System.out.println("Choose which item to update");
         System.out.println("1. login (depreciated)");
         System.out.println("2. password");
         System.out.println("3. role");
         System.out.println("4. faveGames");
         System.out.println("5. phoneNum");
         System.out.println("6. numOverDueGames");
         System.out.println("0. exit");
         choice = readChoice();

         String update;
         switch(choice) {
            case 1:
            // System.out.println("Enter new login");
            // String input = reader.nextLine();
            // String check = "SELECT * FROM Users WHERE login='"+input+"';";
            // update = "UPDATE Users SET login = '"+input+"' WHERE login = '"+id+"';";
            // try {
            //    if(esql.executeQuery(check) > 0) {
            //       System.out.println("user id already exists.\n");
            //    }
            //    else {
            //       esql.executeUpdate(update);
            //       System.out.println("login updated");
            //    }
            // } catch(Exception e) {
            //    System.out.println("Something went wrong.");
            // }
            break;

            case 2:
            System.out.println("Enter new password");
            String input1 = reader.nextLine();
            // String check = "SELECT * FROM Users WHERE login='"+input+"';";
            update = "UPDATE Users SET password = '"+input1+"' WHERE login = '"+id+"';";
            try {
               esql.executeUpdate(update);
               System.out.println("passord updated.");
            } catch(Exception e) {
               System.out.println("Something went wrong.");
            }
            break;

            case 3:
            System.out.println("Enter new role");
            String input2 = reader.nextLine();
            update = "UPDATE Users SET role = '"+input2+"' WHERE login = '"+id+"';";
            try {
               esql.executeUpdate(update);
               System.out.println("role updated.");
            } catch(Exception e) {
               System.out.println("Something went wrong.");
            }
            break;

            case 4:
            System.out.println("Enter new favGames");
            String input3 = reader.nextLine();
            update = "UPDATE Users SET favGames = '"+input3+"' WHERE login = '"+id+"';";
            try {
               esql.executeUpdate(update);
               System.out.println("favGames updated.");
            } catch(Exception e) {
               System.out.println("Something went wrong.");
            }
            break;

            case 5:
            String input4;
            do {
               System.out.print("Enter a new phone number. Use the format +<country code>-xxx-xxx-xxxx.\n");
               input4 = reader.nextLine();
               if(!input4.matches("\\+[0-9]{1,5}\\-[0-9]{3}\\-[0-9]{3}\\-[0-9]{4}")) {
                  System.out.print("Input does not match format!\n");
               }
               else{
                  break;
               }
            }while(true);
            
            update = "UPDATE Users SET phoneNum = '"+input4+"' WHERE login = '"+id+"';";
            try {
               esql.executeUpdate(update);
               System.out.println("phone number updated.");
            } catch(Exception e) {
               System.out.println("Something went wrong.");
            }
            break;

            case 6:
            System.out.println("Enter new number of overdue games:");
            int input5 = readChoice();
            update = "UPDATE Users SET numOverDueGames = '"+Integer.toString(input5)+"' WHERE login = '"+id+"';";
            try {
               esql.executeUpdate(update);
               System.out.println("numOverDueGames updated.");
            } catch(Exception e) {
               System.out.println("Something went wrong.");
            }
            break;

            case 0:
            System.out.println("Exiting....\n");

            default:
            System.out.println("Invalid choice.\n");
            break;
         }
      } while(choice != 0);

   }


}//end GameRental

