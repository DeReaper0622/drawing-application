import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.activation.ActivationGroupDesc;
import java.util.Observable;
import java.util.Observer;

public class ToolbarView extends JToolBar implements Observer {
    private JButton undo = new JButton("Undo");
    private JButton redo = new JButton("Redo");
    private JButton duplicate = new JButton("Duplicate");

    private DrawingModel model;

    ToolbarView(DrawingModel model) {
        super();
        this.model = model;
        model.addObserver(this);
        
        duplicate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (model.selected_shape.selected) {
					model.duplicate_selected();//duplicate selected shape if a shape is selected
				}
				
			}
		});
        
        undo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.undo();
				
			}
		});
        
        redo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				model.redo();
				
			}
		});
        undo.setEnabled(false);
        redo.setEnabled(false);
        setFloatable(false);
        add(undo);
        add(redo);
        add(duplicate);

        ActionListener drawingActionListener = e -> model.setShape(ShapeModel.ShapeType.valueOf(((JButton) e.getSource()).getText()));

        for(ShapeModel.ShapeType mode : ShapeModel.ShapeType.values()) {
            JButton button = new JButton(mode.toString());
            button.addActionListener(drawingActionListener);
            add(button);
        }

        this.update(null, null);
    }

    @Override
    public void update(Observable o, Object arg) {
    	undo.setEnabled(model.can_undo());
    	redo.setEnabled(model.can_redo());
    }
}
