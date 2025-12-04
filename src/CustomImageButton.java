import javax.swing.*;
import java.awt.event.*;

public class CustomImageButton extends JButton {
    private final Icon defaultIcon;
    private final Icon hoverIcon;
    private final Icon pressedIcon;

    public CustomImageButton(String normal, String hover, String pressed) {
        this.defaultIcon = new ImageIcon(normal);
        this.hoverIcon = new ImageIcon(hover);
        this.pressedIcon = new ImageIcon(pressed);

        setIcon(defaultIcon);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setIcon(defaultIcon);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setIcon(hoverIcon);
            }
        });
    }
}