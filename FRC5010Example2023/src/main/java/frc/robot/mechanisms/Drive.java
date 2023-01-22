// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.mechanisms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Robot;
import frc.robot.FRC5010.Controller;
import frc.robot.FRC5010.GenericMechanism;
import frc.robot.FRC5010.VisionSystem;
import frc.robot.FRC5010.commands.DefaultDriveCommand;
import frc.robot.FRC5010.constants.Persisted;
import frc.robot.FRC5010.constants.RobotConstantsDef;
import frc.robot.FRC5010.drive.DifferentialDrivetrain;
import frc.robot.FRC5010.drive.DrivetrainPoseEstimator;
import frc.robot.FRC5010.drive.GenericDrivetrain;
import frc.robot.FRC5010.motors.MotorController5010;
import frc.robot.FRC5010.motors.MotorFactory;
import frc.robot.FRC5010.sensors.GenericGyro;
import frc.robot.FRC5010.sensors.NavXGyro;

/** Add your docs here. */
public class Drive extends GenericMechanism {
    private VisionSystem vision;
    private DrivetrainPoseEstimator poseEstimator;
    private GenericDrivetrain drivetrain;
    private GenericGyro gyro;
    private double trackWidth;
    private double wheelBase;
    private Command defaultDriveCommand;
    private String type;

    public static class Type {
        public static final String DIFF_DRIVE = "DifferentialDrive";
        public static final String THRIFTY_SWERVE_DRIVE = "ThriftySwerveDrive";
        public static final String MK4_SWERVE_DRIVE = "MK4SwerveDrive";
        public static final String MK4I_SWERVE_DRIVE = "MK4ISwerveDrive";
    }

    // Examples of how to use a persisted constants
    // These can live in specific constants files, however
    private static Persisted<Integer> driveVisualH;
    private static Persisted<Integer> driveVisualV;

    public Drive(VisionSystem visionSystem, GenericGyro gyro, String type, double trackWidth, double wheelBase) {
        this.vision = visionSystem;
        this.gyro = gyro;
        this.trackWidth = trackWidth;
        this.wheelBase = wheelBase;
        driveVisualH = new Persisted<>(RobotConstantsDef.DRIVE_VISUAL_H, 60);
        driveVisualV = new Persisted<>(RobotConstantsDef.DRIVE_VISUAL_V, 60);
        mechVisual = new Mechanism2d(driveVisualH.getInteger(), driveVisualV.getInteger());
        SmartDashboard.putData("Drivetrain Visual", mechVisual);

        initRealOrSim();
    }

    @Override
    protected void initRealOrSim() {
        switch(type) {
            case Type.DIFF_DRIVE: {
                initializeDifferentialDrive();
                break;
            }
            case Type.THRIFTY_SWERVE_DRIVE: {
                break;
            }
            case Type.MK4_SWERVE_DRIVE: {
                break;
            }
            case Type.MK4I_SWERVE_DRIVE: {
                break;
            }
            default: {
                break;
            }
        }
    }

    public void setupDefaultCommands() {
        // Handle real or simulation case for default commands
        if (Robot.isReal()) {

        } else {
            
        }
        drivetrain.setDefaultCommand(defaultDriveCommand);
    }

    @Override
    public void configureButtonBindings(Controller driver, Controller operator) {
        // If there needs to be some commands that are real or simulation only use this
        if (Robot.isReal()) {

        } else {
            
        }

        // Example of setting up axis for driving omnidirectional
        driver.setLeftXAxis(driver.createLeftXAxis()
            .negate().deadzone(0.07).limit(1).rate(2).cubed());
        driver.setLeftYAxis(driver.createLeftYAxis()
            .negate().deadzone(0.07).limit(1).rate(2).cubed());
        driver.setRightXAxis(driver.createRightXAxis()
            .negate().deadzone(0.07).limit(1).rate(4).cubed());
        // Put commands that can be both real and simulation afterwards

        defaultDriveCommand = new DefaultDriveCommand(drivetrain, 
            () -> driver.getLeftYAxis(), () -> driver.getLeftXAxis(), () -> driver.getRightXAxis());
    }

    private void initializeDifferentialDrive() {
        MotorController5010 template = MotorFactory.DriveTrainMotor(MotorFactory.NEO(1));
        List<Integer> motorPorts = new ArrayList<>();
        
        // This assumes ports 1 & 2 are left and 3 & 4 are right
        // This is just an example of how to put a sequence of numbers into a list
        motorPorts.addAll(IntStream.rangeClosed(1, 4).boxed().collect(Collectors.toList()));

        drivetrain = new DifferentialDrivetrain(template, motorPorts, gyro, vision, mechVisual, trackWidth);
    }
}
