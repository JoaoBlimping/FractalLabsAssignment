package assignment;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/** Builds ordered strings of key value pairs that have been percent escaped */
public class ParameterStringBuilder
{
  private Map<String, String> parameters = new HashMap<String, String>();
  private String joiner = "&";
  private boolean quote = false;

  /** default constructor */
  public ParameterStringBuilder(){}

  /** special constructor for abnormal formatting
   * @param joiner is the string that will be placed between key value pairs
   * @param quote is whether values should be placed within quotes */
  public ParameterStringBuilder(String joiner, boolean quote)
  {
    this.joiner = joiner;
    this.quote = quote;
  }

  /** add a key value pair to the list to be written in the string
   * @param key is the key added
   * @param value is the corresponding value added */
  public void addParameter(String key, String value)
  {
    parameters.put(percentEncode(key), percentEncode(value));
  }

  /** Converts the whole set of parameters as they are currently into a string
   * @return the string of key value pairs percent escaped */
  public String toString()
  {
    StringBuilder out = new StringBuilder();
    List<String> keys = new ArrayList<String>(parameters.keySet());
    Collections.sort(keys);
    for (String key: keys)
    {
      out.append(key);
      out.append("=");
      if (quote) out.append("\"");
      out.append(parameters.get(key));
      if (quote) out.append("\"");
      out.append(joiner);
    }
    if (keys.size() > 0) out.deleteCharAt(out.length() - joiner.length());
    return out.toString();
  }

  /** Percent encoding implementation according to RFC 3986, Section 2.1.
   * @param input is the string to be encoded
   * @return the percent encoded string */
  public static String percentEncode(String input)
  {
    StringBuilder out = new StringBuilder();
    for (int i = 0;i < input.length();i++)
    {
      char c = input.charAt(i);
      if ((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a) ||
          (c >= 0x2d && c <= 0x2e) || c == 0x5f || c == 0x7e)
      {
        out.append(c);
      }
      else
      {
        out.append('%');
        out.append(String.format("%02X", (int)c));
      }
    }
    return out.toString();
  }
}
