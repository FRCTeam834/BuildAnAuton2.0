import java.awt.Point;

public class LineMath
{
	public static double Angle(Point p1, Point p2)
	{
		return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
	}
	public static double HypotenuseLength(double x, double y)
	{
		return Math.sqrt(x * x + y * y);
	}
}
