package assignment;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ParameterStringBuilderTest
{
  @Test
  public void testPercentEncode()
  {
    assertEquals(ParameterStringBuilder.percentEncode("hello world"), "hello%20world");
    assertEquals(ParameterStringBuilder.percentEncode(""), "");
    assertEquals(ParameterStringBuilder.percentEncode("HelloWorld"), "HelloWorld");
    assertEquals(ParameterStringBuilder.percentEncode("-._~"), "-._~");
    assertEquals(ParameterStringBuilder.percentEncode("!@#$%^"), "%21%40%23%24%25%5E");
  }

  @Test
  public void testParameterString()
  {
    ParameterStringBuilder p = new ParameterStringBuilder();
    assertEquals(p.toString(), "");
    p.addParameter("z", "hello world");
    assertEquals(p.toString(), "z=hello%20world");
    p.addParameter("b", "hello");
    assertEquals(p.toString(), "b=hello&z=hello%20world");
  }
}
