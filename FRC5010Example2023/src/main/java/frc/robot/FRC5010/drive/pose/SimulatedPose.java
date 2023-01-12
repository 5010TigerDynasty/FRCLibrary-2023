// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.FRC5010.drive.pose;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.FRC5010.sensors.GenericGyro;

/** Add your docs here. */
public class SimulatedPose extends GenericPose {
    Pose2d pose = new Pose2d();
    public SimulatedPose(GenericGyro gyro) {
        super(gyro);
    }

    @Override
    public void resetEncoders() {
    }

    @Override
    public void updateVisionMeasurements(Pose2d robotPose, double imageCaptureTime) {
        pose = robotPose;
    }

    @Override
    public void updateLocalMeasurements() {
    }

    @Override
    public Pose2d getCurrentPose() {
        return pose;
    }

    @Override
    public void resetToPose(Pose2d pose) {
        this.pose = pose;
        this.gyro.setAngle(pose.getRotation().getDegrees());
    }
    
}
