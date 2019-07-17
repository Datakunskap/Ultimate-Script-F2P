package script.tanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.ui.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExPriceChecker {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static JsonObject OSBUDDY_SUMMARY_JSON;
    private static final OkHttpClient HTTP_CLIENT_2 = new OkHttpClient();
    private static JsonObject RSBUDDY_SUMMARY_JSON;

    private static void setRSBuddySummaryJson() throws IOException {
        final Request request = new Request.Builder()
                .url("https://rsbuddy.com/exchange/summary.json")
                .get()
                .build();
        final Response response = HTTP_CLIENT_2.newCall(request).execute();
        if (!response.isSuccessful())
            return;

        if (response.body() == null)
            return;

        final Gson gson = new Gson().newBuilder().create();
        RSBUDDY_SUMMARY_JSON = gson.fromJson(response.body().string(), JsonObject.class);
    }

    public static int getRSBuddySellPrice(int id, boolean refresh) throws IOException {
        if (RSBUDDY_SUMMARY_JSON == null || refresh)
            setRSBuddySummaryJson();

        final JsonObject json_objects = RSBUDDY_SUMMARY_JSON.getAsJsonObject(Integer.toString(id));
        if (json_objects == null)
            return 0;

        return json_objects.get("sell_average").getAsInt();
    }

    public static int getRSBuddyBuyPrice(int id, boolean refresh) throws IOException {
        if (RSBUDDY_SUMMARY_JSON == null || refresh)
            setRSBuddySummaryJson();

        final JsonObject json_objects = RSBUDDY_SUMMARY_JSON.getAsJsonObject(Integer.toString(id));
        if (json_objects == null)
            return 0;

        return json_objects.get("buy_average").getAsInt();
    }

    /**
     * Sets the OSBuddy price summary json.
     */
    private static void setOSBuddySummaryJson() throws IOException {
        final Request request = new Request.Builder()
                .url("https://storage.googleapis.com/osbuddy-exchange/summary.json")
                .get()
                .build();
        final Response response = HTTP_CLIENT.newCall(request).execute();
        if (!response.isSuccessful())
            return;

        if (response.body() == null)
            return;

        final Gson gson = new Gson().newBuilder().create();
        OSBUDDY_SUMMARY_JSON = gson.fromJson(response.body().string(), JsonObject.class);
    }

    /**
     * Gets the price of the item id from the OSBuddy price summary json. The entire summary data is stored upon first
     * retrieval.
     *
     * @param id The id of the item.
     * @return The price of the item; 0 otherwise.
     */
    public static int getOSBuddySellPrice(int id, boolean refresh) throws IOException {
        if (OSBUDDY_SUMMARY_JSON == null || refresh)
            setOSBuddySummaryJson();

        final JsonObject json_objects = OSBUDDY_SUMMARY_JSON.getAsJsonObject(Integer.toString(id));
        if (json_objects == null)
            return 0;

        return json_objects.get("sell_average").getAsInt();
    }

    public static int getOSBuddyBuyPrice(int id, boolean refresh) throws IOException {
        if (OSBUDDY_SUMMARY_JSON == null || refresh)
            setOSBuddySummaryJson();

        final JsonObject json_objects = OSBUDDY_SUMMARY_JSON.getAsJsonObject(Integer.toString(id));
        if (json_objects == null)
            return 0;

        return json_objects.get("buy_average").getAsInt();
    }
}