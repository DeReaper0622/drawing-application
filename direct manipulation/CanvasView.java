import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Observable;
import java.util.Observer;

public class CanvasView extends JPanel implements Observer {
	DrawingModel model;
	Point2D lastMouse;
	Point2D startMouse;
	boolean shape_drag = false;
	boolean scale_drag = false;
	boolean rotate_drag = false;

	public CanvasView(DrawingModel model) {
		super();
		this.model = model;

		MouseAdapter mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				lastMouse = e.getPoint();
				startMouse = e.getPoint();
				if (model.selected_shape != null && model.selected_shape.selected) {
					if (model.selected_shape.scale_handle.hitTest(e.getPoint())) {
						scale_drag = true;

					} else if (model.selected_shape.selected_shape.hitTest(e.getPoint())) {
						shape_drag = true;

					} else if (model.selected_shape.rotation_handle.hitTest(e.getPoint())) {
						rotate_drag = true;

					}

				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				lastMouse = e.getPoint();
				if (shape_drag == true) {
					model.move_selected(e.getX() - startMouse.getX(), e.getY() - startMouse.getY());
					startMouse = e.getPoint();
				} else if (scale_drag) {// if drag the scale handle
					//model.scale_selected(e.getX() - startMouse.getX(), e.getY() - startMouse.getY());
					model.scale_selected(e.getPoint());
					startMouse = e.getPoint();
				} else if (rotate_drag) {
					// model.rotate_selected(e.getX() - startMouse.getX(), e.getY() -
					// startMouse.getY());
					model.rotate_selected(e.getPoint());
					startMouse = e.getPoint();
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (shape_drag == false && scale_drag == false && rotate_drag == false) {
					if ((Math.abs(startMouse.getX() - lastMouse.getX()) > 5)
							|| (Math.abs(startMouse.getY() - lastMouse.getY()) > 5)) {// prevent this get called during
																						// click
						ShapeModel shape = new ShapeModel.ShapeFactory().getShape(model.getShape(), (Point) startMouse,
								(Point) lastMouse);
						model.addShape(shape);
						
					}
				}
				if (shape_drag) {
					model.selected_endmove();
				} else if (rotate_drag) {
					model.selected_endrotate();

				} else if (scale_drag) {
					model.selected_endscale(e.getPoint());
				}
				startMouse = null;
				lastMouse = null;
				shape_drag = false;
				scale_drag = false;
				rotate_drag = false;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				model.select_check(new Point(e.getX(), e.getY()));
				repaint();
			}
		};

		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);

		model.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		setBackground(Color.WHITE);

		drawAllShapes(g2);
		drawCurrentShape(g2);
		if (model.selected_shape != null) {
			drawHandles(g2);
		}
	}

	private void drawAllShapes(Graphics2D g2) {
		g2.setColor(new Color(66, 66, 66));
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (ShapeModel shape : model.getShapes()) {
			g2.draw(shape.getShape());
		}
	}

	private void drawCurrentShape(Graphics2D g2) {
		if (startMouse == null) {
			return;
		}

		g2.setColor(new Color(66, 66, 66));
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		g2.draw(new ShapeModel.ShapeFactory().getShape(model.getShape(), (Point) startMouse, (Point) lastMouse)
				.getShape());
	}

	private void drawHandles(Graphics2D g2) {
		if (model.selected_shape.selected) {
			g2.setColor(Color.BLUE);
			g2.fill(model.selected_shape.rotation_handle.getShape());
			g2.fill(model.selected_shape.scale_handle.getShape());
		}

	}
}
