package Core;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class MaterialEditorPanel {

    private VBox m_VerticalBoxLayout;
    private ListView<Material> m_MaterialListView;
    private Material m_SelectedMaterial;
    private boolean m_SelectionChanged = false;

    private ContextMenu m_ActiveObjectContextMenu;  // Right-click on material
    private ContextMenu m_EmptySpaceContextMenu;    // Right-click on empty space
    private MenuItem m_DeleteMenuItem;

    private GridPane m_PropertiesPane;
    private TextField m_NameTextField;
    private ColorPicker m_AlbedoColorPicker;
    private Slider m_RoughnessSlider;
    private Slider m_MetallicSlider;
    private ColorPicker m_EmissionColorPicker;
    private TextField m_EmissionPowerTextField;

    private UserInterface m_UI;

    private Material m_DefaultMaterial;

    public MaterialEditorPanel(UserInterface ui)
    {
        m_UI = ui;

        // Create and setup listview for displaying available materials
        SetupMaterialListView();

        // Create context menu for Material creation/destruction
        CreateContextMenu();

        // Create individual property nodes, pane and set selection context callback
        CreatePropertiesPanel();

        // Resulting layout
        m_VerticalBoxLayout = new VBox(m_MaterialListView, m_PropertiesPane);
        m_VerticalBoxLayout.setMinWidth(200);
        m_VerticalBoxLayout.setSpacing(10);
    }

    public void OnUpdate()
    {
        // Disable properties if no material is selected
        if(m_SelectedMaterial == null)
        {
            m_PropertiesPane.setDisable(true);

            // Reset fields
            m_NameTextField.setText("");
            m_AlbedoColorPicker.setValue(Color.WHITE);
            m_RoughnessSlider.setValue(0.0f);
            m_MetallicSlider.setValue(0.0f);
            m_EmissionColorPicker.setValue(Color.WHITE);
            m_EmissionPowerTextField.setText("");
        } else {
            m_PropertiesPane.setDisable(false);
        }

        // Restrict removal of default material
        m_DeleteMenuItem.setDisable(m_SelectedMaterial == m_DefaultMaterial);
    }

    private void SetupMaterialListView()
    {
        // Default materials
        {
            m_MaterialListView = new ListView<>();

            {
                Material material = new Material("Default Material");
                material.Albedo = new Vector3(1.0f);
                material.Roughness = 1.0f;
                material.Metallic = 0.0f;
                m_MaterialListView.getItems().add(material);
                m_DefaultMaterial = material;
            }

            {
                Material material = new Material("Pink Material");
                material.Albedo = new Vector3(1.0f, 0.0f, 1.0f);
                material.Roughness = 1.0f;
                m_MaterialListView.getItems().add(material);
            }

            {
                Material material = new Material("Blue Material");
                material.Albedo = new Vector3(0.2f, 0.3f, 1.0f);
                material.Roughness = 1.0f;
                m_MaterialListView.getItems().add(material);
            }

            {
                Material material = new Material("Emission Material");
                material.Albedo = new Vector3(0.9f, 0.5f, 0.2f);
                //orangeMaterial.Albedo = new Vector3(1.0f);
                material.Roughness = 0.1f;
                material.EmissionColor = material.Albedo;
                material.EmissionPower = 3.05f;
                m_MaterialListView.getItems().add(material);
            }

            {
                Material material = new Material("Mirror Material");
                material.Albedo = new Vector3(0.8f);
                material.Roughness = 0.0f;
                material.Metallic = 0.0f;
                m_MaterialListView.getItems().add(material);
            }

            {
                Material material = new Material("Yellow Material");
                material.Albedo = new Vector3(1.0f, 1.0f, 0.0f);
                material.Roughness = 1.0f;
                m_MaterialListView.getItems().add(material);
            }

            for(var material : m_MaterialListView.getItems())
                OnMaterialAddNative(material);
        }

        // Customize cell factory
        m_MaterialListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Material> call(ListView<Material> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Material material, boolean empty) {
                        super.updateItem(material, empty);

                        if (empty || material == null) {
                            setText(null);

                            m_MaterialListView.setOnContextMenuRequested(event -> {
                                if (event.isConsumed()) {
                                    return;
                                }
                                m_EmptySpaceContextMenu.show(m_MaterialListView, event.getScreenX(), event.getScreenY());

                                event.consume();
                            });
                        } else {
                            setText(material.Name);

                            setContextMenu(m_ActiveObjectContextMenu);
                        }
                    }
                };
            }
        });

        // Selection callback
        m_MaterialListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            m_SelectedMaterial = newValue;

            if(m_SelectedMaterial == null)
                return;

            // Update the properties window based on the selected item
            // Avoid unnecessary listview refreshing we mark next text change
            m_SelectionChanged = true;
            m_NameTextField.setText(m_SelectedMaterial.Name);
            m_AlbedoColorPicker.setValue(new Color(m_SelectedMaterial.Albedo.X, m_SelectedMaterial.Albedo.Y, m_SelectedMaterial.Albedo.Z, 1.0f));
            m_RoughnessSlider.setValue(m_SelectedMaterial.Roughness);
            m_MetallicSlider.setValue(m_SelectedMaterial.Metallic);
            m_EmissionColorPicker.setValue(new Color(m_SelectedMaterial.EmissionColor.X, m_SelectedMaterial.EmissionColor.Y, m_SelectedMaterial.EmissionColor.Z, 1.0f));
            m_EmissionPowerTextField.setText(Float.toString(m_SelectedMaterial.EmissionPower));
            m_SelectionChanged = false;
        });
    }

    private void CreateContextMenu()
    {
        m_ActiveObjectContextMenu = new ContextMenu();
        m_EmptySpaceContextMenu = new ContextMenu();

        // Create MenuItems for the ContextMenu
        MenuItem newMenuItem = new MenuItem("New");
        m_DeleteMenuItem = new MenuItem("Delete");

        m_EmptySpaceContextMenu.getItems().addAll(newMenuItem);
        m_ActiveObjectContextMenu.getItems().addAll(m_DeleteMenuItem);

        // Handle new objects
        newMenuItem.setOnAction(event -> {
            Material material = new Material("New material");

            m_MaterialListView.getItems().add(material);
            m_MaterialListView.getSelectionModel().select(material);

            // Call native method to register that object
            OnMaterialAddNative(material);

            m_EmptySpaceContextMenu.hide();
        });

        // Handle destroyed objects
        m_DeleteMenuItem.setOnAction(event -> {
            if(m_SelectedMaterial == null)
                return;

            int removedMaterialIndex = OnMaterialRemoveNative(m_SelectedMaterial);
            System.out.println("Removed material: " + removedMaterialIndex);
            if(removedMaterialIndex == -1)
            {
                // This should never happen
                System.out.println("Material for removal was not found!");
                return;
            }

            // Iterate through all scene objects and makes sure that none of them have the removed material
            m_UI.GetSceneEditorPanel().RemoveLeftoverMaterials(removedMaterialIndex);

            m_MaterialListView.getItems().remove(m_SelectedMaterial);

            m_SelectedMaterial = null;
            m_MaterialListView.getSelectionModel().clearSelection();
            m_MaterialListView.refresh();
        });

        m_MaterialListView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> m_EmptySpaceContextMenu.hide());
    }

    private void CreatePropertiesPanel()
    {
        // Setup text fields
        m_NameTextField = new TextField();
        m_NameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || m_SelectionChanged)
                return;

            m_SelectedMaterial.Name = newValue;

            // Unfortunately, ComboBox does not refresh dropdown items so this hack is needed
            m_UI.GetSceneEditorPanel().RefreshCombobox(m_SelectedMaterial);

            // Refresh list view to update material name
            m_MaterialListView.refresh();
        });

        m_AlbedoColorPicker = new ColorPicker();
        m_AlbedoColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || m_SelectionChanged)
                return;

            m_SelectedMaterial.Albedo.X = (float)newValue.getRed();
            m_SelectedMaterial.Albedo.Y = (float)newValue.getGreen();
            m_SelectedMaterial.Albedo.Z = (float)newValue.getBlue();

            OnMaterialValueChangeNative(m_SelectedMaterial);
        });

        m_RoughnessSlider = new Slider(0.0, 1.0, 0.0);
        m_RoughnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || m_SelectionChanged)
                return;

            m_SelectedMaterial.Roughness = newValue.floatValue();
            OnMaterialValueChangeNative(m_SelectedMaterial);
        });

        m_MetallicSlider = new Slider(0.0, 1.0, 0.0);
        m_MetallicSlider.setDisable(false); // Specular lighting is not available
        m_MetallicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || m_SelectionChanged)
                return;

            m_SelectedMaterial.Metallic = newValue.floatValue();
            OnMaterialValueChangeNative(m_SelectedMaterial);
        });

        m_EmissionColorPicker = new ColorPicker();
        m_EmissionColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || m_SelectionChanged)
                return;

            m_SelectedMaterial.EmissionColor.X = (float)newValue.getRed();
            m_SelectedMaterial.EmissionColor.Y = (float)newValue.getGreen();
            m_SelectedMaterial.EmissionColor.Z = (float)newValue.getBlue();

            OnMaterialValueChangeNative(m_SelectedMaterial);
        });

        m_EmissionPowerTextField = Widgets.CreateFloatTextField();
        m_EmissionPowerTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedMaterial == null || Utils.IsOnlyNegativeSign(newValue) || m_SelectionChanged)
                return;

            float newValueFloat = 0.0f;
            if(!newValue.isEmpty())
            {
                newValueFloat = Float.parseFloat(newValue);
            }

            m_SelectedMaterial.EmissionPower = Math.max(newValueFloat, 0.0f);
            OnMaterialValueChangeNative(m_SelectedMaterial);
        });

        // Properties panel
        m_PropertiesPane = new GridPane();
        m_PropertiesPane.setHgap(10);
        m_PropertiesPane.setVgap(5);
        m_PropertiesPane.setPadding(new Insets(10, 10, 10, 10));

        m_PropertiesPane.add(new Label("Name"), 0, 0);
        m_PropertiesPane.add(m_NameTextField, 1, 0);

        m_PropertiesPane.add(new Label("Albedo"), 0, 1);
        m_PropertiesPane.add(m_AlbedoColorPicker, 1, 1);

        m_PropertiesPane.add(new Label("Roughness"), 0, 2);
        m_PropertiesPane.add(m_RoughnessSlider, 1, 2);

        m_PropertiesPane.add(new Label("Metallic"), 0, 3);
        m_PropertiesPane.add(m_MetallicSlider, 1, 3);

        m_PropertiesPane.add(new Label("Emission Color"), 0, 4);
        m_PropertiesPane.add(m_EmissionColorPicker, 1, 4);

        m_PropertiesPane.add(new Label("Emission Power"), 0, 5);
        m_PropertiesPane.add(m_EmissionPowerTextField, 1, 5);
    }

    public ObservableList<Material> GetMaterials() { return m_MaterialListView.getItems();}
    public VBox GetLayout() { return m_VerticalBoxLayout; }
    public Material GetDefaultMaterial() { return m_DefaultMaterial; }

    private native void OnMaterialAddNative(Material material);
    private native int OnMaterialRemoveNative(Material material);
    private native void OnMaterialValueChangeNative(Material material);
}
