import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;



public class EllipseModel extends ShapeModel {
	public EllipseModel(Point startPoint, Point endPoint) {
		super(startPoint, endPoint);
		Rectangle rect = new java.awt.Rectangle(startPoint);
		rect.add(endPoint);
		this.shape = new Ellipse2D.Double(rect.x, rect.y, rect.width, rect.height);
	}

	
	@Override
	public ShapeType this_type() {
		return ShapeType.Ellipse;
	}
}
