package assignment;

import java.util.Base64;
import java.io.InputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;


/** Scans twitter for mentions of key words and reports on their proportional
 * changes over time */
public class TwitterScanner
{
  private static String url = "https://stream.twitter.com/1.1/statuses/sample.json";

  // TODO: fill in these values with your own ones.
  private static String consumerKey = "???";
  private static String consumerSecret = "???";
  private static String token = "???";
  private static String tokenSecret = "???";

  private String companyName;
  private int interval;

  /** Stores a given value as it was at a given time */
  public static class TSValue
  {
    private final Instant timestamp;
    private final double val;

    /** constructor which takes a timestamp and a value and sets them
     * permanently.
     * @param timestamp is the time that this value corresponds to
     * @param val is the value stored */
    public TSValue(Instant timestamp, double val)
    {
      this.timestamp = timestamp;
      this.val = val;
    }

    /** gives you the timestamp
     * @return timestamp */
    public Instant getTimestamp()
    {
      return timestamp;
    }

    /** gives you the value
     * @return value */
    public double getVal()
    {
      return val;
    }

    public String toString()
    {
      return "Time: "+timestamp+", Value: "+val;
    }
  }


  /** Constructor which tells the scanner what keyword it will be seeking
   * @param companyName is the keyword */
  public TwitterScanner(String companyName, int interval)
  {
    this.companyName = companyName.toLowerCase();
    this.interval = interval;
  }

  /** Makes the scanner start scanning */
  public void run()
  {
    // Generate ID for request.
    String nonce = UUID.randomUUID().toString();
    String timestamp = Long.toString(System.currentTimeMillis() / 1000);

    // Generate signature for request.
    String signature;
    try
    {
      signature = buildSignature(nonce, timestamp);
    } catch (NoSuchAlgorithmException e)
    {
      System.err.println("The hashing algorithm was unavailable.");
      return;
    } catch (InvalidKeyException e)
    {
      System.err.println("Key is invalid.");
      return;
    }

    // Build Authorisation String.
    ParameterStringBuilder authKeys = new ParameterStringBuilder(", ", true);
    authKeys.addParameter("oauth_consumer_key", consumerKey);
    authKeys.addParameter("oauth_nonce", nonce);
    authKeys.addParameter("oauth_signature", signature);
    authKeys.addParameter("oauth_signature_method", "HMAC-SHA1");
    authKeys.addParameter("oauth_timestamp", timestamp);
    authKeys.addParameter("oauth_token", token);
    authKeys.addParameter("oauth_version", "1.0");

    // Build the headers for the request to twitter.
    URL u;
    HttpURLConnection c;
    try
    {
      u = new URL(url);
      c = (HttpURLConnection)u.openConnection();
      c.setRequestProperty("Authorization", "OAuth " + authKeys.toString());
      c.setRequestProperty("User-Agent", "OAuth gem v0.4.4");
      InputStream is = c.getInputStream();

      int previous = 0;
      while (true)
      {
        int count = 0;
        long time = System.currentTimeMillis();

        while (System.currentTimeMillis() < time + interval)
        {
          JsonObject status = Json.createReader(is).readObject();
          if (status.get("text") != null)
          {
            String text = status.getString("text");
            if (text.toLowerCase().contains(companyName)) count++;
          }
        }
        if (previous != 0)
        {
          float proportion = (float)count / previous;
          storeValue(new TSValue(Instant.now(), proportion));
        }
        previous = count;
      }
    } catch (IOException e)
    {
      System.err.println(e);
      return;
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /** "Stores" a given TSValue by sending it to STDOUT
   * @param value is the value to be stored */
  private void storeValue(TSValue value)
  {
    System.out.println(value);
  }

  /** Creates the cryptographic signature that twitter needs to prove there was no man in the
   * middle.
   * @param nonce is the unique id of the request being built for
   * @param timestamp is the time of the request
   * @return the signature as a base64 string */
  public static String buildSignature(String nonce, String timestamp)
  throws NoSuchAlgorithmException, InvalidKeyException
  {
    // Build the parameter string.
    ParameterStringBuilder parameters = new ParameterStringBuilder();
    parameters.addParameter("oauth_consumer_key", consumerKey);
    parameters.addParameter("oauth_nonce", nonce);
    parameters.addParameter("oauth_signature_method", "HMAC-SHA1");
    parameters.addParameter("oauth_timestamp", timestamp);
    parameters.addParameter("oauth_token", token);
    parameters.addParameter("oauth_version", "1.0");

    // Build the signature base string.
    StringBuilder signature = new StringBuilder();
    signature.append("GET&");
    signature.append(ParameterStringBuilder.percentEncode(url));
    signature.append("&");
    signature.append(ParameterStringBuilder.percentEncode(parameters.toString()));

    // Build the signing key
    StringBuilder signingKey = new StringBuilder();
    signingKey.append(ParameterStringBuilder.percentEncode(consumerSecret));
    signingKey.append("&");
    signingKey.append(ParameterStringBuilder.percentEncode(tokenSecret));

    // Build the actual cryptographic signature and return it as base64.
    SecretKeySpec key = new SecretKeySpec(signingKey.toString().getBytes(), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(key);
    byte[] raw = mac.doFinal(signature.toString().getBytes());
    return Base64.getEncoder().encodeToString(raw);
  }

  /** Entry point to application
   * @param args is array of commandline arguments which are not used */
  public static void main(String[] args)
  {
    String companyName = "facebook";
    int interval = 60 * 60 * 1000;
    System.out.println("Starting scanner for word '"+companyName+"', and interval "+interval+".");
    TwitterScanner scanner = new TwitterScanner(companyName, interval);
    scanner.run();
  }
}
