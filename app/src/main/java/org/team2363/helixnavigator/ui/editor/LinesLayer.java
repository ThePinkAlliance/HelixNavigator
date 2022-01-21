package org.team2363.helixnavigator.ui.editor;

import java.util.List;

import org.team2363.helixnavigator.document.DocumentManager;
import org.team2363.helixnavigator.document.HDocument;
import org.team2363.helixnavigator.document.HPath;
import org.team2363.helixnavigator.document.waypoint.HWaypoint;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class LinesLayer implements PathLayer {

    private final DocumentManager documentManager;

    private final ObservableList<Node> children = FXCollections.observableArrayList();
    private final ObservableList<Node> childrenUnmodifiable = FXCollections.unmodifiableObservableList(children);
    private final ObservableList<LineView> lineViews = FXCollections.<LineView>observableArrayList();

    private final ChangeListener<? super HPath> onSelectedPathChanged = this::selectedPathChanged;
    private final ListChangeListener<? super HWaypoint> onWaypointsChanged = this::waypointsChanged;
    
    public LinesLayer(DocumentManager documentManager) {
        this.documentManager = documentManager;

        loadDocument(this.documentManager.getDocument());
        this.documentManager.documentProperty().addListener(this::documentChanged);
    }

    private void documentChanged(ObservableValue<? extends HDocument> currentDocument, HDocument oldDocument, HDocument newDocument) {
        unloadDocument(oldDocument);
        loadDocument(newDocument);
    }

    private void unloadDocument(HDocument oldDocument) {
        if (oldDocument != null) {
            unloadSelectedPath(oldDocument.getSelectedPath());
            oldDocument.selectedPathProperty().removeListener(onSelectedPathChanged);
        }
    }

    private void loadDocument(HDocument newDocument) {
        if (newDocument != null) {
            loadSelectedPath(newDocument.getSelectedPath());
            newDocument.selectedPathProperty().addListener(onSelectedPathChanged);
        }
    }

    private void selectedPathChanged(ObservableValue<? extends HPath> currentPath, HPath oldPath, HPath newPath) {
        unloadSelectedPath(oldPath);
        loadSelectedPath(newPath);
    }

    private void unloadSelectedPath(HPath oldPath) {
        if (oldPath != null) {
            lineViews.clear();
            children.clear();
            oldPath.getWaypoints().removeListener(onWaypointsChanged);
        }
    }

    private void loadSelectedPath(HPath newPath) {
        if (newPath != null) {
            updateWaypoints(newPath.getWaypoints());
            newPath.getWaypoints().addListener(onWaypointsChanged);
        }
    }

    private void waypointsChanged(ListChangeListener.Change<? extends HWaypoint> change) {
        updateWaypoints(change.getList());
    }

    private void updateWaypoints(List<? extends HWaypoint> list) {
        lineViews.clear();
        children.clear();
        for (int i = 0; i < list.size() - 1; i++) {
            LineView lineView = new LineView();
            linkLineView(lineView, list.get(i), list.get(i + 1));
            lineViews.add(i, lineView);
            children.add(i, lineView);
        }
    }

    private void linkLineView(LineView lineView, HWaypoint initialWaypoint, HWaypoint finalWaypoint) {
        lineView.initialPointXProperty().bind(initialWaypoint.xProperty());
        lineView.initialPointYProperty().bind(initialWaypoint.yProperty());
        lineView.finalPointXProperty().bind(finalWaypoint.xProperty());
        lineView.finalPointYProperty().bind(finalWaypoint.yProperty());
        lineView.pathAreaWidthProperty().bind(documentManager.pathAreaWidthProperty());
        lineView.pathAreaHeightProperty().bind(documentManager.pathAreaHeightProperty());
        lineView.zoomTranslateXProperty().bind(documentManager.getDocument().zoomTranslateXProperty());
        lineView.zoomTranslateYProperty().bind(documentManager.getDocument().zoomTranslateYProperty());
        lineView.zoomScaleProperty().bind(documentManager.getDocument().zoomScaleProperty());
        lineView.setOnMouseClicked(event -> {
            documentManager.getDocument().getSelectedPath().clearSelection();
        });
    }

    public ObservableList<Node> getChildren() {
        return childrenUnmodifiable;
    }
}