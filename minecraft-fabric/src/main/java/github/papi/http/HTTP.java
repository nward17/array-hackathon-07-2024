package github.papi.http;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HTTP {

    private static final Logger LOGGER = LoggerFactory.getLogger("papi");
    private static final OkHttpClient httpClient = new OkHttpClient();

    public static String search(String username) throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:3000/search?username=" + username)
                .build();

        return call(request);
    }

    public static String optOut(String username) throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:3000/optOut?username=" + username)
                .build();

        return call(request);
    }

    private static String call(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                LOGGER.error("Request failed: {}", response);
                throw new IOException("Unexpected code " + response);
            }
        }
    }
}
