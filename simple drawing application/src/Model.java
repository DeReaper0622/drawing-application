
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;

public class Model {
	/** The observers that are watching this model for changes. */
	private List<Observer> observers;

	public Boolean selection_on = false;
	public String shape_mode = "Freeform";
	public ArrayList<Point> current_shape = new ArrayList<Point>();
	public Shape current_item = new GeneralPath();
	public ArrayList<Paint_item> item_collection = new ArrayList<Paint_item>();
	public Paint_item select_shape;
	public int select_index = -1;
	public Rectangle bound = new Rectangle();;
	public int current_stroke = 1;
	public Color current_stroke_color = Color.BLACK;
	public Color current_fill_color = Color.WHITE;

	/**
	 * Create a new model.
	 */
	public Model() {
		this.observers = new ArrayList<Observer>();
	}

	/**
	 * Add an observer to be notified when this model changes.
	 */
	public void addObserver(Observer observer) {
		this.observers.add(observer);
	}

	/**
	 * Remove an observer from this model.
	 */
	public void removeObserver(Observer observer) {
		this.observers.remove(observer);
	}

	/**
	 * Notify all observers that the model has changed.
	 */
	public void notifyObservers() {
		for (Observer observer : this.observers) {
			observer.update(this);
		}
	}

	public void add_point(Point e) {
		current_shape.add(e);
		if (shape_mode == "Freeform") {
			GeneralPath freeform = new GeneralPath(GeneralPath.WIND_EVEN_ODD, current_shape.size());
			if (current_shape.size() > 0) {
				freeform.moveTo(current_shape.get(0).x, current_shape.get(0).y);
				for (int i = 1; i < current_shape.size(); i++) {
					freeform.lineTo(current_shape.get(i).x, current_shape.get(i).y);
				}
			}
			current_item = freeform;
		} else if (shape_mode == "Straight line") {
			Line2D myline = new Line2D.Double();
			if (current_shape.size() > 2) {
				int last_point = current_shape.size() - 1;
				Point2D start = (Point2D) current_shape.get(0);
				Point2D end = (Point2D) current_shape.get(last_point);
				myline.setLine(start, end);
			}
			current_item = myline;
		} else if (shape_mode == "Rectangle") {
			Rectangle my_rect = new Rectangle();
			if (current_shape.size() > 2) {
				int last_point = current_shape.size() - 1;
				Point2D start = (Point2D) current_shape.get(0);
				Point2D end = (Point2D) current_shape.get(last_point);
				my_rect.setFrameFromDiagonal(start, end);
			}
			current_item = my_rect;
		} else if (shape_mode == "Ellipse") {
			Ellipse2D my_ellipse = new Ellipse2D.Double();
			if (current_shape.size() >= 2) {
				int last_point = current_shape.size() - 1;
				Point2D start = (Point2D) current_shape.get(0);
				Point2D end = (Point2D) current_shape.get(last_point);
				my_ellipse.setFrameFromDiagonal(start, end);
			}
			current_item = my_ellipse;
		}
	}

	public void finish_painting() {
		Paint_item current = new Paint_item();
		current.shape_mode = this.shape_mode;
		current.stroke = this.current_stroke;
		current.stroke_color = this.current_stroke_color;
		current.fill_color = this.current_fill_color;
		if (this.shape_mode == "Freeform") {
			current.shape = this.current_shape;
		}
		current.paint_shape = this.current_item;
		if (this.current_item.getBounds2D().getWidth() != 0) {
			item_collection.add(current);
		}
		this.current_shape = new ArrayList<Point>();
		this.current_item = new GeneralPath();
	}

