import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class UpdateUser extends HttpServlet {

    // inner definitions

    private static class JSONKeys {
        private static final String ERROR_CODE = "ErrorCode";
        private static final String ERROR_MESSAGE = "ErrorMessage";
        private static final String CHANGED_FIELD = "ChangedField";
        private static final String NEW_VALUE = "NewValue";
    }

    private static class ServerError {
        private static final int NO_ERROR = 0;
        private static final int BAD_SQL_CONNECTION = 1;
        private static final int SQL_UPDATE_FAILED = 2;
    }

    // fields

    private static final String JSON_CONTENT = "application/json";

    // methods

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // setup json response
        resp.setContentType(JSON_CONTENT);
        JSONObject json = new JSONObject();

        // parse request
        try {
            // get user from db
            DB db = new DB();
            String userID = req.getParameter("Id");
            ResultSet user = db.findUserByID(userID);

            if (user.first()) {
                // get updates
                String fieldToChange = req.getParameter("FieldToChange");
                String newValue = req.getParameter("NewValue");

                // update record
                String error = db.updateUserWithID(userID, fieldToChange, newValue);
                if (error == null) {
                    json.put(JSONKeys.ERROR_CODE, ServerError.NO_ERROR);
                    json.put(JSONKeys.CHANGED_FIELD, fieldToChange);
                    json.put(JSONKeys.NEW_VALUE, newValue);
                } else {
                    json.put(JSONKeys.ERROR_CODE, ServerError.SQL_UPDATE_FAILED);
                    json.put(JSONKeys.ERROR_MESSAGE, error);
                }

                // close connection
                db.close();
            }
        } catch (SQLException e) {
            json.put(JSONKeys.ERROR_CODE, ServerError.BAD_SQL_CONNECTION);
            json.put(JSONKeys.ERROR_MESSAGE, e.getMessage());
            e.printStackTrace();
        }

        // write response
        resp.getWriter().print(json);
    }
}
