package Core;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class SceneEditorPanel {

    private VBox m_VerticalBoxLayout;
    private ListView<SceneObject> m_SceneObjectListView;
    private SceneObject m_SelectedObject;
    private boolean m_SelectionChanged = false;

    private ContextMenu m_ActiveObjectContextMenu;  // Right-click on scene object
    private ContextMenu m_EmptySpaceContextMenu;    // Right-click on empty space

    // Properties
    private GridPane m_PropertiesPane;
    private TextField m_NameTextField;
    private TextField m_PositionXTextField, m_PositionYTextField, m_PositionZTextField;
    private TextField m_RadiusTextField;
    private ComboBox<Material> m_MaterialsComboBox;

    private UserInterface m_UI;

    public SceneEditorPanel(UserInterface ui)
    {
        m_UI = ui;

        // Create and setup listview for displaying scene objects
        SetupSceneListView();

        // Create context menu for scene object creation/destruction
        CreateContextMenu();

        // Create individual property nodes, pane and set selection context callback
        CreatePropertiesPanel();

        // Resulting layout
        m_VerticalBoxLayout = new VBox(m_SceneObjectListView, m_PropertiesPane);
        m_VerticalBoxLayout.setMinWidth(200);
        m_VerticalBoxLayout.setSpacing(10);
    }

    public void OnUpdate()
    {
        if(m_SelectedObject == null)
        {
            // Reset fields
            m_PropertiesPane.setDisable(true);
            m_NameTextField.setText("");
            m_PositionXTextField.setText("");
            m_PositionYTextField.setText("");
            m_PositionZTextField.setText("");
            m_RadiusTextField.setText("");
            m_MaterialsComboBox.getSelectionModel().clearSelection();
        } else {
            m_PropertiesPane.setDisable(false);
        }
    }

    public VBox GetLayout()
    {
        return m_VerticalBoxLayout;
    }

    private void SetupSceneListView()
    {
        // Default scene
        {
            m_SceneObjectListView = new ListView<>();
            SceneObject smallPlanet = new SceneObject("Small planet");
            smallPlanet.Radius = 1.0f;
            smallPlanet.MaterialIndex = 1;

            SceneObject bigPlanet = new SceneObject("Big Planet");
            bigPlanet.Position = new Vector3(0.0f, -101.0f, 0.0f);
            bigPlanet.Radius = 100.0f;
            bigPlanet.MaterialIndex = 2;

            SceneObject sun = new SceneObject("Sun");
            sun.Position = new Vector3(9.2f, -1.2f, -9.6f);
            sun.Radius = 9.2f;
            sun.MaterialIndex = 3;

            SceneObject mirror = new SceneObject("Mirror");
            mirror.Position = new Vector3(-2.2f, 0, 5.6f);
            mirror.Radius = 1.0f;
            mirror.MaterialIndex = 4;

            SceneObject mirror2 = new SceneObject("Mirror 2");
            mirror2.Position = new Vector3(-3.0f, 0, 0.0f);
            mirror2.Radius = 1.0f;
            mirror2.MaterialIndex = 4;

            SceneObject smallPlanet2 = new SceneObject("Small planet 2");
            smallPlanet2.Position = new Vector3(1.2f, 0.0f, 3.6f);
            smallPlanet2.Radius = 1.0f;
            smallPlanet2.MaterialIndex = 5;

            m_SceneObjectListView.getItems().addAll(smallPlanet, bigPlanet, sun, mirror, mirror2, smallPlanet2);

            for(var sceneObject : m_SceneObjectListView.getItems())
                OnSceneObjectAddNative(sceneObject);
        }

        // Customize cell factory
        m_SceneObjectListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<SceneObject> call(ListView<SceneObject> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(SceneObject sceneObject, boolean empty) {
                        super.updateItem(sceneObject, empty);

                        if (empty || sceneObject == null) {
                            setText(null);
                            m_SceneObjectListView.setOnContextMenuRequested(event -> {
                                if (event.isConsumed()) {
                                    return;
                                }
                                m_EmptySpaceContextMenu.show(m_SceneObjectListView, event.getScreenX(), event.getScreenY());
                                event.consume();
                            });
                        } else {
                            setText(sceneObject.Name);
                            setContextMenu(m_ActiveObjectContextMenu);
                        }
                    }
                };
            }
        });

        // Selection callback
        m_SceneObjectListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            m_SelectedObject = newValue;

            if(m_SelectedObject == null)
                return;

            // Update the properties window based on the selected item
            // Avoid unnecessary listview refreshing we mark next text change
            m_SelectionChanged = true;
            m_NameTextField.setText(m_SelectedObject.Name);
            m_PositionXTextField.setText(Float.toString(m_SelectedObject.Position.X));
            m_PositionYTextField.setText(Float.toString(m_SelectedObject.Position.Y));
            m_PositionZTextField.setText(Float.toString(m_SelectedObject.Position.Z));
            m_RadiusTextField.setText(Float.toString(m_SelectedObject.Radius));
            m_MaterialsComboBox.setValue(m_UI.GetMaterialEditorPanel().GetMaterials().get(m_SelectedObject.MaterialIndex));
            m_SelectionChanged = false;
        });
    }

    private void CreateContextMenu()
    {
        m_ActiveObjectContextMenu = new ContextMenu();
        m_EmptySpaceContextMenu = new ContextMenu();

        // Create MenuItems for the ContextMenu
        MenuItem newMenuItem = new MenuItem("New");
        MenuItem deleteMenuItem = new MenuItem("Delete");

        m_EmptySpaceContextMenu.getItems().add(newMenuItem);
        m_ActiveObjectContextMenu.getItems().add(deleteMenuItem);

        // Handle new objects
        newMenuItem.setOnAction(event -> {
            SceneObject sceneObject = new SceneObject("New object");

            m_SceneObjectListView.getItems().add(sceneObject);
            m_SceneObjectListView.getSelectionModel().select(sceneObject);

            // Call native method to register that object
            OnSceneObjectAddNative(sceneObject);

            m_EmptySpaceContextMenu.hide();
        });

        // Handle destroyed objects
        deleteMenuItem.setOnAction(event -> {
            if(m_SelectedObject == null)
                return;

            OnSceneObjectRemoveNative(m_SelectedObject);
            m_SceneObjectListView.getItems().remove(m_SelectedObject);

            m_SelectedObject = null;
            m_SceneObjectListView.getSelectionModel().clearSelection();
            m_SceneObjectListView.refresh();
        });

        m_SceneObjectListView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            m_EmptySpaceContextMenu.hide();
        });
    }

    private void CreatePropertiesPanel()
    {
        // Setup text fields
        m_NameTextField = new TextField();
        m_NameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || m_SelectionChanged)
                return;

            m_SelectedObject.Name = newValue;
            m_SceneObjectListView.refresh();
        });

        m_PositionXTextField = Widgets.CreateFloatTextField();
        m_PositionXTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || Utils.IsOnlyNegativeSign(newValue) || m_SelectionChanged)
                return;

            float newValueFloat = 0.0f;
            if(!newValue.isEmpty())
            {
                newValueFloat = Float.parseFloat(newValue);
            }

            if(m_SelectedObject.Position.X != newValueFloat)
            {
                m_SelectedObject.Position.X = newValueFloat;
                OnSceneObjectValueChangeNative(m_SelectedObject);
            }
        });

        m_PositionYTextField = Widgets.CreateFloatTextField();
        m_PositionYTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || Utils.IsOnlyNegativeSign(newValue) || m_SelectionChanged)
                return;

            float newValueFloat = 0.0f;
            if(!newValue.isEmpty())
            {
                newValueFloat = Float.parseFloat(newValue);
            }

            if(m_SelectedObject.Position.Y != newValueFloat)
            {
                m_SelectedObject.Position.Y = newValueFloat;
                OnSceneObjectValueChangeNative(m_SelectedObject);
            }
        });

        m_MaterialsComboBox = new ComboBox<>(m_UI.GetMaterialEditorPanel().GetMaterials());
        m_MaterialsComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Material material) {
                if(material == null)
                    return "";
                return material.Name;
            }

            @Override
            public Material fromString(String string) {
                return null;
            }
        });

        m_MaterialsComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Material> call(ListView<Material> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Material item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.Name);
                        }
                    }
                };
            }
        });
        m_MaterialsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || m_SelectionChanged)
                return;

            int materialIndex = 0;

            for (var material : m_UI.GetMaterialEditorPanel().GetMaterials())
            {
                if(material == newValue)
                    break;

                materialIndex++;
            }

            if(m_SelectedObject.MaterialIndex != materialIndex)
            {
                m_SelectedObject.MaterialIndex = materialIndex;
                OnSceneObjectValueChangeNative(m_SelectedObject);
            }
        });

        m_PositionZTextField = Widgets.CreateFloatTextField();
        m_PositionZTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || Utils.IsOnlyNegativeSign(newValue))
                return;

            float newValueFloat = 0.0f;
            if(!newValue.isEmpty())
            {
                newValueFloat = Float.parseFloat(newValue);
            }

            if(m_SelectedObject.Position.Z != newValueFloat)
            {
                m_SelectedObject.Position.Z = newValueFloat;
                OnSceneObjectValueChangeNative(m_SelectedObject);
            }
        });

        m_RadiusTextField = Widgets.CreateFloatTextField();
        m_RadiusTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(m_SelectedObject == null || Utils.IsOnlyNegativeSign(newValue))
                return;

            float newValueFloat = 1.0f;
            if(!newValue.isEmpty())
            {
                newValueFloat = Float.parseFloat(newValue);
            }

            // Limit radius value to at least 0.001f
            newValueFloat = Math.max(newValueFloat, 0.001f);

            if(m_SelectedObject.Radius != newValueFloat)
            {
                m_SelectedObject.Radius = newValueFloat;
                OnSceneObjectValueChangeNative(m_SelectedObject);
            }
        });

        // Properties panel
        m_PropertiesPane = new GridPane();
        m_PropertiesPane.setHgap(10);
        m_PropertiesPane.setVgap(5);
        m_PropertiesPane.setPadding(new Insets(10, 10, 10, 10));

        m_PropertiesPane.add(new Label("Name"), 0, 0);
        m_PropertiesPane.add(m_NameTextField, 1, 0);

        m_PropertiesPane.add(new Label("Position: "), 0, 1);
        m_PropertiesPane.add(new Label(""), 0, 1);

        m_PropertiesPane.add(new Label("X"), 0, 2);
        m_PropertiesPane.add(m_PositionXTextField, 1, 2);

        m_PropertiesPane.add(new Label("Y"), 0, 3);
        m_PropertiesPane.add(m_PositionYTextField, 1, 3);

        m_PropertiesPane.add(new Label("Z"), 0, 4);
        m_PropertiesPane.add(m_PositionZTextField, 1, 4);

        m_PropertiesPane.add(new Label("Radius"), 0, 5);
        m_PropertiesPane.add(m_RadiusTextField, 1, 5);

        m_PropertiesPane.add(new Label("Material"), 0, 6);
        m_PropertiesPane.add(m_MaterialsComboBox, 1, 6);
    }

    public ObservableList<SceneObject> GetSceneObjects() { return m_SceneObjectListView.getItems();}

    public void RefreshCombobox(Material material)
    {
        if(material == m_MaterialsComboBox.getSelectionModel().getSelectedItem())
        {
            m_MaterialsComboBox.getSelectionModel().clearSelection();
            m_MaterialsComboBox.getSelectionModel().select(material);
        }
    }

    public void RemoveLeftoverMaterials(int removedMaterialIndex)
    {
        GetSceneObjects().forEach((SceneObject sceneObject) ->
        {
            if(sceneObject.MaterialIndex > removedMaterialIndex)
            {
                // We removed material somewhere in the middle, so we shift indices, so they still point to appropriate material
                sceneObject.MaterialIndex--;
                OnSceneObjectValueChangeNative(sceneObject);
            }
            else if(sceneObject.MaterialIndex == removedMaterialIndex)
            {
                if(m_SelectedObject != null)
                {
                    m_MaterialsComboBox.setValue(m_UI.GetMaterialEditorPanel().GetDefaultMaterial());
                }

                System.out.println("SET DEFAULT MATERIAL");
                // Set it to default material
                sceneObject.MaterialIndex = 0;
                OnSceneObjectValueChangeNative(sceneObject);
            }
        });
    }

    private native void OnSceneObjectAddNative(SceneObject object);
    private native void OnSceneObjectRemoveNative(SceneObject object);
    private native void OnSceneObjectValueChangeNative(SceneObject object);
}
