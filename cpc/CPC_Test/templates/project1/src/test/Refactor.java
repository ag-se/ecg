package test;

public class Refactor
{
	private static String xyz = "123";
	
	public static void main(String[] args)
	{
		bla();
		blubb();
		bla();
	}
	
	private static void bla()
	{
		System.out.println("xyz: "+xyz);
	}
	
	private static void blubb()
	{
		xyz = "abc";
	}

	public static String getXyz()
	{
		return xyz;
	}
	
	public static void setXyz(String xyz2)
	{
		xyz = xyz2;
	}
}
