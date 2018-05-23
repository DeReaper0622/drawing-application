import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;



public class RectangleModel extends ShapeModel {

	public RectangleModel(Point startPoint, Point endPoint) {
		super(startPoint, endPoint);
		Point top_left = new Point(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y));
		Point bottom_right = new Point(Math.max(startPoint.x, endPoint.x), Math.max(startPoint.y, endPoint.y));
		Rectangle2D rect = new Rectangle2D.Double(top_left.x, top_left.y, bottom_right.x - top_left.x,
				bottom_right.y - top_left.y);

		this.shape = rect;
	}

	@Override
	public ShapeType this_type() {
		return ShapeType.Rectangle;
	}

}
