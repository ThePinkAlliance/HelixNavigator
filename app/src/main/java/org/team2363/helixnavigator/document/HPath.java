package org.team2363.helixnavigator.document;

import java.util.ArrayList;
import java.util.List;

import org.team2363.helixnavigator.document.obstacle.HObstacle;
import org.team2363.helixnavigator.document.obstacle.HPolygonObstacle;
import org.team2363.helixnavigator.document.obstacle.HPolygonPoint;
import org.team2363.helixnavigator.document.timeline.HCustomWaypoint;
import org.team2363.helixnavigator.document.timeline.HHardWaypoint;
import org.team2363.helixnavigator.document.timeline.HInitialGuessWaypoint;
import org.team2363.helixnavigator.document.timeline.HSoftWaypoint;
import org.team2363.helixnavigator.document.timeline.HWaypoint;
import org.team2363.helixtrajectory.HolonomicPath;
import org.team2363.helixtrajectory.HolonomicWaypoint;
import org.team2363.helixtrajectory.InitialGuessPoint;
import org.team2363.helixtrajectory.Obstacle;

import com.jlbabilino.json.DeserializedJSONConstructor;
import com.jlbabilino.json.DeserializedJSONObjectValue;
import com.jlbabilino.json.DeserializedJSONTarget;
import com.jlbabilino.json.InvalidJSONTranslationConfiguration;
import com.jlbabilino.json.JSONDeserializable;
import com.jlbabilino.json.JSONEntry.JSONType;
import com.jlbabilino.json.JSONSerializable;
import com.jlbabilino.json.JSONSerializer;
import com.jlbabilino.json.JSONSerializerException;
import com.jlbabilino.json.SerializedJSONObjectValue;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.transform.Transform;

@JSONSerializable(JSONType.OBJECT)
@JSONDeserializable({JSONType.OBJECT})
public class HPath {

    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final ObservableList<HWaypoint> timeline = FXCollections.<HWaypoint>observableArrayList();
    private final HSelectionModel<HWaypoint> timelineSelectionModel;
    private final ObservableList<HObstacle> obstacles = FXCollections.<HObstacle>observableArrayList();
    private final HSelectionModel<HObstacle> obstaclesSelectionModel;
    private final ReadOnlyBooleanWrapper inPolygonPointMode = new ReadOnlyBooleanWrapper(this, "inPolygonPointMode", false);
    private final ReadOnlyObjectWrapper<HSelectionModel<HPolygonPoint>> polygonPointsSelectionModel = new ReadOnlyObjectWrapper<>(this, "polygonPointsSelectionModel", null);
    private final ReadOnlyObjectWrapper<HTrajectory> trajectory = new ReadOnlyObjectWrapper<HTrajectory>(this, "trajectory", null);

    @DeserializedJSONConstructor
    public HPath() {
        timelineSelectionModel = new HSelectionModel<>(timeline);
        obstaclesSelectionModel = new HSelectionModel<>(obstacles);
        timelineSelectionModel.getSelectedItems().addListener((ListChangeListener.Change<? extends HWaypoint> change) -> {
            updateInPolygonPointMode();
            updatePolygonPointsSelectionModel();
        });
        obstaclesSelectionModel.getSelectedItems().addListener((ListChangeListener.Change<? extends HObstacle> change) -> {
            updateInPolygonPointMode();
            updatePolygonPointsSelectionModel();
        });
    }

    private void updateInPolygonPointMode() {
        setInPolygonPointMode(
                timelineSelectionModel.getSelectedIndices().isEmpty()
                && obstaclesSelectionModel.getSelectedItems().size() == 1
                && obstaclesSelectionModel.getSelectedItems().get(0).isPolygon());
    }
    private void updatePolygonPointsSelectionModel() {
        if (getInPolygonPointMode()) {
            setPolygonPointsSelectionModel(new HSelectionModel<HPolygonPoint>(((HPolygonObstacle) obstaclesSelectionModel.getSelectedItems().get(0)).getPoints()));
        } else {
            setPolygonPointsSelectionModel(null);
        }
    }

    public void transformSelectedElementsRelative(Transform transform) {
        getTimelineSelectionModel().getSelectedItems().forEach(element -> element.transformRelative(transform));
        getObstaclesSelectionModel().getSelectedItems().forEach(element -> element.transformRelative(transform));
    }

    public void moveSelectedElementsRelative(double deltaX, double deltaY) {
        getTimelineSelectionModel().getSelectedItems().forEach(element -> element.translateRelative(deltaX, deltaY));
        getObstaclesSelectionModel().getSelectedItems().forEach(element -> element.translateRelative(deltaX, deltaY));
    }

    public void moveSelectedElementsRelative(double deltaX, double deltaY, HPathElement excludedElement) {
        getTimelineSelectionModel().getSelectedItems().forEach(element -> {
            if (element != excludedElement) {
                element.translateRelative(deltaX, deltaY);
            }
        });
        getObstaclesSelectionModel().getSelectedItems().forEach(element -> {
            if (element != excludedElement) {
                element.translateRelative(deltaX, deltaY);
            }
        });
    }

