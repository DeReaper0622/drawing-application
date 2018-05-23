import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class DrawingModel extends Observable {

	private List<ShapeModel> shapes = new ArrayList<>();
	public FocusModel selected_shape;
	UndoManager undoManager = new UndoManager();
	DrawingUndoable drawingUndoable;

	ShapeModel.ShapeType shapeType = ShapeModel.ShapeType.Rectangle;

	ShapeModel last_shape;// stores the last shape that is edited

	public ShapeModel.ShapeType getShape() {
		return shapeType;
	}

	public void setShape(ShapeModel.ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public DrawingModel() {
	}

	public List<ShapeModel> getShapes() {
		return Collections.unmodifiableList(shapes);
	}

	public void addShape(ShapeModel shape) {
		this.shapes.add(shape);
		this.selected_shape = new FocusModel(shape);
		drawingUndoable = new DrawingUndoable(last_shape, shape);
		drawingUndoable.new_shape = true;
		undoManager.addEdit(drawingUndoable);
		this.setChanged();
		this.notifyObservers();
	}

	public void select_check(Point2D p) {
		for (int i = 0; i < shapes.size(); i++) {
			if (shapes.get(i).hitTest(p)) {
				selected_shape = new FocusModel(shapes.get(i));
				selected_shape.update_hanle();
				this.setChanged();
				this.notifyObservers();
				return;
			}
		}
		selected_shape.selected = false;

	}

	public void redo() {
		// selected_shape.selected_shape.redo();
		// selected_shape.update_hanle();
		if (undoManager.canRedo()) {
			try {
				undoManager.redo();
			} catch (CannotRedoException ex) {
			}
		}
		this.setChanged();
		this.notifyObservers();
	}

	public void undo() {
		// selected_shape.selected_shape.undo();
		// selected_shape.update_hanle();;
		if (undoManager.canUndo()) {
			try {
				undoManager.undo();
			} catch (CannotRedoException ex) {
			}
		}
		this.setChanged();
		this.notifyObservers();
	}

	public boolean can_undo() {
		return undoManager.canUndo();
	}

	public boolean can_redo() {
		return undoManager.canRedo();
	}

	public void move_selected(double dx, double dy) {
		selected_shape.selected_shape.move(dx, dy);
		selected_shape.rotation_handle.move(dx, dy);
		selected_shape.scale_handle.move(dx, dy);

	}

	public void selected_endmove() {
		selected_shape.selected_shape.end_move();
		drawingUndoable = new DrawingUndoable(selected_shape.selected_shape, selected_shape.selected_shape);
		undoManager.addEdit(drawingUndoable);
		this.setChanged();
		this.notifyObservers();
	}

	public void scale_selected(Point p) {
		selected_shape.selected_shape.scale(new Point(p.x,p.y));
		selected_shape.update_hanle();
	}

	public void selected_endscale(Point p) {
		selected_shape.selected_shape.end_scale(new Point(p.x,p.y));
		drawingUndoable = new DrawingUndoable(selected_shape.selected_shape, selected_shape.selected_shape);
		undoManager.addEdit(drawingUndoable);
		this.setChanged();
		this.notifyObservers();
	}

	public void rotate_selected(Point p) {
		double anchor_x = selected_shape.selected_shape.getShape().getBounds().getCenterX();
		double anchor_y = selected_shape.selected_shape.getShape().getBounds().getCenterY();
		double theta = Math.atan2(anchor_y - p.getY(), anchor_x - p.getX());

		double rotation = theta - selected_shape.selected_shape.rotation;

		selected_shape.selected_shape.rotate(rotation, anchor_x, anchor_y);
		selected_shape.rotation_handle.rotate(rotation, anchor_x, anchor_y);
		selected_shape.scale_handle.rotate(rotation, anchor_x, anchor_y);
		selected_shape.selected_shape.rotation = theta;
	}

	public void selected_endrotate() {
		selected_shape.selected_shape.end_rotate();
		drawingUndoable = new DrawingUndoable(selected_shape.selected_shape, selected_shape.selected_shape);
		undoManager.addEdit(drawingUndoable);
		this.setChanged();
		this.notifyObservers();
	}

	public void duplicate_selected() {
		ShapeModel shape;
		double rotation = selected_shape.selected_shape.rotation - Math.PI / 2;

		if (this.selected_shape.selected_shape.this_type() == ShapeModel.ShapeType.Line) {// if we are working with line
			Point start_point = selected_shape.selected_shape.get_point_a();
			Point end_point = selected_shape.selected_shape.get_point_b();
			shape = new ShapeModel.ShapeFactory().getShape(selected_shape.selected_shape.this_type(), start_point,
					end_point);

		} else {// if we are duplicating ellipse or rectangle

			// get the center position of the original bound
			double o_centerx = selected_shape.selected_shape.getShape().getBounds2D().getCenterX();
			double o_centery = selected_shape.selected_shape.getShape().getBounds2D().getCenterY();

			// create the inverse rotation
			AffineTransform at1 = new AffineTransform();
			at1.translate(o_centerx, o_centery);
			at1.rotate(-rotation);
			at1.translate(-o_centerx, -o_centery);

			// get the bound of the original shape
			Rectangle2D bound = at1.createTransformedShape(selected_shape.selected_shape.getShape()).getBounds2D();
			// get the start point and end point for the original shape without
			// transformation
			Point start_point = new Point((int) bound.getX(), (int) bound.getY());
			Point end_point = new Point((int) (bound.getX() + bound.getWidth()),
					(int) (bound.getY() + bound.getHeight()));
			shape = new ShapeModel.ShapeFactory().getShape(selected_shape.selected_shape.this_type(),
					(Point) start_point, (Point) end_point);

			// apply the transform to shape
			shape.rotate(rotation, o_centerx, o_centery);
		}
		shape.rotation = rotation + Math.PI / 2;
		shape.absX+=10;
		shape.absY+=10;//update its location so its 10 pixel down and right
		shape.move(10, 10);
		shapes.add(shape);
		this.selected_shape = new FocusModel(shape);
		this.selected_shape.update_hanle();
		drawingUndoable = new DrawingUndoable(last_shape, shape);
		drawingUndoable.new_shape = true;
		undoManager.addEdit(drawingUndoable);
		this.setChanged();
		this.notifyObservers();
	}

	public class DrawingUndoable extends AbstractUndoableEdit {
		boolean new_shape = false;
		// shape for undo
		ShapeModel p_shape;
		// shape for redo
		ShapeModel n_shape;

		public DrawingUndoable(ShapeModel old_shape, ShapeModel new_shape) {
			p_shape = old_shape;
			n_shape = new_shape;
			last_shape=new_shape;

		}

		public void undo() throws CannotUndoException {
			super.undo();
			if (new_shape) {
				shapes.remove(n_shape);
				selected_shape=null;
				if (p_shape != null) {
					last_shape = p_shape;
				} 
			} else {
				last_shape = p_shape;
				n_shape.undo();
				selected_shape = new FocusModel(p_shape);
				selected_shape.update_hanle();
			}

		}

		public void redo() throws CannotRedoException {
			super.redo();
			last_shape = n_shape;
			if (new_shape) {
				shapes.add(n_shape);
				selected_shape = new FocusModel(n_shape);
				selected_shape.update_hanle();
			} else {
				n_shape.redo();
				selected_shape = new FocusModel(n_shape);
				selected_shape.update_hanle();
			}
		}

	}
}

class FocusModel {
	public ShapeModel selected_shape;
	public EllipseModel rotation_handle;
	public RectangleModel scale_handle;
	public boolean selected = true;

	public FocusModel(ShapeModel shape) {
		this.selected_shape = shape;
		Rectangle2D bound = shape.getShape().getBounds2D();
		// create the rotation handle for selected shape
		Point rotation_topleft = new Point((int) bound.getCenterX() - 5, (int) bound.getY() - 15);
		Point rotation_bottomright = new Point((int) bound.getCenterX() + 5, (int) bound.getY() - 5);
		this.rotation_handle = new EllipseModel(rotation_topleft, rotation_bottomright);

		// create the scale handle for selected shape
		Point scale_topleft = new Point((int) bound.getMaxX() - 5, (int) bound.getMaxY() - 5);
		Point scale_bottomright = new Point((int) bound.getMaxX() + 5, (int) bound.getMaxY() + 5);
		this.scale_handle = new RectangleModel(scale_topleft, scale_bottomright);

		this.selected = true;
	}

	public void update_hanle() {
		// get the rotation
		double rotation = this.selected_shape.rotation - Math.PI / 2;

		// get the center position of the original bound
		double o_centerx = this.selected_shape.getShape().getBounds2D().getCenterX();
		double o_centery = this.selected_shape.getShape().getBounds2D().getCenterY();

		// create the inverse rotation
		AffineTransform at1 = new AffineTransform();
		at1.translate(o_centerx, o_centery);
		at1.rotate(-rotation);
		at1.translate(-o_centerx, -o_centery);

		// get the bound of the original shape
		Rectangle2D bound = at1.createTransformedShape(this.selected_shape.getShape()).getBounds2D();

		// construct the rotation handle for original shape
		Point rotation_topleft = new Point((int) bound.getCenterX() - 5, (int) bound.getY() - 15);
		Point rotation_bottomright = new Point((int) bound.getCenterX() + 5, (int) bound.getY() - 5);
		EllipseModel new_rotation_handle = new EllipseModel(rotation_topleft, rotation_bottomright);

		// construct the scale handle for the original shape
		Point scale_topleft = new Point((int) bound.getMaxX() - 5, (int) bound.getMaxY() - 5);
		Point scale_bottomright = new Point((int) bound.getMaxX() + 5, (int) bound.getMaxY() + 5);
		RectangleModel new_scale_handle = new RectangleModel(scale_topleft, scale_bottomright);

		// apply the rotation
		new_rotation_handle.rotate(rotation, o_centerx, o_centery);
		new_scale_handle.rotate(rotation, o_centerx, o_centery);

		// update the handle
		this.rotation_handle = new_rotation_handle;
		this.scale_handle = new_scale_handle;
	}

}
