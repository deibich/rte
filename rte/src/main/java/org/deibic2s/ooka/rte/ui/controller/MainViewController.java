package org.deibic2s.ooka.rte.ui.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.deibic2s.ooka.rte.SimpleRTE;
import org.deibic2s.ooka.rte.logging.Level;
import org.deibic2s.ooka.rte.core.Component;
import org.deibic2s.ooka.rte.ui.views.LogView;
import org.deibic2s.ooka.rte.utils.ComponentState;
import org.deibic2s.ooka.rte.logging.RTELogCreator;
import org.deibic2s.ooka.rte.logging.RteLogger;

import java.io.File;

public class MainViewController {
    @FXML
    Button btnRestoreComponents;

    @FXML
    Button btnComponentStart;

    @FXML
    Button btnComponentStop;

    @FXML
    Button btnComponentRemove;

    @FXML
    Tab tabRTELog;

    @FXML
    TableColumn<Component, Integer> clmComponentID;

    @FXML
    TableColumn<Component, String> clmComponentName;

    @FXML
    TableColumn<Component, ComponentState> clmComponentState;

    @FXML
    TableView<Component> tableComponents;

    @FXML
    Button btnStartRTE;

    @FXML
    Button btnStopRTE;

    @FXML
    Button btnSelectComponent;

    private Stage myStage;
    private SimpleRTE myRTE;
    private Component currentSelectedComponent;
    private ObservableList<Component> availableComponents;

    public MainViewController(){}

    @FXML
    private void initialize(){
        RteLogger rteLogger = RTELogCreator.getInstance().getRTELogger("main");
        myRTE = new SimpleRTE(rteLogger);
        availableComponents = myRTE.getReadonlyComponentList();

        myRTE.isRTERunningProperty().addListener((observable, oldValue, newValue) -> {
            btnStartRTE.setDisable(newValue);
            btnStopRTE.setDisable(!newValue);
        });

        clmComponentID.setCellValueFactory(
                new PropertyValueFactory<Component, Integer>("id")
        );

        clmComponentName.setCellValueFactory(
                new PropertyValueFactory<Component, String>("name")
        );
        clmComponentState.setCellValueFactory(
                new PropertyValueFactory<Component, ComponentState>("componentState")
        );

        tableComponents.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        tableComponents.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null){
                currentSelectedComponent = newSelection;
                btnComponentRemove.setDisable(false );
                btnComponentStart.setDisable(false );
                btnComponentStop.setDisable(false );
            }else {
                btnComponentRemove.setDisable(true );
                btnComponentStart.setDisable(true );
                btnComponentStop.setDisable(true );

            }
        });


        tableComponents.setItems(availableComponents);
        Tab componentLogTab = new Tab("component");
        tabRTELog.getTabPane().getTabs().add(componentLogTab);
        
        
        generateLogTab(rteLogger, tabRTELog);
        generateLogTab(RTELogCreator.getInstance().getRTELogger("component"), componentLogTab);
        Tab eventstLogTab = new Tab("events");
        tabRTELog.getTabPane().getTabs().add(eventstLogTab);
        
        generateLogTab(RTELogCreator.getInstance().getRTELogger("events"), eventstLogTab);
    }

    public void onStartRTEPressed(ActionEvent actionEvent) {
        myRTE.startRTE();
    }

    public void onStopRTEPressed(ActionEvent actionEvent) {
        myRTE.stopRTE();
    }

    public void onSelectPressed(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JAR", "*.jar")
        );
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(myStage);
        if(file == null)
            return;

        if(!file.isFile() || !file.canRead())
            return;

        myRTE.deployComponent(file.getAbsolutePath());
    }

    public void onRestoreComponentspressed(ActionEvent actionEvent) {
        btnRestoreComponents.setDisable(true);
        myRTE.prepareRestore();
    }

    void setStage(Stage s){
        myStage = s;
    }

    private void generateLogTab(RteLogger l, Tab t){
        LogView logView = new LogView(l);
        logView.setPrefWidth(400);

        ChoiceBox<Level> filterLevel = new ChoiceBox<>(
                FXCollections.observableArrayList(
                        Level.values()
                )
        );
        filterLevel.getSelectionModel().select(Level.DEBUG);
        logView.filterLevelProperty().bind(
                filterLevel.getSelectionModel().selectedItemProperty()
        );

        ToggleButton showTimestamp = new ToggleButton("Show Timestamp");
        logView.showTimeStampProperty().bind(showTimestamp.selectedProperty());

        logView.tailProperty().setValue(true);
        Slider rate = new Slider(0.1, 60, 60);
        logView.refreshRateProperty().bind(rate.valueProperty());
        Label rateLabel = new Label();
        rateLabel.textProperty().bind(Bindings.format("Update: %.2f fps", rate.valueProperty()));
        rateLabel.setStyle("-fx-font-family: monospace;");

        HBox controls = new HBox(
                10,
                filterLevel,
                showTimestamp
        );
        controls.setPadding(new Insets(14,0,0,0));
        controls.setMinHeight(HBox.USE_PREF_SIZE);

        VBox layout = new VBox(
                10,
                controls,
                logView
        );
        VBox.setVgrow(logView, Priority.ALWAYS);
        t.setContent(layout);
    }

    public void onComponentStartPressed(ActionEvent actionEvent) {
        btnRestoreComponents.setDisable(true);
        if(currentSelectedComponent != null){
            myRTE.startComponent(currentSelectedComponent.getId());
        }
    }

    public void onComponentStopPressed(ActionEvent actionEvent) {
        if(currentSelectedComponent != null){
            myRTE.stopComponent(currentSelectedComponent.getId());
        }
    }

    public void onComponentRemovePressed(ActionEvent actionEvent) {
        if(currentSelectedComponent != null){
            myRTE.removeComponent(currentSelectedComponent.getId());
        }
    }
}
