import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

public class ShapeModel {
	Shape shape;
	public double rotation = Math.PI / 2;
	double previous_rotation = Math.PI / 2;// angle of the shape before rotation

	UndoManager undoManager = new UndoManager();
	ShapeUndoable shapeundoable;

	int absX;
	int absY;// location of the center of the shape before move

	Point end_point;

	public ShapeModel(Point startPoint, Point endPoint) {
		absX = (startPoint.x + endPoint.x) / 2;
		absY = (startPoint.y + endPoint.y) / 2;
		end_point=new Point(endPoint.x,endPoint.y);
	}

	public Shape getShape() {
		return shape;
	}

	// You will need to change the hittest to account for transformations.
	public boolean hitTest(Point2D p) {
		return this.getShape().contains(p);
	}

	// move a shape
	public void move(double dx, double dy) {
		AffineTransform at = new AffineTransform();
		at.translate(dx, dy);
		this.shape = at.createTransformedShape(this.shape);
		at.transform(end_point, end_point);
	}

	// end a move of a shape, push the new location and old location to undo stack
	public void end_move() {
		int new_x = (int) shape.getBounds2D().getCenterX();
		int new_y = (int) shape.getBounds2D().getCenterY();
		shapeundoable = new ShapeUndoable(absX, absY, new_x, new_y);
		undoManager.addEdit(shapeundoable);
		absX = new_x;
		absY = new_y;
	}


	public void scale(Point p) {
		Point2D trans_point = new Point2D.Double(p.x,p.y);
		// get the rotation
		double rotation = this.rotation - Math.PI / 2;

		// get the original center
		double o_centerx = shape.getBounds2D().getCenterX();
		double o_centery = shape.getBounds2D().getCenterY();

		AffineTransform at = new AffineTransform();
		at.rotate(rotation, o_centerx, o_centery);
		try {
			at.inverseTransform(trans_point, trans_point);
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
	}

	public void end_scale(Point p) {
		shapeundoable = new ShapeUndoable(new Point(end_point.x,end_point.y),new Point(p.x,p.y));
		undoManager.addEdit(shapeundoable);
		end_point = new Point(p.x,p.y);

	}

	// rotate shape
	public void rotate(double theta, double anchor_x, double anchor_y) {
		AffineTransform at = new AffineTransform();
		at.rotate(theta, anchor_x, anchor_y);
		this.shape = at.createTransformedShape(this.shape);
		at.transform(end_point, end_point);
	}

	public void end_rotate() {
		shapeundoable = new ShapeUndoable(previous_rotation, rotation);
		undoManager.addEdit(shapeundoable);
		previous_rotation = rotation;

	}

	public ShapeType this_type() {
		return null;
	}

	public Point get_point_a() {
		return null;
	}

	public Point get_point_b() {
		return null;
	}

	public void undo() {
		if (undoManager.canUndo()) {
			try {
				undoManager.undo();
			} catch (CannotRedoException ex) {
			}
		}
	}

	public void redo() {
		if (undoManager.canRedo()) {
			try {
				undoManager.redo();
			} catch (CannotRedoException ex) {
			}
		}
	}

	public class ShapeUndoable extends AbstractUndoableEdit {
		boolean translate = false;
		boolean scale = false;
		boolean rotate = false;
		// position for undo
		public int p_translateX = 0;
		public int p_translateY = 0;
		// position for redo
		public int n_translateX = 0;
		public int n_translateY = 0;

		// angle for undo
		public double p_theta = 0;
		// angle for redo
		public double n_theta = 0;

		// scale for undo
		public Point p_point;
		// scale for redo
		public Point n_point;
		

		public ShapeUndoable(int px, int py, int x, int y) {
			translate = true;
			// position for undo
			p_translateX = px;
			p_translateY = py;
			// position for redo
			n_translateX = x;
			n_translateY = y;

		}

		public ShapeUndoable(double old_angle, double new_angle) {
			rotate = true;
			// angle for undo
			p_theta = old_angle;
			// angle for redo
			n_theta = new_angle;
		}

		public ShapeUndoable(Point previous_point, Point new_point) {
			scale = true;
			// scale for undo
			p_point = new Point(previous_point.x,previous_point.y);
			// scale for redo
			n_point = new Point(new_point.x,new_point.y);

		}

		public void undo() throws CannotRedoException {
			super.undo();
			if (translate == true) {
				double x_trans = p_translateX - n_translateX;
				double y_trans = p_translateY - n_translateY;
				move(x_trans, y_trans);
				absX = p_translateX;
				absY = p_translateY;

			} else if (rotate == true) {
				double center_x = shape.getBounds2D().getCenterX();
				double center_y = shape.getBounds2D().getCenterY();
				double rotation_angle = p_theta - n_theta;
				rotate(rotation_angle, center_x, center_y);
				rotation = p_theta;

			} else if (scale == true) {
				scale(p_point);
				end_point=new Point(p_point.x,p_point.y);
			}

		}

		public void redo() throws CannotRedoException {
			super.redo();
			if (translate == true) {
				double x_trans = n_translateX - p_translateX;
				double y_trans = n_translateY - p_translateY;
				move(x_trans, y_trans);
				absX = n_translateX;
				absY = n_translateY;
			} else if (rotate == true) {
				double center_x = shape.getBounds2D().getCenterX();
				double center_y = shape.getBounds2D().getCenterY();
				double rotation_angle = n_theta - p_theta;
				rotate(rotation_angle, center_x, center_y);
				rotation = n_theta;
			} else if (scale == true) {
				scale(n_point);
				end_point=new Point(n_point.x,n_point.y);
			}

		}
	}

	/**
	 * Given a ShapeType and the start and end point of the shape, ShapeFactory
	 * constructs a new ShapeModel using the class reference in the ShapeType enum
	 * and returns it.
	 */
	public static class ShapeFactory {
		public ShapeModel getShape(ShapeType shapeType, Point startPoint, Point endPoint) {
			try {
				Class<? extends ShapeModel> clazz = shapeType.shape;
				Constructor<? extends ShapeModel> constructor = clazz.getConstructor(Point.class, Point.class);

				return constructor.newInstance(startPoint, endPoint);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public enum ShapeType {
		Ellipse(EllipseModel.class), Rectangle(RectangleModel.class), Line(LineModel.class);

		public final Class<? extends ShapeModel> shape;

		ShapeType(Class<? extends ShapeModel> shape) {
			this.shape = shape;
		}
	}
}
