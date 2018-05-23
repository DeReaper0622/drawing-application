
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.*;

public class MainView extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4380826878839078219L;
	private Model model;
	public JMenuBar menu_bar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JMenu edit_menu = new JMenu("Edit");
	private JCheckBoxMenuItem select_item = new JCheckBoxMenuItem("Selection Mode", false);
	private JCheckBoxMenuItem draw_item = new JCheckBoxMenuItem("Drawing Mode", true);
	private JMenuItem delete = new JMenuItem("Delete", KeyEvent.VK_D);
	private JMenuItem transform_item = new JMenuItem("Transform shape", KeyEvent.VK_T);
	private JMenu format_menu = new JMenu("Format");
	private JMenu stroke_menu = new JMenu("Stroke width");
	private ArrayList<Stroke_Radio> stroke_width = new ArrayList<Stroke_Radio>();
	private JMenuItem fill_color = new JMenuItem("Fill Colour");
	private JMenuItem stroke_color = new JMenuItem("Stroke Colour");
	public MyToolbar toolbar;
	public Canvas canvas;

	/**
	 * Create a new View.
	 */
	public MainView(Model model) {
		// Set up the window.
		this.setTitle("CS 349 W18 A2");
		this.setMinimumSize(new Dimension(128, 128));
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		// Setting up file_menu
		JMenuItem new_item = new JMenuItem("New", KeyEvent.VK_N);
		new_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		new_item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.bound_reset();
				model.new_collection();
				model.notifyObservers();
			}

		});
		file_menu.add(new_item);

		JMenuItem exit_item = new JMenuItem("Exit", KeyEvent.VK_E);
		exit_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		exit_item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
				System.exit(0);
			}

		});
		file_menu.add(exit_item);
		menu_bar.add(file_menu);
		//

		// Setting up the edit menu

		select_item.setMnemonic(KeyEvent.VK_S);
		select_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		select_item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.selection_on = true;
				model.notifyObservers();
			}
		});
		edit_menu.add(select_item);

		draw_item.setMnemonic(KeyEvent.VK_R);
		draw_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		draw_item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.selection_on = false;
				model.notifyObservers();
			}

		});
		edit_menu.add(draw_item);
		edit_menu.addSeparator();

		delete.setEnabled(false);
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.delete_selected();
				model.notifyObservers();
			}

		});
		edit_menu.add(delete);

		transform_item.setEnabled(false);
		transform_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		transform_item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Transformation transform_window = new Transformation(model);
			}
		});
		edit_menu.add(transform_item);

		menu_bar.add(edit_menu);
		//

		// setting up the format_menu
		// setting up the stroke width sub menu
		for (int i = 1; i < 11; i++) {
			Stroke_Radio new_width = new Stroke_Radio(i);
			new_width.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					model.change_stroke(new_width.stroke);
					model.notifyObservers();
				}
			});
			stroke_width.add(new_width);
			stroke_menu.add(new_width);
		}
		stroke_width.get(0).setSelected(true);
		format_menu.add(stroke_menu);
		//

		// setting up fill color menu item
		stroke_color.setIconTextGap(20);
		stroke_color.setIcon(new ColorIcon(Color.BLACK, 20));
		stroke_color.setVerticalTextPosition(SwingConstants.CENTER);
		stroke_color.setHorizontalTextPosition(SwingConstants.RIGHT);
		fill_color.setIconTextGap(20);
		fill_color.setIcon(new ColorIcon(Color.WHITE, 20));
		fill_color.setVerticalTextPosition(SwingConstants.CENTER);
		fill_color.setHorizontalTextPosition(SwingConstants.RIGHT);

		// adding action listener for storke_color
		stroke_color.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color current = JColorChooser.showDialog(null, "Choose Stroke Color", model.current_stroke_color);
				model.change_color(current, model.current_fill_color);
				model.notifyObservers();
			}
		});
		// add action listener for fill_color
		fill_color.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color current = JColorChooser.showDialog(null, "Choose Fill Color", model.current_fill_color);
				model.change_color(model.current_stroke_color, current);
				model.notifyObservers();

			}
		});
		format_menu.add(fill_color);
		format_menu.add(stroke_color);

		menu_bar.add(format_menu);
		//
		this.setJMenuBar(menu_bar);

		// add model to toolbar
		toolbar = new MyToolbar(model);
		this.add(toolbar, BorderLayout.NORTH);
		// add model to canvas
		canvas = new Canvas(model);
		this.add(canvas, BorderLayout.CENTER);

		// Hook up this observer so that it will be notified when the model
		// changes.
		this.model = model;
		model.addObserver(this);

		setVisible(true);
	}

	/**
	 * Update with data from the model.
	 */
	public void update(Object observable) {

		select_item.setSelected(model.selection_on);
		draw_item.setSelected(!model.selection_on);
		delete.setEnabled(model.selection_on && (model.select_index > -1));
		transform_item.setEnabled(model.selection_on && (model.select_index > -1));
		for (int i = 0; i < stroke_width.size(); i++) {
			Boolean is_current = model.current_stroke == stroke_width.get(i).stroke;
			stroke_width.get(i).setSelected(is_current);
		}
		stroke_color.setIcon(new ColorIcon(model.current_stroke_color, 20));
		fill_color.setIcon(new ColorIcon(model.current_fill_color, 20));
	}

	// the toolbar object used in the main view

}

