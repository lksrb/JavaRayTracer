package Core;

import javafx.scene.image.*;

public class Renderer {

    private WritableImage m_FinalImage;
    private int m_Width = 0, m_Height = 0;

    private int[] m_RawPixels;

    public void OnResize(int width, int height)
    {
        if(m_Width == width && m_Height == height)
            return;

        m_Width = width;
        m_Height = height;

        m_RawPixels = new int[width * height];
        m_FinalImage = new WritableImage(width, height);

        OnResizeNative(width, height);
    }

    private native void RenderNative(float ts, int[] array, int width, int height);
    private native void OnResizeNative(int width, int height);

    public void Render(float ts)
    {
        // Call C++ native function
        RenderNative(ts, m_RawPixels, m_Width, m_Height);

        // Unfortunately we cannot modify pixels directly inside WritableImage,
        // so we need to set it each frame
        // Performance seems to be unaffected
        m_FinalImage.getPixelWriter().setPixels(0,0, m_Width, m_Height, PixelFormat.getIntArgbPreInstance(), m_RawPixels, 0, m_Width);
    }

    public Image GetFinalImage()
    {
        return m_FinalImage;
    }
}
