package com.phuston.android.kyzr;

/**
 * Created by andrew on 4/18/15.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class NetworksClient {

    private URL sendURL;

    public NetworksClient() throws MalformedURLException {
        sendURL = new URL("http://www.thekyzrproject.com/dbadd");
    }

    public String access(String query) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) sendURL.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(query.length()));

        OutputStream os = connection.getOutputStream();
        os.write(query.getBytes());
        os.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line = "";
        String finalResponse = "";

        while((line = br.readLine()) != null) {
            finalResponse += line;
        }

        return finalResponse;

    }

    public String formatRequest(String id1, String id2, double lat, double lng) throws UnsupportedEncodingException {
        String[] data = {id1, id2, Double.toString(lat), Double.toString(lng)};
        String[] prefixes = {"id1=", "&id2=", "&lat=", "&lng="};
        String encodedData = "";

        for(int i = 0; i < 4; i++) {
            String encode = URLEncoder.encode(data[i], "UTF-8");
            encodedData += prefixes[i] + encode;
        }

        return encodedData;
    }

}
