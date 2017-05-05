package ud.binmonkey.prog3_proyecto_server.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ud.binmonkey.prog3_proyecto_server.common.DateUtils;
import ud.binmonkey.prog3_proyecto_server.common.Language;
import ud.binmonkey.prog3_proyecto_server.common.Role;
import ud.binmonkey.prog3_proyecto_server.common.User;
import ud.binmonkey.prog3_proyecto_server.common.exceptions.NewUserExistsException;
import ud.binmonkey.prog3_proyecto_server.common.exceptions.UserNotFoundException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDB {

    /* TODO: tests */
    private static final Logger LOG = Logger.getLogger(MongoDB.class.getName());
    private static String COLLECTION = "users";
    static {
        try {
            LOG.addHandler(new FileHandler(
                    "logs/" + MongoDB.class.getName() + "." +
                            DateUtils.currentFormattedDate() + ".log.xml", true));
        } catch (SecurityException | IOException e) {
            LOG.log(Level.SEVERE, "Unable to create log file.");
        }
    }

    /* should there be MongoClient, MongoDB and MongoCollection attrs? */

    /**
     * Change username of existing user
      * @param oldUserName current username
     * @param newUserName new username
     */
    public static void changeUserName(String oldUserName, String newUserName) throws UserNotFoundException, NewUserExistsException {

        if(userExists(oldUserName)) {
            if (!userExists(newUserName)) {
                MongoDatabase db = getUsersDB();
                MongoCollection collection = db.getCollection(COLLECTION);
                collection.updateMany(
                        new BasicDBObject("username", oldUserName),
                        new BasicDBObject("$set",
                                new BasicDBObject("username", newUserName)
                        )
                );
                LOG.log(Level.INFO, "Renamed user `" + oldUserName +  "` to user `" + newUserName + "`");
            } else {
                throw new NewUserExistsException(newUserName);
            }
        } else {
            throw new UserNotFoundException(oldUserName);
        }

    }

    /**
     * @return User MongoDatabase object
     */
    public static MongoDatabase getUsersDB(){
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase(COLLECTION);
        try {
            db.createCollection(COLLECTION);
        } catch (MongoCommandException e) {}
        return db;
    }

    /**
     * Find user in database
     * @param userName username of user to be found
     * @return Document containing user
     */
    public static Document getUser(String userName) throws UserNotFoundException {

        MongoDatabase db = getUsersDB();
        MongoCollection collection = db.getCollection(COLLECTION);
        FindIterable iterable = collection.find(new BasicDBObject("username", userName));
        for (Object o: iterable) {
            if (o instanceof Document) {
                if(((Document) o).get("username").equals(userName)) {
                    return (Document) o;
                }
            }
        }
        throw new UserNotFoundException(userName);
    }

    /**
     * Checks if username is already registered in database
     * @param userName username to be checked
     * @return true if it exists, false if not
     */
    public static boolean userExists(String userName) {

        MongoDatabase db = getUsersDB();
        MongoCollection collection = db.getCollection(COLLECTION);
        BasicDBObject query = new BasicDBObject();
        query.put("username", userName);

        /* if user count != 0 */
        return collection.count(query) != 0;
    }

    /**
     * DON'T CALL DIRECTLY UNLESS IT'S FROM UserManager TO AVOID CONFLICT WITH FTP
     * Creates a Document in MongoDB with the Object passed as arg, if one with same username does not already exist
     * @param user user (as an object) to be created
     */
    public static void createUser(User user) throws NewUserExistsException {
        if (!userExists(user.getUserName())) {
            MongoDatabase db = getUsersDB();
            MongoCollection collection = db.getCollection(COLLECTION);
            BasicDBObject doc = new BasicDBObject("username", user.getUserName())
                    .append("display_name", user.getDisplayName())
                    .append("email", user.getEmail())
                    .append("gender", user.getGender())
                    .append("password", user.getPassword())
                    .append("preferred_language", user.getPreferredLanguage().toString())
                    .append("role", user.getRole().toString());
            collection.insertOne(Document.parse(doc.toJson()));
            String logInfo = "";
            int i = 1;
            for(String key: doc.keySet()) {
                logInfo += "\n" + key + ": '" + doc.get(key) + "'";
                if (i < doc.size()) {
                    logInfo += ",";
                }
                i++;
            }
            LOG.log(Level.INFO, "New user created:" + logInfo);
        } else {
            throw new NewUserExistsException(user.getUserName());
        }
    }

    public static void createUser(Document user) {
        MongoDatabase db = getUsersDB();
        MongoCollection collection = db.getCollection(COLLECTION);
        collection.insertOne(user);
    }

    /**
     * DON'T CALL DIRECTLY UNLESS IT'S FROM UserManager TO AVOID CONFLICT WITH FTP
     * Removes all users with username in args (which should only be one)
     * @param userName
     */
    public static void deleteUser(String userName) throws UserNotFoundException {
        if (userExists(userName)) {
            MongoDatabase db = getUsersDB();
            MongoCollection collection = db.getCollection(COLLECTION);
            BasicDBObject query = new BasicDBObject();
            query.append("username", userName);
            collection.deleteOne(query);
            LOG.log(Level.INFO, "User `" + userName + "` deleted.");
        } else {
            throw new UserNotFoundException(userName);
        }
    }

    public static void main(String[] args) throws UserNotFoundException, NewUserExistsException {
        try {
            deleteUser("ben10");
        } catch (UserNotFoundException e) {}
        try {
            deleteUser("10ben");
        } catch (UserNotFoundException e) {}

        MongoDB.createUser(new User(
                "10-10-2010", "Ben Ten", "ben10@ben.ten", "Male",
                "10ben".toCharArray(), Language.EN, Role.USER, "ben10"
        ));
        System.out.println(MongoDB.userExists("ben10"));
        changeUserName("ben10", "10ben");
        System.out.println(MongoDB.userExists("10ben"));
        getUser("10ben");
    }
}