	public void check_selected(Point p) {
		Point2D p2 = (Point2D) p;
		int last = item_collection.size() - 1;
		bound = new Rectangle();
		for (int i = last; i >= 0; i--) {
			// transform the point to check
			Point2D temp = new Point2D.Double(p2.getX(), p2.getY());// stores the temp value of p2
			AffineTransform IAT;
			try {
				IAT = item_collection.get(i).transform.createInverse();
				IAT.transform(temp, temp);
			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//

			// rectangle select check
			if (item_collection.get(i).shape_mode == "Rectangle") {
				Rectangle rectangle = (Rectangle) item_collection.get(i).paint_shape;
				if (rectangle.contains(temp)) {
					select_shape = item_collection.get(i);
					bound = rectangle.getBounds();
					select_index = i;
					bound_resize();
					break;
				}
			}
			// ellipse select check
			else if (item_collection.get(i).shape_mode == "Ellipse") {
				Ellipse2D ellipse = (Ellipse2D) item_collection.get(i).paint_shape;
				if (ellipse.contains(temp)) {
					select_shape = item_collection.get(i);
					bound = ellipse.getBounds();
					select_index = i;
					bound_resize();
					break;
				}

			}

			// line select check
			else if (item_collection.get(i).shape_mode == "Straight line") {
				Line2D myline = (Line2D) item_collection.get(i).paint_shape;
				double distance = myline.ptSegDist(temp);
				if (distance <= 7) {
					select_shape = item_collection.get(i);
					bound = myline.getBounds();
					select_index = i;
					bound_resize();
					break;
				}
			}

			// freeform select check
			else if (item_collection.get(i).shape_mode == "Freeform") {
				GeneralPath freeform = (GeneralPath) item_collection.get(i).paint_shape;
				// check if it is inside the bounding box
				Rectangle bounding_box = freeform.getBounds();
				if (bounding_box.contains(temp.getX(), temp.getY())) {
					Boolean hit = false;
					int end_point = item_collection.get(i).shape.size() - 2;
					for (int k = 0; k < end_point; k++) {
						Point start = item_collection.get(i).shape.get(k);
						Point end = item_collection.get(i).shape.get(k + 1);
						double distance = Line2D.ptSegDist(start.getX(), start.getY(), end.getX(), end.getY(),
								temp.getX(), temp.getY());
						if (distance <= 7) {
							hit = true;
							break;
						}
					}
					if (hit == true) {// if there is hit
						select_shape = item_collection.get(i);
						bound = bounding_box;
						select_index = i;
						bound_resize();
						break;
					}
				}
			}
		}

	}

	public void bound_resize() {
		bound.setRect(bound.getX() - 5, bound.getY() - 5, bound.getWidth() + 10, bound.getHeight() + 10);
	}

	public void bound_reset() {
		bound = new Rectangle();
		select_index = -1;
	}

	public void new_collection() {// clears the shape collection
		item_collection = new ArrayList<Paint_item>();
	}

	public void delete_selected() {// deletes the selected shape
		if (select_index > -1) {
			item_collection.remove(select_index);
		}
		bound_reset();
	}

	public void set_transformation(int x_trans, int y_trans, int rotation, double x_scale, double y_scale) {
		if (select_index > -1) {
			double center_x = bound.getCenterX();
			double center_y = bound.getCenterY();
			AffineTransform new_transform = new AffineTransform();
			new_transform.translate(center_x, center_y);
			new_transform.translate(x_trans, y_trans);
			new_transform.rotate(Math.toRadians(rotation));
			new_transform.scale(x_scale, y_scale);
			new_transform.translate(-center_x, -center_y);
			item_collection.get(select_index).transform = new_transform;
		}

	}

	public void change_stroke(int new_stroke) {
		current_stroke = new_stroke;
		if (select_index > -1) {
			item_collection.get(select_index).stroke = new_stroke;
		}

	}

	public void change_color(Color storke_color, Color fill_color) {
		current_stroke_color = storke_color;
		current_fill_color = fill_color;
		if (select_index > -1) {
			item_collection.get(select_index).stroke_color = storke_color;
			item_collection.get(select_index).fill_color = fill_color;
		}

	}

	public void move_to(int x_trans, int y_trans, Point e) {
		if (select_index > -1 && bound.contains(e)) {// if there is shape selected and the new position
														// is within shape
			String type = item_collection.get(select_index).shape_mode;
			if (!item_collection.get(select_index).transform.isIdentity()) {// if the selected shape is transformed
				AffineTransform new_transform = new AffineTransform();
				new_transform.translate(x_trans, y_trans);
				new_transform.concatenate(item_collection.get(select_index).transform);
				new_transform.translate(-x_trans, -y_trans);
				item_collection.get(select_index).transform = new_transform;
				// move it back to its orignial position apply the transform then move to
				// current location
			}

			if (type == "Rectangle") {// if we are moving a rectangle
				Rectangle my_rect = (Rectangle) item_collection.get(select_index).paint_shape;
				my_rect.setLocation(my_rect.x + x_trans, my_rect.y + y_trans);// reset the location of the rectangle
				bound = my_rect.getBounds();
				bound_resize();// reset the bound of the rectangle
			}

			else if (type == "Ellipse") {// if we are moving a ellipse
				Ellipse2D my_ellipse = (Ellipse2D) item_collection.get(select_index).paint_shape;
				my_ellipse.setFrame(my_ellipse.getX() + x_trans, my_ellipse.getY() + y_trans, my_ellipse.getWidth(),
						my_ellipse.getHeight());
				bound = my_ellipse.getBounds();
				bound_resize();
			}

			else if (type == "Straight line") {// if we are moving a line
				Line2D my_line = (Line2D) item_collection.get(select_index).paint_shape;
				my_line.setLine(my_line.getX1() + x_trans, my_line.getY1() + y_trans, my_line.getX2() + x_trans,
						my_line.getY2() + y_trans);
				bound = my_line.getBounds();
				bound_resize();
			} else if (type == "Freeform") {
				GeneralPath new_freeform = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
						item_collection.get(select_index).shape.size());
				double new_x = item_collection.get(select_index).shape.get(0).x + x_trans;
				double new_y = item_collection.get(select_index).shape.get(0).y + y_trans;
				item_collection.get(select_index).shape.get(0).setLocation(new_x, new_y);
				//updating the points that constructs the freeform
				new_freeform.moveTo(new_x, new_y);
				for (int i = 1; i < item_collection.get(select_index).shape.size(); i++) {
					new_x = item_collection.get(select_index).shape.get(i).x + x_trans;
					new_y = item_collection.get(select_index).shape.get(i).y + y_trans;
					//updating the points that constructs the freeform
					item_collection.get(select_index).shape.get(i).setLocation(new_x, new_y);
					new_freeform.lineTo(new_x, new_y);
				}
				item_collection.get(select_index).paint_shape = new_freeform;//replacing the old freeform to new freeform
				bound = new_freeform.getBounds();
				bound_resize();

			}
		}

	}
}

class Paint_item {
	public String shape_mode;
	public ArrayList<Point> shape;
	public Shape paint_shape;
	public AffineTransform transform = new AffineTransform();
	public int stroke = 1;
	public Color stroke_color;
	public Color fill_color;

}