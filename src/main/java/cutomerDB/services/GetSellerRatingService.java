package cutomer_db.services;

import com.example.DistributedAssignment.cutomer_db.services.GetSellerRatingGrpc;
import com.example.DistributedAssignment.cutomer_db.services.SellerRating;
import com.example.DistributedAssignment.cutomer_db.services.UserID;
import cutomer_db.Utils;
import io.grpc.stub.StreamObserver;

import java.sql.SQLException;

public class GetSellerRatingService extends GetSellerRatingGrpc.GetSellerRatingImplBase {

    @Override
    public void getSellerRating(UserID request, StreamObserver<SellerRating> responseObserver) {
        try {
            SellerRating sellerRating = SellerRating.newBuilder()
                    .setSellerRating(Utils.fetchSellerRatingGRPC(request.getUserId()))
                    .build();
            responseObserver.onNext(sellerRating);
            responseObserver.onCompleted();
        } catch (SQLException exception){
            responseObserver.onError(exception);
            responseObserver.onCompleted();
        }
    }
}
