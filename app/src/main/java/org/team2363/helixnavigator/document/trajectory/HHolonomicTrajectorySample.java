package org.team2363.helixnavigator.document.trajectory;

import org.team2363.helixtrajectory.HolonomicTrajectorySample;

import com.jlbabilino.json.DeserializedJSONConstructor;
import com.jlbabilino.json.DeserializedJSONObjectValue;
import com.jlbabilino.json.JSONDeserializable;
import com.jlbabilino.json.JSONEntry.JSONType;
import com.jlbabilino.json.JSONSerializable;
import com.jlbabilino.json.SerializedJSONObjectValue;

@JSONSerializable(JSONType.OBJECT)
@JSONDeserializable({JSONType.OBJECT})
public class HHolonomicTrajectorySample {

    @SerializedJSONObjectValue(key = "ts")
    public final double ts;
    @SerializedJSONObjectValue(key = "x")
    public final double x;
    @SerializedJSONObjectValue(key = "y")
    public final double y;
    @SerializedJSONObjectValue(key = "heading")
    public final double heading;
    @SerializedJSONObjectValue(key = "vx")
    public final double vx;
    @SerializedJSONObjectValue(key = "vy")
    public final double vy;
    @SerializedJSONObjectValue(key = "omega")
    public final double omega;

    @DeserializedJSONConstructor
    public HHolonomicTrajectorySample(
            @DeserializedJSONObjectValue(key = "ts") double ts,
            @DeserializedJSONObjectValue(key = "x") double x,
            @DeserializedJSONObjectValue(key = "y") double y,
            @DeserializedJSONObjectValue(key = "heading") double heading,
            @DeserializedJSONObjectValue(key = "vx") double vx,
            @DeserializedJSONObjectValue(key = "vy") double vy,
            @DeserializedJSONObjectValue(key = "omega") double omega) {
        this.ts = ts;
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.vx = vx;
        this.vy = vy;
        this.omega = omega;
    }

    public static HHolonomicTrajectorySample interpolate(HHolonomicTrajectorySample a, HHolonomicTrajectorySample b, double ts) {
        double ratioA = (b.ts - ts) / (b.ts - a.ts);
        double ratioB = 1.0 - ratioA;
        double x = ratioA * a.x + ratioB * b.x;
        double y = ratioA * a.y + ratioB * b.y;
        double heading = ratioA * a.heading + ratioB * b.heading;
        double vx = ratioA * a.vx + ratioB * b.vx;
        double vy = ratioA * a.vy + ratioB * b.vy;
        double omega = ratioA * a.omega + ratioB * b.omega;
        return new HHolonomicTrajectorySample(ts, x, y, heading, vx, vy, omega);
    }

    public static HHolonomicTrajectorySample fromTrajectorySample(HolonomicTrajectorySample sample) {
        return new HHolonomicTrajectorySample(sample.timestamp(), sample.x(), sample.y(), sample.heading(),
                sample.velocityX(), sample.velocityY(), sample.angularVelocity());
    }
}