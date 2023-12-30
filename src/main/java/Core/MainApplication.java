package Core;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    static {
        System.loadLibrary("src/backend/x64/Release/libbackend");
    }

    private final String m_Title = "JavaRayTracer";
    private final int m_InitialWidth = 1600;
    private final int m_InitialHeight = 900;
    private boolean m_IsRunning = false;

    private Stage m_Window;
    private UserInterface m_UI;
    private Renderer m_Renderer;

    private float m_TimeStep = 0.0f;
    private long m_LastFrameTime = 0;

    @Override
    public void start(Stage stage) throws IOException
    {
        m_Window = stage;

        m_UI = new UserInterface(m_InitialWidth, m_InitialHeight);
        m_Window.setTitle(m_Title);
        m_Window.setScene(m_UI.GetScene());
        m_Window.show();
        Init();
    }

    @Override
    public void stop() {
        m_IsRunning = false;
    }

    private void Init()
    {
        // Create and initialize renderer
        m_Renderer = new Renderer();

        m_IsRunning = true;

        // Create run loop
        AnimationTimer runLoopTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                m_TimeStep = (now - m_LastFrameTime) / 1e9f;
                m_LastFrameTime = now;

                OnUpdate();
            }
        };

        // Start run loop
        runLoopTimer.start();
    }

    private void OnUpdate()
    {
        // Update title each frame for displaying timestep
        UpdateTitle();

        // Resize viewport scene if necessary
        m_UI.OnResize();

        // Update UI
        m_UI.OnUpdate();

        // Resize raw image if necessary
        m_Renderer.OnResize(m_UI.GetViewportWidth(), m_UI.GetViewportHeight());

        // Render to viewport
        m_Renderer.Render(m_TimeStep);

        // Set raw image to viewport image
        m_UI.SetViewportImage(m_Renderer.GetFinalImage());
    }

    private void UpdateTitle()
    {
        m_Window.setTitle(m_Title + " TimeStep: " + m_TimeStep * 1000.0f + " ms");
    }

    public static void main(String[] args) {
        launch();
    }

}