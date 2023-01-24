// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.FRC5010.robots;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.SPI;
import frc.robot.FRC5010.Vision.VisionPhotonMultiCam;
import frc.robot.FRC5010.constants.SwervePorts;
import frc.robot.FRC5010.drive.SwerveDrivetrain;
import frc.robot.FRC5010.drive.ThriftySwerveModule;
import frc.robot.FRC5010.mechanisms.GenericMechanism;
import frc.robot.FRC5010.mechanisms.SwerveDriveMech;
import frc.robot.FRC5010.robots.RobotFactory.Parts;
import frc.robot.FRC5010.sensors.AnalogInput5010;
import frc.robot.FRC5010.sensors.gyro.GenericGyro;
import frc.robot.FRC5010.sensors.gyro.NavXGyro;

/** Add your docs here. */
public class BabySwerve extends RobotType{
    public BabySwerve() {
        VisionPhotonMultiCam multiVision = new VisionPhotonMultiCam("Vision", 1);
        multiVision.addPhotonCamera("Arducam_OV9281_USB_Camera", 
          new Transform3d( // This describes the vector between the camera lens to the robot center on the ground
            new Translation3d(Units.inchesToMeters(7), 0, Units.inchesToMeters(16.75)), 
            new Rotation3d(0, Units.degreesToRadians(-20), 0)
          )
        );
        List<SwervePorts> swervePorts = new ArrayList<>();
        swervePorts.add(new SwervePorts(1, 2, 0));
        swervePorts.add(new SwervePorts(7, 8, 1));
        swervePorts.add(new SwervePorts(3, 4, 2));
        swervePorts.add(new SwervePorts(5, 6, 3));

        GenericGyro gyro = new NavXGyro(SPI.Port.kMXP);
        GenericMechanism drive = new SwerveDriveMech(multiVision, swervePorts);

        //new Drive(multiVision, gyro, Drive.Type.THRIFTY_SWERVE_DRIVE, 12, 12);
        robotParts.put(Parts.VISION, multiVision);
        robotParts.put(Parts.DRIVE, drive);
    }
}
