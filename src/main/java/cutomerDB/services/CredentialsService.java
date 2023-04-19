package cutomer_db.services;

import com.example.DistributedAssignment.cutomer_db.services.*;
import com.example.DistributedAssignment.cutomer_db.services.Void;
import cutomer_db.Utils;
import io.grpc.stub.StreamObserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CredentialsService extends CredentialsGrpc.CredentialsImplBase {

    List<String> loginMap = new ArrayList<>();
    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<UserID> responseObserver) {
        try {
            int id;
            if(accountNotExists(request.getUsername())){
                id = createNewAccount(request);
            } else {
                id = Utils.fetchUserIDGRPC(request.getUsername(), request.getPassword());
            }
            UserID userID = UserID.newBuilder().setUserId(id).build();
            responseObserver.onNext(userID);
            responseObserver.onCompleted();
        } catch (Exception exception){
            exception.printStackTrace();
            responseObserver.onError(exception);
            responseObserver.onCompleted();
        }
    }

    private static int createNewAccount(CreateAccountRequest request) throws SQLException {
        Connection connection = Utils.getConnectionToCustomerDB();
        String query = "INSERT INTO Login(\"username\",\"password\",\"userType\") VALUES (\"" +
                request.getUsername() + "\", \"" +
                request.getPassword() + "\", \""+
                request.getUserType().name().toLowerCase()+"\")";
        connection.createStatement().execute(query);

        int userID = Utils.fetchUserIDGRPC(request.getUsername(), request.getPassword());

        if(request.getUserType() == UserType.SELLER){
            query = "INSERT INTO Sellers(\"sellerID\",\"sellerName\") VALUES (" +
                    userID + ", \"" +
                    request.getName() + "\")";
        } else {
            query = "INSERT INTO Buyers(\"buyerID\",\"buyerName\") VALUES (" +
                    userID + ", \"" +
                    request.getName() + "\")";
        }
        connection.createStatement().execute(query);
        connection.close();
        return userID;
    }

    private boolean accountNotExists(String username) throws SQLException {
        Connection connection = Utils.getConnectionToCustomerDB();
        String query = "SELECT * from Login where username = \""+username+"\"";
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        boolean accountExists = false;
        while (resultSet.next()){
            accountExists = true;
        }
        connection.close();
        return !accountExists;
    }


    @Override
    public void login(LoginRequest request, StreamObserver<UserID> responseObserver) {
        try {
            UserID userID = UserID.newBuilder()
                    .setUserId(Utils.fetchUserIDGRPC(request.getUsername(), request.getPassword()))
                    .build();
            responseObserver.onNext(userID);
            responseObserver.onCompleted();
        } catch (SQLException exception){
            responseObserver.onError(exception);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void logout(UserID request, StreamObserver<Void> responseObserver) {
        responseObserver.onNext(Void.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void addSession(SessionData request, StreamObserver<Void> responseObserver) {
        loginMap.add(request.getHostname()+request.getUserId());
        responseObserver.onNext(Void.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getSession(SessionData request, StreamObserver<SessionData> responseObserver) {
        SessionData.Builder response = SessionData.newBuilder().setUserId(request.getUserId()).setHostname(request.getHostname());
        if(!loginMap.contains(request.getHostname()+request.getUserId())){
            response.setHostname("");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeSession(SessionData request, StreamObserver<Void> responseObserver) {
        loginMap.remove(request.getHostname()+request.getUserId());
        responseObserver.onNext(Void.newBuilder().build());
        responseObserver.onCompleted();
    }
}
