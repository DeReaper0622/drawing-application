import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;



public class LineModel extends ShapeModel {

	Point a;
	Point b;

	public LineModel(Point startPoint, Point endPoint) {
		super(startPoint, endPoint);
		this.a = startPoint;
		this.b = endPoint;

		Path2D path = new Path2D.Double();
		path.moveTo(startPoint.x, startPoint.y);
		path.lineTo(endPoint.x, endPoint.y);
		this.shape = path;
	}

	@Override
	public boolean hitTest(Point2D p) {
		return pointToLineDistance(a, b, (Point) p) < 10;
	}

	public double pointToLineDistance(Point A, Point B, Point P) {
		double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
		return Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
	}

	@Override
	public void move(double dx, double dy) {
		AffineTransform my_trans = new AffineTransform();
		my_trans.translate(dx, dy);
		this.shape = my_trans.createTransformedShape(this.shape);
		this.a.translate((int) dx, (int) dy);
		this.b.translate((int) dx, (int) dy);
		this.end_point.translate((int) dx, (int) dy);
	}

	public void scale(Point p) {
		Point2D trans_point = new Point2D.Double(p.x,p.y);
		// get the rotation
		double rotation = this.rotation - Math.PI / 2;

		// get the original center
		double o_centerx = this.getShape().getBounds2D().getCenterX();
		double o_centery = this.getShape().getBounds2D().getCenterY();

		// create the inverse rotation about the center
		AffineTransform at1 = new AffineTransform();
		at1.rotate(rotation, o_centerx, o_centery);
		try {
			at1.inverseTransform(p, trans_point);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AffineTransform at2 = new AffineTransform();
		at2.rotate(-rotation,o_centerx,o_centery);
		Rectangle2D bound = at2.createTransformedShape(shape).getBounds2D();
		double old_width = bound.getWidth();
		double old_height = bound.getHeight();
		double updated_width = Math.abs((trans_point.getX()-bound.getCenterX())*2/old_width);
		double updated_height = Math.abs((trans_point.getY()-bound.getCenterY())*2/old_height);
		
		AffineTransform at3 = new AffineTransform();
		at3.translate(o_centerx,o_centery);
		at3.rotate(rotation);
		at3.scale(updated_width, updated_height);
		at3.rotate(-rotation);
		at3.translate(-o_centerx,-o_centery);
		shape = at3.createTransformedShape(shape);
		at3.transform(a, a);
		at3.transform(b, b);//transform both a and b
		
	}

	@Override
	public void rotate(double theta, double anchor_x, double anchor_y) {
		AffineTransform at = new AffineTransform();
		at.rotate(theta, anchor_x, anchor_y);
		this.shape = at.createTransformedShape(this.shape);
		at.transform(a, a);
		at.transform(b, b);
		at.transform(end_point, end_point);
	}
	@Override
	public ShapeType this_type() {
		return ShapeModel.ShapeType.Line;
	}
	@Override
	public Point get_point_a() {
		return new Point(this.a.x,this.a.y);
	}
	
	@Override
	public Point get_point_b() {
		return new Point(this.b.x,this.b.y);
	}
}