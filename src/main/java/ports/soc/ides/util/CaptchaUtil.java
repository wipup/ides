package ports.soc.ides.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Objects;

import org.primefaces.PrimeFaces;
import org.primefaces.json.JSONObject;

public class CaptchaUtil {

	public static void reinitCaptcha(PrimeFaces p) {
		Objects.requireNonNull(p);
		p.executeScript("grecaptcha.reset();");
	}
	
	public static boolean validateCaptcha(String token, String privateKey) throws MalformedURLException, IOException {
		Objects.requireNonNull(token);
		Objects.requireNonNull(privateKey);
		
		boolean success = false;

		URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(25*1000);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		StringBuilder params = new StringBuilder();
		params.append("secret=").append(URLEncoder.encode(privateKey, "UTF-8"));
		params.append("&response=").append(URLEncoder.encode(token, "UTF-8"));

		try (OutputStream out = conn.getOutputStream()) {
			out.write(params.toString().getBytes());
			out.flush();
		}

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = rd.readLine()) != null) {
				response.append(inputLine);
			}
			JSONObject json = new JSONObject(response.toString());
			success = json.getBoolean("success");
		}

		return success;
	}

}
