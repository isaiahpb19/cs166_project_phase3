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
         System.out.println("Done");
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
         "              User Interface      	               \n" +
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
         String update = "INSERT INTO Users VALUES('"+login+"','"+pword+"','customer',NULL,'"+phone+"',0);";
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
      System.out.print("Enter username:");
      username = reader.next();
      System.out.print("Enter password:");
      password = reader.next();
      
      String sql = "SELECT * FROM USERS WHERE login = '" +username+"' AND password = '"+password+"';";
      try {
         if ( esql.executeQuery(sql) > 0) {
            System.out.print("Log In Success.\n");
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
      System.out.println(new String("Phone Number: "+items.get(0).get(0)));
      System.out.println(new String("Number of Overdue Games: "+items.get(0).get(1)));
      System.out.println(new String("Favourite Games: \n"+items.get(0).get(2)));

   }
   public static void updateProfile(GameRental esql) {
      System.out.println("Choose which entry to update:");
      System.out.println("1. Phone number");
      System.out.println("2. Favourite Games");
      System.out.println("3. Password");
      int input = readChoice();

      switch(input) {
         case 1:
            change_phone(esql);
            break;
         case 2:
            change_fav_games(esql);
            break;
         case 3:
            change_password(esql);
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
      System.out.println();
   }
   public static void viewAllOrders(GameRental esql) {}
   public static void viewRecentOrders(GameRental esql) {}
   public static void viewOrderInfo(GameRental esql) {}
   public static void viewTrackingInfo(GameRental esql) {}
   public static void updateTrackingInfo(GameRental esql) {}
   public static void updateCatalog(GameRental esql) {}
   public static void updateUser(GameRental esql) {}


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


}//end GameRental

