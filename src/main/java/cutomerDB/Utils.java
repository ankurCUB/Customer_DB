package cutomerDB;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface Utils {

    int CUSTOMER_DB_PORT = 8089;

    static int fetchUserIDGRPC(String username, String password) throws SQLException {
        Connection fetchConnection = getConnectionToCustomerDB();
        String sql = "SELECT userID FROM Login WHERE \"username\" = \"" + username + "\" and \"password\" = " + "\"" + password + "\"";
        ResultSet resultSet = fetchConnection.createStatement().executeQuery(sql);
        int userID = -1;
        if(resultSet.next()){
            userID = resultSet.getInt(1);
        }
        fetchConnection.close();
        return userID;
    }

    static float fetchSellerRatingGRPC(int sellerID) throws SQLException {
        Connection connection = getConnectionToCustomerDB();
        String query = "SELECT thumbsUp, thumbsDown from Sellers where sellerID = " + sellerID;
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        float rating = -1;
        if(resultSet.next()){
            int thumbsUp = resultSet.getInt(1);
            int thumbsDown = resultSet.getInt(1);

            if (thumbsUp + thumbsDown != 0) {
                rating = 10 * (thumbsUp - thumbsDown) / ((float) (thumbsUp + thumbsDown));
            }
        }
        connection.close();
        return rating;
    }


    static Connection getConnectionToCustomerDB() throws SQLException {
        try {
            File file = new File("CustomerDB.db");
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            if (file.exists()) {
                return DriverManager.getConnection(url);
            } else {
                Connection connection = DriverManager.getConnection(url);
                connection.createStatement().execute("CREATE TABLE \"Buyers\" (\n" +
                        "\t\"buyerId\"\tINTEGER NOT NULL,\n" +
                        "\t\"buyerName\"\tTEXT NOT NULL,\n" +
                        "\t\"itemsPurchased\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"buyerId\" AUTOINCREMENT)\n" +
                        ")");
                connection.createStatement().execute("CREATE TABLE \"Login\" (\n" +
                        "\t\"username\"\tTEXT NOT NULL UNIQUE,\n" +
                        "\t\"password\"\tTEXT NOT NULL,\n" +
                        "\t\"userType\"\tTEXT NOT NULL,\n" +
                        "\t\"userID\"\tINTEGER,\n" +
                        "\tCONSTRAINT \"Login_composite_key\" PRIMARY KEY(\"userID\")\n" +
                        ")");
                connection.createStatement().execute("CREATE TABLE \"PurchaseHistory\" (\n" +
                        "\t\"userID\"\tINTEGER NOT NULL,\n" +
                        "\t\"itemID\"\tINTEGER NOT NULL,\n" +
                        "\t\"quantity\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                        "\t\"timestamp\"\tINTEGER NOT NULL,\n" +
                        "\t\"purchaseID\"\tINTEGER NOT NULL,\n" +
                        "\t\"feedback\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"purchaseID\" AUTOINCREMENT)\n" +
                        ")");
                connection.createStatement().execute("CREATE TABLE \"Sellers\" (\n" +
                        "\t\"sellerId\"\tINTEGER NOT NULL,\n" +
                        "\t\"sellerName\"\tTEXT NOT NULL,\n" +
                        "\t\"thumbsUp\"\tINTEGER DEFAULT 0,\n" +
                        "\t\"thumbsDown\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                        "\t\"itemsSold\"\tINTEGER NOT NULL DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"sellerId\")\n" +
                        ")");
                connection.createStatement().execute("CREATE TABLE \"ShoppingCart\" (" +
                        " \"userID\" INTEGER NOT NULL, \"itemID\" INTEGER NOT NULL," +
                        " \"quantity\" INTEGER NOT NULL, PRIMARY KEY(\"userID\",\"itemID\") )");

                connection.createStatement().execute("CREATE TABLE \"Sessions\" (\n" +
                        "\t\"userID\"\tINTEGER,\n" +
                        "\t\"host\"\tTEXT,\n" +
                        "\tPRIMARY KEY(\"userID\",\"host\")\n" +
                        ")");
                return connection;
            }
        } catch (Exception exception) {
            System.out.println("I printed this : " + exception.getMessage());
            throw exception;
        }
    }
}
