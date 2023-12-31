package Core;

import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.*;

public class UserInterface {

    private Scene m_Scene;

    private ImageView m_DisplayImageView;

    private BorderPane m_Pane;
    private Image m_CurrentImage;

    private int m_SceneWidth, m_SceneHeight;

    private SceneEditorPanel m_SceneEditorPanel;
    private MaterialEditorPanel m_MaterialEditorPanel;

    public UserInterface(int width, int height)
    {
        m_SceneWidth = width;
        m_SceneHeight = height;

        // Create panels
        m_MaterialEditorPanel = new MaterialEditorPanel(this);
        m_SceneEditorPanel = new SceneEditorPanel(this);

        // Create viewport image
        m_DisplayImageView = new ImageView();
        m_DisplayImageView.setFitWidth(1100);
        m_DisplayImageView.setFitHeight(900);

        // Create a BorderPane to organize the layout
        m_Pane = new BorderPane();

        // Set layout
        m_Pane.setLeft(m_MaterialEditorPanel.GetLayout());
        m_Pane.setCenter(m_DisplayImageView);
        m_Pane.setRight(m_SceneEditorPanel.GetLayout());

        // Create scene
        m_Scene = new Scene(m_Pane, width, height);
    }

    public void OnUpdate()
    {
        m_MaterialEditorPanel.OnUpdate();
        m_SceneEditorPanel.OnUpdate();
    }

    public void OnResize()
    {
        int currentWidth = (int)m_Scene.getWidth();
        int currentHeight = (int)m_Scene.getHeight();

        if(m_SceneWidth == currentWidth && m_SceneHeight == currentHeight)
            return;

        // Check diff and resize viewport image accordingly
        double diffWidth = currentWidth - m_SceneWidth;
        double newViewportWidth = GetViewportWidth() + diffWidth;
        SetViewportSize(newViewportWidth, currentHeight);

        // Update scene width and height
        m_SceneWidth = (int)m_Scene.getWidth();
        m_SceneHeight = (int)m_Scene.getHeight();

        System.out.println("Window resized! (" + m_SceneWidth + ", " + m_SceneHeight + ")");
    }

    public void SetViewportImage(Image image)
    {
        if(m_CurrentImage == image)
            return;

        m_CurrentImage = image;

        // Set image into image view
        m_DisplayImageView.setImage(image);
    }

    public SceneEditorPanel GetSceneEditorPanel() { return m_SceneEditorPanel;}
    public MaterialEditorPanel GetMaterialEditorPanel() { return m_MaterialEditorPanel; }
    private void SetViewportSize(double width, double height) { m_DisplayImageView.setFitWidth(width); m_DisplayImageView.setFitHeight(height); }
    public int GetViewportWidth()  { return (int)m_DisplayImageView.getFitWidth(); }
    public int GetViewportHeight()  { return (int)m_DisplayImageView.getFitHeight(); }
    public Scene GetScene() { return m_Scene; }
}