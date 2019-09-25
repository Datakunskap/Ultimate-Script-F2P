package script.automation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import script.automation.data.LaunchedClient;
import script.automation.data.Launcher;
import script.automation.data.QuickLaunch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Management {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    public static boolean startDefaultClient(int pcIndex) throws Exception {
        return startClient(pcIndex,
                null,
                "-Xmx768m -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Xss2m",
                10,
                null,
                1
        );
    }

    public static boolean startClient(int pcIndex, int sleep, String proxy, int count) throws Exception {
        return startClient(pcIndex, "", sleep, proxy, count);
    }

    public static boolean startClient(int pcIndex, int sleep, int count) throws Exception {
        return startClient(pcIndex, "", sleep, "", count);
    }

    public static boolean startClient(int pcIndex, String qs, int sleep, String proxy, int count) throws Exception {
        return startClient(
                pcIndex,
                qs,
                "-Xmx768m -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Xss2m",
                sleep,
                proxy,
                count
        );
    }

    public static boolean startClient(int pcIndex, String qs, String jvmArgs, int sleep, String proxy, int count) throws Exception {
        final String apiKey = Authentication.getApiKey();
        if (apiKey.isEmpty())
            throw new FileNotFoundException("Could not find api key file");

        List<Launcher> launchers = getLaunchers(10);

        final Headers headers = new Headers.Builder()
                .add("ApiClient", apiKey)
                .add("Content-Type", "application/json")
                .build();

        String body = "{\"payload\":" +
                "{" +
                "\"type\":\"start:client\"," +
                "\"session\":\"" + apiKey + "\"," +
                "\"qs\":" + (qs == null ? "null" : qs) + "," +
                "\"jvmArgs\":\"" + jvmArgs + "\"," +
                "\"sleep\":" + sleep + "," +
                "\"proxy\":" + proxy + "" +
                (count > 0 ? ",\"count\":" + count : "") +
                "}," +
                "\"socket\":\"" + launchers.get(pcIndex).getSocketAddress() + "\"" +
                "}";

        final RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                body
        );

        final Request request = new Request.Builder()
                .url("https://services.rspeer.org/api/botLauncher/send")
                .headers(headers)
                .post(requestBody)
                .build();

        final Response response = HTTP_CLIENT.newCall(request).execute();
        return response.isSuccessful();
    }

    public static boolean addProxy(String name, String ip, String port, String username, String password) throws Exception {
        final String apiKey = Authentication.getApiKey();
        if (apiKey.isEmpty())
            throw new FileNotFoundException("Could not find api key file");

        final Headers headers = new Headers.Builder()
                .add("ApiClient", apiKey)
                .add("Content-Type", "application/json")
                .build();

        final String body =
                "{" +
                        "\"Name\":\"" + name + "\"," +
                        "\"Ip\":\"" + ip + "\"," +
                        "\"Port\":\"" + port + "\"," +
                        "\"Username\":\"" + username + "\"," +
                        "\"Password\":\"" + password + "\"" +
                        "}";

        final RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                body
        );

        final Request request = new Request.Builder()
                .url("https://services.rspeer.org/api/botLauncher/saveProxy")
                .headers(headers)
                .post(requestBody)
                .build();

        final Response response = HTTP_CLIENT.newCall(request).execute();
        return response.isSuccessful();
    }

    public static List<QuickLaunch> getQuickLaunchers() throws IOException {
        final String apiKey = Authentication.getApiKey();
        if (apiKey.isEmpty())
            throw new FileNotFoundException("Could not find api key file");

        final Request request = new Request.Builder()
                .url("https://services.rspeer.org/api/botLauncher/getQuickLaunch")
                .header("ApiClient", apiKey)
                .get()
                .build();

        final Response response = HTTP_CLIENT.newCall(request).execute();

        if (!response.isSuccessful())
            return Collections.emptyList();

        final ResponseBody body = response.body();
        if (body == null)
            return Collections.emptyList();

        final Gson gson = new Gson().newBuilder().create();
        final Type clientType = new TypeToken<List<QuickLaunch>>() {
        }.getType();

        return gson.fromJson(body.string(), clientType);
    }

    public static List<LaunchedClient> getRunningClients(String API_KEY) throws IOException {
        //final String apiKey = Authentication.getApiKey();
        if (API_KEY.isEmpty())
            throw new FileNotFoundException("Could not find api key file");

        final Request request = new Request.Builder()
                .url("https://services.rspeer.org/api/botLauncher/connectedClients")
                .header("ApiClient", API_KEY)
                .get()
                .build();

        final Response response = HTTP_CLIENT.newCall(request).execute();

        if (!response.isSuccessful())
            return Collections.emptyList();

        final ResponseBody body = response.body();
        if (body == null)
            return Collections.emptyList();

        final Gson gson = new Gson().newBuilder().create();
        final Type type = new TypeToken<List<LaunchedClient>>() {
        }.getType();
        return gson.fromJson(body.string(), type);
    }

    public static List<Launcher> getLaunchers(int retries) throws IOException, InterruptedException {
        final String apiKey = Authentication.getApiKey();
        if (apiKey.isEmpty())
            throw new FileNotFoundException("Could not find api key file");

        final Request request = new Request.Builder()
                .url("https://services.rspeer.org/api/botLauncher/connected")
                .header("ApiClient", apiKey)
                .get()
                .build();

        final Response response = HTTP_CLIENT.newCall(request).execute();

        if (!response.isSuccessful())
            return Collections.emptyList();

        final ResponseBody body = response.body();
        if (body == null)
            return Collections.emptyList();

        final Gson gson = new Gson().newBuilder().create();
        final JsonObject jsonObject = gson.fromJson(body.string(), JsonObject.class);
        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        final List<Launcher> launchers = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : entries) {
            Launcher launcher = gson.fromJson(entry.getValue().getAsJsonObject(), Launcher.class);
            launcher.setSocketAddress(entry.getKey());
            launchers.add(launcher);
        }

        if (launchers.size() < 1) {
            if (retries > 0) {
                Thread.sleep(10000);
                getLaunchers(retries - 1);
            } else {
                throw new IOException("Failed Getting Launcher");
            }
        }
        return launchers;
    }
}
