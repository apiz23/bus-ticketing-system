package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpsRequest {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void sendEmailReceipt(int bookId) {
        try {
            String endpoint = "https://jg160007-api.vercel.app/bus-ticketing-receipt/send-receipt/" + bookId;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Failed to send request. Status code: " + response.statusCode());
                System.out.println("Response body: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while sending the request.");
        }
    }
}
