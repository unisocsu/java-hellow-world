import java.io.*;
import java.net.*;
import java.util.*;

public class GeminiCli {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: dalvikvm -cp GeminiCli.jar GeminiCli API_KEY \"prompt\"");
            return;
        }

        String apiKey = args[0];
        String prompt = args[1];
        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-2.5-flash:generateContent?key=" +
                URLEncoder.encode(apiKey, "UTF-8")
            );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                "Content-Type", "application/json; charset=UTF-8"
            );
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            String json =
                "{\"contents\":[{\"parts\":[{\"text\":\"" +
                escapeJson(prompt) +
                "\"}]}]}";

            OutputStream out = connection.getOutputStream();
            out.write(json.getBytes("UTF-8"));
            out.flush();
            out.close();

            int code = connection.getResponseCode();
            System.out.println("HTTP " + code);

            InputStream in = code >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (in != null) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8")
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                reader.close();
            }

        } catch (Exception e) {
            System.out.println("ERROR: " + e);
            e.printStackTrace(System.out);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String escapeJson(String s) {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '\\':
                    b.append("\\\\");
                    break;
                case '"':
                    b.append("\\\"");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\t':
                    b.append("\\t");
                    break;
                default:
                    b.append(c);
                    break;
            }
        }

        return b.toString();
    }
}
