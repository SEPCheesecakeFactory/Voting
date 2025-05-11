package Client.GUITest;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GUITestView
{
  @FXML private javafx.scene.layout.HBox topBar;
  @FXML private javafx.scene.control.Label closeButton;
  @FXML private javafx.scene.control.Label minButton;
  @FXML private javafx.scene.control.Label maxButton;

  private double xOffset = 0, yOffset = 0;
  private boolean maximized = false;
  private double prevWidth, prevHeight, prevX, prevY;

  @FXML public void initialize()
  {
    // Dragging:
    topBar.setOnMousePressed(e -> {
      Stage st = getStage(e);
      xOffset = e.getSceneX();
      yOffset = e.getSceneY();
      prevWidth = st.getWidth();
      prevHeight = st.getHeight();
      prevX = st.getX();
      prevY = st.getY();
    });
    topBar.setOnMouseDragged(e -> {
      if (!maximized)
      {
        Stage st = getStage(e);
        st.setX(e.getScreenX() - xOffset);
        st.setY(e.getScreenY() - yOffset);
      }
    });

    // Buttons:
    closeButton.setOnMouseClicked(e -> getStage(e).close());
    minButton.setOnMouseClicked(e -> getStage(e).setIconified(true));
    maxButton.setOnMouseClicked(e -> {
      Stage st = getStage(e);
      if (maximized)
      {
        st.setX(prevX);
        st.setY(prevY);
        st.setWidth(prevWidth);
        st.setHeight(prevHeight);
      }
      else
      {
        prevX = st.getX();
        prevY = st.getY();
        prevWidth = st.getWidth();
        prevHeight = st.getHeight();
        st.setX(0);
        st.setY(0);
        st.setWidth(javafx.stage.Screen.getPrimary().getBounds().getWidth());
        st.setHeight(javafx.stage.Screen.getPrimary().getBounds().getHeight());
      }
      maximized = !maximized;
    });
  }

  private Stage getStage(MouseEvent e)
  {
    return (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
  }
}
