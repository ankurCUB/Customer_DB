package cutomer_db.services;

import com.example.DistributedAssignment.cutomer_db.services.*;
import com.example.DistributedAssignment.cutomer_db.services.Void;
import cutomer_db.Utils;
import io.grpc.stub.StreamObserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuyerService extends BuyerItemServicesGrpc.BuyerItemServicesImplBase {
    @Override
    public void addToPurchaseHistory(PurchaseHistoryResponse request, StreamObserver<Void> responseObserver) {
        try {
            Connection connection = Utils.getConnectionToCustomerDB();
            String query = "INSERT INTO PurchaseHistory (\"itemID\", \"quantity\", \"timestamp\" , \"userID\") VALUES( "
                    +request.getItemID()+", "+request.getQuantity() +", "+request.getTimestamp()+", "+request.getUserID()+" ) ";
            connection.createStatement().execute(query);
            connection.close();
            responseObserver.onNext(Void.newBuilder().build());
        } catch (SQLException exception){
            responseObserver.onError(exception);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getBuyerPurchaseHistory(UserID request, StreamObserver<PurchaseHistoryResponse> responseObserver) {
        try {
            Connection connection = Utils.getConnectionToCustomerDB();
            String query = "SELECT \"itemID\", \"quantity\", \"timestamp\" , \"purchaseID\", \"feedback\" from PurchaseHistory where \"userID\" = " + request.getUserId();
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            while (resultSet.next()){
                PurchaseHistoryResponse purchaseHistoryResponse = PurchaseHistoryResponse.newBuilder()
                        .setItemID(resultSet.getInt(1))
                        .setQuantity(resultSet.getInt(2))
                        .setTimestamp(resultSet.getInt(3))
                        .setPurchaseID(resultSet.getInt(4))
                        .setFeedback(resultSet.getFloat(5))
                        .build();
                responseObserver.onNext(purchaseHistoryResponse);
            }
            connection.close();
        } catch (SQLException exception){
            responseObserver.onError(exception);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void provideFeedback(FeedbackRequest request, StreamObserver<Void> responseObserver) {
        try {
            Connection connectionToCustomerDB = Utils.getConnectionToCustomerDB();
            String query = "SELECT itemID, feedback from PurchaseHistory where \"purchaseID\" = " + request.getPurchaseID();
            ResultSet resultSet = connectionToCustomerDB.createStatement().executeQuery(query);
            if(resultSet.next() && resultSet.getInt(2)==0){
                // update purchase history rating
                query = "UPDATE PurchaseHistory SET \"feedback\" = " + request.getLikeOrDislike()
                        + " WHERE \"purchaseID\" = " + request.getPurchaseID();
                connectionToCustomerDB.createStatement().execute(query);

                // get sellerID for purchased item
                int sellerID = request.getSellerId();
                updateSellerRating(connectionToCustomerDB, request.getLikeOrDislike(), sellerID);
                responseObserver.onNext(Void.newBuilder().build());
            } else {
                responseObserver.onNext(Void.newBuilder().build());
            }
            connectionToCustomerDB.close();
        } catch (SQLException exception){
            responseObserver.onError(exception);
        }
        responseObserver.onCompleted();
    }

    private void updateSellerRating(Connection connectionToCustomerDB, int feedback, int sellerID) throws SQLException  {
        String ratingField = "";
        if(feedback==1){
            ratingField = "thumbsUp";
        } else {
            ratingField = "thumbsDown";
        }
        String query = "SELECT "+ratingField+" FROM Sellers WHERE \"sellerID\" = "+sellerID;
        ResultSet resultSet = connectionToCustomerDB.createStatement().executeQuery(query);
        int newRating = 1;
        if(resultSet.next()){
            newRating += resultSet.getInt(1);
        }

        query = "UPDATE Sellers SET \""+ ratingField+"\" = "+newRating+" WHERE \"sellerID\" = "+sellerID;
        connectionToCustomerDB.createStatement().execute(query);
    }
}
