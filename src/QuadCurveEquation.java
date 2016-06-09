import java.awt.geom.QuadCurve2D;

public class QuadCurveEquation implements CurveEquation{
	private QuadCurve2D.Double curve;
	
	public QuadCurveEquation(QuadCurve2D.Double c) {
		curve = c;
	}
	
	public double getX(double t) {
		return curve.x1 * Math.pow((1-t),2) + 2 * curve.ctrlx *(1-t)*t + curve.x2 *t*t;
	}
	public double getY(double t) {
		return curve.y1 * Math.pow((1-t),2) + 2 * curve.ctrly *(1-t)*t + curve.y2 *t*t;

	}

}
