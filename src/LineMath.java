import java.awt.Point;

public class LineMath
{
	public static double DistanceToLine(Point pt, Point l1, Point l2)
	{
		double AB = pt.distance(l1);
		double BC = l1.distance(l2);
		double AC = pt.distance(l2);

		double s = (AB + BC + AC) / 2;
		double area = (float) Math.sqrt(s * (s - AB) * (s - BC) * (s - AC));

		double AD = (2 * area) / BC;
	    return AD;
	}
	public static double Angle(Point p1, Point p2)
	{
		return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
	}
	public static double HypotenuseLength(double x, double y)
	{
		return Math.sqrt(x * x + y * y);
	}
}