class MyToolbar extends JToolBar implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6995828540667869996L;

	// model for the toolbar
	Model model;
	//

	// buttons for toolbar
	public JToggleButton select_button = new JToggleButton("Select", false);
	public JToggleButton draw_button = new JToggleButton("Draw", true);
	public JButton fill_color = new JButton("Fill Colour");
	public JButton stroke_color = new JButton("Stroke Colour");
	//

	// comboboxes for the toolbar
	String[] shape_list = new String[] { "Freeform", "Straight line", "Rectangle", "Ellipse" };
	public JComboBox<String> shape_selection = new JComboBox<String>(shape_list);
	public JComboBox<String> width_selection = new JComboBox<String>();
	//

	public MyToolbar(Model model) {
		// add toolbar as observer
		this.model = model;
		model.addObserver(this);
		//

		this.setFloatable(false);

		// draw and select button
		Icon mouse_pointer = new ImageIcon("cursor.png");
		Icon pen = new ImageIcon("pen.png");
		select_button.setIcon(mouse_pointer);
		select_button.setSize(100, 35);
		draw_button.setIcon(pen);
		draw_button.setSize(100, 25);
		// action listener for both of them
		select_button.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				model.selection_on = true;
				model.notifyObservers();
				;
			}
		});
		draw_button.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				model.selection_on = false;
				model.notifyObservers();
				;
			}
		});
		//
		this.add(select_button);
		this.add(draw_button);
		this.addSeparator();

		// shape selection combobox
		shape_selection.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getItem().toString() != model.shape_mode) {
					model.shape_mode = event.getItem().toString();
				}
			}
		});
		// change the model if new shape is selected
		this.add(shape_selection);
		//

		// width selection combo box
		for (int i = 1; i < 11; i++) {
			width_selection.addItem(i + " px");
		}
		width_selection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.change_stroke(width_selection.getSelectedIndex() + 1);
				model.notifyObservers();
			}
		});
		this.add(width_selection);
		//

		// setting up fill color and stoke color button
		fill_color.setIcon(new ColorIcon(Color.WHITE));
		fill_color.setIconTextGap(20);
		fill_color.setVerticalTextPosition(SwingConstants.CENTER);
		fill_color.setHorizontalTextPosition(SwingConstants.RIGHT);
		stroke_color.setIcon(new ColorIcon(Color.BLACK));
		stroke_color.setIconTextGap(20);
		stroke_color.setVerticalTextPosition(SwingConstants.CENTER);
		stroke_color.setHorizontalTextPosition(SwingConstants.RIGHT);

		// add action listener for fill_color
		fill_color.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color current = JColorChooser.showDialog(null, "Choose Fill Color", model.current_fill_color);
				model.change_color(model.current_stroke_color, current);
				model.notifyObservers();
			}
		});

		// add action listener for stroke_color
		stroke_color.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color current = JColorChooser.showDialog(null, "Choose Stroke Color", model.current_stroke_color);
				model.change_color(current, model.current_fill_color);
				model.notifyObservers();
			}
		});
		this.add(fill_color);
		this.add(stroke_color);

	}

	public void update(Object observable) {
		// change the mode between selection and drawing

		// =============================================
		draw_button.setSelected(!model.selection_on);
		select_button.setSelected(model.selection_on);
		fill_color.setIcon(new ColorIcon(model.current_fill_color));
		stroke_color.setIcon(new ColorIcon(model.current_stroke_color));
	}
}

