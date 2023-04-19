package cutomer_db;
import cutomer_db.services.BuyerService;
import cutomer_db.services.CredentialsService;
import cutomer_db.services.GetSellerRatingService;
import cutomer_db.services.ShoppingCartServices;
import io.grpc.Server;
import io.grpc.ServerBuilder;


import java.io.IOException;

public class CustomerDBServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(Utils.CUSTOMER_DB_PORT)
                .addService(new BuyerService())
                .addService(new CredentialsService())
                .addService(new GetSellerRatingService())
                .addService(new ShoppingCartServices())
                .build();
        server.start();
        server.awaitTermination();
    }
}
