import java.sql.*;
import java.util.Locale;

/**
 * Created by mrigdon on 2/13/17.
 */
public class DB {

    // fields
    private Connection connection;
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Trojans17";
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/capstonedb?useSSL=false";

    // constructors
    DB(Connection connection) {
        this.connection = connection;
    }

    DB() throws SQLException {
        connection = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASSWORD);
    }

    // methods
    void close() throws SQLException {
        connection.close();
    }

    ResultSet findUserByID(String id) {
        try {
            PreparedStatement getUser = connection.prepareStatement("SELECT * FROM user WHERE Id=?");
            getUser.setString(1, id);
            return getUser.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    String updateUserWithID(String id, String field, String value) {
        try {
            String sql = String.format(Locale.getDefault(), "UPDATE user SET %s=? WHERE Id=?", field);
            PreparedStatement updateUser = connection.prepareStatement(sql);
            updateUser.setString(1, value);
            updateUser.setString(2, id);
            int success = updateUser.executeUpdate();

            if (success > 0) {
                return null;
            } else {
                return "SQL update returned 0 affected rows";
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}