class Canvas extends JComponent implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3296918558835375959L;
	Model model;
	// recording the x y coordinates for mouse drag
	int previous_x = -1;
	int previous_y = -1;

	Canvas(Model model) {
		super();
		// add canvas as observer of the model
		this.model = model;
		model.addObserver(this);
		//

		this.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (model.selection_on) {
					model.bound_reset();
					model.check_selected(new Point(e.getX(), e.getY()));
					repaint();
					model.notifyObservers();
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (model.selection_on != true) {
					model.finish_painting();
				}
				previous_x = -1;
				previous_y = -1;// resets the previous _x and previous_y every time mouse is released
				repaint();
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {

			public void mouseDragged(MouseEvent e) {
				if (model.selection_on != true) {// in drawing mode
					model.add_point(new Point(e.getX(), e.getY()));
				} else {// in selection mode
					if (previous_x > -1) {
						int x_trans = e.getX() - previous_x;
						int y_trans = e.getY() - previous_y;
						model.move_to(x_trans, y_trans, new Point(e.getX(), e.getY()));
					}
					previous_x = e.getX();
					previous_y = e.getY();// gets the new previous xy value

				}
				repaint();
			}

		});

	}

	// custom graphics drawing
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// cast to get 2D drawing methods
		Graphics2D g2 = (Graphics2D) g;

		// paint the finished item
		for (int i = 0; i < model.item_collection.size(); i++) {
			g2.setStroke(new BasicStroke(model.item_collection.get(i).stroke));
			AffineTransform saveXform = g2.getTransform();
			g2.transform(model.item_collection.get(i).transform);
			if (model.item_collection.get(i).shape_mode == "Rectangle"
					|| model.item_collection.get(i).shape_mode == "Ellipse") {
				g2.setColor(model.item_collection.get(i).fill_color);
				g2.fill(model.item_collection.get(i).paint_shape);
			}
			g2.setColor(model.item_collection.get(i).stroke_color);
			g2.draw(model.item_collection.get(i).paint_shape);

			g2.setTransform(saveXform);
		}

		// paint bound of a selected shape if there is one
		if (model.bound.getWidth() != 0) {
			g2.setStroke(new BasicStroke(1));
			g2.setColor(Color.CYAN);
			AffineTransform saveXform = g2.getTransform();
			g2.transform(model.item_collection.get(model.select_index).transform);
			g2.draw(model.bound);
			g2.setTransform(saveXform);
		}
		// paint the current item that the user is drawing so it will be on top of old
		// shapes
		g2.setStroke(new BasicStroke(model.current_stroke));
		if (model.shape_mode == "Rectangle" || model.shape_mode == "Ellipse") {
			g2.setColor(model.current_fill_color);
			g2.fill(model.current_item);
		}
		g2.setColor(model.current_stroke_color);
		g2.draw(model.current_item);

	}

	public void update(Object observable) {
		repaint();
	}
}

class Transformation extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8661797330250577564L;
	JSpinner x_trans = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
	JSpinner y_trans = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
	JSpinner rotation = new JSpinner(new SpinnerNumberModel(0, -360, 360, 1));
	JSpinner x_scale = new JSpinner(new SpinnerNumberModel(1, -10, 10, 0.1));
	JSpinner y_scale = new JSpinner(new SpinnerNumberModel(1, -10, 10, 0.1));
	JButton ok_button = new JButton("OK");
	JButton cancel_button = new JButton("Cancel");

	Transformation(Model model) {
		super();
		setModal(true);
		this.setTitle("Transform shape");
		this.setMinimumSize(new Dimension(350, 200));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		// setting up translation
		JPanel translation = new JPanel();
		translation.add(new JLabel("Translate (px):   x:  "));
		translation.add(x_trans);
		translation.add(new JLabel("y: "));
		translation.add(y_trans);
		this.add(translation);
		//

		// setting up rotation
		JPanel rotate = new JPanel();
		rotate.add(new JLabel("Rotate (degrees):  "));
		rotate.add(rotation);
		this.add(rotate);

		// setting up scaling
		JPanel scaling = new JPanel();
		scaling.add(new JLabel("Scale (times):   x:  "));
		scaling.add(x_scale);
		scaling.add(new JLabel("y: "));
		scaling.add(y_scale);
		this.add(scaling);
		//

		// setting up buttons
		JPanel buttons = new JPanel();
		buttons.add(ok_button);
		buttons.add(cancel_button);
		this.add(buttons);

		// adding actionlistener for both buttons
		cancel_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		ok_button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int dx = (int) x_trans.getValue();
				int dy = (int) y_trans.getValue();
				int r = (int) rotation.getValue();
				double sx = (double) x_scale.getValue();
				double sy = (double) y_scale.getValue();
				model.set_transformation(dx, dy, r, sx, sy);
				model.notifyObservers();
				setVisible(false);
				dispose();
			}
		});
		setVisible(true);
	}

}

class Stroke_Radio extends JRadioButtonMenuItem {
	int stroke;

	public Stroke_Radio(int stroke) {
		super(stroke + " px");
		this.stroke = stroke;
	}
}

class ColorIcon implements Icon {
	private int width;
	private int height;
	private int left = 0;
	private Color color;

	public ColorIcon(Color c) {
		this.width = 15;
		this.height = 15;
		this.color = c;
	}

	public ColorIcon(Color c, int left) {
		this.width = 15;
		this.height = 15;
		this.left = left;
		this.color = c;
	}

	@Override
	public int getIconHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIconWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
		g.fillRect(x - left, y - 8, width, height);
	}

}