    public void moveSelectedPolygonPointsRelative(double deltaX, double deltaY, HPolygonPoint excludedPolygonPoint) {
        getPolygonPointsSelectionModel().getSelectedItems().forEach(element -> {
            if (element != excludedPolygonPoint) {
                element.translateRelative(deltaX, deltaY);
            }
        });
    }

    public void clearWaypointsSelection() {
        timelineSelectionModel.clearSelection();
    }
    public void clearObstaclesSelection() {
        obstaclesSelectionModel.clearSelection();
    }
    public void clearSelection() {
        clearWaypointsSelection();
        clearObstaclesSelection();
    }

    public void clearPolygonPointSelection() {
        if (getInPolygonPointMode()) {
            getPolygonPointsSelectionModel().clearSelection();
        }
    }

    public final StringProperty nameProperty() {
        return name;
    }

    // @DeserializedJSONTarget
    public final void setName(@DeserializedJSONObjectValue(key = "name") String value) {
        name.set(value);
    }

    // @SerializedJSONObjectValue(key = "name")
    public final String getName() {
        return name.get();
    }

    @DeserializedJSONTarget
    public final void setTimeline(@DeserializedJSONObjectValue(key = "timeline") List<? extends HWaypoint> newTimeline) {
        timeline.setAll(newTimeline);
    }

    @SerializedJSONObjectValue(key = "timeline")
    public final ObservableList<HWaypoint> getTimeline() {
        return timeline;
    }

    public final HSelectionModel<HWaypoint> getTimelineSelectionModel() {
        return timelineSelectionModel;
    }

    @DeserializedJSONTarget
    public final void setObstacles(@DeserializedJSONObjectValue(key = "obstacles") List<? extends HObstacle> newObstacles) {
        obstacles.setAll(newObstacles);
    }

    @SerializedJSONObjectValue(key = "obstacles")
    public final ObservableList<HObstacle> getObstacles() {
        return obstacles;
    }

    public final HSelectionModel<HObstacle> getObstaclesSelectionModel() {
        return obstaclesSelectionModel;
    }

    public final ReadOnlyBooleanProperty inPolygonPointModeProperty() {
        return inPolygonPointMode.getReadOnlyProperty();
    }

    private final void setInPolygonPointMode(boolean value) {
        inPolygonPointMode.set(value);
    }

    public final boolean getInPolygonPointMode() {
        return inPolygonPointMode.get();
    }

    public final ReadOnlyObjectProperty<HSelectionModel<HPolygonPoint>> polygonPointsSelectionModelProperty() {
        return polygonPointsSelectionModel.getReadOnlyProperty();
    }

    private final void setPolygonPointsSelectionModel(HSelectionModel<HPolygonPoint> value) {
        polygonPointsSelectionModel.set(value);
    }

    public final HSelectionModel<HPolygonPoint> getPolygonPointsSelectionModel() {
        return polygonPointsSelectionModel.get();
    }

    public final ReadOnlyObjectProperty<HTrajectory> trajectoryProperty() {
        return trajectory.getReadOnlyProperty();
    }

    // TODO: Make this private eventually
    public final void setTrajectory(HTrajectory value) {
        trajectory.set(value);
    }

    public final HTrajectory getTrajectory() {
        return trajectory.get();
    }

    public HolonomicPath toPath(List<Obstacle> obstacles) {
        List<HolonomicWaypoint> htWaypoints = new ArrayList<>();
        int i = 0;
        boolean foundFirstWaypoint = false;
        while (timeline.get(i).isInitialGuess()) {
            i++;
        }
        while (i < timeline.size()) {
            List<InitialGuessPoint> initialGuessPoints = new ArrayList<>();
            while (i < timeline.size() && timeline.get(i).isInitialGuess()) {
                initialGuessPoints.add(((HInitialGuessWaypoint) timeline.get(i)).toInitialGuessPoint());
                i++;
            }
            int waypointIndex = i;
            switch (timeline.get(waypointIndex).getWaypointType()) {
                case SOFT:
                    htWaypoints.add(((HSoftWaypoint) timeline.get(waypointIndex)).toWaypoint(initialGuessPoints, obstacles));
                    break;
                case HARD:
                    htWaypoints.add(((HHardWaypoint) timeline.get(waypointIndex)).toWaypoint(initialGuessPoints, obstacles));
                    break;
                case CUSTOM:
                    htWaypoints.add(((HCustomWaypoint) timeline.get(waypointIndex)).toWaypoint(initialGuessPoints, obstacles));
                    break;
                default:
                break;
            }
            if (!foundFirstWaypoint) {
                obstacles = List.of();
                foundFirstWaypoint = true;
            }
            i++;
        }
        return new HolonomicPath(htWaypoints);
    }

    @Override
    public String toString() {
        try {
            return JSONSerializer.serializeString(this);
        } catch (InvalidJSONTranslationConfiguration | JSONSerializerException e) {
            return "";
        }
    }
}