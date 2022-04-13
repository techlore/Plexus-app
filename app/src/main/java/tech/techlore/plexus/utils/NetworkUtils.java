package tech.techlore.plexus.utils;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {

    private static final OkHttpClient okHttpClient = new OkHttpClient();

    // CHECK NETWORK AVAILABILITY
    public static boolean HasNetwork(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED);

    }

    // CHECK IF NETWORK HAS INTERNET CONNECTION
    // MUST BE CALLED IN BACKGROUND THREAD
    public static boolean HasInternet() {

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("github.com", 443), 1500);
            socket.close();

            return true;
        }
        catch (IOException e) {
            return false;
        }

    }

    public static String URLResponse() throws IOException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/parveshnarwal/Plexus-Demo/main/new.json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute())
        {
            return Objects.requireNonNull(response.body()).string();
        }

    }

}
