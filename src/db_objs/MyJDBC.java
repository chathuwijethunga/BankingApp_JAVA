package db_objs;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;


//interact with MySQL database
public class MyJDBC {
    //database configurations
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/bankapp";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Chathurya098!";

    public static User validateLogin(String username, String password){
        try{
            // establish a connection to the database using configurations
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // create sql query
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?"
            );

            // replace the ? with values
            // parameter index referring to the iteration of the ? so 1 is the first ? and 2 is the second ?
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            // execute query and store into a result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // next() returns true or false
            // true - query returned data and result set now points to the first row
            // false - query returned no data and result set equals to null
            if(resultSet.next()){
                // success
                // get id
                int userId = resultSet.getInt("id");

                // get current balance
                BigDecimal currentBalance = resultSet.getBigDecimal("current_balance");

                // return user object
                return new User(userId, username, password, currentBalance);
            }

        }catch(SQLException e){
            e.printStackTrace();
        }

        // not valid user
        return null;
    }

    // registers new user to the database
    // true - register success
    // false - register fails
    public static boolean register(String username, String password){
        try{
            // first we will need to check if the username has already been taken
            if(!checkUser(username)){
                Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO users(username, password, current_balance) " +
                                "VALUES(?, ?, ?)"
                );

                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setBigDecimal(3, new BigDecimal(0));

                preparedStatement.executeUpdate();
                return true;

            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    private static boolean checkUser(String username){
        try{
            Connection connection= DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()){
                return false;

            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean addTransctionTodatabase(Transactions transactions){
        try{
            Connection connection= DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement insertTransaction = connection.prepareStatement(
                    "Insert INTO transactions(user_id, transaction_type, transaction_amount, transaction_date)" + "VALUES(?, ?, ?, NOW())"
            );
            insertTransaction.setInt(1, transactions.getUserId());
            insertTransaction.setString(2, transactions.getTransactionType());
            insertTransaction.setBigDecimal(3, transactions.getTransactionAmount());
            insertTransaction.executeUpdate();
            return true;

        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }
    public static boolean updateCurrentBalance(User user){
        try{
            Connection connection= DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement updateBalance = connection.prepareStatement("UPDATE users SET current_balance = ? WHERE id = ?");
            updateBalance.setBigDecimal(1, user.getCurrentBalance());
            updateBalance.setInt(2, user.getId());
            updateBalance.executeUpdate();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    // true - transfer was a success
    // false - transfer was a fail
    public static boolean transfer(User user, String transferredUsername, float transferAmount){
        try{
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement queryUser = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );

            queryUser.setString(1, transferredUsername);
            ResultSet resultSet = queryUser.executeQuery();

            while (resultSet.next()) {
                // perform transfer
                User transferredUser = new User(
                        resultSet.getInt("id"),
                        transferredUsername,
                        resultSet.getString("password"),
                        resultSet.getBigDecimal("current_balance")
                );

                // create transaction
                Transactions transferTransaction = new Transactions(
                        user.getId(),
                        "Transfer",
                        BigDecimal.valueOf(-transferAmount),
                        null
                );

                // this transaction will belong to the transferred user
                Transactions receivedTransaction = new Transactions(
                        transferredUser.getId(),
                        "Transfer",
                        new BigDecimal(transferAmount),
                        null
                );

                // update transfer user
                transferredUser.setCurrentBalance(transferredUser.getCurrentBalance().add(BigDecimal.valueOf(transferAmount)));
                updateCurrentBalance(transferredUser);

                // update user current balance
                user.setCurrentBalance(user.getCurrentBalance().subtract(BigDecimal.valueOf(transferAmount)));
                updateCurrentBalance(user);

                // add these transactions to the database
                addTransctionTodatabase(transferTransaction);
                addTransctionTodatabase(receivedTransaction);

                return true;

            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }
    // get all transactions (used for past transaction)
    public static ArrayList<Transactions> getPastTransaction(User user){
        ArrayList<Transactions> pastTransactions = new ArrayList<>();
        try{
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement selectAllTransaction = connection.prepareStatement(
                    "SELECT * FROM transactions WHERE user_id = ?"
            );
            selectAllTransaction.setInt(1, user.getId());

            ResultSet resultSet = selectAllTransaction.executeQuery();

            // iterate through the results (if any)
            while(resultSet.next()){
                // create transaction obj
                Transactions transaction = new Transactions(
                        user.getId(),
                        resultSet.getString("transaction_type"),
                        resultSet.getBigDecimal("transaction_amount"),
                        resultSet.getDate("transaction_date")
                );

                // store into array list
                pastTransactions.add(transaction);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return pastTransactions;
    }


}
