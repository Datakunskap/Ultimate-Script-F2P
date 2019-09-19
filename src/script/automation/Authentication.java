package script.automation;

import script.Beggar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class Authentication {

    public static String getApiKey() throws IOException {
        /*final File apiKeyFile = AutomationFileHelper.getApiKeyFile();
        if (!apiKeyFile.exists())
            throw new FileNotFoundException("No api key file found");

        return Files.lines(apiKeyFile.toPath()).findFirst().orElse("");*/
        return Beggar.API_KEY;
    }
}
