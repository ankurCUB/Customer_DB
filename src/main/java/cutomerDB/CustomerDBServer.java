package cutomerDB;
import cutomerDB.services.BuyerService;
import cutomerDB.services.CredentialsService;
import cutomerDB.services.GetSellerRatingService;
import cutomerDB.services.ShoppingCartServices;
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
